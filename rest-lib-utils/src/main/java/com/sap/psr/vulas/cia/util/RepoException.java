package com.sap.psr.vulas.cia.util;

/**
 * Indicates a connection problem with Maven repository.
 *
 */
public class RepoException extends Exception {
	public RepoException(String _msg) { super(_msg); }
	public RepoException(String _msg, Throwable _cause) { super(_msg, _cause); }
}
