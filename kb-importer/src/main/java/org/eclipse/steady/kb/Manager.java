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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.conn.HttpHostConnectException;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.util.DirWithFileSearch;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.ProcessWrapper;
import org.eclipse.steady.shared.util.ProcessWrapperException;
import org.eclipse.steady.shared.util.StopWatch;
import org.eclipse.steady.shared.util.ThreadUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;

import com.google.gson.Gson;
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

  /**
   * The folder into which kaybee pulls the statements.
   * Should be changed as soon as kaybee merge is properly implemented.
   */
  private static final String KAYBEE_STMTS_PATH = ".kaybee/repositories/github.com_sap.project-kb_vulnerability-data/statements";

  /**
   * The data folder inside kb-importer's Docker container.
   */
  private static final String IMPORT_STMTS_PATH =
    VulasConfiguration.getGlobal()
        .getConfiguration()
        .getString("vulas.kb-importer.statementsPath");
  
  private ThreadPoolExecutor executor;

  private static Map<String, VulnStatus> vulnerabilitiesStatus = new HashMap<String, VulnStatus>();

  private static Set<String> newVulnerabilities = new LinkedHashSet<String>();

  // pairs of vulnId and reason for failure
  private static Map<String, Exception> failures = new HashMap<String, Exception>();

  Map<String, Lock> repoLocks = new HashMap<String, Lock>();

  private StopWatch stopWatch = null;

  private Path tmpDir = null;

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
    FAILED_IMPORT_VULN,
    MALFORMED_INPUT
  }

  /**
   * <p>Constructor for Manager.</p>
   *
   * @param backendConnector a {@link org.eclipse.steady.backend.BackendConnector} object
   */
  public Manager() {
    this.createNewExecutor();
    try {
      this.tmpDir = FileUtil.createTmpDir("import");
    } catch (IOException e) {
      log.error("Error creating temp dir: " + e.getMessage());
    }
  }

  /**
   * <p>createNewExecutor.</p>
   */
  public void createNewExecutor() {
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(ThreadUtil.getNoThreads());
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
   * Returns true if an import is underway, false otherwise.
   *
   * @return a boolean
   */
  public boolean isRunningStart() {
    return this.stopWatch != null && this.stopWatch.isRunning();
  }

  /**
   * Calls kaybee and starts the import of all statements in the data folder.
   *
   * @param mapCommandOptionValues a {@link java.util.HashMap} object
   */
  public synchronized void start(HashMap<String, Object> mapCommandOptionValues) {

    newVulnerabilities = new LinkedHashSet<String>();

    this.stopWatch = new StopWatch("Import vulnerabilities").start();
    
    try {
      kaybeeUpdate();
      this.stopWatch.lap("Updated kaybee", true);

      kaybeePull();
      this.stopWatch.lap("Ran kaybee pull", true);
      
      // Normally, we would call 'kaybee merge", but since this functionality
      // has not been implemented yet, we simply copy the folder where the statements
      // have been pulled to to another folder
      FileUtil.copy(Paths.get(KAYBEE_STMTS_PATH), Paths.get(IMPORT_STMTS_PATH).getParent(), Paths.get(IMPORT_STMTS_PATH).getFileName(), StandardCopyOption.REPLACE_EXISTING);
      this.stopWatch.lap("Copied statements", true);

      setUploadConfiguration(mapCommandOptionValues);

      List<String> vulnIds = this.identifyVulnerabilitiesToImport(IMPORT_STMTS_PATH);
      startList(IMPORT_STMTS_PATH, mapCommandOptionValues, vulnIds);
      retryFailed(IMPORT_STMTS_PATH, mapCommandOptionValues);

      this.stopWatch.stop();
    } catch (Exception e) {
      this.stopWatch.stop(e);
    }
  }

  /**
   * Keeps retrying vulnerabilities that failed due to the high amount of requests.
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
            "Retrying [" + Integer.toString(failuresToRetry.size()) + "] failed vulnerabilities...");
        startList(statementsPath, mapCommandOptionValues, failuresToRetry);
      }
    }
  }

  /**
   * Searches in the given folder for directories containing a
   * <p>statements.yaml</p> file, which correspond to vulnerabilities being
   * imported later on.
   *
   * @param statementsPath a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  public List<String> identifyVulnerabilitiesToImport(String statementsPath) {
    final DirWithFileSearch search = new DirWithFileSearch("statement.yaml");
    Set<Path> vulnDirsPaths = search.search(Paths.get(statementsPath));
    List<String> vulnIds = new ArrayList<String>();
    for (Path dirPath : vulnDirsPaths) {
      String vulnId = dirPath.getFileName().toString();
      log.debug("Found directory [" + dirPath + "] for vulnerability [" + vulnId + "]");
      setVulnStatus(vulnId, VulnStatus.NOT_STARTED);
      vulnIds.add(vulnId);
    }
    return vulnIds;
  }

  /**
   * Creates an {@link ImportCommand} for every vulnerability comprised in the
   * given list. Depending on the presence of {@link ImportCommand.SEQUENTIAL}
   * in the keys of the given arguments, those import commands will be executed
   * sequentially are given to the thread pool executor
   * {@link Manager#executor}.
   *
   * @param statementsPath a {@link java.lang.String} refering to the parent
   * folder of the vulnerability folders to import
   * @param mapCommandOptionValues a {@link java.util.HashMap} with arguments,
   * incl. {@link ImportCommand.SEQUENTIAL}
   * @param vulnIds a {@link java.util.List} of vulnerabilities to be imported
   */
  public synchronized void startList(
      String statementsPath, HashMap<String, Object> mapCommandOptionValues, List<String> vulnIds) {

    if (this.executor.isShutdown() || this.executor.isTerminated()) {
      this.createNewExecutor();
    }

    failures = new HashMap<String, Exception>();

    // Loop vulnerabilities
    for (String vulnId : vulnIds) {
      Path vulnDirPath = Paths.get(statementsPath, vulnId);

      // Copy the arguments to avoid concurrent modification
      HashMap<String, Object> args = new HashMap<String, Object>(mapCommandOptionValues);
      args.put(ImportCommand.DIRECTORY_OPTION, vulnDirPath.toString());

      // Create the import command. Start right away or submit to executor.
      ImportCommand command = new ImportCommand(this, args);
      if (mapCommandOptionValues.containsKey(ImportCommand.SEQUENTIAL)) {
        command.run();
      } else {
        executor.submit(command);
      }
    }

    // Don't accept new vulns and wait for termination
    try {
      executor.shutdown();
      executor.awaitTermination(24, TimeUnit.HOURS);
    } catch (InterruptedException e) {
      log.error("Process interrupted: " + e.getMessage());
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
   * Runs 'kaybe update --force' to update the kaybee binary.
   *
   * @throws java.io.IOException if any.
   * @throws java.lang.InterruptedException if any.
   */
  public void kaybeeUpdate() throws ProcessWrapperException, InterruptedException {
    ProcessWrapper pw = new ProcessWrapper().setCommand(Paths.get(kaybeeBinaryPath), "update", "--force").setPath(this.tmpDir);
    Thread t = new Thread(pw, "kaybee-update");
    t.start();
    t.join();
  }

  /**
   * Runs 'kaybee pull -c <config>' to pull statements from the configured source repositories.
   *
   * @throws java.io.IOException if any.
   * @throws java.lang.InterruptedException if any.
   */
  public void kaybeePull() throws Exception {
    ProcessWrapper pw = new ProcessWrapper().setCommand(Paths.get(kaybeeBinaryPath), "pull", "-c", kaybeeConfPath).setPath(this.tmpDir);
    Thread t = new Thread(pw, "kaybee-pull");
    t.start();
    t.join();
  }

  /**
   * Stops all import threads and waits for their termination.
   */
  public void stop() {
    try {
      executor.shutdownNow();
      executor.awaitTermination(24, TimeUnit.HOURS);
    } catch (InterruptedException e) {
      log.error("Process interrupted: " + e.getMessage(), e);
    }
  }

  /**
   * Imports a single vulnerability whose statement.yaml is expected to be in
   * the correct folder.
   *
   * @param vulnDirStr a {@link java.lang.String} object
   * @param mapCommandOptionValues a {@link java.util.HashMap} object
   * @param vulnId a {@link java.lang.String} object
   */
  public void importSingleVuln(HashMap<String, Object> mapCommandOptionValues, String vulnId) {
    String dir = IMPORT_STMTS_PATH + File.separator + vulnId;
    log.info("Importing vulnerability [" + vulnId + "] from directory [" + dir + "]...");

    // It is necessary to copy the arguments to avoid concurrent modification
    HashMap<String, Object> args = new HashMap<String, Object>(mapCommandOptionValues);
    args.put(ImportCommand.DIRECTORY_OPTION, dir);

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
}
