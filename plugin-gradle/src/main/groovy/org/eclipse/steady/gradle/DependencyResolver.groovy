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
package org.eclipse.steady.java.gradle

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.attributes.Attribute
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier

class DependencyResolver {

    static Set<ResolvedArtifactResult> resolveDirectOnly(Configuration configuration) {

        Configuration nonTransitiveConfiguration = configuration.copyRecursive()
        nonTransitiveConfiguration.setTransitive(false)
        return resolve(nonTransitiveConfiguration)
    }

    static Set<ResolvedArtifactResult> resolve(Configuration configuration) {
        def artifactType = Attribute.of('artifactType', String)
        def types = ['jar', 'aar']
        def result = new HashSet<ResolvedArtifactResult>()

        for (type in types) {
            def artifacts = configuration.incoming.artifactView { aw ->
                aw.attributes { att ->
                    att.attribute(artifactType, type)
                }
            }.artifacts.findAll { it.identifier instanceof ModuleComponentArtifactIdentifier }
            result.addAll(artifacts)
        }
        return result
    }
}
