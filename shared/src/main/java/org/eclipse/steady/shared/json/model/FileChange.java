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
package org.eclipse.steady.shared.json.model;

import java.io.File;

import org.eclipse.steady.shared.util.FileUtil;

/**
 * Represents the change of a file that is managed by a version control system.
 */
public class FileChange {
  public enum Type {
    DEL,
    MOD,
    ADD
  };

  private File oldFile = null, newFile = null;

  /**
   * The repo.
   */
  private String repo = null;

  /**
   * The file path in the source code repository, relative to the repo's root.
   */
  private String repoPath = null;

  /**
   * <p>Constructor for FileChange.</p>
   *
   * @param _repo a {@link java.lang.String} object.
   * @param _path a {@link java.lang.String} object.
   * @param _o a {@link java.io.File} object.
   * @param _n a {@link java.io.File} object.
   */
  public FileChange(String _repo, String _path, File _o, File _n) {
    this.repo = _repo;
    this.repoPath = _path;
    this.oldFile = _o;
    this.newFile = _n;
  }

  /**
   * Returns the type of change, either an addition, a modification or a deletion.
   *
   * @return a {@link Type} object.
   */
  public Type getType() {
    if (this.oldFile == null) return Type.ADD;
    else if (this.newFile == null) return Type.DEL;
    else return Type.MOD;
  }

  /**
   * <p>Getter for the field <code>oldFile</code>.</p>
   *
   * @return a {@link java.io.File} object.
   */
  public File getOldFile() {
    return this.oldFile;
  }
  /**
   * <p>Getter for the field <code>newFile</code>.</p>
   *
   * @return a {@link java.io.File} object.
   */
  public File getNewFile() {
    return this.newFile;
  }
  /**
   * <p>Getter for the field <code>repo</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepo() {
    return this.repo;
  }
  /**
   * <p>Getter for the field <code>repoPath</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepoPath() {
    return this.repoPath;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "FileChange [type="
        + this.getType()
        + ", repo="
        + this.repo
        + ", path="
        + this.repoPath
        + ", old="
        + this.oldFile
        + ", new="
        + this.newFile
        + "]";
  }

  /**
   * Returns the file extension of the file concerned by the change.
   *
   * @return a {@link java.lang.String} object.
   * @throws java.lang.IllegalStateException if any.
   */
  public String getFileExtension() throws IllegalStateException {
    if (this.getOldFile() != null) return FileUtil.getFileExtension(this.getOldFile());
    else if (this.getNewFile() != null) return FileUtil.getFileExtension(this.getNewFile());
    else throw new IllegalStateException("Both old and new file are null");
  }
}
