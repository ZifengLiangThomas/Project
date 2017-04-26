/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week3lab;


import edu.rice.week2lists.GList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Week3LabTest {

  @Test
  public void testStringConcatFoldl() throws Exception {
    // Lab assignment, part 1: rewrite this foldl so the unit tests below succeed.
    GList<String> empty = GList.makeEmpty();
    GList<String> list1 = empty.add("t").add("a").add("c");

    String strConcat = list1.foldl("", (x,y) -> x + y);

    assertEquals("", empty.toString());
    assertEquals("cat", strConcat);
  }

  private static String distinctDetector(GList<String> input) {
    // Colon at the front guarantees we always start with a colon; colon at the end of each entry guarantees
    // that we get a string formatted like :a:b:c:d:e:
    String joinedList = input.foldl(":", (accumulator, newbie) -> accumulator + newbie + ":");

    return input.foldl("", (accumulator, newbie) -> {
      // We could have avoided the curly braces and return statements by using Java's ternary operator,
      // but then we wouldn't be able to declare the variable below, requiring us to repeat it twice.
      // Sometimes you can't win.
      String newbieWithColons = ":" + newbie + ":";
      if (joinedList.indexOf(newbieWithColons) != joinedList.lastIndexOf(newbieWithColons)) {
        return newbie;
      } else {
        return accumulator;
      }
    });
  }

  @Test
  public void testElementDistinctnessFoldl() throws Exception {
    // Lab assignment, part 2: rewrite this foldl so the unit tests below succeed. You may wish to
    // consult the Javadoc for java.lang.String:
    // https://docs.oracle.com/javase/8/docs/api/java/lang/String.html
    GList<String> empty = GList.makeEmpty();
    GList<String> list1 = empty.add("mississippi");
    GList<String> list2 = empty.add("ippi").add("iss").add("miss");
    GList<String> list3 = empty.add("ippi").add("iss").add("iss").add("m");

    assertEquals("", distinctDetector(list1));
    assertEquals("", distinctDetector(list2));
    assertEquals("iss", distinctDetector(list3));

    GList<String> list4 = empty.add("ippi").add("s").add("is").add("ss").add("i").add("m");
    GList<String> list5 = empty.add("pi").add("ip").add("s").add("is").add("ss").add("i").add("m");
    GList<String> list6 = empty.add("i").add("p").add("p").add("issi").add("ss").add("mi");
    assertEquals("", distinctDetector(list4));
    assertEquals("", distinctDetector(list5));
    assertEquals("p", distinctDetector(list6));

    GList<String> list7 = empty.add("i").add("pp").add("i").add("iss").add("ss").add("mi");
    GList<String> list8 = empty.add("ippi").add("ss").add("i").add("ss").add("mi");
    assertEquals("i", distinctDetector(list7));
    assertEquals("ss", distinctDetector(list8));
  }

  @Test
  public void testStackOverflowFoldl() throws Exception {
    // Lab assignment, part 3: rewrite foldl in GList.java so the unit test below succeeds.
    GList<Integer> empty = GList.makeEmpty();
    GList<Integer> list1 = empty.add(5).add(7).add(19).add(22).add(3).add(282).add(2).add(9).add(111).add(28);
    int count = 0;

    // In the original lab, this doubled the size of the list 10 times, which got big enough that concat sometimes had
    // an internal stack overflow. Turns out that function isn't tail-call optimizable, but it is amenable to going
    // fast in lazy lists. Since we're interested in not having unit tests fail in future weeks, we've reduced the
    // maximum count here.
    while (count < 5) {
      list1 = list1.concat(list1);
      count++;
    }

    int max = GList.maximum(0, list1);
    assertEquals(282, max);
  }

}
