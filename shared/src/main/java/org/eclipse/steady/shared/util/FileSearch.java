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
package org.eclipse.steady.shared.util;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.logging.log4j.Logger;

/**
 * Searches for all files having a given extension below one or multiple directories.
 */
public class FileSearch extends AbstractFileSearch {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private String[] suffixes = null;

  private long maxSize = -1;

  /**
   * <p>Constructor for FileSearch.</p>
   *
   * @param _s an array with accepted file extensions
   * @throws java.lang.IllegalArgumentException if any.
   */
  public FileSearch(String[] _s) throws IllegalArgumentException {
    this(_s, -1);
  }

  /**
   * <p>Constructor for FileSearch.</p>
   *
   * @param _s an array with accepted file extensions
   * @param _size maximum accepted file size (no limit, if a value &lt;= 0 is provided)
   * @throws java.lang.IllegalArgumentException if any.
   */
  public FileSearch(String[] _s, long _size) throws IllegalArgumentException {
    if (_s == null || _s.length == 0)
      throw new IllegalArgumentException("At least one file extension must be provided");
    this.suffixes = _s.clone();
    if (_size > 0) this.maxSize = _size;
  }

  /**
   * <p>Getter for the field <code>suffixes</code>.</p>
   *
   * @return an array of {@link java.lang.String} objects.
   */
  public String[] getSuffixes() {
    return this.suffixes.clone();
  }

  /** {@inheritDoc} */
  @Override
  public FileVisitResult visitFile(Path _f, BasicFileAttributes attrs) {
    if (!this.foundFile(_f) && FileUtil.hasFileExtension(_f, this.suffixes)) {
      if (this.maxSize == -1) {
        this.addFile(_f);
      } else {
        if (_f.toFile().length() < this.maxSize) {
          this.addFile(_f);
        } else {
          log.warn(
              "File ["
                  + _f.toAbsolutePath()
                  + "] ignored because it exceeds the maximum accepted size ["
                  + _f.toFile().length()
                  + " > "
                  + this.maxSize
                  + "] bytes");
        }
      }
    }
    return FileVisitResult.CONTINUE;
  }
}
