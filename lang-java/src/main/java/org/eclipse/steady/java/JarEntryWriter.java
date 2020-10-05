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

import java.io.InputStream;
import java.util.jar.JarEntry;

/*
 * Used in conjunction with a {@link JarWriter}. Instances of JarEntryWriter
 * can be registered at a {@link JarWriter} and will be called in case
 * the {@link JarWriter} comes across an entry matching the given regular expression.
 */
/**
 * <p>JarEntryWriter interface.</p>
 *
 */
public interface JarEntryWriter {

  /**
   * Callback used for rewriting particular JAR entries. Return null to rewrite the original JAR entry.
   *
   * @param _entry a {@link java.util.jar.JarEntry} object.
   * @param _regex a {@link java.lang.String} object.
   * @return a {@link java.io.InputStream} object.
   */
  public InputStream getInputStream(String _regex, JarEntry _entry);
}
