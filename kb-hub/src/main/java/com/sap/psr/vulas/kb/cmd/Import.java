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

import com.sap.psr.vulas.kb.meta.CommandMeta;
import com.sap.psr.vulas.kb.task.Executor;
import com.sap.psr.vulas.kb.task.Task;

public class Import implements Command {

  @Override
  public void execute(String _args[]) {
    Task importTask = new com.sap.psr.vulas.kb.task.Import();
    Executor executor = new Executor(importTask);
    executor.execute(_args);
  }

  @Override
  public CommandMeta getInfo() {
    return new CommandMeta("import", "Import data to steady");
  }
}
