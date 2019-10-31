package com.sap.psr.vulas;

/** FileAnalysisException class. */
public class FileAnalysisException extends Exception {
  /**
   * Constructor for FileAnalysisException.
   *
   * @param _msg a {@link java.lang.String} object.
   */
  public FileAnalysisException(String _msg) {
    super(_msg);
  }
  /**
   * Constructor for FileAnalysisException.
   *
   * @param _msg a {@link java.lang.String} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public FileAnalysisException(String _msg, Throwable _cause) {
    super(_msg, _cause);
  }
}
