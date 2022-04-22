package org.eclipse.steady.kb;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


import java.io.File;

import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.core.util.CoreConfiguration;

public class Manager {

  class MyThreadExecutor extends ThreadPoolExecutor {
    public MyThreadExecutor(
        int PoolSize,
        int maxPoolSize,
        long keepAliveTime,
        TimeUnit unit,
        BlockingQueue<Runnable> workQueue) {
      super(PoolSize, maxPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void afterExecute(Runnable run, Throwable throw1) {

      super.afterExecute(run, throw1);

      // String vulnId = ((Import)run).getVulnId();
      // System.out.println("afterExecute()3");
      // System.out.println(vulnId);

      if (throw1 == null) {
        // Manager.addStatus(vulnId, VulnStatus.IMPORTED);
        Manager.addStatus();
      } else {
        System.out.println("encountered exception- " + throw1.getMessage());
        // Manager.addStatus(vulnId, VulnStatus.FAILED);
      }
    }
  }

  private static int imported = 0;

  public static void addStatus() { // String vulnId, VulnStatus vulnStatus) {
    imported += 1;
    // System.out.println("addStatus()");
    // System.out.println(vulnId);
    // vulnerabilitiesStatus.put(vulnId, vulnStatus);
    // System.out.println("addStatus()2");
  }

  private MyThreadExecutor executor =
      new MyThreadExecutor(16, 32, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
  // (ThreadPoolExecutor) Executors.newCachedThreadPool();
  // this.pool = Executors.newFixedThreadPool(_pool_size);

  enum VulnStatus {
    NOT_STARTED,
    PROCESSING,
    IMPORTED,
    FAILED
  }

  private static Map<String, VulnStatus> vulnerabilitiesStatus = new HashMap<String, VulnStatus>();

  public synchronized void start(
      String statementsPath, HashMap<String, Object> mapCommandOptionValues) {

    File statementsDir = new File(statementsPath);
    File[] subdirs = statementsDir.listFiles();

    mapCommandOptionValues.put(Import.OVERWRITE_OPTION, true); // Change this
    setUploadConfiguration(mapCommandOptionValues);

    for (File dir : subdirs) {
      String dirPath = dir.getPath();
      mapCommandOptionValues.put(Import.DIRECTORY_OPTION, dirPath);
      Import command = new Import(mapCommandOptionValues);
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
