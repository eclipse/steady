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
package com.sap.psr.vulas.monitor;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Logger;


import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.java.JarAnalyzer;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.java.JavaPackageId;
import com.sap.psr.vulas.java.WarAnalyzer;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Controls whether or not a given Java class is instrumented.
 * To that end, it uses a couple of blacklists and whitelists read from the configuration.
 * Moreover, it maintains statistics to report on the overall number of classes and constructs instrumented.
 *
 * @see DynamicTransformer
 * @see JarAnalyzer
 * @see WarAnalyzer
 */
public class InstrumentationControl {

	// ====================================== STATIC MEMBERS

	public static enum InstrumentationMetrics { classesTotal, classesInstrumentedSuccess, classesInstrumentedFailure, classesAlreadyInstrumented };

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

	/**
	 * All instances of the class, used to produce overall instrumentation statistics.
	 */
	private static Map<Object, InstrumentationControl> instances = new HashMap<Object, InstrumentationControl>();

	// ====================================== INSTANCE MEMBERS

	private Object instrumentationContext = null;

	private LoaderFilter classloaderWhitelist = null;
	private StringList classloaderBlacklist = new StringList();

	// ------- Members related to blacklist checks for JARs and DIRs

	/** Build from config param instr.blacklist.jars. */
	private StringList blacklistedJars = new StringList();

	/** Build from config param instr.blacklist.dirs. */
	private Set<Path> blacklistedDirs = new HashSet<Path>();

	/** JARs from which classes were loaded, together with the cached result of the blacklist check. */
	private Map<String, Boolean> checkedJars = new ConcurrentHashMap<String, Boolean>();

	private int acceptedJarsCount = 0, blacklistedJarsCount = 0;

	// ------- Members related to blacklist checks for classes

	/** Build from config param instr.blacklist.classes.jre and instr.blacklist.classes.custom. */
	private StringList blacklistedClasses  = new StringList();

	/** Classes for which {@link #isBlacklistedClass(String)} was called, together with the cached result of the blacklist check. */
	private Map<String, Boolean> checkedClasses = new HashMap<String, Boolean>();

	/** Counters for accepted and blacklisted classes. */
	private int acceptedClassesCount = 0, blacklistedClassesCount = 0;

	// ------- Members related to instrumentation stats (on class and package level)

	private int classesCount = 0, successfulInstrumentationCount = 0, failedInstrumentationCount = 0, alreadyInstrumentedCount = 0;
	private Map<JavaPackageId, Integer> successfulInstrumentationCountPP = new HashMap<JavaPackageId, Integer>();
	private Map<JavaPackageId, Integer> failedInstrumentationCountPP = new HashMap<JavaPackageId, Integer>();
	private Map<JavaPackageId, Integer> alreadyInstrumentedCountPP = new HashMap<JavaPackageId, Integer>();
	
	private Set<JavaId> failedInstrumentations = new HashSet<JavaId>();

