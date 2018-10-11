package com.sap.psr.vulas.cia.model.nexus;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="data")
public class NexusNGData {

	@XmlElement(name = "artifact")
	private List<NexusArtifact> artifact = new ArrayList<NexusArtifact>();

	public List<NexusArtifact> getArtifactList() {
		return artifact;
	}

	public void setArtifactList(List<NexusArtifact> _l) {
		this.artifact = _l;
	}
	
	
}
