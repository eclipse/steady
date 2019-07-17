package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>NexusArtifactInfoResourceResponse class.</p>
 *
 */
@XmlRootElement(name="org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse")
public class NexusArtifactInfoResourceResponse {
	
	NexusDescribeInfo data;

	/**
	 * <p>Getter for the field <code>data</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.cia.model.nexus.NexusDescribeInfo} object.
	 */
	public NexusDescribeInfo getData() {
		return data;
	}

	/**
	 * <p>Setter for the field <code>data</code>.</p>
	 *
	 * @param data a {@link com.sap.psr.vulas.cia.model.nexus.NexusDescribeInfo} object.
	 */
	public void setData(NexusDescribeInfo data) {
		this.data = data;
	}
	
	
}
