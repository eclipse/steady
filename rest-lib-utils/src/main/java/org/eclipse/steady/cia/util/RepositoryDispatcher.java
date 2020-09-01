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
package org.eclipse.steady.cia.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.steady.shared.connectivity.ServiceConnectionException;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Artifact;
import org.eclipse.steady.shared.json.model.LibraryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>RepositoryDispatcher class.</p>
 *
 */
public class RepositoryDispatcher implements RepositoryWrapper {

  private static Logger log = LoggerFactory.getLogger(RepositoryDispatcher.class);

  /** {@inheritDoc} */
  @Override
  public Set<ProgrammingLanguage> getSupportedLanguages() {
    final Set<ProgrammingLanguage> l = new HashSet<ProgrammingLanguage>();
    final ServiceLoader<RepositoryWrapper> loader = ServiceLoader.load(RepositoryWrapper.class);
    for (RepositoryWrapper dv : loader) {
      l.addAll(dv.getSupportedLanguages());
    }
    return l;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Artifact> getAllArtifactVersions(
      String group, String artifact, String classifier, String packaging) throws Exception {

    Set<Artifact> result = new TreeSet<Artifact>();
    Set<LibraryId> lids = new TreeSet<LibraryId>();
    final ServiceLoader<RepositoryWrapper> loader = ServiceLoader.load(RepositoryWrapper.class);
    Set<Artifact> partial_result = null;
    for (RepositoryWrapper dv : loader) {
      if (dv.isConfigured()) {
        try {
          partial_result = dv.getAllArtifactVersions(group, artifact, classifier, packaging);
          if (partial_result != null && !partial_result.isEmpty()) {
            for (Artifact a : partial_result) {
              if (!lids.contains(a.getLibId())) {
                result.add(a);
                lids.add(a.getLibId());
              }
            }
          }
        } catch (NotImplementedException e) {

        } catch (UnsupportedOperationException e) {
        }
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Artifact> getGreaterArtifactVersions(
      String group, String artifact, String greaterThanVersion, String classifier, String packaging)
      throws Exception {
    Set<Artifact> result = null;
    final ServiceLoader<RepositoryWrapper> loader = ServiceLoader.load(RepositoryWrapper.class);
    for (RepositoryWrapper dv : loader) {
      if (dv.isConfigured()) {
        try {
          result =
              dv.getGreaterArtifactVersions(
                  group, artifact, greaterThanVersion, classifier, packaging);
          if (result != null && !result.isEmpty()) break;
        } catch (NotImplementedException e) {

        } catch (UnsupportedOperationException e) {
        } catch (ServiceConnectionException e) {
          log.error("Invoked service was unavailable: " + e.getMessage());
        }
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Artifact getLatestArtifactVersion(
      String group, String artifact, String classifier, String packaging) throws Exception {
    Artifact result = null;
    final ServiceLoader<RepositoryWrapper> loader = ServiceLoader.load(RepositoryWrapper.class);

    for (RepositoryWrapper dv : loader) {
      if (dv.isConfigured()) {
        try {
          result = dv.getLatestArtifactVersion(group, artifact, classifier, packaging);
          if (result != null) break;
        } catch (NotImplementedException e) {

        } catch (UnsupportedOperationException e) {
        }
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Artifact getArtifactVersion(
      String group,
      String artifact,
      String version,
      String classifier,
      String packaging,
      ProgrammingLanguage lang)
      throws Exception {
    Artifact result = null;
    final ServiceLoader<RepositoryWrapper> loader = ServiceLoader.load(RepositoryWrapper.class);
    for (RepositoryWrapper dv : loader) {
      if (dv.isConfigured()
          && (lang == null
              || lang.toString().equals("")
              || dv.getSupportedLanguages().contains(lang))) {
        try {
          result = dv.getArtifactVersion(group, artifact, version, classifier, packaging, null);
          if (result != null) break;
        } catch (NotImplementedException e) {

        } catch (UnsupportedOperationException e) {
        }
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Path downloadArtifact(Artifact a) throws Exception {
    Path p = null;

    if (!a.isReadyForDownload())
      throw new IllegalArgumentException("Artifact not fully specified: " + a);

    // Already downloaded?
    if (a.isCached()) {
      log.debug(a.toString() + " available in local m2 repo: [" + a.getAbsM2Path() + "]");
      p = a.getAbsM2Path();

      try {
        if (a.getProgrammingLanguage() == ProgrammingLanguage.JAVA) {
          JarFile j = new JarFile(p.toFile(), false, java.util.zip.ZipFile.OPEN_READ);
          j.close();
        } else if (a.getProgrammingLanguage() == ProgrammingLanguage.PY
            && a.getPackaging().equals("sdist")) {
          TarArchiveInputStream t =
              new TarArchiveInputStream(
                  new GzipCompressorInputStream(
                      new BufferedInputStream(new FileInputStream(p.toFile()))));
          t.getNextTarEntry();
          t.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
        log.error("Exception when opening archive file [" + p.toFile() + "]: " + e.getMessage());
        boolean deleted = p.toFile().delete();
        if (!deleted) log.warn("Couldn't delete presumibly corrupted archive [" + p + "]");
        p = this.downloadArtifactFile(a);
      }
    }
    // No, download!
    else {
      p = this.downloadArtifactFile(a);
    }
    return p;
  }

  private Path downloadArtifactFile(Artifact a) throws Exception {
    Path p = null;
    // Create the dir
    Path artifact_dir = a.getAbsM2Path().getParent();
    if (!artifact_dir.toFile().exists()) Files.createDirectories(artifact_dir);

    final ServiceLoader<RepositoryWrapper> loader = ServiceLoader.load(RepositoryWrapper.class);
    for (RepositoryWrapper dv : loader) {
      if (dv.isConfigured() && (dv.getSupportedLanguages().contains(a.getProgrammingLanguage()))) {
        try {
          p = dv.downloadArtifact(a);
          if (p != null) break;
        } catch (NotImplementedException e) {

        } catch (UnsupportedOperationException e) {
        }
      }
    }
    if (p == null) throw new FileNotFoundException();

    return p;
  }

  /** {@inheritDoc} */
  @Override
  public Artifact getArtifactForDigest(String digest) throws RepoException, InterruptedException {
    Artifact result = null;
    final ServiceLoader<RepositoryWrapper> loader = ServiceLoader.load(RepositoryWrapper.class);
    for (RepositoryWrapper dv : loader) {
      if (dv.isConfigured()) {
        try {
          result = dv.getArtifactForDigest(digest);
          if (result != null) break;
        } catch (NotImplementedException e) {

        } catch (UnsupportedOperationException e) {
        }
      }
    }
    return result;
  }

  // this method should never be called
  /** {@inheritDoc} */
  @Override
  public boolean isConfigured() {
    return false;
  }
}
