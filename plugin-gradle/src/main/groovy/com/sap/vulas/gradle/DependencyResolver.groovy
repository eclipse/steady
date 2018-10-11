package com.sap.vulas.gradle

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
