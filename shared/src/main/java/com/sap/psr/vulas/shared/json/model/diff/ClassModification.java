package com.sap.psr.vulas.shared.json.model.diff;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.json.model.ConstructId;

/** ClassModification class. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassModification implements Comparable {

  private ConstructId oldConstruct = null;
  private ConstructId newConstruct = null;
  private boolean declarationChanged;
  private boolean bodyChanged;

  /**
   * Getter for the field <code>oldConstruct</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public ConstructId getOldConstruct() {
    return oldConstruct;
  }
  /**
   * Setter for the field <code>oldConstruct</code>.
   *
   * @param oldConstruct a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void setOldConstruct(ConstructId oldConstruct) {
    this.oldConstruct = oldConstruct;
  }
  /**
   * Getter for the field <code>newConstruct</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public ConstructId getNewConstruct() {
    return newConstruct;
  }
  /**
   * Setter for the field <code>newConstruct</code>.
   *
   * @param newConstruct a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public void setNewConstruct(ConstructId newConstruct) {
    this.newConstruct = newConstruct;
  }
  /**
   * isDeclarationChanged.
   *
   * @return a boolean.
   */
  public boolean isDeclarationChanged() {
    return declarationChanged;
  }
  /**
   * Setter for the field <code>declarationChanged</code>.
   *
   * @param declarationChanged a boolean.
   */
  public void setDeclarationChanged(boolean declarationChanged) {
    this.declarationChanged = declarationChanged;
  }
  /**
   * isBodyChanged.
   *
   * @return a boolean.
   */
  public boolean isBodyChanged() {
    return bodyChanged;
  }
  /**
   * Setter for the field <code>bodyChanged</code>.
   *
   * @param bodyChanged a boolean.
   */
  public void setBodyChanged(boolean bodyChanged) {
    this.bodyChanged = bodyChanged;
  }

  /** {@inheritDoc} */
  public int compareTo(Object _other) {
    if (_other instanceof ClassModification)
      return this.getNewConstruct()
          .getQname()
          .compareTo(((ClassModification) _other).getNewConstruct().getQname());
    else
      throw new IllegalArgumentException(
          "Expected object of type ["
              + ClassModification.class.getName()
              + "], got "
              + _other.getClass().getName());
  }
}
