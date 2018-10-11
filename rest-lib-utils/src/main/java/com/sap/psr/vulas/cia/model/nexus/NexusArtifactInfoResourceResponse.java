package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse")
public class NexusArtifactInfoResourceResponse {
	
	NexusDescribeInfo data;

	public NexusDescribeInfo getData() {
		return data;
	}

	public void setData(NexusDescribeInfo data) {
		this.data = data;
	}
	
	
}
