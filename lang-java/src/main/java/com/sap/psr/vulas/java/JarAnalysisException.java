package com.sap.psr.vulas.java;

/** JarAnalysisException class. */
public class JarAnalysisException extends Exception {
  private static final long serialVersionUID = 1L;
  /**
   * Constructor for JarAnalysisException.
   *
   * @param _msg a {@link java.lang.String} object.
   */
  public JarAnalysisException(String _msg) {
    super(_msg);
  }
  /**
   * Constructor for JarAnalysisException.
   *
   * @param _msg a {@link java.lang.String} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public JarAnalysisException(String _msg, Throwable _cause) {
    super(_msg, _cause);
  }
}
