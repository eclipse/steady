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
import org.apache.commons.cli.Options;

import org.eclipse.steady.kb.command.Import;
import org.eclipse.steady.kb.command.Command;
import org.eclipse.steady.kb.command.CommandParser;
import org.eclipse.steady.kb.exception.CommandLineParserException;

/**
 * <p>Main class.</p>
 */
public class Main {
  /**
   * <p>main.</p>
   *
   * @param _args an array of {@link java.lang.String} objects
   */
  public static void main(String[] _args) {
    // CommandExecutor.getInstance().execute(_args);

    Command command = new Import();
    Options commandOptions = command.getOptions();

    HashMap<String, Object> mapCommandOptionValues ;
    try {
      mapCommandOptionValues = CommandParser.parse(_args, commandOptions);
    } catch (CommandLineParserException e) {
      System.out.println("commandLineParserException");
      //log.error(e.getMessage());
      //printHelp(commandOptions);
      return;
    }
    mapCommandOptionValues.put(Import.DIRECTORY_OPTION, "/kb-importer/data");

    /*try {
      command.validate(mapCommandOptionValues);
    } catch (ValidationException e) {
      log.error(e.getMessage());
      return;
    }*/

    command.run(mapCommandOptionValues);
    Manager manager = new Manager();
    manager.start("/kb-importer/data/statements", mapCommandOptionValues);
  }
}
