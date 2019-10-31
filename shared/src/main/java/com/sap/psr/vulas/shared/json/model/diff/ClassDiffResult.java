package com.sap.psr.vulas.shared.json.model.diff;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import java.util.Collection;
import java.util.TreeSet;
import javax.validation.constraints.NotNull;

/** ClassDiffResult class. */
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
   * Getter for the field <code>clazz</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public ConstructId getClazz() {
    return this.clazz;
  }
  /**
   * Setter for the field <code>clazz</code>.
   *
   * @param clazz a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void setClazz(ConstructId clazz) {
    this.clazz = clazz;
  }

  /**
   * Getter for the field <code>removedConstructors</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getRemovedConstructors() {
    return removedConstructors;
  }
  /**
   * addRemovedContructor.
   *
   * @param _c a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void addRemovedContructor(ConstructId _c) {
    if (this.removedConstructors == null) this.removedConstructors = new TreeSet<ConstructId>();
    this.removedConstructors.add(_c);
  }
  /**
   * Setter for the field <code>removedConstructors</code>.
   *
   * @param removedConstructors a {@link java.util.Collection} object.
   */
  public void setRemovedConstructors(Collection<ConstructId> removedConstructors) {
    this.removedConstructors = removedConstructors;
  }
  /**
   * addRemovedMethod.
   *
   * @param _m a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void addRemovedMethod(ConstructId _m) {
    if (this.removedMethods == null) this.removedMethods = new TreeSet<ConstructId>();
    this.removedMethods.add(_m);
  }
  /**
   * Getter for the field <code>removedMethods</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getRemovedMethods() {
    return removedMethods;
  }
  /**
   * Setter for the field <code>removedMethods</code>.
   *
   * @param removedMethods a {@link java.util.Collection} object.
   */
  public void setRemovedMethods(Collection<ConstructId> removedMethods) {
    this.removedMethods = removedMethods;
  }
  /**
   * Getter for the field <code>deprecatedConstructors</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getDeprecatedConstructors() {
    return deprecatedConstructors;
  }
  /**
   * addDeprecatedContructor.
   *
   * @param _c a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void addDeprecatedContructor(ConstructId _c) {
    if (this.deprecatedConstructors == null)
      this.deprecatedConstructors = new TreeSet<ConstructId>();
    this.deprecatedConstructors.add(_c);
  }
  /**
   * Setter for the field <code>deprecatedConstructors</code>.
   *
   * @param deprecatedConstructors a {@link java.util.Collection} object.
   */
  public void setDeprecatedConstructors(Collection<ConstructId> deprecatedConstructors) {
    this.deprecatedConstructors = deprecatedConstructors;
  }
  /**
   * Getter for the field <code>deprecatedMethods</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getDeprecatedMethods() {
    return deprecatedMethods;
  }
  /**
   * addDeprecatedMethod.
   *
   * @param _m a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void addDeprecatedMethod(ConstructId _m) {
    if (this.deprecatedMethods == null) this.deprecatedMethods = new TreeSet<ConstructId>();
    this.deprecatedMethods.add(_m);
  }
  /**
   * Setter for the field <code>deprecatedMethods</code>.
   *
   * @param deprecatedMethods a {@link java.util.Collection} object.
   */
  public void setDeprecatedMethods(Collection<ConstructId> deprecatedMethods) {
    this.deprecatedMethods = deprecatedMethods;
  }
  /**
   * Getter for the field <code>modifiedConstructors</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ClassModification> getModifiedConstructors() {
    return modifiedConstructors;
  }
  /**
   * addModifiedContructor.
   *
   * @param _c a {@link com.sap.psr.vulas.shared.json.model.diff.ClassModification} object.
   */
  public void addModifiedContructor(ClassModification _c) {
    if (this.modifiedConstructors == null)
      this.modifiedConstructors = new TreeSet<ClassModification>();
    this.modifiedConstructors.add(_c);
  }
  /**
   * Setter for the field <code>modifiedConstructors</code>.
   *
   * @param modifiedConstructors a {@link java.util.Collection} object.
   */
  public void setModifiedConstructors(Collection<ClassModification> modifiedConstructors) {
    this.modifiedConstructors = modifiedConstructors;
  }
  /**
   * Getter for the field <code>modifiedMethods</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ClassModification> getModifiedMethods() {
    return modifiedMethods;
  }
  /**
   * addModifiedMethod.
   *
   * @param _m a {@link com.sap.psr.vulas.shared.json.model.diff.ClassModification} object.
   */
  public void addModifiedMethod(ClassModification _m) {
    if (this.modifiedMethods == null) this.modifiedMethods = new TreeSet<ClassModification>();
    this.modifiedMethods.add(_m);
  }
  /**
   * Setter for the field <code>modifiedMethods</code>.
   *
   * @param modifiedMethods a {@link java.util.Collection} object.
   */
  public void setModifiedMethods(Collection<ClassModification> modifiedMethods) {
    this.modifiedMethods = modifiedMethods;
  }
  /**
   * Getter for the field <code>undeprecatedConstructors</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getUndeprecatedConstructors() {
    return undeprecatedConstructors;
  }
  /**
   * addUndeprecatedContructor.
   *
   * @param _c a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void addUndeprecatedContructor(ConstructId _c) {
    if (this.undeprecatedConstructors == null)
      this.undeprecatedConstructors = new TreeSet<ConstructId>();
    this.undeprecatedConstructors.add(_c);
  }
  /**
   * Setter for the field <code>undeprecatedConstructors</code>.
   *
   * @param undeprecatedConstructors a {@link java.util.Collection} object.
   */
  public void setUndeprecatedConstructors(Collection<ConstructId> undeprecatedConstructors) {
    this.undeprecatedConstructors = undeprecatedConstructors;
  }
  /**
   * Getter for the field <code>undeprecatedMethods</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getUndeprecatedMethods() {
    return undeprecatedMethods;
  }
  /**
   * addUndeprecatedMethod.
   *
   * @param _m a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void addUndeprecatedMethod(ConstructId _m) {
    if (this.undeprecatedMethods == null) this.undeprecatedMethods = new TreeSet<ConstructId>();
    this.undeprecatedMethods.add(_m);
  }
  /**
   * Setter for the field <code>undeprecatedMethods</code>.
   *
   * @param undeprecatedMethods a {@link java.util.Collection} object.
   */
  public void setUndeprecatedMethods(Collection<ConstructId> undeprecatedMethods) {
    this.undeprecatedMethods = undeprecatedMethods;
  }
  /**
   * Getter for the field <code>newConstructors</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getNewConstructors() {
    return newConstructors;
  }
  /**
   * addNewContructor.
   *
   * @param _c a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void addNewContructor(ConstructId _c) {
    if (this.newConstructors == null) this.newConstructors = new TreeSet<ConstructId>();
    this.newConstructors.add(_c);
  }
  /**
   * Setter for the field <code>newConstructors</code>.
   *
   * @param newConstructors a {@link java.util.Collection} object.
   */
  public void setNewConstructors(Collection<ConstructId> newConstructors) {
    this.newConstructors = newConstructors;
  }
  /**
   * Getter for the field <code>newMethods</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ConstructId> getNewMethods() {
    return newMethods;
  }
  /**
   * addNewMethod.
   *
   * @param _m a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void addNewMethod(ConstructId _m) {
    if (this.newMethods == null) this.newMethods = new TreeSet<ConstructId>();
    this.newMethods.add(_m);
  }
  /**
   * Setter for the field <code>newMethods</code>.
   *
   * @param newMethods a {@link java.util.Collection} object.
   */
  public void setNewMethods(Collection<ConstructId> newMethods) {
    this.newMethods = newMethods;
  }

  /**
   * Returns true if the given {@link ConstructId} is deleted.
   *
   * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
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
   * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
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
