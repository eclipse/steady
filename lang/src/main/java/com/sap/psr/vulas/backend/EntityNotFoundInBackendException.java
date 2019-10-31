package com.sap.psr.vulas.backend;

/** EntityNotFoundInBackendException class. */
public class EntityNotFoundInBackendException extends Exception {
  /**
   * Constructor for EntityNotFoundInBackendException.
   *
   * @param _msg a {@link java.lang.String} object.
   */
  public EntityNotFoundInBackendException(String _msg) {
    super(_msg);
  }
}
