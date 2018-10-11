package com.sap.psr.vulas.shared.enums;

public enum DependencyOrigin {
	DEPMGR((byte)10), FS((byte)20), NESTED((byte)30);

	private byte value;

	private DependencyOrigin(byte _value) { this.value = _value; }

	public String toString() {
		if(this.value==10) return "DEPMGR";
		else if(this.value==20) return "FS";
		else if(this.value==30) return "NESTED";
		else throw new IllegalArgumentException("[" + this.value + "] is not a valid dependency origin");
	}
}
