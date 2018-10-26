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
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.post;

public class VulasMavenPluginTests extends TestCase {


    private StubServerSetup stubServer;

    @Before
    public void startServer() {
        stubServer = new StubServerSetup("foo.bar", "sampletest", "1.0.0");
        stubServer.configureBackendServiceUrl(stubServer.server);
        stubServer.setupMockServices(stubServer.testApp);
    }


    @After
    public void stopServer() {
        stubServer.stop();
    }


    public void testMavenPlugin()
            throws Exception {
        // setup the stubserver to emulate communication with vulas's backend
        startServer();

        try {
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
            verifier.setCliOptions(cliOptions);
            List goals = new ArrayList();
            goals.add("clean");
            goals.add("compile");
            goals.add("vulas:app");
            goals.add("test");

            verifier.executeGoals(goals);

            // check if jacoco has been executed
            verifier.assertFilePresent("target/jacoco.exec");

            //check if vulas has been executed
            verifier.assertFilePresent("target/vulas/tmp");

            verifier.verifyErrorFreeLog();

            //check prepare-vulas-agent has been executed
            verifier.verifyTextInLog("prepare-vulas-agent");

            // reset the stream b
            verifier.resetStreams();
        } finally {
            // stop the stubserver
            stubServer.stop();
        }
    }
}
