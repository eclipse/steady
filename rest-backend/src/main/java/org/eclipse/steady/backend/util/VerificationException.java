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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.backend.util;

import org.eclipse.steady.backend.model.Library;

/**
 * <p>VerificationException class.</p>
 */
public class VerificationException extends Exception {

  private Library lib = null;
  private String url = null;

  /**
   * <p>Constructor for VerificationException.</p>
   *
   * @param _lib a {@link org.eclipse.steady.backend.model.Library} object.
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
    return "Error while verifying library "
        + this.lib
        + " with URL ["
        + this.url
        + "]: "
        + super.getMessage();
  }
}
