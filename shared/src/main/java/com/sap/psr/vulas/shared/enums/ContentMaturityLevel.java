package com.sap.psr.vulas.shared.enums;

/**
 * Defines the maturity of database content. Can be used, for instance, to configure a space in
 * regards to bugs that shall be considered for application analysis.
 */
public enum ContentMaturityLevel {
  DRAFT((byte) 1),
  READY((byte) 2);
  private byte value;

  private ContentMaturityLevel(byte _value) {
    this.value = _value;
  }
  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    if (this.value == 1) return "DRAFT";
    else if (this.value == 2) return "READY";
    else throw new IllegalArgumentException("[" + this.value + "] is not a valid maturity level");
  }
}
