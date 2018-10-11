package com.sap.vulas.gradle;

import org.junit.Test;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@GradleTestProject(name = "java-lib")
public class JavaLibTest extends VulasBaseTest {

    @Test
    public void vulasAppTest() {
        executeBuild("vulasApp", "--stacktrace", "--info");
        assertEquals(SUCCESS, buildResult.task(":vulasApp").getOutcome());
        assertTrue("Dependency/construct count mismatch", buildResult.getOutput().matches("(?s).*Save app \\[com.sap.security.vulas.gradle.plugin.test:java-lib:1.0.0\\] with \\[6\\] dependencies and \\[2\\] constructs .*"));
    }
}
