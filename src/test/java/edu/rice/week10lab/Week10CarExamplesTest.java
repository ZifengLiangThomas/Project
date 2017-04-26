/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week10lab;

import edu.rice.week9covariance.CarExamples;
import edu.rice.week9covariance.CarExamples.*;
import edu.rice.week9covariance.GList;
import org.junit.Test;

import static org.junit.Assert.*;

public class Week10CarExamplesTest {
  private GList<Porsche> carList1 = GList.of(new Porsche911(1971), new Porsche("944",1985));
  private GList<AstonMartin> carList2 =  GList.of(new AstonMartinDB5(1985));
  private GList<Car> carList3 = GList.of(new Corvette(1977), new AstonMartin("DBS", 1980), new BondDB5());
  private GList<Car> narrowCar2 = GList.narrow(carList2);
  private GList<Car> narrowCar1 = GList.narrow(carList1);
  private GList<Car> carList4 = narrowCar1.concat(narrowCar2).concat(carList3);
  private GList<Car> carList5 = carList4.concat(carList4);

  @Test
  public void testCountAstonMartins() throws Exception {
    //count the number of Aston Martins
    assertEquals(0, carList1.filter(car -> car.make.equals("Aston Martin")).length());
    assertEquals(1, carList2.filter(car -> car.make.equals("Aston Martin")).length());
    assertEquals(2, carList3.filter(car -> car.make.equals("Aston Martin")).length());
    assertEquals(3, carList4.filter(car -> car.make.equals("Aston Martin")).length());
    assertEquals(6, carList5.filter(car -> car.make.equals("Aston Martin")).length());
  }

  @Test
  public void testConvertToPorsche911MechA() {
    //convert any car to a Porsche 911 using Mechanic A

    GList<Porsche911> porsche911GList1 = narrowCar1.map(car -> CarExamples.mechanicA(car));
    GList<Porsche911> porsche911GList2 = narrowCar2.map(car -> CarExamples.mechanicA(car));
    GList<Porsche911> porsche911GList3 = carList3.map(car -> CarExamples.mechanicA(car));
    assertEquals(2, porsche911GList1.length());
    assertEquals(1, porsche911GList2.length());
    assertEquals(3, porsche911GList3.length());
    GList<Porsche911> porsche911GList4 = carList4.map(car -> CarExamples.mechanicA(car));
    assertEquals(6, porsche911GList4.length());
    GList<Porsche911> porsche911GList5 = carList5.map(car -> CarExamples.mechanicA(car));
    assertEquals(12, porsche911GList5.length());


 // replace this with the above code, then fix the above code
  }

  @Test
  public void testConvertToPorsche911MechB() {
    //convert any car to a Porsche 911 using Mechanic B (Mechanic A is unavailable)

    GList<Porsche911> porsche911GList1 = narrowCar1.map(
        car -> new Porsche(car.model, car.year)).map(car -> CarExamples.mechanicB(car));
    GList<Porsche911> porsche911GList2 = narrowCar2.map(
        car -> new Porsche(car.model, car.year)).map(car -> CarExamples.mechanicB(car));
    GList<Porsche911> porsche911GList3 = carList3.map(
        car -> new Porsche(car.model, car.year)).map(car -> CarExamples.mechanicB(car));
    assertEquals(2, porsche911GList1.length());
    assertEquals(1, porsche911GList2.length());
    assertEquals(3, porsche911GList3.length());
    GList<Porsche911> porsche911GList4 = carList4.map(
        car -> new Porsche(car.model, car.year)).map(car -> CarExamples.mechanicB(car));
    assertEquals(6, porsche911GList4.length());
    GList<Porsche911> porsche911GList5 = carList5.map(
        car -> new Porsche(car.model, car.year)).map(car -> CarExamples.mechanicB(car));
    assertEquals(12, porsche911GList5.length());


// replace this with the above code, then fix the above code
  }
}
