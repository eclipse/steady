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
package org.eclipse.steady.tasks;

import org.eclipse.steady.Construct;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Dependency;
import org.eclipse.steady.shared.json.model.Library;

/**
 * Methods required to create a method-level BOM (Bill of Material) of an {@link Application} developed in a given
 * {@link ProgrammingLanguage}.
 */
public interface BomTask extends Task {

  /**
   * Returns the {@link Application} including (a) all its {@link Construct}s of the respective {@link ProgrammingLanguage},
   * and (b) the {@link Dependency}s of that application. The {@link Library} of each {@link Dependency} must contain
   * all details such as its {@link Construct}s and properties.
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public Application getCompletedApplication();
}
