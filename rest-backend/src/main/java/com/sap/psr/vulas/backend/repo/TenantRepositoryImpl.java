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
package com.sap.psr.vulas.backend.repo;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sap.psr.vulas.backend.model.Tenant;

/**
 * <p>TenantRepositoryImpl class.</p>
 *
 */
public class TenantRepositoryImpl implements TenantRepositoryCustom {

    private static Logger log = LoggerFactory.getLogger(TenantRepositoryImpl.class);

    @Autowired TenantRepository tenantRepository;

    @Autowired SpaceRepository spaceRepository;

    /** {@inheritDoc} */
    public Boolean isTenantComplete(Tenant _tenant) {
        // Check arguments
        if (_tenant == null) {
            log.error("No tenant submitted");
            return false;
        } else if (!_tenant.hasTenantName()) {
            log.error(
                    "Tenant creation/modification requires name, adjust the configuration"
                            + " accordingly");
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Returns the tenant for the given tenant token. In case a tenant token is not provided, it returns the default tenant (if existing, null otherwise)
     */
    public Tenant getTenant(String _tenantToken) {
        Tenant tenant = null;
        if (_tenantToken == null) {
            tenant = tenantRepository.findDefault();
            if (tenant == null) {
                log.error("No default tenant exists");
                throw new EntityNotFoundException("Default tenant does not exists");
            }
        } else {
            try {
                tenant =
                        TenantRepository.FILTER.findOne(
                                this.tenantRepository.findBySecondaryKey(_tenantToken));
            } catch (EntityNotFoundException enfe) {
                log.error(
                        "A tenant with token ["
                                + _tenantToken
                                + "] does not exist: "
                                + enfe.getMessage());
                throw enfe;
            }
        }

        log.debug("Found " + tenant + " for token [" + _tenantToken + "]");
        return tenant;
    }
}
