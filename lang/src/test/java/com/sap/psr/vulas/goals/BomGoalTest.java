package com.sap.psr.vulas.goals;

import static com.jayway.restassured.RestAssured.expect;
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
import static org.hamcrest.Matchers.equalTo;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.connectivity.PathBuilder;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.VulasConfiguration;


public class BomGoalTest extends AbstractGoalTest {

	/**
	 * App creation results in the following two HTTP calls.
	 * @param _a TODO
	 */
	private void setupMockServices(Application _a) {
		final String s_json = JacksonUtil.asJsonString(_a);
		
		// Options app: 200
		whenHttp(server).
				match(composite(method(Method.OPTIONS), uri("/backend" + PathBuilder.app(_a)))).
			then(
				stringContent(s_json),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.OK_200));
		
		// Put app: 200
		whenHttp(server).
		match(put("/backend" + PathBuilder.apps())).
		then(
				stringContent(s_json),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.OK_200));
		
//		expect()
//			.statusCode(201).
//			when()
//			.post("/backend" + PathBuilder.apps());

		// Options goal exe: 404 (default, no impl needed)
		
		// Post goal exe: 201
		whenHttp(server).
		match(post("/backend" + PathBuilder.goalExcecutions(null, null, _a))).
		then(
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
	 * No HTTP requests shall be made, as the app is incomplete
	 * 
	 * @throws GoalConfigurationException
	 * @throws GoalExecutionException
	 */
	@Test(expected=GoalConfigurationException.class)
	public void testBomWithIncompleteAppConfiguration() throws GoalConfigurationException, GoalExecutionException {
		// Mock REST services
		this.configureBackendServiceUrl(server);
		this.setupMockServices(this.testApp);

		// Set config
		this.vulasConfiguration.setProperty(CoreConfiguration.TENANT_TOKEN, "foo");
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_GROUP, this.testApp.getMvnGroup());
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_ARTIF, this.testApp.getArtifact());
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_VERSI, null); // Will raise the exception

		// Execute goal
		final AbstractGoal goal = GoalFactory.create(GoalType.APP, GoalClient.CLI);
		goal.setConfiguration(this.vulasConfiguration).executeSync();
	}

	/**
	 * One app PUT will be made, as the app is said to exist in the backend.
	 * 
	 * @throws GoalConfigurationException
	 * @throws GoalExecutionException
	 */
	@Test
	public void testBomSkipSaveEmptyApp() throws GoalConfigurationException, GoalExecutionException {
		// Mock REST services
		this.configureBackendServiceUrl(server);
		this.setupMockServices(this.testApp);

		// Set config
		this.vulasConfiguration.setProperty(CoreConfiguration.TENANT_TOKEN, "foo");
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_GROUP, this.testApp.getMvnGroup());
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_ARTIF, this.testApp.getArtifact());
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_VERSI, this.testApp.getVersion());

		// Execute goal
		final AbstractGoal goal = GoalFactory.create(GoalType.APP, GoalClient.CLI);
		goal.setConfiguration(this.vulasConfiguration).executeSync();

		// Check the HTTP calls made
		verifyHttp(server).times(1, 
				method(Method.PUT),
				uri("/backend" + PathBuilder.app(this.testApp)));
		verifyHttp(server).times(2, 
				method(Method.POST),
				uri("/backend" + PathBuilder.goalExcecutions(null, null, this.testApp)));
	}

	/**
	 * Two HTTP requests shall be made
	 * 
	 * @throws GoalConfigurationException
	 * @throws GoalExecutionException
	 */
	@Test
	public void testBomSaveEmptyApp() throws GoalConfigurationException, GoalExecutionException {
		// Mock REST services
		this.configureBackendServiceUrl(server);
		this.setupMockServices(this.testApp);

		// Set config
		this.vulasConfiguration.setProperty(CoreConfiguration.TENANT_TOKEN, "foo");
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_GROUP, this.testApp.getMvnGroup());
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_ARTIF, this.testApp.getArtifact());
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_CTX_VERSI, this.testApp.getVersion());
		this.vulasConfiguration.setProperty(CoreConfiguration.APP_UPLOAD_EMPTY, new Boolean(true));
				
		// Execute goal
		final AbstractGoal goal = GoalFactory.create(GoalType.APP, GoalClient.CLI);
		goal.setConfiguration(this.vulasConfiguration).executeSync();
		
		// Check the HTTP calls made (1 app PUT, 2 goal exe POST)
		verifyHttp(server).times(1, 
				method(Method.PUT),
				uri("/backend" + PathBuilder.app(this.testApp)));
		verifyHttp(server).times(2, 
				method(Method.POST),
				uri("/backend" + PathBuilder.goalExcecutions(null, null, this.testApp)));
	}
}
