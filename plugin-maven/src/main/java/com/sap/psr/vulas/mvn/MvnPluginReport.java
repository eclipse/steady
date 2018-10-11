package com.sap.psr.vulas.mvn;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import com.sap.psr.vulas.goals.ReportException;
import com.sap.psr.vulas.goals.ReportGoal;
import com.sap.psr.vulas.shared.json.model.Application;

@Mojo( name = "report", defaultPhase = LifecyclePhase.VERIFY, requiresOnline = true )
public class MvnPluginReport extends AbstractVulasMojo {

	@Override
	protected void createGoal() {
		this.goal = new ReportGoal();
	}

	@Override
	protected void executeGoal() throws Exception {
		// Collect all modules to be reported on
		final Set<Application> modules = new HashSet<Application>(); 
		this.collectApplicationModules(this.project, modules);
		((ReportGoal)this.goal).setApplicationModules(modules);

		try {
			this.goal.executeSync();
		}
		// ReportException will be passed on as MojoFailure, i.e., the goal execution terminates normally
		catch (ReportException re) {
			throw new MojoFailureException(re.getLongMessage(), re);
		}
	}

	/**
	 * Recursively loops over the subprojects of the given project in order to build
	 * a complete set of {@link Application} modules.
	 * @param _prj
	 * @param _ids
	 */
	private void collectApplicationModules(MavenProject _prj, Set<Application> _ids) {
		_ids.add(new Application(_prj.getGroupId(), _prj.getArtifactId(), _prj.getVersion()));
		if(_prj.getPackaging().equalsIgnoreCase("pom")) {
			for(MavenProject module: _prj.getCollectedProjects()) {
				this.collectApplicationModules(module, _ids);
			}
		}
	}
}
