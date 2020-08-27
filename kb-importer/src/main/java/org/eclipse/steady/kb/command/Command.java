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
package org.eclipse.steady.kb.command;

import java.util.HashMap;
import org.apache.commons.cli.Options;
import org.eclipse.steady.kb.exception.ValidationException;

/**
 * Command Interface
 */
public interface Command {
  /**
   * get the command name
   *
   * @return a {@link java.lang.String}
   */
  String getCommandName();

  /**
   * run a command. logic to execute a command with arguments
   *
   * @param args a {@link java.util.Map}}
   */
  void run(HashMap<String, Object> args);

  // TODO: may be we might have to change this to our own bean like
  // List<com.sap.psr.vulas.kb.model.Options> rather than the apache-cli Options
  /**
   * get command options
   *
   * @return command options
   */
  Options getOptions();

  /**
   * validate command with command arguments. Throw a validation exception on any validation error
   *
   * @param args a {@link java.util.Map
   * @throws ValidationException
   */
  void validate(HashMap<String, Object> args) throws ValidationException;
}
