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

    public ArtifactLibrary(String g, String a, String v, Long timestamp) {
        this.g = g;
        this.a = a;
        this.v = v;
        this.timestamp = timestamp;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    
    
    
}
