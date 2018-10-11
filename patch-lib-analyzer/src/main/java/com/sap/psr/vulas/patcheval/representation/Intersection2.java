/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

/**
 * Represents the intersection between distances to vulnerable and to fixed (for a certain construct) among different libraries.
 */
public class Intersection2 {
    ArtifactResult2 from, to;
    int occurrences;
    Double confidence;
    
    public Intersection2(ArtifactResult2 from, ArtifactResult2 to, int o, Double d) {
        this.from = from;
        this.to = to;
        this.occurrences = o;
        this.confidence = d;
        
    }

    public ArtifactResult2 getFrom() {
        return from;
    }

    public void setFrom(ArtifactResult2 from) {
        this.from = from;
    }

    public ArtifactResult2 getTo() {
        return to;
    }

    public void setTo(ArtifactResult2 to) {
        this.to = to;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    


    
}
