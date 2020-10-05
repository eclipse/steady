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
package org.eclipse.steady.java.sign.gson;

import java.lang.reflect.Type;
import java.util.Set;

import org.eclipse.steady.ConstructId;
import org.eclipse.steady.java.JavaClassId;
import org.eclipse.steady.java.JavaClassInit;
import org.eclipse.steady.java.JavaConstructorId;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.java.JavaMethodId;
import org.eclipse.steady.java.JavaPackageId;
import org.eclipse.steady.java.sign.ASTConstructBodySignature;
import org.eclipse.steady.java.sign.ASTSignatureChange;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * <p>GsonHelper class.</p>
 *
 */
public class GsonHelper {

  /**
   * Returns a Vulas-specific GsonBuilder, i.e., with several custom serializers and deserializers registered.	 *
   *
   * @return a {@link com.google.gson.GsonBuilder} object.
   */
  public static GsonBuilder getCustomGsonBuilder() {
    final GsonBuilder gson = new GsonBuilder();

    // Subclasses of ConstructId and JavaId
    gson.registerTypeAdapter(ConstructId.class, new ConstructIdSerializer());
    gson.registerTypeAdapter(JavaPackageId.class, new ConstructIdSerializer());
    gson.registerTypeAdapter(JavaClassId.class, new ConstructIdSerializer());
    gson.registerTypeAdapter(JavaClassInit.class, new ConstructIdSerializer());
    gson.registerTypeAdapter(JavaConstructorId.class, new ConstructIdSerializer());
    gson.registerTypeAdapter(JavaMethodId.class, new ConstructIdSerializer());

    // Signature-related classes
    gson.registerTypeAdapter(ASTSignatureChange.class, new ASTSignatureChangeDeserializer());
    gson.registerTypeAdapter(ASTConstructBodySignature.class, new ASTSignatureDeserializer());

    return gson;
  }

  static class ConstructIdSerializer
      implements JsonSerializer<ConstructId>, JsonDeserializer<ConstructId> {

    public JsonElement serialize(
        ConstructId src, Type typeOfSrc, JsonSerializationContext context) {
      final JsonObject c = new JsonObject();
      c.addProperty("lang", src.getLanguage().toString());
      c.addProperty("type", JavaId.typeToString(((JavaId) src).getType()));
      c.addProperty("qname", src.getQualifiedName());
      final Set<String> annotations = ((JavaId) src).getAnnotations();
      if (!annotations.isEmpty()) {
        final JsonArray anno = new JsonArray();
        for (String a : annotations) {
          anno.add(new JsonPrimitive(a));
        }
        c.add("a", anno);
      }
      return c;
    }

    public ConstructId deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      // To be returned
      ConstructId cid = null;

      final JsonObject c = (JsonObject) json;
      final String t = c.getAsJsonPrimitive("type").getAsString();
      final String qn = c.getAsJsonPrimitive("qname").getAsString();

      if (JavaId.typeToString(JavaId.Type.PACKAGE).equals(t)) cid = new JavaPackageId(qn);
      else if (JavaId.typeToString(JavaId.Type.CLASS).equals(t)) cid = JavaId.parseClassQName(qn);
      else if (JavaId.typeToString(JavaId.Type.CLASSINIT).equals(t))
        cid = JavaId.parseClassInitQName(qn);
      else if (JavaId.typeToString(JavaId.Type.METHOD).equals(t)) cid = JavaId.parseMethodQName(qn);
      else if (JavaId.typeToString(JavaId.Type.CONSTRUCTOR).equals(t))
        cid = JavaId.parseConstructorQName(qn);

      // TODO: Add annotations

      return cid;
    }
  }
}
