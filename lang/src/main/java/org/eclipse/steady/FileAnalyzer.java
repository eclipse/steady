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
package org.eclipse.steady;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Analyzes a given file or directory in order to search for contained programming constructs.
 * Instances of {@link FileAnalyzer} are built by {@link FileAnalyzerFactory}.
 * {@link FileAnalyzer}s can be nested and form a tree structure, which is addressed by
 * methods {@link #hasChilds()} and {@link #getChilds(boolean)}.
 */
public interface FileAnalyzer {

  /**
   * Returns all supported file extensions.
   *
   * @return an array of {@link java.lang.String} objects.
   */
  public String[] getSupportedFileExtensions();

  /**
   * Returns true if the given {@link File} can be analyzed, false otherwise.
   *
   * @param _file a {@link java.io.File} object.
   * @return a boolean.
   */
  public boolean canAnalyze(File _file);

  /**
   * Sets the {@link File} to be analyzed. Note that this does not necessarily trigger the analysis.
   *
   * @param _file a {@link java.io.File} object.
   * @throws org.eclipse.steady.FileAnalysisException if any.
   */
  public void analyze(File _file) throws FileAnalysisException;

  /**
   * Searches for programming {@link Construct}s contained in a given {@link File} and returns them to the caller.
   * The {@link File} to be analyzed is not specified using the methods of the interface. Typically, it is passed
   * to the constructor of the respective implementation.
   *
   * @return a {@link java.util.Map} object.
   * @throws org.eclipse.steady.FileAnalysisException if any.
   */
  public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException;

  /**
   * Returns true if the given {@link ConstructId} is part of the analyzed file, false otherwise.
   *
   * @param _id a {@link org.eclipse.steady.ConstructId} object.
   * @return a boolean.
   * @throws org.eclipse.steady.FileAnalysisException if any.
   */
  public boolean containsConstruct(ConstructId _id) throws FileAnalysisException;

  /**
   * Returns the {@link Construct} with the given {@link ConstructId} or null if no such construct is present in the analyzed file.
   *
   * @param _id a {@link org.eclipse.steady.ConstructId} object.
   * @return a {@link org.eclipse.steady.Construct} object.
   * @throws org.eclipse.steady.FileAnalysisException if any.
   */
  public Construct getConstruct(ConstructId _id) throws FileAnalysisException;

  /**
   * Returns true if there are any child {@link FileAnalyzer}s, false otherwise.
   * Some analyzers will never have any childs, e.g., those that parse single source code files.
   * In case of other analyzes, e.g., the {@link DirAnalyzer}, it depends on whether there are
   * any "analyzable" files.
   *
   * @return a boolean.
   */
  public boolean hasChilds();

  /**
   * Returns nested {@link FileAnalyzer}s or null if no child analyzers exist.
   * Depending on the argument, only direct childs or also indirect childs will be returned.
   *
   * @param _recursive a boolean.
   * @return a {@link java.util.Set} object.
   */
  public Set<FileAnalyzer> getChilds(boolean _recursive);

  /** {@inheritDoc} */
  @Override
  int hashCode();

  /** {@inheritDoc} */
  @Override
  boolean equals(Object obj);
}
