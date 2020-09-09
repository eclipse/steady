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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.goals;

import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.report.Report;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.ExemptionSet;
import org.eclipse.steady.shared.util.FileUtil;

/**
 * <p>ReportGoal class.</p>
 *
 */
public class ReportGoal extends AbstractAppGoal {

  private Set<Application> modules = null;

  /**
   * <p>Constructor for ReportGoal.</p>
   */
  public ReportGoal() {
    super(GoalType.REPORT);
  }

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
    if (!FileUtil.isAccessibleDirectory(_path))
      throw new IllegalArgumentException("Cannot write report to [" + _path + "]");
  }

  /** {@inheritDoc} */
  @Override
  protected void executeTasks() throws Exception {
    final Configuration cfg = this.getConfiguration().getConfiguration();

    final Report report =
        new Report(this.getGoalContext(), this.getApplicationContext(), this.modules);
    report.setExceptionThreshold(
        cfg.getString(CoreConfiguration.REP_EXC_THRESHOLD, Report.THRESHOLD_ACT_EXE));
    report.setExemptions(ExemptionSet.createFromConfiguration(cfg));
    report.setCreateAffectedLibraries(cfg.getBoolean(CoreConfiguration.REP_CREATE_AFF_LIB, false));

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
    if (report.isThrowBuildException()) {
      final ReportException re = new ReportException(report.getExceptionMessage(), null);
      re.setLongMessage(report.getResultAsString());
      throw re;
    }
  }
}
