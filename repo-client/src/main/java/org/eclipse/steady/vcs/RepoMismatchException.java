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
package org.eclipse.steady.vcs;

/**
 * Thrown to indicate that a given IVCSClient cannot interact with a VCS repository at the given URL.
 * Reasons can be, for instance, that there is no VCS repository at all or a different type than expected (e.g., GIT instead of SVN).
 * Connectivity problems should NOT be indicated using this exception.
 */
public class RepoMismatchException extends Exception {
  /**
   * <p>Constructor for RepoMismatchException.</p>
   *
   * @param _client a {@link org.eclipse.steady.vcs.IVCSClient} object.
   * @param _url a {@link java.lang.String} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public RepoMismatchException(IVCSClient _client, String _url, Throwable _cause) {
    super(
        "VCS client "
            + ((Object) _client).getClass().getName()
            + " (type "
            + _client.getType()
            + ") does not match to the repository (if any) at URL "
            + _url,
        _cause);
  }
}
