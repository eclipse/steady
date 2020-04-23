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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Loader for all commands
 */
public class CommandsLoader {
  private static Map<String, Command> commands = new HashMap<>();
  private static CommandsLoader commandsLoader;

  private CommandsLoader() {}

  /**
   * get instance of commands loader
   * 
   * @return
   */
  public static CommandsLoader getInstance() {
    init();
    return commandsLoader == null ? new CommandsLoader() : commandsLoader;
  }

  /**
   * get list of registered commands in service loader
   * 
   * @return commands
   */
  public Map<String, Command> getCommands() {
    return commands;
  }

  private static void init() {
    ServiceLoader<Command> serviceLoader = ServiceLoader.load(Command.class);
    for (Command command : serviceLoader) {
      commands.put(command.getInfo().getCommand(), command);
    }
  }
}
