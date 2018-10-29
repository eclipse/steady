import com.jayway.restassured.RestAssured;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.connectivity.PathBuilder;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Space;
import com.sap.psr.vulas.shared.json.model.Tenant;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.xebialabs.restito.server.StubServer;
import org.glassfish.grizzly.http.util.HttpStatus;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.options;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.*;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.uri;


public class StubServerSetup {

    public StubServer server;

    protected Tenant testTenant;
    protected Space testSpace;
    protected Application testApp;


    private String backendURL;

    public StubServerSetup(String group, String artifact, String version) {
        server = new StubServer().run();
        RestAssured.port = server.getPort();

        testTenant = this.buildTestTenant();
        testSpace = this.buildTestSpace();
        testApp = this.buildTestApplication(group, artifact, version);

        // App context
        System.setProperty(CoreConfiguration.APP_CTX_GROUP, testApp.getMvnGroup());
        System.setProperty(CoreConfiguration.APP_CTX_ARTIF, testApp.getArtifact());
        System.setProperty(CoreConfiguration.APP_CTX_VERSI, testApp.getVersion());

        // Identify app code
        System.setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.READ_WRITE.toString());

        // Identify app code
        System.setProperty(CoreConfiguration.APP_PREFIXES, "com.acme");
    }

    public void stop() {
        server.stop();
        System.out.println("Stopped Server:" + this.backendURL);

    }

    protected Tenant buildTestTenant() {
        final String rnd = StringUtil.getRandonString(6);
        return new Tenant("tenant-token-" + rnd, "tenant-name-" + rnd);
    }

    protected Space buildTestSpace() {
        final String rnd = StringUtil.getRandonString(6);
        return new Space("space-token-" + rnd, "space-name-" + rnd, "space-description");
    }


    protected Application buildTestApplication(String group, String artifact, String version) {
        return new Application(group, artifact, version);
    }

    protected void configureBackendServiceUrl(StubServer _ss) {
        final StringBuffer b = new StringBuffer();
        b.append("http://localhost:").append(_ss.getPort()).append("/backend");
        this.backendURL = b.toString();
        VulasConfiguration.getGlobal().setProperty(VulasConfiguration.getServiceUrlKey(Service.BACKEND), this.backendURL);
        System.out.println("Started Server:" + this.backendURL);

    }

    public String getBackendURL() {
        return backendURL;
    }


    /**
     * App creation results in the following two HTTP calls.
     *
     * @param _a TODO
     */
    public void setupMockServices(Application _a) {
        final String s_json = JacksonUtil.asJsonString(_a);
        whenHttp(server).
                match(post("/backend" + PathBuilder.apps())).
                then(
                        stringContent(s_json),
                        contentType("application/json"),
                        charset("UTF-8"),
                        status(HttpStatus.CREATED_201));

        expect()
                .statusCode(201).
                when()
                .post("/backend" + PathBuilder.apps());

        whenHttp(server).
                match(post("/backend" + PathBuilder.goalExcecutions(null, null, _a))).
                then(
                        stringContent(s_json),
                        contentType("application/json"),
                        charset("UTF-8"),
                        status(HttpStatus.CREATED_201));

        expect()
                .statusCode(201).
                when()
                .post("/backend" + PathBuilder.goalExcecutions(null, null, _a));

        whenHttp(server).
                match(uri("/backend" + PathBuilder.app(testApp))).
                then(
                        stringContent(s_json),
                        contentType("application/json"),
                        charset("UTF-8"),
                        status(HttpStatus.OK_200));

        whenHttp(server).
                match(uri("/backend" + PathBuilder.app(testApp)+"/deps")).
                then(
                        stringContent("[]"),
                        contentType("application/json"),
                        charset("UTF-8"),
                        status(HttpStatus.OK_200));
    }


}
