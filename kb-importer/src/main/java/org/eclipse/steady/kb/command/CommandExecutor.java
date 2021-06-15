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

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.kb.exception.CommandLineParserException;
import org.eclipse.steady.kb.exception.ValidationException;

/**
 * command executor
 */
public class CommandExecutor {

  private static final String DIRECTORY_OPTION = "d";
  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();
  private Map<Command.NAME, Command> commands = new HashMap<>();
  private static CommandExecutor commandExecutor;

  private CommandExecutor() {
    init();
  }

  /**
   * <p>getInstance.</p>
   *
   * @return a {@link org.eclipse.steady.kb.command.CommandExecutor} object
   */
  public static synchronized CommandExecutor getInstance() {
    if (commandExecutor == null) {
      commandExecutor = new CommandExecutor();
    }
    return commandExecutor;
  }

  private void init() {
    ServiceLoader<Command> serviceProviders = ServiceLoader.load(Command.class);
    Iterator<Command> iterator = serviceProviders.iterator();
    while (iterator.hasNext()) {
      Command command = iterator.next();
      commands.put(command.getCommandName(), command);
    }
  }

  /**
   * command executor
   *
   * @param _args a array of {java.lang.String}
   */
  public void execute(String _args[]) {
    if (_args.length == 0) {
      log.error("No arguments passed");
      return;
    }

    Command command = null;
    try {
      command = commands.get(Command.NAME.valueOf(_args[0].toUpperCase()));
    } catch (IllegalArgumentException e) {
      // skip when an unknown command name is passed. Default to import
    }

    if (command == null) {
      command = new Import();
    }

    Options commandOptions = command.getOptions();

    HashMap<String, Object> mapCommandOptionValues;
    try {
      mapCommandOptionValues = CommandParser.parse(_args, commandOptions);
      Object rootDirObj = mapCommandOptionValues.get(DIRECTORY_OPTION);
      if (rootDirObj != null) {
        String rootDir = (String) rootDirObj;
        rootDir = getAbsolutePath(rootDir);
        mapCommandOptionValues.put(DIRECTORY_OPTION, rootDir);
      }
    } catch (CommandLineParserException e) {
      log.error(e.getMessage());
      printHelp(commandOptions);
      return;
    }

    try {
      command.validate(mapCommandOptionValues);
    } catch (ValidationException e) {
      log.error(e.getMessage());
      return;
    }

    command.run(mapCommandOptionValues);
  }

  /**
   * Print command help
   *
   * @param a {@link org.apache.commons.cli.Options}
   */
  private void printHelp(Options commandOptions) {
    // Showing import help
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar <jar> <options>", new Import().getOptions());
  }

  /**
   * get directory absolute path if it is relative
   *
   * @param rootDir
   * @return absolute path
   */
  private String getAbsolutePath(String rootDir) {
    return Paths.get(rootDir).toAbsolutePath().normalize().toString();
  }
}
