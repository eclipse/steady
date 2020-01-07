/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.goals;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.report.Report;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Exemption;
import com.sap.psr.vulas.shared.json.model.ExemptionUnassessed;
import com.sap.psr.vulas.shared.json.model.IExemption;
import com.sap.psr.vulas.shared.util.FileUtil;

/**
 * <p>ReportGoal class.</p>
 *
 */
public class ReportGoal extends AbstractAppGoal {

	private Set<Application> modules = null;

	/**
	 * <p>Constructor for ReportGoal.</p>
	 */
	public ReportGoal() { super(GoalType.REPORT); }

	/**
	 * <p>setApplicationModules.</p>
	 *
	 * @param _modules a {@link java.util.Set} object.
	 */
	public void setApplicationModules(Set<Application> _modules) {
		this.modules = _modules;
	}

	/**
	 * <p>setReportDir.</p>
	 *
	 * @param _path a {@link java.nio.file.Path} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public void setReportDir(Path _path) throws IllegalArgumentException {
		if(!FileUtil.isAccessibleDirectory(_path))
			throw new IllegalArgumentException("Cannot write report to [" + _path + "]");
	}

	/** {@inheritDoc} */
	@Override
	protected void executeTasks() throws Exception {
		final Configuration cfg = this.getConfiguration().getConfiguration();

		final Report report = new Report(this.getGoalContext(), this.getApplicationContext(), this.modules);

		// Exception threshold
		report.setExceptionThreshold(cfg.getString(CoreConfiguration.REP_EXC_THRESHOLD, Report.THRESHOLD_ACT_EXE));

		// Exemptions
		final Set<IExemption> exempts = new HashSet<IExemption>();
		IExemption exempt = ExemptionUnassessed.readFromConfiguration(cfg);
		if(exempt!=null)
			exempts.add(exempt);
		exempts.addAll(Exemption.readFromConfiguration(cfg));
		report.setExemptions(exempts);

		// Fetch the vulns
		try {
			report.fetchAppVulnerabilities();
		} catch (Exception e) {
			throw new GoalExecutionException("Error fetching vulnerabilities: " + e.getMessage(), e);
		}

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
