package org.eclipse.steady.kb;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import java.io.File;

import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.core.util.CoreConfiguration;

public class Manager {

  public void setVulnStatus(String vulnId, VulnStatus vulnStatus) {
    vulnerabilitiesStatus.put(vulnId, vulnStatus);
  }

  public VulnStatus getVulnStatus(String vulnId) {
    return vulnerabilitiesStatus.get(vulnId);
  }

  private ThreadPoolExecutor executor =
      // new MyThreadExecutor(16, 32, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
      (ThreadPoolExecutor) Executors.newCachedThreadPool();
  // (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

  public enum VulnStatus {
    NOT_STARTED,
    PROCESSING,
    DIFF_DONE,
    IMPORTED,
    FAILED,
    FAILED_IMPORT_LIB,
    FAILED_IMPORT_VULN,
    NO_FIXES
  }

  private static Map<String, VulnStatus> vulnerabilitiesStatus = new HashMap<String, VulnStatus>();

  Map<String, Lock> repoLocks = new HashMap<String, Lock>();

  public void lockRepo(String repo) {
    System.out.println("Lock: " + repo);
    if (!repoLocks.containsKey(repo)) {
      System.out.println("no key: " + repo);
      repoLocks.put(repo, new ReentrantLock());
    }
    repoLocks.get(repo).lock();
    System.out.println("Locked:" + repo);
  }

  public void unlockRepo(String repo) {
    System.out.println("Unlock: " + repo);
    if (!repoLocks.containsKey(repo)) {
      System.out.println("ERROR : Lock not found");
      return;
    }
    repoLocks.get(repo).unlock();
    System.out.println("Unlocked: " + repo);
  }

  public synchronized void start(
    String statementsPath, HashMap<String, Object> mapCommandOptionValues) {

    File statementsDir = new File(statementsPath);
    File[] subdirs = statementsDir.listFiles();

    mapCommandOptionValues.put(Import.OVERWRITE_OPTION, true); // Change this
    setUploadConfiguration(mapCommandOptionValues);
    System.out.println(subdirs);
    for (File dir : subdirs) {
      if (dir.getName().startsWith("CVE")) {
        String vulnId = dir.getName().toString();
        setVulnStatus(vulnId, VulnStatus.NOT_STARTED);
      }
    }
    for (File dir : subdirs) {
      String dirPath = dir.getPath();
      System.out.println(dirPath);
      if (dir.getName().startsWith("CVE")) {
        // It is necessary to copy the arguments to avoid concurrent modification
        HashMap<String, Object> args = new HashMap<String, Object>(mapCommandOptionValues);
        args.put(Import.DIRECTORY_OPTION, dirPath);
        Import command = new Import(this, args);
        executor.submit(command);
        //command.run();
        System.out.println(status());
      }
    }
    while (true) {
      System.out.println(status());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
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

  public String status() {
    int not_started = 0;
    int imported = 0;
    int processing = 0;
    int diff_done = 0;
    int no_fixes = 0;
    int failed = 0;
    int failed_vuln = 0;
    int failed_lib = 0;
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
        default:
          break;
      }
    }
    return "imported = "
        + Integer.toString(imported)
        + "\nnot_started = "
        + Integer.toString(not_started)
        + "\nprocessing = "
        + Integer.toString(processing)
        + "\ndiff_done = "
        + Integer.toString(diff_done)
        + "\nno_commits = "
        + Integer.toString(no_fixes)
        + "\nfailed = "
        + Integer.toString(failed)
        + "\nfailed vuln = "
        + Integer.toString(failed_lib)
        + "\nfailed lib = "
        + Integer.toString(failed_vuln);
  }

}
