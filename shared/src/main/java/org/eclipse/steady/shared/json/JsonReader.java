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
package org.eclipse.steady.shared.json;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.util.FileUtil;

/**
 * Reads a serialized object from disk.
 *
 * @param <T> type of objects handled by the reader.
 */
public class JsonReader<T> {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(JsonReader.class);

  private Class<T> clazz;

  /**
   * <p>Constructor for JsonReader.</p>
   *
   * @param _clazz a {@link java.lang.Class} object.
   */
  public JsonReader(Class<T> _clazz) {
    this.clazz = _clazz;
  }

  /**
   * <p>read.</p>
   *
   * @param _path a {@link java.nio.file.Path} object.
   * @return a T object.
   */
  @SuppressWarnings("unchecked")
  public T read(Path _path) {
    T object = null;
    if (FileUtil.isAccessibleFile(_path)) {
      try {
        final String json = FileUtil.readFile(_path);
        object = (T) JacksonUtil.asObject(json, this.clazz);
      } catch (IOException e) {
        log.error("Error reading from file [" + _path + "]: " + e.getMessage(), e);
      } catch (ClassCastException e) {
        log.error("Error reading from file [" + _path + "]: " + e.getMessage(), e);
      } catch (Exception e) {
        log.error("Error reading from file [" + _path + "]: " + e.getMessage(), e);
      }
    }
    return object;
  }
}
