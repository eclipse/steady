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
package com.sap.psr.vulas.backend.util;

import java.util.List;
import java.util.Set;

import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

/**
 * <p>DigestVerifier interface.</p>
 *
 */
public interface DigestVerifier {

    /**
     * Returns all programming languages supported by the respective package repo.
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<ProgrammingLanguage> getSupportedLanguages();

    /**
     * Returns all digest algorithms supported by the respective package repo.
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<DigestAlgorithm> getSupportedDigestAlgorithms();

    /**
     * Returns the URL used to verify the digest.
     * Returns null if the verification did not succeed.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVerificationUrl();

    /**
     * Returns the release timestamp of the given digest (milliseconds, between the current time and midnight, January 1, 1970 UTC).
     * Returns null if the verification did not succeed.
     *
     * @see System#currentTimeMillis()
     * @return a {@link java.util.Calendar} object.
     */
    public java.util.Calendar getReleaseTimestamp();

    /**
     * Returns null if the verification did not succeed, e.g., due to connectivity issues.
     * Returns the list of verified LibraryId if the digest is known to the respective package repo (an empty list otherwise).
     *
     * @throws com.sap.psr.vulas.backend.util.VerificationException if the verification URL could not be reached, the HTTP response was malformed or similar
     * @param _lib a {@link com.sap.psr.vulas.backend.model.Library} object.
     * @return a {@link java.lang.Boolean} object.
     */
    public List<LibraryId> verify(Library _lib) throws VerificationException;
}
