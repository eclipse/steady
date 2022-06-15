/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.kb;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

import org.apache.http.conn.HttpHostConnectException;
import com.google.gson.Gson;

import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.util.DirWithFileSearch;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.shared.util.StopWatch;
/**
 * Creates and executes threads for processing each vulnerability.
 * Keeps track of the state of each one of them.
 */
public class Manager {

  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private ThreadPoolExecutor executor;

  private static Map<String, VulnStatus> vulnerabilitiesStatus = new HashMap<String, VulnStatus>();
  private static Set<String> newVulnerabilities = new LinkedHashSet<String>();

  // pairs of vulnId and reason for failure
  private static Map<String, Exception> failures = new HashMap<String, Exception>();

  Map<String, Lock> repoLocks = new HashMap<String, Lock>();

  private boolean startIsRunning;

  private BackendConnector backendConnector;
  private StopWatch stopWatch = null;

  public enum VulnStatus {
    NOT_STARTED,
    STARTING,
    EXTRACTING,
    CLONING,
    IMPORTING,
    IMPORTED,
    FAILED_EXTRACT_OR_CLONE,
    FAILED_CONNECTION,
    SKIP_CLONE,
    FAILED_IMPORT_LIB,
    FAILED_IMPORT_VULN
  }

  public Manager(BackendConnector backendConnector) {
    this.backendConnector = backendConnector;
    this.createNewExecutor();
  }

