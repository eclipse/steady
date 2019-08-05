package com.sap.psr.vulas.python;

import java.net.URI;

/**
 * Thrown to indicate a problem when calling OS-level services.
 */
public class ProcessWrapperException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * <p>Constructor for ProcessWrapperException.</p>
	 *
	 * @param _message a {@link java.lang.String} object.
	 */
	public ProcessWrapperException(String _message) {
		super(_message);
	}
	
	/**
	 * <p>Constructor for ProcessWrapperException.</p>
	 *
	 * @param _message a {@link java.lang.String} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public ProcessWrapperException(String _message, Throwable _cause) {
		super(_message, _cause);
	}
}
