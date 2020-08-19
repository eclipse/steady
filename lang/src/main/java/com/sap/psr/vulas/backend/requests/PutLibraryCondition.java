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
package com.sap.psr.vulas.backend.requests;

import com.sap.psr.vulas.backend.HttpResponse;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Library;

/**
 * <p>PutLibraryCondition class.</p>
 *
 */
public class PutLibraryCondition implements ResponseCondition {

  private Library lib;
  private int constructs_count;

  /**
   * <p>Constructor for PutLibraryCondition.</p>
   *
   * @param _l a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   */
  public PutLibraryCondition(Library _l) {
    this.lib = _l;
  }

  /** {@inheritDoc} */
  @Override
  public boolean meetsCondition(HttpResponse _response) {
    if (_response == null || !_response.hasBody()) return false;
    boolean meets = false;
    Library backend_lib = (Library) JacksonUtil.asObject(_response.getBody(), Library.class);

    // int existing_constructs = backend_lib.countConstructTypes().countTotal();
    constructs_count = (this.lib.getConstructs() == null ? 0 : this.lib.getConstructs().size());

    ContentCondition c =
        new ContentCondition(
            "\\\"countTotal\\\"\\s*:\\s*([\\d]*)",
            ContentCondition.Mode.LT_DOUBLE,
            Integer.toString(constructs_count));
    if (c.meetsCondition(_response)) meets = true;
    else if (backend_lib.getLibraryId() == null && this.lib.getLibraryId() != null) {
      meets = true;
    } else if ((backend_lib.getBundledLibraryIds() == null
            || backend_lib.getBundledLibraryIds().size() == 0)
        && (this.lib.getBundledLibraryIds() != null
            && this.lib.getBundledLibraryIds().size() > 0)) {
      meets = true;
    }

    return meets;
  }

  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return "[body LT_DOUBLE "
        + this.constructs_count
        + "] OR [existing_GAV==null, current_GAV!=null] OR [existing_bundledLibIds==null,"
        + " current_bundledLibIds!=null]";
  }
}
