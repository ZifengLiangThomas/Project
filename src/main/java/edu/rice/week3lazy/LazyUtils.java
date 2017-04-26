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

import edu.rice.list.IList;
import edu.rice.list.LazyList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class LazyUtils {
  /**
   * You're going to implement a function that takes two lists (which might be lazy and might be eager)
   * defined over Comparable values. We're going to assume that these lists are both sorted. You're going
   * to then return a new list that merges them together, but you *must* do it lazily. These lists might
   * be infinitely long, but you need to return in constant time.
   *
   * <p>Use match() rather than using empty(), head(), and tail(). You may find LazyList.make() to be
   * helpful.
   */
  @NotNull
  @Contract(pure = true)
  public static <T extends Comparable<T>> IList<T> merge(@NotNull IList<T> list1, @NotNull  IList<T> list2) {
    return list1.match(
        emptyList1 -> list2,
        (head1, tail1) -> list2.match(
            emptyList2 -> list1,
            (head2, tail2) -> head1.compareTo(head2) < 0
                ? LazyList.make(head1, () -> merge(tail1, list2))
                : LazyList.make(head2, () -> merge(tail2, list1))));
  }

  /**
   * You're also going to implement a function that takes two lists (which might be lazy and might be eager)
   * defined over any values of any type. You have a function that takes the first value of each list and
   * combines them, and then you get back a list of the function applied to these in pairs. Again you're
   * going to do this lazily, since these lists might be infinitely long and you need to return in constant time.
   *
   * <p>If you get to a situation where one is empty and the other isn't, just return an empty-list.
   *
   * <p>Use match() rather than using empty(), head(), and tail(). You may find LazyList.make() to be helpful.
   */
  @NotNull
  @Contract(pure = true)
  public static <T1, T2, R> IList<R> zip(@NotNull IList<T1> list1, @NotNull IList<T2> list2,
                                         @NotNull BiFunction<T1,T2,R> zipFunc) {
    return list1.match(
        emptyList1 -> LazyList.makeEmpty(),
        (head1, tail1) -> list2.match(
            emptyList2 -> LazyList.makeEmpty(),
            (head2, tail2) -> LazyList.make(zipFunc.apply(head1, head2), () -> zip(tail1, tail2, zipFunc))));
  }
}
