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
package com.sap.psr.vulas.cia.util;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.java.JarAnalyzer;
import com.sap.psr.vulas.python.PythonArchiveAnalyzer;
import com.sap.psr.vulas.shared.cache.Cache;
import com.sap.psr.vulas.shared.cache.CacheException;
import com.sap.psr.vulas.shared.cache.ObjectFetcher;
import com.sap.psr.vulas.shared.json.model.Artifact;

/**
 * <p>FileAnalyzerFetcher class.</p>
 *
 */
public class FileAnalyzerFetcher implements ObjectFetcher<Artifact, FileAnalyzer> {

  private static Logger log = LoggerFactory.getLogger(FileAnalyzerFetcher.class);

  private static Cache<Artifact, FileAnalyzer> ANALYZERS_CACHE =
      new Cache<Artifact, FileAnalyzer>(new FileAnalyzerFetcher(), 10080, 50);

  /**
   * <p>read.</p>
   *
   * @param _key a {@link com.sap.psr.vulas.shared.json.model.Artifact} object.
   * @return a {@link com.sap.psr.vulas.FileAnalyzer} object.
   * @throws com.sap.psr.vulas.shared.cache.CacheException if any.
   */
  public static FileAnalyzer read(Artifact _key) throws CacheException {
    return ANALYZERS_CACHE.get(_key);
  }

  /** {@inheritDoc} */
  @Override
  public FileAnalyzer fetch(Artifact _key) throws CacheException {
    // The artifact whose JAR is to be downloaded
    _key.setClassifier(null);

    RepositoryDispatcher r = new RepositoryDispatcher();
    Path file = null;
    try {
      file = r.downloadArtifact(_key);
    } catch (IllegalArgumentException ie) {
      throw new CacheException("Artifact [" + _key + "] not ready for download", ie);
    } catch (Exception e) {
      throw new CacheException("Cannot download [" + _key + "]", e);
    }

    FileAnalyzer fa = new JarAnalyzer();
    try {
      if (fa.canAnalyze(file.toFile())) {
        fa.analyze(file.toFile());
      } else {
        fa = new PythonArchiveAnalyzer();
        fa.analyze(file.toFile());
        fa.getConstructs();
      }
    } catch (FileAnalysisException fe) {
      throw new CacheException("Cannot Analyze file [" + file.toString() + "]", fe);
    }
    return fa;
  }
}
