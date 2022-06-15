/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
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

/**
 * Import data of a single vulnerability.
 * Calls 3 tasks sequentially: ExtractOrClone, ImportVulnerability and ImportAffectedLibraries.
 */
public class ImportCommand implements Runnable {

  public static final String METADATA_JSON = "metadata.json";
  public static final String STATEMENT_YAML = "statement.yaml";
  public static final String SOURCE_TAR = "changed-source-code.tar.gz";

  public static final String VERBOSE_OPTION = "v";
  public static final String UPLOAD_CONSTRUCT_OPTION = "u";
  public static final String SKIP_CLONE_OPTION = "u";
  public static final String OVERWRITE_OPTION = "o";
  public static final String DELETE_OPTION = "del";
  public static final String DIRECTORY_OPTION = "d";
  public static final String TIME_REFETCH_ALL_OPTION = "t";
  public static final String DELETE = "del";
  public static final String SEQUENTIAL = "seq";

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private StopWatch stopWatch = null;
  private Path vulnDir;
  private String vulnId;
  private BackendConnector backendConnector;
  private HashMap<String, Object> args;
  Manager manager;

  public ImportCommand(
      Manager manager, HashMap<String, Object> args, BackendConnector backendConnector) {
    this.manager = manager;
    this.backendConnector = backendConnector;
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

    manager.setVulnStatus(vulnId, Manager.VulnStatus.STARTING);
    boolean bugExists = false;
    try {
      bugExists = this.backendConnector.isBugExisting(vulnId);
    } catch (BackendConnectionException e) {
      log.error("Can't connect to the Backend");
      manager.setVulnStatus(vulnId, Manager.VulnStatus.FAILED_CONNECTION);
      manager.addFailure(vulnId, e);
      log.error(e.getMessage());
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

      manager.addNewVulnerability(vulnId);
      log.info("Bug [{}] does not exist in backend", vulnId);
    }

    String statementPath = findStatementPath();

    if (statementPath != null) {
      Vulnerability vuln;
      try {
        vuln = Metadata.getFromYaml(statementPath);
      } catch (IOException e) {
        log.error("Error while reading Yaml file for [{}]", vulnId);
        return;
      }
      if ((vuln.getCommits() == null || vuln.getCommits().size() == 0)
          && (vuln.getArtifacts() == null || vuln.getArtifacts().size() == 0)) {
        log.warn("No fix commits or affected artifacts for vulnerability " + vuln.getVulnId());
        vuln.setCommits(new ArrayList<Commit>());
      } else {
        ExtractOrClone extractOrClone =
            new ExtractOrClone(
                this.manager,
                vuln,
                new File(this.vulnDir.toString()),
                (boolean) args.get(SKIP_CLONE_OPTION));

        this.stopWatch.lap("ExtractOrClone");
        extractOrClone.execute();
      }

      if (manager.getVulnStatus(vuln.getVulnId()) != Manager.VulnStatus.FAILED_EXTRACT_OR_CLONE
          && manager.getVulnStatus(vuln.getVulnId()) != Manager.VulnStatus.SKIP_CLONE) {

        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.IMPORTING);
        ImportVulnerability importVulnerability = new ImportVulnerability();

        ImportAffectedLibraries importAffectedLibraries = new ImportAffectedLibraries();

        this.stopWatch.lap("ImportVulnerability");
        try {
          importVulnerability.execute(vuln, args, backendConnector);
        } catch (IOException | BackendConnectionException e) {
          manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED_IMPORT_VULN);
          manager.addFailure(vuln.getVulnId(), e);
          log.error(e.getMessage());
          return;
        }
        this.stopWatch.lap("ImportAffectedLibraries");
        try {
          importAffectedLibraries.execute(vuln, args, backendConnector);
        } catch (IOException | MalformedPackageURLException | BackendConnectionException e) {
          manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED_IMPORT_LIB);
          manager.addFailure(vuln.getVulnId(), e);
          log.error(e.getMessage());
          return;
        }
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.IMPORTED);
      }
      this.stopWatch.stop();
      log.info(
          vuln.getVulnId() + " StopWatch Runtime " + Long.toString(this.stopWatch.getRuntime()));
    }
  }

  public String findStatementPath() {
    if (FileUtil.isAccessibleFile(vulnDir + File.separator + STATEMENT_YAML)) {
      return vulnDir + File.separator + STATEMENT_YAML;
    } else {
      ImportCommand.log.error("Invalid directory {}", vulnDir);
      return null;
    }
  }
}
