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
package com.sap.psr.vulas.java.sign;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.psr.vulas.java.sign.gson.ASTSignatureChangeSerializer;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.sign.Signature;
import com.sap.psr.vulas.sign.SignatureChange;

import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

/**
 * <p>ASTSignatureChange class.</p>
 *
 */
public class ASTSignatureChange extends DistillerUtil implements SignatureChange, Serializable {

  public static enum OperationType {
    Insert,
    Update,
    Move,
    Delete
  };

  private Signature mDefSignature = null;

  private Signature mFixSignature = null;

  private Set<SourceCodeChange> listOfChanges = null;

  private String mMethodName;

  // TODO : (Something fishy here, needs an improvement - see the sourcecode of changedistiller)
  // A StructureEntityVersion has all the SourceCodeChange's
  private StructureEntityVersion structureEntity = null;

  /**
   * <p>Constructor for ASTSignatureChange.</p>
   *
   * @param defSignature a {@link com.sap.psr.vulas.sign.Signature} object.
   * @param fixSignature a {@link com.sap.psr.vulas.sign.Signature} object.
   */
  public ASTSignatureChange(Signature defSignature, Signature fixSignature) {
    super();
    this.mDefSignature = defSignature;
    this.mFixSignature = fixSignature;
    // Name of the root of the AST for either the fixed or for the defective AST
    mMethodName = ((ASTSignature) mFixSignature).getValue();
  }

  /**
   * Used to create instance of ASTSignatureChange during deserialization of
   * JSON object,  when deserializing a ASTSignatureChange, we only have the list of
   * SourceCodeChanges
   * read from the DB.
   *
   * @param srcCodeChanges - List of SourceCodeChanges
   */
  /*public ASTSignatureChange(Set<SourceCodeChange> srcCodeChanges){
  	this(null,null);
  	this.setListOfChanges(srcCodeChanges);
  	List<SourceCodeChange> srcCodeChgs = new ArrayList<SourceCodeChange>(srcCodeChanges);
  	this.structureEntity.setSourceCodeChanges(srcCodeChgs);
  }*/

  /**
   * <p>Constructor for ASTSignatureChange.</p>
   *
   * @param strEntityVersion a {@link ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion} object.
   */
  public ASTSignatureChange(StructureEntityVersion strEntityVersion) {
    // this(null,null);
    this.setStructureEntity(strEntityVersion);
    this.setListOfChanges(
        new HashSet<SourceCodeChange>(this.getStructureEntity().getSourceCodeChanges()));
  }

  /**
   * <p>addChange.</p>
   *
   * @param scc a {@link ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange} object.
   */
  public void addChange(SourceCodeChange scc) {
    this.listOfChanges.add(scc);
  }

  /**
   * <p>removeChange.</p>
   *
   * @param scc a {@link ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange} object.
   */
  public void removeChange(SourceCodeChange scc) {
    this.listOfChanges.remove(scc);
  }

  /**
   * <p>toSourceCodeChanges.</p>
   *
   * @param _objects a {@link java.util.Set} object.
   * @return a {@link java.util.Set} object.
   */
  public static Set<SourceCodeChange> toSourceCodeChanges(Set<Object> _objects) {
    final Set<SourceCodeChange> changes = new HashSet<SourceCodeChange>();
    for (Object o : _objects) {
      changes.add((SourceCodeChange) o);
    }
    return changes;
  }

  /**
   * <p>Getter for the field <code>structureEntity</code>.</p>
   *
   * @return a {@link ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion} object.
   */
  public StructureEntityVersion getStructureEntity() {
    return structureEntity;
  }

