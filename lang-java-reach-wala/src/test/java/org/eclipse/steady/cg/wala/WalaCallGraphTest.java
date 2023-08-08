/**
 * This file is part of Eclipse Steady.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>SPDX-License-Identifier: Apache-2.0 SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or
 * an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.cg.wala;
/**
 * This file is part of Eclipse Steady.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>SPDX-License-Identifier: Apache-2.0
 *
 * <p>Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.steady.cg.ReachabilityAnalyzer;
import org.eclipse.steady.cg.spi.CallgraphConstructorFactory;
import org.eclipse.steady.cg.spi.ICallgraphConstructor;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.GoalContext;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.shared.enums.PathSource;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.junit.Test;

public class WalaCallGraphTest {

  static {
    VulasConfiguration.getGlobal()
        .setProperty(
            CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE.toString());
  }

  private GoalContext getGoalContext() {
    final GoalContext ctx = new GoalContext();
    ctx.setApplication(new Application("foo", "bar", "0.0"));
    ctx.setVulasConfiguration(new VulasConfiguration());
    return ctx;
  }

  @Test
  public void callgraphServiceRegistered() {
    ICallgraphConstructor callgraphConstructor =
        CallgraphConstructorFactory.buildCallgraphConstructor("wala", null, false);
    assertEquals(callgraphConstructor.getFramework(), "wala");
    assertEquals(
        callgraphConstructor.getClass().getName(), WalaCallgraphConstructor.class.getName());
    assertTrue(callgraphConstructor instanceof ICallgraphConstructor);
  }

  @Test
  public void examplesWalaTest() {
    final ReachabilityAnalyzer ra = new ReachabilityAnalyzer(this.getGoalContext());
    ra.setCallgraphConstructor(WalaCallgraphConstructor.FRAMEWORK, false);

    // Set classpaths
    final Set<Path> app_paths = new HashSet<Path>(), dep_paths = new HashSet<Path>();
    app_paths.add(Paths.get("./src/test/resources/examples.jar"));
    dep_paths.add(Paths.get("./src/test/resources/empty.jar"));
    ra.setAppClasspaths(app_paths);
    ra.setDependencyClasspaths(dep_paths);

    // Set the EP manually
    final Set<ConstructId> entrypoints = new HashSet<ConstructId>();
    entrypoints.add(
        JavaId.toSharedType(
            JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Examples.main(String[])")));
    ra.setEntryPoints(entrypoints, PathSource.A2C, false);
    ra.setAppConstructs(entrypoints);

    // Set the target constructs (manually, rather than using a bug)
    final Map<String, Set<ConstructId>> target_constructs = new HashMap<String, Set<ConstructId>>();
    final Set<ConstructId> changes = new HashSet<ConstructId>();
    changes.add(
        JavaId.toSharedType(
            JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Cat.saySomething()")));
    changes.add(
        JavaId.toSharedType(
            JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Fish.saySomething()")));
    changes.add(
        JavaId.toSharedType(
            JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Dog.saySomething()")));
    changes.add(
        JavaId.toSharedType(
            JavaId.parseMethodQName("com.sap.psr.vulas.cg.test.Car.saySomething()")));
    target_constructs.put("does-not-exist", changes);
    ra.setTargetConstructs(target_constructs);

    try {
      ReachabilityAnalyzer.startAnalysis(ra, 600000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void examplesWalaTestJdk17() {
    final ReachabilityAnalyzer ra = new ReachabilityAnalyzer(this.getGoalContext());
    ra.setCallgraphConstructor(WalaCallgraphConstructor.FRAMEWORK, false);

    // Set classpaths
    final Set<Path> app_paths = new HashSet<Path>(), dep_paths = new HashSet<Path>();
    app_paths.add(Paths.get("./src/test/resources/examplesJdk17.jar"));
    dep_paths.add(Paths.get("./src/test/resources/empty.jar"));
    ra.setAppClasspaths(app_paths);
    ra.setDependencyClasspaths(dep_paths);

    // Set the EP manually
    final Set<ConstructId> entrypoint = new HashSet<ConstructId>();
    entrypoint.add(
        JavaId.toSharedType(JavaId.parseMethodQName("org.example.Examples.main(String[])")));
    ra.setEntryPoints(entrypoint, PathSource.A2C, false);
    ra.setAppConstructs(entrypoint);

    // Set the target constructs (manually, rather than using a bug)
    final Map<String, Set<ConstructId>> target_constructs = new HashMap<String, Set<ConstructId>>();
    final Set<ConstructId> changes = new HashSet<ConstructId>();
    changes.add(JavaId.toSharedType(JavaId.parseMethodQName("org.example.Cat.saySomething()")));
    changes.add(JavaId.toSharedType(JavaId.parseMethodQName("org.example.Fish.saySomething()")));
    changes.add(JavaId.toSharedType(JavaId.parseMethodQName("org.example.Dog.saySomething()")));
    changes.add(JavaId.toSharedType(JavaId.parseMethodQName("org.example.Car.saySomething()")));
    target_constructs.put("does-not-exist", changes);
    ra.setTargetConstructs(target_constructs);

    try {
      ReachabilityAnalyzer.startAnalysis(ra, 600000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
