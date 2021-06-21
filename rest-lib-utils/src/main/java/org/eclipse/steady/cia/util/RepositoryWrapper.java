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
package org.eclipse.steady.cia.util;

import java.nio.file.Path;
import java.util.Set;

import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.Artifact;

/**
 * <p>RepositoryWrapper interface.</p>
 */
public interface RepositoryWrapper {

  /**
   * Returns all programming languages supported by the respective package repo.
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<ProgrammingLanguage> getSupportedLanguages();

  /**
   * Returns true if the urls are configured (not null) in the class constructor
   *
   * @return a boolean.
   */
  public boolean isConfigured();

  /**
   * Returns all versions for the given group and artifact. Optionally only versions with either a certain
   * classifier and/or a certain packaging can be obtained.
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param classifier (optional) to filter on all versions having a certain classifier
   * @param packaging (optional) to filter on all versions having a certain packaging
   * @return all versions existing in the target repository.
   * @throws java.lang.Exception if any.
   */
  public Set<Artifact> getAllArtifactVersions(
      String group, String artifact, String classifier, String packaging) throws Exception;

  /**
   * Returns all versions greater than the one provided for the given group and artifact. If available the timestamp is used for comparing versions, otherwise an alphanumerical
   * comparison is used. Optionally only versions with either a certain
   * classifier and/or a certain packaging can be obtained.
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param greaterThanVersion a {@link java.lang.String} object.
   * @param classifier (optional) to filter on all versions having a certain classifier
   * @param packaging (optional) to filter on all versions having a certain packaging
   * @return all versions greater than the one provided in argument greaterThanVersion
   * @throws java.lang.Exception if any.
   */
  public Set<Artifact> getGreaterArtifactVersions(
      String group, String artifact, String greaterThanVersion, String classifier, String packaging)
      throws Exception;

  /**
   * Returns the latest version for the given group and artifact. Optionally only versions with either a certain
   * classifier and/or a certain packaging can be obtained.
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param classifier (optional) to filter on all versions having a certain classifier
   * @param packaging (optional) to filter on all versions having a certain packaging
   * @return the latest version
   * @throws java.lang.Exception if any.
   */
  public Artifact getLatestArtifactVersion(
      String group, String artifact, String classifier, String packaging) throws Exception;

  /**
   * Returns the artifact for the given group, artifact and version. Optionally only versions with either a certain
   * classifier and/or a certain packaging can be obtained.
   *
   * @param group a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param classifier a {@link java.lang.String} object.
   * @param packaging a {@link java.lang.String} object.
   * @param lang a {@link org.eclipse.steady.shared.enums.ProgrammingLanguage} object.
   * @return a {@link org.eclipse.steady.shared.json.model.Artifact} object.
   * @throws java.lang.Exception if any.
   */
  public Artifact getArtifactVersion(
      String group,
      String artifact,
      String version,
      String classifier,
      String packaging,
      ProgrammingLanguage lang)
      throws Exception;

  /**
   * Downloads the artifact a and returns the path where it stores it (null otherwise).
   *
   * @param a a {@link org.eclipse.steady.shared.json.model.Artifact} object.
   * @throws java.lang.Exception if any.
   * @return a {@link java.nio.file.Path} object.
   */
  public Path downloadArtifact(Artifact a) throws Exception;

  /**
   * Returns the artifact having the digest d
   *
   * @param digest a {@link java.lang.String} object.
   * @throws org.eclipse.steady.cia.util.RepoException if any.
   * @throws java.lang.InterruptedException if any.
   * @return a {@link org.eclipse.steady.shared.json.model.Artifact} object.
   */
  public Artifact getArtifactForDigest(String digest) throws RepoException, InterruptedException;
}
