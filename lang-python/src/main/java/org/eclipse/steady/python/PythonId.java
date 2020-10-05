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
package org.eclipse.steady.python;

import org.eclipse.steady.ConstructId;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.JsonBuilder;

import com.google.gson.JsonObject;

/**
 * <p>PythonId class.</p>
 *
 */
public class PythonId extends ConstructId {

  /** Supported Python construct types. */
  public static enum Type {
    PACKAGE,
    MODULE,
    CLASS,
    CONSTRUCTOR,
    METHOD,
    FUNCTION
  };

  /** Constant &lt;script&gt;. */
  public static final String SCRIPT_NAME = "<script>";

  /**
   * Transforms the shared type {@link ConstructType} into the corresponding local type {@link PythonId.Type}.
   *
   * @param _type a {@link org.eclipse.steady.python.PythonId.Type} object.
   * @return a {@link org.eclipse.steady.shared.enums.ConstructType} object.
   */
  public static ConstructType toSharedType(Type _type) {
    switch (_type) {
      case CLASS:
        return ConstructType.CLAS;
      case METHOD:
        return ConstructType.METH;
      case MODULE:
        return ConstructType.MODU;
      case PACKAGE:
        return ConstructType.PACK;
      case FUNCTION:
        return ConstructType.FUNC;
      case CONSTRUCTOR:
        return ConstructType.CONS;
      default:
        throw new IllegalArgumentException("Unknown type [" + _type + "]");
    }
  }

  /**
   * <p>typeFromString.</p>
   *
   * @param _t a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.python.PythonId.Type} object.
   */
  public static Type typeFromString(String _t) {
    if ("PACK".equalsIgnoreCase(_t)) return Type.PACKAGE;
    else if ("CLAS".equalsIgnoreCase(_t)) return Type.CLASS;
    else if ("MODU".toString().equalsIgnoreCase(_t)) return Type.MODULE;
    else if ("FUNC".toString().equalsIgnoreCase(_t)) return Type.FUNCTION;
    else if ("CONS".toString().equalsIgnoreCase(_t)) return Type.CONSTRUCTOR;
    else if ("METH".toString().equalsIgnoreCase(_t)) return Type.METHOD;
    else throw new IllegalArgumentException("Unknown type [" + _t + "]");
  }

  // Members
  protected Type type = null;
  protected PythonId definitionContext = null;
  protected String simpleName = "";

  PythonId(PythonId _ctx, Type _t, String _simple_name) {
    super(ProgrammingLanguage.PY);

    // Packages dont have a definition context
    if (_t == Type.PACKAGE && _ctx != null)
      throw new IllegalArgumentException("Packages cannot have a definition context");

    // Set the members
    this.definitionContext = _ctx;
    this.type = _t;
    this.simpleName = _simple_name;
  }

  /**
   * <p>Getter for the field <code>type</code>.</p>
   *
   * @return a {@link org.eclipse.steady.python.PythonId.Type} object.
   */
  public Type getType() {
    return this.type;
  }

  /**
   * <p>getSharedType.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.ConstructType} object.
   */
  public ConstructType getSharedType() {
    return toSharedType(this.type);
  }

  /**
   * Returns the package of the construct, or null if no such package exists.
   *
   * @return a {@link org.eclipse.steady.python.PythonId} object.
   */
  public PythonId getPackage() {
    PythonId pack = null;
    // Package of packages is null
    if (this.type == Type.PACKAGE)
      ;
    else if (this.definitionContext == null)
      ;
    else {
      if (this.definitionContext.getType() == Type.PACKAGE) pack = this.definitionContext;
      else pack = this.getDefinitionContext().getPackage();
    }
    return pack;
  }

  /** {@inheritDoc} */
  @Override
  public PythonId getDefinitionContext() {
    return this.definitionContext;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return this.simpleName;
  }

  /** {@inheritDoc} */
  @Override
  public String getQualifiedName() {
    final StringBuffer b = new StringBuffer();
    if (this.definitionContext != null)
      b.append(this.definitionContext.getQualifiedName()).append(".");
    b.append(this.simpleName);
    return b.toString();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return this.getLanguage()
        + " "
        + this.getSharedType().toString()
        + " ["
        + this.getQualifiedName()
        + "]";
  }

  /** {@inheritDoc} */
  @Override
  public String getSimpleName() {
    return this.simpleName;
  }

  /** {@inheritDoc} */
  @Override
  public JsonObject toGSON() {
    final JsonObject jb = new JsonObject();
    jb.addProperty("lang", this.getLanguage().toString());
    jb.addProperty("type", this.getSharedType().toString());
    jb.addProperty("qname", this.getQualifiedName());
    return jb;
  }

  /** {@inheritDoc} */
  @Override
  public String toJSON() {
    final JsonBuilder jb = new JsonBuilder();
    jb.startObject();
    jb.appendObjectProperty("lang", this.getLanguage().toString());
    jb.appendObjectProperty("type", this.getSharedType().toString());
    jb.appendObjectProperty("qname", this.getQualifiedName());
    jb.endObject();
    return jb.getJson();
  }
}
