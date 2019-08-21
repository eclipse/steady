package com.sap.psr.vulas.mvn;

import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.sap.psr.vulas.cg.T2CGoal;
import com.sap.psr.vulas.goals.AbstractAppGoal;

/**
 * The plugin can be executed for Eclipse projects through 'Run As' &gt; 'Maven build...' &gt; Goal 'vulas:t2c'.
 */
@Mojo( name = "t2c", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresOnline = true )
public class MvnPluginT2C extends AbstractVulasMojo {

	/** {@inheritDoc} */
	@Override
	protected void createGoal() {
		this.goal = new T2CGoal();
	}
}
