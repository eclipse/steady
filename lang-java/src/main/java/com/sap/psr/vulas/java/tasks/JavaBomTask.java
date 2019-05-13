package com.sap.psr.vulas.java.tasks;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.DirAnalyzer;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.java.JarAnalysisManager;
import com.sap.psr.vulas.java.JarAnalyzer;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.ThreadUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.sap.psr.vulas.tasks.AbstractBomTask;

public class JavaBomTask extends AbstractBomTask {

	private static final Log log = LogFactory.getLog(JavaBomTask.class);
	
	private static final String[] EXT_FILTER = new String[] { "jar", "war", "class", "java", "aar" };

	private String[] appPrefixes = null;

	private StringList appJarNames = null;
	
	private static final List<GoalClient> pluginGoalClients = Arrays.asList(GoalClient.MAVEN_PLUGIN, GoalClient.GRADLE_PLUGIN);

	@Override
	public Set<ProgrammingLanguage> getLanguage() { return new HashSet<ProgrammingLanguage>(Arrays.asList(new ProgrammingLanguage[] { ProgrammingLanguage.JAVA })); }

	/**
	 * Returns true if the configuration setting {@link CoreConfiguration#APP_PREFIXES} shall be considered, false otherwise.
	 */
	private final boolean useAppPrefixes() {
		return this.appPrefixes!=null && !this.isOneOfGoalClients(pluginGoalClients);
	}

	/**
	 * Returns true if the configuration setting {@link CoreConfiguration#APP_PREFIXES} shall be considered, false otherwise.
	 */
	private final boolean useAppJarNames() {
		return this.appJarNames!=null && !this.isOneOfGoalClients(pluginGoalClients);
	}

	@Override
	public void configure(VulasConfiguration _cfg) throws GoalConfigurationException {
		super.configure(_cfg);

		// App constructs identified using package prefixes
		this.appPrefixes = _cfg.getStringArray(CoreConfiguration.APP_PREFIXES, null);

		// Print warning message in case the setting is used as part of the Maven plugin
		if(this.appPrefixes!=null && this.isOneOfGoalClients(pluginGoalClients)) {
			log.warn("Configuration setting [" + CoreConfiguration.APP_PREFIXES + "] ignored when running the goal as Maven plugin");
			this.appPrefixes = null;
		}

		// App constructs identified using JAR file name patterns (regex)
		final String[] app_jar_names = _cfg.getStringArray(CoreConfiguration.APP_JAR_NAMES, null);
		if(app_jar_names!=null) {
			// Print warning message in case the setting is used as part of the Maven plugin
			if( this.isOneOfGoalClients(pluginGoalClients)) {
				log.warn("Configuration setting [" + CoreConfiguration.APP_JAR_NAMES + "] ignored when running the goal as Maven plugin");
				this.appJarNames = null;
			}
			else {
				this.appJarNames = new StringList();
				this.appJarNames.addAll(app_jar_names);
			}
		}

		// CLI: Only one of appPrefixes and appJarNames can be used
		if(!this.isOneOfGoalClients(pluginGoalClients)) {
			if(this.appPrefixes!=null && this.appJarNames!=null) {
				throw new GoalConfigurationException("Exactly one of the configuration settings [" + CoreConfiguration.APP_PREFIXES + "] and [" + CoreConfiguration.APP_JAR_NAMES + "] must be set");
			}
			else if(this.appPrefixes==null && this.appJarNames==null) {
				throw new GoalConfigurationException("Exactly one of the configuration settings [" + CoreConfiguration.APP_PREFIXES + "] and [" + CoreConfiguration.APP_JAR_NAMES + "] must be set");
			}
		}
	}

