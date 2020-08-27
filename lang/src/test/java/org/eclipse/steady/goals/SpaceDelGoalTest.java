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

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.composite;
import static com.xebialabs.restito.semantics.Condition.delete;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.uri;

import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.AbstractGoal;
import org.eclipse.steady.goals.GoalConfigurationException;
import org.eclipse.steady.goals.GoalExecutionException;
import org.eclipse.steady.goals.GoalFactory;
import org.eclipse.steady.shared.connectivity.PathBuilder;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.model.Space;
import org.eclipse.steady.shared.json.model.Tenant;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;

/**
 * https://zeroturnaround.com/rebellabs/the-correct-way-to-use-integration-tests-in-your-build-process/
 *
 */
public class SpaceDelGoalTest extends AbstractGoalTest {

  private void setupMockServices(Tenant _t, Space _s) {
    final String s_json = JacksonUtil.asJsonString(_s);

    // Options space
    whenHttp(server)
        .match(composite(method(Method.OPTIONS), uri("/backend" + PathBuilder.space(_s))))
        . // , withHeader(Constants.HTTP_TENANT_HEADER, _t.getTenantToken())
        then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));

    expect().statusCode(200).when().options("/backend" + PathBuilder.space(_s));

    // Delete space
    whenHttp(server)
        .match(delete("/backend" + PathBuilder.space(_s)))
        .then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));

    expect().statusCode(200).when().delete("/backend" + PathBuilder.space(_s));

    // Post goal exe
    whenHttp(server)
        .match(post("/backend" + PathBuilder.goalExcecutions(null, _s, null)))
        .then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));

    expect().statusCode(200).when().post("/backend" + PathBuilder.goalExcecutions(null, _s, null));
  }

  @Test
  public void testDelSpace() throws GoalConfigurationException, GoalExecutionException {
    // Mock REST services
    this.configureBackendServiceUrl(server);
    this.setupMockServices(this.testTenant, this.testSpace);

    this.vulasConfiguration.setProperty(
        CoreConfiguration.TENANT_TOKEN, testTenant.getTenantToken());
    this.vulasConfiguration.setProperty(CoreConfiguration.SPACE_TOKEN, testSpace.getSpaceToken());

    final AbstractGoal goal = GoalFactory.create(GoalType.SPACEDEL, GoalClient.CLI);
    goal.setConfiguration(this.vulasConfiguration).executeSync();
  }
}
