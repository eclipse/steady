package com.sap.psr.vulas.mvn;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.sap.psr.vulas.goals.CleanGoal;


/**
 * 
 */
@Mojo( name = "clean", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresOnline = true )
public class MvnPluginClean extends AbstractVulasMojo {
		
	@Override
	protected void createGoal() {
		this.goal = new CleanGoal();
	}
}
