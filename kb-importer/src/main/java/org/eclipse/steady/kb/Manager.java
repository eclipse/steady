package org.eclipse.steady.kb;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.io.File;

import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.core.util.CoreConfiguration;

public class Manager {

  private static int imported = 0;

  public static void addStatus(String vulnId, VulnStatus vulnStatus) {
    imported += 1;
    System.out.println("addStatus()");
    System.out.println(vulnId);
    vulnerabilitiesStatus.put(vulnId, vulnStatus);
    System.out.println("addStatus()2");
  }

  private ThreadPoolExecutor executor =
      //new MyThreadExecutor(16, 32, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
      //(ThreadPoolExecutor) Executors.newCachedThreadPool();
      (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

  enum VulnStatus {
    NOT_STARTED,
    PROCESSING,
    IMPORTED,
    FAILED
  }

  private static Map<String, VulnStatus> vulnerabilitiesStatus = new HashMap<String, VulnStatus>();
  // synchronized Map<String, Lock> reposInProcess = new HashMap<String, Lock>();
  Map<String, Lock> repoLocks = new HashMap<String, Lock>();
  

  //private synchronized Set<String> vulnDone;

  /*public synchronized boolean isRepoInProcess(String repo) {
    return reposInProcess.get(repo);
  }*/

  public void start(String repo) {
   // reposInProcess.put(repo, true);
    if (!repoLocks.containsKey(repo)){
      repoLocks.put(repo, new ReentrantLock());
    }
    repoLocks.get(repo).lock();
  }

  public void complete(String repo) {
    if (!repoLocks.containsKey(repo)) {
      System.out.println("ERROR : Lock not found");
      return;
    }
    repoLocks.get(repo).unlock();
  }
  //HashMap<String, Set> all_vulns =

  public synchronized void start(
      String statementsPath, HashMap<String, Object> mapCommandOptionValues) {

    File statementsDir = new File(statementsPath);
    File[] subdirs = statementsDir.listFiles();

    mapCommandOptionValues.put(Import.OVERWRITE_OPTION, true); // Change this
    setUploadConfiguration(mapCommandOptionValues);

    for (File dir : subdirs) {
      String dirPath = dir.getPath();
      mapCommandOptionValues.put(Import.DIRECTORY_OPTION, dirPath);
      Import command = new Import(this, mapCommandOptionValues);
      executor.submit(command);
    }
    for (int i = 0; i < 10; i++) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.out.println("Interrupted");
      }
      System.out.println(status());
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
    /*int imported = 0;
    int others = 0;
    for (VulnStatus vulnStatus : new ArrayList<VulnStatus>(vulnerabilitiesStatus.values())) {
        if (vulnStatus == VulnStatus.IMPORTED) {
            imported += 1;
        }
        else {
            others += 1;
        }
    }*/

    return "imported = "
        + Integer.toString(imported); // + "\nothers = " + Integer.toString(others);
  }
}
