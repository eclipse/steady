package com.sap.psr.vulas.shared.enums;

public enum CoverageStatus {

	COVERED((byte)10), OPEN((byte)20), OUT_OF_SCOPE((byte)30), UNKNOWN((byte)40), ERROR((byte)50);
	
	private byte value;
	
	private CoverageStatus(byte _value) { this.value = _value; }
	
	public byte getStatusCode() { return this.value; }
	
	public String toString() {
		     if(this.value==10) return "Covered";
		else if(this.value==20) return "Open";
		else if(this.value==30) return "Out-of-scope";
		else if(this.value==40) return "Unknown";
		else if(this.value==50) return "Error";
		else throw new IllegalArgumentException("[" + this.value + "] is not a valid coverage status");
	}
	
	public String getText() { return this.toString(); }
	
	public String getDescription() {
		     if(this.value==10) return "Vulnerability part of Vulas database";
		else if(this.value==20) return "Vulnerability under investigation";
		else if(this.value==30) return "Vulnerability out of scope for Vulas";
		else if(this.value==40) return "Vulnerability not known to Vulas";
		else if(this.value==50) return "Error when establishing coverage";
		else throw new IllegalArgumentException("[" + this.value + "] is not a valid coverage status");
	}
}