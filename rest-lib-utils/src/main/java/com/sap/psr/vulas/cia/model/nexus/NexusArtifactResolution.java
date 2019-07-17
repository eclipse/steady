package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>NexusArtifactResolution class.</p>
 *
 */
@XmlRootElement(name="artifact-resolution")//, namespace="com.concretepage" )
public class NexusArtifactResolution {

	NexusResolvedArtifact data;

	/**
	 * <p>Getter for the field <code>data</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.cia.model.nexus.NexusResolvedArtifact} object.
	 */
	public NexusResolvedArtifact getData() {
		return data;
	}

	/**
	 * <p>Setter for the field <code>data</code>.</p>
	 *
	 * @param data a {@link com.sap.psr.vulas.cia.model.nexus.NexusResolvedArtifact} object.
	 */
	public void setData(NexusResolvedArtifact data) {
		this.data = data;
	}
}
