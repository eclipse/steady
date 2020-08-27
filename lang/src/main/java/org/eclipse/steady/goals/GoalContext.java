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
package org.eclipse.steady.goals;

import java.io.Serializable;

import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Space;
import org.eclipse.steady.shared.json.model.Tenant;
import org.eclipse.steady.shared.util.Constants;
import org.eclipse.steady.shared.util.VulasConfiguration;

/**
 * Represents the context of a goal execution. All its elements can be specified using
 * the tenant, space and application related configuration settings.
 *
 * @see CoreConfiguration#APP_CTX_GROUP
 * @see CoreConfiguration#TENANT_TOKEN
 * @see CoreConfiguration#SPACE_TOKEN
 *
 * The secret tokens of the tenant and space, if any, will be included as HTTP headers
 * into every request sent to the Vulas backend.
 * @see Constants#HTTP_TENANT_HEADER
 * @see Constants#HTTP_SPACE_HEADER
 */
public class GoalContext implements Serializable {

  private static final long serialVersionUID = 5117042314456447399L;

  private Tenant tenant = null;

  private Space space = null;

  private Application application = null;

  private transient VulasConfiguration vulasConfiguration = null;

  /**
   * <p>hasTenant.</p>
   *
   * @return a boolean.
   */
  public boolean hasTenant() {
    return this.tenant != null;
  }
  /**
   * <p>Getter for the field <code>tenant</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Tenant} object.
   */
  public Tenant getTenant() {
    return this.tenant;
  }
  /**
   * <p>Setter for the field <code>tenant</code>.</p>
   *
   * @param _t a {@link org.eclipse.steady.shared.json.model.Tenant} object.
   */
  public void setTenant(Tenant _t) {
    this.tenant = _t;
  }

  /**
   * <p>hasSpace.</p>
   *
   * @return a boolean.
   */
  public boolean hasSpace() {
    return this.space != null;
  }
  /**
   * <p>Getter for the field <code>space</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Space} object.
   */
  public Space getSpace() {
    return this.space;
  }
  /**
   * <p>Setter for the field <code>space</code>.</p>
   *
   * @param _s a {@link org.eclipse.steady.shared.json.model.Space} object.
   */
  public void setSpace(Space _s) {
    this.space = _s;
  }

  /**
   * <p>hasApplication.</p>
   *
   * @return a boolean.
   */
  public boolean hasApplication() {
    return this.application != null;
  }
  /**
   * <p>Getter for the field <code>application</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public Application getApplication() {
    return this.application;
  }
  /**
   * <p>Setter for the field <code>application</code>.</p>
   *
   * @param _a a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public void setApplication(Application _a) {
    this.application = _a;
  }

  /**
   * <p>Getter for the field <code>vulasConfiguration</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   */
  public VulasConfiguration getVulasConfiguration() {
    return vulasConfiguration;
  }
  /**
   * <p>Setter for the field <code>vulasConfiguration</code>.</p>
   *
   * @param vulasConfiguration a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   */
  public void setVulasConfiguration(VulasConfiguration vulasConfiguration) {
    this.vulasConfiguration = vulasConfiguration;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final StringBuffer b = new StringBuffer();
    b.append("[");

    if (this.tenant != null) b.append("tenant=").append(this.tenant.toString());

    if (this.space != null) {
      if (!b.toString().equals("[")) b.append(", ");
      b.append("space=").append(this.space.toString());
    }

    if (this.application != null) {
      if (!b.toString().equals("[")) b.append(", ");
      b.append("app=").append(this.application.toString());
    }
    b.append("]");
    return b.toString();
  }
}
