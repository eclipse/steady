package com.sap.psr.vulas.cia.model.nexus;

/**
 * <p>NexusDescribeInfo class.</p>
 *
 */
public class NexusDescribeInfo {
	Long uploaded;
	Long lastChanged;
	String sha1Hash;
	
	/**
	 * <p>Getter for the field <code>uploaded</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getUploaded() {
		return uploaded;
	}
	/**
	 * <p>Setter for the field <code>uploaded</code>.</p>
	 *
	 * @param uploaded a {@link java.lang.Long} object.
	 */
	public void setUploaded(Long uploaded) {
		this.uploaded = uploaded;
	}
	/**
	 * <p>Getter for the field <code>lastChanged</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getLastChanged() {
		return lastChanged;
	}
	/**
	 * <p>Setter for the field <code>lastChanged</code>.</p>
	 *
	 * @param lastChanged a {@link java.lang.Long} object.
	 */
	public void setLastChanged(Long lastChanged) {
		this.lastChanged = lastChanged;
	}
	/**
	 * <p>Getter for the field <code>sha1Hash</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSha1Hash() {
		return sha1Hash;
	}
	/**
	 * <p>Setter for the field <code>sha1Hash</code>.</p>
	 *
	 * @param sha1Hash a {@link java.lang.String} object.
	 */
	public void setSha1Hash(String sha1Hash) {
		this.sha1Hash = sha1Hash;
	}
	
	
}
