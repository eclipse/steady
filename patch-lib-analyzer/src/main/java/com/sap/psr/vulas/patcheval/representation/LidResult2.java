/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

import com.sap.psr.vulas.shared.json.model.LibraryId;

/**
 * Class containing all the information computed on a library Id during the first phase (correspond to 1 line in the CSV)
 */
public class LidResult2 implements Comparable<LidResult2>{
    private LibraryId libId;
    private Long timestamp;
    private boolean qnameInJar;
    private Boolean sourcesAvailable=null;
   
    private String ast_lid=null;
    private Integer changesToV=null;
    private Integer changesToF=null;

    
 
    /**
     * <p>Constructor for LidResult2.</p>
     *
     * @param libId a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     * @param t a {@link java.lang.Long} object.
     * @param qnameInJar a boolean.
     * @param sourcesAvailable a boolean.
     * @param changesToV a int.
     * @param changesToF a int.
     * @param _ast_lid a {@link java.lang.String} object.
     */
    public LidResult2(LibraryId libId, Long t, boolean qnameInJar, boolean sourcesAvailable, int changesToV, int changesToF, String _ast_lid) {
        this.libId = libId;
        this.timestamp = t;
        this.qnameInJar = qnameInJar;
        this.sourcesAvailable = sourcesAvailable;
        this.ast_lid = _ast_lid;
        this.changesToV = changesToV;
        this.changesToF = changesToF;
    }

    // only for constructs ADD or DEL
    /**
     * <p>Constructor for LidResult2.</p>
     *
     * @param libId a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     * @param t a {@link java.lang.Long} object.
     * @param qnameInJar a boolean.
     */
    public LidResult2(LibraryId libId, Long t, boolean qnameInJar) {
        this.libId = libId;
        this.timestamp = t;
        this.qnameInJar = qnameInJar;
    }
    
    // only PYTHON bug where we cannot check constructs containment or equality
    /**
     * <p>Constructor for LidResult2.</p>
     *
     * @param libId a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     * @param t a {@link java.lang.Long} object.
     */
    public LidResult2(LibraryId libId, Long t) {
        this.libId = libId;
        this.timestamp = t;
    }
    
    /**
     * <p>isQnameInJar.</p>
     *
     * @return a boolean.
     */
    public boolean isQnameInJar() {
        return qnameInJar;
    }

    /**
     * <p>Setter for the field <code>qnameInJar</code>.</p>
     *
     * @param qnameInJar a boolean.
     */
    public void setQnameInJar(boolean qnameInJar) {
        this.qnameInJar = qnameInJar;
    }

    /**
     * <p>isSourcesAvailable.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean isSourcesAvailable() {
        return sourcesAvailable;
    }

    /**
     * <p>Setter for the field <code>sourcesAvailable</code>.</p>
     *
     * @param sourcesAvailable a {@link java.lang.Boolean} object.
     */
    public void setSourcesAvailable(Boolean sourcesAvailable) {
        this.sourcesAvailable = sourcesAvailable;
    }

    /**
     * <p>Getter for the field <code>libId</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     */
    public LibraryId getLibId() {
        return libId;
    }

    /**
     * <p>Setter for the field <code>libId</code>.</p>
     *
     * @param libId a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
     */
    public void setLibId(LibraryId libId) {
        this.libId = libId;
    }

    /**
     * <p>Getter for the field <code>changesToV</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getChangesToV() {
        return changesToV;
    }

    /**
     * <p>Setter for the field <code>changesToV</code>.</p>
     *
     * @param changesToV a {@link java.lang.Integer} object.
     */
    public void setChangesToV(Integer changesToV) {
        this.changesToV = changesToV;
    }

    /**
     * <p>Getter for the field <code>changesToF</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getChangesToF() {
        return changesToF;
    }

    /**
     * <p>Setter for the field <code>changesToF</code>.</p>
     *
     * @param changesToF a {@link java.lang.Integer} object.
     */
    public void setChangesToF(Integer changesToF) {
        this.changesToF = changesToF;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(this.libId.toString()).append(" -> ");
        sb.append("QnameInJar: ").append(qnameInJar)
                .append("; sourcesAvailable: ").append(sourcesAvailable);
        return sb.toString();
    }
    
    // "Note: this class has a natural ordering that is inconsistent with equals."
    /** {@inheritDoc} */
    @Override
    public int compareTo(LidResult2 other){
        if ( this.getTimestamp().equals(other.getTimestamp()) ){
            return 0;
        } 
        return this.getTimestamp() - other.getTimestamp() < 0? -1 : 1;
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

	/**
	 * <p>Getter for the field <code>ast_lid</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAst_lid() {
		return ast_lid;
	}

	/**
	 * <p>Setter for the field <code>ast_lid</code>.</p>
	 *
	 * @param ast_lid a {@link java.lang.String} object.
	 */
	public void setAst_lid(String ast_lid) {
		this.ast_lid = ast_lid;
	}
}
