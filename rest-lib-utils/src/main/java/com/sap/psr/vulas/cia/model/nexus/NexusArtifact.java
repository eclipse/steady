package com.sap.psr.vulas.cia.model.nexus;


import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>NexusArtifact class.</p>
 *
 */
@XmlRootElement(name="artifact")
public class NexusArtifact {
	
	String groupId;
	
	String artifactId;
	
	String version;

	/**
	 * <p>Getter for the field <code>groupId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * <p>Setter for the field <code>groupId</code>.</p>
	 *
	 * @param groupId a {@link java.lang.String} object.
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * <p>Getter for the field <code>artifactId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * <p>Setter for the field <code>artifactId</code>.</p>
	 *
	 * @param artifact a {@link java.lang.String} object.
	 */
	public void setArtifactId(String artifact) {
		this.artifactId = artifact;
	}

	/**
	 * <p>Getter for the field <code>version</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * <p>Setter for the field <code>version</code>.</p>
	 *
	 * @param version a {@link java.lang.String} object.
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	// It does not look like this field is correctly populated
	//String latestRelease;

	
}
