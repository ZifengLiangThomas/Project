/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.list;

import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ListMatchTest {
  private final IList<String> list0 = List.makeEmpty();
  private final IList<String> list1 = List.of("Hello");
  private final IList<String> list2 = List.of("Hello", "Rice");
  private final IList<String> list3 = List.of("Hello", "Rice", "Owls");

  @Test
  public void testMatchDeconstruct2() throws Exception {
    final Function<IList<String>, String> uberMatcher =
        list -> list.match(
            emptyList -> "empty",
            (head, tail) -> head + "/" + tail.toString());

    assertEquals("empty", uberMatcher.apply(list0));
    assertEquals("Hello/List()", uberMatcher.apply(list1));
    assertEquals("Hello/List(\"Rice\")", uberMatcher.apply(list2));
    assertEquals("Hello/List(\"Rice\", \"Owls\")", uberMatcher.apply(list3));
  }

  @Test
  public void testMatchDeconstruct3() throws Exception {
    final Function<IList<String>, String> uberMatcher =
        list -> list.match(
            emptyList -> "empty",
            (head, tail) -> head + "/" + tail.toString(),
            (head, second, tail) -> head + "/" + second + "/" + tail.toString());

    assertEquals("empty", uberMatcher.apply(list0));
    assertEquals("Hello/List()", uberMatcher.apply(list1));
    assertEquals("Hello/Rice/List()", uberMatcher.apply(list2));
    assertEquals("Hello/Rice/List(\"Owls\")", uberMatcher.apply(list3));
  }

}