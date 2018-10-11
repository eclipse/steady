package com.sap.psr.vulas.cia.model.nexus;



public class NexusResolvedArtifact {

	//@XmlAttribute(name="groupId")
	String groupId;
	
//	@XmlAttribute(name="artifactId")
	String artifactId;
	
	//@XmlAttribute(name="version")
	String version;
	
	//@XmlAttribute(name="extension")
	String extension;
	
	//@XmlAttribute(name="snapshot")
	String snapshot;
	
	//@XmlAttribute(name="snapshotBuildNumber")
	String snapshotBuildNumber;
	
	//@XmlAttribute(name="snapshotTimeStamp")
	String snapshotTimeStamp;
	
	//@XmlAttribute(name="sha1")
	String sha1;
	
	//@XmlAttribute(name="repositoryPath")
	String repositoryPath;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}

	public String getSnapshotBuildNumber() {
		return snapshotBuildNumber;
	}

	public void setSnapshotBuildNumber(String snapshotBuildNumber) {
		this.snapshotBuildNumber = snapshotBuildNumber;
	}

	public String getSnapshotTimeStamp() {
		return snapshotTimeStamp;
	}

	public void setSnapshotTimeStamp(String snapshotTimeStamp) {
		this.snapshotTimeStamp = snapshotTimeStamp;
	}

	public String getSha1() {
		return sha1;
	}

	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}
}
