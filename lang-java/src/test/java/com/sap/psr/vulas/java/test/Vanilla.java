package com.sap.psr.vulas.java.test;

import com.sap.psr.vulas.monitor.ClassVisitorTest;

public class Vanilla {
  public Vanilla(String _string) {
    for (int i = 0; i < 2; i++) {}
  }

  public void foo(String _string) {
    for (int i = 0; i < 2; i++) {}
  }

  /**
   * Flagged as vulnerable in {@link ClassVisitorTest#testVisitMethodsInstr}.
   *
   * @param _string
   */
  public void vuln(String _string) {
    for (int i = 0; i < 2; i++) {}
  }
}
