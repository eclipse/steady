package com.sap.psr.vulas.backend.util;

import com.sap.psr.vulas.backend.model.Library;

/**
 * <p>VerificationException class.</p>
 *
 */
public class VerificationException extends Exception {

	private Library lib = null;
	private String url = null;
	
	/**
	 * <p>Constructor for VerificationException.</p>
	 *
	 * @param _lib a {@link com.sap.psr.vulas.backend.model.Library} object.
	 * @param _url a {@link java.lang.String} object.
	 * @param _e a {@link java.lang.Throwable} object.
	 */
	public VerificationException(Library _lib, String _url, Throwable _e) {
		super(_e);
		this.lib = _lib;
		this.url = _url;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMessage() {
		return "Error while verifying library " + this.lib + " with URL [" + this.url + "]: " + super.getMessage();
	}
}
