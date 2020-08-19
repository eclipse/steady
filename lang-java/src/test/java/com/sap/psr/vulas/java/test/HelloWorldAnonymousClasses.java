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

public class HelloWorldAnonymousClasses {

  interface HelloWorld {
    public void greet();

    public void greetSomeone(String someone);
  }

  public void sayHello() {

    class EnglishGreeting implements HelloWorld {
      String name = "world";

      public void greet() {
        greetSomeone("world");
      }

      public void greetSomeone(String someone) {
        name = someone;
        System.out.println("Hello " + name);
      }
    }

    HelloWorld englishGreeting = new EnglishGreeting();

    HelloWorld frenchGreeting =
        new HelloWorld() {
          String name = "tout le monde";

          public void greet() {
            greetSomeone("tout le monde");
          }

          public void greetSomeone(String someone) {
            String nameChanged = someone;
            System.out.println("Salut " + nameChanged);
          }
        };

    HelloWorld spanishGreeting =
        new HelloWorld() {
          String name = "mundo";

          public void greet() {
            greetSomeone("mundo");
          }

          public void greetSomeone(String someone) {
            name = someone;
            System.out.println("Hola, " + name);
          }

          public void addedGreetSomeone(String someone) {
            name = someone;
            System.out.println("Hola, " + name);
          }
        };
    englishGreeting.greet();
    frenchGreeting.greetSomeone("Fred");
    spanishGreeting.greet();
  }

  public static void main(String... args) {
    HelloWorldAnonymousClasses myApp = new HelloWorldAnonymousClasses();
    myApp.sayHello();
  }
}
