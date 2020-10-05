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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.java.test;

import java.io.Serializable;

public class TestAgainAnon {

  // Member class
  class Class_A {

    void method_A() {

      // Anonymous class (as method variable)
      final Serializable anon =
          new Serializable() {
            void method_B() {}
          };
    }

    void method_C() {

      // Anonymous class (as method variable)
      final Serializable anon =
          new Serializable() {
            void method_D() {}
          };
    }
  }

  //////////////////////////////////

  public void method_E() {

    int i = 0;

    // // Anonymous class (as method variable)
    final Serializable anon =
        new Serializable() {
          void method_F() {
            final Serializable anon1 =
                new Serializable() {
                  void method_G() {
                    return;
                  }
                };
            final Serializable anon2 =
                new Serializable() {
                  void method_H() {
                    return;
                  }
                };
          }
        };
    //
    // Named class
    class Class_B {

      void method_L() {

        // Anonymous class (as method variable)
        final Serializable anon =
            new Serializable() {
              void method_M() {
                // Anon inside anon
                final Serializable anon2 =
                    new Serializable() {
                      void method_N() {}
                    };
              }
            };
      }
    }
  }
}
