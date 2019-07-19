package com.sap.psr.vulas.shared.enums;

/**
 * The client running goal(s).
 */
public enum GoalClient {


	CLI((byte)10), MAVEN_PLUGIN((byte)20), REST_SERVICE((byte)30), AGENT((byte)40), SETUPTOOLS((byte)50), GRADLE_PLUGIN((byte)60);

	private byte value;

	private GoalClient(byte _value) { this.value = _value; }

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		if(this.value==10) return "CLI";
		else if(this.value==20) return "MAVEN";
		else if(this.value==30) return "REST";
		else if(this.value==40) return "AGENT";
		else if(this.value==50) return "SETUPTOOLS";
		else if(this.value==60) return "GRADLE";
		else throw new IllegalArgumentException("[" + this.value + "] is not a valid goal client");
	}

}
