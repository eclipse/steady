package org.eclipse.steady.kb;

import java.util.Map;
import java.util.HashMap;

import java.io.File;

import org.eclipse.steady.kb.command.Import;

class Manager {

  // private ThreadPoolExecutor ....;
  private Map<String, String> runningProcesses = new HashMap<String, String>(); // Lock ?

  public void start(String statementsPath) {

    File statementsDir = new File(statementsPath);
    System.out.println(statementsDir);
    File[] subdirs = statementsDir.listFiles();
    System.out.println(subdirs);

    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("o", false);
    args.put("v", false);
    for (File dir : subdirs) {
      if (dir.isDirectory()) {
        String dirPath = dir.getPath();
        args.put(Import.DIRECTORY_OPTION, dirPath);
        Import command = new Import();
        command.run(args);
      }
    }
  }


  /*public String status() {
      return "";
  }*/

}
