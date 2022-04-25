package org.eclipse.steady.kb;

import java.util.HashMap;
import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.Logger;

import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.kb.task.ExtractOrClone;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.kb.Manager;

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
    boolean bugExists = false;
    try {
      // System.out.println("before isBugExisting");
      bugExists = this.backendConnector.isBugExisting(vulnId);
      // System.out.println("after isBugExisting");
    } catch (BackendConnectionException e) {
      log.error("Can't connect to the Backend");
      return;
    }
    Boolean overwrite = (Boolean) args.get(OVERWRITE_OPTION);
    if (bugExists) {
      if (overwrite) {
        args.put(DELETE, true);
      } else {
        System.out.println("bugExists no Overwrite");
        log.info("Bug [{}] already exists in backend, analysis will be skipped", vulnId);
        return;
      }
    }
    String statementPath = findStatementPath();
    System.out.println("statementPath");
    System.out.println(statementPath);
    if (statementPath != null) {
      Vulnerability vuln;
      try {
        vuln = Metadata.getFromYaml(statementPath);
      } catch (IOException e) {
        log.error("Error while reading Yaml file for [{}]", vulnId);
        return;
      }
      // ImportVulnerability importVulnerability = new ImportVulnerability(vuln, args);
      // ImportAffectedLibraries importAffectedLibraries = new ImportAffectedLibraries(vuln, args);

      ExtractOrClone extractOrClone = new ExtractOrClone(this.manager);
      extractOrClone.execute(new File(this.vulnDir.toString()), vuln);
      // importVulnerability.execute();
      // importAffectedLibraries.execute();
    }
  }

  public String findStatementPath() {
    // TODO: Should also check for metadata.json?
    // Review this function
    if (FileUtil.isAccessibleFile(vulnDir + File.separator + STATEMENT_YAML)) {
      return vulnDir + File.separator + STATEMENT_YAML;
    }
    // Since there is one Import task per vulnerability, there is no need to loop over
    // subdirectories
    else if (FileUtil.isAccessibleDirectory(vulnDir)) {
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
                  "Skipping {} as the directory does not contain statement.yaml" + " file",
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
