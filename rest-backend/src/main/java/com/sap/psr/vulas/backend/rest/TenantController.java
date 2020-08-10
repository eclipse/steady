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

import javax.persistence.EntityNotFoundException;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.repo.TenantRepository;
import com.sap.psr.vulas.backend.util.TokenUtil;
import com.sap.psr.vulas.shared.util.StopWatch;

/**
 * <p>TenantController class.</p>
 *
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/tenants")
public class TenantController {

    private static Logger log = LoggerFactory.getLogger(TenantController.class);

    private TenantRepository tenantRepository;

    private final Filter cacheFilter;

    @Autowired
    TenantController(TenantRepository tenantRepository, Filter cacheFilter) {
        this.tenantRepository = tenantRepository;
        this.cacheFilter = cacheFilter;
    }

    /**
     * Returns all existing {@link Tenant}s.
     *
     * @return a {@link org.springframework.http.ResponseEntity} object.
     */
    @RequestMapping(
            value = "",
            method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    @JsonView(Views.Default.class)
    public ResponseEntity<Iterable<Tenant>> getAllTenants() {
        try {
            // Load existing tenant
            try {
                final Iterable<Tenant> all_tenants = this.tenantRepository.findAll();
                return new ResponseEntity<Iterable<Tenant>>(all_tenants, HttpStatus.OK);
            } catch (EntityNotFoundException enfe) {
                return new ResponseEntity<Iterable<Tenant>>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception enfe) {
            return new ResponseEntity<Iterable<Tenant>>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns the default {@link Tenant}.
     *
     * @return a {@link org.springframework.http.ResponseEntity} object.
     */
    @RequestMapping(
            value = "default",
            method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    @JsonView(Views.Default.class)
    public ResponseEntity<Tenant> getDefaultTenant() {
        try {
            try {
                final Tenant def = this.tenantRepository.findDefault();
                if (def == null)
                    throw new EntityNotFoundException("No default tenant can be found");
                else return new ResponseEntity<Tenant>(def, HttpStatus.OK);
            } catch (EntityNotFoundException enfe) {
                return new ResponseEntity<Tenant>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception enfe) {
            return new ResponseEntity<Tenant>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Checks whether a {@link Tenant} with the given token exists in the database.
     *
     * @param token a {@link java.lang.String} object.
     * @return 404 {@link HttpStatus#NOT_FOUND} if tenant with given token does not exist, 200 {@link HttpStatus#OK} if the tenant is found
     */
    @RequestMapping(value = "/{token:.+}", method = RequestMethod.OPTIONS)
    @JsonView(Views.Default.class)
    public ResponseEntity<Tenant> isTenantExisting(@PathVariable String token) {
        try {
            TenantRepository.FILTER.findOne(tenantRepository.findBySecondaryKey(token));
            return new ResponseEntity<Tenant>(HttpStatus.OK);
        } catch (EntityNotFoundException enfe) {
            return new ResponseEntity<Tenant>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Creates a new {@link Tenant} with a new, random token in the database and returns it to the client.
     *
     * @param tenant a {@link com.sap.psr.vulas.backend.model.Tenant} object.
     * @return a {@link org.springframework.http.ResponseEntity} object.
     */
    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = {"application/json;charset=UTF-8"},
            produces = {"application/json;charset=UTF-8"})
    @JsonView(Views.Default.class)
    public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenant) {
        final StopWatch sw =
                new StopWatch(
                                "Create tenant ["
                                        + (tenant == null ? null : tenant.getTenantName())
                                        + "]")
                        .start();
        try {
            if (!this.tenantRepository.isTenantComplete(tenant)) {
                return new ResponseEntity<Tenant>(HttpStatus.BAD_REQUEST);
            }
            // check that only 1 default tenant exists
            else if (tenant.isDefault() && tenantRepository.findDefault() != null) {
                log.error("A default tenant already exists! Only one default tenant is allowed");
                return new ResponseEntity<Tenant>(HttpStatus.BAD_REQUEST);
            }

            // Always create token, whatever has been submitted
            tenant.setTenantToken(TokenUtil.generateToken());
            this.tenantRepository.save(tenant);
            //		this.tenantRepository.createDefaultSpaces(tenant);
            sw.stop();
            return new ResponseEntity<Tenant>(tenant, HttpStatus.CREATED);
        } catch (Exception enfe) {
            sw.stop(enfe);
            return new ResponseEntity<Tenant>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets an existing {@link Tenant} and returns it to the client.
     *
     * @param token a {@link java.lang.String} object.
     * @return a {@link org.springframework.http.ResponseEntity} object.
     */
    @RequestMapping(
            value = "/{token:.+}",
            method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    @JsonView(Views.Default.class)
    public ResponseEntity<Tenant> getTenant(@PathVariable String token) {
        try {
            // Load existing tenant
            try {
                final Tenant tenant =
                        TenantRepository.FILTER.findOne(
                                this.tenantRepository.findBySecondaryKey(token));
                return new ResponseEntity<Tenant>(tenant, HttpStatus.OK);
            } catch (EntityNotFoundException enfe) {
                return new ResponseEntity<Tenant>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception enfe) {
            return new ResponseEntity<Tenant>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Modifies an existing {@link Tenant} and returns it to the client.
     *
     * @param token a {@link java.lang.String} object.
     * @param new_tenant a {@link com.sap.psr.vulas.backend.model.Tenant} object.
     * @return a {@link org.springframework.http.ResponseEntity} object.
     */
    @RequestMapping(
            value = "/{token:.+}",
            method = RequestMethod.PUT,
            consumes = {"application/json;charset=UTF-8"},
            produces = {"application/json;charset=UTF-8"})
    @JsonView(Views.Default.class)
    public ResponseEntity<Tenant> modifyTenant(
            @PathVariable String token, @RequestBody Tenant new_tenant) {
        try {
            if (!this.tenantRepository.isTenantComplete(new_tenant)) {
                return new ResponseEntity<Tenant>(HttpStatus.BAD_REQUEST);
            }
            // (SP 27-10-2017) The default tenant can now be modified as the notion that it's the
            // default one is stored in the db
            //			else if(token.equals(Constants.DEFAULT_TENANT)) {
            //				log.error("The following tenants cannot be modified: [" + Constants.DEFAULT_TENANT
            // + "]");
            //				return new ResponseEntity<Tenant>(HttpStatus.BAD_REQUEST);
            //			}
            else if (!token.equals(new_tenant.getTenantToken())) {
                log.error("Token in path variable and request body do not match");
                return new ResponseEntity<Tenant>(HttpStatus.BAD_REQUEST);
            }
            // check that only 1 default tenant exists
            else if (new_tenant.isDefault()
                    && tenantRepository.findDefault() != null
                    && !tenantRepository.findDefault().equals(new_tenant)) {
                log.error("A default tenant already exists! Only one default tenant is allowed");
                return new ResponseEntity<Tenant>(HttpStatus.BAD_REQUEST);
            }

            // Load existing tenant, update members and save
            try {
                Tenant old_tenant =
                        TenantRepository.FILTER.findOne(
                                this.tenantRepository.findBySecondaryKey(
                                        new_tenant.getTenantToken()));
                old_tenant.setTenantName(new_tenant.getTenantName());
                old_tenant.setDefault(new_tenant.isDefault());
                this.tenantRepository.save(old_tenant);
                return new ResponseEntity<Tenant>(old_tenant, HttpStatus.OK);
            } catch (EntityNotFoundException enfe) {
                return new ResponseEntity<Tenant>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception enfe) {
            return new ResponseEntity<Tenant>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a given {@link Tenant}.
     *
     * @param token a {@link java.lang.String} object.
     * @return a {@link org.springframework.http.ResponseEntity} object.
     */
    @RequestMapping(value = "/{token:.+}", method = RequestMethod.DELETE)
    @JsonView(Views.Default.class)
    public ResponseEntity<Tenant> deleteTenant(@PathVariable String token) {
        try {
            //			// Check arguments
            //			if(token.equals(Constants.DEFAULT_TENANT)) {
            //				log.error("The following tenants cannot be deleted: [" + Constants.DEFAULT_TENANT
            // + "]");
            //				return new ResponseEntity<Tenant>(HttpStatus.BAD_REQUEST);
            //			}

            // Delete existing tenant
            try {
                Tenant old_tenant =
                        TenantRepository.FILTER.findOne(
                                this.tenantRepository.findBySecondaryKey(token));
                // TODO (27-10-2017): Once we stop supporting vulas 2.x the following condition can
                // be removed
                if (old_tenant.isDefault()) {
                    log.error(
                            "The default tenant cannot be deleted until we stop supporting vulas"
                                + " 2.x.");
                    return new ResponseEntity<Tenant>(HttpStatus.BAD_REQUEST);
                }
                // TODO: Implement deletion
                return new ResponseEntity<Tenant>(HttpStatus.OK);
            } catch (EntityNotFoundException enfe) {
                return new ResponseEntity<Tenant>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception enfe) {
            return new ResponseEntity<Tenant>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
