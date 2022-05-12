package org.eclipse.steady.kb;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;
import com.github.packageurl.MalformedPackageURLException;

import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.kb.task.ExtractOrClone;
import org.eclipse.steady.kb.task.ImportVulnerability;
import org.eclipse.steady.kb.task.ImportAffectedLibraries;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.model.Commit;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.shared.util.StopWatch;

public class Import implements Runnable {

  public static final String METADATA_JSON = "metadata.json";
  public static final String STATEMENT_YAML = "statement.yaml";
  public static final String SOURCE_TAR = "changed-source-code.tar.gz";

  public static final String UPLOAD_CONSTRUCT_OPTION = "u";
  public static final String OVERWRITE_OPTION = "o";
  public static final String DELETE_OPTION = "del";
  public static final String DIRECTORY_OPTION = "d";
  public static final String DELETE = "del";
  public static final String SEQUENTIAL = "seq";

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private StopWatch stopWatch = null;
  private Path vulnDir;
  private String vulnId;
  private BackendConnector backendConnector;
  private HashMap<String, Object> args;
  Manager manager;

  public Import(Manager manager, HashMap<String, Object> args) {
    this.manager = manager;
    this.backendConnector = BackendConnector.getInstance();
    this.vulnDir = Paths.get((String) args.get(DIRECTORY_OPTION));
    this.vulnId = vulnDir.getFileName().toString();
    this.args = args;
    this.stopWatch = new StopWatch(this.vulnId).start();
  }

  public String getVulnId() {
    return this.vulnId;
  }

  @Override
  public void run() {

    manager.setVulnStatus(vulnId, Manager.VulnStatus.PROCESSING);
    boolean bugExists = false;
    try {
      bugExists = this.backendConnector.isBugExisting(vulnId);
    } catch (BackendConnectionException e) {
      log.error("Can't connect to the Backend");
      return;
    }
    Boolean overwrite = false;
    if (args.containsKey(OVERWRITE_OPTION)) {
      overwrite = (Boolean) args.get(OVERWRITE_OPTION);
    }
    if (bugExists) {
      if (overwrite) {
        args.put(DELETE, true);
        log.info("Bug [{}] already exists in backend and will be overwritten", vulnId);
      } else {
        log.info("Bug [{}] already exists in backend, analysis will be skipped", vulnId);
        manager.setVulnStatus(vulnId, Manager.VulnStatus.IMPORTED);
        return;
      }
    } else {
        log.info("Bug [{}] does not exist in backend", vulnId);
    }

    String statementPath = findStatementPath();

    if (statementPath != null) {
      //System.out.println("d");
      Vulnerability vuln;
      try {
        // System.out.println("getFromYaml...");
        vuln = Metadata.getFromYaml(statementPath);
        // System.out.println("after getFromYaml");
      } catch (IOException e) {
        log.error("Error while reading Yaml file for [{}]", vulnId);
        return;
      }
      //System.out.println("e");
      if ((vuln.getCommits() == null || vuln.getCommits().size() == 0) 
          && (vuln.getArtifacts() == null || vuln.getArtifacts().size() == 0)) {
        log.warn("No fix commits or affected artifacts for vulnerability " + vuln.getVulnId());
        vuln.setCommits(new ArrayList<Commit>());
        //manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.NO_FIXES);
        //return;
      } else {
        ExtractOrClone extractOrClone =
            new ExtractOrClone(this.manager, vuln, new File(this.vulnDir.toString()));
        
        this.stopWatch.lap("ExtractOrClone");
        extractOrClone.execute();
      }
      
      if (manager.getVulnStatus(vuln.getVulnId()) != Manager.VulnStatus.FAILED
          && manager.getVulnStatus(vuln.getVulnId()) != Manager.VulnStatus.SKIP_CLONE) {
          //&& manager.getVulnStatus(vuln.getVulnId()) != Manager.VulnStatus.NO_FIXES) {
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.DIFF_DONE);
        ImportVulnerability importVulnerability = new ImportVulnerability();

        ImportAffectedLibraries importAffectedLibraries = new ImportAffectedLibraries();

        this.stopWatch.lap("ImportVulnerability");
        try {
          importVulnerability.execute(vuln, args, backendConnector);
        } catch (IOException | BackendConnectionException e) {
          manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED_IMPORT_VULN);
          e.printStackTrace();
        }
        this.stopWatch.lap("ImportAffectedLibraries");
        try {
          importAffectedLibraries.execute(vuln, args, backendConnector);
        } catch (IOException | MalformedPackageURLException | BackendConnectionException e) {
          manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED_IMPORT_LIB);
          e.printStackTrace();
        }
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.IMPORTED);
      }
      this.stopWatch.stop();
      log.info(vuln.getVulnId() + " StopWatch Runtime " + Long.toString(this.stopWatch.getRuntime()));
    }
  }

  public String findStatementPath() {
    // Review this function
    if (FileUtil.isAccessibleFile(vulnDir + File.separator + STATEMENT_YAML)) {
      return vulnDir + File.separator + STATEMENT_YAML;
    } else if (FileUtil.isAccessibleDirectory(vulnDir)) {
      File directory = new File(vulnDir.toString());
      File[] fList = directory.listFiles();
      if (fList != null) {
        for (File file : fList) {
          if (file.isDirectory()) {
            if (FileUtil.isAccessibleFile(
                file.getAbsolutePath() + File.separator + STATEMENT_YAML)) {
              return file.getAbsolutePath() + File.separator + STATEMENT_YAML;
            } else {
              Import.log.warn(
                  "Skipping {} as the directory does not contain statement.yaml file",
                  file.getAbsolutePath());
            }
          }
        }
      }
    } else {
      Import.log.error("Invalid directory {}", vulnDir);
    }
    return null;
  }
}
