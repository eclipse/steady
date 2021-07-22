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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.kb.exception.ValidationException;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.task.Task;
import org.eclipse.steady.kb.task.TaskProvider;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;

/**
 * import command
 */
public class Import implements Command {

  private static final String METADATA_JSON = "metadata.json";
  private static final String UPLOAD_CONSTRUCT_OPTION = "u";
  private static final String DIRECTORY_OPTION = "d";
  private static final String OVERWRITE_OPTION = "o";
  private static final String VERBOSE_OPTION = "v";

  private static final String UPLOAD_LONG_OPTION = "upload";
  private static final String VERBOSE_LONG_OPTION = "verbose";
  private static final String OVERWRITE_LONG_OPTION = "overwrite";
  private static final String DIRECTORY_LONG_OPTION = "directory";

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /** {@inheritDoc} */
  @Override
  public Command.NAME getCommandName() {
    return Command.NAME.IMPORT;
  }

  /** {@inheritDoc} */
  @Override
  public void run(HashMap<String, Object> args) {
    String dirPath = (String) args.get(DIRECTORY_OPTION);

    // Whether to upload JSON to the backend or save to the disk
    Object uploadConstruct = args.get(UPLOAD_CONSTRUCT_OPTION);
    VulasConfiguration.getGlobal()
        .setProperty(
            CoreConfiguration.BACKEND_CONNECT,
            (uploadConstruct != null
                ? CoreConfiguration.ConnectType.READ_WRITE.toString()
                : CoreConfiguration.ConnectType.READ_ONLY.toString()));

    if (FileUtil.isAccessibleFile(dirPath + File.separator + METADATA_JSON)) {
      importVuln(args, dirPath);
    } else if (FileUtil.isAccessibleDirectory(dirPath)) {
      File directory = new File(dirPath);
      File[] fList = directory.listFiles();
      if (fList != null) {
        for (File file : fList) {
          if (file.isDirectory()) {
            if (FileUtil.isAccessibleFile(
                file.getAbsolutePath() + File.separator + METADATA_JSON)) {
              importVuln(args, file.getAbsolutePath());
            } else {
              Import.log.warn(
                  "Skipping {} as the directory does not contain metdata.json file",
                  file.getAbsolutePath());
            }
          }
        }
      }

    } else {
      Import.log.error("Invalid directory {}", dirPath);
    }
  }

  private void importVuln(HashMap<String, Object> args, String dirPath) {
    Vulnerability vuln = null;
    try {
      vuln = Metadata.getVulnerabilityMetadata(dirPath);
    } catch (JsonSyntaxException | IOException e1) {
      Import.log.error(e1.getMessage(), e1);
      return;
    }

    if (vuln == null) {
      Import.log.error("Please specify the vulerability id in the json");
      return;
    }

    List<Task> importTasks = TaskProvider.getInstance().getTasks(Command.NAME.IMPORT);

    for (Task task : importTasks) {
      try {
        args.put(DIRECTORY_OPTION, dirPath);
        task.execute(vuln, args, BackendConnector.getInstance());
      } catch (Exception e) {
        log.error(
            "Got ["
                + e.getClass().getName()
                + "] when importing vulnerability ["
                + vuln.getVulnId()
                + "]: "
                + e.getMessage(),
            e);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public Options getOptions() {
    Options options = new Options();
    options.addRequiredOption(
        DIRECTORY_OPTION,
        DIRECTORY_LONG_OPTION,
        true,
        "directory containing mutiple commit folders with meta files or directory containing"
            + " multiple vulerability folders having mutiple commit folders with meta files");
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

  /** {@inheritDoc} */
  @Override
  public void validate(HashMap<String, Object> args) throws ValidationException {
    String dir = (String) args.get(DIRECTORY_OPTION);
    if (!Files.isDirectory(Paths.get(dir))) {
      throw new ValidationException("directory " + dir + "does not exist");
    }
  }
}
