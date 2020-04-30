/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.kb.command;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.psr.vulas.ConstructChange;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.kb.Main;
import com.sap.psr.vulas.kb.exception.ValidationException;
import com.sap.psr.vulas.kb.model.Commit;
import com.sap.psr.vulas.kb.model.Vulnerability;
import com.sap.psr.vulas.kb.util.ConstructSet;
import com.sap.psr.vulas.kb.util.Metadata;
import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * import command
 */
public class Import implements Command {

  private static final String UPLOAD_CONSTRUCT_OPTION = "u";
  private static final String DIRECTORY_OPTION = "d";
  private static final String OVERWRITE_OPTION = "o";
  private static final String VERBOSE_OPTION = "v";

  private static final String UPLOAD_LONG_OPTION = "upload";
  private static final String VERBOSE_LONG_OPTION = "verbose";
  private static final String OVERWRITE_LONG_OPTION = "overwrite";
  private static final String DIRECTORY_LONG_OPTION = "directory";

  private static final Logger log = LoggerFactory.getLogger(Import.class);

  @Override
  public String getCommandName() {
    return "import";
  }

  @Override
  public void run(HashMap<String, Object> args) {
    String rootDir = null;
    rootDir = (String) args.get(DIRECTORY_OPTION);
    rootDir = getAbsolutePath(rootDir);

    Vulnerability meta = null;
    meta = Metadata.getVulnerabilityMetadata(rootDir);
    if (meta == null) {
      return;
    }

    String vulnId = meta.getVulnId();

    // Whether to upload JSON to the backend or save to the disk
    VulasConfiguration.getGlobal().setProperty(CoreConfiguration.BACKEND_CONNECT,
        ((Boolean) args.get(UPLOAD_CONSTRUCT_OPTION)
            ? CoreConfiguration.ConnectType.READ_WRITE.toString()
            : CoreConfiguration.ConnectType.READ_ONLY.toString()));

    try {
      if (!(Boolean)args.get(OVERWRITE_OPTION)
          && BackendConnector.getInstance().isBugExisting(vulnId)) {
        Import.log.info("Bug [{}] already exists in backend, analysis will be skipped", vulnId);
        return;
      }
    } catch (BackendConnectionException e) {
      Import.log.error("Error in connecting to backend - {}", e.getMessage());
      return;
    }

    List<Commit> commits = new ArrayList<Commit>();
    File file = new File(rootDir);

    File commitDirs[] = file.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.isDirectory();
      }
    });

    for (File commitDir : commitDirs) {
      String dir = commitDir.getAbsolutePath();
      Commit commit = Metadata.getCommitMetadata(dir);
      if (commit != null) {
        commits.add(commit);
      }
    }

    Set<ConstructChange> changes = null;
    Map<String, Set<ConstructChange>> allChanges = new HashMap<String, Set<ConstructChange>>();
    for (Commit commit : commits) {
      changes = ConstructSet.identifyConstructChanges(commit, allChanges);
      if ((Boolean) args.get(VERBOSE_OPTION)) {
        for (ConstructChange chg : changes) {
          Import.log.info(chg.toString());
        }
      }
    }

    final String json = toJSON(meta, commits, allChanges);

    try {
      BackendConnector.getInstance().uploadChangeList(vulnId, json);
    } catch (BackendConnectionException e) {
      Import.log.error("Error in connecting to backend - {}", e.getMessage());
    }
  }

  @Override
  public Options getOptions() {
    Options options = new Options();
    options.addRequiredOption(DIRECTORY_OPTION, DIRECTORY_LONG_OPTION, true,
        "directory containing mutiple commit folders with meta files");
    options.addOption(OVERWRITE_OPTION, OVERWRITE_LONG_OPTION, false,
        "overwrite the bug if it already exists in the backend");
    options.addOption(VERBOSE_OPTION, VERBOSE_LONG_OPTION, false, "Verbose mode");
    options.addOption(UPLOAD_CONSTRUCT_OPTION, UPLOAD_LONG_OPTION, false,
        "Upload construct changes");

    return options;
  }

  @Override
  public void validate(HashMap<String, Object> args) throws ValidationException {
    String dir = (String) args.get(DIRECTORY_OPTION);
    if (!Files.isDirectory(Paths.get(getAbsolutePath(dir)))) {
      throw new ValidationException("directory " + dir + "does not exist");
    }
  }

  /**
   * get directory absolute path if it is relative
   * 
   * @param rootDir
   * @return
   */
  private String getAbsolutePath(String rootDir) {
    Path p = Paths.get(rootDir);
    if (p.isAbsolute()) {
      return rootDir;
    } else {
      String jarPath =
          new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())
              .getParentFile().getAbsolutePath();
      return (jarPath + File.separator + rootDir);
    }
  }

  /**
   * <p>
   * toJSON.
   * </p>
   * 
   * @param _vulnerability a {@link com.sap.psr.vulas.kb.model.Vulnerability} object.
   * @param _commits {@link java.util.List}.
   * @param _allChanges a {@link java.util.Map}.
   * @return a {@link java.lang.String} object.
   */
  private String toJSON(Vulnerability _vulnerability, List<Commit> _commits,
      Map<String, Set<ConstructChange>> _allChanges) throws ConcurrentModificationException {
    final StringBuilder b = new StringBuilder();
    b.append(" { ");
    b.append(" \"bugId\" : \"").append(_vulnerability.getVulnId()).append("\", ");
    b.append(" \"maturity\" : \"" + ContentMaturityLevel.DRAFT.toString() + "\", ");
    b.append(" \"origin\" : \"" + BugOrigin.PUBLIC.toString() + "\", ");

    String description = _vulnerability.getDescription();
    if (description != null) {
      b.append(" \"descriptionAlt\" : ").append(JsonBuilder.escape(description)).append(", ");
    }

    List<String> links = _vulnerability.getLinks();
    if (links != null) {
      int i = 0;
      b.append(" \"reference\" : [");
      for (String link : links) {
        if (i != 0) {
          b.append(", ");
        } else
          i++;
        b.append(JsonBuilder.escape(link));
      }

      b.append("], ");
    }

    b.append(" \"constructChanges\" : [ ");
    int i = 0;
    final Set<ConstructChange> consol_ch =
        ConstructSet.getConsolidatedChanges(_commits, _allChanges);
    for (ConstructChange c : consol_ch) {
      b.append(c.toJSON());
      if (++i < consol_ch.size())
        b.append(", ");
    }
    b.append(" ] } ");
    return b.toString();
  }
}
