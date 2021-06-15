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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarFile;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.shared.util.FileUtil;

/**
 * <p>AarAnalyzer class.</p>
 */
@NotThreadSafe
public class AarAnalyzer extends JarAnalyzer {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static final String CLASSES_JAR = "classes.jar";

  // private JarFile aar;
  private JarWriter aarWriter;

  private Path tmpDir = null; // To where the AAR is extracted

  /** {@inheritDoc} */
  @Override
  public String[] getSupportedFileExtensions() {
    return new String[] {"aar"};
  }

  /** {@inheritDoc} */
  @Override
  public synchronized void analyze(final File _file) throws FileAnalysisException {
    try {
      // this.aar = new JarFile(_file, false, java.util.zip.ZipFile.OPEN_READ);
      this.aarWriter = new JarWriter(_file.toPath());
      this.url = _file.getAbsolutePath().toString();

      try {
        this.tmpDir = java.nio.file.Files.createTempDirectory("aar_analysis_");
      } catch (IOException e) {
        throw new IllegalStateException("Unable to create temp directory", e);
      }

      this.aarWriter.extract(this.tmpDir);

      // TODO: What if no classes.jar
      final File classes_jar = this.tmpDir.resolve(CLASSES_JAR).toFile();
      if (classes_jar != null && FileUtil.isAccessibleFile(classes_jar.toPath())) {
        JarAnalyzer.insertClasspath(classes_jar.toPath().toAbsolutePath().toString());
        this.jar = new JarFile(classes_jar, false, java.util.zip.ZipFile.OPEN_READ);
        this.jarWriter = new JarWriter(classes_jar.toPath());
      } else {
        log.warn("No " + CLASSES_JAR + " found in [" + _file.toPath().toAbsolutePath() + "]");
      }
    } catch (IllegalStateException e) {
      log.error("IllegalStateException when analyzing file [" + _file + "]: " + e.getMessage());
      throw new FileAnalysisException(
          "Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
    } catch (IOException e) {
      log.error("IOException when analyzing file [" + _file + "]: " + e.getMessage());
      throw new FileAnalysisException(
          "Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Exception when analyzing file [" + _file + "]: " + e.getMessage());
      throw new FileAnalysisException(
          "Error when analyzing file [" + _file + "]: " + e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * Returns the SHA1 digest of the AAR by computing it on the fly.
   */
  @Override
  public synchronized String getSHA1() {
    return this.aarWriter.getSHA1();
  }

  /**
   * <p>getFileName.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFileName() {
    return this.aarWriter.getOriginalJarFileName().toString(); // + "!" + CLASSES_JAR;
  }
}
