package com.sap.psr.vulas.backend;

import java.net.HttpURLConnection;

/**
 * Http response of the RESTful API of the backend.
 */
public class HttpResponse {

	/** Http statuc code (200, 201, etc.), cf. {@link HttpUrlConnection}. */
	private int status;
	
	/** The body of the Http response message. */
	private String body;
	
	/** Creates a response with the given status but without body. */
	public HttpResponse(int _status) { this(_status, null); }
	
	/** Creates a response with the given status and body. */
	public HttpResponse(int _status, String _body) {
		this.status = _status;
		this.body = _body;
	}

	public int getStatus() { return status; }
	public boolean isNotFound() { return this.status==HttpURLConnection.HTTP_NOT_FOUND; }
	public boolean isOk() { return this.status==HttpURLConnection.HTTP_OK; }
	public boolean isCreated() { return this.status==HttpURLConnection.HTTP_CREATED; }
	public boolean isServerError() { return this.status>=500 && this.status<600; }
	public boolean isServiceUnavailable() { return this.status==HttpURLConnection.HTTP_UNAVAILABLE; }
	
	public String getBody() { return body; }
	public void setBody(String _body) { this.body = _body; }
	public boolean hasBody() { return this.body!=null; }
	
	public String toString() {
		return "[rc=" + this.getStatus() + ", hasBody=" + this.hasBody() + "]";
	}
}
