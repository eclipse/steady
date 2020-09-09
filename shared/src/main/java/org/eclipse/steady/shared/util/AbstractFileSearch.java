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

import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;

/**
 * <p>AbstractFileSearch class.</p>
 *
 */
public class AbstractFileSearch extends SimpleFileVisitor<Path> {

  private static Logger log = null;

  private static final Logger getLog() {
    if (AbstractFileSearch.log == null)
      AbstractFileSearch.log = org.apache.logging.log4j.LogManager.getLogger();
    return AbstractFileSearch.log;
  }

  private final Set<Path> files = new TreeSet<Path>();

  /**
   * <p>foundFile.</p>
   *
   * @param _p a {@link java.nio.file.Path} object.
   * @return a boolean.
   */
  protected boolean foundFile(Path _p) {
    return this.files.contains(_p);
  }

  /**
   * <p>addFile.</p>
   *
   * @param _p a {@link java.nio.file.Path} object.
   */
  protected void addFile(Path _p) {
    this.files.add(_p);
  }

  /**
   * Clears previous search results.
   */
  public void clear() {
    this.files.clear();
  }

  /**
   * Starts searching for files in the given paths, with unlimited depth.
   *
   * @param _paths the FS path to search in
   * @return a set of paths for all files found
   */
  public Set<Path> search(Set<Path> _paths) {
    return this.search(_paths, java.lang.Integer.MAX_VALUE);
  }

  /**
   * Starts searching for files in the given paths.
   *
   * @param _paths one or multiple FS paths to search in
   * @param _depth the depth of nested directories to search in
   * @return a set of paths for all files found
   */
  public Set<Path> search(Set<Path> _paths, int _depth) {
    for (Path p : _paths) this.search(p, _depth);
    AbstractFileSearch.getLog()
        .info(
            "Found a total of [" + this.files.size() + "] files in [" + _paths.size() + "] paths");
    return this.files;
  }

  /**
   * Starts searching for files in the given path, with unlimited depth.
   *
   * @param _p the FS path to search in
   * @return a set of paths for all files found
   */
  public Set<Path> search(Path _p) {
    return this.search(_p, java.lang.Integer.MAX_VALUE);
  }

  /**
   * Starts searching for files in the given path, up to the specified depth.
   *
   * @param _p the FS path to search in
   * @param _depth the depth of nested directories to search in
   * @return a set of paths for all files found
   */
  public Set<Path> search(@NotNull Path _p, int _depth) {
    try {
      if (Files.isDirectory(_p))
        Files.walkFileTree(_p, new HashSet<FileVisitOption>(), _depth, this);
      else if (Files.isRegularFile(_p))
        this.visitFile(_p, Files.readAttributes(_p, BasicFileAttributes.class));
    } catch (Exception e) {
      AbstractFileSearch.getLog()
          .error("Error while analyzing path [" + _p + "]: " + e.getMessage());
    }
    if (_p.isAbsolute())
      AbstractFileSearch.getLog()
          .info(
              "Found ["
                  + this.files.size()
                  + "] files in absolute path ["
                  + _p.toAbsolutePath()
                  + "]");
    else
      AbstractFileSearch.getLog()
          .info(
              "Found ["
                  + this.files.size()
                  + "] files in relative path ["
                  + _p
                  + "], i.e., absolute path ["
                  + _p.toAbsolutePath()
                  + "]");
    return this.files;
  }
}
