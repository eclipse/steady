package com.sap.psr.vulas.cia.model;

/**
 * Thrown by {@link GoalExecution#execute()}, indicates that the execution of the respective goal failed.
 */
public class ScanException extends Exception {
	
	public ScanException(Throwable _cause) {
		super(_cause);
	}
	
	public ScanException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
}
