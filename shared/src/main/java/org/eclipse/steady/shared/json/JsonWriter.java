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
 * Serializes a given object and writes it to disk.
 *
 * @param <T> type of objects handled by the writer.
 */
public class JsonWriter<T> {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * <p>write.</p>
   *
   * @param _object a T object.
   * @param _path a {@link java.nio.file.Path} object.
   * @return a boolean.
   */
  public boolean write(T _object, Path _path) {
    boolean success = false;
    if (_object != null && _path != null) {
      // Serialize
      final String json = JacksonUtil.asJsonString(_object);
      // Write
      try {
        FileUtil.writeToFile(_path.toFile(), json);
        success = true;
      } catch (IOException e) {
        log.error("Error writing to file [" + _path + "]: " + e.getMessage(), e);
      }
    }
    return success;
  }
}
