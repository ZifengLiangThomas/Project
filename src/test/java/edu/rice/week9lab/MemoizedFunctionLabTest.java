/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week9lab;

import edu.rice.list.IList;
import edu.rice.list.List;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.*;

import edu.rice.dynamic.MemoizedFunction;

public class MemoizedFunctionLabTest {
  private final Function<Integer,Integer> triple = x -> 3 * x;

  @Test
  public void makeBasics() throws Exception {
    final Function<Integer,Integer> memoizedTriple = MemoizedFunction.make(triple);
    assertEquals((Integer) 3, memoizedTriple.apply(1));
    assertEquals((Integer) 12, memoizedTriple.apply(4));
    assertEquals((Integer) 12, memoizedTriple.apply(4));
  }

  private int minCoinsLoop(int sum) {
    int[] coins = {1, 5, 10, 25};
    int[] min = new int[sum + 1];
    min[0] = 0;

    //initialize using max number of coins possible
    for (int i = 1; i < min.length; i++) {
      min[i] = sum;
    }

    //find the min number of coins needed for each sum
    for (int i = 1; i <= sum; i++) {
      for (int val:coins) {
        if ((val <= i) && ((min[i - val] + 1) < min[i])) {
          min[i] = min[i - val] + 1;
        }
      }
    }
    return min[sum];
  }

  private int minCoinsRecur(int sum) {
    IList<Integer> coins = List.of(1, 5, 10, 25);

    //find the min number of coins needed for each sum
    return coins.foldl(sum,(x,y) -> ((y <= sum) && ((minCoinsRecur(sum - y) + 1) < x)) ? minCoinsRecur(sum - y) + 1 : x);
  }

  private final BiFunction<Function<Integer, Integer>, Integer, Integer> minCoins = (self, sum) -> {

    IList<Integer> coins = List.of(1, 5, 10, 25);

    //find the min number of coins needed for each sum
    return coins.foldl(sum,(x,y) -> ((y <= sum) && ((self.apply(sum - y) + 1) < x)) ? self.apply(sum - y) + 1 : x);
  };

  @Test
  public void testMinCoinsLoopEasy() throws Exception {
    assertEquals(1, minCoinsLoop(10));
    assertEquals(2, minCoinsLoop(15));
    assertEquals(3, minCoinsLoop(12));
    assertEquals(2, minCoinsLoop(26));
  }

  @Test
  public void testMinCoinsLoopHard() throws Exception {
    assertEquals(20, minCoinsLoop(500));
    assertEquals(40, minCoinsLoop(1000));
    assertEquals(55, minCoinsLoop(1307));
    assertEquals(81, minCoinsLoop(2001));
    assertEquals(102, minCoinsLoop(2530));
    assertEquals(124, minCoinsLoop(3046));
    assertEquals(145, minCoinsLoop(3533));
  }

  @Test
  public void testMinCoinsRecurEasy() throws Exception {
    assertEquals(0, minCoinsRecur(0));
    assertEquals(1, minCoinsRecur(10));
    assertEquals(2, minCoinsRecur(15));
    assertEquals(3, minCoinsRecur(12));
    assertEquals(2, minCoinsRecur(26));
  }

  @Test
  public void testMinCoinsRecurHard() throws Exception {
    //assertEquals(20, minCoinsRecur(500));
    assertEquals((Integer) 20, MemoizedFunction.makeRecursive(minCoins).apply(500));
  }

  @Test
  public void testMemoMinCoinsEasy() throws Exception {
    final Function<Integer, Integer> memoMinCoins = MemoizedFunction.makeRecursive(minCoins);

    assertEquals((Integer) 1, memoMinCoins.apply(10));
    assertEquals((Integer) 2, memoMinCoins.apply(15));
    assertEquals((Integer) 3, memoMinCoins.apply(12));
    assertEquals((Integer) 2, memoMinCoins.apply(26));
  }

  @Test
  public void testMemoMinCoinsHard() throws Exception {
    final Function<Integer, Integer> memoMinCoins = MemoizedFunction.makeRecursive(minCoins);

    assertEquals((Integer) 20, memoMinCoins.apply(500));
    assertEquals((Integer) 40, memoMinCoins.apply(1000));
    assertEquals((Integer) 55, memoMinCoins.apply(1307));
    assertEquals((Integer) 81, memoMinCoins.apply(2001));
    assertEquals((Integer) 102, memoMinCoins.apply(2530));
    assertEquals((Integer) 124, memoMinCoins.apply(3046));
    assertEquals((Integer) 145, memoMinCoins.apply(3533));
  }
}