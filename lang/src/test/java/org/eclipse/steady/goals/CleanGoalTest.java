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
package org.eclipse.steady.goals;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.composite;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.put;
import static com.xebialabs.restito.semantics.Condition.uri;

import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.connectivity.PathBuilder;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.model.Application;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;

public class CleanGoalTest extends AbstractGoalTest {

  /**
   * Same as in {@link BomGoalTest}.
   */
  private void setupMockServices(Application _a) {
    final String s_json = JacksonUtil.asJsonString(_a);

    // Options app: 200
    whenHttp(server)
        .match(composite(method(Method.OPTIONS), uri("/backend" + PathBuilder.app(_a))))
        .then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));

    // Put app: 201
    whenHttp(server)
        .match(put("/backend" + PathBuilder.app(_a)))
        .then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.CREATED_201));

    // Post app: 200 (for clean goal)
    whenHttp(server)
        .match(post("/backend" + PathBuilder.app(_a)))
        .then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));

    //		expect()
    //			.statusCode(201).
    //			when()
    //			.post("/backend" + PathBuilder.apps());

    // Options goal exe: 404 (default, no need to implement, results in 4 goal execution uploads in
    // total)

    // Post goal exe: 201
    whenHttp(server)
        .match(post("/backend" + PathBuilder.goalExcecutions(null, null, _a)))
        .then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.CREATED_201));

    //		expect()
    //			.statusCode(201).
    //			when()
    //			.post("/backend" + PathBuilder.goalExcecutions(null, null, _a));
  }

  /**
   * Create empty app, clean and upload again
   */
  @Test
  public void testClean() throws GoalConfigurationException, GoalExecutionException {
    // Mock REST services
    this.configureBackendServiceUrl(server);
    this.setupMockServices(this.testApp);

    // Set config
    this.vulasConfiguration.setProperty(CoreConfiguration.TENANT_TOKEN, "foo");
    this.vulasConfiguration.setProperty(
        CoreConfiguration.APP_CTX_GROUP, this.testApp.getMvnGroup());
    this.vulasConfiguration.setProperty(
        CoreConfiguration.APP_CTX_ARTIF, this.testApp.getArtifact());
    this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_VERSI, this.testApp.getVersion());
    this.vulasConfiguration.setProperty(CoreConfiguration.APP_UPLOAD_EMPTY, new Boolean(true));

    // APP
    AbstractGoal goal = GoalFactory.create(GoalType.APP, GoalClient.CLI);
    goal.setConfiguration(this.vulasConfiguration).executeSync();

    // CLEAN
    goal = GoalFactory.create(GoalType.CLEAN, GoalClient.CLI);
    goal.setConfiguration(this.vulasConfiguration).executeSync();

    // Check the HTTP calls made (1 app PUT, 1 app POST, 4 goal exe POST)
    verifyHttp(server)
        .times(1, method(Method.PUT), uri("/backend" + PathBuilder.app(this.testApp)));
    verifyHttp(server)
        .times(1, method(Method.POST), uri("/backend" + PathBuilder.app(this.testApp)));
    verifyHttp(server)
        .times(
            4,
            method(Method.POST),
            uri("/backend" + PathBuilder.goalExcecutions(null, null, this.testApp)));
  }
}
