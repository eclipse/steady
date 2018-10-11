package com.sap.vulas.gradle;

import org.junit.Test;
import org.junit.Ignore;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@GradleTestProject(name = "android-libs")
public class AndroidLibsTest extends VulasBaseTest {

    @Ignore("needs investigation how to make Android SDK available for the test")
    @Test
    public void vulasAppTest() {
        String taskName = "vulasApp";

        executeBuild(taskName, "--stacktrace", "--info");

        assertEquals(SUCCESS, buildResult.task(":android-lib1:" + taskName).getOutcome());
        assertEquals(SUCCESS, buildResult.task(":android-lib2:" + taskName).getOutcome());

        assertTrue("Dependency/construct count mismatch", buildResult.getOutput().matches("(?s).*Save app \\[com.sap.security.vulas.gradle.plugin.test:android-lib1:1.0.0\\] with \\[2\\] dependencies and \\[3\\] constructs .*"));
        assertTrue("Dependency/construct count mismatch", buildResult.getOutput().matches("(?s).*Save app \\[com.sap.security.vulas.gradle.plugin.test:android-lib2:1.0.0\\] with \\[5\\] dependencies and \\[3\\] constructs .*"));


    }
}
