package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;

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
	
	public AffectedLibrary() { super(); }

	public AffectedLibrary(Bug bug, LibraryId libraryId, Boolean affected) {
		super();
		this.bugId = bug;
		this.libraryId = libraryId;
		this.affected = affected;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Bug getBugId() { return bugId; }
	public void setBugId(Bug bug) { this.bugId = bug; }
	
	public LibraryId getLibraryId() { return libraryId; } 
	public void setLibraryId(LibraryId libraryId) { this.libraryId = libraryId; }

	public Boolean getAffected() { return affected; }
	public void setAffected(Boolean affected) { this.affected = affected; }
	
	public AffectedVersionSource getSource() { return source; }
	public void setSource(AffectedVersionSource source) { this.source = source; }

	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }

	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	public String getExplanation() { return explanation; }
	public void setExplanation(String explanation) { this.explanation = explanation; }

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

        public Library getLib() {
            return lib;
        }

        public void setLib(Library lib) {
            this.lib = lib;
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
			builder.append("    LibraryId ").append(this.getLibraryId());
		}
		else {
			builder.append("[").append(this.getId()).append(":").append(this.getBugId()).append(":affected=").append(this.getAffected()).append("]");
		}
		return builder.toString();
	}
}
