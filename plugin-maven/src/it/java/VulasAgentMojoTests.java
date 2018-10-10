import com.sap.psr.vulas.mvn.TestProjectStub;
import com.sap.psr.vulas.mvn.VulasAgentMojo;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test interplay AgentOptions and AgentMojo
 */
public class VulasAgentMojoTests {

    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    public void testCreateCommandLineArgs() throws Exception {

        TestProjectStub stub = new TestProjectStub("/target/test-classes/unitTestPom/", "pom.xml");

        VulasAgentMojo myMojo = (VulasAgentMojo) rule.lookupConfiguredMojo(stub, "prepare-vulas-agent");
        assertNotNull(myMojo);

        myMojo.execute();
        String finalArg = stub.getProperties().getProperty("argLine");

        assertNotNull(finalArg);
        assertTrue(finalArg.contains("-javaagent:myaggent"));
        assertTrue(finalArg.contains("-noverify"));

    }


    @Test
    public void testPrependCommandLineArgs() throws Exception {

        TestProjectStub stub = new TestProjectStub("/target/test-classes/unitTestPom/", "pom2.xml");

        VulasAgentMojo myMojo = (VulasAgentMojo) rule.lookupConfiguredMojo(stub, "prepare-vulas-agent");
        assertNotNull(myMojo);


        myMojo.execute();

        String finalArg = stub.getProperties().getProperty("argLine");

        assertNotNull(finalArg);
        assertTrue(finalArg.contains("-javaagent:myaggent"));
        assertTrue(finalArg.contains("-noverify"));
        assertTrue(finalArg.contains("-Djava.security.manager"));
        assertTrue(finalArg.contains("-Djava.security.policy=${basedir}/src/test/resources/java.policy"));

    }
}
