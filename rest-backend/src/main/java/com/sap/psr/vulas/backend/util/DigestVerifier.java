package com.sap.psr.vulas.backend.util;

import java.util.Set;

import com.sap.psr.vulas.backend.model.Library;
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
	 * Returns true or false depending on whether the digest is known to the respective package repo.
	 *
	 * @throws com.sap.psr.vulas.backend.util.VerificationException if the verification URL could not be reached, the HTTP response was malformed or similar
	 * @param _lib a {@link com.sap.psr.vulas.backend.model.Library} object.
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean verify(Library _lib) throws VerificationException;
}
