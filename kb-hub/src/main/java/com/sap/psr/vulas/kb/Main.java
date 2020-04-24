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

import java.util.Arrays;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.psr.vulas.kb.cmd.Command;
import com.sap.psr.vulas.kb.cmd.CommandsLoader;

public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] _args) {
    
    if (_args.length == 0) {
      log.error("Command not found");
      return;
    }

    String command = _args.length == 0 ? "help" : _args[0];
    Map<String, Command> commands = CommandsLoader.getInstance().getCommands();

    Command cmd = commands.get(command);
    if (cmd == null) {
      log.error("[{}] command not found", command);
      command = "help";
      cmd = commands.get(command);
    }

    String[] argsWithoutCmd =
        _args.length == 0 ? _args : Arrays.copyOfRange(_args, 1, _args.length);
    cmd.execute(argsWithoutCmd);
  }
}
