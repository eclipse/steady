package com.sap.psr.vulas.shared.connectivity;

/**
 * RESTful service that clients can connect to.
 *
 * Example services comprise, for instance, the backend service where analysis results are stored,
 * or the cia service that provides information on Java archives, classes and single methods.
 */
public enum Service {
	BACKEND((byte)10), CIA((byte)20), CVE((byte)30), JIRA((byte)40);
	private byte value;
	private Service(byte _value) { this.value = _value; }
	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		     if(this.value==10) return "backend";
		else if(this.value==20) return "cia";
		else if(this.value==30) return "cve";
		else if(this.value==40) return "jira";
		else throw new IllegalArgumentException("[" + this.value + "] is not a valid service");
	}
}
