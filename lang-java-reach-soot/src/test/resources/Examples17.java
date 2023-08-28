package com.sap.psr.vulas.cg.test;

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

public class Examples {
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

