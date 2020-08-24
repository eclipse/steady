/**
 * This file is part of Eclipse Steady.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>SPDX-License-Identifier: Apache-2.0
 *
 * <p>Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.backend.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import javax.persistence.EntityNotFoundException;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.repo.SpaceRepository;
import com.sap.psr.vulas.backend.repo.TenantRepository;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.enums.PropertySource;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.util.Constants;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = MainController.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SpaceControllerTest {

  private MediaType contentType =
      new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));

  private MockMvc mockMvc;
  private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;

  public static final String TEST_DEFAULT_SPACE = "public";
  public static final String TEST_DEFAULT_TENANT = "default";

  @Autowired private SpaceRepository spaceRepository;

  @Autowired private TenantRepository tenantRepository;

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
    this.spaceRepository.deleteAll();
    this.tenantRepository.deleteAll();
  }

  @After
  public void reset() throws Exception {
    this.spaceRepository.deleteAll();
    this.tenantRepository.deleteAll();
  }

  //	/**
  //	 * Check space creation without tenant header
  //	 * @throws Exception
  //	 */
  //	@Test
  //	public void testSpaceCreationNoTenant() throws Exception {
  //		final Tenant d_tenant = this.createDefaultTenant();
  //		com.sap.psr.vulas.shared.json.model.Space new_shared_space = new
  // com.sap.psr.vulas.shared.json.model.Space();
  //		new_shared_space.setSpaceName("spaceNoTenant");
  //		new_shared_space.setDefault(false);
  //		new_shared_space.setExportConfiguration(ExportConfiguration.OFF);
  //		new_shared_space.setSpaceDescription("description");
  //		new_shared_space.setOwnerEmails(new HashSet<String>(Arrays.asList(new String[]
  // {"foo@bar.com"})));
  //
  //
  //		// Post w/o tenant header, returns 400
  //		MockHttpServletRequestBuilder post_builder = post("/spaces")
  //				.content(JacksonUtil.asJsonString(new_shared_space))
  //				.contentType(MediaType.APPLICATION_JSON)
  //				.accept(MediaType.APPLICATION_JSON);
  //		mockMvc.perform(post_builder).andExpect(status().isBadRequest());
  //
  //		post_builder = post("/spaces")
  //				.header(Constants.HTTP_TENANT_HEADER,  d_tenant.getTenantToken())
  //				.content(JacksonUtil.asJsonString(new_shared_space))
  //				.contentType(MediaType.APPLICATION_JSON)
  //				.accept(MediaType.APPLICATION_JSON);
  //		mockMvc.perform(post_builder).andExpect(status().isCreated())
  //		.andExpect(content().contentType(contentType))
  //		.andExpect(jsonPath("$.spaceName", is(space_name)));
  //	}

  /**
   * Check space creation.
<<<<<<< HEAD
=======
   *
>>>>>>> master
   * @throws Exception
   */
  @Test
  public void testSpaceCreation() throws Exception {
    assertEquals(0, this.spaceRepository.count());
    final Tenant d_tenant = this.createDefaultTenant();
    final Space d_space = this.createSpace(d_tenant, TEST_DEFAULT_SPACE, true);
    assertEquals(1, this.spaceRepository.count());

    // Create 2nd space used for some tests
    final String space_name = "foo";
    com.sap.psr.vulas.shared.json.model.Space new_shared_space =
        new com.sap.psr.vulas.shared.json.model.Space();
    new_shared_space.setSpaceName(TEST_DEFAULT_SPACE);
    new_shared_space.setDefault(
        true); // This will not be possible, as another default space has been created above
    new_shared_space.setExportConfiguration(ExportConfiguration.OFF);
    new_shared_space.setSpaceDescription("bar");
    new_shared_space.setOwnerEmails(
        new HashSet<String>(Arrays.asList(new String[] {"foo@bar.com"})));

    // returns 400 as default space already exists
    MockHttpServletRequestBuilder post_builder =
        post("/spaces")
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .content(JacksonUtil.asJsonString(new_shared_space))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc.perform(post_builder).andExpect(status().isBadRequest());
    //		MvcResult result = mockMvc.perform(post_builder).andExpect(status().isCreated())
    //				.andExpect(content().contentType(contentType))
    //				.andExpect(jsonPath("$.spaceName", is(space_name))).andReturn();

    // Still one space only
    assertEquals(1, this.spaceRepository.count());

    new_shared_space.setDefault(false);

    // Post w/o tenant header to try to create a NON default space
    post_builder =
        post("/spaces")
            .content(JacksonUtil.asJsonString(new_shared_space))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc.perform(post_builder).andExpect(status().isCreated());

    assertEquals(2, this.spaceRepository.count());

    // Create with read-only enabled -> bad request
    new_shared_space.setReadOnly(true);
    post_builder =
        post("/spaces")
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .content(JacksonUtil.asJsonString(new_shared_space))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc.perform(post_builder).andExpect(status().isBadRequest());

    // Still two space only
    assertEquals(2, this.spaceRepository.count());

    new_shared_space.setReadOnly(false);
    new_shared_space.setSpaceName(space_name);
    new_shared_space.setDefault(false);

    post_builder =
        post("/spaces")
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .content(JacksonUtil.asJsonString(new_shared_space))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.spaceName", is(space_name)));

    // Now three space
    assertEquals(3, this.spaceRepository.count());

    // Post with non-existing tenant token, defaults in controller method
    MockHttpServletRequestBuilder post_builder2 =
        post("/spaces")
            .content(JacksonUtil.asJsonString(new_shared_space))
            .contentType(MediaType.APPLICATION_JSON)
            .header(Constants.HTTP_TENANT_HEADER, "does-not-exist")
            .accept(MediaType.APPLICATION_JSON);
    mockMvc.perform(post_builder2).andExpect(status().isNotFound());
  }

  /**
   * Check presence of spaces.
<<<<<<< HEAD
=======
   *
>>>>>>> master
   * @throws Exception
   */
  @Test
  public void testGetSpace() throws Exception {
    assertEquals(0, this.spaceRepository.count());
    final Tenant d_tenant = this.createDefaultTenant();
    final Space d_space = this.createSpace(d_tenant, TEST_DEFAULT_SPACE, true);
    assertEquals(1, this.spaceRepository.count());

    // Default tenant exists
    MockHttpServletRequestBuilder opts_builder =
        options("/spaces/" + TEST_DEFAULT_SPACE)
            .header(Constants.HTTP_TENANT_HEADER, TEST_DEFAULT_TENANT);
    mockMvc.perform(opts_builder).andExpect(status().isOk());

    // Default tenant members
    MockHttpServletRequestBuilder get_builder =
        get("/spaces/" + TEST_DEFAULT_SPACE)
            .header(Constants.HTTP_TENANT_HEADER, TEST_DEFAULT_TENANT)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.spaceToken", is(TEST_DEFAULT_SPACE)))
        .andExpect(jsonPath("$.spaceName", is(TEST_DEFAULT_SPACE)));
  }

  /**
   * Check space creation and deletion.
<<<<<<< HEAD
=======
   *
>>>>>>> master
   * @throws Exception
   */
  @Test
  public void testSpaceCreationAndDeletion() throws Exception {
    assertEquals(0, this.spaceRepository.count());
    final Tenant d_tenant = this.createDefaultTenant();
    final Space d_space = this.createSpace(d_tenant, TEST_DEFAULT_SPACE, true);
    assertEquals(1, this.spaceRepository.count());

    final String space_name = "foo";
    com.sap.psr.vulas.shared.json.model.Space new_shared_space =
        new com.sap.psr.vulas.shared.json.model.Space();
    new_shared_space.setSpaceName("foo");
    new_shared_space.setExportConfiguration(ExportConfiguration.OFF);
    new_shared_space.setSpaceDescription("bar");
    new_shared_space.setOwnerEmails(
        new HashSet<String>(Arrays.asList(new String[] {"foo@bar.com"})));

    // Post w tenant header (should create a NON default space as it's not provided)
    MockHttpServletRequestBuilder post_builder =
        post("/spaces")
            .content(JacksonUtil.asJsonString(new_shared_space))
            .contentType(MediaType.APPLICATION_JSON)
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .accept(MediaType.APPLICATION_JSON);
    MvcResult result =
        mockMvc
            .perform(post_builder)
            .andExpect(status().isCreated())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$.default", is(false)))
            .andExpect(jsonPath("$.spaceName", is(space_name)))
            .andReturn();

    assertEquals(2, this.spaceRepository.count());

    // Get the generated token
    com.sap.psr.vulas.shared.json.model.Space created_shared_space =
        (com.sap.psr.vulas.shared.json.model.Space)
            JacksonUtil.asObject(
                result.getResponse().getContentAsString(),
                com.sap.psr.vulas.shared.json.model.Space.class);

    // Post with non-existing tenant token, defaults in controller method
    MockHttpServletRequestBuilder del_builder =
        delete("/spaces/" + created_shared_space.getSpaceToken())
            .header(Constants.HTTP_TENANT_HEADER, TEST_DEFAULT_TENANT)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc.perform(del_builder).andExpect(status().isOk());

    assertEquals(1, this.spaceRepository.count());
  }

  /**
   * Test modification of read-only space.
<<<<<<< HEAD
=======
   *
>>>>>>> master
   * @throws Exception
   */
  @Test
  public void testReadOnlySpace() throws Exception {
    assertEquals(0, this.spaceRepository.count());
    final Tenant d_tenant = this.createDefaultTenant();
    final Space d_space = this.createSpace(d_tenant, TEST_DEFAULT_SPACE, true);
    assertEquals(1, this.spaceRepository.count());

    // Change to read-only (should work)
    d_space.setReadOnly(true);
    MockHttpServletRequestBuilder post_builder =
        put("/spaces/" + d_space.getSpaceToken())
            .content(JacksonUtil.asJsonString(d_space))
            .contentType(MediaType.APPLICATION_JSON)
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.default", is(true)))
        .andExpect(jsonPath("$.spaceName", is(TEST_DEFAULT_SPACE)));

    // Change to read-write (should fail)
    d_space.setReadOnly(false);
    post_builder =
        put("/spaces/" + d_space.getSpaceToken())
            .content(JacksonUtil.asJsonString(d_space))
            .contentType(MediaType.APPLICATION_JSON)
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .accept(MediaType.APPLICATION_JSON);
    mockMvc.perform(post_builder).andExpect(status().isBadRequest());
  }

  /**
   * Creates a workspace and searches for it.
<<<<<<< HEAD
=======
   *
>>>>>>> master
   * @throws Exception
   */
  @Test
  public void testSearchSpaces() throws Exception {
    assertEquals(0, this.spaceRepository.count());
    final Tenant d_tenant = this.createDefaultTenant();
    final Space d_space = this.createSpace(d_tenant, TEST_DEFAULT_SPACE, true);
    assertEquals(1, this.spaceRepository.count());

    final String space_name = "foo";
    com.sap.psr.vulas.shared.json.model.Space new_shared_space =
        new com.sap.psr.vulas.shared.json.model.Space();
    new_shared_space.setSpaceName("foo");
    new_shared_space.setExportConfiguration(ExportConfiguration.OFF);
    new_shared_space.setSpaceDescription("bar");
    new_shared_space.setOwnerEmails(
        new HashSet<String>(Arrays.asList(new String[] {"foo@bar.com"})));
    final List<com.sap.psr.vulas.shared.json.model.Property> props =
        new ArrayList<com.sap.psr.vulas.shared.json.model.Property>();
    props.add(
        new com.sap.psr.vulas.shared.json.model.Property(
            PropertySource.USER, "propName", "propValue"));
    new_shared_space.setProperties(props);

    // Post w tenant header (should create a NON default space as it's not provided)
    MockHttpServletRequestBuilder post_builder =
        post("/spaces")
            .content(JacksonUtil.asJsonString(new_shared_space))
            .contentType(MediaType.APPLICATION_JSON)
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .accept(MediaType.APPLICATION_JSON);
    MvcResult result =
        mockMvc
            .perform(post_builder)
            .andExpect(status().isCreated())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$.default", is(false)))
            .andExpect(jsonPath("$.spaceName", is(space_name)))
            .andReturn();

    // Create another one (private)
    new_shared_space.setPublic(false);
    post_builder =
        post("/spaces")
            .content(JacksonUtil.asJsonString(new_shared_space))
            .contentType(MediaType.APPLICATION_JSON)
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .accept(MediaType.APPLICATION_JSON);
    result =
        mockMvc
            .perform(post_builder)
            .andExpect(status().isCreated())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$.default", is(false)))
            .andExpect(jsonPath("$.spaceName", is(space_name)))
            .andReturn();

    assertEquals(3, this.spaceRepository.count());

    // Search : 0 hits
    MockHttpServletRequestBuilder search_builder =
        get("/spaces/search")
            .param("propertyName", "propName")
            .param("value", "xyz", "abc")
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .accept(MediaType.APPLICATION_JSON);
    result = mockMvc.perform(search_builder).andExpect(status().isOk()).andReturn();

    com.sap.psr.vulas.shared.json.model.Space[] search_result =
        (com.sap.psr.vulas.shared.json.model.Space[])
            JacksonUtil.asObject(
                result.getResponse().getContentAsString(),
                com.sap.psr.vulas.shared.json.model.Space[].class);
    assertEquals(0, search_result.length);

    // Search : 1 hit
    search_builder =
        get("/spaces/search")
            .param("propertyName", "propName")
            .param("value", "xyz", "abc", "propValue")
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .accept(MediaType.APPLICATION_JSON);
    result = mockMvc.perform(search_builder).andExpect(status().isOk()).andReturn();

    search_result =
        (com.sap.psr.vulas.shared.json.model.Space[])
            JacksonUtil.asObject(
                result.getResponse().getContentAsString(),
                com.sap.psr.vulas.shared.json.model.Space[].class);
    assertEquals(
        1,
        search_result.length); // Should be one public space, the private one shall not be returned

    // Bad reqeust due to lack of search term
    search_builder =
        get("/spaces/search")
            .param("propertyName", "propName")
            .header(Constants.HTTP_TENANT_HEADER, d_tenant.getTenantToken())
            .accept(MediaType.APPLICATION_JSON);
    mockMvc.perform(search_builder).andExpect(status().isBadRequest());
  }

  private Tenant createDefaultTenant() {
    Tenant default_tenant = null;
    try {
      default_tenant =
          TenantRepository.FILTER.findOne(tenantRepository.findBySecondaryKey(TEST_DEFAULT_TENANT));
    } catch (EntityNotFoundException e) {
      default_tenant = new Tenant();
      default_tenant.setTenantToken(TEST_DEFAULT_TENANT);
      default_tenant.setTenantName(TEST_DEFAULT_TENANT);
      default_tenant.setDefault(true);
      tenantRepository.save(default_tenant);

      default_tenant =
          TenantRepository.FILTER.findOne(tenantRepository.findBySecondaryKey(TEST_DEFAULT_TENANT));
    }
    return default_tenant;
  }

  private Space createSpace(Tenant _t, String _token, boolean _default) {
    Space space = null;
    try {
      SpaceRepository.FILTER.findOne(spaceRepository.findBySecondaryKey(_token));

    } catch (EntityNotFoundException e) {
      // default space
      space = new Space();
      space.setSpaceName(_token);
      space.setSpaceToken(_token);
      space.setDefault(_default);
      space.setExportConfiguration(ExportConfiguration.OFF);
      space.setSpaceDescription(_token);
      space.setSpaceOwners(new HashSet<String>(Arrays.asList(new String[] {"foo@bar.com"})));
      space.setTenant(_t);
      spaceRepository.save(space);
    }
    return space;
  }
}
