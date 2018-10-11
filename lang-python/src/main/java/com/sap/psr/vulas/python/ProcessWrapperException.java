package com.sap.psr.vulas.python;

import java.net.URI;

/**
 * Thrown to indicate a problem when calling OS-level services.
 */
public class ProcessWrapperException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ProcessWrapperException(String _message) {
		super(_message);
	}
	
	public ProcessWrapperException(String _message, Throwable _cause) {
		super(_message, _cause);
	}
}
