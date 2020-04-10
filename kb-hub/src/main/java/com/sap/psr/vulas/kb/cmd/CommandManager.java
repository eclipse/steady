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
package com.sap.psr.vulas.kb.cmd;

import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import com.sap.psr.vulas.kb.context.Context;

/**
 * Command manager to run commands
 */
public class CommandManager {
  private Command cmd;

  public CommandManager(Command cmd) {
    this.cmd = cmd;
  }

  /**
   * Run the command
   * 
   * @param cmdLineArgs
   * @param context
   * @throws ParseException
   */
  public void runCommand(CommandLine cmdLineArgs, Context context) throws ParseException {
    // TODO: upgrade commons cli to remove this logic. Newer version has getOption(Option option)
    // which works for both short and long version
    String opt = cmd.getCommandOption().getOpt();
    String longOpt = cmd.getCommandOption().getLongOpt();
    if (cmdLineArgs.hasOption(opt)) {
      runCommandWithOption(cmdLineArgs, context, opt);
    } else if (cmdLineArgs.hasOption(longOpt)) {
      runCommandWithOption(cmdLineArgs, context, longOpt);
    } else if (cmd.getCommandOption().isRequired()) {
      throw new ParseException("Required parameters are missing");
    }
  }

  private void runCommandWithOption(CommandLine cmdLineArgs, Context context, String opt) {
    String[] values = cmdLineArgs.getOptionValues(opt);
    cmd.execute(Arrays.asList(values), context);
  }
}
