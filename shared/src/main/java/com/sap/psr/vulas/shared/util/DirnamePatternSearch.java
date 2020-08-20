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
package com.sap.psr.vulas.shared.util;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

/**
 * Searches for all directories containing a file with a given name.
 */
public class DirnamePatternSearch extends AbstractFileSearch {

  private Pattern pattern = null;

  /**
   * <p>Constructor for DirnamePatternSearch.</p>
   *
   * @param _regex a {@link java.lang.String} object.
   */
  public DirnamePatternSearch(@NotNull String _regex) {
    this(Pattern.compile(_regex));
  }

  /**
   * <p>Constructor for DirnamePatternSearch.</p>
   *
   * @param _pattern a {@link java.util.regex.Pattern} object.
   */
  public DirnamePatternSearch(@NotNull Pattern _pattern) {
    this.pattern = _pattern;
  }

  /** {@inheritDoc} */
  @Override
  public FileVisitResult preVisitDirectory(Path _f, BasicFileAttributes attrs) {
    if (_f.toFile().isDirectory() && !this.foundFile(_f) && _f.getFileName() != null) {
      final Matcher m = this.pattern.matcher(_f.getFileName().toString());
      if (m.matches()) {
        this.addFile(_f);
      }
    }
    return FileVisitResult.CONTINUE;
  }
}
