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
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.eclipse.steady.cg;

import org.junit.Test;

import static org.junit.Assert.*;

import org.eclipse.steady.cg.spi.CallgraphConstructorFactory;
import org.eclipse.steady.cg.spi.ICallgraphConstructor;
import org.eclipse.steady.shared.util.VulasConfiguration;

public class CallgraphConstructorFactoryTest {

  static {
    VulasConfiguration.getGlobal()
        .setProperty("vulas.reach.cli.plugins.dir", "target/test-classes");
  }

  @Test
  public void getDummyCallgraphServiceFromPluginFolder() {
    ICallgraphConstructor callgraphConstructor =
        CallgraphConstructorFactory.buildCallgraphConstructor("dummy", null, true);
    assertEquals(callgraphConstructor.getFramework(), "dummy");
    assertEquals(
        "org.eclipse.steady.cg.DummyCallgraphConstructor",
        callgraphConstructor.getClass().getName());
    assertTrue(callgraphConstructor instanceof ICallgraphConstructor);
  }

  @Test
  public void getDummyCallgraphServiceFromClasspath() {
    ICallgraphConstructor callgraphConstructor =
        CallgraphConstructorFactory.buildCallgraphConstructor("dummy", null, false);
    assertEquals(callgraphConstructor, null);
  }
}
