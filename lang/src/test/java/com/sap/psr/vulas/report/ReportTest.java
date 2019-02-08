package com.sap.psr.vulas.report;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.composite;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.uri;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.Configuration;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractGoalTest;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.shared.connectivity.PathBuilder;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;


public class ReportTest extends AbstractGoalTest {

	/**
	 * App creation results in the following two HTTP calls.
	 * @param _a TODO
	 */
	private void setupMockServices(Application _a) throws IOException {
		final String s_json = JacksonUtil.asJsonString(_a);
		String path = null;
		
		// Options app: 200
		path = "/backend" + PathBuilder.app(_a);
		whenHttp(server).
		match(composite(method(Method.OPTIONS), uri(path))).
		then(
				stringContent(s_json),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.OK_200));

		// Get historical vuln deps
		path = "/backend" + PathBuilder.appVulnDeps(_a, true, false, true);
		path = path.substring(0, path.indexOf('?'));
		whenHttp(server).
		match(composite(method(Method.GET), uri(path), parameter("includeHistorical", "true"), parameter("includeAffected", "false"), parameter("includeAffectedUnconfirmed", "true"))).
		then(
				stringContent(FileUtil.readFile(Paths.get("./src/test/resources/vuln_deps_hist.json"))),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.OK_200));

		// Get actual vuln deps
		path = "/backend" + PathBuilder.appVulnDeps(_a, false, true, true);
		path = path.substring(0, path.indexOf('?'));
		whenHttp(server).
		match(composite(method(Method.GET), uri(path), parameter("includeHistorical", "false"), parameter("includeAffected", "true"), parameter("includeAffectedUnconfirmed", "true"))).
		then(
				stringContent(FileUtil.readFile(Paths.get("./src/test/resources/vuln_deps_actual.json"))),
				contentType("application/json"),
				charset("UTF-8"),
				status(HttpStatus.OK_200));
	}

	/**
	 * Two HTTP requests shall be made
	 * 
	 * @throws GoalConfigurationException
	 * @throws GoalExecutionException
	 */
	@Test
	public void testReport() {
		try {
			// Mock REST services
			this.configureBackendServiceUrl(server);
			this.setupMockServices(this.testApp);

			final Configuration cfg = vulasConfiguration.getConfiguration();

			final GoalContext goal_context = new GoalContext();
			goal_context.setVulasConfiguration(vulasConfiguration);
			goal_context.setTenant(this.testTenant);
			goal_context.setSpace(this.testSpace);
			goal_context.setApplication(this.testApp);

			final Report report = new Report(goal_context, this.testApp, null);

			// Set all kinds of exceptions
			report.setExceptionThreshold(cfg.getString(CoreConfiguration.REP_EXC_THRESHOLD, Report.THRESHOLD_ACT_EXE));

			// Excluded bugs
			report.addExcludedBugs(cfg.getStringArray(CoreConfiguration.REP_EXCL_BUGS));

			// Excluded scopes
			report.addExcludedScopes(cfg.getStringArray(CoreConfiguration.REP_EXC_SCOPE_BL));

			// Exclude non-assessed vuln deps
			report.setIgnoreUnassessed(cfg.getString(CoreConfiguration.REP_EXCL_UNASS, Report.IGN_UNASS_KNOWN));

			// Fetch the vulns
			report.fetchAppVulnerabilities();

			// Loop over vulnerabilities
			report.processVulnerabilities();

			final Path report_dir = Paths.get("./target/vulas/report");
			if(!report_dir.toFile().exists())
				report_dir.toFile().mkdirs();

			report.writeResult(report_dir);

			// Check the HTTP calls made
			String path = "/backend" + PathBuilder.appVulnDeps(this.testApp, true, false, true);
			path = path.substring(0, path.indexOf('?'));
			verifyHttp(server).times(1, 
					method(Method.GET),
					uri(path),
					parameter("includeHistorical", "true"), parameter("includeAffected", "false"), parameter("includeAffectedUnconfirmed", "true") );
			verifyHttp(server).times(1, 
					method(Method.GET),
					uri(path),
					parameter("includeHistorical", "false"), parameter("includeAffected", "true"), parameter("includeAffectedUnconfirmed", "true") );
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
