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
package org.eclipse.steady.java.mvn;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.AbstractAppGoal;
import org.eclipse.steady.goals.GoalExecutionException;
import org.eclipse.steady.java.ArchiveAnalysisManager;
import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.enums.Scope;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Dependency;
import org.eclipse.steady.shared.json.model.Library;
import org.eclipse.steady.shared.json.model.LibraryId;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.StringList;
import org.eclipse.steady.shared.util.StringList.CaseSensitivity;
import org.eclipse.steady.shared.util.StringList.ComparisonMode;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;

/**
 * <p>Abstract AbstractVulasMojo class.</p>
 */
public abstract class AbstractVulasMojo extends AbstractMojo {

  private static final String INCLUDES = "vulas.maven.includes";

  private static final String EXCLUDES = "vulas.maven.excludes";

  private static final String IGNORE_POMS = "vulas.maven.ignorePoms";

  /** Constant <code>PLUGIN_CFG_LAYER="Maven-Plugin-Config"</code> */
  protected static final String PLUGIN_CFG_LAYER = "Maven-Plugin-Config";

  @Parameter(defaultValue = "${project}", property = "project", required = true, readonly = true)
  protected MavenProject project;

  @Parameter(defaultValue = "${session}", property = "session", required = true, readonly = true)
  protected MavenSession session;

  /**
   * All plugin configuration settings of the element &lt;layeredConfiguration&gt; are put in this {@link Map}.
   */
  @Parameter private Map<?, ?> layeredConfiguration;

  protected AbstractAppGoal goal = null;

  private StringList includeArtifacts = null;
  private StringList excludeArtifacts = null;
  private boolean ignorePoms = false;

  /** The configuration used throughout the execution of the goal. */
  protected VulasConfiguration vulasConfiguration = new VulasConfiguration();

  /**
   * Puts the plugin configuration element &lt;layeredConfiguration&gt; as a new layer into {@link VulasConfiguration}.
   * If no such element exists, e.g., because the POM file does not contain a plugin section for Vulas, default settings
   * are established using {@link MavenProject} and {@link VulasConfiguration#setPropertyIfEmpty(String, Object)}.
   *
   * @throws java.lang.Exception
   */
  public final void prepareConfiguration() throws Exception {

    // Delete any transient settings that remaining from a previous goal execution (if any)
    final boolean contained_values = this.vulasConfiguration.clearTransientProperties();
    if (contained_values) getLog().info("Transient configuration settings deleted");

    // Get the configuration layer from the plugin configuration (can be null)
    this.vulasConfiguration.addLayerAfterSysProps(
        PLUGIN_CFG_LAYER, this.layeredConfiguration, null, true);

    // Check whether the application context can be established
    Application app = null;
    try {
      app = CoreConfiguration.getAppContext(this.vulasConfiguration);
    }
    // In case the plugin is called w/o using the Vulas profile, project-specific settings are not
    // set
    // Set them using the project member
    catch (ConfigurationException e) {
      this.vulasConfiguration.setPropertyIfEmpty(
          CoreConfiguration.APP_CTX_GROUP, this.project.getGroupId());
      this.vulasConfiguration.setPropertyIfEmpty(
          CoreConfiguration.APP_CTX_ARTIF, this.project.getArtifactId());
      this.vulasConfiguration.setPropertyIfEmpty(
          CoreConfiguration.APP_CTX_VERSI, this.project.getVersion());
      app = CoreConfiguration.getAppContext(this.vulasConfiguration);
    }

    // Set defaults for all the paths
    this.vulasConfiguration.setPropertyIfEmpty(
        VulasConfiguration.TMP_DIR,
        Paths.get(this.project.getBuild().getDirectory(), "vulas", "tmp").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.UPLOAD_DIR,
        Paths.get(this.project.getBuild().getDirectory(), "vulas", "upload").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.INSTR_SRC_DIR,
        Paths.get(this.project.getBuild().getDirectory()).toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.INSTR_TARGET_DIR,
        Paths.get(this.project.getBuild().getDirectory(), "vulas", "target").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.INSTR_INCLUDE_DIR,
        Paths.get(this.project.getBuild().getDirectory(), "vulas", "include").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.INSTR_LIB_DIR,
        Paths.get(this.project.getBuild().getDirectory(), "vulas", "lib").toString());
    this.vulasConfiguration.setPropertyIfEmpty(
        CoreConfiguration.REP_DIR,
        Paths.get(this.project.getBuild().getDirectory(), "vulas", "report").toString());

    // Read app constructs from src/main/java and target/classes
    final String p =
        Paths.get(this.project.getBuild().getOutputDirectory()).toString()
            + ","
            + Paths.get(this.project.getBuild().getSourceDirectory()).toString();
    this.vulasConfiguration.setPropertyIfEmpty(CoreConfiguration.APP_DIRS, p);

    // Test how-to get the reactor POM in a reliable manner
    // The following method call fails if Maven is called with option -pl
    getLog().info("Top level project: " + this.session.getTopLevelProject());
    getLog().info("Execution root dir: " + this.session.getExecutionRootDirectory());

    // Includes, excludes and ignorePoms
    this.includeArtifacts = new StringList(this.vulasConfiguration.getStringArray(INCLUDES, null));
    this.excludeArtifacts = new StringList(this.vulasConfiguration.getStringArray(EXCLUDES, null));
    this.ignorePoms = this.vulasConfiguration.getConfiguration().getBoolean(IGNORE_POMS, false);
  }

