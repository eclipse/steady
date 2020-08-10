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
package com.sap.psr.vulas.java.goals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.bytecode.BytecodeComparator;
import com.sap.psr.vulas.goals.AbstractAppGoal;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.VulnerableDependency;

/**
 * For all non-assessed tuples (library,bug), the code of modified methods and constructors is compared with the
 * respective counterparts in libraries that have already been assessed as vulnerable or non-vulnerable.
 * Only if equality to all constructs of either the vulnerable or non-vulnerable counterparts is found,
 * the non-assessed library is assessed accordingly.
 * In more details, the code comparison is done by comparing the abstract syntax trees of the decompiled
 * bytecode.
 */
public class CheckBytecodeGoal extends AbstractAppGoal {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    /**
     * <p>
     * Constructor for CheckBytecodeGoal.
     * </p>
     */
    public CheckBytecodeGoal() {
        super(GoalType.CHECKCODE);
    }

    /** {@inheritDoc} */
    @Override
    protected void executeTasks() throws Exception {
        final Application app = this.getApplicationContext();

        final Map<Path, Dependency> deps = this.getKnownDependencies();

        final BytecodeComparator comparator = new BytecodeComparator(this.getGoalContext());

        // Loop over all vulnerable dependencies that are NOT assessed (orange hour glasses)
        final Set<VulnerableDependency> vulndeps =
                BackendConnector.getInstance()
                        .getAppVulnDeps(this.getGoalContext(), app, false, false, true);

        for (VulnerableDependency vulndep : vulndeps) {
            if (vulndep.getAffectedVersionConfirmed()
                    == 0) { // Redundant check due to the flags used in the GET request of
                            // getAppVulnDeps

                Path p = null;

                // Find the path of the vulndep among the dependencies or use the path returned
                // from the backend by default (which is always present). In case the path from
                // the backend is used, the goal only works when it runs on the same system that
                // run goal APP. This is currently the only supported working mode for the cli
                for (Entry<Path, Dependency> e : deps.entrySet()) {

                    // Here we retrieve the library path based solely on the digest even though the
                    // same library may originate multiple dependencies whose uniqueness is given by
                    // the triple library, parent, relativePath. The idea is that if we have the
                    // same digest we have the same code
                    if (e.getValue()
                            .getLib()
                            .getDigest()
                            .equals(vulndep.getDep().getLib().getDigest())) {
                        p = e.getKey();
                        break;
                    }
                }

                if (p == null) {
                    p = Paths.get(vulndep.getDep().getPath());
                    if (p == null || !p.toFile().exists()) {
                        log.error(
                                "Path ["
                                        + p
                                        + "] for vulnerability ["
                                        + vulndep.getBug().getBugId()
                                        + "] in dependency ["
                                        + vulndep.getDep().getFilename()
                                        + "] does not exist");
                        continue;
                    }
                }

                log.info(
                        "Using path ["
                                + p
                                + "] to analyze vulnerability ["
                                + vulndep.getBug().getBugId()
                                + "] in dependency ["
                                + vulndep.getDep().getFilename()
                                + "]");
                comparator.compareLibForBug(
                        vulndep.getDep().getLib(), vulndep.getBug().getBugId(), p);
            }
        }
    }
}
