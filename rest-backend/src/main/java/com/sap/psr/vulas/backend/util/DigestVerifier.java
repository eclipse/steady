package com.sap.psr.vulas.backend.util;

import java.util.Set;

import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

public interface DigestVerifier {
	
	/**
	 * Returns all programming languages supported by the respective package repo.
	 * @return
	 */
	public Set<ProgrammingLanguage> getSupportedLanguages();
	
	/**
	 * Returns all digest algorithms supported by the respective package repo.
	 * @return
	 */
	public Set<DigestAlgorithm> getSupportedDigestAlgorithms();
	
	/**
	 * Returns the URL used to verify the digest.
	 * Returns null if the verification did not succeed.
	 * @return
	 */
	public String getVerificationUrl();
	
	/**
	 * Returns the release timestamp of the given digest (milliseconds, between the current time and midnight, January 1, 1970 UTC).
	 * Returns null if the verification did not succeed.
	 * @see System#currentTimeMillis()
	 */
	public Long getReleaseTimestamp();

	/**
	 * Returns null if the verification did not succeed, e.g., due to connectivity issues.
	 * Returns true or false depending on whether the digest is known to the respective package repo.
	 * @throws VerificationException if the verification URL could not be reached, the HTTP response was malformed or similar
	 */
	public Boolean verify(Library _lib) throws VerificationException;
}
