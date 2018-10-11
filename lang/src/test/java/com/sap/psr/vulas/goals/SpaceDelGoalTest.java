package com.sap.psr.vulas.goals;

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
import static com.xebialabs.restito.semantics.Condition.withHeader;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.connectivity.PathBuilder;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Space;
import com.sap.psr.vulas.shared.json.model.Tenant;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * https://zeroturnaround.com/rebellabs/the-correct-way-to-use-integration-tests-in-your-build-process/
 *
 */
public class SpaceDelGoalTest extends AbstractGoalTest {
	
	private void setupMockServices(Tenant _t, Space _s) {
		final String s_json = JacksonUtil.asJsonString(_s);
		
		// Options space
		whenHttp(server).
		match(composite(method(Method.OPTIONS), uri("/backend" + PathBuilder.space(_s)))). //, withHeader(Constants.HTTP_TENANT_HEADER, _t.getTenantToken())
		then(
				stringContent(s_json),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.OK_200));
		
		expect()
			.statusCode(200).
			when()
			.options("/backend" + PathBuilder.space(_s));
		
		// Delete space
		whenHttp(server).
		match(delete("/backend" + PathBuilder.space(_s))).
		then(
				stringContent(s_json),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.OK_200));
		
		expect()
			.statusCode(200).
			when()
			.delete("/backend" + PathBuilder.space(_s));

		// Post goal exe
		whenHttp(server).
		match(post("/backend" + PathBuilder.goalExcecutions(null, _s, null))).
		then(
				stringContent(s_json),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.OK_200));
		
		expect()
			.statusCode(200).
			when()
			.post("/backend" + PathBuilder.goalExcecutions(null, _s, null));
	}

	/**
	 * Two HTTP requests made for space creation and goal execution creation
	 * 
	 * @throws GoalConfigurationException
	 * @throws GoalExecutionException
	 */
	@Test
	public void testDelSpace() throws GoalConfigurationException, GoalExecutionException {
		// Mock REST services
		this.configureBackendServiceUrl(server);
		this.setupMockServices(this.testTenant, this.testSpace);
				
		final VulasConfiguration cfg = new VulasConfiguration();
		cfg.setProperty(CoreConfiguration.TENANT_TOKEN, testTenant.getTenantToken());
		cfg.setProperty(CoreConfiguration.SPACE_TOKEN, testSpace.getSpaceToken());
		
		final AbstractGoal goal = GoalFactory.create(GoalType.SPACEDEL, GoalClient.CLI);
		goal.setConfiguration(cfg).executeSync();
	}
}
