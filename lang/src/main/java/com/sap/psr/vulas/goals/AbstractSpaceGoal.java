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
package com.sap.psr.vulas.goals;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Space;

/**
 * Represents a goal executed in the context of a given {@link Space}.
 */
public abstract class AbstractSpaceGoal extends AbstractGoal {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    /**
     * <p>Constructor for AbstractSpaceGoal.</p>
     *
     * @param _type a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
     */
    protected AbstractSpaceGoal(GoalType _type) {
        super(_type);
    }

    /**
     * All {@link Space}-related goals need to have a tenant configured.
     *
     * @throws com.sap.psr.vulas.goals.GoalConfigurationException if no tenant is specified
     */
    protected void checkPreconditions() throws GoalConfigurationException {
        if (!this.getGoalContext().hasTenant())
            throw new GoalConfigurationException("No tenant configured");
    }

    /**
     * <p>updateFromConfig.</p>
     *
     * @param _s a {@link com.sap.psr.vulas.shared.json.model.Space} object.
     */
    protected void updateFromConfig(Space _s) {
        final Configuration c = this.getConfiguration().getConfiguration();
        _s.setSpaceName(c.getString(CoreConfiguration.SPACE_NAME, null));
        _s.setSpaceDescription(c.getString(CoreConfiguration.SPACE_DESCR, null));
        _s.setExportConfiguration(
                ExportConfiguration.parse(
                        c.getString(CoreConfiguration.SPACE_EXPCFG, "AGGREGATED")));
        _s.setPublic(c.getBoolean(CoreConfiguration.SPACE_PUBLIC, true));
        _s.setBugFilter(c.getInt(CoreConfiguration.SPACE_BUGFLT, -1));
        _s.setOwnerEmails(
                new HashSet<String>(
                        Arrays.asList(c.getStringArray(CoreConfiguration.SPACE_OWNERS))));
    }
}
