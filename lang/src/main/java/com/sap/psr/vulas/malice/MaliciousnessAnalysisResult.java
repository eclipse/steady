package com.sap.psr.vulas.malice;

public class MaliciousnessAnalysisResult {

	private double result = 0;
	
	private String reason = null;
	
	private String analyzer = null;

	public double getResult() { return result; 	}

	public void setResult(double result) {
		this.result = result;
	}
	
	public boolean isBenign() { return result==0d; }
	
	public boolean isMalicious() { return result>0d; }

	public String getReason() {
		return reason;
	}

	public void setReason(String _reason) {
		this.reason = _reason;
	}
	
	public void appendReason(String _reason, String _separator) {
		this.reason = (this.reason==null ? "" : this.reason + _separator) + _reason;
	}

	public String getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}	
}
