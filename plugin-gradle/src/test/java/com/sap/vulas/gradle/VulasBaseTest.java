package com.sap.vulas.gradle;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public abstract class VulasBaseTest {

    protected static List<File> pluginClasspath;

    protected BuildResult buildResult;

    private static final String MINIMUM_GRADLE_VERSION="4.4";

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    @BeforeClass
    public static void computeClassPath() throws IOException {
        GradleRunner gr = GradleRunner.create().withPluginClasspath();
        pluginClasspath = (List<File>) gr.getPluginClasspath();

        List<String> classpathLines = Files.readAllLines(Paths.get("target/test.classpath"), Charset.defaultCharset());

        for (String classpathElement:classpathLines.get(0).split(File.pathSeparator)) {
            pluginClasspath.add(new File(classpathElement));
        }
    }

    @Before
    public void baseSetUp() throws IOException, URISyntaxException {
        GradleTestProject gsp = this.getClass().getAnnotation(GradleTestProject.class);
        assert gsp != null : "Annotate test class with " + GradleTestProject.class.getName();

        String projectName = gsp.name();

        URL testProject = VulasBaseTest.class.getClassLoader().getResource(projectName);
        assert testProject != null : String.format("Test project '%s' has not been found", projectName);
        FileUtils.copyDirectory(new File(testProject.toURI()), testProjectDir.getRoot());
    }

    protected void executeBuild(String... arguments) {
        buildResult = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments(arguments)
                .withPluginClasspath(pluginClasspath)
                .withDebug(true)
                .withGradleVersion(MINIMUM_GRADLE_VERSION)
                .build();
    }

    @After
    public void baseTearDown() {
        if (buildResult != null) {
            System.out.println(buildResult.getOutput());
        }
    }

}
