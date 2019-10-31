package com.sap.psr.vulas.goals;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.malice.MaliciousnessAnalysisResult;
import com.sap.psr.vulas.malice.MaliciousnessAnalyzerLoop;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.tasks.BomTask;
import java.nio.file.Paths;
import java.util.ServiceLoader;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** BomGoal class. */
public class BomGoal extends AbstractAppGoal {

  private static final Log log = LogFactory.getLog(BomGoal.class);

  /** Constructor for BomGoal. */
  public BomGoal() {
    super(GoalType.APP);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Evaluates the configuration setting {@link CoreConfiguration#APP_PREFIXES}.
   */
  @Override
  protected void prepareExecution() throws GoalConfigurationException {
    super.prepareExecution();
  }

  /** {@inheritDoc} */
  @Override
  protected void executeTasks() throws Exception {

    // The application to be completed
    Application a = this.getApplicationContext();

    // Create, configure and execute tasks
    final ServiceLoader<BomTask> loader = ServiceLoader.load(BomTask.class);
    for (BomTask t : loader) {
      try {
        // Configure
        t.setApplication(a);
        t.setSearchPaths(this.getAppPaths());
        t.setGoalClient(this.getGoalClient());
        t.setKnownDependencies(this.getKnownDependencies());
        t.configure(this.getConfiguration());

        // Execute
        t.execute();
        t.cleanUp();
        a = t.getCompletedApplication();
      } catch (Exception e) {
        log.error("Error running task " + t + ": " + e.getMessage(), e);
      }
    }

    final MaliciousnessAnalyzerLoop loop = new MaliciousnessAnalyzerLoop();

    // Get a clean set of dependencies
    // The removal of dependencies on the same digest is not done here any longer to be able to set
    // parents on the dependencies that will not be removed
    // Once we add the usage of relativePath to distinguish among dependencies, the removal can be
    // done at this step again.
    //	final Set<Dependency> no_dupl_deps =
    // DependencyUtil.removeDuplicateLibraryDependencies(a.getDependencies());
    //	a.setDependencies(no_dupl_deps);

    // Upload libraries and binaries (if requested)
    if (a.getDependencies() != null) {
      for (Dependency dep : a.getDependencies()) {

        // Check maliciousness
        if (dep.getPath() != null && Paths.get(dep.getPath()).toFile().canRead()) {
          final Set<MaliciousnessAnalysisResult> results =
              loop.isMalicious(Paths.get(dep.getPath()).toFile());
        }

        // Upload lib
        final Library lib = dep.getLib();
        if (lib != null) {
          if (lib.hasValidDigest()) {
            BackendConnector.getInstance().uploadLibrary(this.getGoalContext(), lib);
            if (CoreConfiguration.isJarUploadEnabled(this.getGoalContext().getVulasConfiguration()))
              BackendConnector.getInstance()
                  .uploadLibraryFile(lib.getDigest(), Paths.get(dep.getPath()));
          } else {
            log.error("Library of dependency [" + dep + "] has no valid digest");
          }
        } else {
          log.error("Dependency [" + dep + "] has no library");
        }
      }
    }

    final boolean upload_empty =
        this.getConfiguration()
            .getConfiguration()
            .getBoolean(CoreConfiguration.APP_UPLOAD_EMPTY, false);
    final boolean app_exists_in_backend =
        BackendConnector.getInstance().isAppExisting(this.getGoalContext(), a);

    // Upload if non-empty or already exists in backend or empty ones shall be uploaded
    if (!a.isEmpty() || app_exists_in_backend || upload_empty) {
      log.info(
          "Save app "
              + a
              + " with ["
              + a.getDependencies().size()
              + "] dependencies and ["
              + a.getConstructs().size()
              + "] constructs (uploadEmpty="
              + upload_empty
              + ")");
      BackendConnector.getInstance().uploadApp(this.getGoalContext(), a);
    } else {
      log.warn(
          "Skip save of empty app "
              + this.getApplicationContext()
              + " (uploadEmpty="
              + upload_empty
              + ", existsInBackend="
              + app_exists_in_backend
              + ")");
      this.skipGoalUpload();
    }
  }
}
