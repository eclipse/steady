package com.sap.psr.vulas.backend.util;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.util.CollectionUtil;

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
		for(DigestVerifier dv: loader) {					
			l.addAll(dv.getSupportedLanguages());
		}
		return l;
	}

	/** {@inheritDoc} */
	@Override
	public Set<DigestAlgorithm> getSupportedDigestAlgorithms() {
		final Set<DigestAlgorithm> l = new HashSet<DigestAlgorithm>();
		final ServiceLoader<DigestVerifier> loader = ServiceLoader.load(DigestVerifier.class);
		for(DigestVerifier dv: loader) {					
			l.addAll(dv.getSupportedDigestAlgorithms());
		}
		return l;
	}

	/** {@inheritDoc} */
	@Override
	public String getVerificationUrl() { return url; }
	
	/** {@inheritDoc} */
	@Override
	public java.util.Calendar getReleaseTimestamp() { return this.timestamp; }

	/**
	 * {@inheritDoc}
	 *
	 * Loops over available implementations of {@link DigestVerifier} in order to verify the digest of a given {@link Library}.
	 */
	public Boolean verify(Library _lib) throws VerificationException {
		if(_lib==null || _lib.getDigest()==null)
			throw new IllegalArgumentException("No library or digest provided: [" + _lib + "]");

		// Will only have a true or false value if either one verifier returns true, or all returned false (thus, no exception happened)
		Boolean verified = null;
		int exception_count = 0;

		// Perform the loop
		final ServiceLoader<DigestVerifier> loader = ServiceLoader.load(DigestVerifier.class);
		for(DigestVerifier l: loader) {
			// Check that programming language and digest alg match (in order to avoid a couple of queries)
			final CollectionUtil<ProgrammingLanguage> u = new CollectionUtil<ProgrammingLanguage>();
			final Set<ProgrammingLanguage> developed_in = _lib.getDevelopedIn();
			if( (developed_in.isEmpty() || u.haveIntersection(developed_in, l.getSupportedLanguages())) &&
					l.getSupportedDigestAlgorithms().contains(_lib.getDigestAlgorithm())) {
				try {
					verified = l.verify(_lib);
					if(verified!=null && verified) {
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
		return ( exception_count==0 ? verified : null);
	}
}
