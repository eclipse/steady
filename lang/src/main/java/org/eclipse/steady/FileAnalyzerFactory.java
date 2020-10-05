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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.util.FileUtil;

/**
 * Creates instances of {@link FileAnalyzer}.
 */
public class FileAnalyzerFactory {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static String[] supportedFileExtensions = null;

  /**
   * Loops over all {@link FileAnalyzer}s and returns an array of all supported file extensions.
   *
   * @return an array of {@link java.lang.String} objects.
   */
  public static synchronized String[] getSupportedFileExtensions() {
    if (supportedFileExtensions == null) {
      final Set<String> exts = new HashSet<String>();
      final ServiceLoader<FileAnalyzer> loader = ServiceLoader.load(FileAnalyzer.class);
      for (FileAnalyzer l : loader) {
        final String[] ana_exts = l.getSupportedFileExtensions();
        for (String ana_ext : ana_exts) {
          if (exts.contains(ana_ext))
            log.warn("File extension [" + ana_ext + "] supported multiple times");
          else exts.add(ana_ext);
        }
      }
      supportedFileExtensions = new String[exts.size()];
      new ArrayList<String>(exts).toArray(supportedFileExtensions);
    }
    return supportedFileExtensions.clone();
  }

  /**
   * <p>isSupportedFileExtension.</p>
   *
   * @param _ext a {@link java.lang.String} object.
   * @return a boolean.
   */
  public static boolean isSupportedFileExtension(String _ext) {
    return Arrays.asList(getSupportedFileExtensions()).contains(_ext);
  }

  /**
   * Creates instances of {@link FileAnalyzer} depending on the file.
   *
   * @param _file a {@link java.io.File} object.
   * @return a {@link org.eclipse.steady.FileAnalyzer} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public static final FileAnalyzer buildFileAnalyzer(File _file) throws IllegalArgumentException {
    return FileAnalyzerFactory.buildFileAnalyzer(_file, null);
  }

  /**
   * Creates instances of {@link FileAnalyzer} depending on the file.
   *
   * @param _file a {@link java.io.File} object.
   * @param _exts an array of {@link java.lang.String} objects.
   * @return a {@link org.eclipse.steady.FileAnalyzer} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public static final FileAnalyzer buildFileAnalyzer(File _file, String[] _exts)
      throws IllegalArgumentException {
    FileAnalyzer fa = null;

    // Check implementations of FileAnalyzer service
    if (FileUtil.isAccessibleDirectory(_file) || FileUtil.isAccessibleFile(_file.toPath())) {
      final ServiceLoader<FileAnalyzer> loader = ServiceLoader.load(FileAnalyzer.class);
      for (FileAnalyzer l : loader) {
        if (l.canAnalyze(_file)) {
          try {
            fa = l;

            // This is to limit the search of the DirAnalyzer to given extensions
            if (_exts != null && fa instanceof DirAnalyzer)
              ((DirAnalyzer) fa).setExtensionFilter(_exts);

            fa.analyze(_file);
          } catch (FileAnalysisException e) {
            log.error(
                "Error when creating file analyzer for ["
                    + _file.toString()
                    + "]: "
                    + e.getMessage(),
                e);
          }
        }

        if (fa != null) break;
      }
    }
    // Not accessible
    else
      throw new IllegalArgumentException(
          "File [" + _file.toString() + "] not found or not readable");

    if (fa == null) log.warn("No analyzer found for file  [" + _file.toString() + "]");

    return fa;
  }
}
