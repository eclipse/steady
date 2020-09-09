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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.backend.repo;

import org.eclipse.steady.backend.model.Tenant;

/**
 * Specifies additional methods of the {@link TenantRepository}.
 */
public interface TenantRepositoryCustom {

  /**
   * Checks that the tenant contains all user-provided info.
   *
   * @param _tenant a {@link org.eclipse.steady.backend.model.Tenant} object.
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean isTenantComplete(Tenant _tenant);

  /**
   * Returns the tenant for the given tenant token (default tenant if token is null).
   *
   * @param _tenantToken a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.backend.model.Tenant} object.
   */
  public Tenant getTenant(String _tenantToken);
}
