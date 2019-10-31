package com.sap.psr.vulas.shared.json;

/** JsonSyntaxException class. */
public class JsonSyntaxException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor for JsonSyntaxException.
   *
   * @param _message a {@link java.lang.String} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public JsonSyntaxException(String _message, Throwable _cause) {
    super(_message, _cause);
  }
}
