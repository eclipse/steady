package com.sap.psr.vulas.cia.model.nexus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sap.psr.vulas.shared.json.model.LibraryId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusLibId {

	private String groupId;
	private String artifactId;
	private String version;
	
	private String repository;
	
	public NexusLibId(){}
	
	public NexusLibId(String g, String a, String v){
		this.groupId=g;
		this.artifactId=a;
		this.version=v;
		
	}
	
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
	

	
	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	
}
