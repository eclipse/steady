/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
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
