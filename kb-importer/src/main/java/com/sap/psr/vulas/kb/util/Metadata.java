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
package com.sap.psr.vulas.kb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.psr.vulas.kb.model.Commit;
import com.sap.psr.vulas.kb.model.Vulnerability;

/**
 * Metadata 
 */
public class Metadata {

  private static final String VULN_ID = "vulnerability_id";
  private static final String LINKS = "links";
  private static final String DESCRIPTION = "description";
  private static final String REPO = "repository";
  private static final String TIMESTAMP = "timestamp";
  private static final String COMMIT_ID = "commit_id";
  private static final String BRANCH = "branch";
  private static final String META_PROPERTIES_FILE = "meta.properties";

  private static final Logger log = LoggerFactory.getLogger(Metadata.class);

  /**
   * read commit information from meta file
   * 
   * @param commitDir a {@link java.lang.String} object.
   * @return _commit a {@link com.sap.psr.vulas.kb.model.Commit} object.
   */
  public static Commit getCommitMetadata(String commitDir) {
    String filePath = commitDir + File.separator + META_PROPERTIES_FILE;
    File commitFile = new File(filePath);
    if (!commitFile.exists() || !commitFile.isFile()) {
      log.error("The commit folder {} or the meta file is missing {} in commit folder", commitDir,  filePath);
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
    commit.setDirectory(commitDir);

    return commit;
  }

  /**
   * read vulnerability information from meta file
   * 
   * @param rootDir a {@link java.lang.String} object.
   * @return _commit a {@link com.sap.psr.vulas.kb.model.Vulnerability} object.
   */
  public static Vulnerability getVulnerabilityMetadata(String rootDir) {
    String filePath = rootDir + File.separator + META_PROPERTIES_FILE;
    File rootMetaFile = new File(filePath);
    if (!rootMetaFile.exists() || !rootMetaFile.isFile()) {
      throw new IllegalArgumentException("The root folder "+rootDir+"  or the meta file in root directory is missing "+filePath);
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

    Vulnerability metadata = new Vulnerability();
    String vulnId = prop.getProperty(VULN_ID);
    if (vulnId == null) {
      throw new IllegalArgumentException("The vulnId is missing missing in the "+filePath+" file");
    }

    metadata.setVulnId(vulnId);
    metadata.setDescription(prop.getProperty(DESCRIPTION));
    String links = prop.getProperty(LINKS);
    if(links!=null)metadata.setLinks(Arrays.asList(links.split(",")));

    return metadata;
  }
}