  public void createNewExecutor() {
    this.executor = // (ThreadPoolExecutor) Executors.newCachedThreadPool();
        (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
  }

  public void addNewVulnerability(String vulnId) {
    newVulnerabilities.add(vulnId);
  }

  public void setVulnStatus(String vulnId, VulnStatus vulnStatus) {
    vulnerabilitiesStatus.put(vulnId, vulnStatus);
  }

  public VulnStatus getVulnStatus(String vulnId) {
    if (vulnerabilitiesStatus.containsKey(vulnId)) {
      return vulnerabilitiesStatus.get(vulnId);
    } else return null;
  }

  public void addFailure(String vulnId, Exception e) {
    failures.put(vulnId, e);
  }

  public void lockRepo(String repo) {
    if (!repoLocks.containsKey(repo)) {
      repoLocks.put(repo, new ReentrantLock());
    }
    repoLocks.get(repo).lock();
  }

  public void unlockRepo(String repo) {
    if (!repoLocks.containsKey(repo)) {
      return;
    }
    repoLocks.get(repo).unlock();
  }

  public boolean isRunningStart() {
    return this.startIsRunning;
  }

  public synchronized void start(
      String statementsPath, HashMap<String, Object> mapCommandOptionValues) {
    this.startIsRunning = true;

    this.stopWatch = new StopWatch("Manager started").start();
    newVulnerabilities = new LinkedHashSet<String>();

    try {
      log.info("Updating kaybee...");
      kaybeeUpdate();
      log.info("Running kaybee pull...");
      kaybeePull();
    } catch (IOException | InterruptedException e) {
      log.error("Exception while performing update: " + e.getMessage());
      this.startIsRunning = false;
      return;
    }
    setUploadConfiguration(mapCommandOptionValues);
    List<String> vulnIds = this.identifyVulnerabilitiesToImport(statementsPath);
    startList(statementsPath, mapCommandOptionValues, vulnIds);
    retryFailed(statementsPath, mapCommandOptionValues);
    this.stopWatch.stop();
    this.startIsRunning = false;
    log.info("Manager StopWatch Runtime " + Long.toString(this.stopWatch.getRuntime()));
  }

  /**
   * Keep retrying vulnerabilities that failed due to the high amount of requests.
   */
  public void retryFailed(String statementsPath, HashMap<String, Object> mapCommandOptionValues) {

    List<String> failuresToRetry = new ArrayList<String>();
    while (true) {
      for (String vulnId : failures.keySet()) {
        if (failures.get(vulnId) instanceof BackendConnectionException
            || failures.get(vulnId) instanceof HttpHostConnectException) {
          failuresToRetry.add(vulnId);
        }
      }
      if (failuresToRetry.isEmpty()) {
        break;
      } else {
        log.info(
            "Retrying " + Integer.toString(failuresToRetry.size()) + " failed vulnerabilities");
        startList(statementsPath, mapCommandOptionValues, failuresToRetry);
      }
    }
  }

  public List<String> identifyVulnerabilitiesToImport(String statementsPath) {
    File statementsDir = new File(statementsPath);
    List<String> vulnIds = new ArrayList<String>();

    final DirWithFileSearch search = new DirWithFileSearch("statement.yaml");
    Set<Path> vulnDirsPaths = search.search(Paths.get(statementsDir.getPath()));
    for (Path dirPath : vulnDirsPaths) {
      File vulnDir = dirPath.toFile();
      log.info("Found vulnerability directory: " + vulnDir.getName());
      String vulnId = vulnDir.getName().toString();
      setVulnStatus(vulnId, VulnStatus.NOT_STARTED);
      vulnIds.add(vulnId);
    }
    return vulnIds;
  }

  public synchronized void startList(
      String statementsPath, HashMap<String, Object> mapCommandOptionValues, List<String> vulnIds) {

    if (this.executor.isShutdown() || this.executor.isTerminated()) {
      this.createNewExecutor();
    }

    failures = new HashMap<String, Exception>();

    for (String vulnId : vulnIds) {
      Path vulnDirPath = Paths.get(statementsPath, vulnId);
      String vulnDirStr = vulnDirPath.toString();
      log.info("Initializing process for directory " + vulnDirPath);

      // It is necessary to copy the arguments to avoid concurrent modification
      HashMap<String, Object> args = new HashMap<String, Object>(mapCommandOptionValues);
      args.put(ImportCommand.DIRECTORY_OPTION, vulnDirStr);
      ImportCommand command = new ImportCommand(this, args, this.backendConnector);
      if (mapCommandOptionValues.containsKey(ImportCommand.SEQUENTIAL)) {
        command.run();
      } else {
        executor.submit(command);
      }
    }
    try {
      executor.shutdown();
      executor.awaitTermination(24, TimeUnit.HOURS);
      log.info("Finished importing vulnerabilities");
    } catch (InterruptedException e) {
      log.error("Process interrupted");
      log.error(e.getMessage());
    }
  }

  private void setUploadConfiguration(HashMap<String, Object> args) {
    Object uploadConstruct = args.get(ImportCommand.UPLOAD_CONSTRUCT_OPTION);
    VulasConfiguration.getGlobal()
        .setProperty(
            CoreConfiguration.BACKEND_CONNECT,
            (uploadConstruct != null
                ? CoreConfiguration.ConnectType.READ_WRITE.toString()
                : CoreConfiguration.ConnectType.READ_ONLY.toString()));
  }

  public void kaybeeUpdate() throws IOException, InterruptedException {
    // TODO : write directory as option/property
    Process updateProcess = Runtime.getRuntime().exec("/kb-importer/data/kaybee update --force");
    updateProcess.waitFor();
    if (updateProcess.exitValue() != 0) {
      log.error("Failed to update kaybee");
    }
  }

  public void kaybeePull() throws IOException, InterruptedException {
    // TODO : write directories as option/property
    Process pullProcess =
        Runtime.getRuntime()
            .exec("/kb-importer/data/kaybee pull -c /kb-importer/conf/kaybeeconf.yaml");
    pullProcess.waitFor();
    if (pullProcess.exitValue() != 0) {
      log.error("Kaybee pull failed");
    }
  }

  public void stop() {
    try {
      executor.shutdownNow();
      executor.awaitTermination(24, TimeUnit.HOURS);
      this.startIsRunning = false;
    } catch (InterruptedException e) {
      log.error("Process interrupted");
      log.error(e.getMessage());
    }
  }

  public void importSingleVuln(
      String vulnDirStr, HashMap<String, Object> mapCommandOptionValues, String vulnId) {

    log.info("Initializing process for directory " + vulnDirStr);

    // It is necessary to copy the arguments to avoid concurrent modification
    HashMap<String, Object> args = new HashMap<String, Object>(mapCommandOptionValues);
    args.put(ImportCommand.DIRECTORY_OPTION, vulnDirStr);
    ImportCommand command = new ImportCommand(this, args, this.backendConnector);
    command.run();
  }

  public String status() {
    HashMap<VulnStatus, Integer> statusCount = new HashMap<VulnStatus, Integer>();
    for (VulnStatus vulnStatus : new ArrayList<VulnStatus>(vulnerabilitiesStatus.values())) {
      if (!statusCount.containsKey(vulnStatus)) {
        statusCount.put(vulnStatus, 0);
      }
      statusCount.put(vulnStatus, statusCount.get(vulnStatus) + 1);
    }
    HashMap<String, VulnStatus> newVulnStatus = new HashMap<String, VulnStatus>();
    for (String vulnId : newVulnerabilities) {
      newVulnStatus.put(vulnId, vulnerabilitiesStatus.get(vulnId));
    }
    HashMap<String, Object> statusMap = new HashMap<String, Object>();
    statusMap.put("count", statusCount);
    statusMap.put("new_vulnerabilities", newVulnerabilities);
    HashMap<String, String> failureReasons = new HashMap<String, String>();
    for (String vulnId : failures.keySet()) {
      failureReasons.put(vulnId, failures.get(vulnId).toString());
    }
    statusMap.put("failures", failureReasons);
    String statusStr = new Gson().toJson(statusMap);
    return statusStr;
  }
}
