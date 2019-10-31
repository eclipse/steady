package com.sap.vulas.gradle;

import com.sap.psr.vulas.goals.CleanGoal;

public class GradlePluginClean extends AbstractVulasTask {

  @Override
  protected void createGoal() {
    this.goal = new CleanGoal();
  }
}
