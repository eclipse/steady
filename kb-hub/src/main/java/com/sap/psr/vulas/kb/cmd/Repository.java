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

import java.util.List;
import org.apache.commons.cli.Option;
import com.sap.psr.vulas.kb.context.Context;

/**
 * Command for Repository
 */
public class Repository implements Command {

  @Override
  public Option getCommandOption() {
    Option option = new Option("r", "repo", true, "VCS repository URL");
    option.setRequired(true);
    return option;
  }

  @Override
  public void execute(List<String> cmdValues, Context context) {
    context.setBugId(cmdValues.get(0));
  }
}
