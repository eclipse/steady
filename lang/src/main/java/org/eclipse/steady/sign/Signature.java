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
package org.eclipse.steady.sign;

/**
 * The so called construct signature is a representation of the body of a construct that allows comparing different releases.
 * Possible implementations can be, for instance, abstract syntax trees (AST).
 */
public interface Signature {

  /**
   * Returns true if the signatures are equal, false otherwise.
   *
   * @param _o the signature to compare with
   * @return a boolean.
   */
  public boolean equals(Object _o);

  /**
   * Returns a short (perhaps compressed) JSON representation of the signature, to be uploaded to the central Vulas engine.
   * The reason to have a short representation is that we need to upload and store signatures for all applications and dependencies analyzed.
   * Moreover, it would be very good to create a representation that can be compared also on server-side, e.g., by means of SQL statements or stored procedures.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toJson();

  /**
   * <p>toString.</p>
   *
   * @return String representation of signature
   */
  public String toString();
}
