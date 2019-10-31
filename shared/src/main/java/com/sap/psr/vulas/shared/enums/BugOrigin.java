package com.sap.psr.vulas.shared.enums;

import com.sap.psr.vulas.shared.json.model.Bug;

/**
 * Defines the origin of a given {@link Bug} in the sense of the type of information used for
 * creating it.
 *
 * <p>{@link BugOrigin#PUBLIC} means that the bug is publicly disclosed and a description is
 * provided in a databases and/or issue tracker. {@link BugOrigin#MCHLRN} means that the bug has
 * been found by analyzing commits (using Machine Learning). {@link BugOrigin#SRCSCN} means that the
 * bug has been found by static source code scans.
 */
public enum BugOrigin {
  PUBLIC((byte) 1),
  MCHLRN((byte) 2),
  SRCSCN((byte) 4),
  PRIVAT((byte) 8);
  private byte value;

  private BugOrigin(byte _value) {
    this.value = _value;
  }
  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    if (this.value == 1) return "PUBLIC";
    else if (this.value == 2) return "MCHLRN";
    else if (this.value == 4) return "SRCSCN";
    else if (this.value == 8) return "PRIVAT";
    else throw new IllegalArgumentException("[" + this.value + "] is not a valid origin");
  }
}
