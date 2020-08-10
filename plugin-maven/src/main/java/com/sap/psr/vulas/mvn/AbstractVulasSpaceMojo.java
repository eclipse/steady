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
package com.sap.psr.vulas.mvn;

import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractSpaceGoal;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>Abstract AbstractVulasSpaceMojo class.</p>
 *
 */
public abstract class AbstractVulasSpaceMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", property = "session", required = true, readonly = true)
    protected MavenSession session;

    /**
     * All plugin configuration settings of the element <layeredConfiguration> are put in this {@link Map}.
     */
    @Parameter private Map layeredConfiguration;

    protected AbstractSpaceGoal goal = null;

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
                "Plugin configuration", this.layeredConfiguration, null, true);

        // Check whether the application context can be established
        Application app = null;
        try {
            app = CoreConfiguration.getAppContext(this.vulasConfiguration);
        }
        // In case the plugin is called w/o using the Vulas profile, project-specific settings are
        // not set
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
    }

    /**
     * This method, called by Maven, first invokes {@link AbstractVulasSpaceMojo#createGoal()} and then {@link AbstractVulasSpaceMojo#executeGoal()}.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException if any.
     * @throws org.apache.maven.plugin.MojoFailureException if any.
     */
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            this.prepareConfiguration();

            // Create the goal
            this.createGoal();
            this.goal.setGoalClient(GoalClient.MAVEN_PLUGIN);
            this.goal.setConfiguration(this.vulasConfiguration);

            // Execute the goal
            this.executeGoal();
        }
        // Expected problems will be passed on as is
        catch (MojoFailureException mfe) {
            throw mfe;
        }
        // Unexpected problems (the goal execution terminates abnormally/unexpectedly)
        catch (GoalExecutionException gee) {
            throw new MojoExecutionException(gee.getMessage(), gee);
        }
        // Every other exception results in a MojoExecutionException (= unexpected)
        catch (Exception e) {
            throw new MojoExecutionException(
                    "Error during Vulas goal execution " + this.goal + ": ", e);
        }
    }

    /**
     * Creates the respective goal.
     *
     * MUST be overridden by subclasses.
     */
    protected abstract void createGoal();

    /**
     * Calls {@link AbstractSpaceGoal#executeSync()}.
     *
     * CAN be overridden by subclasses.
     *
     * @throws java.lang.Exception if any.
     */
    protected void executeGoal() throws Exception {
        this.goal.executeSync();
    }
}
