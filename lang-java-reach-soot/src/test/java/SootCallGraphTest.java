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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.steady.cg.ReachabilityAnalyzer;
import org.eclipse.steady.cg.soot.SootCallgraphConstructor;
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

public class SootCallGraphTest {

  static {
    VulasConfiguration.getGlobal()
        .setProperty(
            CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE.toString());
  }

  private GoalContext getGoalContext() {
    final GoalContext ctx = new GoalContext();
    ctx.setApplication(new Application("foo", "bar", "0.0"));
    ctx.setVulasConfiguration(VulasConfiguration.getGlobal());
    return ctx;
  }

  private void runSootAnalysis(String jarPAth) {
    final ReachabilityAnalyzer ra = new ReachabilityAnalyzer(this.getGoalContext());
    ra.setCallgraphConstructor(SootCallgraphConstructor.FRAMEWORK, false);
    // Set classpaths
    final Set<Path> app_paths = new HashSet<Path>(), dep_paths = new HashSet<Path>();
    app_paths.add(Paths.get(jarPAth));
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
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void callgraphServiceRegistered() {
    ICallgraphConstructor callgraphConstructor =
        CallgraphConstructorFactory.buildCallgraphConstructor("soot", null, false);
    assertEquals(callgraphConstructor.getFramework(), "soot");
    assertEquals(
        callgraphConstructor.getClass().getName(), SootCallgraphConstructor.class.getName());
    assertTrue(callgraphConstructor instanceof ICallgraphConstructor);
  }

  @Test
  public void examplesSootTestNoneEntrypointGenerator() {
    VulasConfiguration.getGlobal().setProperty("vulas.reach.soot.entrypointGenerator", "none");
    runSootAnalysis("./src/test/resources/examples.jar");
  }

  @Test
  public void examplesSootTestNoneEntrypointGeneratorJDK17() {
    VulasConfiguration.getGlobal().setProperty("vulas.reach.soot.entrypointGenerator", "none");
    runSootAnalysis("./src/test/resources/examples17.jar");
  }

  @Test
  public void examplesSootTestDefaultEntryPointGenerator() {
    VulasConfiguration.getGlobal()
        .setProperty(
            "vulas.reach.soot.entrypointGenerator",
            "soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator");
    runSootAnalysis("./src/test/resources/examples.jar");
  }

  @Test
  public void examplesSootTestDefaultEntryPointGeneratorJDK17() {
    VulasConfiguration.getGlobal()
        .setProperty(
            "vulas.reach.soot.entrypointGenerator",
            "soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator");
    runSootAnalysis("./src/test/resources/examples17.jar");
  }

  @Test
  public void examplesSootTestCustomEntryPointGenerator() {
    VulasConfiguration.getGlobal()
        .setProperty(
            "vulas.reach.soot.entrypointGenerator",
            "org.eclipse.steady.cg.soot.CustomEntryPointCreator");
    runSootAnalysis("./src/test/resources/examples.jar");
  }

  @Test
  public void examplesSootTestCustomEntryPointGeneratorJDK17() {
    VulasConfiguration.getGlobal()
        .setProperty(
            "vulas.reach.soot.entrypointGenerator",
            "org.eclipse.steady.cg.soot.CustomEntryPointCreator");
    runSootAnalysis("./src/test/resources/examples17.jar");
  }
}
