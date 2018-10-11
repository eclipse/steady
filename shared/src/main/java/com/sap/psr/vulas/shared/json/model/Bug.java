package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value = { "affectedVersions", "createdAt", "createdBy" }, allowGetters=true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
public class Bug implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;
	
	public static final String CVSS_NA = "n/a";

	@JsonIgnore
	private Long id;

	private String bugId = null;

	private String bugIdAlt = null;

	private ContentMaturityLevel maturity;

	private BugOrigin origin;
	
	private Float cvssScore = null;
	
	private String cvssVersion = null;
	
	private String cvssVector = null;
	
	private String cvssDisplayString = null;
	
	/**
	 * Indicates the source of the bug information, e.g., the NVD or vendor-specific advisories.
	 * Can be used to collect further information (using the bugId as primary key in the external information source).
	 */
	private String source = null;

	private String description = null;

	private String descriptionAlt = null;

	private Collection<String> reference = null;

	@JsonManagedReference
	private Collection<ConstructChange> constructChanges;

	@JsonManagedReference
	private Collection<AffectedLibrary> affectedVersions;

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;

	private String createdBy;

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar modifiedAt;

	private String modifiedBy;

	public Bug() { super(); }

	public Bug(String bugId) {
		super();
		this.bugId = bugId;
	}

	public Bug(String bugId, String source, String description, Collection<String> refs) {
		super();
		this.bugId = bugId;
		this.source = source;
		this.description = description;
		this.reference = refs;
	}

	public Collection<String> getReference() {	return reference; }

	public void setReference(Collection<String> reference) {	this.reference = reference;	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getBugId() { return bugId; }
	public void setBugId(String bugid) { this.bugId = bugid; }

	public String getSource() { return source; }
	public void setSource(String source) { this.source = source; }

	public String getDescription() { return description; }
	public void setDescription(String descr) { this.description = descr; }
	
	

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

	public String getDescriptionAlt() {
		return descriptionAlt;
	}

	public void setDescriptionAlt(String descriptionAlt) {
		this.descriptionAlt = descriptionAlt;
	}

	public Collection<ConstructChange> getConstructChanges() { return constructChanges; }
	public void setConstructChanges(Collection<ConstructChange> constructChanges) { this.constructChanges = constructChanges; }

	public Collection<AffectedLibrary> getAffectedVersions() { return affectedVersions; }
	public void setAffectedVersions(Collection<AffectedLibrary> affectedVersions) { this.affectedVersions = affectedVersions; }

	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }

	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

	public java.util.Calendar getModifiedAt() { return modifiedAt; }
	public void setModifiedAt(java.util.Calendar modifiedAt) { this.modifiedAt = modifiedAt; }

	public String getModifiedBy() { return modifiedBy; }
	public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

	@JsonProperty(value = "countConstructChanges")
	public int countConstructChanges() { return (this.getConstructChanges()==null ? -1 : this.getConstructChanges().size()); }

	@Override
	public final String toString() {
		return this.toString(false);
	}

	public final String toString(boolean _deep) {
		final StringBuilder builder = new StringBuilder();
		if(_deep) {
			builder.append("Bug ").append(this.toString(false)).append(System.getProperty("line.separator"));
			for(ConstructChange cc: this.getConstructChanges()) {
				builder.append("    construct change ").append(cc);
				builder.append(", construct ID ").append(cc.getConstructId()).append(System.getProperty("line.separator"));
			}
		}
		else {
			builder.append("[").append(this.getId()).append(":").append(this.getBugId()).append("]");
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bug other = (Bug) obj;
		if (bugId == null) {
			if (other.bugId != null)
				return false;
		} else if (!bugId.equals(other.bugId))
			return false;
		return true;
	}

	/**
	 * Compares on the basis of the {@link #bugId}.
	 */
	@Override
	public int compareTo(Object o) {
		return this.getBugId().compareToIgnoreCase(((Bug)o).getBugId());
	}
}
