package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="searchNGResponse")
public class NexusSearchNGResponse {

	NexusNGData data;
	
	public NexusNGData getData() {
		return data;
	}

	public void setData(NexusNGData data) {
		this.data = data;
	}

}
