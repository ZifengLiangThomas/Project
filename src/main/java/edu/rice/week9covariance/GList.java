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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface for a functional list over generic types.
 */
public interface GList<T> {
  // Data definition: a GList is one of two things:
  // - Cons: an element of type T, and another GList<T>
  // - Empty

  /**
   * Create a new empty list of the given parameter type.
   */
  @NotNull
  @Contract(pure = true)
  static <T> GList<T> makeEmpty() {
    @SuppressWarnings("unchecked")
    GList<T> castSingleton = (GList<T>) Empty.SINGLETON;
    return castSingleton;
  }

  /**
   * Given a traditional Java array, return a list; note that if the underlying array changes, the list will
   * not update itself. The values are copied out immediately.
   */
  @NotNull
  @Contract(pure = true)
  static <T> GList<T> fromArray(@Nullable T[] source) {
    if (source == null) {
      return makeEmpty();
    }
    return fromArray(source, 0);
  }

  /**
   * Given a traditional Java array, return a list; note that if the underlying array changes, the list will
   * not update itself. The values are copied out immediately, starting at the given offset.
   */
  @NotNull
  @Contract(pure = true)
  static <T> GList<T> fromArray(@NotNull T[] source, int offset) {
    if (offset >= source.length) {
      return makeEmpty();
    }
    return new GList.Cons<>(source[offset], fromArray(source, offset + 1));
  }

  /**
   * Varargs constructor.
   */
  @NotNull
  @SuppressWarnings("varargs")
  @SafeVarargs
  @Contract(pure = true)
  static <T> GList<T> of(T... source) {
    return fromArray(source);
  }


  /**
   * Returns a new list with the given value in the front.
   */
  @NotNull
  @Contract(pure = true)
  GList<T> add(@NotNull T value);

  /**
   * Returns a new list equal to the old list without its head() element.
   */
  @NotNull
  @Contract(pure = true)
  GList<T> tail();

  /**
   * Returns a new list equal to all the elements in the old list satisfying the predicate.
   */
  @NotNull
  @Contract(pure = true)
  GList<T> filter(@NotNull Predicate<? super T> predicate);

  /**
   * Returns a new list equal to the old list with the function applied to each value.
   */
  @NotNull
  @Contract(pure = true)
  <R> GList<R> map(@NotNull Function<? super T, ? extends R> f);

  /**
   * Returns a value of type T equal to the elements of the list applied in sequence to one another
   * with the given operator. This happens from left-to-right (i.e., from head() to tail()). The accumulator
   * value is used to the left of the list's head. If the list is empty, the accumulator value is returned.
   *
   * <p>Example, to join a list of strings together, you might write:
   * <br>
   * String result = listOfStrings.foldl("", (x,y)-&gt;x+y);
   * <br>
   * The lambda concatenates two strings, and the accumulator is the empty-string.
   */
  @NotNull
  @Contract(pure = true)
  T foldl(@NotNull T accumulator, @NotNull BinaryOperator<T> operator);

  /**
   * Returns a value of type T equal to the elements of the list applied in sequence to one another
   * with the given operator. This happens from right-to-left (i.e., from tail() to head()). The accumulator
   * value is used to the right of the list's last non-empty value. If the list is empty, the accumulator value is returned.
   *
   * <p>Example, to join a list of strings together, you might write:
   * <br>
   * String result = listOfStrings.foldr("", (x,y)-&gt;x+y);
   * <br>
   * The lambda concatenates two strings, and the accumulator is the empty-string.
   */
  @NotNull
  @Contract(pure = true)
  T foldr(@NotNull T accumulator, @NotNull BinaryOperator<T> operator);

  /**
   * Returns the value of the first element in the list.
   */
  @NotNull
  @Contract(pure = true)
  T head();

  /**
   * Computes the number of elements in the list.
   */
  @Contract(pure = true)
  int length();

  /**
   * Returns whether the list is empty or not.
   */
  @Contract(pure = true)
  boolean empty();

  /**
   * Returns whether the value o is somewhere in the list.
   */
  @Contract(pure = true)
  boolean contains(T o);

  /**
   * General-purpose structural pattern matching on a list with deconstruction as well.
   *
   * @param emptyFunc
   *     called if the list is empty
   * @param nonEmptyFunc
   *     called if the list has at least one value within
   * @param <Q>
   *     the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of invoking whichever function matches
   */
  @NotNull
  @Contract(pure = true)
  default <Q> Q match(@NotNull Function<? super GList<T>, ? extends Q> emptyFunc,
                      @NotNull BiFunction<? super T, ? super GList<T>, ? extends Q> nonEmptyFunc) {
    if (empty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(head(), tail());
    }
  }


