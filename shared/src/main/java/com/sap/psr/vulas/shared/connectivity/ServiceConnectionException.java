package com.sap.psr.vulas.shared.connectivity;

import java.net.URI;

/**
 * Thrown to indicate a problem when calling RESTful services.
 */
public class ServiceConnectionException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private URI uri;
	
	/**
	 * <p>Constructor for ServiceConnectionException.</p>
	 *
	 * @param _message a {@link java.lang.String} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public ServiceConnectionException(String _message, Throwable _cause) {
		super(_message, _cause);
	}
	
	/**
	 * <p>Constructor for ServiceConnectionException.</p>
	 *
	 * @param _uri a {@link java.net.URI} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public ServiceConnectionException(URI _uri, Throwable _cause) {
		super("Error calling [" + _uri + "]: " + _cause.getMessage(), _cause);
		this.uri = _uri;
	}
}
