package com.sap.psr.vulas.cia.model.nexus;

public class NexusDescribeInfo {
	Long uploaded;
	Long lastChanged;
	String sha1Hash;
	
	public Long getUploaded() {
		return uploaded;
	}
	public void setUploaded(Long uploaded) {
		this.uploaded = uploaded;
	}
	public Long getLastChanged() {
		return lastChanged;
	}
	public void setLastChanged(Long lastChanged) {
		this.lastChanged = lastChanged;
	}
	public String getSha1Hash() {
		return sha1Hash;
	}
	public void setSha1Hash(String sha1Hash) {
		this.sha1Hash = sha1Hash;
	}
	
	
}
