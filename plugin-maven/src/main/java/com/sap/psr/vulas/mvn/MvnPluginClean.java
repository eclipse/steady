package com.sap.psr.vulas.mvn;

import com.sap.psr.vulas.goals.CleanGoal;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/** MvnPluginClean class. */
@Mojo(name = "clean", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresOnline = true)
public class MvnPluginClean extends AbstractVulasMojo {

  /** {@inheritDoc} */
  @Override
  protected void createGoal() {
    this.goal = new CleanGoal();
  }
}
