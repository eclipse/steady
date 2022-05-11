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
package org.eclipse.steady.kb.util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.yaml.snakeyaml.Yaml;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.kb.model.Commit;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.model.Note;
import org.eclipse.steady.kb.model.Artifact;

/**
 * Metadata
 */
public class Metadata {

  private static final String META_PROPERTIES_FILE = "metadata.json";

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * read commit information from meta file
   *
   * @param commitDir a {@link java.lang.String} object.
   * @return _commit a {@link org.eclipse.steady.kb.model.Commit} object.
   * @throws java.io.IOException if any.
   * @throws com.google.gson.JsonSyntaxException if any.
   */
  public static Commit getCommitMetadata(String commitDir) throws JsonSyntaxException, IOException {
    String filePath = commitDir + File.separator + META_PROPERTIES_FILE;
    if (!FileUtil.isAccessibleFile(filePath)) {
      log.error(
          "The commit folder {} or the meta file is missing {} in commit folder",
          commitDir,
          filePath);
      return null;
    }

    Gson gson = new Gson();
    Commit metadata = null;
    metadata = gson.fromJson(FileUtil.readFile(filePath), Commit.class);
    metadata.setDirectory(commitDir);
    return metadata;
  }

  public static void writeCommitMetadata(String commitDir, HashMap<String, String> commitMetadata)
      throws IOException {
    String filePath = commitDir + File.separator + META_PROPERTIES_FILE;
    File file = new File(filePath);
    file.createNewFile();
    Writer writer = new FileWriter(filePath, false);
    new Gson().toJson(commitMetadata, writer);
    writer.close();
  }

  /**
   * read vulnerability information from meta file
   *
   * @param rootDir a {@link java.lang.String} object.
   * @return _commit a {@link org.eclipse.steady.kb.model.Vulnerability} object.
   * @throws java.io.IOException if any.
   * @throws com.google.gson.JsonSyntaxException if any.
   */
  public static Vulnerability getVulnerabilityMetadata(String rootDir)
      throws JsonSyntaxException, IOException {
    String filePath = rootDir + File.separator + META_PROPERTIES_FILE;
    if (!FileUtil.isAccessibleFile(filePath)) {
      throw new IllegalArgumentException(
          "The root folder "
              + rootDir
              + "  or the meta file in root directory is missing "
              + filePath);
    }

    Gson gson = new Gson();
    Vulnerability metadata = null;
    metadata = gson.fromJson(FileUtil.readFile(filePath), Vulnerability.class);

    if (StringUtils.isBlank(metadata.getVulnId())) {
      throw new IllegalArgumentException(
          "The vulnerability_id is missing missing in the " + filePath + " file");
    }

    return metadata;
  }

  public static Vulnerability getFromYaml(String metadataPathString) throws IOException {
    // Yaml yaml = new Yaml(new Constructor(Vulnerability.class));

    Path metadataPath = Paths.get(metadataPathString);
    Yaml yaml = new Yaml();

    // String metadataString = File.readString(dirPath + "/metadata.yaml");
    String metadataString = new String(Files.readAllBytes(metadataPath));

    // Vulnerability vulnerability = yaml.load(metadataString);
    Map<String, Object> vulnerabilityMap = yaml.load(metadataString);
    // ObjectMapper objectMapper = new ObjectMapper();
    // Vulnerability vulnerability = objectMapper.convertValue(vulnerabilityMap,
    // Vulnerability.class);

    Vulnerability vulnerability = new Vulnerability();

    vulnerability.setVulnId((String) vulnerabilityMap.get("vulnerability_id"));

    if (vulnerabilityMap.containsKey("notes")) {
      List<HashMap<String, Object>> notesMaps =
          (List<HashMap<String, Object>>) vulnerabilityMap.get("notes");
      List<Note> notes = new ArrayList<Note>();
      for (HashMap<String, Object> noteMap : notesMaps) {
        Note note = new Note();
        note.setText((String) noteMap.get("text"));
        List<String> links = (List<String>) noteMap.get("links");
        note.setLinks(links);
        notes.add(note);
      }
      vulnerability.setNotes(notes);
    }

    if (vulnerabilityMap.containsKey("artifacts")) {
      List<HashMap<String, Object>> artifactsMaps =
          (List<HashMap<String, Object>>) vulnerabilityMap.get("artifacts");
      List<Artifact> artifacts = new ArrayList<Artifact>();
      for (HashMap<String, Object> artifactMap : artifactsMaps) {
        Artifact artifact = new Artifact();
        artifact.setId((String) artifactMap.get("id"));
        artifact.setReason((String) artifactMap.get("reason"));
        artifact.setAffected((Boolean) artifactMap.get("affected"));
        artifacts.add(artifact);
      }
      vulnerability.setArtifacts(artifacts);
    }

    if (vulnerabilityMap.containsKey("aliases")) {
      List<String> aliases = (List<String>) vulnerabilityMap.get("aliases");
      vulnerability.setAliases(aliases);
    }

    List<Commit> commitList = new ArrayList<Commit>();
    if (vulnerabilityMap.containsKey("fixes")) {
      List<HashMap<String, Object>> fixes =
          (List<HashMap<String, Object>>) vulnerabilityMap.get("fixes");
      for (HashMap<String, Object> fix : fixes) {
        String branch = fix.get("id").toString();
        List<HashMap<String, String>> commits = (List<HashMap<String, String>>) fix.get("commits");
        for (HashMap<String, String> commitMap : commits) {
          Commit commit = new Commit();
          String repository = commitMap.get("repository");
          String commitId = commitMap.get("id");
          commit.setRepoUrl(repository);
          commit.setCommitId(commitId);
          commit.setBranch(branch);
          commitList.add(commit);
        }
      }
      vulnerability.setCommits(commitList);
    }

    return vulnerability;
  }
}
