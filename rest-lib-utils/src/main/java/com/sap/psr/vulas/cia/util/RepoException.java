package com.sap.psr.vulas.cia.util;

/**
 * Indicates a connection problem with Maven repository.
 */
public class RepoException extends Exception {
	/**
	 * <p>Constructor for RepoException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 */
	public RepoException(String _msg) { super(_msg); }
	/**
	 * <p>Constructor for RepoException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public RepoException(String _msg, Throwable _cause) { super(_msg, _cause); }
}
