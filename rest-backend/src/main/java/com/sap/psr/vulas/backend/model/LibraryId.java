package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;


/**
 * Human-readable library ID, e.g., a Maven artifact identifier.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table( name="LibraryId", uniqueConstraints=@UniqueConstraint( columnNames = { "mvnGroup", "artifact", "version" } ) )
//@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public class LibraryId implements Serializable,Comparable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	@Column(nullable = false, length = 512)
	@JsonProperty("group")
	private String mvnGroup;

	@Column(nullable = false, length = 512)
	private String artifact;
	
	@Column(nullable = false, length = 128)
	private String version;
	
	@OneToMany(mappedBy = "libraryId")
	@JsonView(Views.LibraryIdDetails.class)
//	@JsonBackReference
	private Collection<AffectedLibrary> affLibraries;
	
	public LibraryId() { super(); }
	
	public LibraryId(String group, String artifact, String version) {
		super();
		this.mvnGroup = group;
		this.artifact = artifact;
		this.version = version;
	}
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getMvnGroup() { return mvnGroup; }
	public void setMvnGroup(String group) { this.mvnGroup = group; }

	public String getArtifact() { return artifact; }
	public void setArtifact(String artifact) { this.artifact = artifact; }

	public String getVersion() { return version; }
	public void setVersion(String version) { this.version = version; }
	
	public Collection<AffectedLibrary> getAffLibraries() { return affLibraries; }
	public void setAffLibraries(Collection<AffectedLibrary> a) { this.affLibraries = a; }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
		result = prime * result + ((mvnGroup == null) ? 0 : mvnGroup.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
	
	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[").append(this.getId()).append(":").append(this.getMvnGroup()).append("|").append(this.getArtifact()).append("|").append(this.getVersion()).append("]");
		return builder.toString();
	}
	
	/**
	 * Returns true if the given {@link LibraryId} only differs in the version while group and artifact are identical, false otherwise.
	 * @param _other
	 * @return
	 */
	public boolean equalsButVersion(LibraryId obj) {
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
		return true;
	}
	
	@Override
	public int compareTo(Object _other) {
		if(_other==null || !(_other instanceof LibraryId))
			throw new IllegalArgumentException();
		int v = this.getMvnGroup().compareTo(((LibraryId)_other).getMvnGroup());
		if(v==0) v = this.getArtifact().compareTo(((LibraryId)_other).getArtifact());
		if(v==0) v = this.getVersion().compareTo(((LibraryId)_other).getVersion());
		return v;
	}
	
	public com.sap.psr.vulas.shared.json.model.LibraryId toSharedType() {
		return new com.sap.psr.vulas.shared.json.model.LibraryId(this.mvnGroup, this.artifact, this.version);
	}
}
