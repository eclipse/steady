package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="artifact-resolution")//, namespace="com.concretepage" )
public class NexusArtifactResolution {

	NexusResolvedArtifact data;

	public NexusResolvedArtifact getData() {
		return data;
	}

	public void setData(NexusResolvedArtifact data) {
		this.data = data;
	}
}
