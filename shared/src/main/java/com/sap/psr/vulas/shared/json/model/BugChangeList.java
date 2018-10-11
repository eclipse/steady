/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.shared.json.model;
import java.util.Calendar;
import java.util.Collection;


import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;
import com.sap.psr.vulas.shared.json.model.metrics.Metrics;


public class BugChangeList{

    private String bugId;
    
    private String bugIdAlt = null;

	private ContentMaturityLevel maturity;

	private BugOrigin origin;
	
	private Float cvssScore = null;
	
	private String cvssVersion = null;
	
	private String cvssVector = null;
	
	private String cvssDisplayString = null;
	
    private String source;
    private String description;
    private String descriptionAlt;	
	private Collection<String> reference;
    private Collection<ConstructChange> constructChanges;
    private Collection<AffectedLibrary> affectedVersions;
    private java.util.Calendar createdAt;
    private String createdBy;
    private java.util.Calendar modifiedAt;
    private Metrics countAffLibIds;

    
    
    public Float getCvssScore() {
		return cvssScore;
	}

	public void setCvssScore(Float cvssScore) {
		this.cvssScore = cvssScore;
	}

	public String getCvssVersion() {
		return cvssVersion;
	}

	public void setCvssVersion(String cvssVersion) {
		this.cvssVersion = cvssVersion;
	}

	public String getCvssVector() {
		return cvssVector;
	}

	public void setCvssVector(String cvssVector) {
		this.cvssVector = cvssVector;
	}
	
	public String getCvssDisplayString() {
		return cvssDisplayString;
	}

	public void setCvssDisplayString(String cvssDisplayString) {
		this.cvssDisplayString = cvssDisplayString;
	}

	public String getBugIdAlt() {
		return bugIdAlt;
	}

	public void setBugIdAlt(String bugIdAlt) {
		this.bugIdAlt = bugIdAlt;
	}

	public ContentMaturityLevel getMaturity() {
		return maturity;
	}

	public void setMaturity(ContentMaturityLevel maturity) {
		this.maturity = maturity;
	}

	public BugOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(BugOrigin origin) {
		this.origin = origin;
	}

	public Metrics getCountAffLibIds() {
		return countAffLibIds;
	}

	public void setCountAffLibIds(Metrics metrics) {
		this.countAffLibIds = metrics;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public String getDescriptionAlt() {
        return descriptionAlt;
    }

    public void setDescriptionAlt(String description) {
        this.descriptionAlt = description;
    }

    public Collection<String> getReference() {
		return reference;
	}

	public void setReference(Collection<String> reference) {
		this.reference = reference;
	}

	public Collection<ConstructChange> getConstructChanges() {
        return constructChanges;
    }

    public void setConstructChanges(Collection<ConstructChange> constructChanges) {
        this.constructChanges = constructChanges;
    }

    public Collection<AffectedLibrary> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(Collection<AffectedLibrary> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Calendar getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Calendar modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
    private String modifiedBy;
    
    private int countConstructChanges;

    public String getBugId() {
        return bugId;
    }

    public void setBugId(String bugId) {
        this.bugId = bugId;
    }   

    public int getCountConstructChanges() {
        return countConstructChanges;
    }

    public void setCountConstructChanges(int countConstructChanges) {
        this.countConstructChanges = countConstructChanges;
    }
    
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    
}