	@Override
	public void execute() throws GoalExecutionException {

		// All app constructs
		final Set<ConstructId> app_constructs = new HashSet<ConstructId>();

		// Dependency files
		final Map<Path, JarAnalyzer> dep_files = new HashMap<Path, JarAnalyzer>();
		if(this.getKnownDependencies()!=null) {
			for(Path p: this.getKnownDependencies().keySet())
				dep_files.put(p, null);
		}

		// 1) Find app constructs by looping over all app paths
		if(this.hasSearchPath()) {
			for(Path p: this.getSearchPath()) {
				log.info("Searching for Java constructs in search path [" + p + "] with filter [" + StringUtil.join(EXT_FILTER, ", ") + "] ...");
				final FileAnalyzer fa = FileAnalyzerFactory.buildFileAnalyzer(p.toFile(), EXT_FILTER);

				// Prefixes or jar name regex: Filter JAR constructs
				if(this.useAppPrefixes() || this.useAppJarNames()) {

					// All analyzers to loop over
					final Set<FileAnalyzer> analyzers = new HashSet<FileAnalyzer>();

					// Add child analyzers and analyzer itself (except DirAnalyzer)
					if(fa.hasChilds())
						analyzers.addAll(fa.getChilds(true));					
					if(!(fa instanceof DirAnalyzer))
						analyzers.add(fa);
					
					// Log
					int count = 0;
					if(this.useAppPrefixes()) {
						log.info("Looping over Java archive analyzers to separate application and dependency code using package prefix(es) [" + StringUtil.join(this.appPrefixes, ", ") + "] ...");
					} else if(this.useAppJarNames()) {
						log.info("Looping over Java archive analyzers to separate application and dependency code using filename pattern(s) [" + this.appJarNames.toString(", ") + "] ...");
					}

					// Loop over all analyzers
					for(FileAnalyzer fa2: analyzers) {

						try {
							if(fa2 instanceof JarAnalyzer) {
								final JarAnalyzer ja = (JarAnalyzer)fa2;

								// Prefixes
								if(this.useAppPrefixes()) {							
									final Set<ConstructId> constructs = JavaId.filter(fa2.getConstructs().keySet(), this.appPrefixes);

									// Constructs match to the prefixes
									if(constructs!=null && constructs.size()>0) {
										app_constructs.addAll(constructs);
										
										// Exclusively app constructs
										if(constructs.size()==fa2.getConstructs().size()) {
											log.info(StringUtil.padLeft(++count,  4) + " [" + StringUtil.padLeft(ja.getFileName(), 30) + "]: All [" + fa2.getConstructs().size() + "] constructs matched prefix(es): Constructs added to application, file NOT added as dependency");
										}
										// Mixed archive: Add as dependency
										else {
											log.info(StringUtil.padLeft(++count,  4) + " [" + StringUtil.padLeft(ja.getFileName(), 30) + "]: [" + constructs.size() + "/" + fa2.getConstructs().size() + "] constructs matched prefix(es): Constructs added to application, file added as dependency");
											dep_files.put(ja.getPath(), ja);
										}
									}
									// No constructs match to the prefixes
									else {
										log.info(StringUtil.padLeft(++count,  4) + " [" + StringUtil.padLeft(ja.getFileName(), 30) + "]: None of the [" + fa2.getConstructs().size() + "] constructs matched prefix(es): No constructs added to application, file added as dependency");
										dep_files.put(ja.getPath(), ja);
									}
								}
								// Jar name regex
								else if(this.useAppJarNames()) {
									// Belongs to application
									if(this.appJarNames.contains(ja.getFileName(), StringList.ComparisonMode.PATTERN, StringList.CaseSensitivity.CASE_INSENSITIVE)) {
										log.info(StringUtil.padLeft(++count,  4) + " [" + StringUtil.padLeft(ja.getFileName(), 30) + "]: Filename matches pattern(s), all of its [" + fa2.getConstructs().size() + "] constructs added to application");
										app_constructs.addAll(fa2.getConstructs().keySet());
									}
									// Dependency
									else {
										log.info(StringUtil.padLeft(++count,  4) + " [" + StringUtil.padLeft(ja.getFileName(), 30) + "]: Filename does not match pattern(s), file added as dependency");
										dep_files.put(ja.getPath(), ja);
									}
								}							
							}
							// Important: What is in java and class files is always part of the app
							else {
								app_constructs.addAll(fa2.getConstructs().keySet());
							}
						} catch (FileAnalysisException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
				// No prefixes and jar name regex: Add all constructs to app
				else {
					try {
						app_constructs.addAll(fa.getConstructs().keySet());
					} catch (FileAnalysisException e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		}

		// 2) Analyze all of the JAR/WAR files
		final Set<JarAnalyzer> app_dependencies = new HashSet<JarAnalyzer>();
	
		final long timeout   = this.vulasConfiguration.getConfiguration().getLong(CoreConfiguration.JAR_TIMEOUT, -1);
		final int no_threads = ThreadUtil.getNoThreads(this.vulasConfiguration, 2);
		
		final JarAnalysisManager mgr = new JarAnalysisManager(no_threads, timeout, false, this.getApplication());
		mgr.setMavenDependencies(this.getKnownDependencies());
		mgr.startAnalysis(dep_files, null);

		// Loop over all analyzers created above and add to app dependencies
		final Set<JarAnalyzer> analyzers = mgr.getAnalyzers();
		for(JarAnalyzer ja: analyzers) {

			try {
				// Prefixes can be used: Filter JAR constructs
				if(this.useAppPrefixes()) {
					final Set<ConstructId> constructs = JavaId.filter(ja.getConstructs().keySet(), this.appPrefixes);

					// Constructs match to the prefixes
					if(constructs!=null && constructs.size()>0) {
						app_constructs.addAll(constructs);

						// Exclusively app constructs
						if(constructs.size()==ja.getConstructs().size()) {
							log.info("All of the [" + ja.getConstructs().size() + "] constructs from [" + ja.getFileName() + "] matched to prefix [" + StringUtil.join(this.appPrefixes, ", ") + "]: Constructs added to application, file removed from dependencies");
						}
						// Mixed archive: Add as dependency
						else {
							log.info("[" + constructs.size() + "/" + ja.getConstructs().size() + "] constructs from [" + ja.getFileName() + "] matched to prefix [" + StringUtil.join(this.appPrefixes, ", ") + "]: Constructs added to application, file kept as dependency");
							app_dependencies.add(ja);
						}
					}
					// No constructs match to the prefixes
					else {
						log.info("None of the [" + ja.getConstructs().size() + "] constructs from [" + ja.getFileName() + "] matched to prefix [" + StringUtil.join(this.appPrefixes, ", ") + "]: No constructs added to application, file kept as dependency");
						app_dependencies.add(ja);
					}
				}
				// Prefixes cannot be used: Add JAR as dependency unless jar name regex exists
				else {
					if(this.useAppJarNames() && this.appJarNames.contains(ja.getFileName(), StringList.ComparisonMode.PATTERN, StringList.CaseSensitivity.CASE_INSENSITIVE)) {
						app_constructs.addAll(ja.getConstructs().keySet());
					} else {
						app_dependencies.add(ja);
					}
				}
			} catch (FileAnalysisException e) {
				log.error(e.getMessage(), e);
			}
		}

		// Update application
		final Application a = this.getApplication();
		a.addConstructs(ConstructId.getSharedType(app_constructs));

		// Loop all JAR analyzers and add a corresponding dependency
		for(JarAnalyzer ja: app_dependencies) {
			try {
				Dependency dep = null;
				if(ja.getParent()!=null){
					dep = mgr.getMavenDependency(ja.getParent().getPath());
				}
				else
					dep = mgr.getMavenDependency(ja.getPath());

				final Dependency new_dep = new Dependency();
				new_dep.setLib(ja.getLibrary());
				new_dep.setApp(this.getApplication());
				new_dep.setFilename(ja.getFileName());
				new_dep.setPath(ja.getPath().toString());
				
				new_dep.setScope( (dep!=null ? dep.getScope() : Scope.RUNTIME) );
				new_dep.setTransitive( (ja.getParent()!= null? new Boolean(true) :(dep!=null ? new Boolean(dep.getTransitive()) : new Boolean(false)) ) );
				new_dep.setDeclared( ((dep!=null && ja.getParent()==null) ? new Boolean(true): new Boolean(false)) );
				
				a.addDependency(new_dep);
			} catch (FileAnalysisException e) {
				log.error(e.getMessage(), e);
			}
		}

		// Set the one to be returned
		this.setCompletedApplication(a);
	}
}