	/**
	 * Instances are only created through {@link InstrumentationControl#getInstance()}, in order to maintain
	 * a set of all instances existing.
	 */
	private InstrumentationControl(Object _context) {
		this.instrumentationContext = _context;

		//TODO: It is maybe better to use static members for the blacklists, maybe no need to re-create the same lists over and over again

		// Get the configuration
		final Configuration cfg = VulasConfiguration.getGlobal().getConfiguration();

		// Only instrument classes loader by a certain class loader
		if(cfg.getString("instr.whitelist.classloader", null)!=null)
			this.classloaderWhitelist = new ClassNameLoaderFilter(cfg.getString("instr.whitelist.classloader", null), cfg.getBoolean("instr.whitelist.classloader.acceptChilds", true));

		// Blacklist from configuration (evaluated during transform)
		this.blacklistedClasses.addAll(cfg.getStringArray(CoreConfiguration.INSTR_BLACKLIST_CLASSES));
		this.blacklistedClasses.addAll(cfg.getStringArray(CoreConfiguration.INSTR_BLACKLIST_JRE_CLASSES));
		this.blacklistedClasses.addAll(cfg.getStringArray(CoreConfiguration.INSTR_BLACKLIST_CUSTOM_CLASSES));

		//TODO: Duplicate to the class loader filter?
		this.classloaderBlacklist.addAll(cfg.getStringArray(CoreConfiguration.INSTR_BLACKLIST_CLASSLOADER));

		// JAR and DIR blacklists (evaluated during transform)
		this.blacklistedJars.addAll(cfg.getStringArray(CoreConfiguration.INSTR_BLACKLIST_JARS));
		this.blacklistedJars.addAll(cfg.getStringArray(CoreConfiguration.INSTR_BLACKLIST_CUSTOM_JARS));
		final String[] items = cfg.getStringArray(CoreConfiguration.INSTR_BLACKLIST_DIRS);
		for(String item: items) {
			try {
				this.blacklistedDirs.add(Paths.get(item));
			} catch (Exception e) {
				InstrumentationControl.log.error("Error when adding [" + item + "] to blacklisted JAR dirs: " + e.getMessage());
			}
		}
		
		// Depending on the configuration, dependencies with certain scopes can be added to the JAR blacklist
		final String[] scopes = cfg.getStringArray(CoreConfiguration.INSTR_BLACKLIST_JAR_SCOPES);
		if(scopes!=null && scopes.length>0 && VulasConfiguration.getGlobal().hasServiceUrl(Service.BACKEND)) {
			try {
				// Read all dependencies and add JARs whose dependency matches the specified scope(s) to the blacklist
				final Set<Dependency> deps = BackendConnector.getInstance().getAppDeps(CoreConfiguration.buildGoalContextFromGlobalConfiguration(), CoreConfiguration.getAppContext());
				int blacklisted_deps_count = 0;
				for(Dependency dep: deps) {
					if(dep.getScope()!=null) {
						for(String scope: scopes) {
							if(scope.equalsIgnoreCase(dep.getScope().toString())) {
								this.blacklistedJars.add(dep.getFilename());
								blacklisted_deps_count++;
								break;
							}
						}
					}
				}
				InstrumentationControl.log.info("Added [" + blacklisted_deps_count + "] dependencies with scopes [" + StringUtil.join(scopes, ", ") + "] to JAR blacklist");
			} catch (ConfigurationException e) {
				InstrumentationControl.log.error("Configuration error when adding JARs with blacklisted scopes to JAR blacklist: " + e.getMessage());
			} catch (BackendConnectionException e) {
				InstrumentationControl.log.error("Connection error when adding JARs with blacklisted scopes to JAR blacklist: " + e.getMessage());
			}
		}
	}

	/**
	 * Updates the statistics depending on whether the instrumentation of the given Java class succeeded or not.
	 *
	 * @param _jcid the class that was instrumented successfully or not
	 * @param _instr_successful null if the class was already instrumented, or true/false if the instrumentation was done in this transformer
	 */
	public void updateInstrumentationStatistics(JavaId _jcid, Boolean _instr_successful) {
		final JavaPackageId pid = _jcid.getJavaPackageId();
		this.classesCount++;

		// Case 1: Already instrumented
		if(_instr_successful==null) {
			final Integer count = this.alreadyInstrumentedCountPP.get(pid);
			final Integer new_count = new Integer((count==null ? 1 : count.intValue()+1));
			this.alreadyInstrumentedCountPP.put(pid, new_count);
			this.alreadyInstrumentedCount++;
		}
		// Case 2: Successful instrumentation
		else if(_instr_successful.booleanValue()) {
			final Integer count = this.successfulInstrumentationCountPP.get(pid);
			final Integer new_count = new Integer((count==null ? 1 : count.intValue()+1));
			this.successfulInstrumentationCountPP.put(pid, new_count);
			this.successfulInstrumentationCount++;
		}
		// Case 3: Unsuccessful instrumentation
		else {
			final Integer count = this.failedInstrumentationCountPP.get(pid);
			final Integer new_count = new Integer((count==null ? 1 : count.intValue()+1));
			this.failedInstrumentationCountPP.put(pid, new_count);
			this.failedInstrumentationCount++;
			this.failedInstrumentations.add(_jcid);
		}
	}

