package com.sap.psr.vulas.mvn;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.sap.psr.vulas.goals.BomGoal;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.LibraryId;


/**
 * This Mojo identifies the constructs belonging to the application itself and belonging to all its dependencies.
 * Those are then uploaded to the central Vulas engine for further analysis (test coverage, vulnerability assessments, archive integrity).
 */
@Mojo( name = "app", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST, requiresOnline = true )
public class MvnPluginBom extends AbstractVulasMojo {
		
	/** {@inheritDoc} */
	@Override
	protected void createGoal() {
		this.goal = new BomGoal();
	}
}
