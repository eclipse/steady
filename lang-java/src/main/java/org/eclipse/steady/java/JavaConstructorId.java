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
package org.eclipse.steady.java;

import java.util.List;

import org.eclipse.steady.ConstructId;

/**
 * Identifies a class constructor relative to its class (normal or nested).
 */
public class JavaConstructorId extends JavaId {

  private JavaId context = null;
  private List<String> parameterTypes = null;

  /**
   * <p>Constructor for JavaConstructorId.</p>
   *
   * @param _simple_name a {@link org.eclipse.steady.java.JavaId} object.
   * @param _parameter_types a {@link java.util.List} object.
   */
  public JavaConstructorId(JavaId _simple_name, List<String> _parameter_types) {
    super(JavaId.Type.CONSTRUCTOR);
    this.context = _simple_name;
    this.parameterTypes = _parameter_types;
  }

  /**
   * Returns the definition context, which can be a class or enum.
   * Due to the bad naming, rather use {@link JavaMethodId#getDefinitionContext()}.
   *
   * @return a {@link org.eclipse.steady.java.JavaId} object.
   */
  @Deprecated
  public JavaId getJavaClassContext() {
    return this.context;
  }

  /**
   * Returns the fully qualified constructor name, including package and (unqualified) parameter types.
   * Example: test.package.TestClass(int)
   *
   * @return a {@link java.lang.String} object.
   */
  public String getQualifiedName() {
    return context.getQualifiedName() + JavaId.parameterTypesToString(this.parameterTypes, true);
  }

  /**
   * Returns the constructor name, which is equal to the class name, plus the (unqualified) parameter types in brackets.
   * Example: TestClass(int)
   *
   * @return a {@link java.lang.String} object.
   */
  public String getName() {
    return this.context.getSimpleName() + JavaId.parameterTypesToString(this.parameterTypes, true);
  }

  /**
   * Returns the simple constructor name, which is equal to the class name.
   * Example: Test
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSimpleName() {
    return this.context.getSimpleName();
  }

  /**
   * <p>getDefinitionContext.</p>
   *
   * @return a {@link org.eclipse.steady.ConstructId} object.
   */
  public ConstructId getDefinitionContext() {
    return this.context;
  }

  /**
   * <p>getJavaPackageId.</p>
   *
   * @return a {@link org.eclipse.steady.java.JavaPackageId} object.
   */
  public JavaPackageId getJavaPackageId() {
    return this.context.getJavaPackageId();
  }

  /**
   * <p>getConstructorIdParams.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getConstructorIdParams() {
    return JavaId.parameterTypesToString(this.parameterTypes);
  }

  /**
   * Returns true if the constructor has parameters, false otherwise.
   *
   * @return a boolean.
   */
  public boolean hasParams() {
    return this.parameterTypes != null && this.parameterTypes.size() > 0;
  }
}
