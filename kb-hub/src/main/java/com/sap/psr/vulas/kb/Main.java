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
package com.sap.psr.vulas.kb;

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sap.psr.vulas.kb.cmd.Command;
import com.sap.psr.vulas.kb.cmd.CommandManager;
import com.sap.psr.vulas.kb.cmd.CommandsLoader;
import com.sap.psr.vulas.kb.context.Context;

public class Main {
  private static final Log LOGGER = LogFactory.getLog(Main.class);

  public static void main(String[] _args) {

    List<Command> commands = CommandsLoader.getInstance().getCommands();
    final Options options = new Options();
    for (Command command : commands) {
      options.addOption(command.getCommandOption());
    }

    final CommandLineParser parser = new DefaultParser();
    try {
      final CommandLine cmdLine = parser.parse(options, _args);
      Context context = new Context();
      for (Command command : commands) {
        CommandManager manager = new CommandManager(command);
        manager.runCommand(cmdLine, context);
      }
    } catch (ParseException e) {
      LOGGER.error(e.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("kb-hub", options);
    }
  }
}
