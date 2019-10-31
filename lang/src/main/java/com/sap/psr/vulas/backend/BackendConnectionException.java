package com.sap.psr.vulas.backend;

import java.net.URI;

/** BackendConnectionException class. */
public class BackendConnectionException extends Exception {
  private int httpResponseStatus;
  private String httpResponseBody;
  private URI uri;
  /**
   * Constructor for BackendConnectionException.
   *
   * @param _message a {@link java.lang.String} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public BackendConnectionException(String _message, Throwable _cause) {
    super(_message, _cause);
  }
  /**
   * Constructor for BackendConnectionException.
   *
   * @param _method a {@link com.sap.psr.vulas.backend.HttpMethod} object.
   * @param _uri a {@link java.net.URI} object.
   * @param _response_status a int.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public BackendConnectionException(
      HttpMethod _method, URI _uri, int _response_status, Throwable _cause) {
    super(
        "Got error ["
            + _response_status
            + "] when calling ["
            + _method
            + "] on ["
            + _uri
            + "]"
            + (_cause == null || _cause.getMessage() == null ? "" : ": " + _cause.getMessage()),
        _cause);
    this.httpResponseStatus = _response_status;
    this.uri = _uri;
  }
  /**
   * Getter for the field <code>httpResponseBody</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getHttpResponseBody() {
    return httpResponseBody;
  }
  /**
   * Setter for the field <code>httpResponseBody</code>.
   *
   * @param httpResponseBody a {@link java.lang.String} object.
   */
  public void setHttpResponseBody(String httpResponseBody) {
    this.httpResponseBody = httpResponseBody;
  }

  /**
   * Getter for the field <code>httpResponseStatus</code>.
   *
   * @return a int.
   */
  public int getHttpResponseStatus() {
    return httpResponseStatus;
  }
}
