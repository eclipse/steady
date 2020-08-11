/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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


    public Verifier testPlugin(String pomFileName)
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

        //check if vulas has been executed
        verifier.assertFilePresent("target/vulas/tmp");

        verifier.verifyErrorFreeLog();

        return verifier;


    }

    @Test
    public void prepareGoalTest() throws Exception {
        Verifier verifier = testPlugin("pom.xml");
        // check prepare-vulas-agent has been executed
        verifier.verifyTextInLog("prepare-vulas-agent");
        // check if jacoco has been executed
        verifier.assertFilePresent("target/jacoco.exec");

    }


    @Test
    public void backwardsCompabilityTest() throws Exception {
        String pomFileName = "backwardComppom.xml";
        Path pomFilePath = Paths.get("target", "test-classes", "testproject", pomFileName);
        // Since environment variables and system properties are not passed to the forked vm
        // write the backendURL directly into the pom file
        String content = new String(Files.readAllBytes(pomFilePath), Charset.defaultCharset());
        content = content.replaceAll("REPLACE_WITH_BACKENDURL", stubServer.getBackendURL());
        Files.write(pomFilePath, content.getBytes(Charset.defaultCharset()));

        Verifier verifier = testPlugin(pomFileName);
        verifier.verifyTextInLog("/vulas/lib/vulas-core-latest-jar-with-dependencies.jar");
    }


    @Test
    public void mixedConfigurationTest() throws Exception {
        String pomFileName = "mixedpom.xml";
        Path pomFilePath = Paths.get("target", "test-classes", "testproject", pomFileName);
        // Since environment variables and system properties are not passed to the forked vm
        // write the backendURL directly into the pom file
        String content = new String(Files.readAllBytes(pomFilePath), Charset.defaultCharset());
        content = content.replaceAll("REPLACE_WITH_BACKENDURL", stubServer.getBackendURL());
        Files.write(pomFilePath, content.getBytes(Charset.defaultCharset()));

        Verifier verifier = testPlugin(pomFileName);
        // in the mixed setting: the manual javaagent is always executed
        // all argLine arguments set by any maven plugin are ignored see https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html
        verifier.verifyTextInLog("/vulas/lib/vulas-core-latest-jar-with-dependencies.jar");
    }


}
