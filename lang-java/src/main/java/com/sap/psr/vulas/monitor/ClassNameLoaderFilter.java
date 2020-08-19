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
package com.sap.psr.vulas.monitor;

/**
 * Checks whether the provided class loader is of the given type, or whether it is a child of another class loader of that type.
 * Use 'org.apache.catalina.loader.WebappClassLoader', for instance, to only consider classes loaded/traced in the context of
 * Web applications deployed in Tomcat (but not classes loaded by parent class loaders, such as the system or bootstrap loader).
 */
public class ClassNameLoaderFilter implements LoaderFilter {

  private String classname = null;
  private boolean acceptChilds = false;

  /**
   * <p>Constructor for ClassNameLoaderFilter.</p>
   *
   * @param _classname a {@link java.lang.String} object.
   * @param _accept_childs a boolean.
   */
  public ClassNameLoaderFilter(String _classname, boolean _accept_childs) {
    this.classname = _classname;
    this.acceptChilds = _accept_childs;
  }

  /**
   * {@inheritDoc}
   *
   * Returns true of the given _loader is accepted by the filter.
   */
  public boolean accept(Loader _loader) {
    if (_loader.getClassLoader().getClass().getName().equals(this.classname)) return true;

    if (!this.acceptChilds) return false;

    final Loader parent = _loader.getParent();
    if (parent == null) return false;
    else return this.accept(parent);
  }
}
