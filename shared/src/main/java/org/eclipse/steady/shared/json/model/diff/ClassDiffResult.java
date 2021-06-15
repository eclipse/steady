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
package org.eclipse.steady.shared.json.model.diff;

import java.util.Collection;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.json.model.ConstructId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>ClassDiffResult class.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassDiffResult {

  @JsonProperty("class")
  private ConstructId clazz = null;

  private Collection<ConstructId> removedConstructors = null;
  private Collection<ConstructId> removedMethods = null;

  private Collection<ConstructId> deprecatedConstructors = null;
  private Collection<ConstructId> deprecatedMethods = null;

  private Collection<ClassModification> modifiedConstructors = null;
  private Collection<ClassModification> modifiedMethods = null;

  private Collection<ConstructId> undeprecatedConstructors = null;
  private Collection<ConstructId> undeprecatedMethods = null;

  private Collection<ConstructId> newConstructors = null;
  private Collection<ConstructId> newMethods = null;

  /**
   * <p>Getter for the field <code>clazz</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public ConstructId getClazz() {
    return this.clazz;
  }
  /**
   * <p>Setter for the field <code>clazz</code>.</p>
   *
   * @param clazz a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void setClazz(ConstructId clazz) {
    this.clazz = clazz;
  }

  /**
   * <p>Getter for the field <code>removedConstructors</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getRemovedConstructors() {
    return removedConstructors;
  }
  /**
   * <p>addRemovedContructor.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void addRemovedContructor(ConstructId _c) {
    if (this.removedConstructors == null) this.removedConstructors = new TreeSet<ConstructId>();
    this.removedConstructors.add(_c);
  }
  /**
   * <p>Setter for the field <code>removedConstructors</code>.</p>
   *
   * @param removedConstructors a {@link java.util.Collection} object.
   */
  public void setRemovedConstructors(Collection<ConstructId> removedConstructors) {
    this.removedConstructors = removedConstructors;
  }
  /**
   * <p>addRemovedMethod.</p>
   *
   * @param _m a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void addRemovedMethod(ConstructId _m) {
    if (this.removedMethods == null) this.removedMethods = new TreeSet<ConstructId>();
    this.removedMethods.add(_m);
  }
  /**
   * <p>Getter for the field <code>removedMethods</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getRemovedMethods() {
    return removedMethods;
  }
  /**
   * <p>Setter for the field <code>removedMethods</code>.</p>
   *
   * @param removedMethods a {@link java.util.Collection} object.
   */
  public void setRemovedMethods(Collection<ConstructId> removedMethods) {
    this.removedMethods = removedMethods;
  }
  /**
   * <p>Getter for the field <code>deprecatedConstructors</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getDeprecatedConstructors() {
    return deprecatedConstructors;
  }
  /**
   * <p>addDeprecatedContructor.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void addDeprecatedContructor(ConstructId _c) {
    if (this.deprecatedConstructors == null)
      this.deprecatedConstructors = new TreeSet<ConstructId>();
    this.deprecatedConstructors.add(_c);
  }
  /**
   * <p>Setter for the field <code>deprecatedConstructors</code>.</p>
   *
   * @param deprecatedConstructors a {@link java.util.Collection} object.
   */
  public void setDeprecatedConstructors(Collection<ConstructId> deprecatedConstructors) {
    this.deprecatedConstructors = deprecatedConstructors;
  }
  /**
   * <p>Getter for the field <code>deprecatedMethods</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getDeprecatedMethods() {
    return deprecatedMethods;
  }
  /**
   * <p>addDeprecatedMethod.</p>
   *
   * @param _m a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void addDeprecatedMethod(ConstructId _m) {
    if (this.deprecatedMethods == null) this.deprecatedMethods = new TreeSet<ConstructId>();
    this.deprecatedMethods.add(_m);
  }
  /**
   * <p>Setter for the field <code>deprecatedMethods</code>.</p>
   *
   * @param deprecatedMethods a {@link java.util.Collection} object.
   */
  public void setDeprecatedMethods(Collection<ConstructId> deprecatedMethods) {
    this.deprecatedMethods = deprecatedMethods;
  }
  /**
   * <p>Getter for the field <code>modifiedConstructors</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ClassModification> getModifiedConstructors() {
    return modifiedConstructors;
  }
  /**
   * <p>addModifiedContructor.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.json.model.diff.ClassModification} object.
   */
  public void addModifiedContructor(ClassModification _c) {
    if (this.modifiedConstructors == null)
      this.modifiedConstructors = new TreeSet<ClassModification>();
    this.modifiedConstructors.add(_c);
  }
  /**
   * <p>Setter for the field <code>modifiedConstructors</code>.</p>
   *
   * @param modifiedConstructors a {@link java.util.Collection} object.
   */
  public void setModifiedConstructors(Collection<ClassModification> modifiedConstructors) {
    this.modifiedConstructors = modifiedConstructors;
  }
  /**
   * <p>Getter for the field <code>modifiedMethods</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ClassModification> getModifiedMethods() {
    return modifiedMethods;
  }
  /**
   * <p>addModifiedMethod.</p>
   *
   * @param _m a {@link org.eclipse.steady.shared.json.model.diff.ClassModification} object.
   */
  public void addModifiedMethod(ClassModification _m) {
    if (this.modifiedMethods == null) this.modifiedMethods = new TreeSet<ClassModification>();
    this.modifiedMethods.add(_m);
  }
  /**
   * <p>Setter for the field <code>modifiedMethods</code>.</p>
   *
   * @param modifiedMethods a {@link java.util.Collection} object.
   */
  public void setModifiedMethods(Collection<ClassModification> modifiedMethods) {
    this.modifiedMethods = modifiedMethods;
  }
  /**
   * <p>Getter for the field <code>undeprecatedConstructors</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getUndeprecatedConstructors() {
    return undeprecatedConstructors;
  }
  /**
   * <p>addUndeprecatedContructor.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void addUndeprecatedContructor(ConstructId _c) {
    if (this.undeprecatedConstructors == null)
      this.undeprecatedConstructors = new TreeSet<ConstructId>();
    this.undeprecatedConstructors.add(_c);
  }
  /**
   * <p>Setter for the field <code>undeprecatedConstructors</code>.</p>
   *
   * @param undeprecatedConstructors a {@link java.util.Collection} object.
   */
  public void setUndeprecatedConstructors(Collection<ConstructId> undeprecatedConstructors) {
    this.undeprecatedConstructors = undeprecatedConstructors;
  }
  /**
   * <p>Getter for the field <code>undeprecatedMethods</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getUndeprecatedMethods() {
    return undeprecatedMethods;
  }
  /**
   * <p>addUndeprecatedMethod.</p>
   *
   * @param _m a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void addUndeprecatedMethod(ConstructId _m) {
    if (this.undeprecatedMethods == null) this.undeprecatedMethods = new TreeSet<ConstructId>();
    this.undeprecatedMethods.add(_m);
  }
  /**
   * <p>Setter for the field <code>undeprecatedMethods</code>.</p>
   *
   * @param undeprecatedMethods a {@link java.util.Collection} object.
   */
  public void setUndeprecatedMethods(Collection<ConstructId> undeprecatedMethods) {
    this.undeprecatedMethods = undeprecatedMethods;
  }
  /**
   * <p>Getter for the field <code>newConstructors</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getNewConstructors() {
    return newConstructors;
  }
  /**
   * <p>addNewContructor.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void addNewContructor(ConstructId _c) {
    if (this.newConstructors == null) this.newConstructors = new TreeSet<ConstructId>();
    this.newConstructors.add(_c);
  }
  /**
   * <p>Setter for the field <code>newConstructors</code>.</p>
   *
   * @param newConstructors a {@link java.util.Collection} object.
   */
  public void setNewConstructors(Collection<ConstructId> newConstructors) {
    this.newConstructors = newConstructors;
  }
  /**
   * <p>Getter for the field <code>newMethods</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getNewMethods() {
    return newMethods;
  }
  /**
   * <p>addNewMethod.</p>
   *
   * @param _m a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void addNewMethod(ConstructId _m) {
    if (this.newMethods == null) this.newMethods = new TreeSet<ConstructId>();
    this.newMethods.add(_m);
  }
  /**
   * <p>Setter for the field <code>newMethods</code>.</p>
   *
   * @param newMethods a {@link java.util.Collection} object.
   */
  public void setNewMethods(Collection<ConstructId> newMethods) {
    this.newMethods = newMethods;
  }

  /**
   * Returns true if the given {@link ConstructId} is deleted.
   *
   * @param _cid a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   * @return a boolean.
   */
  @JsonIgnore
  public boolean isDeleted(@NotNull ConstructId _cid) {
    return (_cid.getType() == ConstructType.CONS
            && this.removedConstructors != null
            && this.removedConstructors.contains(_cid))
        || (_cid.getType() == ConstructType.METH
            && this.removedMethods != null
            && this.removedMethods.contains(_cid));
  }

  /**
   * Returns true if the given {@link ConstructId} is modified.
   *
   * @param _cid a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   * @return a boolean.
   */
  @JsonIgnore
  public boolean isBodyChanged(@NotNull ConstructId _cid) {
    boolean mod = false;
    if (_cid.getType() == ConstructType.CONS && this.modifiedConstructors != null) {
      for (ClassModification m : this.modifiedConstructors) {
        if (m.getNewConstruct().getQname().equals(_cid.getQname()) && m.isBodyChanged()) {
          mod = true;
          break;
        }
      }
    } else if (_cid.getType() == ConstructType.METH && this.modifiedMethods != null) {
      for (ClassModification m : this.modifiedMethods) {
        if (m.getNewConstruct().getQname().equals(_cid.getQname()) && m.isBodyChanged()) {
          mod = true;
          break;
        }
      }
    }
    return mod;
  }
}
