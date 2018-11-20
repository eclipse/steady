package com.sap.psr.vulas.cli;

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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.shared.categories.Slow;
import com.sap.psr.vulas.shared.connectivity.PathBuilder;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Application;

public class VulasCliTest extends AbstractGoalTest {

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

		// Post app: 200 (for clean goal)
		whenHttp(server).
		match(post("/backend" + PathBuilder.app(_a))).
		then(
				stringContent(s_json),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.OK_200));
		
//		expect()
//		.statusCode(201).
//		when()
//		.post("/backend" + PathBuilder.apps());

		whenHttp(server).
		match(post("/backend" + PathBuilder.goalExcecutions(null, null, _a))).
		then(
				stringContent(s_json),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.CREATED_201));

//		expect()
//		.statusCode(201).
//		when()
//		.post("/backend" + PathBuilder.goalExcecutions(null, null, _a));
	}

	/**
	 * 
	 */
	@Test
	public void testCleanGoal() throws GoalConfigurationException, GoalExecutionException {
		//System.setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE.toString());

		// Mock REST services
		this.configureBackendServiceUrl(server);
		this.setupMockServices(this.testApp);

		final String[] args = new String[] { "-goal", "clean" };
		VulasCli.main(args);

		// Check the HTTP calls made
		verifyHttp(server).times(1, 
				method(Method.POST),
				uri("/backend" + PathBuilder.app(testApp)));
		verifyHttp(server).times(2, 
				method(Method.POST),
				uri("/backend" + PathBuilder.goalExcecutions(null, null, this.testApp)));
	}

	/**
	 * 
	 */
	@Test
	public void testAppGoal() throws GoalConfigurationException, GoalExecutionException {
		//System.setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE.toString());

		// App: Relative and absolute folders with and without spaces
		final Path rel_app_with_space = Paths.get("src", "test", "resources", "appfolder with space");
		final Path abs_app = Paths.get("src", "test", "resources", "appfolder").toAbsolutePath();
		final Path py_app = Paths.get("src", "test", "resources", "foo").toAbsolutePath();
		final String app_string = rel_app_with_space.toString() + "," + abs_app + "," + py_app;

		// Dep: Relative and absolute folders with and without spaces
		final Path rel_dep_with_space = Paths.get("src", "test", "resources", "depfolder with space");
		final Path abs_dep = Paths.get("src", "test", "resources", "depfolder").toAbsolutePath();
		final Path cc = Paths.get("src", "test", "resources", "depfolder", "commons-collections-3.2.2.jar");
		final String dep_string = cc.toString() + "," + rel_dep_with_space.toString() + "," + abs_dep.toString();

		System.setProperty(CoreConfiguration.APP_DIRS, app_string + "," + dep_string);

		// Mock REST services
		this.configureBackendServiceUrl(server);
		this.setupMockServices(this.testApp);

		final String[] args = new String[] { "-goal", "app" };
		VulasCli.main(args);

		// Check the HTTP calls made
		verifyHttp(server).times(1, 
				method(Method.PUT),
				uri("/backend" + PathBuilder.app(this.testApp)));
		verifyHttp(server).times(2, 
				method(Method.POST),
				uri("/backend" + PathBuilder.goalExcecutions(null, null, this.testApp)));
	}

	/**
	 * 
	 */
	@Test
	@Category(com.sap.psr.vulas.shared.categories.Slow.class)
	public void testPyAppGoal() throws GoalConfigurationException, GoalExecutionException {
		//System.setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE.toString());
		System.setProperty(CoreConfiguration.APP_DIRS, "./src/test/resources/cf-helloworld");
		
		// Mock REST services
		this.configureBackendServiceUrl(server);
		this.setupMockServices(this.testApp);

		final String[] args = new String[] { "-goal", "app" };
		VulasCli.main(args);

		// Check the HTTP calls made
		verifyHttp(server).times(1, 
				method(Method.POST),
				uri("/backend" + PathBuilder.apps()));
		verifyHttp(server).times(2, 
				method(Method.POST),
				uri("/backend" + PathBuilder.goalExcecutions(null, null, this.testApp)));
	}
	
	/**
	 * 
	 */
	@Test
	public void testJavaAppGoal() throws GoalConfigurationException, GoalExecutionException {
		//System.setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE.toString());
		System.setProperty(CoreConfiguration.APP_DIRS, "./src/test/resources/java-app");
		
		// Mock REST services
		this.configureBackendServiceUrl(server);
		this.setupMockServices(this.testApp);

		final String[] args = new String[] { "-goal", "app" };
		VulasCli.main(args);

		// Check the HTTP calls made
		verifyHttp(server).times(1, 
				method(Method.PUT),
				uri("/backend" + PathBuilder.app(this.testApp)));
		verifyHttp(server).times(2, 
				method(Method.POST),
				uri("/backend" + PathBuilder.goalExcecutions(null, null, this.testApp)));
	}
}
