package com.sap.psr.vulas.java.test;

import java.io.Serializable;

public class NestedDeclarationMess2 {

  // Member interface
  interface DoSomethingElse {

    // Member class
    class DoThis {

      void doThis() {

        // Anonymous class (as method variable)
        final Serializable anon =
            new Serializable() {
              void doThat() {}
            };
      }
    }

    public void doSomethingElse();
  }

  // Member class (anon)
  final DoSomethingElse doSomethingElse =
      new DoSomethingElse() {
        public void doSomethingElse() {}
      };

  // Member class
  class DoThis {

    void doThis() {

      // Anonymous class (as method variable)
      final Serializable anon =
          new Serializable() {
            void doThat() {}
          };
    }
  }

  public void doSomething() {

    // Anonymous class (as method variable)
    final Serializable anon =
        new Serializable() {
          void doSomething() {}
        };

    // Named class
    class DoThat {

      void doThat() {

        // Anonymous class (as method variable)
        final Serializable anon =
            new Serializable() {
              void doThis() {}
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
