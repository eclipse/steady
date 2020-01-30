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
package com.sap.psr.vulas.report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Bug;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.VulnerableDependency;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.StringList.CaseSensitivity;
import com.sap.psr.vulas.shared.util.StringList.ComparisonMode;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>Report class.</p>
 *
 */
public class Report {

	private static final Log log = LogFactory.getLog(Report.class);

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyy HH:mm:ss");
	
	/**
	 * Whether or not archives with question marks will be ignored.
	 */
	public static final String IGN_UNASS_ALL = "all";
	/** Constant <code>IGN_UNASS_KNOWN="known"</code> */
	public static final String IGN_UNASS_KNOWN = "known";
	/** Constant <code>IGN_UNASS_OFF="off"</code> */
	public static final String IGN_UNASS_OFF = "off";
	
	/**
	 * If IGN_UNASS_KNOWN, archives that are well-known to Maven Central and which contain change list constructs, 
	 * but which have not been assessed yet will not raise a build exception.
	 */
	private String ignoreUnassessed = IGN_UNASS_KNOWN; 

	/** Constant <code>THRESHOLD_NONE="noException"</code> */
	public static final String THRESHOLD_NONE    = "noException";
	/** Constant <code>THRESHOLD_DEP_ON="dependsOn"</code> */
	public static final String THRESHOLD_DEP_ON  = "dependsOn";
	/** Constant <code>THRESHOLD_POT_EXE="potentiallyExecutes"</code> */
	public static final String THRESHOLD_POT_EXE = "potentiallyExecutes";
	/** Constant <code>THRESHOLD_ACT_EXE="actuallyExecutes"</code> */
	public static final String THRESHOLD_ACT_EXE = "actuallyExecutes";

	private static final String TEMPLATE_FILE_HTML = "velocity_template.html";
	static final String REPORT_FILE_HTML = "vulas-report.html";
	
	private static final String TEMPLATE_FILE_XML = "velocity_template.xml";
	static final String REPORT_FILE_XML = "vulas-report.xml";

	private static final String TEMPLATE_FILE_JSON = "velocity_template.json";
	static final String REPORT_FILE_JSON = "vulas-report.json";
	
	private Map<String,Long> stats = new HashMap<String,Long>();

	private String exceptionThreshold = THRESHOLD_POT_EXE;

	/**
	 * A vulnerability in blacklisted scopes will not cause an exception (multiple scopes to be separated by comma, default: test)
	 */
	private StringList excludedScopes = new StringList();

	/**
	 * Vulnerabilities explicitly mentioned will not cause an exception (multiple bugs to be separated by comma)
	 */
	private StringList excludedBugs = new StringList();
	
	private Application app = null;

	private Set<Application> modules = null;

	/**
	 *  All vulnerabilities of all application modules, collected in {@link Report#fetchAppVulnerabilities()}.
	 */
	private Set<AggregatedVuln> vulns = new TreeSet<AggregatedVuln>();
	
	/**
	 *  Vulnerabilities that cause a build exception, determined in {@link Report#processVulnerabilities()}.
	 */
	private Set<AggregatedVuln> vulnsAboveThreshold = new TreeSet<AggregatedVuln>();
		
	/**
	 *  Vulnerabilities that do not cause a build exception, determined in {@link Report#processVulnerabilities()}.
	 */
	private Set<AggregatedVuln> vulnsBelowThreshold = new TreeSet<AggregatedVuln>();
	
	// The following are used to inform about obsolete exemptions
	private Set<String> historicalVulns = new HashSet<String>();
	private Set<String> relevantVulns = new HashSet<String>();

	final VelocityContext context = new VelocityContext();
	
	private GoalContext goalContext = null;

	/**
	 * <p>Constructor for Report.</p>
	 *
	 * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
	 * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
	 * @param _modules a {@link java.util.Set} object.
	 */
	public Report(GoalContext _ctx, Application _app, Set<Application> _modules) {
		this.goalContext = _ctx;
		this.app = _app;

		if(_modules==null) {
			this.modules = new HashSet<Application>();
			this.modules.add(this.app);
		}
		else
			this.modules = _modules;
				
		Report.log.info("Report to be done for " + this.app + ", [" + this.modules.size() + "] modules in total: " + this.modules);
	}

