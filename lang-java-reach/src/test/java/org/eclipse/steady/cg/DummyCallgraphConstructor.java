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
package org.eclipse.steady.cg;

import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.eclipse.steady.cg.spi.ICallgraphConstructor;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.util.VulasConfiguration;

import com.ibm.wala.util.graph.Graph;

public class DummyCallgraphConstructor implements ICallgraphConstructor {
  public String getFramework() {
    return "dummy";
  }

  public void setAppClasspath(String _cp) {}

  public void setDepClasspath(String _dependencyClasspath) {}

  public void setExcludePackages(String _packages) {}

  public Set<ConstructId> getEntrypoints() {
    return null;
  }

  public void buildCallgraph(boolean _policy) throws CallgraphConstructException {}

  public long getConstructionTime() {
    return 0L;
  }

  public Configuration getConfiguration() {
    return null;
  }

  public Graph<ConstructId> getCallgraph() {
    return null;
  }

  public void setEntrypoints(Set<ConstructId> _constructs) throws CallgraphConstructException {}

  public void setAppContext(Application _ctx) {}

  /** {@inheritDoc} */
  public void setVulasConfiguration(VulasConfiguration _cfg) {}

  public Configuration getConstructorConfiguration() {
    return null;
  }
}
