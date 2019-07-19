/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

/**
 * Helper class for deserialization of change-list.
 */
public class Bug {
    String bugId;
    String source;

    /**
     * <p>Constructor for Bug.</p>
     *
     * @param bugId a {@link java.lang.String} object.
     * @param source a {@link java.lang.String} object.
     */
    public Bug(String bugId, String source) {
        this.bugId = bugId;
        this.source = source;
    }

    /**
     * <p>Getter for the field <code>bugId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBugId() {
        return bugId;
    }

    /**
     * <p>Setter for the field <code>bugId</code>.</p>
     *
     * @param bugId a {@link java.lang.String} object.
     */
    public void setBugId(String bugId) {
        this.bugId = bugId;
    }

    /**
     * <p>Getter for the field <code>source</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSource() {
        return source;
    }

    /**
     * <p>Setter for the field <code>source</code>.</p>
     *
     * @param source a {@link java.lang.String} object.
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    
}
