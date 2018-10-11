package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;
import com.sap.psr.vulas.shared.json.model.metrics.Counter;
import com.sap.psr.vulas.shared.json.model.metrics.Metrics;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value = { "createdAt", "createdBy", "modifiedAt" }, allowGetters=true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
@Entity
@Table( name="Bug", uniqueConstraints=@UniqueConstraint( columnNames = { "bugId" } ), indexes = {@Index(name = "bugId_index",  columnList="bugId", unique = true)} )
public class Bug implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	@Column(nullable = false, length = 32)
	private String bugId = null;
	
	@Column(nullable = true, length = 32)
	private String bugIdAlt = null;
		
	@Column(nullable = false, length = 5)
	@Enumerated(EnumType.STRING)
	private ContentMaturityLevel maturity;
	
	@Column(nullable = false, length = 6)
	@Enumerated(EnumType.STRING)
	private BugOrigin origin;
		
	@Column(columnDefinition = "text")
	private String description = null; // Can be overwritten with external vuln information, e.g., from official CVE
	
	@Column(columnDefinition = "text")
	private String descriptionAlt = null;
	
	@Column(nullable = true)
	private Float cvssScore = null; // Can be overwritten with external vuln information, e.g., from official CVE
	
	@Column(nullable = true, length = 5)
	private String cvssVersion = null; // Can be overwritten with external vuln information, e.g., from official CVE
	
	@Column(nullable = true, length = 100)
	private String cvssVector = null; // Can be overwritten with external vuln information, e.g., from official CVE
	
	@ElementCollection
	@CollectionTable(name="BugReferences")
	private Collection<String> reference;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "bug", fetch = FetchType.LAZY, orphanRemoval=true)
	@JsonManagedReference
	@JsonView(Views.BugDetails.class)
	private Collection<ConstructChange> constructChanges;
	
	@OneToMany(cascade = {}, mappedBy = "bugId", fetch = FetchType.LAZY)
	//@JsonManagedReference
	@JsonView(Views.Never.class)
	private Collection<AffectedLibrary> affectedVersions;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;
	
	@Column
	private String createdBy;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar modifiedAt;
	
	@Column
	private String modifiedBy;
	
	public Bug() { super(); }
	
	public Bug(String bugId) {
		super();
		this.bugId = bugId;
	}
	
	public Bug(String bugId, String description, Collection<String> refs) {
		super();
		this.bugId = bugId;
		this.description = description;
		this.reference = refs;
	}
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getBugId() { return bugId; }
	public void setBugId(String bugid) { this.bugId = bugid; }
	
	public String getBugIdAlt() { return bugIdAlt; }
	public void setBugIdAlt(String bugidAlt) { this.bugIdAlt = bugidAlt; }
	
	/**
	 * Compares the given String with both {@link #bugId} and {@link #bugIdAlt}.
	 * 
	 * @param _id
	 * @return
	 */
	@JsonIgnore
	public boolean hasBugId(String _id) {
		return (this.getBugId()!=null && this.getBugId().equalsIgnoreCase(_id)) || (this.getBugIdAlt()!=null && this.getBugIdAlt().equalsIgnoreCase(_id)); 
	}
	
	public ContentMaturityLevel getMaturity() { return maturity; }
	public void setMaturity(ContentMaturityLevel maturity) { this.maturity = maturity; }

	public BugOrigin getOrigin() { return origin; }
	public void setOrigin(BugOrigin origin) { this.origin = origin; }

	public String getDescription() { return description; }
	public void setDescription(String descr) { this.description = descr; }

	public String getDescriptionAlt() { return descriptionAlt; }
	public void setDescriptionAlt(String descr) { this.descriptionAlt = descr; }

	public Collection<String> getReference() {return reference;}
	public void setReference(Collection<String> reference) {this.reference = reference;}

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
	
	public Float getCvssScore() { return cvssScore; }
	public void setCvssScore(Float cvss) { this.cvssScore = cvss; }
	
	public String getCvssVersion() { return cvssVersion; }
	public void setCvssVersion(String cvssVersion) { this.cvssVersion = cvssVersion; }

	public String getCvssVector() { return cvssVector; }
	public void setCvssVector(String cvssVector) { this.cvssVector = cvssVector; }
	
	public String getCvssDisplayString() {
		if(this.getCvssScore()==null || this.getCvssVersion()==null)
			return com.sap.psr.vulas.shared.json.model.Bug.CVSS_NA;
		else
			return this.getCvssScore() + " (v" + this.getCvssVersion() + ")";
	}

	@JsonProperty(value = "countAffLibIds")
	@JsonView(Views.BugDetails.class)
	public Metrics getAffLibIdsCounter() {
		final Metrics metrics = new Metrics();
		List<Counter> l = new ArrayList<Counter>();
		HashMap<AffectedVersionSource,Integer> avCount = new HashMap<AffectedVersionSource,Integer>();
		//instatiate counter(name,int) based on return value of findAffLIbper Source and Bug
		Collection<AffectedLibrary> c = this.getAffectedVersions();
		if(c!=null){
			for (AffectedLibrary a : c){
				if(avCount.containsKey(a.getSource())){
					avCount.put(a.getSource(), avCount.get(a.getSource()) + 1);
				}
				else{
					avCount.put(a.getSource(), 1);
				}
			}
			for(Entry<AffectedVersionSource, Integer> e:avCount.entrySet()){
				l.add(new Counter(e.getKey().toString(),e.getValue()));
			}
			metrics.setCounters(l);
		}
		return metrics;
	}	

	@JsonProperty(value = "countConstructChanges")
	@JsonView(Views.BugDetails.class)
	public int countConstructChanges() { return (this.getConstructChanges()==null ? -1 : this.getConstructChanges().size()); }
	
	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null)
			this.setCreatedAt(Calendar.getInstance());
		this.setModifiedAt(Calendar.getInstance());
	}
	
	@PreUpdate
	public void preUpdate() {
		this.setModifiedAt(Calendar.getInstance());
	}
	
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
			builder.append("[bugid=").append(this.getBugId());
			if(this.getConstructChanges()!=null) {
				builder.append(", #changes=").append(this.getConstructChanges().size());
			}
			builder.append("]");
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
}