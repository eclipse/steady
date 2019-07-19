package com.sap.psr.vulas.vcs;

/**
 * Thrown to indicate that no IVCSClient could be instantiated for the given URL.
 */
public class NoRepoClientException extends Exception {
	/**
	 * <p>Constructor for NoRepoClientException.</p>
	 *
	 * @param _url a {@link java.lang.String} object.
	 */
	public NoRepoClientException(String _url) {
		super("No VCS client found for URL " + _url);
	}
}
