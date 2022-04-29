package org.eclipse.steady.kb;

import java.util.HashMap;
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
import org.eclipse.steady.backend.BackendConnectionException;

public class Import implements Runnable {

  public static final String METADATA_JSON = "metadata.json";
  public static final String STATEMENT_YAML = "statement.yaml";
  public static final String SOURCE_TAR = "changed-source-code.tar.gz";

  public static final String UPLOAD_CONSTRUCT_OPTION = "u";
  public static final String OVERWRITE_OPTION = "o";
  public static final String DELETE_OPTION = "del";
  public static final String DIRECTORY_OPTION = "d";
  public static final String DELETE = "del";

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();
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
    System.out.println("a");
    Boolean overwrite = (Boolean) args.get(OVERWRITE_OPTION);
    if (bugExists) {
      if (overwrite) {
        args.put(DELETE, true);
      } else {
        log.info("Bug [{}] already exists in backend, analysis will be skipped", vulnId);
        return;
      }
    }
    System.out.println("b");
    String statementPath = findStatementPath();
    System.out.println("c");

    if (statementPath != null) {
      System.out.println("d");
      Vulnerability vuln;
      try {
        // System.out.println("getFromYaml...");
        vuln = Metadata.getFromYaml(statementPath);
        // System.out.println("after getFromYaml");
      } catch (IOException e) {
        log.error("Error while reading Yaml file for [{}]", vulnId);
        return;
      }
      System.out.println("e");
      if (vuln.getCommits() == null || vuln.getCommits().size() == 0) {
        log.error("No fix commits for vulnerability " + vuln.getVulnId());
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.NO_FIXES);
        return;
      }

      ExtractOrClone extractOrClone =
          new ExtractOrClone(this.manager, vuln, new File(this.vulnDir.toString()));
      extractOrClone.execute();

      if (manager.getVulnStatus(vuln.getVulnId()) != Manager.VulnStatus.FAILED) {
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.DIFF_DONE);
        ImportVulnerability importVulnerability = new ImportVulnerability();
        ImportAffectedLibraries importAffectedLibraries = new ImportAffectedLibraries();
        try {
          importVulnerability.execute(vuln, args, backendConnector);
        } catch (IOException | BackendConnectionException e) {
          manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED_IMPORT_VULN);
          e.printStackTrace();
        }
        try {
          importAffectedLibraries.execute(vuln, args, backendConnector);
        } catch (IOException | MalformedPackageURLException | BackendConnectionException e) {
          manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED_IMPORT_LIB);
          e.printStackTrace();
        }
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.IMPORTED);
      }
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
      System.out.println("statement not found");
      Import.log.error("Invalid directory {}", vulnDir);
    }
    return null;
  }
}
