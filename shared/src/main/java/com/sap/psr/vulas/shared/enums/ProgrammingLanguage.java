package com.sap.psr.vulas.shared.enums;

/**
 * <p>ProgrammingLanguage class.</p>
 */
public enum ProgrammingLanguage {
	
	JAVA((byte)10), PY((byte)20), JS((byte)30);
	
	private byte value;
	
	private ProgrammingLanguage(byte _value) { this.value = _value; }

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		if(this.value==10) return "JAVA";
		else if(this.value==20) return "PY";
		else if(this.value==30) return "JS";
		else throw new IllegalArgumentException("[" + this.value + "] is not a valid programming language");
	}
}
