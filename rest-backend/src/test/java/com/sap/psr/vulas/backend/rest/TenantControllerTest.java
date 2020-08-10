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
package com.sap.psr.vulas.backend.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Predicate;

import javax.persistence.EntityNotFoundException;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.repo.TenantRepository;
import com.sap.psr.vulas.shared.json.JacksonUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = MainController.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TenantControllerTest {

    private MediaType contentType =
            new MediaType(
                    MediaType.APPLICATION_JSON.getType(),
                    MediaType.APPLICATION_JSON.getSubtype(),
                    Charset.forName("utf8"));

    private MockMvc mockMvc;
    private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;

    public static final String TEST_DEFAULT_SPACE = "public";
    public static final String TEST_DEFAULT_TENANT = "default";

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
                "the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Check tenant creation.
     * @throws Exception
     */
    @Test
    public void testTenantCreation() throws Exception {

        createDefaultTenant();

        final String tenant_name = "foo";
        com.sap.psr.vulas.shared.json.model.Tenant new_shared_tenant =
                new com.sap.psr.vulas.shared.json.model.Tenant();

        // attempt to create another default tenant
        new_shared_tenant.setTenantName(TEST_DEFAULT_TENANT);
        new_shared_tenant.setDefault(true);
        new_shared_tenant.setTenantToken(
                TEST_DEFAULT_TENANT); // Token does not matter, will be generated anyways
                                      // server-side

        MockHttpServletRequestBuilder post_builder =
                post("/tenants")
                        .content(JacksonUtil.asJsonString(new_shared_tenant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(post_builder).andExpect(status().isBadRequest());

        assertEquals(1, this.tenantRepository.count());

        // create a non default tenant
        new_shared_tenant.setTenantName(tenant_name);
        new_shared_tenant.setDefault(false);

        post_builder =
                post("/tenants")
                        .content(JacksonUtil.asJsonString(new_shared_tenant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(post_builder)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.tenantName", is(tenant_name)));

        assertEquals(2, this.tenantRepository.count());
    }

    /**
     * Check presence of default tenant.
     * @throws Exception
     */
    @Test
    public void testGetTenant() throws Exception {

        createDefaultTenant();

        // Read tenant from repo by token
        final Tenant tenant =
                TenantRepository.FILTER.findOne(
                        this.tenantRepository.findBySecondaryKey(TEST_DEFAULT_TENANT));

        // Read default tenant
        final Tenant default_tenant = this.tenantRepository.findDefault();

        // Default tenant exists
        MockHttpServletRequestBuilder opts_builder = options("/tenants/" + TEST_DEFAULT_TENANT);
        mockMvc.perform(opts_builder).andExpect(status().isOk());

        // Default tenant members
        MockHttpServletRequestBuilder get_builder =
                get("/tenants/" + TEST_DEFAULT_TENANT).accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(get_builder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                //	.andExpect(content().json(JacksonUtil.asJsonString(default_tenant))) //the
                // deserialized tenant does not match the one read from the db as the spaces are not
                // transfered
                .andExpect(jsonPath("$.default", is(true)))
                .andExpect(jsonPath("$.spaces").doesNotExist())
                .andExpect(jsonPath("$.tenantToken", is(TEST_DEFAULT_TENANT)))
                .andExpect(jsonPath("$.tenantName", is(TEST_DEFAULT_TENANT)));
    }

    private void createDefaultTenant() {
        Tenant default_tenant = null;

        try {
            default_tenant =
                    TenantRepository.FILTER.findOne(
                            tenantRepository.findBySecondaryKey(TEST_DEFAULT_TENANT));

        } catch (EntityNotFoundException e) {
            default_tenant = new Tenant();
            default_tenant.setTenantToken(TEST_DEFAULT_TENANT);
            default_tenant.setTenantName(TEST_DEFAULT_TENANT);
            default_tenant.setDefault(true);
            tenantRepository.save(default_tenant);
        }
    }
}