  /**
   * Returns a new list equal to the "other" list concatenated at the end of "this" list.
   *
   * <p>Examples:
   * <br>
   * {1,2,3}.concat({4,5}) returns {1,2,3,4,5}
   * <br>
   * emptyList.concat({1,2}) returns {1,2}
   * <br>
   * {1,2}.concat(emptyList} returns {1,2}
   */
  @NotNull
  @Contract(pure = true)
  GList<T> concat(@NotNull GList<? extends T> other);

  /**
   * Returns a new list equal to at most the first n elements of "this" list. If n &gt; length(),
   * then the returned list will be equal to "this" list. If n &lt;= 0, an empty list will be returned.
   */
  @NotNull
  @Contract(pure = true)
  GList<T> limit(int n);

  /**
   * Narrows a list from a wildcard parameter to a regular parameter.
   */
  @NotNull
  @Contract(pure = true)
  static <T> GList<T> narrow(GList<? extends T> list) {
    @SuppressWarnings("unchecked")
    GList<T> result = (GList<T>) list;
    return result;
  }

  /**
   * Returns a list of integers, beginning at start and continuing by increment until the headVal
   * would be outside of [start,end] (i.e., the inclusive range).
   *
   * <p>Examples:
   * <br>
   * rangeInt(1,5,1) returns {1,2,3,4,5}
   * <br>
   * rangeInt(1,5,2) returns {1,3,5}
   * <br>
   * rangeInt(5,1,-1) returns {5,4,3,2,1}
   *
   */
  @NotNull
  @Contract(pure = true)
  static GList<Integer> rangeInt(int start, int end, int increment) {
    //    throw new RuntimeException("rangeInt not implemented yet");
    if (start < end) {
      return Internal.rangeIntHelper(start, start, end, increment);
    } else {
      return Internal.rangeIntHelper(start, end, start, increment);
    }
  }

  /**
   * Used internally for non-public static methods. Java9 allows for "private" methods
   * in interfaces, which would have made this much nicer, but they're not in Java8.
   */
  class Internal {
    // do not construct this class!
    private Internal() { }

