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
package org.eclipse.steady.backend.util;

import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.LibraryId;
import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loops over available implementations of {@link DigestVerifier} in order to verify the digest of a given {@link Library}.
 */
public class DigestVerifierEnumerator implements DigestVerifier {

  private static Logger log = LoggerFactory.getLogger(DigestVerifierEnumerator.class);

  private String url = null;

  /** Release timestamp of the given digest (null if unknown). */
  private java.util.Calendar timestamp;

  /** {@inheritDoc} */
  @Override
  public Set<ProgrammingLanguage> getSupportedLanguages() {
    final Set<ProgrammingLanguage> l = new HashSet<ProgrammingLanguage>();
    final ServiceLoader<DigestVerifier> loader = ServiceLoader.load(DigestVerifier.class);
    for (DigestVerifier dv : loader) {
      l.addAll(dv.getSupportedLanguages());
    }
    return l;
  }

  /** {@inheritDoc} */
  @Override
  public Set<DigestAlgorithm> getSupportedDigestAlgorithms() {
    final Set<DigestAlgorithm> l = new HashSet<DigestAlgorithm>();
    final ServiceLoader<DigestVerifier> loader = ServiceLoader.load(DigestVerifier.class);
    for (DigestVerifier dv : loader) {
      l.addAll(dv.getSupportedDigestAlgorithms());
    }
    return l;
  }

  /** {@inheritDoc} */
  @Override
  public String getVerificationUrl() {
    return url;
  }

  /** {@inheritDoc} */
  @Override
  public java.util.Calendar getReleaseTimestamp() {
    return this.timestamp;
  }

  /**
   * {@inheritDoc}
   *
   * Loops over available implementations of {@link DigestVerifier} in order to verify the digest of a given {@link Library}.
   */
  public List<LibraryId> verify(Library _lib) throws VerificationException {
    if (_lib == null || _lib.getDigest() == null)
      throw new IllegalArgumentException("No library or digest provided: [" + _lib + "]");

    // Will only have a list of LibraryIds or an empty list if either one verifier returns (thus, no
    // exception happened)
    List<LibraryId> verified = null;
    int exception_count = 0;

    // Perform the loop
    final ServiceLoader<DigestVerifier> loader = ServiceLoader.load(DigestVerifier.class);
    for (DigestVerifier l : loader) {
      // Check that programming language and digest alg match (in order to avoid a couple of
      // queries)
      final CollectionUtil<ProgrammingLanguage> u = new CollectionUtil<ProgrammingLanguage>();
      final Set<ProgrammingLanguage> developed_in = _lib.getDevelopedIn();
      if ((developed_in.isEmpty() || u.haveIntersection(developed_in, l.getSupportedLanguages()))
          && l.getSupportedDigestAlgorithms().contains(_lib.getDigestAlgorithm())) {
        try {
          verified = l.verify(_lib);
          if (verified != null && verified.size() > 0) {
            this.url = l.getVerificationUrl();
            this.timestamp = l.getReleaseTimestamp();
            break;
          }
        } catch (VerificationException e) {
          exception_count++;
          log.error(e.getMessage());
        }
      }
    }

    // Return null if an exception happened, verified otherwise
    return (exception_count == 0 ? verified : null);
  }
}
