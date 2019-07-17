/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

/**
 * Helper  class for deserialization of all versions of artifacts returned from CIA, respect to a certain group,artifact.
 */
public class ArtifactLibrary {
    String g,a,v;
    Long timestamp;

    /**
     * <p>Constructor for ArtifactLibrary.</p>
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
     * <p>Getter for the field <code>g</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getG() {
        return g;
    }

    /**
     * <p>Setter for the field <code>g</code>.</p>
     *
     * @param g a {@link java.lang.String} object.
     */
    public void setG(String g) {
        this.g = g;
    }

    /**
     * <p>Getter for the field <code>a</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getA() {
        return a;
    }

    /**
     * <p>Setter for the field <code>a</code>.</p>
     *
     * @param a a {@link java.lang.String} object.
     */
    public void setA(String a) {
        this.a = a;
    }

    /**
     * <p>Getter for the field <code>v</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getV() {
        return v;
    }

    /**
     * <p>Setter for the field <code>v</code>.</p>
     *
     * @param v a {@link java.lang.String} object.
     */
    public void setV(String v) {
        this.v = v;
    }

    /**
     * <p>Getter for the field <code>timestamp</code>.</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * <p>Setter for the field <code>timestamp</code>.</p>
     *
     * @param timestamp a {@link java.lang.Long} object.
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    
    
    
}
