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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.kb.meta.CommandMeta;

public class Version implements Command {

  private static final Logger log = LoggerFactory.getLogger(Version.class);

  @Override
  public void execute(String _args[]) {
    String vulasRelease = CoreConfiguration.getVulasRelease();
    if(!StringUtils.isEmpty(vulasRelease) && !vulasRelease.equals("unknown")) log.info(vulasRelease);
  }

  @Override
  public CommandMeta getInfo() {
    return new CommandMeta("version", "kb-hub version");
  }
}
