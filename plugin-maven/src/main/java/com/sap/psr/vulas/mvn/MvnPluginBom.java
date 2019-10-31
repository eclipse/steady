package com.sap.psr.vulas.mvn;

import com.sap.psr.vulas.goals.BomGoal;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * This Mojo identifies the constructs belonging to the application itself and belonging to all its
 * dependencies. Those are then uploaded to the central Vulas engine for further analysis (test
 * coverage, vulnerability assessments, archive integrity).
 */
@Mojo(
    name = "app",
    defaultPhase = LifecyclePhase.TEST,
    requiresDependencyResolution = ResolutionScope.TEST,
    requiresOnline = true)
public class MvnPluginBom extends AbstractVulasMojo {

  /** {@inheritDoc} */
  @Override
  protected void createGoal() {
    this.goal = new BomGoal();
  }
}
