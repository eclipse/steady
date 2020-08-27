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
package org.eclipse.steady.python.sign;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * <p>PythonConstructDigestSerializer class.</p>
 *
 */
public class PythonConstructDigestSerializer extends StdSerializer<PythonConstructDigest> {

  /**
   * <p>Constructor for PythonConstructDigestSerializer.</p>
   */
  public PythonConstructDigestSerializer() {
    this(null);
  }

  /**
   * <p>Constructor for PythonConstructDigestSerializer.</p>
   *
   * @param t a {@link java.lang.Class} object.
   */
  public PythonConstructDigestSerializer(Class<PythonConstructDigest> t) {
    super(t);
  }

  /** {@inheritDoc} */
  @Override
  public void serialize(
      PythonConstructDigest value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeStartObject();
    jgen.writeStringField("computedFrom", value.getComputedFrom());
    jgen.writeStringField("computedFromType", value.getComputedFromType().toString());
    jgen.writeStringField("digest", value.getDigest());
    jgen.writeStringField("digestAlgorihtm", value.getDigestAlgorithm().toString());
    jgen.writeEndObject();
  }
}
