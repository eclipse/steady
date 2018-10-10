import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import junit.framework.TestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VulasMavenPluginTests extends TestCase {

    static {
        VulasConfiguration.getGlobal().setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE.toString());
    }


    public void testMavenPlugin()
            throws Exception {

        // set the maven project to test
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/testproject");

        Verifier verifier;

        // remove artifacts created by this test from the m2 repository
        verifier = new Verifier(testDir.getAbsolutePath());
        verifier.deleteArtifact("foo.bar", "sampletest", "1.0.0", "pom");


        // execute the goals

        List cliOptions = new ArrayList();
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
        verifier.assertFilePresent("target/vulas/upload");

        verifier.verifyErrorFreeLog();

        // reset the stream b
        verifier.resetStreams();


    }
}
