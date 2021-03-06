/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week9covariance;

import static edu.rice.week9covariance.GList.narrow;

/**
 * This example goes along with our lecture on "beautiful" code and the Liskov Substitution Principle.
 */
public interface CarExamples {
  // okay, now let's say we want to have some lists of cars
  GList<Car> carList = GList.of(
      new Car("Dodge", "Diplomat", 1985),
      new Car("Ford", "Pinto", 1977));

  GList<Car> carList2 = GList.of(
      new Porsche("928S4", 1988), // allowed because Porsche can go anywhere a Car can go
      new Car("Ford", "Pinto", 1977));

  GList<Porsche> porscheList = GList.of(
      new Porsche("944", 1987),
      new Porsche("356", 1964));

  GList<Porsche911> porsche911list = GList.of(
      new Porsche911(1991),
      new Porsche911(2015));

  // how about this?
  GList<? extends Car> wildcardPorscheList = porscheList;
  GList<? extends Car> wildcardPorsche911list = porsche911list;
  GList<Car> manyCars = carList.concat(porscheList);
  GList<Car> mappedPorsches = porscheList.map(x -> x);
  GList<Car> narrowPorsches = narrow(porscheList); // much nicer
  GList<Porsche> narrowPorsches2 = narrow(porsche911list); // also works
  GList<Porsche> morePorsches = porscheList.concat(porsche911list);
  GList<Car> moreCars = narrow(porscheList.concat(porsche911list));

  // okay, let's see how match works
  String firstCarModel = wildcardPorsche911list.match(
      emptyList -> "none!",

      // The inferred type of firstCar will be fun for the compiler to figure out. wildcardPorsche911list says
      // that it's ? extends Car, and that's all we need. Every car (or class that extends Car) has a member
      // variable called "model", so this code can compile and run.
      (firstCar, tailList) -> firstCar.model);

  // consumes cars, produces 911's
  static Porsche911 mechanicA(Car c) {
    return new Porsche911(c.year);
  }

  // consumes Porsches, produces 911's
  static Porsche911 mechanicB(Porsche c) {
    return new Porsche911(c.year);
  }

  GList<Porsche> newPorscheList = carList.map(CarExamples::mechanicA);
  GList<Porsche> newPorscheList2 = porscheList.map(CarExamples::mechanicB);

  /**
   * The basic Car class, which other sorts of cars will subclass.
   */
  class Car {
    public final String make;
    public final String model;
    public final int year;

    /**
     * Construct a new car of a given make, model, and year.
     */
    public Car(String make, String model, int year) {
      this.make = make;
      this.model = model;
      this.year = year;
    }

    public void honk() {
      System.out.println("Honk!");
    }

    public void fireMachineGuns() {
      System.out.println("Oddly, nothing happens.");
    }
  }

  /**
   * Aston Martin: for the wealthy car buyer who wants to spend more money for less performance.
   */
  class AstonMartin extends Car {
    /**
     * Construct a new Porsche of a given model, and year.
     */
    public AstonMartin(String model, int year) {
      super("Aston Martin", model, year);
    }
  }

  /**
   * Aston DB5: the classic.
   */
  class AstonMartinDB5 extends AstonMartin {
    /**
     * Construct a new AstonMartinDB5 of a given year.
     */
    public AstonMartinDB5(int year) {
      super("DB5", year);
    }
  }

  /**
   * The paradigmatic DB5 for the paradigmatic secret agent.
   * <a href="http://www.autoblog.com/2010/10/27/james-bonds-aston-martin-db5-sells-for-4-6-million/">James Bond's
   * DB5: with machine guns!</a>
   */
  class BondDB5 extends AstonMartinDB5 {
    /**
     * Construct a beautiful 1964 Aston Martin DB5, with machine guns. Shaken, not stirred.
     */
    public BondDB5() {
      super(1964);
    }

    @Override
    public void fireMachineGuns() {
      System.out.println("BANG BANG BANG!");
    }
  }

  /**
   * Porsche: a special sort of car.
   */
  class Porsche extends Car {
    /**
     * Construct a new Porsche of a given model, and year.
     */
    public Porsche(String model, int year) {
      super("Porsche", model, year);
    }
  }

  /**
   * Porsche911: a special sort of Porsche.
   */
  class Porsche911 extends Porsche {
    /**
     * Construct a new Porsche 911 of a given year.
     */
    public Porsche911(int year) {
      super("911", year);
    }
  }

  /**
   * Chevy: a somewhat less special sort of Car.
   */
  class Chevy extends Car {
    /**
     * Construct a new Chevy of a given model and year.
     */
    public Chevy(String model, int year) {
      super("Chevy", model, year);
    }
  }

  /**
   * Corvette: a special sort of Chevy.
   */
  class Corvette extends Chevy {
    /**
     * Construct a new Chevy Corvette of a given year.
     */
    public Corvette(int year) {
      super("Corvette", year);
    }
  }
}
