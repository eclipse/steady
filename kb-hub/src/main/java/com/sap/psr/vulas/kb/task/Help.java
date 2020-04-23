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
package com.sap.psr.vulas.kb.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.psr.vulas.kb.cmd.Command;
import com.sap.psr.vulas.kb.cmd.CommandsLoader;
import com.sap.psr.vulas.kb.meta.CommandMeta;

public class Help implements Task {
  private static final Logger log = LoggerFactory.getLogger(Help.class);

  @Override
  public void run(String _args[]) {
    if (_args.length == 0) {
      noArgsHelp();
    }

    // TODO: other help with args. Example - kbhub help import
  }

  private void noArgsHelp() {
    log.info(String.format("List of kb-hub commands:%n"));
    for (Command command : CommandsLoader.getInstance().getCommands().values()) {
      CommandMeta commandInfo = command.getInfo();
      log.info(String.format("  %-13s %s", commandInfo.getCommand(), commandInfo.getDescription()));
    }
  }
}
