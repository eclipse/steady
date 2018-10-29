package com.sap.psr.vulas.mvn;


import com.sap.psr.vulas.mvn.VulasAgentMojo;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.MojoRule;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for the AgentOptions standalone
 */
public class VulasAgentOptionsTests {
    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    public void testCreateCommandLineArgs() throws Exception {

        TestProjectStub stub = new TestProjectStub("/target/test-classes/unitTestPom/", "pom.xml");

        VulasAgentMojo myMojo = (VulasAgentMojo) rule.lookupConfiguredMojo(stub, "prepare-vulas-agent");
        assertNotNull(myMojo);


        VulasAgentMojo.VulasAgentOptions vulasAgentOptions = myMojo.new VulasAgentOptions();

        assertNotNull(vulasAgentOptions);
        String finalArg = vulasAgentOptions.prependVMArguments("", new File("myaggent"));
        System.out.println(finalArg);
        assertTrue(finalArg.contains("-javaagent:myaggent"));
        assertTrue(finalArg.contains("-noverify"));

    }

    @Test
    public void testPrependCommandLineArgs() throws Exception {

        TestProjectStub stub = new TestProjectStub("/target/test-classes/unitTestPom/", "pom2.xml");

        VulasAgentMojo myMojo = (VulasAgentMojo) rule.lookupConfiguredMojo(stub, "prepare-vulas-agent");
        assertNotNull(myMojo);


        VulasAgentMojo.VulasAgentOptions vulasAgentOptions = myMojo.new VulasAgentOptions();

        assertNotNull(vulasAgentOptions);
        String finalArg = vulasAgentOptions.prependVMArguments("-Djava.security.manager -Djava.security.policy=${basedir}/src/test/resources/java.policy", new File("myaggent"));
        System.out.println(finalArg);
        assertTrue(finalArg.contains("-javaagent:myaggent"));
        assertTrue(finalArg.contains("-noverify"));
        assertTrue(finalArg.contains("-Djava.security.manager"));
        assertTrue(finalArg.contains("-Djava.security.policy=${basedir}/src/test/resources/java.policy"));

    }


    @Test
    public void testRemoveDuplicateAgentCommandLineArgs() throws Exception {

        TestProjectStub stub = new TestProjectStub("/target/test-classes/unitTestPom/", "pom2.xml");

        VulasAgentMojo myMojo = (VulasAgentMojo) rule.lookupConfiguredMojo(stub, "prepare-vulas-agent");
        assertNotNull(myMojo);

        VulasAgentMojo.VulasAgentOptions vulasAgentOptions = myMojo.new VulasAgentOptions();
        assertNotNull(vulasAgentOptions);

        String finalArg = vulasAgentOptions.prependVMArguments("-javaagent:myaggent", new File("myaggent"));

        assertNotNull(finalArg);

        assertTrue(finalArg.contains("-javaagent:myaggent"));
        assertTrue(finalArg.contains("-noverify"));


    }

    @Test
    public void testRemoveOriginalAgentCommandLineArgs() throws Exception {

        TestProjectStub stub = new TestProjectStub("/target/test-classes/unitTestPom/", "pom2.xml");

        VulasAgentMojo myMojo = (VulasAgentMojo) rule.lookupConfiguredMojo(stub, "prepare-vulas-agent");
        assertNotNull(myMojo);

        VulasAgentMojo.VulasAgentOptions vulasAgentOptions = myMojo.new VulasAgentOptions();
        assertNotNull(vulasAgentOptions);

        String finalArg = vulasAgentOptions.prependVMArguments("-javaagent:/mnt/folder/myproject/vulas/lib/vulas-core-latest-jar-with-dependencies.jar -DfooProp=bar", new File("myaggent"));

        assertNotNull(finalArg);

        assertTrue(finalArg.contains("-javaagent:myaggent"));
        assertTrue(!finalArg.contains("/vulas/lib/vulas-core-latest-jar-with-dependencies.jar"));

        assertTrue(finalArg.contains("-noverify"));


    }




}
