package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>NexusSearchNGResponse class.</p>
 *
 */
@XmlRootElement(name="searchNGResponse")
public class NexusSearchNGResponse {

	NexusNGData data;
	
	/**
	 * <p>Getter for the field <code>data</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.cia.model.nexus.NexusNGData} object.
	 */
	public NexusNGData getData() {
		return data;
	}

	/**
	 * <p>Setter for the field <code>data</code>.</p>
	 *
	 * @param data a {@link com.sap.psr.vulas.cia.model.nexus.NexusNGData} object.
	 */
	public void setData(NexusNGData data) {
		this.data = data;
	}

}
