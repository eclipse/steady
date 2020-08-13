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
package com.sap.psr.vulas.tasks;


import com.sap.psr.vulas.shared.json.model.Application;

/**
 * <p>Abstract AbstractBomTask class.</p>
 *
 */
public abstract class AbstractBomTask extends AbstractTask implements BomTask {

  private Application app = null;

  /**
   * <p>setCompletedApplication.</p>
   *
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   */
  protected void setCompletedApplication(Application _app) {
    this.app = _app;
  }

  /** {@inheritDoc} */
  @Override
  public Application getCompletedApplication() {
    return app;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }
}
