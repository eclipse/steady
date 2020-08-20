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
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sap.psr.vulas.kb.model.Commit;
import com.sap.psr.vulas.kb.model.Vulnerability;
import com.sap.psr.vulas.shared.util.FileUtil;

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
   * @return _commit a {@link com.sap.psr.vulas.kb.model.Commit} object.
   * @throws IOException
   * @throws JsonSyntaxException
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

  /**
   * read vulnerability information from meta file
   *
   * @param rootDir a {@link java.lang.String} object.
   * @return _commit a {@link com.sap.psr.vulas.kb.model.Vulnerability} object.
   * @throws IOException
   * @throws JsonSyntaxException
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
}
