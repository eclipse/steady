package com.sap.vulas.gradle;

import com.sap.psr.vulas.cg.A2CGoal;

public class GradlePluginA2C extends AbstractVulasTask{

    @Override
    protected void createGoal() {
        this.goal = new A2CGoal();
    }
}
