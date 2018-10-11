package com.sap.vulas.gradle;

public class VulasPluginCommon {

    public enum VulasTasks {
        vulasClean, vulasApp, vulasA2C, vulasReport
    }

    public static final String VULAS_PLUGIN_NAME = "vulas";

    protected enum ProjectOutputTypes { JAR, AAR, APK }

    public enum GradleGavProperty { group, name, version }
}
