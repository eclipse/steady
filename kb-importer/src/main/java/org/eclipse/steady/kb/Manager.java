package org.eclipse.steady.kb;

import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
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

import com.google.gson.Gson;

import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.util.DirWithFileSearch;
import org.eclipse.steady.backend.BackendConnector;

public class Manager {

  private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private ThreadPoolExecutor executor =
      // new MyThreadExecutor(16, 32, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
      (ThreadPoolExecutor) Executors.newCachedThreadPool();
  // (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

  private static Map<String, VulnStatus> vulnerabilitiesStatus = new HashMap<String, VulnStatus>();
  private static Set<String> newVulnerabilities = new LinkedHashSet<String>();

  // pairs of vulnId and reason for failure
  private static Map<String, String> failures = new HashMap<String, String>();

  Map<String, Lock> repoLocks = new HashMap<String, Lock>();
  
  private boolean isRunningStart;

  public enum VulnStatus {
    NOT_STARTED,
    STARTING,
    EXTRACTING,
    CLONING,
    IMPORTING,
    IMPORTED,
    FAILED_EXTRACT_OR_CLONE,
    SKIP_CLONE,
    FAILED_IMPORT_LIB,
    FAILED_IMPORT_VULN
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

  public void addFailure(String vulnId, String reason) {
    failures.put(vulnId, reason);
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

  public boolean getIsRunningStart() {
    return this.isRunningStart;
  }

  public synchronized void start(
    String statementsPath, HashMap<String, Object> mapCommandOptionValues) {
    this.isRunningStart = true;
    if (this.executor.isShutdown() || this.executor.isTerminated()) {
      this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }
    newVulnerabilities = new LinkedHashSet<String>();

    try {
      log.info("Updating kaybee...");
      kaybeeUpdate();
      log.info("Running kaybee pull...");
      kaybeePull();
    } catch (IOException | InterruptedException e) {
      log.error("Exception while performing update: " + e.getMessage());
      this.isRunningStart = false;
      return;
    }

    File statementsDir = new File(statementsPath);
    File[] subdirs = statementsDir.listFiles();

    setUploadConfiguration(mapCommandOptionValues);
    final DirWithFileSearch search = new DirWithFileSearch("statement.yaml");
    Set<Path> vulnDirsPaths = search.search(Paths.get(statementsDir.getPath()));
    for (Path dirPath : vulnDirsPaths) {
      File vulnDir = dirPath.toFile();
      log.info("Found vulnerability directory: " + vulnDir.getName());
      String vulnId = vulnDir.getName().toString();
      setVulnStatus(vulnId, VulnStatus.NOT_STARTED);

    }
    BackendConnector backendConnector = BackendConnector.getInstance();
    for (Path vulnDirPath : vulnDirsPaths) {
      File vulnDir = vulnDirPath.toFile();
      String vulnDirStr = vulnDirPath.toString();
      log.info("Initializing process for directory " + vulnDirPath);

      // It is necessary to copy the arguments to avoid concurrent modification
      HashMap<String, Object> args = new HashMap<String, Object>(mapCommandOptionValues);
      args.put(Import.DIRECTORY_OPTION, vulnDirStr);
      Import command = new Import(this, args, BackendConnector.getInstance());
      if (mapCommandOptionValues.containsKey(Import.SEQUENTIAL)) {
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
    this.isRunningStart = false;
  }

  private void setUploadConfiguration(HashMap<String, Object> args) {
    Object uploadConstruct = args.get(Import.UPLOAD_CONSTRUCT_OPTION);
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
    Process pullProcess = Runtime.getRuntime().exec("/kb-importer/data/kaybee pull -c /kb-importer/conf/kaybeeconf.yaml");
    pullProcess.waitFor();
    if (pullProcess.exitValue() != 0) {
      log.error("Kaybee pull failed");
    }
  }

  public void stop() {
    try {
      executor.shutdownNow();
      executor.awaitTermination(24, TimeUnit.HOURS);
      this.isRunningStart = false;
    } catch (InterruptedException e) {
      log.error("Process interrupted");
      log.error(e.getMessage());
    }
  }

  public void importSingleVuln(
    String vulnDirStr, HashMap<String, Object> mapCommandOptionValues, String vulnId) {

    // Should have a lock per vulnerability

    log.info("Initializing process for directory " + vulnDirStr);

    if (this.executor.isShutdown() || this.executor.isTerminated()) {
      this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    // It is necessary to copy the arguments to avoid concurrent modification
    HashMap<String, Object> args = new HashMap<String, Object>(mapCommandOptionValues);
    args.put(Import.DIRECTORY_OPTION, vulnDirStr);
    Import command = new Import(this, args, BackendConnector.getInstance());
    if (mapCommandOptionValues.containsKey(Import.SEQUENTIAL)) {
      command.run();
    } else {
      executor.submit(command);
    }
  }

  public String status() {
    HashMap<VulnStatus, Integer> statusCount = new HashMap<VulnStatus, Integer>();
    for (VulnStatus vulnStatus : new ArrayList<VulnStatus>(vulnerabilitiesStatus.values())) {
      if (!statusCount.containsKey(vulnStatus)) {
        statusCount.put(vulnStatus, 0);
      }
      statusCount.put(vulnStatus, statusCount.get(vulnStatus)+1);
    }
    HashMap<String, VulnStatus> newVulnStatus = new HashMap<String, VulnStatus>();
    for (String vulnId : newVulnerabilities) {
      newVulnStatus.put(vulnId, vulnerabilitiesStatus.get(vulnId));
    }
    HashMap<String, Object> statusMap = new HashMap<String, Object>();
    statusMap.put("count", statusCount);
    statusMap.put("new_vulnerabilities", newVulnerabilities);
    statusMap.put("failures", failures);
    String statusStr = new Gson().toJson(statusMap);
    return statusStr;
  }

}