	/**
	 * <p>Getter for the field <code>exceptionThreshold</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getExceptionThreshold() { return exceptionThreshold; }
	/**
	 * <p>Setter for the field <code>exceptionThreshold</code>.</p>
	 *
	 * @param _threshold a {@link java.lang.String} object.
	 */
	public void setExceptionThreshold(String _threshold) {
		if(_threshold!=null)
			this.exceptionThreshold = _threshold;
		Report.log.info("Exception threshold: " + this.exceptionThreshold);
	}

	/**
	 * <p>addExcludedBugs.</p>
	 *
	 * @param _items a {@link java.lang.String} object.
	 */
	public void addExcludedBugs(String _items) {
		if(_items!=null && !_items.equals("")) {
			this.excludedBugs.addAll(_items, ",", true);
			Report.log.warn("Excluded bugs: " + this.excludedBugs);
		}
	}
	
	/**
	 * <p>addExcludedBugs.</p>
	 *
	 * @param _items an array of {@link java.lang.String} objects.
	 */
	public void addExcludedBugs(String[] _items) {
		if(_items!=null) {
			this.excludedBugs.addAll(_items, true);
			Report.log.warn("Excluded bugs: " + this.excludedBugs);
		}
	}

	/**
	 * <p>addExcludedScopes.</p>
	 *
	 * @param _items a {@link java.lang.String} object.
	 */
	public void addExcludedScopes(String _items) {
		if(_items!=null && !_items.equals("")) {
			this.excludedScopes.addAll(_items, ",", true);
			Report.log.warn("Excluded scopes: " + this.excludedScopes);
		}
	}
	
	/**
	 * <p>addExcludedScopes.</p>
	 *
	 * @param _items an array of {@link java.lang.String} objects.
	 */
	public void addExcludedScopes(String[] _items) {
		if(_items!=null) {
			this.excludedScopes.addAll(_items, true);
			Report.log.warn("Excluded scopes: " + this.excludedScopes);
		}
	}
	
	/**
	 * <p>Setter for the field <code>ignoreUnassessed</code>.</p>
	 *
	 * @param _ignore a {@link java.lang.String} object.
	 */
	public void setIgnoreUnassessed(String _ignore) {
		if(_ignore!=null) {
			if(_ignore.equalsIgnoreCase(IGN_UNASS_ALL)) {
				this.ignoreUnassessed = IGN_UNASS_ALL;
				Report.log.warn("All unassessed vulnerabilities will be ignored");
			}
			else if(_ignore.equalsIgnoreCase(IGN_UNASS_OFF)) {
				this.ignoreUnassessed = IGN_UNASS_OFF;
			}
			else {
				this.ignoreUnassessed = IGN_UNASS_KNOWN;
				Report.log.warn("All unassessed vulnerabilities in archives with known digests will be ignored");
			}
		}
	}
	
	private boolean ignoreUnassessed(VulnerableDependency _a) {
		if(this.ignoreUnassessed.equalsIgnoreCase(IGN_UNASS_OFF))
			return false;
		else if(this.ignoreUnassessed.equalsIgnoreCase(IGN_UNASS_ALL))
			return !_a.isAffectedVersionConfirmed();
		else
			return !_a.isAffectedVersionConfirmed() && _a.getDep().getLib().isWellknownDigest();
	}
	
	private boolean isAmongAggregatedModules(LibraryId _libid) {
		for(Application prj: this.modules) {
			if(prj.getMvnGroup().equals(_libid.getMvnGroup()) &&
				prj.getArtifact().equals(_libid.getArtifact()) &&
				prj.getVersion().equals(_libid.getVersion()))
				return true;
		}
		return false;
	}
	
