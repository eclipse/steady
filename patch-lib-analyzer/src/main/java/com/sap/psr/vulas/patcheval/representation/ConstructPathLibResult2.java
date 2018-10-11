/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;


import com.sap.psr.vulas.shared.enums.ConstructChangeType;

/**
 * Contains the results for a certain construct,path and library Id (i.e., for a line of the CSV file)

 */
public class ConstructPathLibResult2 {
    private String qname;
    private String path;
    private ConstructChangeType type;
    private LidResult2 lidResult;
    private String vulnAst;
    private String fixedAst;
    
    
    public ConstructPathLibResult2(String _qname, String _path, ConstructChangeType constructChangeType, LidResult2 _lidres, String _v, String _f) {
        this.qname = _qname;
        this.path = _path;
        this.type = constructChangeType;
        this.lidResult = _lidres;
        this.vulnAst =_v;
        this.fixedAst = _f;
    }

    public String getQname() {
        return qname;
    }

    public void setQname(String qname) {
        this.qname = qname;
    }

    
    
    public String getVulnAst() {
		return vulnAst;
	}

	public void setVulnAst(String vulnAst) {
		this.vulnAst = vulnAst;
	}

	public String getFixedAst() {
		return fixedAst;
	}

	public void setFixedAst(String fixedAst) {
		this.fixedAst = fixedAst;
	}

	public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ConstructChangeType getType() {
        return type;
    }

    public void setType(ConstructChangeType t) {
        this.type = t;
    }

    
    public LidResult2 getLidResult() {
        return lidResult;
    }

    public void setLidResult(LidResult2 lidResult) {
        this.lidResult = lidResult;
    }


        
//    @Override
//    public String toString(){
//        StringBuilder sb = new StringBuilder();
//        sb.append("Qname: ").append(qname).append("; path: ").append(path).append("; lidResults: ");
//        for ( LidResult lr : lidResults) {
//            sb.append("\n");
//            sb.append(lr.toString()).append(" ,");
//        }
//        return sb.toString();
//    }
}
