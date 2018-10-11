package com.sap.psr.vulas.shared.connectivity;

import java.net.URI;

/**
 * Thrown to indicate a problem when calling RESTful services.
 */
public class ServiceConnectionException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private URI uri;
	
	public ServiceConnectionException(String _message, Throwable _cause) {
		super(_message, _cause);
	}
	
	public ServiceConnectionException(URI _uri, Throwable _cause) {
		super("Error calling [" + _uri + "]: " + _cause.getMessage(), _cause);
		this.uri = _uri;
	}
}
