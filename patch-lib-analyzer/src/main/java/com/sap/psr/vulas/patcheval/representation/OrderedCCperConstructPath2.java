/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.json.model.ConstructChange;
import com.sap.psr.vulas.shared.json.model.ConstructId;


/**
 * Helper Class used during PatchEvalManager.
 */
public class OrderedCCperConstructPath2{
	
	private static final Log log = LogFactory.getLog(OrderedCCperConstructPath2.class);
	
    private ConstructId constructId;
    private String repoPath;
	private SortedSet<ConstructChange> changes = new TreeSet<ConstructChange>();

    /**
     * <p>Constructor for OrderedCCperConstructPath2.</p>
     *
     * @param constructId a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
     * @param repoPath a {@link java.lang.String} object.
     */
    public OrderedCCperConstructPath2(ConstructId constructId, String repoPath) {
        this.constructId = constructId;
        this.repoPath = repoPath;
    }
    
    /**
     * <p>addConstructChange.</p>
     *
     * @param _construct_change a {@link com.sap.psr.vulas.shared.json.model.ConstructChange} object.
     */
    public void addConstructChange(ConstructChange _construct_change) {
		this.changes.add(_construct_change);
	}

    /**
     * <p>Getter for the field <code>constructId</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
     */
    public ConstructId getConstructId() {
        return constructId;
    }

    /**
     * <p>Setter for the field <code>constructId</code>.</p>
     *
     * @param constructId a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
     */
    public void setConstructId(ConstructId constructId) {
        this.constructId = constructId;
    }

    /**
     * <p>Getter for the field <code>repoPath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRepoPath() {
        return repoPath;
    }

    /**
     * <p>Setter for the field <code>repoPath</code>.</p>
     *
     * @param repoPath a {@link java.lang.String} object.
     */
    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
    	if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
    	OrderedCCperConstructPath2 other = (OrderedCCperConstructPath2)obj;
        return other.getConstructId().equals(this.getConstructId()) && other.getRepoPath().equals(this.repoPath);
    }
    
    /** {@inheritDoc} */
    @Override 
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(constructId.getQname()).append(":").append(repoPath);
        return sb.toString();
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode(){
        return this.constructId.hashCode()+this.repoPath.hashCode();
    }
    
	/**
	 * Returns the type of change considering all commits.
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
	 */
	public ConstructChangeType getOverallChangeType() {
		if(this.isConstructExistedBeforeFirstCommit() && this.isConstructExistsAfterLastCommit()) {
			return ConstructChangeType.MOD;
		}
		else if(this.isConstructExistedBeforeFirstCommit() && !this.isConstructExistsAfterLastCommit()) {
			return ConstructChangeType.DEL;
		}
		else if(!this.isConstructExistedBeforeFirstCommit() && this.isConstructExistsAfterLastCommit()) {
			return ConstructChangeType.ADD;
		}
		// Strange case: A construct has been added as part of a commit, but removed later on.
		// Example: CVE-2012-2098, repo http://svn.apache.org/repos/asf/commons/proper/compress
		// 			Method org.apache.commons.compress.compressors.bzip2.BlockSort.randomiseBlock(Data,int)
		// 			Added as part of commit 1332540, deleted as part of commit 1340790
		else {
			OrderedCCperConstructPath2.log.info("Construct [" + this.constructId + "] only existed temporarily (during fix development) and will be ignored for the fix containement check");
			return ConstructChangeType.NUL;
		}
	}

	/**
	 * <p>isConstructExistedBeforeFirstCommit.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isConstructExistedBeforeFirstCommit() {
		if(this.changes.size()==0) throw new IllegalStateException("No commits exist");
		return !changes.first().getConstructChangeType().equals(ConstructChangeType.ADD);
	}

	/**
	 * <p>isConstructExistsAfterLastCommit.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isConstructExistsAfterLastCommit() {
		if(this.changes.size()==0) throw new IllegalStateException("No commits exist");
		return !changes.last().getConstructChangeType().equals(ConstructChangeType.DEL);
	}

	private String getBefore() {
		String s = null;
		if(this.changes.first().getBuggyBody()!=null){
			s = this.changes.first().getBuggyBody();
                }
                return s;
	}

	private String getAfter() {
		String s = null;
		if(this.changes.last().getFixedBody()!=null){
			s = this.changes.last().getFixedBody();
                }
		return s;
	}
	
	/**
	 * If the overall change type is MOD(ified), the method returns the signature change considering
	 * all commits. In the other cases, it returns null.
	 *
	 * @return a {@link com.sap.psr.vulas.patcheval.representation.OverallConstructChange} object.
	 */
	public OverallConstructChange getOverallCC() {

        if(this.getOverallChangeType().equals(ConstructChangeType.MOD)) {
            if(this.changes.first().getBuggyBody()!=null && this.changes.last().getFixedBody()!=null) {
            	return new OverallConstructChange(this.getAfter(), this.getBefore(), this.getOverallChangeType(), this.repoPath,this.constructId);
                                    }
        }
        return new OverallConstructChange(null, null, this.getOverallChangeType(), this.repoPath,this.constructId);
                		
	}

    
}
