package com.sap.psr.vulas.mvn;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.sap.psr.vulas.cg.A2CGoal;

/**
 * This Mojo performs a static source analysis to see whether vulnerable OSS coding is reachable from application constructs.
 */
@Mojo( name = "a2c", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresOnline = true )
public class MvnPluginA2C extends AbstractVulasMojo {

	/** {@inheritDoc} */
	@Override
	protected void createGoal() {
		this.goal = new A2CGoal();
	}
}
