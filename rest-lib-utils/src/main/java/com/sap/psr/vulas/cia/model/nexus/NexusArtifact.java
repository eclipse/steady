package com.sap.psr.vulas.cia.model.nexus;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="artifact")
public class NexusArtifact {
	
	String groupId;
	
	String artifactId;
	
	String version;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifact) {
		this.artifactId = artifact;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	// It does not look like this field is correctly populated
	//String latestRelease;

	
}
