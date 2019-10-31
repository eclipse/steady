package com.sap.psr.test.nested;

/** Test class for generating AST of constructs inside a Nested class */
public class Test {

  public class NestedTestClass {

    public NestedTestClass() {
      int a = 0;
      int b = 1;
    }

    public void test() {
      int a = 2;
      int b = 3;

      while (true) {
        int a = 2;
      }
    }
  }
}
