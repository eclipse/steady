package com.sap.psr.vulas.backend;

import java.net.HttpURLConnection;

/** Http response of the RESTful API of the backend. */
public class HttpResponse {

  /** Http statuc code (200, 201, etc.), cf. {@link HttpUrlConnection}. */
  private int status;

  /** The body of the Http response message. */
  private String body;

  /**
   * Creates a response with the given status but without body.
   *
   * @param _status a int.
   */
  public HttpResponse(int _status) {
    this(_status, null);
  }

  /**
   * Creates a response with the given status and body.
   *
   * @param _status a int.
   * @param _body a {@link java.lang.String} object.
   */
  public HttpResponse(int _status, String _body) {
    this.status = _status;
    this.body = _body;
  }

  /**
   * Getter for the field <code>status</code>.
   *
   * @return a int.
   */
  public int getStatus() {
    return status;
  }
  /**
   * isNotFound.
   *
   * @return a boolean.
   */
  public boolean isNotFound() {
    return this.status == HttpURLConnection.HTTP_NOT_FOUND;
  }
  /**
   * isOk.
   *
   * @return a boolean.
   */
  public boolean isOk() {
    return this.status == HttpURLConnection.HTTP_OK;
  }
  /**
   * isCreated.
   *
   * @return a boolean.
   */
  public boolean isCreated() {
    return this.status == HttpURLConnection.HTTP_CREATED;
  }
  /**
   * isServerError.
   *
   * @return a boolean.
   */
  public boolean isServerError() {
    return this.status >= 500 && this.status < 600;
  }
  /**
   * isServiceUnavailable.
   *
   * @return a boolean.
   */
  public boolean isServiceUnavailable() {
    return this.status == HttpURLConnection.HTTP_UNAVAILABLE;
  }

  /**
   * Getter for the field <code>body</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBody() {
    return body;
  }
  /**
   * Setter for the field <code>body</code>.
   *
   * @param _body a {@link java.lang.String} object.
   */
  public void setBody(String _body) {
    this.body = _body;
  }
  /**
   * hasBody.
   *
   * @return a boolean.
   */
  public boolean hasBody() {
    return this.body != null;
  }

  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return "[rc=" + this.getStatus() + ", hasBody=" + this.hasBody() + "]";
  }
}