	/**
	 * Downloads vulnerable dependencies for all {@link Application}s in member variable {@link #modules}.
	 * 
	 * The member variable {@link #modules} only contains multiple {@link Application}s if the report is created through the Maven plugin and
	 * the respective Maven project (for which the report goal is executed) is a multi-module aggregator project. If both conditions are met,
	 * each project module corresponds to one {@link Application} in member variable {@link #modules}.
	 *
	 * @throws java.io.IOException
	 * @throws com.sap.psr.vulas.backend.BackendConnectionException
	 */
	public void fetchAppVulnerabilities() throws IOException, BackendConnectionException {
		for(Application prj: this.modules) {
			try {
				// Fetch and collect historical vulns
				final Set<VulnerableDependency> historical_vuln_deps = BackendConnector.getInstance().getAppVulnDeps(this.goalContext, prj, true, false, true);
				for(VulnerableDependency v: historical_vuln_deps) {
					this.historicalVulns.add(v.getBug().getBugId());
				}
			
				// Fetch and prepare relevant vulns
				final Set<VulnerableDependency> vuln_deps = BackendConnector.getInstance().getAppVulnDeps(this.goalContext, prj, false, true, true);
				for(VulnerableDependency v: vuln_deps) {
					this.relevantVulns.add(v.getBug().getBugId());					
					
					v.setApp(prj);
					final AggregatedVuln new_av   = new AggregatedVuln(v.getDep().getLib().getDigest(), v.getDep().getFilename(), v.getBug());
					final AggregatedVuln added_av = this.update(this.vulns, new_av);
					
					//HP(19.12.2017): Only add if the vulnerability is not in on the of the other modules (which happens if you start scans for OSS projects)
					if(v.getDep().getLib().getLibraryId()!=null && this.isAmongAggregatedModules(v.getDep().getLib().getLibraryId()))
						log.warn("Skipping [" + v.getBug().getBugId() + "] for dependency of " + prj + " on " + v.getDep().getLib().getLibraryId() + ", the latter is one of the aggregated modules");
					else
						added_av.addAnalysis(v);
				}
			} catch (BackendConnectionException e) {
				// Throw exception in order to abort report creation
				final BackendConnectionException bce = new BackendConnectionException("Error fetching vulnerable dependencies for application (module) " + prj + ": " + e.getMessage(), e);
				Report.log.error(bce);
				throw bce;
			}
		}  
	}

	private AggregatedVuln update(Set<AggregatedVuln> _set, AggregatedVuln _av) {
		for(AggregatedVuln av: _set)
			if(av.equals(_av))
				return av;
		_set.add(_av);
		return _av;
	}

