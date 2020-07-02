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
package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.util.Collection;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.json.model.AffectedConstructChange;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;

/**
 * <p>AffectedLibrary class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "createdAt", "createdBy" })
public class AffectedLibrary implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Long id;
	
	@JsonBackReference
	private Bug bugId;

	private LibraryId libraryId;
	
    private Library lib;
        
	private AffectedVersionSource source;
	
	private Boolean affected;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;
	
	private String createdBy;
	
	private String explanation;
	
	private String lastVulnerable;
	
	private String firstFixed;
	
	private String fromIntersection;
	
	private String toIntersection;
	
	private Boolean sourcesAvailable;
	
	private String ADFixed;
	
	private String ADPathFixed;
	
	private Collection<AffectedConstructChange> affectedcc;
	
	private String overallConfidence;
	
	private String pathConfidence;	
	
	/**
	 * <p>Constructor for AffectedLibrary.</p>
	 */
	public AffectedLibrary() { super(); }

	/**
	 * <p>Constructor for AffectedLibrary.</p>
	 *
	 * @param bug a {@link com.sap.psr.vulas.shared.json.model.Bug} object.
	 * @param libraryId a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
	 * @param affected a {@link java.lang.Boolean} object.
	 */
	public AffectedLibrary(Bug bug, LibraryId libraryId, Boolean affected) {
		super();
		this.bugId = bug;
		this.libraryId = libraryId;
		this.affected = affected;
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
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Bug} object.
	 */
	public Bug getBugId() { return bugId; }
	
	/**
	 * <p>Setter for the field <code>bugId</code>.</p>
	 *
	 * @param bug a {@link com.sap.psr.vulas.shared.json.model.Bug} object.
	 */
	public void setBugId(Bug bug) { this.bugId = bug; }
	
	/**
	 * <p>Getter for the field <code>libraryId</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
	 */
	public LibraryId getLibraryId() { return libraryId; }
	
	/**
	 * <p>Setter for the field <code>libraryId</code>.</p>
	 *
	 * @param libraryId a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
	 */
	public void setLibraryId(LibraryId libraryId) { this.libraryId = libraryId; }

	/**
	 * <p>Getter for the field <code>affected</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getAffected() { return affected; }
	
	/**
	 * <p>Setter for the field <code>affected</code>.</p>
	 *
	 * @param affected a {@link java.lang.Boolean} object.
	 */
	public void setAffected(Boolean affected) { this.affected = affected; }
	
	/**
	 * <p>Getter for the field <code>source</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.AffectedVersionSource} object.
	 */
	public AffectedVersionSource getSource() { return source; }
	
	/**
	 * <p>Setter for the field <code>source</code>.</p>
	 *
	 * @param source a {@link com.sap.psr.vulas.shared.enums.AffectedVersionSource} object.
	 */
	public void setSource(AffectedVersionSource source) { this.source = source; }

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
	 * <p>Getter for the field <code>explanation</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getExplanation() { return explanation; }
	
	/**
	 * <p>Setter for the field <code>explanation</code>.</p>
	 *
	 * @param explanation a {@link java.lang.String} object.
	 */
	public void setExplanation(String explanation) { this.explanation = explanation; }

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
		result = prime * result + ((libraryId == null) ? 0 : libraryId.hashCode());
		result = prime * result + ((lib == null) ? 0 : lib.hashCode());
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
		AffectedLibrary other = (AffectedLibrary) obj;
		if (bugId == null) {
			if (other.bugId != null)
				return false;
		} else if (!bugId.equals(other.bugId))
			return false;
		if (libraryId == null) {
			if (other.libraryId != null)
				return false;
		} else if (!libraryId.equals(other.libraryId))
			return false;
		if (lib == null) {
			if (other.lib != null)
				return false;
		} else if (!lib.equals(other.lib))
			return false;
		return true;
	}

	/**
	 * <p>Getter for the field <code>lib</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Library} object.
	 */
	public Library getLib() {
		return lib;
	}

	/**
	 * <p>Setter for the field <code>lib</code>.</p>
	 *
	 * @param lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
	 */
	public void setLib(Library lib) {
		this.lib = lib;
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
			builder.append("Library affected: [").append(this.getAffected()).append("]").append(System.getProperty("line.separator"));
			builder.append("    Bug ").append(this.getBugId());
			builder.append("    LibraryId ").append(this.getLibraryId());
		}
		else {
			builder.append("[").append(this.getId()).append(":").append(this.getBugId()).append(":affected=").append(this.getAffected()).append("]");
		}
		return builder.toString();
	}
	
	/**
	 * <p>Getter for the field <code>sourcesAvailable</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public Boolean getSourcesAvailable() {
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
	 * <p>Getter for the field <code>ADFixed</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getADFixed() {
		return ADFixed;
	}

	/**
	 * <p>Setter for the field <code>aDFixed</code>.</p>
	 *
	 * @param aDFixed a {@link java.lang.String} object.
	 */
	public void setADFixed(String aDFixed) {
		ADFixed = aDFixed;
	}

	/**
	 * <p>Getter for the field <code>ADPathFixed</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getADPathFixed() {
		return ADPathFixed;
	}

	/**
	 * <p>Setter for the field <code>aDPathFixed</code>.</p>
	 *
	 * @param aDPathFixed a {@link java.lang.String} object.
	 */
	public void setADPathFixed(String aDPathFixed) {
		ADPathFixed = aDPathFixed;
	}

	/**
	 * <p>Getter for the field <code>affectedcc</code>.</p>
	 *
	 * @return a collection of {@link com.sap.psr.vulas.shared.json.model.AffectedConstructChange} object.
	 */
	public Collection<AffectedConstructChange> getAffectedcc() {
		return affectedcc;
	}

	/**
	 * <p>Setter for the field <code>affectedcc</code>.</p>
	 *
	 * @param affectedcc a collection of {@link com.sap.psr.vulas.shared.json.model.AffectedConstructChange} object.
	 */
	public void setAffectedcc(Collection<AffectedConstructChange> affectedcc) {
		this.affectedcc = affectedcc;
	}

	/**
	 * <p>Getter for the field <code>overallConfidence</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getOverallConfidence() {
		return overallConfidence;
	}

	/**
	 * <p>Setter for the field <code>overallConfidence</code>.</p>
	 *
	 * @param overallConfidence a {@link java.lang.String} object.
	 */
	public void setOverallConfidence(String overallConfidence) {
		this.overallConfidence = overallConfidence;
	}

	/**
	 * <p>Getter for the field <code>pathConfidence</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPathConfidence() {
		return pathConfidence;
	}

	/**
	 * <p>Setter for the field <code>pathConfidence</code>.</p>
	 *
	 * @param pathConfidence a {@link java.lang.String} object.
	 */
	public void setPathConfidence(String pathConfidence) {
		this.pathConfidence = pathConfidence;
	}

	/**
	 * <p>Getter for the field <code>lastVulnerable</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLastVulnerable() {
		return lastVulnerable;
	}

	/**
	 * <p>Setter for the field <code>lastVulnerable</code>.</p>
	 *
	 * @param lastVulnerable a {@link java.lang.String} object.
	 */
	public void setLastVulnerable(String lastVulnerable) {
		this.lastVulnerable = lastVulnerable;
	}

	/**
	 * <p>Getter for the field <code>firstFixed</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFirstFixed() {
		return firstFixed;
	}

	/**
	 * <p>Setter for the field <code>firstFixed</code>.</p>
	 *
	 * @param firstFixed a {@link java.lang.String} object.
	 */
	public void setFirstFixed(String firstFixed) {
		this.firstFixed = firstFixed;
	}

	/**
	 * <p>Getter for the field <code>fromIntersection</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFromIntersection() {
		return fromIntersection;
	}

	/**
	 * <p>Setter for the field <code>fromIntersection</code>.</p>
	 *
	 * @param fromIntersection a {@link java.lang.String} object.
	 */
	public void setFromIntersection(String fromIntersection) {
		this.fromIntersection = fromIntersection;
	}

	/**
	 * <p>Getter for the field <code>toIntersection</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getToIntersection() {
		return toIntersection;
	}

	/**
	 * <p>Setter for the field <code>toIntersection</code>.</p>
	 *
	 * @param toIntersection a {@link java.lang.String} object.
	 */
	public void setToIntersection(String toIntersection) {
		this.toIntersection = toIntersection;
	}

}
