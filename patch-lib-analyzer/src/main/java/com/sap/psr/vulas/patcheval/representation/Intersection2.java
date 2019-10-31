/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

/**
 * Represents the intersection between distances to vulnerable and to fixed (for a certain
 * construct) among different libraries.
 */
public class Intersection2 {
  ArtifactResult2 from, to;
  int occurrences;
  Double confidence;

  /**
   * Constructor for Intersection2.
   *
   * @param from a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
   * @param to a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
   * @param o a int.
   * @param d a {@link java.lang.Double} object.
   */
  public Intersection2(ArtifactResult2 from, ArtifactResult2 to, int o, Double d) {
    this.from = from;
    this.to = to;
    this.occurrences = o;
    this.confidence = d;
  }

  /**
   * Getter for the field <code>from</code>.
   *
   * @return a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
   */
  public ArtifactResult2 getFrom() {
    return from;
  }

  /**
   * Setter for the field <code>from</code>.
   *
   * @param from a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
   */
  public void setFrom(ArtifactResult2 from) {
    this.from = from;
  }

  /**
   * Getter for the field <code>to</code>.
   *
   * @return a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
   */
  public ArtifactResult2 getTo() {
    return to;
  }

  /**
   * Setter for the field <code>to</code>.
   *
   * @param to a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
   */
  public void setTo(ArtifactResult2 to) {
    this.to = to;
  }

  /**
   * Getter for the field <code>confidence</code>.
   *
   * @return a {@link java.lang.Double} object.
   */
  public Double getConfidence() {
    return confidence;
  }

  /**
   * Setter for the field <code>confidence</code>.
   *
   * @param confidence a {@link java.lang.Double} object.
   */
  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }
}
