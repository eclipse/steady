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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.cli;

import java.util.ArrayList;

import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.connectivity.Service;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Space;
import org.eclipse.steady.shared.json.model.Tenant;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.junit.After;
import org.junit.Before;

import com.jayway.restassured.RestAssured;
import com.xebialabs.restito.server.StubServer;

public class AbstractGoalTest {

  protected StubServer server;

  protected Tenant testTenant;
  protected Space testSpace;
  protected Application testApp;

  @Before
  public void start() {
    server = new StubServer().run();
    RestAssured.port = server.getPort();

    testTenant = this.buildTestTenant();
    testSpace = this.buildTestSpace();
    testApp = this.buildTestApplication();

    // Clear and set properties
    this.clearVulasProperties();

    // App context
    System.setProperty(CoreConfiguration.APP_CTX_GROUP, testApp.getMvnGroup());
    System.setProperty(CoreConfiguration.APP_CTX_ARTIF, testApp.getArtifact());
    System.setProperty(CoreConfiguration.APP_CTX_VERSI, testApp.getVersion());

    // Identify app code
    System.setProperty(
        CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.READ_WRITE.toString());

    // Identify app code
    System.setProperty(CoreConfiguration.APP_PREFIXES, "com.acme");
  }

  @After
  public void stop() {
    this.clearVulasProperties();
    server.stop();
  }

  private void clearVulasProperties() {
    final ArrayList<String> l = new ArrayList<String>();
    for (Object k : System.getProperties().keySet())
      if (k instanceof String && ((String) k).startsWith("vulas.")) l.add((String) k);
    for (String k : l) System.clearProperty(k);
  }

  protected Tenant buildTestTenant() {
    final String rnd = StringUtil.getRandonString(6);
    return new Tenant("tenant-token-" + rnd, "tenant-name-" + rnd);
  }

  protected Space buildTestSpace() {
    final String rnd = StringUtil.getRandonString(6);
    return new Space("space-token-" + rnd, "space-name-" + rnd, "space-description");
  }

  protected Application buildTestApplication() {
    final String rnd = StringUtil.getRandonString(6);
    return new Application("app-group-" + rnd, "app-artifact-" + rnd, "app-version");
  }

  protected void configureBackendServiceUrl(StubServer _ss) {
    final StringBuffer b = new StringBuffer();
    b.append("http://localhost:").append(_ss.getPort()).append("/backend");
    System.setProperty(VulasConfiguration.getServiceUrlKey(Service.BACKEND), b.toString());
  }
}
