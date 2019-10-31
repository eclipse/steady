package com.sap.psr.vulas.goals;

/** ReportException class. */
public class ReportException extends GoalExecutionException {
  private String longMessage = null;
  /**
   * Constructor for ReportException.
   *
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public ReportException(Throwable _cause) {
    super(_cause);
  }
  /**
   * Constructor for ReportException.
   *
   * @param _msg a {@link java.lang.String} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public ReportException(String _msg, Throwable _cause) {
    super(_msg, _cause);
  }
  /**
   * Getter for the field <code>longMessage</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getLongMessage() {
    return longMessage;
  }
  /**
   * Setter for the field <code>longMessage</code>.
   *
   * @param longMessage a {@link java.lang.String} object.
   */
  public void setLongMessage(String longMessage) {
    this.longMessage = longMessage;
  }
}
