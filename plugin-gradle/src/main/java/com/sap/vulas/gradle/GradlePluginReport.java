package com.sap.vulas.gradle;

import static com.sap.vulas.gradle.GradleProjectUtilities.*;
import static com.sap.vulas.gradle.VulasPluginCommon.*;

import com.sap.psr.vulas.goals.ReportException;
import com.sap.psr.vulas.goals.ReportGoal;
import com.sap.psr.vulas.shared.json.model.Application;
import java.util.HashSet;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskExecutionException;

public class GradlePluginReport extends AbstractVulasTask {
  @Override
  protected void createGoal() {
    this.goal = new ReportGoal();
  }

  @Override
  protected void executeGoal() throws Exception {

    final Set<Application> modules = new HashSet<>();
    final Set<Project> subProjects = project.getSubprojects();

    // if the plugin applied to an 'aggregator' project, then the project itself
    // is not producing any report
    if (this.projectOutputType != null) {
      modules.add(this.app);
    }

    // if the project has subprojects examine them
    if (!subProjects.isEmpty()) {
      getLogger().debug("Adding subprojects if eligible for reporting");
      for (Project sp : subProjects) {

        if (!sp.getPlugins().hasPlugin(VulasPlugin.class)) {
          getLogger()
              .debug("Vulas plugin not applied on subproject {} , skipping it.", sp.getName());
          continue;
        }

        if (!hasKnownProjectOutputType(sp, getLogger())) {
          getLogger().debug("Output type of subproject {} is unknown, skipping it.", sp.getName());
          continue;
        }

        String groupId = getMandatoryProjectProperty(sp, GradleGavProperty.group, getLogger());
        String artifactId = getMandatoryProjectProperty(sp, GradleGavProperty.name, getLogger());
        String version = getMandatoryProjectProperty(sp, GradleGavProperty.version, getLogger());
        Application subProjectMvnId = new Application(groupId, artifactId, version);
        modules.add(subProjectMvnId);
      }
    }

    if (!modules.isEmpty()) {

      ((ReportGoal) this.goal).setApplicationModules(modules);

      try {
        this.goal.executeSync();
      }
      // ReportException will be passed on as MojoFailure, i.e., the goal execution terminates
      // normally
      catch (ReportException re) {
        getLogger().error(re.getLongMessage());
        throw new TaskExecutionException(this, re);
      }
    } else {
      getLogger()
          .quiet(
              "Skipping report generation as neither the project or none of its subprojects are eligible.");
    }
  }
}
