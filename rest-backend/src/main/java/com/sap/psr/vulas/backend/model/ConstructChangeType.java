package com.sap.psr.vulas.backend.model;

/**
 * Type of change of a given {@link ConstructId} as part of a bug fix. ADD means that a certain
 * construct, e.g., Java method, has been added, MOD that it's body has been modified, and DEL that
 * it has been deleted.
 *
 * <p>An identical copy exists in vulas-core, package com.sap.psr.vulas.
 */
public enum ConstructChangeType {
  ADD((byte) 10),
  MOD((byte) 20),
  DEL((byte) 30);
  private byte value;

  private ConstructChangeType(byte _value) {
    this.value = _value;
  }
  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    if (this.value == 10) return "ADD";
    else if (this.value == 20) return "MOD";
    else if (this.value == 30) return "DEL";
    else
      throw new IllegalArgumentException(
          "[" + this.value + "] is not a valid contruct change type");
  }
}