	/**
	 * Processes all the vulnerabilities of all application modules as obtained from the backend.
	 * Populates the Velocity {@link #context} required for rendering the templates in {@link #writeResult(Path)}. 
	 */
	public void processVulnerabilities() {
		// Will be shown but do not raise a build exception
		final Set<AggregatedVuln> vulnsToReport = new TreeSet<AggregatedVuln>();

		// Stats to be added to the goal execution
		long vulns_incl = 0, vulns_reach = 0, vulns_traced = 0;
		long vulns_traced_not_reach = 0; // A particularly interesting case: Static analysis says it is not reachable, however, we collected a trace
		
		// Collect obsolete exemptions
		final Set<String> obsolHistorical = new HashSet<String>();
		final Set<String> obsolSignNotPresent = new HashSet<String>();
		for(String v: this.excludedBugs) {
			if(this.historicalVulns.contains(v) && !relevantVulns.contains(v)) {
				obsolHistorical.add(v);
			}
			else if(!this.historicalVulns.contains(v) && !relevantVulns.contains(v)) {
				obsolSignNotPresent.add(v);
			}
		}
		if(!obsolHistorical.isEmpty())
			log.warn("Exemptions for the following vulnerabilities are obsolete, because they concern previous version(s) of the respective application dependency(ies) (historical vulnerability): [" + StringUtil.join(obsolHistorical, ", ") + "]");
		if(!obsolSignNotPresent.isEmpty())
			log.warn("Exemptions for the following vulnerabilities are obsolete, because none of the application dependencies contain potentially affected code signatures: [" + StringUtil.join(obsolSignNotPresent, ", ") + "]");

		// Process all vulnerable dependencies obtained from the backend
		for(AggregatedVuln v: this.vulns) {
			for(VulnerableDependency analysis: v.getAnalyses()) {
				
				// Is the vulnerability exempted?
				analysis.setBlacklisted(this.isIgnoredForBuildException(analysis, v.getBug().getBugId()));				

				// Only report if there is a confirmed problem or a manual check/activity is required (orange hourglass)
				// In other words: ignore historical vulnerabilities, i.e., cases where a non-vulnerable archive is used
				if(!analysis.isNoneAffectedVersion()) {
					vulnsToReport.add(v);
					vulns_incl++;
				}
				
				// Counters
				if(!analysis.isNoneAffectedVersion() && ( analysis.isReachable() || !analysis.isReachableConfirmed() )) { vulns_reach++; }
				if(!analysis.isNoneAffectedVersion() && ( analysis.isTraced() || !analysis.isTracedConfirmed() )) { vulns_traced++; }
				
				// Interesting case: Traced but not reachable
				if(analysis.isTraced() && analysis.isReachable() && analysis.isReachableConfirmed()) { vulns_traced_not_reach++; }
				
				// Is analysis above the configured exception threshold?
				if( (exceptionThreshold.equalsIgnoreCase(THRESHOLD_DEP_ON)  && ( analysis.isAffectedVersion() || !analysis.isAffectedVersionConfirmed() ) ) ||
						(exceptionThreshold.equalsIgnoreCase(THRESHOLD_POT_EXE) && ( !analysis.isNoneAffectedVersion() && ( analysis.isReachable() || !analysis.isReachableConfirmed() ) ) ) ||
						(exceptionThreshold.equalsIgnoreCase(THRESHOLD_ACT_EXE) && ( !analysis.isNoneAffectedVersion() && ( analysis.isTraced() || !analysis.isTracedConfirmed() ) ) ) ) {
					analysis.setAboveThreshold(true);
				} else {
					analysis.setAboveThreshold(false);
				}

				// Will this vuln result in a build exception (depending on blacklist and analysis threshold)?
				if(analysis.isThrowsException()) { v.aboveThreshold = true; }
			}
		}
		
		// Split vulnerabilities to be reported into 2 sets
		for(AggregatedVuln v: vulnsToReport) {
			if(v.aboveThreshold)
				vulnsAboveThreshold.add(v);
			else
				vulnsBelowThreshold.add(v);
		}

		// Write stats to map
		this.stats.put("report.vulnsIncluded", vulns_incl); // Stats for non-blacklisted ones
		this.stats.put("report.vulnsReachable", vulns_reach); // 
		this.stats.put("report.vulnsTraced", vulns_traced); //
		this.stats.put("report.vulnsTracedNotReachable", vulns_traced_not_reach); //

		this.stats.put("report.buildFailure", Long.valueOf(this.isThrowBuildException()?1:0) );
		
		this.stats.put("report.vulnsAboveThreshold", Long.valueOf(vulnsAboveThreshold.size()));
		this.stats.put("report.vulnsBelowThreshold", Long.valueOf(vulnsBelowThreshold.size()));
		
		this.stats.put("report.isAggregated", Long.valueOf((this.isAggregated() ? 1 : 0)));
		this.stats.put("report.projectsReportedOn", Long.valueOf(modules.size()));

		// Analysis results
		this.context.put("vulnsToReport", vulnsToReport);
		this.context.put("vulnsAboveThreshold", vulnsAboveThreshold);
		this.context.put("vulnsBelowThreshold", vulnsBelowThreshold);
		
		this.context.put("obsoleteExemptionsHistorical", StringUtil.join(obsolHistorical, ", "));
		this.context.put("obsoleteExemptionsSignatureNotPresent", StringUtil.join(obsolSignNotPresent, ", "));

		// Basic info
		this.context.put("vulas-backend-serviceUrl", this.goalContext.getVulasConfiguration().getServiceUrl(Service.BACKEND));
		this.context.put("app", app);
		this.context.put("space", this.goalContext.getSpace());
		this.context.put("projects", modules);
		this.context.put("generatedAt", dateFormat.format(new Date()));
		this.context.put("vulas-shared-homepage", this.goalContext.getVulasConfiguration().getConfiguration().getString(VulasConfiguration.HOMEPAGE, "undefined"));
		
		// Configuration
		this.context.put("exceptionThreshold", this.exceptionThreshold);
		this.context.put("exceptionScopeBlacklist", this.excludedScopes.toString(", "));
		this.context.put("exceptionExcludedBugs", this.excludedBugs.toString(", "));
		this.context.put("isAggregated", Boolean.valueOf(this.isAggregated()));
		this.context.put("thresholdMet", vulnsAboveThreshold.isEmpty());
	}

	/**
	 * Returns true if there are any application modules, false otherwise.
	 * @return
	 */
	private boolean isAggregated() {
		return this.modules!=null && this.modules.size()>1;
	}

