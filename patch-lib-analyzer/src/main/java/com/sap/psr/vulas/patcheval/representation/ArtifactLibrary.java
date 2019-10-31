/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

/**
 * Helper class for deserialization of all versions of artifacts returned from CIA, respect to a
 * certain group,artifact.
 */
public class ArtifactLibrary {
  String g, a, v;
  Long timestamp;

  /**
   * Constructor for ArtifactLibrary.
   *
   * @param g a {@link java.lang.String} object.
   * @param a a {@link java.lang.String} object.
   * @param v a {@link java.lang.String} object.
   * @param timestamp a {@link java.lang.Long} object.
   */
  public ArtifactLibrary(String g, String a, String v, Long timestamp) {
    this.g = g;
    this.a = a;
    this.v = v;
    this.timestamp = timestamp;
  }

  /**
   * Getter for the field <code>g</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getG() {
    return g;
  }

  /**
   * Setter for the field <code>g</code>.
   *
   * @param g a {@link java.lang.String} object.
   */
  public void setG(String g) {
    this.g = g;
  }

  /**
   * Getter for the field <code>a</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getA() {
    return a;
  }

  /**
   * Setter for the field <code>a</code>.
   *
   * @param a a {@link java.lang.String} object.
   */
  public void setA(String a) {
    this.a = a;
  }

  /**
   * Getter for the field <code>v</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getV() {
    return v;
  }

  /**
   * Setter for the field <code>v</code>.
   *
   * @param v a {@link java.lang.String} object.
   */
  public void setV(String v) {
    this.v = v;
  }

  /**
   * Getter for the field <code>timestamp</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getTimestamp() {
    return timestamp;
  }

  /**
   * Setter for the field <code>timestamp</code>.
   *
   * @param timestamp a {@link java.lang.Long} object.
   */
  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
}
