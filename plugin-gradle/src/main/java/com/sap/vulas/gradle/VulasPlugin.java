package com.sap.vulas.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static com.sap.vulas.gradle.VulasPluginCommon.*;

public class VulasPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        target.getTasks().create(VulasTasks.vulasClean.name(), GradlePluginClean.class);
        target.getTasks().create(VulasTasks.vulasApp.name(), GradlePluginApp.class);
        target.getTasks().create(VulasTasks.vulasA2C.name(), GradlePluginA2C.class);
        target.getTasks().create(VulasTasks.vulasReport.name(), GradlePluginReport.class);
    }
}