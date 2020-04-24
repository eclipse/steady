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
package com.sap.psr.vulas.kb.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.psr.vulas.ConstructChange;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.kb.meta.Commit;
import com.sap.psr.vulas.kb.meta.Metadata;
import com.sap.psr.vulas.kb.util.Constructs;
import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class Import implements Task {

  private static final String UPLOAD_CONSTRUCT_OPTION = "u";
  private static final String DIRECTORY_OPTION = "d";
  private static final String OVERWRITE_OPTION = "o";
  private static final String VERBOSE_OPTION = "v";
  private static final String META_PROPERTIES_FILE = "meta.properties";
  private static final String REPO = "repo";
  private static final String TIMESTAMP = "timestamp";
  private static final String COMMIT_ID = "commitId";
  private static final String BRANCH = "branch";

  private static final Logger log = LoggerFactory.getLogger(Import.class);
  private Map<String, Set<ConstructChange>> changes = new HashMap<String, Set<ConstructChange>>();

  @Override
  public void run(String[] _args) {
    Options options = new Options();
    options.addOption(DIRECTORY_OPTION, "root-directory", true,
        "directory containing meta file and multiple folders containing vcs data");
    options.addOption(OVERWRITE_OPTION, "overwrite-if-existing", false,
        "overwrite the bug if it already exists in the backend");
    options.addOption(VERBOSE_OPTION, "verbose", false, "Verbose mode");
    options.addOption(UPLOAD_CONSTRUCT_OPTION, "upload", false, "Upload construct changes");

    final CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, _args);
    } catch (ParseException e) {
      Import.log.error(e.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("Import", options);
    }

    String rootDir = null;
    if (!cmd.hasOption(DIRECTORY_OPTION)) {
      Import.log.error("The option (d)irectory is mandatory");
      return;
    }

    rootDir = cmd.getOptionValue(DIRECTORY_OPTION);

    Metadata meta = null;
    meta = readMetaFile(rootDir + File.separator + META_PROPERTIES_FILE);
    if (meta == null) {
      return;
    }

    boolean verbose = false;
    boolean overwrite = false;

    if (cmd.hasOption(VERBOSE_OPTION))
      verbose = true;
    if (cmd.hasOption(OVERWRITE_OPTION))
      overwrite = true;

    String vulnId = meta.getVulnId();

    // Whether to upload JSON to the backend or save to the disk
    final boolean upload = cmd.hasOption(UPLOAD_CONSTRUCT_OPTION);
    VulasConfiguration.getGlobal().setProperty(CoreConfiguration.BACKEND_CONNECT,
        (upload ? CoreConfiguration.ConnectType.READ_WRITE.toString()
            : CoreConfiguration.ConnectType.READ_ONLY.toString()));

    try {
      if (!overwrite && BackendConnector.getInstance().isBugExisting(vulnId)) {
        Import.log.info("Bug [{}] already exists in backend, analysis will be skipped", vulnId);
        return;
      }
    } catch (BackendConnectionException e) {
      Import.log.error("Error in connecting to backend - {}", e.getMessage());
      return;
    }

    if (vulnId == null)
      throw new IllegalArgumentException(
          "The following options are mandatory as part of meta json file: repo and bugId");

    List<Commit> commits = new ArrayList<Commit>();
    File file = new File(rootDir);
    File commitFiles[] = file.listFiles();
    for(File commitFile: commitFiles) {
      if(commitFile.isDirectory()) {
        String dir = commitFile.getAbsolutePath();
        Commit commit = readCommitMetaFile(dir + File.separator + META_PROPERTIES_FILE);
        if (commit != null) {
          commit.setDirectory(dir);
          commits.add(commit);
        }
      }
    }

    Set<ConstructChange> changes = null;
    for (Commit commit : commits) {
      changes = Constructs.identifyConstructChanges(commit, this.changes);
      if (verbose) {
        for (ConstructChange chg : changes) {
          Import.log.info(chg.toString());
        }
      }
    }

    final String json = toJSON(meta, commits);

    try {
      BackendConnector.getInstance().uploadChangeList(vulnId, json);
    } catch (BackendConnectionException e) {
      Import.log.error("Error in connecting to backend - {}", e.getMessage());
    }
  }

  /**
   * read vulnerability information from meta file
   * 
   * @param filePath a {@link java.lang.String} object.
   * @return _commit a {@link com.sap.psr.vulas.kb.meta.Metadata} object.
   */
  private Metadata readMetaFile(String filePath) {
    File rootMetaFile = new File(filePath);
    if (!rootMetaFile.exists() || !rootMetaFile.isFile()) {
      throw new IllegalArgumentException("The meta file in root directory is missing");
    }

    Properties prop = new Properties();
    try (FileInputStream inStream = new FileInputStream(rootMetaFile)) {
      prop.load(inStream);
    } catch (FileNotFoundException e) {
      log.error("Error reading meta file {}", e.getMessage());
      return null;
    } catch (IOException e) {
      log.error("Error reading meta file {}", e.getMessage());
      return null;
    }

    Metadata metadata = new Metadata();
    String vulnId = prop.getProperty("vulnId");
    if (vulnId == null) {
      throw new IllegalArgumentException("The vulnId is missing missing in the root.meta file");
    }

    metadata.setVulnId(vulnId);
    metadata.setDescription(prop.getProperty("description"));
    String links = prop.getProperty("links");
    metadata.setLinks(Arrays.asList(links.split(",")));

    return metadata;
  }

  /**
   * read commit information from meta file
   * 
   * @param filePath a {@link java.lang.String} object.
   * @return _commit a {@link com.sap.psr.vulas.kb.meta.Commit} object.
   */
  private Commit readCommitMetaFile(String filePath) {
    File commitFile = new File(filePath);
    if (!commitFile.exists() || !commitFile.isFile()) {
      log.error("The meta file is missing {}", filePath);
      return null;
    }

    Properties prop = new Properties();
    try (FileInputStream inStream = new FileInputStream(commitFile)) {
      prop.load(inStream);
    } catch (FileNotFoundException e) {
      log.error("Error reading meta file {}", e.getMessage());
      return null;
    } catch (IOException e) {
      log.error("Error reading meta file {}", e.getMessage());
      return null;
    }

    Commit commit = new Commit();
    commit.setBranch(prop.getProperty(BRANCH));
    commit.setCommitId(prop.getProperty(COMMIT_ID));
    commit.setTimestamp(prop.getProperty(TIMESTAMP));
    commit.setRepoUrl(prop.getProperty(REPO));

    return commit;
  }

  /**
   * <p>
   * toJSON.
   * </p>
   *
   * @param _revs an array of {@link java.lang.String} objects.
   * @return a {@link java.lang.String} object.
   * @throws java.util.ConcurrentModificationException if any.
   */
  public String toJSON(Metadata _meta, List<Commit> _commits)
      throws ConcurrentModificationException {
    final StringBuilder b = new StringBuilder();
    b.append(" { ");
    b.append(" \"bugId\" : \"").append(_meta.getVulnId()).append("\", ");
    b.append(" \"maturity\" : \"" + ContentMaturityLevel.DRAFT.toString() + "\", ");
    b.append(" \"origin\" : \"" + BugOrigin.PUBLIC.toString() + "\", ");

    String description = _meta.getDescription();
    if (description != null) {
      b.append(" \"descriptionAlt\" : ").append(JsonBuilder.escape(description)).append(", ");
    }

    List<String> links = _meta.getLinks();
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
        Constructs.getConsolidatedChanges(_commits, this.changes);
    for (ConstructChange c : consol_ch) {
      b.append(c.toJSON());
      if (++i < consol_ch.size())
        b.append(", ");
    }
    b.append(" ] } ");
    return b.toString();
  }
}
