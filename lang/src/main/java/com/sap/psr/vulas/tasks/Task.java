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
package com.sap.psr.vulas.tasks;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>Task interface.</p>
 *
 */
public interface Task {

    // ====================  Setter methods used to passed general context information to the task

    /**
     * Sets the {@link GoalClient} in whose context the task is executed.
     *
     * @param _client a {@link com.sap.psr.vulas.shared.enums.GoalClient} object.
     */
    public void setGoalClient(GoalClient _client);

    /**
     * Sets the {@link Application} in whose context the task is executed.
     *
     * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
     */
    public void setApplication(Application _app);

    /**
     * Sets one or more file systems {@link Path}s that contain application code and/or dependencies.
     *
     * @param _paths a {@link java.util.List} object.
     */
    public void setSearchPaths(List<Path> _paths);

    /**
     * Provides the task with application {@link Dependency}s that are already known before task execution.
     * This information may come from build systems or the like, and typically facilitates the task execution.
     *
     * @param _known_dependencies a {@link java.util.Map} object.
     */
    public void setKnownDependencies(Map<Path, Dependency> _known_dependencies);

    // ==================== Configure, execute and clean up

    /**
     * Called prior to {@link Task#execute()}.
     *
     * @throws com.sap.psr.vulas.goals.GoalConfigurationException
     * @param _cfg a {@link com.sap.psr.vulas.shared.util.VulasConfiguration} object.
     */
    public void configure(VulasConfiguration _cfg) throws GoalConfigurationException;

    /**
     * Performs the actual application analysis.
     * Right after, task-specific methods can be used to retrieve the analysis results.
     *
     * @throws com.sap.psr.vulas.goals.GoalExecutionException
     */
    public void execute() throws GoalExecutionException;

    /**
     * Called after {@link Task#execute()}.
     */
    public void cleanUp();

    // ====================  Getter methods are mostly task-specific and exist in sub-interfaces

    /**
     * The analysis of an application is typically dependent on its {@link ProgrammingLanguage}s.
     * This method returns the {@link ProgrammingLanguage}s covered by the {@link Task}.
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<ProgrammingLanguage> getLanguage();
}
