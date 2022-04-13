package org.eclipse.steady.kb;

import java.util.Map;
import java.util.HashMap;

import java.io.File;

import org.eclipse.steady.kb.command.Import;
import org.eclipse.steady.kb.command.Command;

class Manager {

  // private ThreadPoolExecutor ....;
  private Map<String, String> runningProcesses = new HashMap<String, String>(); // Lock ?

  public void start(String statementsPath,
    HashMap<String, Object> mapCommandOptionValues) {

    File statementsDir = new File(statementsPath);
    System.out.println(statementsDir);
    File[] subdirs = statementsDir.listFiles();
    System.out.println(subdirs);

    for (File dir : subdirs) {
      if (dir.isDirectory()) {
        String dirPath = dir.getPath();
        mapCommandOptionValues.put(Import.DIRECTORY_OPTION, dirPath);
        System.out.println("mapCommandOptionValues");
        System.out.println(mapCommandOptionValues);
        Import command = new Import();
        command.run(mapCommandOptionValues);
      }
    }
  }


  /*public String status() {
      return "";
  }*/

}
