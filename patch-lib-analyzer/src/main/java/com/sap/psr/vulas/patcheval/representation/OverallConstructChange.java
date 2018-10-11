/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

import com.sap.psr.vulas.shared.enums.ConstructChangeType;

import com.sap.psr.vulas.shared.json.model.ConstructId;



/**
 * class representing a consolidated change list 
 * (no duplicates because of multiple commits on the same construct)
 */
public class OverallConstructChange {
    String fixedBody, buggyBody;
    ConstructChangeType changetype;
    String repoPath;
    ConstructId constructId;
    
    public OverallConstructChange(String fixedBody, String buggyBody, ConstructChangeType changetype, String repoPath, ConstructId constructId) {
        this.fixedBody = fixedBody;
        this.buggyBody = buggyBody;
        this.changetype = changetype;
        this.repoPath = repoPath;
        this.constructId = constructId;
    }
    
   

    public String getFixedBody() {
        return fixedBody;
    }

    public void setFixedBody(String fixedBody) {
        this.fixedBody = fixedBody;
    }

    public String getBuggyBody() {
        return buggyBody;
    }

    public void setBuggyBody(String buggyBody) {
        this.buggyBody = buggyBody;
    }

    public ConstructChangeType getChangeType() {
        return changetype;
    }

    public void setChangeType(ConstructChangeType changetype) {
        this.changetype = changetype;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public ConstructId getConstructId() {
        return constructId;
    }

    public void setConstructId(ConstructId constructId) {
        this.constructId = constructId;
    }

    public ConstructChangeType getChangetype() {
        return changetype;
    }

    public void setChangetype(ConstructChangeType changetype) {
        this.changetype = changetype;
    }

    
    
}
