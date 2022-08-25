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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.task.ExtractOrClone;
import org.eclipse.steady.kb.task.ImportAffectedLibraries;
import org.eclipse.steady.kb.task.ImportVulnerability;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.StopWatch;

import com.github.packageurl.MalformedPackageURLException;

/**
 * Imports information pertaining to a single vulnerability statement. To do so,
 * it calls 3 tasks in method {@link ImportCommand#run()}:
 * {@link ExtractOrClone}, {@link ImportVulnerability} and
 * {@link ImportAffectedLibraries}.
 */
public class ImportCommand implements Runnable {

  /** The file name of statements coming from Project KB. */
  public static final String STATEMENT_YAML = "statement.yaml";

  /** The file name of tarballs coming from Project KB, and which contain the
   * source code changes created by the respective fix commits. */
  public static final String SOURCE_TAR = "changed-source-code.tar.gz";

  /** Constant <code>VERBOSE_OPTION="v"</code> */
  public static final String VERBOSE_OPTION = "v";

  /** Constant <code>UPLOAD_CONSTRUCT_OPTION="u"</code> */
  public static final String UPLOAD_CONSTRUCT_OPTION = "u";

  /** Constant <code>SKIP_CLONE_OPTION="u"</code> */
  public static final String SKIP_CLONE_OPTION = "u";

  /** Constant <code>OVERWRITE_OPTION="o"</code> */
  public static final String OVERWRITE_OPTION = "o";

  /** Constant <code>DELETE_OPTION="del"</code> */
  public static final String DELETE_OPTION = "del";

  /** Constant <code>DIRECTORY_OPTION="d"</code> */
  public static final String DIRECTORY_OPTION = "d";

  /** Constant <code>TIME_REFETCH_ALL_OPTION="t"</code> */
  public static final String TIME_REFETCH_ALL_OPTION = "t";

  /** Constant <code>DELETE="del"</code> */
  public static final String DELETE = "del";
  
  /** Constant <code>SEQUENTIAL="seq"</code> */
  public static final String SEQUENTIAL = "seq";

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private StopWatch stopWatch = null;
  private Path vulnDir;
  private String vulnId;
  private HashMap<String, Object> args;

  /**
   * The {@link Manager} that started the command. Used to reflect the status of
   * the import and maintain a list of failed imports.
   */
  Manager manager;

  /**
   * <p>Constructor for ImportCommand.</p>
   *
   * @param manager a {@link org.eclipse.steady.kb.Manager} object
   * @param args a {@link java.util.HashMap} object
   */
  public ImportCommand(
      Manager manager, HashMap<String, Object> args) {
    this.manager = manager;
    this.vulnDir = Paths.get((String) args.get(DIRECTORY_OPTION));
    this.vulnId = vulnDir.getFileName().toString();
    this.args = args;
    this.stopWatch = new StopWatch(this.vulnId);
  }

  /**
   * <p>Getter for the field <code>vulnId</code>.</p>
   *
   * @return a {@link java.lang.String} object
   */
  public String getVulnId() {
    return this.vulnId;
  }

  /** {@inheritDoc} */
  @Override
  public void run() {
    this.stopWatch.start();
    manager.setVulnStatus(vulnId, Manager.VulnStatus.STARTING);

    BackendConnector backend_connector = BackendConnector.getInstance();

    // Does the vulnerability already exist?
    boolean bugExists = false;
    try {
      bugExists = backend_connector.isBugExisting(vulnId);
    } catch (BackendConnectionException e) {
      manager.setVulnStatus(vulnId, Manager.VulnStatus.FAILED_CONNECTION);
      manager.addFailure(vulnId, e);
      this.stopWatch.stop(e);
      return;
    }

    // Override or not?
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
        this.stopWatch.stop();
        return;
      }
    }
    else {
      manager.addNewVulnerability(vulnId);
      log.info("Bug [{}] does not exist in backend", vulnId);
    }

    Path statementPath = this.vulnDir.resolve(STATEMENT_YAML);

    // statement.yaml does not exist? This should not happen, because the
    // Manager only picks directories that contain a statement.yaml file.
    if (!FileUtil.isAccessibleFile(statementPath)) {
      ImportCommand.log.error("Cannot read [" + statementPath + "]");
      manager.setVulnStatus(vulnId, Manager.VulnStatus.MALFORMED_INPUT);
      this.stopWatch.stop();
      return;
    }
    // Proceed with the import
    else {

      // Read statement.yaml
      Vulnerability vuln;
      try {
        vuln = Metadata.getFromYaml(statementPath.toString());
      } catch (IOException e) {
        this.stopWatch.stop(e);
        return;
      }

      // Statement does not have commits nor affected libs?
      if ((vuln.getCommits() == null || vuln.getCommits().size() == 0)
          && (vuln.getArtifacts() == null || vuln.getArtifacts().size() == 0)) {
        log.warn("No fix commits or affected artifacts for vulnerability [" + vuln.getVulnId() + "]");
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.MALFORMED_INPUT);
        this.stopWatch.stop();
        return;
      }
      
      // Extract source code tarball (if any) or clone repo
      else {
        ExtractOrClone extractOrClone =
            new ExtractOrClone(
                this.manager,
                vuln,
                new File(this.vulnDir.toString()),
                (boolean) args.get(SKIP_CLONE_OPTION));
        extractOrClone.execute();
        this.stopWatch.lap("Cloned repo or extracted source code tarball");
      }

      if (manager.getVulnStatus(vuln.getVulnId()) != Manager.VulnStatus.FAILED_EXTRACT_OR_CLONE
          && manager.getVulnStatus(vuln.getVulnId()) != Manager.VulnStatus.SKIP_CLONE) {

        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.IMPORTING);

        try {
          ImportVulnerability importVulnerability = new ImportVulnerability();
          importVulnerability.execute(vuln, args, backend_connector);
          this.stopWatch.lap("Imported change list using the fix-commits");
        } catch (IOException | BackendConnectionException e) {
          manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED_IMPORT_VULN);
          manager.addFailure(vuln.getVulnId(), e);
          this.stopWatch.stop(e);
          return;
        }
        
        try {
          ImportAffectedLibraries importAffectedLibraries = new ImportAffectedLibraries();
          importAffectedLibraries.execute(vuln, args, backend_connector);
          this.stopWatch.lap("Imported affected libraries");
        } catch (IOException | MalformedPackageURLException | BackendConnectionException e) {
          manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.FAILED_IMPORT_LIB);
          manager.addFailure(vuln.getVulnId(), e);
          this.stopWatch.stop(e);
          return;
        }
        manager.setVulnStatus(vuln.getVulnId(), Manager.VulnStatus.IMPORTED);
      }
      this.stopWatch.stop();
    }
  }
}
