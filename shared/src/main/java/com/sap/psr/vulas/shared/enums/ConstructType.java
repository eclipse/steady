package com.sap.psr.vulas.shared.enums;

/** ConstructType class. */
public enum ConstructType {
  PACK((byte) 10),
  CLAS((byte) 20),
  ENUM((byte) 30),
  INTF((byte) 40),
  METH((byte) 50),
  CONS((byte) 60),
  INIT((byte) 70),
  FUNC((byte) 80),
  MODU((byte) 90);

  private byte value;

  private ConstructType(byte _value) {
    this.value = _value;
  }

  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    if (this.value == 10) return "PACK";
    else if (this.value == 20) return "CLAS";
    else if (this.value == 30) return "ENUM";
    else if (this.value == 40) return "INTF";
    else if (this.value == 50) return "METH";
    else if (this.value == 60) return "CONS";
    else if (this.value == 70) return "INIT";
    else if (this.value == 80) return "FUNC";
    else if (this.value == 90) return "MODU";
    else
      throw new IllegalArgumentException(
          "[" + this.value + "] is not a valid contruct construct type");
  }

  /**
   * Returns an array with all existing construct types.
   *
   * @return an array of {@link com.sap.psr.vulas.shared.enums.ConstructType} objects.
   */
  public static ConstructType[] getAllAsArray() {
    return new ConstructType[] {PACK, CLAS, ENUM, INTF, METH, CONS, INIT, FUNC, MODU};
  }
}
