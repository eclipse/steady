package com.sap.psr.vulas.cg;

/** CallgraphConstructException class. */
public class CallgraphConstructException extends Exception {
  /**
   * Constructor for CallgraphConstructException.
   *
   * @param _msg a {@link java.lang.String} object.
   */
  public CallgraphConstructException(String _msg) {
    super(_msg);
  }
  /**
   * Constructor for CallgraphConstructException.
   *
   * @param _msg a {@link java.lang.String} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public CallgraphConstructException(String _msg, Throwable _cause) {
    super(_msg, _cause);
  }
}
