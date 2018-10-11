package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.util.Constants;

/**
 * A space is an isolated environment within a given {@link Tenant}. Every application scan has to happen in the context of a space.
 * The scan of an {@link Application} in one space is entirely independent of the scan of the same {@link Application} in another space.
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Space implements Serializable {

	public String spaceToken;
	
	public String spaceName;
	
	public String spaceDescription;
		
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar lastModified;
	
	/**
	 * Configures the export of scan results.
	 */
	private ExportConfiguration exportConfiguration = ExportConfiguration.AGGREGATED;
	
	/**
	 * Determines whether the space is visible in a public directory.
	 */
	private boolean isPublic = true;
	
	/**
	 * Determines whether the space is the default one.
	 */
	private boolean isDefault = false;
	
	/**
	 * Determines the vulnerabilities that should be considered by the scans done in the scope of this space.
	 */
	private int bugFilter = -1;
	
	/**
	 * Email address of the space owner(s)
	 */
	private Set<String> ownerEmails = null;
	
	public Space() {}
	
	public Space(String _t, String _n, String _d) {
		this.setSpaceToken(_t);
		this.setSpaceName(_n);
		this.setSpaceDescription(_d);
	}
	
	public String getSpaceToken() { return spaceToken; }
	public void setSpaceToken(String spaceToken) { this.spaceToken = spaceToken; }
	
	@JsonIgnore
	public boolean isValidSpaceToken() { return this.spaceToken!=null && !this.spaceToken.equals(""); }
	
	public String getSpaceName() { return spaceName; }
	public void setSpaceName(String spaceName) { this.spaceName = spaceName; }

	public String getSpaceDescription() { return spaceDescription; }
	public void setSpaceDescription(String spaceDescription) { this.spaceDescription = spaceDescription; }
	
	public boolean hasNameAndDescription() {
		return this.spaceName!=null && this.spaceDescription!=null && !this.spaceName.equals("") && !this.spaceDescription.equals("");
	}

	public ExportConfiguration getExportConfiguration() { return exportConfiguration; }
	public void setExportConfiguration(ExportConfiguration exportConfiguration) { this.exportConfiguration = exportConfiguration; }

	public boolean isPublic() { return isPublic; }
	public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

	public int getBugFilter() { return bugFilter; }
	public void setBugFilter(int bugFilter) { this.bugFilter = bugFilter; }

	public Set<String> getOwnerEmails() { return ownerEmails; }
	public void setOwnerEmails(Set<String> ownerEmails) { this.ownerEmails = ownerEmails; }
	
	public String toString() {
		return this.spaceToken;
	}
	
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((spaceToken == null) ? 0 : spaceToken.hashCode());
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
		Space other = (Space) obj;
		if (spaceToken == null) {
			if (other.spaceToken != null)
				return false;
		} else if (!spaceToken.equals(other.spaceToken))
			return false;
		return true;
	}
}