    @NotNull
    @Contract(pure = true)
    private static GList<Integer> rangeIntHelper(int current, int min, int max, int increment) {
      if (current < min || current > max) {
        return makeEmpty();
      } else {
        return rangeIntHelper(current + increment, min, max, increment).add(current);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////
  // the methods below will be implemented as part of the week 2 lab
  // Unit tests are in Week2LabTest.java
  //////////////////////////////////////////////////////////////////////

  /**
   * For lists of comparable types, it's useful to compute their "minimum" based on the
   * comparison function. This is a static method rather than a member method because not
   * all lists are lists over comparable headVal types. If the input is an empty list, the
   * default headVal should be returned.
   *
   * <p>Examples:
   * GList.minimum(0, {5,2,9,3,7}) returns 2
   * <br>
   * GList.minimum(0, emptyList) returns 0
   * <br>
   * GList.minimum("", {"Charlie", "Alice", "Bob"}) returns "Alice"
   */
  @NotNull
  @Contract(pure = true)
  static <T extends Comparable<? super T>> T minimum(@NotNull T defaultValue, @NotNull GList<T> list) {
    if (list.empty()) {
      return defaultValue;
    } else {
      return list.tail().foldl(list.head(), (a, b) -> (a.compareTo(b) < 0) ? a : b);
    }
  }

  /**
   * For lists of comparable types, it's useful to compute their "maximum" based on the
   * comparison function. This is a static method rather than a member method because not
   * all lists are lists over comparable headVal types. If the input is an empty list, the
   * default headVal should be returned.
   *
   * <p>Examples:
   * GList.maximum(0, {5,2,9,3,7}) returns 9
   * <br>
   * GList.maximum(0, emptyList) returns 0
   * <br>
   * GList.maximum("", {"Charlie", "Alice", "Bob"}) returns "Charlie"
   */
  @NotNull
  @Contract(pure = true)
  static <T extends Comparable<? super T>> T maximum(@NotNull T defaultValue, @NotNull GList<T> list) {
    if (list.empty()) {
      return defaultValue;
    } else {
      return list.tail().foldl(list.head(), (a, b) -> (a.compareTo(b) > 0) ? a : b);
    }
    //    throw new RuntimeException("maximum not implemented yet");
  }

  /**
   * For lists of numbers, it's useful to compute their average.
   * This is a static method rather than a member method because not
   * all types allow you to do arithmetic with them. If the list is
   * empty, the default headVal should be returned.
   *
   * <p>Examples:
   * <br>
   * GList.average(0, {5,2,9,3,7}) returns 5.2
   * <br>
   * GList.average(0, emptyList) returns 0
   */
  @Contract(pure = true)
  static double average(double defaultValue, @NotNull GList<Double> list) {
    if (list.empty()) {
      return defaultValue;
    } else {
      return list.foldl(0.0, (a, b) -> a + b) / list.length();
    }
    //    throw new RuntimeException("average not implemented yet");
  }

  class Cons<T> implements GList<T> {
    @NotNull
    private final T headVal;

    @NotNull
    private final GList<T> tailVal;

    private Cons(@NotNull T value, @NotNull GList<? extends T> tailList) {
      this.headVal = value;
      this.tailVal = narrow(tailList);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> add(@NotNull T value) {
      return new Cons<>(value, this);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> tail() {
      return tailVal;
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> filter(@NotNull Predicate<? super T> predicate) {
      if (predicate.test(headVal)) {
        return tailVal.filter(predicate).add(headVal);
      } else {
        return tailVal.filter(predicate);
      }
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public <R> GList<R> map(@NotNull Function<? super T,? extends R> f) {
      R head = f.apply(headVal);
      GList<R> tailList = tailVal.map(f);
      return tailList.add(head);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public T foldl(@NotNull T accumulator, @NotNull BinaryOperator<T> operator) {
      return tailVal.foldl(operator.apply(accumulator, headVal), operator);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public T foldr(@NotNull T accumulator, @NotNull BinaryOperator<T> operator) {
      return operator.apply(headVal, tailVal.foldr(accumulator, operator));
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public T head() {
      return headVal;
    }

    @Override
    @Contract(pure = true)
    public int length() {
      return 1 + tailVal.length();
    }

    @Override
    @Contract(pure = true)
    public boolean empty() {
      return false;
    }

    @Override
    @Contract(pure = true)
    public boolean contains(@NotNull T o) {
      if (o == headVal) {
        return true; // if they're pointing to the exact same object
      } else if (o.equals(headVal)) {
        return true; // we found it
      }

      // we didn't find it, so let's look recursively
      return tail().contains(o);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public String toString() {
      // this check isn't strictly necessary, but we don't want to add whitespace
      // between the last list element and the empty-list afterward
      if (tailVal.empty()) {
        return headVal.toString();
      } else {
        return headVal.toString() + " " + tailVal.toString();
      }
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof GList<?>)) {
        return false;
      }

      GList<?> otherList = (GList<?>) other;
      return head().equals(otherList.head()) && tail().equals(otherList.tail());
    }

    @Override
    public int hashCode() {
      return headVal.hashCode() + tailVal.hashCode() * 31; // a hack, but better than nothing
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> concat(@NotNull GList<? extends T> other) {
      return tailVal.concat(other).add(headVal);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> limit(int n) {
      if (n < 1) {
        return makeEmpty();
      } else {
        return tailVal.limit(n - 1).add(headVal);
      }
      //    throw new RuntimeException("limit not implemented yet");
    }

  }

  class Empty<T> implements GList<T> {
    private Empty() { }

    private static final GList<?> SINGLETON = new Empty<>();

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> add(@NotNull T val) {
      return new Cons<>(val, this);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> tail() {
      // An unusual design decision, but we're having the tail() of an empty list be another empty list,
      // rather than blowing up with an exception or something. This might allow naïve code, iterating over
      // a list, to hit an infinite loop, but that's a bug and it still needs to be fixed.
      return this;
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> filter(@NotNull Predicate<? super T> predicate) {
      return makeEmpty();
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public T head() {
      throw new NoSuchElementException("can't take head() of an empty list");
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> concat(@NotNull GList<? extends T> other) {
      return narrow(other);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public GList<T> limit(int n) {
      return this;
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public <R> GList<R> map(@NotNull Function<? super T, ? extends R> f) {
      return makeEmpty();
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public T foldl(@NotNull T accumulator, @NotNull BinaryOperator<T> operator) {
      return accumulator;
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public T foldr(@NotNull T accumulator, @NotNull BinaryOperator<T> operator) {
      return accumulator;
    }

    @Override
    @Contract(pure = true)
    public int length() {
      return 0;
    }

    @Override
    @Contract(pure = true)
    public boolean empty() {
      return true;
    }

    @Override
    @Contract(pure = true)
    public boolean contains(@NotNull T t) {
      return false;
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public String toString() {
      return "";
    }

    @Override
    public boolean equals(Object other) {
      return other != null &&
          other instanceof GList<?> &&
          ((GList<?>) other).empty();
    }

    @Override
    public int hashCode() {
      return 1; // a hack, but better than nothing
    }
  }
}
