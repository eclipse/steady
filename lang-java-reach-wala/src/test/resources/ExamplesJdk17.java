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
package org.example;

import java.util.SortedSet;
import java.util.TreeSet;

sealed abstract class Animal implements Comparable<Animal> permits Cat, Dog, Fish {

  public abstract void saySomething();

  public int compareTo(Animal _a) {
    return getClass().getName().compareTo(_a.getClass().getName());
  }
}

final class Cat extends Animal {
  public void saySomething() {
    System.out.println("purr");
  }
}

final class Dog extends Animal {
  public void saySomething() {
    System.out.println("woof");
  }
}

final class Fish extends Animal {
  public void saySomething() {
    System.out.println("...");
  }
}

class Car {
  public void saySomething() {
    System.out.println("honk!");
  }
}

public class ExamplesJdk17 {
  static SortedSet<Animal> animals = new TreeSet<>();

  private static Animal createFish() {
    return new Fish();
  }

  private static Animal createCat() {
    Animal cat = new Cat();
    animals.add(cat);
    return cat;
  }

  public static void main(String[] args) {
    Animal animal;
    if (args.length == 0) {
      animal = createCat();
      animal.saySomething();
    } else {
      animal = createFish();
      animal.saySomething();
    }
  }
}
