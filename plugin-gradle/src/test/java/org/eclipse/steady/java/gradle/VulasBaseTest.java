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
package org.eclipse.steady.java.gradle;

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
import java.nio.file.Paths;
import java.util.List;

public abstract class VulasBaseTest {

  protected static List<File> pluginClasspath = new java.util.ArrayList<File>();

  protected BuildResult buildResult;

  private static final String MINIMUM_GRADLE_VERSION = "5.0";

  @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();

  @BeforeClass
  public static void computeClassPath() throws IOException {
    GradleRunner gr = GradleRunner.create().withPluginClasspath();
    pluginClasspath.addAll((List<File>) gr.getPluginClasspath());

    List<String> classpathLines =
        Files.readAllLines(Paths.get("target/test.classpath"), Charset.defaultCharset());

    for (String classpathElement : classpathLines.get(0).split(File.pathSeparator)) {
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
    buildResult =
        GradleRunner.create()
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
