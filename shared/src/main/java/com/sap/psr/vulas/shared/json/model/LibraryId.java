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

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Human-readable library ID, for instance, a Maven artifact identifier.
 * Can be sorted using group, artifact, timestamp and version (if timestamp is null).
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryId implements Serializable, Comparable<LibraryId> {

	private static final long serialVersionUID = 1L;

	private static Logger log = org.apache.logging.log4j.LogManager.getLogger();	

	@JsonIgnore
	private Long id;

	@JsonProperty("group")
	private String mvnGroup;

	private String artifact;

	private String version;		

	/**
	 * <p>Constructor for LibraryId.</p>
	 */
	public LibraryId() { super(); }

	/**
	 * <p>Constructor for LibraryId.</p>
	 *
	 * @param group a {@link java.lang.String} object.
	 * @param artifact a {@link java.lang.String} object.
	 * @param version a {@link java.lang.String} object.
	 */
	public LibraryId(String group, String artifact, String version) {
		super();
		this.mvnGroup = group;
		this.artifact = artifact;
		this.version = version;
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
	 * <p>Getter for the field <code>mvnGroup</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getMvnGroup() { return mvnGroup; }
	/**
	 * <p>Setter for the field <code>mvnGroup</code>.</p>
	 *
	 * @param group a {@link java.lang.String} object.
	 */
	public void setMvnGroup(String group) { this.mvnGroup = group; }

	/**
	 * <p>Getter for the field <code>artifact</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getArtifact() { return artifact; }
	/**
	 * <p>Setter for the field <code>artifact</code>.</p>
	 *
	 * @param artifact a {@link java.lang.String} object.
	 */
	public void setArtifact(String artifact) { this.artifact = artifact; }

	/**
	 * <p>Getter for the field <code>version</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getVersion() { return version; }
	/**
	 * <p>Setter for the field <code>version</code>.</p>
	 *
	 * @param version a {@link java.lang.String} object.
	 */
	public void setVersion(String version) { this.version = version; }

	/**
	 * Returns true if group, artfiact and version have been specified, false otherwise.
	 *
	 * @return a boolean.
	 */
	@JsonIgnore
	public boolean isDefined() {
		return this.mvnGroup!=null && this.artifact!=null && this.version!=null;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
		result = prime * result + ((mvnGroup == null) ? 0 : mvnGroup.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		LibraryId other = (LibraryId) obj;
		if (artifact == null) {
			if (other.artifact != null)
				return false;
		} else if (!artifact.equals(other.artifact))
			return false;
		if (mvnGroup == null) {
			if (other.mvnGroup != null)
				return false;
		} else if (!mvnGroup.equals(other.mvnGroup))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[").append(this.getMvnGroup()).append("|").append(this.getArtifact()).append("|").append(this.getVersion()).append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Compares this library ID with the specified library ID using group, artifact and version.
	 */
	@Override
	public int compareTo(LibraryId _other) {
		int result = this.getMvnGroup().compareToIgnoreCase(_other.getMvnGroup());
		if(result==0)
			result = this.getArtifact().compareToIgnoreCase(_other.getArtifact());
		if(result==0){
			Version v = new Version(this.getVersion());
			result = v.compareTo(new Version(_other.getVersion()));
		}
		return result;
	}	
}
