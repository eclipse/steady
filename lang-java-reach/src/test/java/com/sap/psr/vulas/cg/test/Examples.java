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
