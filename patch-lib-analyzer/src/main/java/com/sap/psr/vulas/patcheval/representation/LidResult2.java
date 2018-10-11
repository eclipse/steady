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
    public LidResult2(LibraryId libId, Long t, boolean qnameInJar) {
        this.libId = libId;
        this.timestamp = t;
        this.qnameInJar = qnameInJar;
    }
    
    // only PYTHON bug where we cannot check constructs containment or equality
    public LidResult2(LibraryId libId, Long t) {
        this.libId = libId;
        this.timestamp = t;
    }
    
    public boolean isQnameInJar() {
        return qnameInJar;
    }

    public void setQnameInJar(boolean qnameInJar) {
        this.qnameInJar = qnameInJar;
    }

    public Boolean isSourcesAvailable() {
        return sourcesAvailable;
    }

    public void setSourcesAvailable(Boolean sourcesAvailable) {
        this.sourcesAvailable = sourcesAvailable;
    }

    public LibraryId getLibId() {
        return libId;
    }

    public void setLibId(LibraryId libId) {
        this.libId = libId;
    }

    public Integer getChangesToV() {
        return changesToV;
    }

    public void setChangesToV(Integer changesToV) {
        this.changesToV = changesToV;
    }

    public Integer getChangesToF() {
        return changesToF;
    }

    public void setChangesToF(Integer changesToF) {
        this.changesToF = changesToF;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(this.libId.toString()).append(" -> ");
        sb.append("QnameInJar: ").append(qnameInJar)
                .append("; sourcesAvailable: ").append(sourcesAvailable);
        return sb.toString();
    }
    
    // "Note: this class has a natural ordering that is inconsistent with equals."
    @Override
    public int compareTo(LidResult2 other){
        if ( this.getTimestamp().equals(other.getTimestamp()) ){
            return 0;
        } 
        return this.getTimestamp() - other.getTimestamp() < 0? -1 : 1;
    }

	

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getAst_lid() {
		return ast_lid;
	}

	public void setAst_lid(String ast_lid) {
		this.ast_lid = ast_lid;
	}
}
