package com.sap.psr.vulas.mvn;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.ReportException;
import com.sap.psr.vulas.goals.ReportGoal;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

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
	 * Builds the set of {@link Application}s to be considered in the result report.
	 * 
	 * If the given {@link MavenProject} has the packaging type 'POM', applications
	 * corresponding to all its sub-modules will be added to this set.
	 * 
	 * Depending on the configuration option {@link CoreConfiguration#REP_OVERRIDE_VER},
	 * the application version is either taken from the POM or from configuration
	 * setting {@link CoreConfiguration#APP_CTX_VERSI}.
	 * 
	 * @param _prj
	 * @param _ids
	 */
	private void collectApplicationModules(MavenProject _prj, Set<Application> _ids) {

		// The version as specified in the POM of the given project		
		final String pom_version = _prj.getVersion();
		
		// The version specified with configuration option {@link CoreConfiguration#APP_CTX_VERSI}
		final String app_ctx_version = this.goal.getGoalContext().getApplication().getVersion();
		
		// The application module to be added
		final Application app = new Application(_prj.getGroupId(), _prj.getArtifactId(), pom_version);
		
		// Override version found in the respective pom.xml with the version of the application context
		// This becomes necessary if module scan results are NOT uploaded with the version found in the POM,
		// but with one specified in other ways, e.g., per -Dvulas.core.appContext.version
		if(this.vulasConfiguration.getConfiguration().getBoolean(CoreConfiguration.REP_OVERRIDE_VER, false) && !pom_version.equals(app_ctx_version)) {
			app.setVersion(app_ctx_version);
			this.getLog().warn("Report will include application version " + app + " rather than version [" + pom_version + "] specified in its POM");
		}
		
		_ids.add(app);
		if(_prj.getPackaging().equalsIgnoreCase("pom")) {
			for(MavenProject module: _prj.getCollectedProjects()) {
				this.collectApplicationModules(module, _ids);
			}
		}
	}
}
