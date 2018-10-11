package com.sap.psr.vulas.mvn;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.sap.psr.vulas.goals.InstrGoal;


/**
 * This plugin analyzes all Java archives in a given Maven project in order to identify all their Java constructs.
 * Those are then uploaded to a remote service for further analysis (test coverage, vulnerability assessments, archive integrity).
 * The plugin can be executed for Eclipse projects through 'Run As' > 'Maven build...' > Goal 'vulas:instr'.
 * 
 * help:describe -Dplugin=com.sap.research.security.vulas:vulas-maven-plugin
 */
@Mojo( name = "instr", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresOnline = true )
public class MvnPluginInstr extends AbstractVulasMojo {

	@Override
	protected void createGoal() {
		this.goal = new InstrGoal();
	}
}
