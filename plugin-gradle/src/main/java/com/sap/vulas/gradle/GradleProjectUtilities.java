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
package com.sap.vulas.gradle;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.sap.vulas.gradle.VulasPluginCommon.*;


public class GradleProjectUtilities {


    private static final Map<String, ProjectOutputTypes> knownPlugins;

    static {
        knownPlugins = new HashMap<>();
        knownPlugins.put("java", ProjectOutputTypes.JAR);
        knownPlugins.put("com.android.library", ProjectOutputTypes.AAR);
        knownPlugins.put("com.android.application", ProjectOutputTypes.APK);
    }


    protected static String getMandatoryProjectProperty(Project project, GradleGavProperty property, Logger logger) {

        final String propertyName=property.name();

        logger.debug("Looking for property [{}] in project", propertyName);

        String propertyValue = null;

        if(project.hasProperty(propertyName)) {
            propertyValue = project.getProperties().get(propertyName).toString();
        }

        if (propertyValue == null || propertyValue.isEmpty() || propertyValue.equals("undefined")) {
            logger.error("Property [{}] is not defined, please define it!", propertyName);
            throw new GradleException();
        }

        logger.debug("Property found: {}={}", propertyName, propertyValue);

        return propertyValue;
    }

    protected static ProjectOutputTypes determineProjectOutputType(Project project, Logger logger) {

        ProjectOutputTypes projectOutputType = null;

        for ( String kp: knownPlugins.keySet()) {
            if (project.getPlugins().hasPlugin(kp)) {
                logger.quiet("Found plugin: " + kp);
                projectOutputType = knownPlugins.get(kp);
                break;
            }
        }

        if (projectOutputType != null) {
            logger.quiet("Project type determined: {}", projectOutputType.toString());
        }

        return projectOutputType;
    }

    protected static boolean hasKnownProjectOutputType(Project project, Logger logger) {
        return determineProjectOutputType(project, logger) != null;
    }

}
