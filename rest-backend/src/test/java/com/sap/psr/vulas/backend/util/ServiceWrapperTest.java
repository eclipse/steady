package com.sap.psr.vulas.backend.util;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.get;
import static org.junit.Assert.assertEquals;

import com.jayway.restassured.RestAssured;
import com.sap.psr.vulas.backend.rest.CoverageController.CveClassifierResponse;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.connectivity.ServiceConnectionException;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.xebialabs.restito.server.StubServer;
import java.io.IOException;
import java.nio.file.Paths;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServiceWrapperTest {

  /** Mocks the CVE classifier. */
  protected StubServer cveService;

  @Before
  public void setup() throws Exception {
    cveService = new StubServer().run();
    RestAssured.port = cveService.getPort();
    StringBuffer b = new StringBuffer();
    b.append("http://localhost:").append(cveService.getPort()).append("/cves/");
    VulasConfiguration.getGlobal()
        .setProperty(VulasConfiguration.getServiceUrlKey(Service.CVE), b.toString());
  }

  @After
  public void stop() {
    cveService.stop();
  }

  /**
   * Mocks the CVE classifier for the given vulnerabilities.
   *
   * @param _cves
   * @throws IOException
   */
  private void setupMockServices(String... _cves) throws IOException {
    for (String s : _cves) {
      whenHttp(cveService)
          .match(get("/cves/" + s))
          .then(
              stringContent(
                  FileUtil.readFile(Paths.get("./src/test/resources/cves/" + s + ".json"))),
              contentType("application/json"),
              charset("UTF-8"),
              status(HttpStatus.OK_200));

      expect().statusCode(200).when().get("/cves/" + s);
    }
  }

  /** Calls {@link ServiceWrapper#classify(String)} for a number of vulnerabilities. */
  @Test
  public void testClassifier() throws ServiceConnectionException, IOException {
    this.setupMockServices("CVE-2014-0050", "CVE-2017-0001");

    final CveClassifierResponse cve20140050 =
        ServiceWrapper.getInstance().classify("CVE-2014-0050");
    assertEquals("java", cve20140050.getLanguage().toLowerCase());
    assertEquals("oss", cve20140050.getLicense().toLowerCase());

    final CveClassifierResponse cve20170001 =
        ServiceWrapper.getInstance().classify("CVE-2017-0001");
    assertEquals("other", cve20170001.getLanguage().toLowerCase());
    assertEquals("proprietary", cve20170001.getLicense().toLowerCase());
  }
}