	/**
	 * Logs various statistics, depending on the log level.
	 * Called by {@link UploadScheduler#run()}.
	 */
	public synchronized void logStatistics() {
		// Log loader hierarchy
		/*if(InstrumentationControl.log.isDebugEnabled()) {
			InstrumentationControl.log.debug("Class loader hierarchy:");
			this.loaderHierarchy.logHierarchy(this.loaderHierarchy.getRoot(), 0);
		}*/
		
		if(this.checkedJars.size()>0 || this.checkedClasses.size()>0)
			InstrumentationControl.log.info("Instrumentation metrics in context [" + this.instrumentationContext.toString() + "]:");

		if(this.checkedClasses.size()>0)
			InstrumentationControl.log.info("    Class name filter: [" + this.acceptedClassesCount + " classes] accepted for instrumentation, [" + this.blacklistedClassesCount + " classes] ignored (blacklisted)");
		
		// Accepted and ignored (blacklisted) JARs
		if(this.checkedJars.size()>0) {
			InstrumentationControl.log.info("    JAR name and directory filter: [" + this.acceptedJarsCount + " JARs] accepted for instrumentation, [" + this.blacklistedJarsCount + " JARs] ignored (blacklisted)");
			if(InstrumentationControl.log.isInfoEnabled())
				for(String path: this.checkedJars.keySet()) {
					if(this.checkedJars.get(path)) 
						InstrumentationControl.log.info("        [IGNOR] [" + path + "]");
					else
						InstrumentationControl.log.info("        [ACCEP] [" + path + "]");
				}
		}

		// Accepted and ignored (blacklisted) classes
		if(this.checkedClasses.size()>0) {
			InstrumentationControl.log.info("    Of  [" + StringUtil.padLeft(this.classesCount, 5) + "] classes considered for instrumentation after class and JAR filters:");
			InstrumentationControl.log.info("        [" + StringUtil.padLeft(this.alreadyInstrumentedCount, 5) + "] classes in [" + StringUtil.padLeft(this.alreadyInstrumentedCountPP.keySet().size(), 3) + "] packages: Instrumentation existed");
			if(InstrumentationControl.log.isDebugEnabled())
				for(JavaPackageId pid : this.alreadyInstrumentedCountPP.keySet())
					InstrumentationControl.log.debug("        |    " + this.alreadyInstrumentedCountPP.get(pid).intValue() + " in " + pid.toString());

			// Log no. of instrumented classes, and those for which instrumentation failed (per package)
			InstrumentationControl.log.info("        [" + StringUtil.padLeft(this.successfulInstrumentationCount, 5) + "] classes in [" + StringUtil.padLeft(this.successfulInstrumentationCountPP.keySet().size(), 3) + "] packages: Instrumentation successful");
			//		for(JavaPackageId pid : this.successfulInstrumentationCountPP.keySet())
			//			ConstructTracer.log.info("        |    " + this.successfulInstrumentationCountPP.get(pid).intValue() + " in " + pid.toString());

			InstrumentationControl.log.info("        [" + StringUtil.padLeft(this.failedInstrumentationCount, 5) + "] classes in [" + StringUtil.padLeft(this.failedInstrumentationCountPP.keySet().size(), 3) + "] packages: Instrumentation failed");
			if(InstrumentationControl.log.isDebugEnabled())
				for(JavaPackageId pid : this.failedInstrumentationCountPP.keySet())
					InstrumentationControl.log.debug("        |    " + this.failedInstrumentationCountPP.get(pid).intValue() + " in " + pid.toString());
		}
	}

	/**
	 * <p>getMetric.</p>
	 *
	 * @param _metric a {@link com.sap.psr.vulas.monitor.InstrumentationControl.InstrumentationMetrics} object.
	 * @return a long.
	 */
	public long getMetric(InstrumentationMetrics _metric) {
		if(InstrumentationMetrics.classesTotal.equals(_metric))
			return this.classesCount;
		else if(InstrumentationMetrics.classesAlreadyInstrumented.equals(_metric))
			return this.alreadyInstrumentedCount;
		else if(InstrumentationMetrics.classesInstrumentedSuccess.equals(_metric))
			return this.successfulInstrumentationCount;
		else if(InstrumentationMetrics.classesInstrumentedFailure.equals(_metric))
			return this.failedInstrumentationCount;
		else
			return -1;
	}

	/**
	 * <p>getStatistics.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<InstrumentationMetrics, Long> getStatistics() {
		final Map<InstrumentationMetrics, Long> stats = new HashMap<InstrumentationMetrics, Long>();
		stats.put(InstrumentationMetrics.classesTotal,  			 new Long(this.classesCount));
		stats.put(InstrumentationMetrics.classesAlreadyInstrumented, new Long(this.alreadyInstrumentedCount));
		stats.put(InstrumentationMetrics.classesInstrumentedSuccess, new Long(this.successfulInstrumentationCount));
		stats.put(InstrumentationMetrics.classesInstrumentedFailure, new Long(this.failedInstrumentationCount));
		return stats;
	}

	/**
	 * Returns true if the JAR at the specified path is blacklisted, false otherwise.
	 * A JAR is blacklisted if the file name matches any of the patterns specified with configuration parameter instr.blacklist.jars,
	 * or if it resides in a directory (or subdirectory) of any of the paths specified with instr.blacklist.dirs.
	 *
	 * @param _url a {@link java.net.URL} object.
	 * @return a boolean.
	 */
	public boolean isBlacklistedJar(URL _url) {
		// If the URL does not point to a class that has been loaded from a URL, return false
		if(_url==null) return true;

		// Check whether it has been loaded from a JAR: If not, return false (probably an app class), otherwise check the blacklist
		final String jar_path = FileUtil.getJARFilePath(_url.toString());
		if(jar_path==null) return false;

		// If a JAR URL can be determined, check if we already checked whether it is blacklisted
		if(!this.checkedJars.containsKey(jar_path)) {
			String path2 = jar_path.toString().replaceAll("C:", "C");
			final String jar_file = Paths.get(path2).getFileName().toString();

			// Compare path with whitelist
			boolean blacklisted_dir = false;
			for(Path path: this.blacklistedDirs) {
				if(Paths.get(path2).startsWith(path)) {
					blacklisted_dir = true;
					break;
				}
			}

			// Compare filename with blacklist
			boolean blacklisted_jar = this.blacklistedJars.contains(jar_file, StringList.ComparisonMode.PATTERN, StringList.CaseSensitivity.CASE_INSENSITIVE);

			// Result
			final boolean is_blacklisted = blacklisted_dir || blacklisted_jar;
			InstrumentationControl.log.info("JAR [" + jar_path + "] is blacklisted: [" + is_blacklisted + "]");

			// Cache
			this.checkedJars.put(jar_path, is_blacklisted);

			// Stats
			if(is_blacklisted) this.blacklistedJarsCount++;
			else this.acceptedJarsCount++;
		}

		return this.checkedJars.get(jar_path);
	}

