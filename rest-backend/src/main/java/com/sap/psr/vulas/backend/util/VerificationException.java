package com.sap.psr.vulas.backend.util;

import com.sap.psr.vulas.backend.model.Library;

public class VerificationException extends Exception {

	private Library lib = null;
	private String url = null;
	
	public VerificationException(Library _lib, String _url, Throwable _e) {
		super(_e);
		this.lib = _lib;
		this.url = _url;
	}
	
	@Override
	public String getMessage() {
		return "Error while verifying library " + this.lib + " with URL [" + this.url + "]: " + super.getMessage();
	}
}
