package com.sap.psr.vulas.cia.model;

/**
 * Thrown by {@link GoalExecution#execute()}, indicates that the execution of the respective goal failed.
 */
public class ScanException extends Exception {
	
	/**
	 * <p>Constructor for ScanException.</p>
	 *
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public ScanException(Throwable _cause) {
		super(_cause);
	}
	
	/**
	 * <p>Constructor for ScanException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public ScanException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
}
