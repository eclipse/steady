package com.sap.psr.vulas.sign;

/**
 * The so called construct signature is a representation of the body of a construct that allows
 * comparing different releases. Possible implementations can be, for instance, abstract syntax
 * trees (AST).
 */
public interface Signature {

  /**
   * Returns true if the signatures are equal, false otherwise.
   *
   * @param _o the signature to compare with
   * @return a boolean.
   */
  public boolean equals(Object _o);

  /**
   * Returns a short (perhaps compressed) JSON representation of the signature, to be uploaded to
   * the central Vulas engine. The reason to have a short representation is that we need to upload
   * and store signatures for all applications and dependencies analyzed. Moreover, it would be very
   * good to create a representation that can be compared also on server-side, e.g., by means of SQL
   * statements or stored procedures.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toJson();

  /**
   * toString.
   *
   * @return String representation of signature
   */
  public String toString();
}