  /**
   * This method, called by Maven, first invokes {@link AbstractVulasMojo#createGoal()} and then {@link AbstractVulasMojo#executeGoal()}.
   *
   * @throws org.apache.maven.plugin.MojoExecutionException if any.
   * @throws org.apache.maven.plugin.MojoFailureException if any.
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      this.prepareConfiguration();

      final boolean do_process =
          this instanceof MvnPluginReport || this.isPassingFilter(this.project);
      if (do_process) {
        // Create the goal
        this.createGoal();
        this.goal.setGoalClient(GoalClient.MAVEN_PLUGIN);
        this.goal.setConfiguration(this.vulasConfiguration);

        // Set the application paths
        this.goal.addAppPaths(
            FileUtil.getPaths(
                this.vulasConfiguration.getStringArray(CoreConfiguration.APP_DIRS, null)));

        // Set the dependency paths
        this.setKnownDependencies();

        // Execute the goal
        this.executeGoal();
      }
    }
    // Expected problems will be passed on as-is
    catch (MojoFailureException mfe) {
      throw mfe;
    }
    // Unexpected problems (the goal execution terminates abnormally/unexpectedly)
    catch (GoalExecutionException gee) {
      throw new MojoExecutionException(gee.getMessage(), gee);
    }
    // Every other exception results in a MojoExecutionException (= unexpected)
    catch (Exception e) {
      throw new MojoExecutionException("Error during goal execution " + this.goal + ": ", e);
    }
  }

  /**
   * Evaluates the configuration settings {@link AbstractVulasMojo#INCLUDES}, {@link AbstractVulasMojo#EXCLUDES} and {@link #IGNORE_POMS} to
   * determine whether the given {@link MavenProject} shall be processed.
   *
   * @param _prj a {@link org.apache.maven.project.MavenProject} object.
   * @return a boolean.
   */
  protected boolean isPassingFilter(MavenProject _prj) {
    boolean do_process = true;

    // Only included ones
    if (!this.includeArtifacts.isEmpty()) {
      do_process =
          this.includeArtifacts.contains(
              _prj.getArtifactId(), ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE);
      if (do_process)
        this.getLog()
            .info(
                "Artifact ["
                    + _prj.getArtifactId()
                    + "] explicitly included for processing via configuration parameter ["
                    + INCLUDES
                    + "]");
      else
        this.getLog()
            .warn(
                "Artifact ["
                    + _prj.getArtifactId()
                    + "] will NOT be processed, it is not among those explicitly included for"
                    + " processing via configuration parameter ["
                    + INCLUDES
                    + "]");
    }

    // Excluded (explicitly or through packaging)
    else {
      if (!this.excludeArtifacts.isEmpty()
          && this.excludeArtifacts.contains(
              _prj.getArtifactId(), ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE)) {
        this.getLog()
            .warn(
                "Artifact ["
                    + _prj.getArtifactId()
                    + "] explicitly excluded from processing via configuration parameter ["
                    + EXCLUDES
                    + "]");
        do_process = false;
      }
      if (do_process && this.ignorePoms && "POM".equalsIgnoreCase(_prj.getPackaging())) {
        this.getLog()
            .warn(
                "Artifact ["
                    + _prj.getArtifactId()
                    + "] excluded from processing via configuration parameter ["
                    + IGNORE_POMS
                    + "]");
        do_process = false;
      }
    }

    return do_process;
  }

  /**
   * Creates the respective goal.
   * <p>
   * MUST be overridden by subclasses.
   */
  protected abstract void createGoal();

  /**
   * Calls {@link AbstractAppGoal#executeSync()}.
   * <p>
   * CAN be overridden by subclasses.
   *
   * @throws java.lang.Exception if any.
   */
  protected void executeGoal() throws Exception {
    this.goal.executeSync();
  }