	/**
	 * Returns true if a build exception shall be thrown, which is the case if a threshold other than NONE is defined and vulnerabilities exist above this threshold.
	 *
	 * @return a boolean.
	 */
	public boolean isThrowBuildException() {
		return !this.exceptionThreshold.equalsIgnoreCase("none") && !this.vulnsAboveThreshold.isEmpty();
	}

	/**
	 * Returns a human-readable description of the configuration.
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,String> getConfiguration() {
		final Map<String,String> cfg = new HashMap<String,String>();
		cfg.put("report.exceptionThreshold", this.exceptionThreshold);
		cfg.put("report.exceptionScopeBlacklist", this.excludedScopes.toString(", "));
		cfg.put("report.exceptionExcludedBugs", this.excludedBugs.toString(", "));
		cfg.put("report.aggregated", Boolean.toString(this.isAggregated()));
		return cfg;
	}

	/**
	 * <p>Getter for the field <code>stats</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,Long> getStats() { return this.stats; }
	
	/**
	 * <p>getExceptionMessage.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getExceptionMessage() {
		final StringBuilder builder = new StringBuilder();

		// Explanatory text
		if(exceptionThreshold.equalsIgnoreCase(THRESHOLD_DEP_ON))
			builder.append("Application depends on archives with vulnerable code");
		else if(exceptionThreshold.equalsIgnoreCase(THRESHOLD_POT_EXE))
			builder.append("Application potentially executes vulnerable code");
		else if(exceptionThreshold.equalsIgnoreCase(THRESHOLD_ACT_EXE))
			builder.append("Application actually executes vulnerable code");
		
		return builder.toString();
	}

	/**
	 * <p>getResultAsString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getResultAsString() {
		final StringBuilder builder = new StringBuilder();

		// Explanatory text
		if(exceptionThreshold.equalsIgnoreCase(THRESHOLD_DEP_ON))
			builder.append("The application depends on the following vulnerable archives: ");
		else if(exceptionThreshold.equalsIgnoreCase(THRESHOLD_POT_EXE))
			builder.append("The application potentially executes vulnerable code of the following vulnerable archives (or reachability was not checked): ");
		else if(exceptionThreshold.equalsIgnoreCase(THRESHOLD_ACT_EXE))
			builder.append("The application actually executes vulnerable code of the following vulnerable archives (or no tests were run): ");

		// Will it result in a build exception?
		int i = 0;
		for(AggregatedVuln v: this.vulnsAboveThreshold) {
			for(VulnerableDependency analysis: v.getAnalyses()) {
				if(analysis.isThrowsException()) {
					builder.append(System.getProperty("line.separator")).append("      ").append(++i).append(": ");
					builder.append("[filename=").append(v.filename);
					builder.append(", scope=").append(analysis.getDep().getScope());
					builder.append(", transitive=").append(analysis.getDep().getTransitive());
					builder.append(", wellknownSha1=").append(analysis.getDep().getLib().isWellknownDigest());
					builder.append(", isAffectedVersionConfirmed=").append(analysis.isAffectedVersionConfirmed());
					builder.append(", bug=").append(v.bug.getBugId()).append("]");
				}
			}
		}

		return builder.toString();
	}
	
	/**
	 * Creates result reports in HTML, XML and JSON.
	 *
	 * @param _dir a {@link java.nio.file.Path} object.
	 */
	public void writeResult(@NotNull Path _dir) {
		this.writeResultAsHtml(_dir);
		this.writeResultAsXml(_dir);
		this.writeResultAsJson(_dir);
	}
	
