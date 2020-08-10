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

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.java.goals.InstrGoal;
import com.sap.psr.vulas.shared.util.FileUtil;

/**
 * This plugin analyzes all Java archives in a given Maven project in order to identify all their Java constructs.
 * Those are then uploaded to a remote service for further analysis (test coverage, vulnerability assessments, archive integrity).
 * The plugin can be executed for Eclipse projects through 'Run As' &gt; 'Maven build...' &gt; Goal 'vulas:instr'.
 *
 * help:describe -Dplugin=com.sap.research.security.vulas:vulas-maven-plugin
 */
@Mojo(
        name = "instr",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresOnline = true)
public class MvnPluginInstr extends AbstractVulasMojo {

    @Parameter(property = "plugin.artifactMap", required = true, readonly = true)
    private Map<String, Artifact> pluginArtifactMap;

    private static final String VULAS_AGENT_ARTIFACT_NAME =
            "com.sap.research.security.vulas:lang-java";
    private static final String VULAS_AGENT_ARTIFACT_CLASSIFIER = "jar-with-dependencies";

    /** {@inheritDoc} */
    @Override
    protected void createGoal() {
        this.goal = new InstrGoal();
    }

    /** {@inheritDoc} */
    @Override
    protected void executeGoal() throws Exception {
        // Copy the agent JAR into the include folder
        final Path lib_dir = this.vulasConfiguration.getDir(CoreConfiguration.INSTR_LIB_DIR);
        final Path incl_dir = this.vulasConfiguration.getDir(CoreConfiguration.INSTR_INCLUDE_DIR);

        final Path incl_agent = FileUtil.copyFile(this.getAgentJarFile().toPath(), incl_dir);
        final Path lib_agent = FileUtil.copyFile(this.getAgentJarFile().toPath(), lib_dir);

        getLog().info(
                        "Copied ["
                                + this.getAgentJarFile().toPath()
                                + "] to ["
                                + incl_agent
                                + "] and ["
                                + lib_agent
                                + "]");
        super.executeGoal();
    }

    private File getAgentJarFile() throws MojoExecutionException {
        final Artifact vulasAgentArtifact = pluginArtifactMap.get(VULAS_AGENT_ARTIFACT_NAME);
        if (vulasAgentArtifact == null
                || !vulasAgentArtifact.hasClassifier()
                || !vulasAgentArtifact.getClassifier().equals(VULAS_AGENT_ARTIFACT_CLASSIFIER)) {
            throw new MojoExecutionException(
                    "Could not found "
                            + VULAS_AGENT_ARTIFACT_NAME
                            + ":"
                            + VULAS_AGENT_ARTIFACT_CLASSIFIER);
        }
        return vulasAgentArtifact.getFile();
    }
}
