package com.sap.psr.vulas.cg;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.sap.psr.vulas.shared.enums.GoalClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.goals.AbstractAppGoal;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.java.JarWriter;
import com.sap.psr.vulas.monitor.ClassPoolUpdater;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.util.FileSearch;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.StringList.CaseSensitivity;
import com.sap.psr.vulas.shared.util.StringList.ComparisonMode;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>Abstract AbstractReachGoal class.</p>
 *
 */
public abstract class AbstractReachGoal extends AbstractAppGoal {

    private static final Log log = LogFactory.getLog(AbstractReachGoal.class);

    private Set<Path> preparedDepClasspath = new HashSet<Path>();

    private Set<Path> preparedAppClasspath = new HashSet<Path>();

    /** Rewritten Java archives to be deleted after goal execution. */
    private Set<Path> rewrittenJars = new HashSet<Path>();

    private Set<com.sap.psr.vulas.shared.json.model.ConstructId> appConstructs = null;

    /**
     * <p>Constructor for AbstractReachGoal.</p>
     *
     * @param _type a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
     */
    protected AbstractReachGoal(GoalType _type) {
        super(_type);
    }

    /**
     * Calls the backend in order to retrieve all application constructs of the given application.
     *
     * @return a {@link java.util.Set} object.
     */
    protected final Set<com.sap.psr.vulas.shared.json.model.ConstructId> getAppConstructs() {
        if (this.appConstructs == null) {
            try {
                this.appConstructs = BackendConnector.getInstance().getAppConstructIds(this.getGoalContext(), this.getApplicationContext());
            } catch (BackendConnectionException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
        return this.appConstructs;
    }

    /**
     * Prepares the classpaths, including the rewriting of JAR files (if requested).
     */
    private final void prepareClasspath() {

        // The problems described in Jira ticket VULAS-1429 look as if the caches survive A2C executions on different modules. Check and clear explicitly.
        ClassPoolUpdater.getInstance().reset();

        final FileSearch jar_search = new FileSearch(JAR_EXT);
        final FileSearch class_search = new FileSearch(CLASS_EXT);

        final boolean preprocess = this.getConfiguration().getConfiguration().getBoolean(ReachabilityConfiguration.REACH_PREPROCESS, true);

        final StringList exclude_jars = new StringList(this.getConfiguration().getConfiguration().getStringArray(ReachabilityConfiguration.REACH_EXCL_JARS));

        // Append known dependencies to classpath (can be none in case of CLI)
        for (Path p : this.getKnownDependencies().keySet()) {
        	Path appended_path = null;
          Path fileName = p.getFileName();

          if (fileName != null) {
            if (exclude_jars.isEmpty())
              appended_path = JarWriter.appendToClasspath(this.preparedDepClasspath, p, preprocess);
            else if (!exclude_jars.contains(fileName.toString(), ComparisonMode.PATTERN, CaseSensitivity.CASE_INSENSITIVE))
              appended_path = JarWriter.appendToClasspath(this.preparedDepClasspath, p, preprocess);
            else
              log.info("[" + p + "] excluded from reachability analysis");
          }

          if(appended_path!=null && !appended_path.equals(p))
          	this.rewrittenJars.add(appended_path);
        }

        ClassPoolUpdater.getInstance().appendToClasspath(this.preparedDepClasspath);

        // Find JARs in all source paths (can contain app code and dependencies in case of the CLI)
        for (Path app_dir : this.getAppPaths()) {
            // Search for JAR files
            jar_search.clear();

            // Add them one by one to the classpath (except those excluded through configuration)
            final Set<Path> paths = jar_search.search(app_dir);
              if (paths != null){
                for (Path p : paths) {
                  Path appended_path = null;
                  Path fileName = p.getFileName();

                  if (fileName != null) {
                    if (exclude_jars.isEmpty())
                      appended_path = JarWriter.appendToClasspath(this.preparedAppClasspath, p, preprocess);
                      else if (!exclude_jars.contains(fileName.toString(), ComparisonMode.PATTERN, CaseSensitivity.CASE_INSENSITIVE))
                        appended_path = JarWriter.appendToClasspath(this.preparedAppClasspath, p, preprocess);
                      else
                          log.info("[" + p + "] excluded from reachability analysis");

                    if(appended_path!=null && !appended_path.equals(p))
                        this.rewrittenJars.add(appended_path);
                  }
              }
            }

            // Search for class files
            class_search.clear();
            final Set<Path> classes = class_search.search(app_dir);
            log.info("Update class path for [" + classes.size() + "] class files");
            this.preparedAppClasspath.addAll(ClassPoolUpdater.getInstance().getClasspaths(classes));
        }

        ClassPoolUpdater.getInstance().appendToClasspath(preparedAppClasspath);

        log.info("Rewrote [" + this.rewrittenJars.size() + "] dependencies");
    }

    /**
     * Gets the entry points of the {@link ReachabilityAnalyzer}.
     * <p>
     * MUST be overridden by subclasses, e.g., by {@link A2CGoal} and {@link T2CGoal}.
     *
     * @return a {@link java.util.Set} object.
     */
    protected abstract Set<com.sap.psr.vulas.shared.json.model.ConstructId> getEntryPoints();

    /**
     * Sets the entry points of the {@link ReachabilityAnalyzer}.
     * <p>
     * MUST be overridden by subclasses, e.g., by {@link A2CGoal} and {@link T2CGoal}.
     *
     * @param _ra a {@link com.sap.psr.vulas.cg.ReachabilityAnalyzer} object.
     */
    protected abstract void setEntryPoints(ReachabilityAnalyzer _ra);

    /**
     * {@inheritDoc}
     *
     * Prepares the classpaths.
     */
    @Override
    protected final void prepareExecution() throws GoalConfigurationException {
        super.prepareExecution();
        this.prepareClasspath();
    }

    /** {@inheritDoc} */
    @Override
    protected final void executeTasks() throws Exception {

        // Create reachability analyzer
        final ReachabilityAnalyzer ra = new ReachabilityAnalyzer(this.getGoalContext());
        ra.setAppConstructs(this.getAppConstructs());
        ra.setAppClasspaths(this.preparedAppClasspath);
        ra.setDependencyClasspaths(this.preparedDepClasspath);

        // Are there any entry points
        if (this.getEntryPoints() == null || this.getEntryPoints().isEmpty()) {
            log.warn("No entry points, reachability analysis cannot be performed");
            return;
        }

        // Set the entry points in the resp. subclass
        this.setEntryPoints(ra);

        //set the call graph constructor, based on the configured framework
        ra.setCallgraphConstructor(this.getConfiguration().getConfiguration().getString(ReachabilityConfiguration.REACH_FWK, "wala"), this.getGoalClient() == GoalClient.CLI);

        ra.setTargetConstructs(this.getConfiguration().getConfiguration().getString(ReachabilityConfiguration.REACH_BUGS, null));
        ra.setExcludePackages(this.getConfiguration().getConfiguration().getString(ReachabilityConfiguration.REACH_EXCL_PACK, null));

        // Trigger the analysis
        final boolean success = ReachabilityAnalyzer.startAnalysis(ra, this.getConfiguration().getConfiguration().getInt(ReachabilityConfiguration.REACH_TIMEOUT, 15) * 60L * 1000L);

        // Upload
        if (success)
            ra.upload();

        // Add goal stats
        this.addGoalStats(this.getGoalType().toString() + ".analysisTerminated", (success ? 1 : 0));
        this.addGoalStats(this.getGoalType().toString() + ".entryPoints", this.getEntryPoints().size());
        this.addGoalStats(this.getGoalType().toString() + ".classpathLength", ra.getAppClasspath().split(System.getProperty("path.separator")).length + ra.getDependencyClasspath().split(System.getProperty("path.separator")).length);
        this.addGoalStats(this.getGoalType().toString() + ".callgraphNodes", ra.getNodeCount());
        this.addGoalStats(this.getGoalType().toString() + ".callgraphEdges", ra.getEdgeCount());
        this.addGoalStats(this.getGoalType().toString(), ra.getStatistics());
    }

    /**
     * {@inheritDoc}
     *
     * Deletes pre-processed JAR files (if any).
     */
    @Override
    protected final void cleanAfterExecution() {
        if (this.getConfiguration().getConfiguration().getBoolean(ReachabilityConfiguration.REACH_PREPROCESS, true)) {
        	log.info("Deleting [" + this.rewrittenJars.size() + "] temporary (pre-processed) dependencies...");
            for (Path p : this.rewrittenJars) {
                try {
                    boolean ret = p.toFile().delete();
                    if(ret)
                        log.info("    Deleted temporary (pre-processed) dependency [" + p + "] ");
                    else
                        log.warn("    Cannot delete temporary (pre-processed) dependency [" + p + "]");
                } catch (Exception ioe) {
                    log.error("    Cannot delete temporary (pre-processed) dependency [" + p + "]: " + ioe.getMessage());
                }
            }
        }
    }
}
