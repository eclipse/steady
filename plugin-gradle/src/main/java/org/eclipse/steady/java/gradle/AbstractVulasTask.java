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
package org.eclipse.steady.java.gradle;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.api.AndroidSourceSet;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.AbstractAppGoal;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;

import org.apache.commons.configuration.ConfigurationException;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.JavaCompile;

import static org.eclipse.steady.java.gradle.GradleProjectUtilities.*;
import static org.eclipse.steady.java.gradle.VulasPluginCommon.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractVulasTask extends DefaultTask {

  protected Application app = null;

  protected AbstractAppGoal goal = null;
  protected Project project = this.getProject();
  protected ProjectOutputTypes projectOutputType;

  /** The configuration used throughout the execution of the goal. */
  protected VulasConfiguration vulasConfiguration = new VulasConfiguration();

  /**
   * Puts the plugin configuration element <layeredConfiguration> as a new layer into {@link VulasConfiguration}.
   * If no such element exists, e.g., because the POM file does not contain a plugin section for Vulas, default settings
   * are established using {@link MavenProject} and {@link VulasConfiguration#setPropertyIfEmpty(String, Object)}.
   *
   * @throws Exception
   */
  public final void prepareConfiguration() throws Exception {

    // Delete any transient settings that remaining from a previous goal execution (if any)
    final boolean contained_values = this.vulasConfiguration.clearTransientProperties();
    if (contained_values) {
      getLogger().info("Transient configuration settings deleted");
    }

    // Examining extra properties, if a property starts with "vulas" add it to
    // the configuration as transient property
    ExtraPropertiesExtension ext = project.getExtensions().getExtraProperties();

    for (Map.Entry<String, Object> extraProp : ext.getProperties().entrySet()) {
      if (extraProp.getKey().startsWith(VULAS_PLUGIN_NAME)) {
        this.vulasConfiguration.setProperty(extraProp.getKey(), extraProp.getValue());
        getLogger()
            .debug(
                "Added property to configuration {}={}", extraProp.getKey(), extraProp.getValue());
      }
    }

    // Check whether the application context can be established
    try {
      app = CoreConfiguration.getAppContext(this.vulasConfiguration);
    }
    // In case the plugin is called w/o using the Vulas profile, project-specific settings are not
    // set
    // Set them using the project member
    catch (ConfigurationException e) {

      this.vulasConfiguration.setProperty(
          CoreConfiguration.APP_CTX_GROUP,
          getMandatoryProjectProperty(project, GradleGavProperty.group, getLogger()));
      this.vulasConfiguration.setProperty(
          CoreConfiguration.APP_CTX_ARTIF,
          getMandatoryProjectProperty(project, GradleGavProperty.name, getLogger()));
      this.vulasConfiguration.setProperty(
          CoreConfiguration.APP_CTX_VERSI,
          getMandatoryProjectProperty(project, GradleGavProperty.version, getLogger()));
      // TODO: packaging is not straightforward with gradle, leave it empty for now
      // VulasConfiguration.setProperty(CoreConfiguration.APP_CTX_PACKA, "", "", true);
      app = CoreConfiguration.getAppContext(this.vulasConfiguration);
    }

    String rootDir = project.getProjectDir().getAbsolutePath();
    String buildDir = project.getBuildDir().getAbsolutePath();

    // Set defaults for all the paths
    this.vulasConfiguration.setPropertyIfEmpty(
        VulasConfiguration.TMP_DIR, Paths.get(buildDir, "vulas", "tmp").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.UPLOAD_DIR, Paths.get(buildDir, "vulas", "upload").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.INSTR_SRC_DIR, Paths.get(rootDir).toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.INSTR_TARGET_DIR, Paths.get(buildDir, "vulas", "target").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.INSTR_INCLUDE_DIR, Paths.get(buildDir, "vulas", "include").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.INSTR_LIB_DIR, Paths.get(buildDir, "vulas", "lib").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.REP_DIR, Paths.get(buildDir, "vulas", "report").toString());

    Set<String> appPaths = new HashSet<>();

    SourceSetContainer sourceSets;

    if (projectOutputType == ProjectOutputTypes.JAR) {
      sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets");
      SourceSet mainSourceSet = sourceSets.getAt("main");

      Set<File> sourceDirs = mainSourceSet.getAllJava().getSourceDirectories().getFiles();
      for (File sd : sourceDirs) {
        appPaths.add(sd.getAbsolutePath());
      }

      Set<File> outputDirs = mainSourceSet.getOutput().getClassesDirs().getFiles();
      for (File od : outputDirs) {
        appPaths.add(od.getAbsolutePath());
      }

    } else if (projectOutputType == ProjectOutputTypes.AAR
        || projectOutputType == ProjectOutputTypes.APK) {

      ExtensionContainer e = project.getExtensions();
      BaseExtension ae = (BaseExtension) e.getByName("android");

      AndroidSourceSet mainSourceSet = ae.getSourceSets().getByName("main");

      Set<File> srcDirs = mainSourceSet.getJava().getSrcDirs();

      for (File srcDir : srcDirs) {
        appPaths.add(srcDir.toString());
      }

      JavaCompile jc =
          (JavaCompile) project.getTasksByName("compileReleaseJavaWithJavac", true).toArray()[0];
      appPaths.add(jc.getDestinationDir().toString());
    }

    String strAppPaths = String.join(",", appPaths);
    getLogger().quiet("App paths: {}", strAppPaths);

    this.vulasConfiguration.setPropertyIfEmpty(CoreConfiguration.APP_DIRS, strAppPaths);

    getLogger()
        .info("Top level project: " + project.getRootProject().getBuildFile().getAbsolutePath());
    getLogger().info("Execution root dir: " + project.getRootProject().getProjectDir());

    this.vulasConfiguration.log(new String[] {VULAS_PLUGIN_NAME}, "    ");
  }

  @TaskAction
  public final void executeTask() throws Exception {

    projectOutputType = determineProjectOutputType(this.project, getLogger());

    this.prepareConfiguration();
    this.createGoal();
    this.goal.setGoalClient(GoalClient.GRADLE_PLUGIN);
    this.goal.setConfiguration(this.vulasConfiguration);

    // Set the application paths
    this.goal.addAppPaths(
        FileUtil.getPaths(
            this.vulasConfiguration.getStringArray(CoreConfiguration.APP_DIRS, null)));

    this.executeGoal();
  }

  protected abstract void createGoal();

  protected void executeGoal() throws Exception {
    this.goal.executeSync();
  }
}
