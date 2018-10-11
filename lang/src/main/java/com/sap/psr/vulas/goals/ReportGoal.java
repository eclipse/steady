package com.sap.psr.vulas.goals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.report.Report;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.FileUtil;

public class ReportGoal extends AbstractAppGoal {
	
	private Set<Application> modules = null;

	public ReportGoal() { super(GoalType.REPORT); }
			
	public void setApplicationModules(Set<Application> _modules) {
		this.modules = _modules;
	}

	public void setReportDir(Path _path) throws IllegalArgumentException {
		if(!FileUtil.isAccessibleDirectory(_path))
			throw new IllegalArgumentException("Cannot write report to [" + _path + "]");
	}

	@Override
	protected void executeTasks() throws Exception {
		final Configuration cfg = this.getConfiguration().getConfiguration();
		
		final Report report = new Report(this.getGoalContext(), this.getApplicationContext(), this.modules);

		// Set all kinds of exceptions
		report.setExceptionThreshold(cfg.getString(CoreConfiguration.REP_EXC_THRESHOLD, Report.THRESHOLD_ACT_EXE));

		// Excluded bugs
		report.addExcludedBugs(cfg.getStringArray(CoreConfiguration.REP_EXCL_BUGS));

		// Excluded scopes
		report.addExcludedScopes(cfg.getStringArray(CoreConfiguration.REP_EXC_SCOPE_BL));
		
		// Exclude non-assessed vuln deps
		report.setIgnoreUnassessed(cfg.getString(CoreConfiguration.REP_EXCL_UNASS, Report.IGN_UNASS_KNOWN));
		
		// Fetch the vulns
		try {
			report.fetchAppVulnerabilities();
		} catch (IOException e) {
			throw new GoalExecutionException("Error fetching vulnerabilities from the backend: " + e.getMessage(), e);
		}

		// Loop over vulnerabilities
		report.processVulnerabilities();

		report.writeResult(this.getConfiguration().getDir(CoreConfiguration.REP_DIR));

		// Stats
		this.addGoalStats("report", report.getStats());

		// Throw exception if threshold is not met (or none is defined)
		if(report.isThrowBuildException()) {
			final ReportException re = new ReportException(report.getExceptionMessage(), null);
			re.setLongMessage(report.getResultAsString());
			throw re;
		}
	}
}