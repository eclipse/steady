package com.sap.psr.vulas.java.test;

import java.io.Serializable;

public class TestAnon {

  // Member interface
  interface Interface_A {

    // Member class
    class Class_A {

      void Method_A() {

        // Anonymous class (as method variable)
        final Serializable anon =
            new Serializable() {
              void Method_B() {}
            };
      }
    }

    public void Method_C();
  }

  // Member class (anon)
  final Interface_A doSomethingElse =
      new Interface_A() {
        public void Method_C() {}
      };

  // Member class
  class Class_B {

    void Method_E() {

      // Anonymous class (as method variable)
      final Serializable anon =
          new Serializable() {
            void Method_F() {}
          };
    }
  }

  public void Method_G() {

    // Anonymous class (as method variable)
    final Serializable anon =
        new Serializable() {
          void Method_H() {}
        };

    // Named class
    class Class_C {

      void Method_L() {

        // Anonymous class (as method variable)
        final Serializable anon =
            new Serializable() {
              void Method_M() {}
            };
      }
    }
  }

  public void Method_H() {
    // Named class
    class Class_C {

      void Method_L() {

        // Anonymous class (as method variable)
        final Serializable anon =
            new Serializable() {
              void Method_M() {}
            };
      }
    }
  }

  public enum Foo {
    A,
    B;

    void bar() {
      // Named class
      class DoThis {

        void doThis() {

          // Anonymous class (as method variable)
          final Serializable anon =
              new Serializable() {
                void doThat() {}
              };
        }
      }
    }
  };
}
