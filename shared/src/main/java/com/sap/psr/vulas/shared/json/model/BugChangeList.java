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


/**
 * <p>BugChangeList class.</p>
 *
 */
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

    
    
    /**
     * <p>Getter for the field <code>cvssScore</code>.</p>
     *
     * @return a {@link java.lang.Float} object.
     */
    public Float getCvssScore() {
		return cvssScore;
	}

	/**
	 * <p>Setter for the field <code>cvssScore</code>.</p>
	 *
	 * @param cvssScore a {@link java.lang.Float} object.
	 */
	public void setCvssScore(Float cvssScore) {
		this.cvssScore = cvssScore;
	}

	/**
	 * <p>Getter for the field <code>cvssVersion</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCvssVersion() {
		return cvssVersion;
	}

	/**
	 * <p>Setter for the field <code>cvssVersion</code>.</p>
	 *
	 * @param cvssVersion a {@link java.lang.String} object.
	 */
	public void setCvssVersion(String cvssVersion) {
		this.cvssVersion = cvssVersion;
	}

	/**
	 * <p>Getter for the field <code>cvssVector</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCvssVector() {
		return cvssVector;
	}

	/**
	 * <p>Setter for the field <code>cvssVector</code>.</p>
	 *
	 * @param cvssVector a {@link java.lang.String} object.
	 */
	public void setCvssVector(String cvssVector) {
		this.cvssVector = cvssVector;
	}
	
	/**
	 * <p>Getter for the field <code>cvssDisplayString</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCvssDisplayString() {
		return cvssDisplayString;
	}

	/**
	 * <p>Setter for the field <code>cvssDisplayString</code>.</p>
	 *
	 * @param cvssDisplayString a {@link java.lang.String} object.
	 */
	public void setCvssDisplayString(String cvssDisplayString) {
		this.cvssDisplayString = cvssDisplayString;
	}

	/**
	 * <p>Getter for the field <code>bugIdAlt</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBugIdAlt() {
		return bugIdAlt;
	}

	/**
	 * <p>Setter for the field <code>bugIdAlt</code>.</p>
	 *
	 * @param bugIdAlt a {@link java.lang.String} object.
	 */
	public void setBugIdAlt(String bugIdAlt) {
		this.bugIdAlt = bugIdAlt;
	}

	/**
	 * <p>Getter for the field <code>maturity</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.ContentMaturityLevel} object.
	 */
	public ContentMaturityLevel getMaturity() {
		return maturity;
	}

	/**
	 * <p>Setter for the field <code>maturity</code>.</p>
	 *
	 * @param maturity a {@link com.sap.psr.vulas.shared.enums.ContentMaturityLevel} object.
	 */
	public void setMaturity(ContentMaturityLevel maturity) {
		this.maturity = maturity;
	}

	/**
	 * <p>Getter for the field <code>origin</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.BugOrigin} object.
	 */
	public BugOrigin getOrigin() {
		return origin;
	}

	/**
	 * <p>Setter for the field <code>origin</code>.</p>
	 *
	 * @param origin a {@link com.sap.psr.vulas.shared.enums.BugOrigin} object.
	 */
	public void setOrigin(BugOrigin origin) {
		this.origin = origin;
	}

	/**
	 * <p>Getter for the field <code>countAffLibIds</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
	 */
	public Metrics getCountAffLibIds() {
		return countAffLibIds;
	}

	/**
	 * <p>Setter for the field <code>countAffLibIds</code>.</p>
	 *
	 * @param metrics a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
	 */
	public void setCountAffLibIds(Metrics metrics) {
		this.countAffLibIds = metrics;
	}

	/**
	 * <p>Getter for the field <code>description</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDescription() {
        return description;
    }

    /**
     * <p>Setter for the field <code>description</code>.</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        this.description = description;
    }

	/**
	 * <p>Getter for the field <code>descriptionAlt</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDescriptionAlt() {
        return descriptionAlt;
    }

    /**
     * <p>Setter for the field <code>descriptionAlt</code>.</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescriptionAlt(String description) {
        this.descriptionAlt = description;
    }

    /**
     * <p>Getter for the field <code>reference</code>.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getReference() {
		return reference;
	}

	/**
	 * <p>Setter for the field <code>reference</code>.</p>
	 *
	 * @param reference a {@link java.util.Collection} object.
	 */
	public void setReference(Collection<String> reference) {
		this.reference = reference;
	}

	/**
	 * <p>Getter for the field <code>constructChanges</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<ConstructChange> getConstructChanges() {
        return constructChanges;
    }

    /**
     * <p>Setter for the field <code>constructChanges</code>.</p>
     *
     * @param constructChanges a {@link java.util.Collection} object.
     */
    public void setConstructChanges(Collection<ConstructChange> constructChanges) {
        this.constructChanges = constructChanges;
    }

    /**
     * <p>Getter for the field <code>affectedVersions</code>.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<AffectedLibrary> getAffectedVersions() {
        return affectedVersions;
    }

    /**
     * <p>Setter for the field <code>affectedVersions</code>.</p>
     *
     * @param affectedVersions a {@link java.util.Collection} object.
     */
    public void setAffectedVersions(Collection<AffectedLibrary> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    /**
     * <p>Getter for the field <code>createdAt</code>.</p>
     *
     * @return a {@link java.util.Calendar} object.
     */
    public Calendar getCreatedAt() {
        return createdAt;
    }

    /**
     * <p>Setter for the field <code>createdAt</code>.</p>
     *
     * @param createdAt a {@link java.util.Calendar} object.
     */
    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * <p>Getter for the field <code>createdBy</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * <p>Setter for the field <code>createdBy</code>.</p>
     *
     * @param createdBy a {@link java.lang.String} object.
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * <p>Getter for the field <code>modifiedAt</code>.</p>
     *
     * @return a {@link java.util.Calendar} object.
     */
    public Calendar getModifiedAt() {
        return modifiedAt;
    }

    /**
     * <p>Setter for the field <code>modifiedAt</code>.</p>
     *
     * @param modifiedAt a {@link java.util.Calendar} object.
     */
    public void setModifiedAt(Calendar modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    /**
     * <p>Getter for the field <code>modifiedBy</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getModifiedBy() {
        return modifiedBy;
    }

    /**
     * <p>Setter for the field <code>modifiedBy</code>.</p>
     *
     * @param modifiedBy a {@link java.lang.String} object.
     */
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
    private String modifiedBy;
    
    private int countConstructChanges;

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
     * <p>Getter for the field <code>countConstructChanges</code>.</p>
     *
     * @return a int.
     */
    public int getCountConstructChanges() {
        return countConstructChanges;
    }

    /**
     * <p>Setter for the field <code>countConstructChanges</code>.</p>
     *
     * @param countConstructChanges a int.
     */
    public void setCountConstructChanges(int countConstructChanges) {
        this.countConstructChanges = countConstructChanges;
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
