package com.sap.psr.vulas.backend;

import java.net.URI;
import java.net.URL;

public class BackendConnectionException extends Exception {
	private int httpResponseStatus;
	private String httpResponseBody;
	private URI uri;
	public BackendConnectionException(String _message, Throwable _cause) {
		super(_message, _cause);
	}
	public BackendConnectionException(HttpMethod _method, URI _uri, int _response_status, Throwable _cause) {
		super("Got error [" + _response_status + "] when calling [" + _method + "] on [" + _uri + "]" + (_cause==null || _cause.getMessage()==null ? "" : ": " + _cause.getMessage()), _cause);
		this.httpResponseStatus = _response_status;
		this.uri = _uri;
	}
	public String getHttpResponseBody() { return httpResponseBody; }
	public void setHttpResponseBody(String httpResponseBody) { this.httpResponseBody = httpResponseBody; }	
	
	public int getHttpResponseStatus() { return httpResponseStatus; }
}
