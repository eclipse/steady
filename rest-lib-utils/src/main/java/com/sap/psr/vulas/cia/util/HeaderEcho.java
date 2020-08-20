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
package com.sap.psr.vulas.cia.util;

import org.springframework.http.HttpHeaders;

/**
 * Used to echo the Http request header X-Vulas-Echo to clients.
 * This header allows Http clients to maintain the context in which a certain Ajax call has been done.
 */
public class HeaderEcho {

  /** Constant <code>ECHO_HEADER="X-Vulas-Echo"</code> */
  public static final String ECHO_HEADER = "X-Vulas-Echo";

  /**
   * If the given echo value is not null and not an empty {@link String}, the method returns
   * {@link HttpHeaders} with the identical value for the header field X-Vulas-Echo. Otherwise,
   * the {@link HttpHeaders} are empty.
   *
   * @param _echo_value_in_request a {@link java.lang.String} object.
   * @return a {@link org.springframework.http.HttpHeaders} object.
   */
  public static HttpHeaders getHeaders(String _echo_value_in_request) {
    final HttpHeaders headers = new HttpHeaders();
    if (_echo_value_in_request != null && !_echo_value_in_request.equals("")) {
      headers.set(
          "Access-Control-Expose-Headers",
          ECHO_HEADER); // https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#Access-Control-Expose-Headers
      headers.set(ECHO_HEADER, _echo_value_in_request);
    }
    return headers;
  }
}