	/**
	 * <p>writeResult.</p>
	 *
	 * @param _dir a {@link java.nio.file.Path} object.
	 * @param _template a {@link java.lang.String} object.
	 * @param _report a {@link java.lang.String} object.
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path writeResult(@NotNull Path _dir, String _template, String _report) {
		Template template = null;

		final VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();

		PrintWriter pw = null;
		File file = null;
		try {
			// Get the template
			final InputStream input = this.getClass().getClassLoader().getResourceAsStream(_template);
			if (input == null) throw new IOException("Template file doesn't exist");
			template = ve.getTemplate(_template);

			// Create dir if required
			if(!FileUtil.isAccessibleDirectory(_dir))
				Files.createDirectories(_dir);
			
			// Write report
			file = Paths.get(_dir.toString(), _report).toFile();
			pw = new PrintWriter(file, FileUtil.getCharsetName());
			template.merge(context, pw);

			Report.log.info("Report with analysis results has been written to [" + file.toPath().toAbsolutePath().normalize() + "]");
		} catch (Exception e) {
			Report.log.error("Exception while creating report [" + file + "] with template [" + _template + "]: " + e.getMessage());
		}
		finally {
			if(pw!=null) {
				pw.flush();
				pw.close();	
			}
		}

		return (file==null ? null : file.toPath().toAbsolutePath());
	}

	/**
	 * <p>writeResultAsHtml.</p>
	 *
	 * @param _dir a {@link java.nio.file.Path} object.
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path writeResultAsHtml(@NotNull Path _dir) {
		return this.writeResult(_dir, TEMPLATE_FILE_HTML, REPORT_FILE_HTML);
	}
	
	/**
	 * <p>writeResultAsXml.</p>
	 *
	 * @param _dir a {@link java.nio.file.Path} object.
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path writeResultAsXml(@NotNull Path _dir) {
		return this.writeResult(_dir, TEMPLATE_FILE_XML, REPORT_FILE_XML);
	}
	
	/**
	 * <p>writeResultAsJson.</p>
	 *
	 * @param _dir a {@link java.nio.file.Path} object.
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path writeResultAsJson(@NotNull Path _dir) {
		return this.writeResult(_dir, TEMPLATE_FILE_JSON, REPORT_FILE_JSON);
	}

	/**
	 * Returns true if the given analysis will not lead to a build exception according to the
	 * configured scope blacklists and excluded bugs.
	 * @param _a
	 * @param _excl_scopes
	 * @param _excl_bugs
	 * @return
	 */
	private boolean isIgnoredForBuildException(VulnerableDependency _a, String _bugid) {
		return (this.excludedScopes!=null && _a.getDep().getScope()!=null && this.excludedScopes.contains(_a.getDep().getScope().toString(), ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE)) ||
				(this.excludedBugs!=null && this.excludedBugs.contains(_bugid, ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE))  ||
				(this.ignoreUnassessed(_a));
	}

	public static class AggregatedVuln implements Comparable {

		public String archiveid; // Digest
		public String getArchiveid() { return archiveid; }

		public String filename;
		public String getFilename() { return filename; }
				
		public Bug bug = null;
		public Bug getBug() { return this.bug; }

		public Set<VulnerableDependency> analyses = new HashSet<VulnerableDependency>();
		public void addAnalysis(VulnerableDependency _dep) {
			if(this.analyses.contains(_dep)) {
				return;
			} else {
				this.analyses.add(_dep);
			}
		}
		public Set<VulnerableDependency> getAnalyses() { return analyses; }

		public AggregatedVuln(String _digest, String _filename, Bug _bug) { this.archiveid = _digest; this.filename = _filename; this.bug = _bug;}

		public boolean aboveThreshold = false;
		public boolean hasFindingsAboveThreshold() { return aboveThreshold; }
		
		public String toString() {
			return "[" + this.filename + ", " + this.bug.getBugId() + ", #analyses=" + this.analyses.size() + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (aboveThreshold ? 1231 : 1237);
			result = prime * result + ((archiveid == null) ? 0 : archiveid.hashCode());
			result = prime * result + ((bug.getBugId() == null) ? 0 : bug.getBugId().hashCode());
			return result;
		}

		/**
		 * Returns true if both digest and bug are equals, false otherwise.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			AggregatedVuln other = (AggregatedVuln) obj;
			if (archiveid == null) {
				if (other.archiveid != null)
					return false;
			} else if (!archiveid.equals(other.archiveid))
				return false;
			if (bug == null) {
				if (other.bug != null)
					return false;
			} else if (!bug.equals(other.bug))
				return false;
			return true;
		}

		@Override
		public int compareTo(Object _o) {
			AggregatedVuln other = null;
			if(_o instanceof AggregatedVuln)
				other = (AggregatedVuln)_o;
			else
				throw new IllegalArgumentException();

			final int filename_comparison = this.filename==null || other.filename==null ? 0 : this.filename.compareTo(other.filename);
			final int bugid_comparison = this.bug.compareTo(other.getBug());

			if(filename_comparison!=0)
				return filename_comparison;
			else
				return bugid_comparison;
		}
	}
}
