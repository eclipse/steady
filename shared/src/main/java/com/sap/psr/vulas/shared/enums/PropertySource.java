package com.sap.psr.vulas.shared.enums;

/**
 * <p>PropertySource class.</p>
 */
public enum PropertySource {
	
	JAVA_MANIFEST((byte)10), GOAL_CONFIG((byte)20), SYSTEM_INFO((byte)30), PIP((byte)40), EMBEDDED_POM((byte)50), USER((byte)60), NPM((byte)70);
	
	private byte value;
	
	private PropertySource(byte _value) { this.value = _value; }

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		if(this.value==10) return "Java manifest file";
		else if(this.value==20) return "Goal config";
		else if(this.value==30) return "System info";
		else if(this.value==40) return "PIP";
		else if(this.value==50) return "Embedded POM";
		else if(this.value==60) return "User";
		else if(this.value==70) return "NPM";
		else throw new IllegalArgumentException("[" + this.value + "] is not a valid programming language");
	}
}
