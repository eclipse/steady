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
package com.sap.psr.vulas.cg.test;

import java.util.SortedSet;
import java.util.TreeSet;

abstract class Animal implements Comparable {
	
	public abstract void saySomething();
	
	public int compareTo(Object _a) {
	  return getClass().getName().compareTo(_a.getClass().getName());
	}
}

class Cat extends Animal {
	public void saySomething() {
	  System.out.println("purr");
	}
}


class Dog extends Animal {
  public void saySomething() {
    System.out.println("woof");
  }
}


class Fish extends Animal {
  public void saySomething() {
    System.out.println("...");
  }
}

class Car {
  public void saySomething() {
    System.out.println("honk!");
  }
}

public class Examples {
  static SortedSet<Animal> animals = new TreeSet<Animal>();
  
  private static Animal createFish() {
    return new Fish();
  }
  
  private static Animal createCat()
  {
    Animal cat = new Cat();
    animals.add(cat);
    return cat;
  }
  
  public static void main(String[] args)
  {
    Animal animal = null;
    if(args.length == 0) {
    	animal = createCat();
    	animal.saySomething();
    } else {
    	animal = createFish();
    	animal.saySomething();
    }
  }
}
