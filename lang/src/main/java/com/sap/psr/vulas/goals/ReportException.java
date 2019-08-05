package com.sap.psr.vulas.goals;

/**
 * <p>ReportException class.</p>
 *
 */
public class ReportException extends GoalExecutionException {
	private String longMessage = null;
	/**
	 * <p>Constructor for ReportException.</p>
	 *
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public ReportException(Throwable _cause) {
		super(_cause);
	}
	/**
	 * <p>Constructor for ReportException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public ReportException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
	/**
	 * <p>Getter for the field <code>longMessage</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLongMessage() {
		return longMessage;
	}
	/**
	 * <p>Setter for the field <code>longMessage</code>.</p>
	 *
	 * @param longMessage a {@link java.lang.String} object.
	 */
	public void setLongMessage(String longMessage) {
		this.longMessage = longMessage;
	}
}
