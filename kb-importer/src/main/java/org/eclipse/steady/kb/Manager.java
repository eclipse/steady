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

  final String kaybeeBinaryPath =
      VulasConfiguration.getGlobal()
          .getConfiguration()
          .getString("vulas.kb-importer.kaybeeBinaryPath");

  final String kaybeeConfPath =
      VulasConfiguration.getGlobal()
          .getConfiguration()
          .getString("vulas.kb-importer.kaybeeConfPath"); 


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

  /**
   * <p>Constructor for Manager.</p>
   *
   * @param backendConnector a {@link org.eclipse.steady.backend.BackendConnector} object
   */
  public Manager(BackendConnector backendConnector) {
    this.backendConnector = backendConnector;
    this.createNewExecutor();
  }

  /**
   * <p>createNewExecutor.</p>
   */
  public void createNewExecutor() {
    this.executor = // (ThreadPoolExecutor) Executors.newCachedThreadPool();
        (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
  }

  /**
   * <p>addNewVulnerability.</p>
   *
   * @param vulnId a {@link java.lang.String} object
   */
  public void addNewVulnerability(String vulnId) {
    newVulnerabilities.add(vulnId);
  }

  /**
   * <p>setVulnStatus.</p>
   *
   * @param vulnId a {@link java.lang.String} object
   * @param vulnStatus a {@link org.eclipse.steady.kb.Manager.VulnStatus} object
   */
  public void setVulnStatus(String vulnId, VulnStatus vulnStatus) {
    vulnerabilitiesStatus.put(vulnId, vulnStatus);
  }

  /**
   * <p>getVulnStatus.</p>
   *
   * @param vulnId a {@link java.lang.String} object
   * @return a {@link org.eclipse.steady.kb.Manager.VulnStatus} object
   */
  public VulnStatus getVulnStatus(String vulnId) {
    if (vulnerabilitiesStatus.containsKey(vulnId)) {
      return vulnerabilitiesStatus.get(vulnId);
    } else return null;
  }

  /**
   * <p>addFailure.</p>
   *
   * @param vulnId a {@link java.lang.String} object
   * @param e a {@link java.lang.Exception} object
   */
  public void addFailure(String vulnId, Exception e) {
    failures.put(vulnId, e);
  }

  /**
   * <p>lockRepo.</p>
   *
   * @param repo a {@link java.lang.String} object
   */
  public void lockRepo(String repo) {
    if (!repoLocks.containsKey(repo)) {
      repoLocks.put(repo, new ReentrantLock());
    }
    repoLocks.get(repo).lock();
  }

  /**
   * <p>unlockRepo.</p>
   *
   * @param repo a {@link java.lang.String} object
   */
  public void unlockRepo(String repo) {
    if (!repoLocks.containsKey(repo)) {
      return;
    }
    repoLocks.get(repo).unlock();
  }

  /**
   * <p>isRunningStart.</p>
   *
   * @return a boolean
   */
  public boolean isRunningStart() {
    return this.startIsRunning;
  }

  /**
   * <p>start.</p>
   *
   * @param statementsPath a {@link java.lang.String} object
   * @param mapCommandOptionValues a {@link java.util.HashMap} object
   */
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
      log.info("Copying statements folder...");
      copyStatements();
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
   *
   * @param statementsPath a {@link java.lang.String} object
   * @param mapCommandOptionValues a {@link java.util.HashMap} object
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

  /**
   * <p>identifyVulnerabilitiesToImport.</p>
   *
   * @param statementsPath a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
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

  /**
   * <p>startList.</p>
   *
   * @param statementsPath a {@link java.lang.String} object
   * @param mapCommandOptionValues a {@link java.util.HashMap} object
   * @param vulnIds a {@link java.util.List} object
   */
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
      ImportCommand command = new ImportCommand(this, args);
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

  /**
   * <p>kaybeeUpdate.</p>
   *
   * @throws java.io.IOException if any.
   * @throws java.lang.InterruptedException if any.
   */
  public void kaybeeUpdate() throws IOException, InterruptedException {
    Process updateProcess = Runtime.getRuntime().exec(kaybeeBinaryPath + " update --force");
    updateProcess.waitFor();
    if (updateProcess.exitValue() != 0) {
      log.error("Failed to update kaybee");
    }
  }

  /**
   * <p>kaybeePull.</p>
   *
   * @throws java.io.IOException if any.
   * @throws java.lang.InterruptedException if any.
   */
  public void kaybeePull() throws IOException, InterruptedException {
    String pullCommand = kaybeeBinaryPath + " pull -c " + kaybeeConfPath;
    System.out.println(pullCommand);
    Process pullProcess =
        Runtime.getRuntime()
            .exec(pullCommand);
    pullProcess.waitFor();
    if (pullProcess.exitValue() != 0) {
      log.error("Kaybee pull failed");
    }
  }

  /**
   * <p>copyStatements.</p>
   *
   * @throws java.io.IOException if any.
   * @throws java.lang.InterruptedException if any.
   */
  public void copyStatements() throws IOException, InterruptedException {
    Process copyProcess =
        Runtime.getRuntime()
            .exec("cp " + ImporterController.statementsKaybeePath + " " + ImporterController.statementsPath);
    copyProcess.waitFor();
    if (copyProcess.exitValue() != 0) {
      log.error("failed to copy statements from .kaybee directory");
    }
  }

  /**
   * <p>stop.</p>
   */
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

  /**
   * <p>importSingleVuln.</p>
   *
   * @param vulnDirStr a {@link java.lang.String} object
   * @param mapCommandOptionValues a {@link java.util.HashMap} object
   * @param vulnId a {@link java.lang.String} object
   */
  public void importSingleVuln(
      String vulnDirStr, HashMap<String, Object> mapCommandOptionValues, String vulnId) {

    log.info("Initializing process for directory " + vulnDirStr);

    // It is necessary to copy the arguments to avoid concurrent modification
    HashMap<String, Object> args = new HashMap<String, Object>(mapCommandOptionValues);
    args.put(ImportCommand.DIRECTORY_OPTION, vulnDirStr);
    ImportCommand command = new ImportCommand(this, args);
    command.run();
  }

  /**
   * <p>status.</p>
   *
   * @return a {@link java.lang.String} object
   */
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

  /**
   * <p>Getter for the field <code>backendConnector</code>.</p>
   *
   * @return a {@link org.eclipse.steady.backend.BackendConnector} object
   */
  public BackendConnector getBackendConnector() {
    return this.backendConnector;
  }
}
