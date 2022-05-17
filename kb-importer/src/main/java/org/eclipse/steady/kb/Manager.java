package org.eclipse.steady.kb;

import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.util.DirWithFileSearch;
import org.eclipse.steady.backend.BackendConnector;

public class Manager {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private ThreadPoolExecutor executor =
      // new MyThreadExecutor(16, 32, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
      (ThreadPoolExecutor) Executors.newCachedThreadPool();
  // (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

  private static Map<String, VulnStatus> vulnerabilitiesStatus = new HashMap<String, VulnStatus>();

  Map<String, Lock> repoLocks = new HashMap<String, Lock>();

  public enum VulnStatus {
    NOT_STARTED,
    PROCESSING,
    DIFF_DONE,
    IMPORTED,
    FAILED,
    SKIP_CLONE,
    FAILED_IMPORT_LIB,
    FAILED_IMPORT_VULN,
    NO_FIXES
  }

  public void setVulnStatus(String vulnId, VulnStatus vulnStatus) {
    vulnerabilitiesStatus.put(vulnId, vulnStatus);
  }

  public VulnStatus getVulnStatus(String vulnId) {
    return vulnerabilitiesStatus.get(vulnId);
  }

  public void lockRepo(String repo) {
    //System.out.println("Lock: " + repo);
    if (!repoLocks.containsKey(repo)) {
      //System.out.println("no key: " + repo);
      repoLocks.put(repo, new ReentrantLock());
    }
    repoLocks.get(repo).lock();
    //System.out.println("Locked:" + repo);
  }

  public void unlockRepo(String repo) {
    //System.out.println("Unlock: " + repo);
    if (!repoLocks.containsKey(repo)) {
      //System.out.println("ERROR : Lock not found");
      return;
    }
    repoLocks.get(repo).unlock();
    //System.out.println("Unlocked: " + repo);
  }

  public synchronized void start(
    String statementsPath, HashMap<String, Object> mapCommandOptionValues) {
    
    try {
      log.info("Updating kaybee...");
      kaybeeUpdate();
      log.info("Running kaybee pull...");
      kaybeePull();
    } catch (IOException | InterruptedException e) {
      log.error("Exception while performing update: " + e.getMessage());
      return;
    }

    File statementsDir = new File(statementsPath);
    File[] subdirs = statementsDir.listFiles();

    setUploadConfiguration(mapCommandOptionValues);
    final DirWithFileSearch search = new DirWithFileSearch("statement.yaml");
    Set<Path> vulnDirsPaths = search.search(Paths.get(statementsDir.getPath()));
    for (Path dirPath : vulnDirsPaths) {
      File vulnDir = dirPath.toFile();

      System.out.println("vuln dir " + vulnDir.getName());
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
    } catch (InterruptedException e) {
      log.error("Process interrupted");
      log.error(e.getMessage());
    }
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

  public String status() {
    int not_started = 0;
    int imported = 0;
    int processing = 0;
    int diff_done = 0;
    int no_fixes = 0;
    int failed = 0;
    int failed_vuln = 0;
    int failed_lib = 0;
    int skip_clone = 0;
    for (VulnStatus vulnStatus : new ArrayList<VulnStatus>(vulnerabilitiesStatus.values())) {
      switch (vulnStatus) {
        case NOT_STARTED:
          not_started += 1;
          break;
        case IMPORTED:
          imported += 1;
          break;
        case PROCESSING:
          processing += 1;
          break;
        case DIFF_DONE:
          diff_done += 1;
          break;
        case NO_FIXES:
          no_fixes += 1;
          break;
        case FAILED_IMPORT_VULN:
          failed_vuln += 1;
          break;
        case FAILED_IMPORT_LIB:
          failed_lib += 1;
          break;
        case FAILED:
          failed += 1;
          break;
        case SKIP_CLONE:
          skip_clone += 1;
          break;
        default:
          break;
      }
    }
    String statusStr = 
        "\nnot_started: "
        + Integer.toString(not_started)
        + "\nextracting/cloning: "
        + Integer.toString(processing)
        + "\nimporting vuln/libraries: "
        + Integer.toString(diff_done)
        + "\nimported: "
        + Integer.toString(imported);
    if (no_fixes > 0) {
      statusStr += "\nno_fixes: "
          + Integer.toString(no_fixes);
    }
    if (failed > 0) {
      statusStr += "\nfailed extract/clone: "
          + Integer.toString(failed);
    }
    if (failed_vuln > 0) {
      statusStr += "\nfailed vuln: "
          + Integer.toString(failed_lib);
    }
    if (failed_lib > 0) {
      statusStr += "\nfailed lib: "
          + Integer.toString(failed_vuln); 
    }
    if (skip_clone > 0) {
      statusStr += "\nskip_clone: "
          + Integer.toString(skip_clone); 
    }
    return statusStr + "\n";
  }

}
