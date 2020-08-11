/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
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

/**
 * <p>Bug class.</p>
 *
 */
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
	
	/**
	 * <p>Constructor for Bug.</p>
	 */
	public Bug() { super(); }
	
	/**
	 * <p>Constructor for Bug.</p>
	 *
	 * @param bugId a {@link java.lang.String} object.
	 */
	public Bug(String bugId) {
		super();
		this.bugId = bugId;
	}
	
	/**
	 * <p>Constructor for Bug.</p>
	 *
	 * @param bugId a {@link java.lang.String} object.
	 * @param description a {@link java.lang.String} object.
	 * @param refs a {@link java.util.Collection} object.
	 */
	public Bug(String bugId, String description, Collection<String> refs) {
		super();
		this.bugId = bugId;
		this.description = description;
		this.reference = refs;
	}
	
	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getId() { return id; }
	/**
	 * <p>Setter for the field <code>id</code>.</p>
	 *
	 * @param id a {@link java.lang.Long} object.
	 */
	public void setId(Long id) { this.id = id; }

	/**
	 * <p>Getter for the field <code>bugId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBugId() { return bugId; }
	/**
	 * <p>Setter for the field <code>bugId</code>.</p>
	 *
	 * @param bugid a {@link java.lang.String} object.
	 */
	public void setBugId(String bugid) { this.bugId = bugid; }
	
	/**
	 * <p>Getter for the field <code>bugIdAlt</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBugIdAlt() { return bugIdAlt; }
	/**
	 * <p>Setter for the field <code>bugIdAlt</code>.</p>
	 *
	 * @param bugidAlt a {@link java.lang.String} object.
	 */
	public void setBugIdAlt(String bugidAlt) { this.bugIdAlt = bugidAlt; }
	
	/**
	 * Compares the given String with both {@link #bugId} and {@link #bugIdAlt}.
	 *
	 * @param _id a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	@JsonIgnore
	public boolean hasBugId(String _id) {
		return (this.getBugId()!=null && this.getBugId().equalsIgnoreCase(_id)) || (this.getBugIdAlt()!=null && this.getBugIdAlt().equalsIgnoreCase(_id)); 
	}
	
	/**
	 * <p>Getter for the field <code>maturity</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.ContentMaturityLevel} object.
	 */
	public ContentMaturityLevel getMaturity() { return maturity; }
	/**
	 * <p>Setter for the field <code>maturity</code>.</p>
	 *
	 * @param maturity a {@link com.sap.psr.vulas.shared.enums.ContentMaturityLevel} object.
	 */
	public void setMaturity(ContentMaturityLevel maturity) { this.maturity = maturity; }

	/**
	 * <p>Getter for the field <code>origin</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.BugOrigin} object.
	 */
	public BugOrigin getOrigin() { return origin; }
	/**
	 * <p>Setter for the field <code>origin</code>.</p>
	 *
	 * @param origin a {@link com.sap.psr.vulas.shared.enums.BugOrigin} object.
	 */
	public void setOrigin(BugOrigin origin) { this.origin = origin; }

	/**
	 * <p>Getter for the field <code>description</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDescription() { return description; }
	/**
	 * <p>Setter for the field <code>description</code>.</p>
	 *
	 * @param descr a {@link java.lang.String} object.
	 */
	public void setDescription(String descr) { this.description = descr; }

	/**
	 * <p>Getter for the field <code>descriptionAlt</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDescriptionAlt() { return descriptionAlt; }
	/**
	 * <p>Setter for the field <code>descriptionAlt</code>.</p>
	 *
	 * @param descr a {@link java.lang.String} object.
	 */
	public void setDescriptionAlt(String descr) { this.descriptionAlt = descr; }

	/**
	 * <p>Getter for the field <code>reference</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<String> getReference() {return reference;}
	/**
	 * <p>Setter for the field <code>reference</code>.</p>
	 *
	 * @param reference a {@link java.util.Collection} object.
	 */
	public void setReference(Collection<String> reference) {this.reference = reference;}

	/**
	 * <p>Getter for the field <code>constructChanges</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<ConstructChange> getConstructChanges() { return constructChanges; }
	/**
	 * <p>Setter for the field <code>constructChanges</code>.</p>
	 *
	 * @param constructChanges a {@link java.util.Collection} object.
	 */
	public void setConstructChanges(Collection<ConstructChange> constructChanges) { this.constructChanges = constructChanges; }
	
	/**
	 * <p>Getter for the field <code>affectedVersions</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<AffectedLibrary> getAffectedVersions() { return affectedVersions; }
	/**
	 * <p>Setter for the field <code>affectedVersions</code>.</p>
	 *
	 * @param affectedVersions a {@link java.util.Collection} object.
	 */
	public void setAffectedVersions(Collection<AffectedLibrary> affectedVersions) { this.affectedVersions = affectedVersions; }
	
	/**
	 * <p>Getter for the field <code>createdAt</code>.</p>
	 *
	 * @return a {@link java.util.Calendar} object.
	 */
	public java.util.Calendar getCreatedAt() { return createdAt; }
	/**
	 * <p>Setter for the field <code>createdAt</code>.</p>
	 *
	 * @param createdAt a {@link java.util.Calendar} object.
	 */
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }

	/**
	 * <p>Getter for the field <code>createdBy</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCreatedBy() { return createdBy; }
	/**
	 * <p>Setter for the field <code>createdBy</code>.</p>
	 *
	 * @param createdBy a {@link java.lang.String} object.
	 */
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	/**
	 * <p>Getter for the field <code>modifiedAt</code>.</p>
	 *
	 * @return a {@link java.util.Calendar} object.
	 */
	public java.util.Calendar getModifiedAt() { return modifiedAt; }
	/**
	 * <p>Setter for the field <code>modifiedAt</code>.</p>
	 *
	 * @param modifiedAt a {@link java.util.Calendar} object.
	 */
	public void setModifiedAt(java.util.Calendar modifiedAt) { this.modifiedAt = modifiedAt; }

	/**
	 * <p>Getter for the field <code>modifiedBy</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getModifiedBy() { return modifiedBy; }
	/**
	 * <p>Setter for the field <code>modifiedBy</code>.</p>
	 *
	 * @param modifiedBy a {@link java.lang.String} object.
	 */
	public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
	
	/**
	 * <p>Getter for the field <code>cvssScore</code>.</p>
	 *
	 * @return a {@link java.lang.Float} object.
	 */
	public Float getCvssScore() { return cvssScore; }
	/**
	 * <p>Setter for the field <code>cvssScore</code>.</p>
	 *
	 * @param cvss a {@link java.lang.Float} object.
	 */
	public void setCvssScore(Float cvss) { this.cvssScore = cvss; }
	
	/**
	 * <p>Getter for the field <code>cvssVersion</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCvssVersion() { return cvssVersion; }
	/**
	 * <p>Setter for the field <code>cvssVersion</code>.</p>
	 *
	 * @param cvssVersion a {@link java.lang.String} object.
	 */
	public void setCvssVersion(String cvssVersion) { this.cvssVersion = cvssVersion; }

	/**
	 * <p>Getter for the field <code>cvssVector</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCvssVector() { return cvssVector; }
	/**
	 * <p>Setter for the field <code>cvssVector</code>.</p>
	 *
	 * @param cvssVector a {@link java.lang.String} object.
	 */
	public void setCvssVector(String cvssVector) { this.cvssVector = cvssVector; }
	
	/**
	 * <p>getCvssDisplayString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCvssDisplayString() {
		if(this.getCvssScore()==null || this.getCvssVersion()==null)
			return com.sap.psr.vulas.shared.json.model.Bug.CVSS_NA;
		else
			return this.getCvssScore() + " (v" + this.getCvssVersion() + ")";
	}

	/**
	 * <p>getAffLibIdsCounter.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.metrics.Metrics} object.
	 */
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

	/**
	 * <p>countConstructChanges.</p>
	 *
	 * @return a int.
	 */
	@JsonProperty(value = "countConstructChanges")
	@JsonView(Views.BugDetails.class)
	public int countConstructChanges() { return (this.getConstructChanges()==null ? -1 : this.getConstructChanges().size()); }
	
	/**
	 * <p>prePersist.</p>
	 */
	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null)
			this.setCreatedAt(Calendar.getInstance());
		this.setModifiedAt(Calendar.getInstance());
	}
	
	/**
	 * <p>preUpdate.</p>
	 */
	@PreUpdate
	public void preUpdate() {
		this.setModifiedAt(Calendar.getInstance());
	}
	
	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return this.toString(false);
	}
	
	/**
	 * <p>toString.</p>
	 *
	 * @param _deep a boolean.
	 * @return a {@link java.lang.String} object.
	 */
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

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
		return result;
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
		Bug other = (Bug) obj;
		if (bugId == null) {
			if (other.bugId != null)
				return false;
		} else if (!bugId.equals(other.bugId))
			return false;
		return true;
	}
}
