package com.sap.psr.vulas.mvn;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractAppGoal;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.StringList.CaseSensitivity;
import com.sap.psr.vulas.shared.util.StringList.ComparisonMode;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public abstract class AbstractVulasMojo extends AbstractMojo {

    private static final String INCLUDES = "vulas.maven.includes";

    private static final String EXCLUDES = "vulas.maven.excludes";

    private static final String IGNORE_POMS = "vulas.maven.ignorePoms";

    @Parameter(defaultValue = "${project}", property = "project", required = true, readonly = true)
    protected MavenProject project;


    @Parameter(defaultValue = "${session}", property = "session", required = true, readonly = true)
    protected MavenSession session;

    /**
     * All plugin configuration settings of the element <layeredConfiguration> are put in this {@link Map}.
     */
    @Parameter
    private Map<?, ?> layeredConfiguration;

    protected AbstractAppGoal goal = null;

    private StringList includeArtifacts = null;
    private StringList excludeArtifacts = null;
    private boolean ignorePoms = false;

    /**
     * Puts the plugin configuration element <layeredConfiguration> as a new layer into {@link VulasConfiguration}.
     * If no such element exists, e.g., because the POM file does not contain a plugin section for Vulas, default settings
     * are established using {@link MavenProject} and {@link VulasConfiguration#setPropertyIfEmpty(String, Object)}.
     *
     * @throws Exception
     */
    public final void prepareConfiguration() throws Exception {

        // Delete any transient settings that remaining from a previous goal execution (if any)
        final boolean contained_values = VulasConfiguration.getGlobal().clearTransientProperties();
        if (contained_values)
            getLog().info("Transient configuration settings deleted");

        // Get the configuration layer from the plugin configuration (can be null)
        VulasConfiguration.getGlobal().addAfterSystemProperties("Plugin configuration", this.layeredConfiguration, null, true);

        // Check whether the application context can be established
        Application app = null;
        try {
            app = CoreConfiguration.getAppContext();
        }
        // In case the plugin is called w/o using the Vulas profile, project-specific settings are not set
        // Set them using the project member
        catch (ConfigurationException e) {
            VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.APP_CTX_GROUP, this.project.getGroupId());
            VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.APP_CTX_ARTIF, this.project.getArtifactId());
            VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.APP_CTX_VERSI, this.project.getVersion());
            app = CoreConfiguration.getAppContext();
        }

        // Set defaults for all the paths
        VulasConfiguration.getGlobal().setPropertyIfEmpty(VulasConfiguration.TMP_DIR, Paths.get(this.project.getBuild().getDirectory(), "vulas", "tmp").toString());
        VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.UPLOAD_DIR, Paths.get(this.project.getBuild().getDirectory(), "vulas", "upload").toString());
        VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_SRC_DIR, Paths.get(this.project.getBuild().getDirectory()).toString());
        VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_TARGET_DIR, Paths.get(this.project.getBuild().getDirectory(), "vulas", "target").toString());
        VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_INCLUDE_DIR, Paths.get(this.project.getBuild().getDirectory(), "vulas", "include").toString());
        VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_LIB_DIR, Paths.get(this.project.getBuild().getDirectory(), "vulas", "lib").toString());
        VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.REP_DIR, Paths.get(this.project.getBuild().getDirectory(), "vulas", "report").toString());

        // Read app constructs from src/main/java and target/classes
        final String p = Paths.get(this.project.getBuild().getOutputDirectory()).toString() + "," + Paths.get(this.project.getBuild().getSourceDirectory()).toString();
        VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.APP_DIRS, p);

        // Test how-to get the reactor POM in a reliable manner
        // The following method call fails if Maven is called with option -pl
        getLog().info("Top level project: " + this.session.getTopLevelProject());
        getLog().info("Execution root dir: " + this.session.getExecutionRootDirectory());

        // Includes, excludes and ignorePoms
        this.includeArtifacts = new StringList(VulasConfiguration.getGlobal().getStringArray(INCLUDES, null));
        this.excludeArtifacts = new StringList(VulasConfiguration.getGlobal().getStringArray(EXCLUDES, null));
        this.ignorePoms = VulasConfiguration.getGlobal().getConfiguration().getBoolean(IGNORE_POMS, false);
    }

    /**
     * This method, called by Maven, first invokes {@link AbstractVulasMojo#createGoal()} and then {@link AbstractVulasMojo#executeGoal()}.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            this.prepareConfiguration();

            final boolean do_process = this instanceof MvnPluginReport || this.isPassingFilter(this.project);
            if (do_process) {
                // Create the goal
                this.createGoal();
                this.goal.setGoalClient(GoalClient.MAVEN_PLUGIN);
                this.goal.setConfiguration(VulasConfiguration.getGlobal());

                // Set the application paths
                this.goal.addAppPaths(FileUtil.getPaths(VulasConfiguration.getGlobal().getStringArray(CoreConfiguration.APP_DIRS, null)));

                // Set the dependency paths
                this.setKnownDependencies();

                // Execute the goal
                this.executeGoal();
            }
        }
        // Expected problems will be passed on as is
        catch (MojoFailureException mfe) {
            throw mfe;
        }
        // Unexpected problems (the goal execution terminates abnormally/unexpectedly)
        catch (GoalExecutionException gee) {
            throw new MojoExecutionException(gee.getMessage(), gee);
        }
        // Every other exception results in a MojoExecutionException (= unexpected)
        catch (Exception e) {
            throw new MojoExecutionException("Error during Vulas goal execution " + this.goal + ": ", e);
        }
    }

    /**
     * Evaluates the configuration settings {@link AbstractVulasMojo#INCLUDES}, {@link AbstractVulasMojo#EXCLUDES} and {@link #IGNORE_POMS} to
     * determine whether the given {@link MavenProject} shall be processed.
     *
     * @param _prj
     * @return
     */
    private boolean isPassingFilter(MavenProject _prj) {
        boolean do_process = true;

        // Only included ones
        if (!this.includeArtifacts.isEmpty()) {
            do_process = this.includeArtifacts.contains(_prj.getArtifactId(), ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE);
            if (do_process)
                this.getLog().info("Artifact [" + _prj.getArtifactId() + "] explicitly included for processing via configuration parameter [" + INCLUDES + "]");
            else
                this.getLog().warn("Artifact [" + _prj.getArtifactId() + "] will NOT be processed, it is not among those explicitly included for processing via configuration parameter [" + INCLUDES + "]");
        }

        // Excluded (explicitly or through packaging)
        else {
            if (!this.excludeArtifacts.isEmpty() && this.excludeArtifacts.contains(_prj.getArtifactId(), ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE)) {
                this.getLog().warn("Artifact [" + _prj.getArtifactId() + "] explicitly excluded from processing via configuration parameter [" + EXCLUDES + "]");
                do_process = false;
            }
            if (do_process && this.ignorePoms && "POM".equalsIgnoreCase(_prj.getPackaging())) {
                this.getLog().warn("Artifact [" + _prj.getArtifactId() + "] excluded from processing via configuration parameter [" + IGNORE_POMS + "]");
                do_process = false;
            }
        }

        return do_process;
    }

    /**
     * Creates the respective goal.
     * <p>
     * MUST be overridden by subclasses.
     *
     * @return
     */
    protected abstract void createGoal();

    /**
     * Simply calls {@AbstractAnalysisGoal#execute}.
     * <p>
     * CAN be overridden by subclasses.
     *
     * @return
     */
    protected void executeGoal() throws Exception {
        this.goal.executeSync();
    }

    /**
     * Identifies known dependencies and passes them to Mojos inheriting from {@link AbstractAppGoal}.
     * Note that such subclasses should be annotated with "requiresDependencyResolution = ResolutionScope.TEST".
     *
     * @throws DependencyResolutionRequiredException
     */
    private final void setKnownDependencies() throws DependencyResolutionRequiredException {
        if (this.goal != null && this.goal instanceof AbstractAppGoal) {

            // ---- Determine dependencies (Vulas 2.x)

			/*final Set<String> runtime_system_classpath = new HashSet<String>();
			runtime_system_classpath.addAll(project.getRuntimeClasspathElements());
			runtime_system_classpath.addAll(project.getSystemClasspathElements());
			int i=0;
			for (final String resource : runtime_system_classpath) {
				//this.goal.addDepPath(Paths.get(resource));
				getLog().info("Dependency [" + ++i + "]: " + resource);
			}*/

            // ---- Determine dependencies (Vulas 3.x)

            // Not sure why this line existed, let's see what happens if I comment it out :)
            //final ClassLoader originalContextClassLoader = currentThread().getContextClassLoader();

            // Old way of learning about dependencies (getArtifacts seems much better)
            //for (final String resource : project.getRuntimeClasspathElements()) {

            // Path to dependency info
            final Map<Path, Dependency> dep_for_path = new HashMap<Path, Dependency>();

            // Dependencies (direct and transitive), including Maven ID and file system path
            @SuppressWarnings("deprecation") final Set<Artifact> direct_artifacts = project.getDependencyArtifacts(); // The artifact class does not seem to tell whether it is a direct or transitive dependency (hence we keep the call and suppress the warning)
            final Set<Artifact> artifacts = project.getArtifacts();
            int count = 0;
            Dependency dep = null;
            Library lib = null;
            for (Artifact a : artifacts) {
                // Create lib w/o SHA1
                lib = new Library();
                lib.setLibraryId(new LibraryId(a.getGroupId(), a.getArtifactId(), a.getVersion()));

                // Create dependency and put into map
                dep = new Dependency(this.goal.getGoalContext().getApplication(), lib, Scope.valueOf(a.getScope().toUpperCase()), !direct_artifacts.contains(a), null, a.getFile().toPath().toString());
                dep_for_path.put(a.getFile().toPath(), dep);

                getLog().info("Dependency [" + StringUtil.padLeft(++count, 4) + "]: Dependency [libid=" + dep.getLib().getLibraryId() + ", path " + a.getFile().getPath() + ", direct=" + direct_artifacts.contains(a) + ", scope=" + dep.getScope() + "] created for Maven artifact [g=" + a.getGroupId() + ", a=" + a.getArtifactId() + ", base version=" + a.getBaseVersion() + ", version=" + a.getVersion() + ", classifier=" + a.getClassifier() + "]");
                getLog().info("    " + this.trailToString(a.getDependencyTrail(), " => "));
            }

            //TODO: Is it necessary to check whether the above dependency (via getArtifacts) is actually the one added to the classpath (via project.getRuntimeClasspathElements())?
            //TODO: It may be that a different version (file) is chosen due to conflict resolution. Still, those cases should also be visible in the frontend (archive view).

            ((AbstractAppGoal) this.goal).setKnownDependencies(dep_for_path);
        }
    }

    private final String trailToString(List<String> _trail, String _sep) {
        final StringBuffer b = new StringBuffer();
        if (_trail != null) {
            for (int i = 0; i < _trail.size(); i++) {
                if (i > 0)
                    b.append(_sep);
                b.append(_trail.get(i));
            }
        }
        return b.toString();
    }
}