	/**
	 * Returns true if the class with the specified qualified name is blacklisted, false otherwise.
	 * A qualified name is blacklisted if its package (or any of its parent packages) is specified
	 * as part of the configuration parameters instr.blacklist.classes.jre or instr.blacklist.classes.custom.
	 *
	 * @param _qname a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean isBlacklistedClass(String _qname) {
		if(!this.checkedClasses.containsKey(_qname)) {
			// Result
			final boolean is_blacklisted = this.blacklistedClasses.contains(_qname, StringList.ComparisonMode.STARTSWITH, StringList.CaseSensitivity.CASE_SENSITIVE);

			// Cache
			this.checkedClasses.put(_qname,  is_blacklisted);

			// Stats
			if(is_blacklisted) this.blacklistedClassesCount++;
			else this.acceptedClassesCount++;
		}
		return this.checkedClasses.get(_qname);
	}

	/**
	 * Returns the total number of classes looked at.
	 *
	 * @return a int.
	 */
	public int countClassesTotal() { return this.classesCount; }

	/**
	 * Returns the number of classes that were already instrumented.
	 *
	 * @see #updateInstrumentationStatistics(JavaId, Boolean)
	 * @return a int.
	 */
	public int countClassesInstrumentedAlready() { return this.alreadyInstrumentedCount; }

	/**
	 * Returns the number of classes that were successfully instrumented.
	 *
	 * @see #updateInstrumentationStatistics(JavaId, Boolean)
	 * @return a int.
	 */
	public int countClassesInstrumentedSuccess() { return this.successfulInstrumentationCount; }

	/**
	 * Returns the number of classes that could not be instrumented.
	 *
	 * @see #updateInstrumentationStatistics(JavaId, Boolean)
	 * @return a int.
	 */
	public int countClassesInstrumentedFailure() { return this.failedInstrumentationCount; }
	
	/**
	 * Returns the classes which could not be instrumented, hence, for which no
	 * traces or other information could be collected.
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<JavaId> getFailedInstrumentations() { return this.failedInstrumentations; }
	
	// ====================================== STATIC MEMBERS

	/**
	 * <p>getInstance.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.monitor.InstrumentationControl} object.
	 */
	public static synchronized InstrumentationControl getInstance() {
		return InstrumentationControl.getInstance(null);
	}

	/**
	 * <p>getInstance.</p>
	 *
	 * @param _context a {@link java.lang.Object} object.
	 * @return a {@link com.sap.psr.vulas.monitor.InstrumentationControl} object.
	 */
	public static synchronized InstrumentationControl getInstance(Object _context) {
		InstrumentationControl instance = null;
		if(!instances.containsKey(_context))
			InstrumentationControl.instances.put(_context, new InstrumentationControl(_context));
		instance = instances.get(_context);
		return instance;
	}

	/**
	 * <p>getOverallStatistics.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public static synchronized Map<String, Long> getOverallStatistics() {
		final Map<String, Long> overall_stats = new HashMap<String, Long>();
		for(InstrumentationControl ctrl: instances.values()) {
			final Map<InstrumentationMetrics, Long> stats = ctrl.getStatistics();
			for(InstrumentationMetrics m: stats.keySet()) {
				final String key = m.toString();
				if(overall_stats.containsKey(key)) {
					final long new_count = overall_stats.get(key).longValue() + stats.get(m).longValue();
					overall_stats.put(key, new Long(new_count));
				} else {
					overall_stats.put(key,  stats.get(m));
				}
			}
		}
		return overall_stats;
	}

	/**
	 * <p>logOverallStatistics.</p>
	 */
	public static synchronized void logOverallStatistics() {
		for(InstrumentationControl ctrl: instances.values()) {
			ctrl.logStatistics();
		}
	}
}
