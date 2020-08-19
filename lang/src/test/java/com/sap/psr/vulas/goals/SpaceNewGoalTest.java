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
package com.sap.psr.vulas.goals;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.junit.Assert.assertEquals;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.connectivity.PathBuilder;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Space;

public class SpaceNewGoalTest extends AbstractGoalTest {

  /**
   * Space creation results in the following two HTTP calls.
   * @param _s
   */
  private void setupMockSpaceServices(Space _s) {
    final String s_json = JacksonUtil.asJsonString(_s);
    whenHttp(server)
        .match(post("/backend" + PathBuilder.spaces()))
        .then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.CREATED_201));

    //		expect()
    //			.statusCode(201).
    //			and().
    //			body("spaceName", equalTo(_s.getSpaceName())).
    //			when()
    //			.post("/backend/spaces");

    whenHttp(server)
        .match(post("/backend" + PathBuilder.goalExcecutions(null, _s, null)))
        .then(
            stringContent(s_json),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.CREATED_201));

    //		expect()
    //		.statusCode(201).
    //		when()
    //		.post("/backend" + PathBuilder.goalExcecutions(null, _s, null));
  }

  /**
   * 0 HTTP requets, as no space name and description are provided.
   *
   * @throws GoalConfigurationException
   * @throws GoalExecutionException
   */
  @Test(expected = GoalExecutionException.class)
  public void testNewSpaceWithoutTenantConfiguration()
      throws GoalConfigurationException, GoalExecutionException {
    final AbstractGoal goal = GoalFactory.create(GoalType.SPACENEW, GoalClient.CLI);
    goal.setConfiguration(this.vulasConfiguration).executeSync();
  }

  /**
   * 0 HTTP requets, as no backend service URL configured.
   *
   * @throws GoalConfigurationException
   * @throws GoalExecutionException
   */
  @Test(expected = GoalExecutionException.class)
  public void testNewSpaceWithoutBackendConfiguration()
      throws GoalConfigurationException, GoalExecutionException {
    this.vulasConfiguration.setProperty(
        CoreConfiguration.TENANT_TOKEN, testTenant.getTenantToken());
    final AbstractGoal goal = GoalFactory.create(GoalType.SPACENEW, GoalClient.CLI);
    goal.setConfiguration(this.vulasConfiguration).executeSync();
  }

  /**
   * 2 HTTP requests, for space creation and goal execution creation.
   *
   * @throws GoalConfigurationException
   * @throws GoalExecutionException
   */
  @Test
  public void testNewSpace() throws GoalConfigurationException, GoalExecutionException {
    // Mock REST services
    this.configureBackendServiceUrl(server);
    this.setupMockSpaceServices(testSpace);

    // Set config
    this.vulasConfiguration.setProperty(
        CoreConfiguration.TENANT_TOKEN, testTenant.getTenantToken());
    this.vulasConfiguration.setProperty(CoreConfiguration.SPACE_NAME, testSpace.getSpaceName());
    this.vulasConfiguration.setProperty(
        CoreConfiguration.SPACE_DESCR, testSpace.getSpaceDescription());

    // Execute goal
    final AbstractGoal goal = GoalFactory.create(GoalType.SPACENEW, GoalClient.CLI);
    goal.setConfiguration(this.vulasConfiguration).executeSync();

    final Space new_space = (Space) goal.getResultObject();
    assertEquals(testSpace, new_space);

    // Check the HTTP calls made
    verifyHttp(server).times(1, method(Method.POST), uri("/backend" + PathBuilder.spaces()));
    verifyHttp(server)
        .times(
            0,
            method(Method.POST),
            uri("/backend" + PathBuilder.goalExcecutions(null, testSpace, null)));
  }
}
