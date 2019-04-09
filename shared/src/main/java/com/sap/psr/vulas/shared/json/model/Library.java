package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.json.model.view.Views;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value = { "constructCounter", "constructTypeCounters" }, allowGetters=true)
public class Library implements Serializable {

	private static final long serialVersionUID = 1L;

	private String digest;
	
	private DigestAlgorithm digestAlgorithm;

	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;

	@JsonView(Views.LibDetails.class)
	private Collection<Property> properties;

	@JsonView(Views.LibDetails.class)
	private Collection<ConstructId> constructs;

	//http://stackoverflow.com/questions/23260464/how-to-serialize-using-jsonview-with-nested-objects
	/**
	 * Human-readable library identifier, e.g., a Maven artifact ID consisting of group, artifact and version.
	 */
	private LibraryId libraryId;

	/**
	 * True if the library provider or a trusted software repository confirms the mapping of SHA1 to human-readable ID, false otherwise.
	 */
	private Boolean wellknownDigest;	
	
	/**
	 * The URL used to verify the digest. Will be empty if none of the available
	 * package repositories was able to confirm the digest.
	 */
	@JsonIgnoreProperties(value = { "digestVerificationUrl" }, allowGetters=true)
	private String digestVerificationUrl;	

	public Library() { super(); }

	public Library(String digest) {
		super();
		this.digest = digest;
	}

	public String getDigest() { return digest; }
	public void setDigest(String digest) { this.digest = digest; }
	
	public DigestAlgorithm getDigestAlgorithm() { return digestAlgorithm; }
	public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) { this.digestAlgorithm = digestAlgorithm; }
	
	/**
	 * Returns true if the library has a digest and a digest algorithm, false otherwise.
	 * @return
	 */
	public boolean hasValidDigest() {
		return this.getDigest()!=null && this.getDigestAlgorithm()!=null;
	}

	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }

	public Collection<Property> getProperties() { return properties; }
	public void setProperties(Collection<Property> properties) { this.properties = properties; }

	public Collection<ConstructId> getConstructs() { return constructs; }
	public void setConstructs(Collection<ConstructId> constructs) { this.constructs = constructs; }

	public LibraryId getLibraryId() { return libraryId; }
	public void setLibraryId(LibraryId _library_id) { this.libraryId = _library_id; }
	
	public boolean isWellknownDigest() { return wellknownDigest!=null && wellknownDigest.equals(true); }
	public Boolean getWellknownDigest() { return wellknownDigest; }
	public void setWellknownSha1(Boolean wellknownDigest) { this.wellknownDigest = wellknownDigest; }
	
	public String getDigestVerificationUrl() { return digestVerificationUrl; }
	public void setDigestVerificationUrl(String digestVerificationUrl) { this.digestVerificationUrl = digestVerificationUrl; }
	
	@JsonProperty(value = "constructCounter")
	@JsonView(Views.LibDetails.class)
	public int countConstructs() { return ( this.getConstructs()==null ? 0 : this.getConstructs().size()); }

	@JsonProperty(value = "constructTypeCounters")
	@JsonView(Views.LibDetails.class)
	public ConstructIdFilter countConstructTypes() { return new ConstructIdFilter(this.getConstructs()); }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((digest == null) ? 0 : digest.hashCode());
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
		Library other = (Library) obj;
		if (digest == null) {
			if (other.digest != null)
				return false;
		} else if (!digest.equals(other.digest))
			return false;
		return true;
	}

	@Override
	public final String toString() { return this.toString(false); }
	
	/**
	 * Returns a short or long string representation of the library.
	 * @param _deep
	 * @return
	 */
	public final String toString(boolean _deep) {
		final StringBuilder builder = new StringBuilder();
		if(_deep) {
			builder.append("Library ").append(this.toString(false)).append(System.getProperty("line.separator"));
			for(ConstructId cid: this.getConstructs()) {
				builder.append("    ConstructId     ").append(cid).append(System.getProperty("line.separator"));
			}
		}
		else {
			builder.append("[").append(this.getDigest()).append("]");
		}
		return builder.toString();
	}
}
