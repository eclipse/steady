package com.sap.psr.vulas.mvn;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.sap.psr.vulas.goals.SpaceCleanGoal;


/**
 */
@Mojo( name = "cleanSpace", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresOnline = true )
public class MvnPluginSpaceClean extends AbstractVulasSpaceMojo {
		
	@Override
	protected void createGoal() {
		this.goal = new SpaceCleanGoal();
	}
}
