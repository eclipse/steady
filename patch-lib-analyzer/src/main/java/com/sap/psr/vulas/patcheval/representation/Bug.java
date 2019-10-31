/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

/** Helper class for deserialization of change-list. */
public class Bug {
  String bugId;
  String source;

  /**
   * Constructor for Bug.
   *
   * @param bugId a {@link java.lang.String} object.
   * @param source a {@link java.lang.String} object.
   */
  public Bug(String bugId, String source) {
    this.bugId = bugId;
    this.source = source;
  }

  /**
   * Getter for the field <code>bugId</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBugId() {
    return bugId;
  }

  /**
   * Setter for the field <code>bugId</code>.
   *
   * @param bugId a {@link java.lang.String} object.
   */
  public void setBugId(String bugId) {
    this.bugId = bugId;
  }

  /**
   * Getter for the field <code>source</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSource() {
    return source;
  }

  /**
   * Setter for the field <code>source</code>.
   *
   * @param source a {@link java.lang.String} object.
   */
  public void setSource(String source) {
    this.source = source;
  }
}
