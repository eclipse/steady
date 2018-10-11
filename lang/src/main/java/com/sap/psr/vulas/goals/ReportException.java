package com.sap.psr.vulas.goals;

public class ReportException extends GoalExecutionException {
	private String longMessage = null;
	public ReportException(Throwable _cause) {
		super(_cause);
	}
	public ReportException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
	public String getLongMessage() {
		return longMessage;
	}
	public void setLongMessage(String longMessage) {
		this.longMessage = longMessage;
	}
}
