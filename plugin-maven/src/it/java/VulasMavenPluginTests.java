import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.connectivity.PathBuilder;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import junit.framework.TestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.*;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.post;

public class VulasMavenPluginTests {


    private static StubServerSetup stubServer;

    @BeforeClass
    public static void startServer() {
        // setup the stubserver to simulate communication with vulas's backend
        stubServer = new StubServerSetup("foo.bar", "sampletest", "1.0.0");
        stubServer.configureBackendServiceUrl(stubServer.server);
        stubServer.setupMockServices(stubServer.testApp);
    }


    @AfterClass
    public static void stopServer() {
        // stop the stubserver
        stubServer.stop();
    }


    public Verifier testPlugin(String pomFileName, boolean checkJacoco)
            throws Exception {

        // set the maven project to test
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/testproject");

        Verifier verifier = new Verifier(testDir.getAbsolutePath());

        // remove artifacts created by this test from the m2 repository
        verifier.deleteArtifact("foo.bar", "sampletest", "1.0.0", "pom");

        // execute the goals
        List cliOptions = new ArrayList();
        // pass the backendURL to the mvn invoke command
        Properties properties = new Properties();
        properties.setProperty(VulasConfiguration.getServiceUrlKey(Service.BACKEND), stubServer.getBackendURL());
        verifier.setSystemProperties(properties);

        // do not recurse into sub-projects
        cliOptions.add("-N");
        cliOptions.add("-f=" + pomFileName);
        verifier.setCliOptions(cliOptions);
        List goals = new ArrayList();
        goals.add("clean");
        goals.add("compile");
        goals.add("vulas:app");
        goals.add("test");

        verifier.executeGoals(goals);
        if (checkJacoco) {
            // check if jacoco has been executed
            verifier.assertFilePresent("target/jacoco.exec");
        }
        //check if vulas has been executed
        verifier.assertFilePresent("target/vulas/tmp");

        verifier.verifyErrorFreeLog();

        return verifier;


    }

    @Test
    public void prepareGoalTest() throws Exception {
        Verifier verifier = testPlugin("pom.xml", true);
        //check prepare-vulas-agent has been executed
        verifier.verifyTextInLog("prepare-vulas-agent");

    }


    @Test
    public void backwardsCompabilityTest() throws Exception {
        String pomFileName = "backwardComppom.xml";
        Path pomFilePath = Paths.get("target", "test-classes", "testproject", pomFileName);
        // Since environment variables and system properties are not passed to the forked vm,
        // write backendURL into the pom file directly
        String content = new String(Files.readAllBytes(pomFilePath), Charset.defaultCharset());
        content = content.replaceAll("REPLACE_WITH_BACKENDURL", stubServer.getBackendURL());
        Files.write(pomFilePath, content.getBytes(Charset.defaultCharset()));


        Verifier verifier = testPlugin(pomFileName, false);
        verifier.verifyTextInLog("javaagent:");
        verifier.verifyTextInLog("/vulas/lib/vulas-core-latest-jar-with-dependencies.jar");
    }

    @Test
    public void mixedConfigurationTest() throws Exception {
        Verifier verifier = testPlugin("mixedpom.xml", false);
        // in the mixed setting: the prepare-vulas-goal is executed, and the original javaagent is ignored
        verifier.verifyTextInLog("prepare-vulas-agent");
        verifier.verifyTextInLog("javaagent:");

    }


}
