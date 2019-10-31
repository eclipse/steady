package com.sap.psr.vulas.malice;

/** MaliciousnessAnalysisResult class. */
public class MaliciousnessAnalysisResult {

  private double result = 0;

  private String reason = null;

  private String analyzer = null;

  /**
   * Getter for the field <code>result</code>.
   *
   * @return a double.
   */
  public double getResult() {
    return result;
  }

  /**
   * Setter for the field <code>result</code>.
   *
   * @param result a double.
   */
  public void setResult(double result) {
    this.result = result;
  }

  /**
   * isBenign.
   *
   * @return a boolean.
   */
  public boolean isBenign() {
    return result == 0d;
  }

  /**
   * isMalicious.
   *
   * @return a boolean.
   */
  public boolean isMalicious() {
    return result > 0d;
  }

  /**
   * Getter for the field <code>reason</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getReason() {
    return reason;
  }

  /**
   * Setter for the field <code>reason</code>.
   *
   * @param _reason a {@link java.lang.String} object.
   */
  public void setReason(String _reason) {
    this.reason = _reason;
  }

  /**
   * appendReason.
   *
   * @param _reason a {@link java.lang.String} object.
   * @param _separator a {@link java.lang.String} object.
   */
  public void appendReason(String _reason, String _separator) {
    this.reason = (this.reason == null ? "" : this.reason + _separator) + _reason;
  }

  /**
   * Getter for the field <code>analyzer</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAnalyzer() {
    return analyzer;
  }

  /**
   * Setter for the field <code>analyzer</code>.
   *
   * @param analyzer a {@link java.lang.String} object.
   */
  public void setAnalyzer(String analyzer) {
    this.analyzer = analyzer;
  }
}
