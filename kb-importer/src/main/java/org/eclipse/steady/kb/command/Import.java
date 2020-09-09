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
package org.eclipse.steady.kb.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.kb.exception.ValidationException;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.task.ImportAffectedLibraries;
import org.eclipse.steady.kb.task.ImportVulnerability;
import org.eclipse.steady.kb.util.Metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.packageurl.MalformedPackageURLException;
import com.google.gson.JsonSyntaxException;

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

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  @Override
  public String getCommandName() {
    return "import";
  }

  @Override
  public void run(HashMap<String, Object> args) {
    Vulnerability vuln = null;
    try {
      vuln = Metadata.getVulnerabilityMetadata((String) args.get(DIRECTORY_OPTION));
    } catch (JsonSyntaxException | IOException e1) {
      Import.log.error(e1.getMessage(), e1);
      return;
    }

    if (vuln == null) {
      Import.log.error("Please specify the vulerability id in the json");
      return;
    }

    ImportVulnerability importVuln = new ImportVulnerability(vuln, args);
    try {
      importVuln.execute();
    } catch (JsonSyntaxException | BackendConnectionException | IOException e) {
      log.error(e.getMessage(), e);
      return;
    }

    ImportAffectedLibraries importAffectedLibs = new ImportAffectedLibraries(vuln, args);
    try {
      importAffectedLibs.execute();
    } catch (MalformedPackageURLException
        | JsonProcessingException
        | BackendConnectionException e) {
      log.error(e.getMessage(), e);
      return;
    }
  }

  @Override
  public Options getOptions() {
    Options options = new Options();
    options.addRequiredOption(
        DIRECTORY_OPTION,
        DIRECTORY_LONG_OPTION,
        true,
        "directory containing mutiple commit folders with meta files");
    options.addOption(
        OVERWRITE_OPTION,
        OVERWRITE_LONG_OPTION,
        false,
        "overwrite the bug if it already exists in the backend");
    options.addOption(VERBOSE_OPTION, VERBOSE_LONG_OPTION, false, "Verbose mode");
    options.addOption(
        UPLOAD_CONSTRUCT_OPTION, UPLOAD_LONG_OPTION, false, "Upload construct changes");

    return options;
  }

  @Override
  public void validate(HashMap<String, Object> args) throws ValidationException {
    String dir = (String) args.get(DIRECTORY_OPTION);
    if (!Files.isDirectory(Paths.get(dir))) {
      throw new ValidationException("directory " + dir + "does not exist");
    }
  }
}
