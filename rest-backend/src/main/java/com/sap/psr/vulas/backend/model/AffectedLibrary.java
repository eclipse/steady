package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "createdAt", "createdBy" })
@Entity
@Table( name="BugAffectedLibrary", 
uniqueConstraints={@UniqueConstraint( columnNames = { "bugId", "libraryId", "source" } ),@UniqueConstraint( columnNames = { "bugId", "lib", "source" })} ) 
public class AffectedLibrary implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;
	
	@ManyToOne(optional = false, cascade = {})
	@JoinColumn(name = "bugId", referencedColumnName = "bugId") // Required for the unique constraint
	@JsonView(Views.LibraryIdDetails.class)
	//@JsonBackReference
	private Bug bugId;

	
	@ManyToOne(optional = true, cascade = {})
	@JoinColumn(name = "libraryId", referencedColumnName = "id") // Required for the unique constraint
	@JsonView(Views.BugAffLibs.class)
//	@JsonManagedReference
	private LibraryId libraryId;
	
	@ManyToOne(optional = true, cascade = {})
	@JoinColumn(name = "lib", referencedColumnName = "digest") // Required for the unique constraint
	private Library lib;
	
	@OneToMany( cascade = { CascadeType.ALL }, mappedBy = "affectedLib", fetch = FetchType.LAZY,  orphanRemoval=true)
	@JsonManagedReference
	@JsonView(Views.BugAffLibsDetails.class)
	private Collection<AffectedConstructChange> affectedcc;
	
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AffectedVersionSource source;
	
	@Column
	private Boolean affected;
	
	@Column
	private Boolean sourcesAvailable;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar modifiedAt;
	
	@Column
	private String createdBy;
	
	@Column(nullable=true,columnDefinition = "text")
	//@Lob
	private String explanation;
	
	@Column
	private String overallConfidence;
	
	@Column
	private String pathConfidence;
	


	@Column
	private String lastVulnerable;
	
	@Column
	private String firstFixed;
	
	@Column
	private String fromIntersection;
	
	@Column
	private String toIntersection;
	
	@Column
	private String ADFixed;
	
	@Column
	private String ADPathFixed;
	
	
	public AffectedLibrary() { super(); }

	public AffectedLibrary(Bug bug, LibraryId libraryId, Boolean affected, Library lib, Collection<AffectedConstructChange> aff_cc, Boolean sourceAvailable) {
		super();
		this.bugId = bug;
		this.libraryId = libraryId;
		this.affected = affected;
		this.lib = lib;
		this.affectedcc = aff_cc;
		this.sourcesAvailable = sourceAvailable;
	}

	public Boolean getSourcesAvailable() {
		return sourcesAvailable;
	}

	public void setSourcesAvailable(Boolean sourceAvailable) {
		this.sourcesAvailable = sourceAvailable;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Bug getBugId() { return bugId; }
	public void setBugId(Bug bug) { this.bugId = bug; }
	
	public LibraryId getLibraryId() { return libraryId; } 
	public void setLibraryId(LibraryId libraryId) { this.libraryId = libraryId; }

	public Library getLib() { return this.lib; } 
	public void setLib(Library library) { this.lib = library; }
	
	public Collection<AffectedConstructChange> getAffectedcc() { return affectedcc; }
	public void setAffectedcc(Collection<AffectedConstructChange> affectedcc) { this.affectedcc= affectedcc; }
		
	public Boolean getAffected() { return affected; }
	public Boolean isAffected() { return this.getAffected()==true; }
	public void setAffected(Boolean affected) { this.affected = affected; }
	
	public AffectedVersionSource getSource() { return source; }
	public void setSource(AffectedVersionSource source) { this.source = source; }

	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }
	
	public java.util.Calendar getModifiedAt() { return modifiedAt; }
	public void setModifiedAt(java.util.Calendar modifiedAt) { this.modifiedAt = modifiedAt; }

	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	public String getExplanation() { return explanation; }
	public void setExplanation(String explanation) { this.explanation = explanation; }

	public String getLastVulnerable() { return this.lastVulnerable; }
	public void setLastVulnerable(String lv) { this.lastVulnerable = lv; }
	
	public String getFirstFixed() { return this.firstFixed; }
	public void setFirstFixed(String ff) { this.firstFixed = ff; }
	
	public String getFromIntersection() { return this.fromIntersection; }
	public void setFromIntersection(String fi) { this.fromIntersection = fi; }
	
	public String getToIntersection() { return this.toIntersection; }
	public void setToIntersection(String ti) { this.toIntersection = ti; }
	
	
	public String getOverallConfidence() {
		return overallConfidence;
	}

	public void setOverallConfidence(String overallConfidence) {
		this.overallConfidence = overallConfidence;
	}

	public String getPathConfidence() {
		return pathConfidence;
	}

	public void setPathConfidence(String pathConfidence) {
		this.pathConfidence = pathConfidence;
	}
	
	
	
	public String getADFixed() {
		return ADFixed;
	}

	public void setADFixed(String aDFixed) {
		ADFixed = aDFixed;
	}

	public String getADPathFixed() {
		return ADPathFixed;
	}

	public void setADPathFixed(String aDPathFixed) {
		ADPathFixed = aDPathFixed;
	}

	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null) {
			this.setCreatedAt(Calendar.getInstance());
		}
		if(this.getModifiedAt()==null) {
			this.setModifiedAt(Calendar.getInstance());
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
		result = prime * result + ((libraryId == null) ? 0 : libraryId.hashCode());
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
		return true;
	}
	
	@Override
	public final String toString() {
		return this.toString(false);
	}
	
	public final String toString(boolean _deep) {
		final StringBuilder builder = new StringBuilder();
		if(_deep) {
			builder.append("Library affected: [").append(this.getAffected()).append("]").append(System.getProperty("line.separator"));
			builder.append("    Bug ").append(this.getBugId());
			if(this.getLibraryId()!=null)
				builder.append("    LibraryId ").append(this.getLibraryId());
			if(this.getLib()!=null)
				builder.append("    Library ").append(this.getLib());
		}
		else {
			builder.append("[").append("bugid=").append(this.getBugId().getBugId()).append(", affected=").append(this.getAffected()).append(", source=").append(this.getSource().toString());
			if(this.getLibraryId()!=null) {
				builder.append(", libid=").append(this.getLibraryId().getMvnGroup()).append(":").append(this.getLibraryId().getArtifact()).append(":").append(this.getLibraryId().getVersion());
			}
			if(this.getLib()!=null)
				builder.append(", lib digest=").append(this.getLib().getDigest());
			builder.append("]");
		}
		return builder.toString();
	}
}
