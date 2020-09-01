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
package org.eclipse.steady.backend.rest;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.steady.shared.connectivity.Service;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.restassured.RestAssured;
import com.xebialabs.restito.server.StubServer;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = MainController.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IT02_CoverageControllerIT {

  /** Mocks the CVE endpoint. */
  protected StubServer cveService;

  /** Mocks JIRA. */
  protected StubServer jiraService;

  private MediaType contentType =
      new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));

  private MockMvc mockMvc;
  private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {

    this.mappingJackson2HttpMessageConverter =
        Arrays.asList(converters).stream()
            .filter(
                new Predicate<HttpMessageConverter<?>>() {
                  @Override
                  public boolean test(HttpMessageConverter<?> hmc) {
                    return hmc instanceof MappingJackson2HttpMessageConverter;
                  }
                })
            .findAny()
            .get();

    Assert.assertNotNull(
        "the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
  }

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();

    // CVE service
    cveService = new StubServer().run();
    RestAssured.port = cveService.getPort();
    StringBuffer b = new StringBuffer();
    b.append("http://localhost:").append(cveService.getPort()).append("/cves/");
    VulasConfiguration.getGlobal()
        .setProperty(VulasConfiguration.getServiceUrlKey(Service.CVE), b.toString());

    // JIRA service
    jiraService = new StubServer().run();
    RestAssured.port = jiraService.getPort();
    b = new StringBuffer();
    b.append("http://localhost:").append(cveService.getPort()).append("/rest/api/2/search");
    VulasConfiguration.getGlobal()
        .setProperty(VulasConfiguration.getServiceUrlKey(Service.JIRA), b.toString());
  }

  @After
  public void stop() {
    cveService.stop();
    jiraService.stop();
  }

  @Test
  public void testJiraSearch() throws Exception {
    // JiraSearchResponse r = ServiceWrapper.getInstance().searchJira("CVE-2014-0160");
    System.out.println("Something");
  }

  /**
   * Tests the coverage service, which makes calls to Jira and the classifier service.
   * @throws Exception
   */
  /*@Test
  public void testUnknown() throws Exception {
  	MvcResult r = mockMvc.perform(get("/coverage/CVE-2014-0050"))
  			.andExpect(status().is2xxSuccessful())
  			.andExpect(jsonPath("$.bug", is("CVE-2014-0050")))
  			.andExpect(jsonPath("$.description", startsWith("MultipartStream.java in Apache Commons FileUpload")))
  			//.andExpect(jsonPath("$.statusText", is(CoverageStatus.UNKNOWN.toString())))
  			.andReturn();

  	String content = r.getResponse().getContentAsString();

  	r = mockMvc.perform(get("/coverage/CVE-2017-0001"))
  			.andExpect(status().is2xxSuccessful())
  			.andExpect(jsonPath("$.bug", is("CVE-2017-0001")))
  			.andExpect(jsonPath("$.description", startsWith("The Graphics Device Interface (GDI) in Microsoft Windows Vista SP2")))
  			.andExpect(jsonPath("$.statusText", is(CoverageStatus.OUT_OF_SCOPE.toString())))
  			.andReturn();

  	content = r.getResponse().getContentAsString();

  	r = mockMvc.perform(get("/coverage/CVE-2017-12629"))
  			.andExpect(status().is2xxSuccessful())
  			.andExpect(jsonPath("$.bug", is("CVE-2017-12629")))
  			.andExpect(jsonPath("$.description", startsWith("Remote code execution occurs in Apache Solr")))
  			.andExpect(jsonPath("$.statusText", is(CoverageStatus.OPEN.toString())))
  			.andReturn();

  	content = r.getResponse().getContentAsString();
  }*/

  /*@Test
  public void testOutOfScope() throws Exception {
  	mockMvc.perform(get("/coverage/CVE-2014-0160"))
  		.andExpect(status().is2xxSuccessful())
  		.andExpect(jsonPath("$.bug", is("CVE-2014-0160")))
  		.andExpect(jsonPath("$.statusText", is(CoverageStatus.OUT_OF_SCOPE.toString())));
  }*/
}
