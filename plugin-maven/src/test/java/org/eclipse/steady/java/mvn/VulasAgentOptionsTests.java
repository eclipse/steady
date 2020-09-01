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
package org.eclipse.steady.java.mvn;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for the AgentOptions standalone
 */
public class VulasAgentOptionsTests {
  @Rule public MojoRule rule = new MojoRule();

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
    String finalArg =
        vulasAgentOptions.prependVMArguments(
            "-Djava.security.manager"
                + " -Djava.security.policy=${basedir}/src/test/resources/java.policy",
            new File("myaggent"));
    System.out.println(finalArg);
    assertTrue(finalArg.contains("-javaagent:myaggent"));
    assertTrue(finalArg.contains("-noverify"));
    assertTrue(finalArg.contains("-Djava.security.manager"));
    assertTrue(
        finalArg.contains("-Djava.security.policy=${basedir}/src/test/resources/java.policy"));
  }

  @Test
  public void testRemoveDuplicateAgentCommandLineArgs() throws Exception {

    TestProjectStub stub = new TestProjectStub("/target/test-classes/unitTestPom/", "pom2.xml");

    VulasAgentMojo myMojo = (VulasAgentMojo) rule.lookupConfiguredMojo(stub, "prepare-vulas-agent");
    assertNotNull(myMojo);

    VulasAgentMojo.VulasAgentOptions vulasAgentOptions = myMojo.new VulasAgentOptions();
    assertNotNull(vulasAgentOptions);

    String finalArg =
        vulasAgentOptions.prependVMArguments("-javaagent:myaggent", new File("myaggent"));

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

    String finalArg =
        vulasAgentOptions.prependVMArguments(
            "-javaagent:/mnt/c/Users/myuser/testproject/vulas/lib/vulas-core-latest-jar-with-dependencies.jar"
                + " -DfooProp=bar",
            new File("myaggent"));

    assertNotNull(finalArg);

    assertTrue(finalArg.contains("-javaagent:myaggent"));
    assertTrue(!finalArg.contains("/vulas/lib/vulas-core-latest-jar-with-dependencies.jar"));

    assertTrue(finalArg.contains("-noverify"));
  }
}