  /**
   * Identifies known dependencies and passes them to Mojos inheriting from {@link AbstractAppGoal}.
   * Note that such subclasses should be annotated with "requiresDependencyResolution = ResolutionScope.TEST".
   *
   * @throws DependencyResolutionRequiredException
   */
  private final void setKnownDependencies() throws DependencyResolutionRequiredException {
    if (this.goal != null && this.goal instanceof AbstractAppGoal) {

      // Paths to Steady dependencies
      final Map<Path, Dependency> dep_for_path = new HashMap<Path, Dependency>();
      int count = 0;
      for (Artifact a : project.getArtifacts()) {

        // Create library
        final Library lib = new Library();
        lib.setLibraryId(new LibraryId(a.getGroupId(), a.getArtifactId(), a.getVersion()));
        lib.setDigest(FileUtil.getDigest(a.getFile(), DigestAlgorithm.SHA1));

        // Create dependency
        final Dependency dep =
            new Dependency(
                this.goal.getGoalContext().getApplication(),
                lib,
                Scope.fromString(a.getScope().toUpperCase(), Scope.RUNTIME),
                false, // Direct by default, may be changed to transitive below
                null,
                a.getFile().toPath().toString());

        // Set parent dependency (if there is any and it is NOT an intra-project Maven dependency
        // with path target/classes)
        final LibraryId parent = this.getParent(a.getDependencyTrail());

        // Parent found in dependency trail (a string) -> Find and set parent dependency
        if (parent != null) {
          for (Dependency parent_dep : dep_for_path.values()) {
            final File artifact_file = Paths.get(parent_dep.getPath()).toFile();
            if (parent_dep.getLib().getLibraryId().equals(parent)
                && !artifact_file.isDirectory()
                && ArchiveAnalysisManager.canAnalyze(artifact_file)) {
              dep.setParent(parent_dep);
              dep.setTransitive(true);
              break;
            }
          }
        }

        dep_for_path.put(a.getFile().toPath(), dep);

        getLog()
            .info(
                "Dependency ["
                    + StringUtil.padLeft(++count, 4)
                    + "]: Dependency [libid="
                    + dep.getLib().getLibraryId()
                    + ", parent="
                    + (dep.getParent() == null ? "null" : dep.getParent().getLib().getLibraryId())
                    + ", path="
                    + a.getFile().getPath()
                    + ", direct="
                    + !dep.getTransitive()
                    + ", scope="
                    + dep.getScope()
                    + "] created for Maven artifact [g="
                    + a.getGroupId()
                    + ", a="
                    + a.getArtifactId()
                    + ", base version="
                    + a.getBaseVersion()
                    + ", version="
                    + a.getVersion()
                    + ", classifier="
                    + a.getClassifier()
                    + "]");
        getLog().info("    " + StringUtil.join(a.getDependencyTrail(), " => "));
      }

      // TODO: Is it necessary to check whether the above dependency (via getArtifacts) is actually
      // the one added to the classpath (via project.getRuntimeClasspathElements())?
      // TODO: It may be that a different version (file) is chosen due to conflict resolution.
      // Still, those cases should also be visible in the frontend (archive view).

      ((AbstractAppGoal) this.goal).setKnownDependencies(dep_for_path);
    }
  }

  /**
   * <p>getParent.</p>
   *
   * @param _trail a {@link java.util.List} object.
   * @return a {@link org.eclipse.steady.shared.json.model.LibraryId} object.
   */
  protected final LibraryId getParent(List<String> _trail) {
    LibraryId parent = null;
    // Should not occur
    if (_trail == null || _trail.size() < 2) {
      getLog().warn("Invalid dependency trail [" + _trail + "]");
    }
    // Dependency is direct, there's no parent
    else if (_trail.size() == 2) {
      ;
    }
    // Dependency is transitive, get parent
    else {
      parent = this.parseGAPV(_trail.get(_trail.size() - 2));
    }
    return parent;
  }

  /**
   * Parses one element of the {@link Artifact}'s dependency trail, which is a {@link String} comprising groupId, artifactId, type and version.
   *
   * @param _string a {@link java.lang.String} object
   * @return a {@link LibraryId} created from groupId, artifactId and version (or null if the given String does not have the expected format)
   */
  protected final LibraryId parseGAPV(@NotNull String _string) {
    final String[] gapv = _string.split(":");
    if (gapv.length != 4) {
      getLog().warn("Could not identify GAPV in [" + _string + "]");
      return null;
    } else {
      return new LibraryId(gapv[0], gapv[1], gapv[3]);
    }
  }
}
