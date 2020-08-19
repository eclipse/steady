/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.backend;

import java.net.URI;

/**
 * <p>BackendConnectionException class.</p>
 *
 */
public class BackendConnectionException extends Exception {
  private int httpResponseStatus;
  private String httpResponseBody;
  private URI uri;
  /**
   * <p>Constructor for BackendConnectionException.</p>
   *
   * @param _message a {@link java.lang.String} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public BackendConnectionException(String _message, Throwable _cause) {
    super(_message, _cause);
  }
  /**
   * <p>Constructor for BackendConnectionException.</p>
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
   * <p>Getter for the field <code>httpResponseBody</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getHttpResponseBody() {
    return httpResponseBody;
  }
  /**
   * <p>Setter for the field <code>httpResponseBody</code>.</p>
   *
   * @param httpResponseBody a {@link java.lang.String} object.
   */
  public void setHttpResponseBody(String httpResponseBody) {
    this.httpResponseBody = httpResponseBody;
  }

  /**
   * <p>Getter for the field <code>httpResponseStatus</code>.</p>
   *
   * @return a int.
   */
  public int getHttpResponseStatus() {
    return httpResponseStatus;
  }
}
