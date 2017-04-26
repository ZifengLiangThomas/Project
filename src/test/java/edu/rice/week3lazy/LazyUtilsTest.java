/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week3lazy;

import edu.rice.util.Pair;
import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import org.junit.Test;

import static org.junit.Assert.*;
import static edu.rice.week3lazy.LazyUtils.*;

public class LazyUtilsTest {

  @Test
  public void testMergeSimple() throws Exception {
    IList<Integer> list1 = List.of(1,3,5);
    IList<Integer> list2 = List.of(2,4,6);
    assertEquals(List.of(1,2,3,4,5,6), merge(list1, list2));

    IList<Integer> list3 = List.of(1,3,5,7,9);
    IList<Integer> list4 = List.of(2,4,6,8,10);
    assertEquals(List.of(1,2,3,4,5,6,7,9), merge(list3, list2));
    assertEquals(List.of(1,2,3,4,5,6,7,9), merge(list2, list3));
    assertEquals(List.of(1,2,3,4,5,6,8,10), merge(list4, list1));
    assertEquals(List.of(1,2,3,4,5,6,8,10), merge(list1, list4));
  }

  @Test
  public void testMergeLazy() throws Exception {
    // if this test doesn't finish in constant time, then you didn't do a lazy list merge

    IList<Integer> evens = LazyList.iterate(0, x -> x + 2); // even numbers
    IList<Integer> odds = LazyList.iterate(1, x -> x + 2); // odd numbers
    assertEquals(List.of(0,1,2,3,4,5), merge(evens, odds).limit(6));
  }

  @Test
  public void testZipSimple() throws Exception {
    IList<String> names = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve", "Frank", "Hao");
    IList<Integer> lengths = names.map(String::length);

    assertEquals(
        names.map(name -> new Pair<>(name, name.length())),
        zip(names, lengths, Pair::new));
  }

  @Test
  public void testZipLazy() throws Exception {
    IList<Integer> evens = LazyList.iterate(0, x -> x + 2);
    IList<Integer> squares = evens.map(x -> x * x);
    IList<Integer> evensPlusSquares = zip(evens, squares, (even, square) -> even + square);

    assertEquals(List.of(0, 2 + 4, 4 + 16, 6 + 36, 8 + 64), evensPlusSquares.limit(5));
  }
}