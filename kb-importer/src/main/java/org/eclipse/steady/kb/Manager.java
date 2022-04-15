package org.eclipse.steady.kb;

import java.util.Map;
import java.util.HashMap;

import java.io.File;

import org.eclipse.steady.kb.Import;
import org.eclipse.steady.kb.command.Command;

class Manager {

    private ThreadPoolExecutor executor = 
        (ThreadPoolExecutor) Executors.newCachedThreadPool();
  
    enum VulnStatus {
        NOT_STARTED,
        PROCESSING,
        IMPORTED,
        FAILED
    }
    
    private Map<String, VulnStatus> vulnerabilitiesStatus = new HashMap<String, String>();

    public void start(String statementsPath,
        HashMap<String, Object> mapCommandOptionValues) {

        File statementsDir = new File(statementsPath);
        File[] subdirs = statementsDir.listFiles();

        setUploadConfiguration(mapCommandOptionValues);

        for (File dir : subdirs) {
            if (dir.isDirectory(){
                vulnerabilitiesStatus.put(dir.getName(), VulnStatus.NOT_STARTED);
            } 
        }
        for (File dir : vulnerabilitiesStatus.keySet()){
            //synchronized (vulnerabilitiesStatus.get(dir.getName())) {
                String dirPath = dir.getPath();
                mapCommandOptionValues.put(Import.DIRECTORY_OPTION, dirPath);
                Import import = new Import(mapCommandOptionValues);
                executor.execute(import);
            //}
        }
    }

    private void setUploadConfiguration() {

        Object uploadConstruct = args.get(UPLOAD_CONSTRUCT_OPTION);
        VulasConfiguration.getGlobal()
            .setProperty(
                CoreConfiguration.BACKEND_CONNECT,
                (uploadConstruct != null
                    ? CoreConfiguration.ConnectType.READ_WRITE.toString()
                    : CoreConfiguration.ConnectType.READ_ONLY.toString()));
    }

  /*public String status() {
      return "";
  }*/

}