  /**
   * <p>Setter for the field <code>structureEntity</code>.</p>
   *
   * @param structureEntity a {@link ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion} object.
   */
  public void setStructureEntity(StructureEntityVersion structureEntity) {
    this.structureEntity = structureEntity;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((listOfChanges == null) ? 0 : listOfChanges.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ASTSignatureChange other = (ASTSignatureChange) obj;
    if (listOfChanges == null) {
      if (other.listOfChanges != null) return false;
    } else {
      final boolean is_equal =
          ASTSignatureChange.isSameElements(
              (Collection<SourceCodeChange>) this.listOfChanges,
              (Collection<SourceCodeChange>) other.listOfChanges);
      return is_equal;
    }
    return true;
  }

  /**
   * <p>isSameElements.</p>
   *
   * @param _a a {@link java.util.Collection} object.
   * @param _b a {@link java.util.Collection} object.
   * @return a boolean.
   */
  public static boolean isSameElements(
      Collection<SourceCodeChange> _a, Collection<SourceCodeChange> _b) {
    if (_a.size() != _b.size()) return false;
    for (SourceCodeChange this_scc : _a) {
      boolean scc_found = false;
      for (SourceCodeChange other_scc : _b) {
        if (this_scc.equals(other_scc)) {
          scc_found = true;
          break;
        }
      }
      if (!scc_found) return false;
    }
    return true;
  }

  /**
   * <p>Getter for the field <code>mDefSignature</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.sign.Signature} object.
   */
  public Signature getmDefSignature() {
    return mDefSignature;
  }

  /**
   * <p>Setter for the field <code>mDefSignature</code>.</p>
   *
   * @param mDefSignature a {@link com.sap.psr.vulas.sign.Signature} object.
   */
  public void setmDefSignature(Signature mDefSignature) {
    this.mDefSignature = mDefSignature;
  }

  /**
   * <p>Getter for the field <code>mFixSignature</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.sign.Signature} object.
   */
  public Signature getmFixSignature() {
    return mFixSignature;
  }

  /**
   * <p>Setter for the field <code>mFixSignature</code>.</p>
   *
   * @param mFixSignature a {@link com.sap.psr.vulas.sign.Signature} object.
   */
  public void setmFixSignature(Signature mFixSignature) {
    this.mFixSignature = mFixSignature;
  }

  /**
   * <p>Getter for the field <code>listOfChanges</code>.</p>
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<SourceCodeChange> getListOfChanges() {
    return listOfChanges;
  }

  /**
   * <p>Setter for the field <code>listOfChanges</code>.</p>
   *
   * @param listOfChanges a {@link java.util.Set} object.
   */
  public void setListOfChanges(Set<SourceCodeChange> listOfChanges) {
    this.listOfChanges = listOfChanges;
  }

  /**
   * <p>Getter for the field <code>mMethodName</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getmMethodName() {
    return mMethodName;
  }

  /**
   * <p>Setter for the field <code>mMethodName</code>.</p>
   *
   * @param mMethodName a {@link java.lang.String} object.
   */
  public void setmMethodName(String mMethodName) {
    this.mMethodName = mMethodName;
  }

  /**
   * <p>operationTypetoJSON.</p>
   *
   * @param t a {@link com.sap.psr.vulas.java.sign.ASTSignatureChange.OperationType} object.
   * @return a {@link java.lang.String} object.
   */
  public String operationTypetoJSON(OperationType t) {
    final StringBuilder b = new StringBuilder();
    b.append("\"operationType\" : \"");
    switch (t) {
      case Insert:
        b.append("INSERT\"");
        break;
      case Update:
        b.append("UPDATE\"");
        break;
      case Move:
        b.append("MOVE\"");
        break;
      case Delete:
        b.append("DELETE\"");
        break;
      default:
        b.append("???\"");
        break;
    }
    return b.toString();
  }

  private String operationTypeToString(OperationType t) {
    switch (t) {
      case Insert:
        return "I";
      case Update:
        return "U";
      case Move:
        return "M";
      case Delete:
        return "D";
      default:
        return "?";
    }
  }

  /**
   * {@inheritDoc}
   *
   * Returns the set of edit operations required to change the defective AST int the fixed one.
   * Attention: This method will change the defective signature passed as argument to the constructor.
   */
  @Override
  public Set<Object> getModifications() {

    if (this.listOfChanges == null) {
      structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, this.mMethodName, 0);
      Distiller mDistiller = mInjector.getInstance(DistillerFactory.class).create(structureEntity);

      if (this.mDefSignature != null && this.mFixSignature != null) {
        Node defSignatureNode = ((ASTSignature) this.mDefSignature).getRoot();
        Node fixSignatureNode = ((ASTSignature) this.mFixSignature).getRoot();

        // This call modifies the "Defective" Version, basically it converts it into the Fixed
        // Version
        // (I don't know why , nothing is mentioned in the API documentation)
        // TODO : Read the the changedistiller paper  for a better understanding
        // (http://www.merlin.uzh.ch/publication/show/2531)
        mDistiller.extractClassifiedSourceCodeChanges(defSignatureNode, fixSignatureNode);
        List<SourceCodeChange> change =
            structureEntity
                .getSourceCodeChanges(); // Extract the set of sourceCodeChanges from the
                                         // StructureEntityVersion

        // Debug
        if (change != null) this.listOfChanges = new HashSet<SourceCodeChange>(change);

        /*	if(change != null)
        {
              for(SourceCodeChange ch : change){
              		listOfChanges.add(ch);
              	}
        }*/
      }
    }

    // Good to know :
    // http://stackoverflow.com/questions/905964/how-to-cast-generic-list-types-in-java?answertab=active#tab-top
    final Set<Object> set = new HashSet<Object>();
    if (this.listOfChanges != null) {
      set.addAll(this.listOfChanges); // Replaces the following loop
      /*for (SourceCodeChange  e :  this.listOfChanges) {
      	set.add( e); // need to cast each object specifically
      }*/
    }
    return set;
  }

  /** {@inheritDoc} */
  @Override
  public String toJSON() {
    final Map<Class<?>, StdSerializer<?>> custom_serializers =
        new HashMap<Class<?>, StdSerializer<?>>();
    custom_serializers.put(ASTSignatureChange.class, new ASTSignatureChangeSerializer());
    return JacksonUtil.asJsonString(this, custom_serializers);
  }

  /**
   * Returns a formatted 'SourceCodeChange' elements
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    int count = 0;
    final StringBuffer buffer = new StringBuffer();

    if (listOfChanges != null) {
      buffer.append("Test - Everything SourceCodeChange Offers :\n");

      for (SourceCodeChange change : listOfChanges) {
        buffer.append("\n CHANGE" + ++count + "\n");
        buffer.append("\n ChangedEntity = " + change.getChangedEntity());
        buffer.append(
            "\n ChangedEntity Source Range= " + change.getChangedEntity().getSourceRange());
        buffer.append("\n UniqueName = " + change.getChangedEntity().getUniqueName());
        buffer.append("\n Entity Label = " + change.getChangedEntity().getLabel());
        buffer.append("\n Entity Type = " + change.getChangedEntity().getType().toString());
        buffer.append("\n Change Type Label = " + change.getLabel());
        buffer.append("\n Parent  =  " + change.getParentEntity());
        buffer.append("\n Parent Source Range = " + change.getParentEntity().getSourceRange());
        buffer.append("\n Simple Name = " + change.getClass().getSimpleName());
        buffer.append("\n " + change.toString() + "\n");
        // buffer.append("\n SignificanceLevel(CRUCIAL, HIGH, MEDIMU, LOW) :" +
        // change.getSignificanceLevel().toString());
        // buffer.append("\n ChangedEntity, StartPosition : " +
        // change.getChangedEntity().getStartPosition());
      }
    }
    return buffer.toString();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return this.getListOfChanges().size() == 0;
  }
}
