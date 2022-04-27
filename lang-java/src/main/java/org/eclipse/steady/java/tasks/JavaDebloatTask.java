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
package org.eclipse.steady.java.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.GoalExecutionException;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.json.model.Dependency;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.tasks.AbstractTask;
import org.eclipse.steady.tasks.DebloatTask;
import org.vafer.jdependency.Clazz;
import org.vafer.jdependency.Clazzpath;
import org.vafer.jdependency.ClazzpathUnit;

/**
 * <p>JavaBomTask class.</p>
 */
public class JavaDebloatTask extends AbstractTask implements DebloatTask {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static final String[] EXT_FILTER = new String[] {"jar", "war", "class", "java", "aar"};

  private Set<ConstructId> traces = null;

  private Set<Dependency> dependencies = null;

  private static final List<GoalClient> pluginGoalClients =
      Arrays.asList(GoalClient.MAVEN_PLUGIN, GoalClient.GRADLE_PLUGIN);

  /** {@inheritDoc} */
  @Override
  public Set<ProgrammingLanguage> getLanguage() {
    return new HashSet<ProgrammingLanguage>(
        Arrays.asList(new ProgrammingLanguage[] {ProgrammingLanguage.JAVA}));
  }

  /** {@inheritDoc} */
  @Override
  public void execute() throws GoalExecutionException {

    final Clazzpath cp = new Clazzpath();

    // list of classpathunits to be used as application entrypoints
    List<ClazzpathUnit> app = new ArrayList<ClazzpathUnit>();
    // list of classpathunits to be used as test entrypoints
    List<ClazzpathUnit> test = new ArrayList<ClazzpathUnit>();

    // list of classes from Steasy traces/reachableConstructs (also used as entrypoints)
    final SortedSet<String> used_classes = new TreeSet<String>();
    // classpathunits for the application dependencies (as identified by maven)
    Set<ClazzpathUnit> maven_deps = new HashSet<ClazzpathUnit>();
    // classpathunits considered used according to traces, reachable constructs and jdependency
    // analysis
    Set<ClazzpathUnit> deps_used = new HashSet<ClazzpathUnit>();

    try {
      // 1) Add application paths (to be then used as entrypoints)
      if (this.hasSearchPath()) {
        for (Path p : this.getSearchPath()) {
          log.info("Add app path [" + p + "] to classpath");
          app.add(cp.addClazzpathUnit(p));
        }
      }
      for (Path p :
          FileUtil.getPaths(
              this.vulasConfiguration.getStringArray(CoreConfiguration.TEST_DIRS, null))) {
        log.info("Add test path [" + p + "] to classpath");
        test.add(cp.addClazzpathUnit(p));
      }
      // 2) Add dependencies to jdependency classpath object and populate set of dependencies
      if (this.getKnownDependencies() != null) {
        for (Path p : this.getKnownDependencies().keySet()) {
          maven_deps.add(cp.addClazzpathUnit(p));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    log.info("[" + app.size() + "] ClasspathUnit for the application to be used as entrypoints ");
    log.info("[" + test.size() + "] ClasspathUnit for tests to be used as entrypoints ");
    log.info("[" + cp.getUnits().length + "] classpathUnits in jdependency classpath object");

    // Retrieve traces to be used as Clazz entrypoints (1 class may be part of multiple
    // classpathUnits)
    for (ConstructId t : traces) {
      if (t.getType().equals(ConstructType.CLAS)
          || t.getType().equals(ConstructType.INTF)
          || t.getType().equals(ConstructType.ENUM)) {
        used_classes.add(t.getQname());
      }
    }
    log.info("Retrieved [" + used_classes.size() + "] clazzes from steady traces");

    // Loop reachable constructs (METH, CONS, INIT), find their definition
    // context (CLASS, ENUM, INTF) and use those as jdependency clazz
    // entrypoints (1 class may be part of multiple classpathUnits).
    for (Dependency d : dependencies) {
      log.info(
          "Processing ["
              + d.getReachableConstructIds().size()
              + "] reachable constructs of "
              + d.getLib().getLibraryId());
      if (d.getReachableConstructIds() != null) {
        for (ConstructId c : d.getReachableConstructIds()) {
          JavaId core_construct = (JavaId) JavaId.toCoreType(c);
          JavaId def_context = (JavaId) core_construct.getDefinitionContext();
          used_classes.add(def_context.getQualifiedName());
        }
      }
    }

    log.info("Using [" + used_classes.size() + "] clazzes from Steady traces/reachable constructs");

    // also collect "missing" classes to be able to check their relation with jre objects considered
    // used
    // to be removed from final version
    SortedSet<Clazz> missing = new TreeSet<Clazz>();
    for (Clazz c : cp.getMissingClazzes()) {
      missing.add(c);
    }

    // Classes considered used
    final SortedSet<String> needed = new TreeSet<String>();
    final Set<Clazz> removable = cp.getClazzes();

    // loop over classpathunits (representing the application) marked as entrypoints to find needed
    // classes
    for (ClazzpathUnit u : app) {
      removable.removeAll(u.getClazzes());
      removable.removeAll(u.getTransitiveDependencies());
      for (Clazz c : u.getClazzes()) {
        needed.add(c.getName());
      }
      for (Clazz c : u.getTransitiveDependencies()) {
        needed.add(c.getName());
        deps_used.addAll(c.getClazzpathUnits());
      }
    }

    // loop over class (representing traces and reachable constructs) and use them as entrypoints to
    // find
    // needed classes
    for (String class_name : used_classes) {
      needed.add(class_name);

      Clazz c = cp.getClazz(class_name);

      if (c == null) {
        log.warn("Could not obtain jdependency clazz for steady class [" + class_name + "]");
      } else {
        for (Clazz cl : c.getTransitiveDependencies()) {
          needed.add(cl.getName());
          deps_used.addAll(cl.getClazzpathUnits());
        }
        Set<ClazzpathUnit> units = c.getClazzpathUnits();
        if (units.size() > 1) {
          log.warn(
              "Added as entrypoints multiple ClasspathUnits from single class ["
                  + c
                  + "] : ["
                  + StringUtil.join(units, ",")
                  + "]");
        }
        deps_used.addAll(units);

        if (removable.contains(c)) {
          removable.remove(c);
          removable.removeAll(c.getTransitiveDependencies());
        }
      }
    }

    final SortedSet<Clazz> removable_sorted = new TreeSet<Clazz>(removable);

    Set<Clazz> remaining = cp.getClazzes();
    remaining.removeAll(removable_sorted);
    final SortedSet<Clazz> remaining_sorted = new TreeSet<Clazz>(remaining);
    log.info("jdependency classpath classes size: [" + cp.getClazzes().size() + "]");
    log.info("Needed classes: [" + needed.size() + "]");
    log.info("Needed by difference: [" + remaining_sorted.size() + "]");
    log.info("Removable classes: [" + removable_sorted.size() + "]");
    log.info("Used dependencies: [" + deps_used.size() + "] out of [" + maven_deps.size() + "]");

    maven_deps.removeAll(deps_used);

    // Write names of needed/removable classes and dependencies to disk
    try {
      File f =
          Paths.get(
                  this.vulasConfiguration.getDir(CoreConfiguration.SLICING_DIR).toString(),
                  "removable-classes.txt")
              .toFile();
      FileUtil.writeToFile(f, StringUtil.join(removable_sorted, System.lineSeparator()));
      log.info("List of removable classes written to [" + f.toPath() + "]");

      f =
          Paths.get(
                  this.vulasConfiguration.getDir(CoreConfiguration.SLICING_DIR).toString(),
                  "used-classes-steady.txt")
              .toFile();
      FileUtil.writeToFile(f, StringUtil.join(used_classes, System.lineSeparator()));
      log.info(
          "List of used classes according to steady backend data (no jdependency) written to ["
              + f.toPath()
              + "]");

      f =
          Paths.get(
                  this.vulasConfiguration.getDir(CoreConfiguration.SLICING_DIR).toString(),
                  "needed-classes.txt")
              .toFile();
      FileUtil.writeToFile(f, StringUtil.join(needed, System.lineSeparator()));
      log.info("List of needed classes written to [" + f.toPath() + "]");

      f =
          Paths.get(
                  this.vulasConfiguration.getDir(CoreConfiguration.SLICING_DIR).toString(),
                  "remaining-classes.txt")
              .toFile();
      FileUtil.writeToFile(f, StringUtil.join(remaining_sorted, System.lineSeparator()));
      log.info("List of remaining classes written to [" + f.toPath() + "]");

      f =
          Paths.get(
                  this.vulasConfiguration.getDir(CoreConfiguration.SLICING_DIR).toString(),
                  "missing-classes.txt")
              .toFile();
      FileUtil.writeToFile(f, StringUtil.join(missing, System.lineSeparator()));
      log.info("List of missing classes written to [" + f.toPath() + "]");

      f =
          Paths.get(
                  this.vulasConfiguration.getDir(CoreConfiguration.SLICING_DIR).toString(),
                  "removable-deps.txt")
              .toFile();
      FileUtil.writeToFile(f, StringUtil.join(maven_deps, System.lineSeparator()));
      log.info("List of removable dependencies written to [" + f.toPath() + "]");
    } catch (IOException e) {
      e.printStackTrace();
    }

    // also consider test classes to find needed classes
    for (ClazzpathUnit u : test) {
      removable.removeAll(u.getClazzes());
      removable.removeAll(u.getTransitiveDependencies());
      for (Clazz c : u.getClazzes()) {
        needed.add(c.getName());
      }
      for (Clazz c : u.getTransitiveDependencies()) {
        needed.add(c.getName());
        deps_used.addAll(c.getClazzpathUnits());
      }
    }
    maven_deps.removeAll(deps_used);
    log.info("Needed classes with test: [" + needed.size() + "]");
    log.info("Removable classes with test: [" + removable.size() + "]");
    log.info("Used dependencies with test: [" + deps_used.size() + "]");

    try {
      File f =
          Paths.get(
                  this.vulasConfiguration.getDir(CoreConfiguration.SLICING_DIR).toString(),
                  "needed-classes-w-test.txt")
              .toFile();
      FileUtil.writeToFile(f, StringUtil.join(needed, System.lineSeparator()));
      log.info("List of needed classes with test written to [" + f.toPath() + "]");
      f =
          Paths.get(
                  this.vulasConfiguration.getDir(CoreConfiguration.SLICING_DIR).toString(),
                  "removable-deps-w-test.txt")
              .toFile();
      FileUtil.writeToFile(f, StringUtil.join(maven_deps, System.lineSeparator()));
      log.info("List of removable dependencies with test written to [" + f.toPath() + "]");

      f =
          Paths.get(
                  this.vulasConfiguration.getDir(CoreConfiguration.SLICING_DIR).toString(),
                  "used-deps.txt")
              .toFile();
      FileUtil.writeToFile(f, StringUtil.join(deps_used, System.lineSeparator()));
      log.info("List of used dependencies written to [" + f.toPath() + "]");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setTraces(Set<ConstructId> _traces) {
    this.traces = _traces;
  }

  /** {@inheritDoc} */
  @Override
  public void setReachableConstructIds(Set<Dependency> _deps) {
    this.dependencies = _deps;
  }
}
