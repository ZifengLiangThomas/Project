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

import edu.rice.util.Option;
import edu.rice.util.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static edu.rice.list.IList.narrow;

/**
 * Functional lists! A variety of static methods are provided to help construct a list.
 * These should be used rather than trying to directly call the List constructor.
 * @see IList
 */
public interface List<T> extends IList<T> {
  //
  // Data definition:
  //
  // A List is either: an empty-list, or a "cons cell" containing a value and a reference to another list.
  // These are represented by the List.Empty and List.Cons classes.
  //
  // External users will never see either of these. Instead, they'll use the static methods of List to get
  // things started, then will use the exported public interface (IList) which both Empty and Cons implement.
  //

  /**
   * Create an empty list of the given type parameter.
   */
  @NotNull
  @Contract(pure = true)
  static <T> IList<T> makeEmpty() {
    // The same instance can be used for any T since it will always be empty.
    // Details:
    // http://stackoverflow.com/questions/14313528/returning-a-generic-empty-list

    @SuppressWarnings("unchecked")
    IList<T> typedEmptyList = (IList<T>) Empty.SINGLETON;
    return typedEmptyList;
  }

  /**
   * Construct a list with the specified head element and another list as the tail.
   * demand. Note that this constructor is public and is intended for use by any client.
   * If you want a list with one element, then you should use List.makeEmpty() to create
   * the second argument. Alternately, use the List.of() helper method.
   * @see List#of(Object[])
   */
  @NotNull
  @Contract(pure = true)
  static <T> IList<T> make(@NotNull T headVal, @NotNull IList<? extends T> tailVal) {
    //
    // Engineering note: why are we using a static function rather than just making the List.Cons
    // constructor public? Primarily because we want to avoid the "List.Cons" type showing up anywhere
    // in user code. We're returning what seems like a perfectly normal IList, just like everything else here
    // does, and we expect them to be used interchangeably. By hiding the constructor behind this
    // static method, we can hide the implementation details of a List.
    //
    return new List.Cons<>(headVal, tailVal);
  }

  /**
   * Construct a list with only one element.
   */
  @NotNull
  @Contract(pure = true)
  static <T> IList<T> make(@NotNull T headVal) {
    return make(headVal, makeEmpty());
  }

  @NotNull
  @Override
  @Contract(pure = true)
  default IList<T> add(@NotNull T t) {
    // This code is the same for both Cons and Empty, so we can put it here and avoid repeating ourselves.
    return make(t, this);
  }

  /**
   * Constructs a functional list from an original java.util.List
   */
  @NotNull
  @Contract(pure = true)
  static <T> IList<T> fromList(@NotNull java.util.List<? extends T> original) {
    return fromIterator(original.iterator());
  }

  /**
   * given a traditional Java iterator, return a list that captures the output of the iterator. If the iterator is
   * infinite, this method may never return or may run out of memory.
   */
  @NotNull
  @Contract(pure = true)
  static <T> IList<T> fromIterator(@NotNull Iterator<? extends T> source) {
    if (source.hasNext()) {
      T nextVal = source.next();
      return make(nextVal, fromIterator(source));
    } else {
      return makeEmpty();
    }
  }

  /**
   * Given a traditional Java enumeration (very similar to an iterator), which might well be infinite,
   * return a lazy thunking list that captures the output of the iterator.
   */
  @NotNull
  @Contract(pure = true)
  static <T> IList<T> fromEnumeration(@NotNull Enumeration<? extends T> source) {
    if (source.hasMoreElements()) {
      T nextVal = source.nextElement();
      return make(nextVal, fromEnumeration(source));
    } else {
      return makeEmpty();
    }
  }


  /**
   * given a traditional Java array, return a list; note that if the underlying array changes, the list will
   * not update itself. The values are copied immediately.
   */
  @NotNull
  @Contract(pure = true)
  static <T> IList<T> fromArray(@Nullable T[] source) {
    if (source == null) {
      return makeEmpty();
    }
    return fromArray(source, 0);
  }

  /**
   * given a traditional Java array, return a list starting at the given offset; note that if the underlying array changes,
   * the list will not update itself. The values are copied immediately.
   */
  @NotNull
  @Contract(pure = true)
  static <T> IList<T> fromArray(@NotNull T[] source, int offset) {
    if (offset >= source.length) {
      return makeEmpty();
    }
    return make(source[offset], fromArray(source, offset + 1));
  }

  /**
   * Varargs constructor.
   */
  @NotNull
  @SuppressWarnings("varargs")
  @SafeVarargs
  @Contract(pure = true)
  static <T> IList<T> of(@Nullable T... source) {
    return fromArray(source);
  }

  /**
   * Returns a list of integers from min to max, inclusive.
   */
  @NotNull
  @Contract(pure = true)
  static IList<Integer> rangeInt(int min, int max) {
    if (min > max) {
      return makeEmpty();
    } else {
      return rangeInt(min, max, 1);
    }
  }

  /**
   * Returns a list of integers from min to max, inclusive, skipping every increment until the result would be outside
   * the range. Note: won't work if increment is negative. You may prefer one of the iterate() methods for a more
   * general-purpose way of generating such things. (e.g., rangeInt(0, 10, 2) -&gt; [0, 2, 4, 6, 8, 10])
   */
  @NotNull
  @Contract(pure = true)
  static IList<Integer> rangeInt(int start, int finish, int increment) {
    if (start < finish) {
      return Helpers.rangeInt(start, start, finish, increment);
    } else {
      return Helpers.rangeInt(start, finish, start, increment);
    }
  }

  /**
   * Functions that aren't meant for public use.
   */
  class Helpers {
    private Helpers() { } // do not instantiate!

    //
    // Engineering note: when we have "static" functions inside a class, that means they're package-scope.
    // These functions are visible anywhere inside edu.rice.list, and hidden elsewhere. That's different
    // from the static methods in the List interface, where everything is implicitly public. Java9 will allow
    // for private method in interfaces, which will change how we might solve this problem.
    //

    /**
     * Returns a list of integers, starting at "current" and incrementing by "increment" until outside of the
     * range [min,max] (inclusive).
     */
    @NotNull
    @Contract(pure = true)
    static IList<Integer> rangeInt(int current, int min, int max, int increment) {
      if (increment == 0) {
        throw new RuntimeException("rangeInt with zero increment not allowed");
      }

      // weird input yields empty output!
      if (min > max) {
        return makeEmpty();
      }

      // assumption: min < max
      if (current < min || current > max) {
        return makeEmpty();
      }
      return make(current, rangeInt(current + increment, min, max, increment));
    }

    /**
     * Recursively compare two lists for equality.
     *
     * <p>>Note that, unlike equals(), this is a static method that that takes the two lists as arguments. Also,
     * this will work for <i>any</i> two lists, not just eager lists.
     */
    @Contract(pure = true)
    static <T> boolean equalsHelper(@NotNull IList<? extends T> a, @NotNull IList<? extends T> b) {
      // standard recursive version:

      //    return a.match(
      //        aEmpty -> b.empty(),
      //        (aHead, aTail) -> b.match(
      //            bEmpty -> false,
      //            (bHead, bTail) -> aHead.equals(bHead) && equalsHelper(aTail, bTail)
      //        )
      //    );

      // tail-recursion optimization makes this a big uglier, but it's necessary to avoid stack overflows:

      for (;;) {
        boolean aEmpty = a.empty();
        boolean bEmpty = b.empty();

        if (aEmpty) {
          // if both lists are empty, then they're equal, otherwise they're not
          return bEmpty;
        }

        if (bEmpty) {
          // if only one is empty, then they're not equal
          return false;
        }

        if (!a.head().equals(b.head())) {
          // if the head elements are not equal, then the lists are not equal
          return false;
        }

        // otherwise, we need to consider the list tails
        a = a.tail();
        b = b.tail();
      }
    }

    /**
     * Computes a hash value over the elements in a list. It's a static helper function that can be used by anything
     * that implements IList, allowing for consistency across IList implementations.
     */
    @Contract(pure = true)
    static <T> int hashHelper(@NotNull IList<? extends T> list) {
      // this is a kludge: we're repeatedly multiplying by 31 and then adding in each element's hashCode
      return list.foldl(1, (hashVal, elem) -> hashVal * 31 + elem.hashCode());
    }

    /**
     * This is the implementation of toString() that all implementations of IList will use, so they behave
     * consistently.
     */
    @NotNull
    @Contract(pure = true)
    static <T> String toStringHelper(@NotNull IList<T> list) {
      return "List(" + list.map(Strings::objectToEscapedString).join(", ") + ")";
    }
  }

  /**
   * This class implements non-empty lists ("cons cells"). External users will never use this, which is
   * why this class isn't public. Instead, use IList.
   * @see IList
   */
  class Cons<T> implements List<T> {
    @NotNull
    private final T headVal;
    @NotNull
    private final IList<T> tailVal;

    /**
     * Note, this constructor is not meant to be used by clients. Instead, use add() or List.make().
     */
    private Cons(@NotNull T headVal, @NotNull IList<? extends T> tailVal) {
      this.headVal = headVal;
      this.tailVal = narrow(tailVal);
    }

    @Override
    @Contract(pure = true)
    public boolean empty() {
      return false;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public T head() {
      return headVal;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public IList<T> tail() {
      return tailVal;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public IList<T> concat(@NotNull IList<? extends T> afterTail) {
      return make(headVal, tailVal.concat(afterTail));
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
      return Helpers.hashHelper(this);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public <U, V> IList<V> zip(@NotNull IList<? extends U> list,
                               @NotNull BiFunction<? super T, ? super U, ? extends V> zipFunc) {
      return narrow(list).match(
          emptyList -> makeEmpty(),
          (head2, tail2) -> make(zipFunc.apply(headVal, head2), tail().zip(tail2, zipFunc)));
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public IList<T> filter(@NotNull Predicate<? super T> predicate) {
      if (predicate.test(headVal)) {
        return make(headVal, tailVal.filter(predicate));
      } else {
        return tailVal.filter(predicate);
      }
    }

    @NotNull
    @Override
    public IList<T> takeWhile(@NotNull Predicate<? super T> predicate) {
      if (predicate.test(headVal)) {
        return make(headVal, tailVal.takeWhile(predicate));
      } else {
        return makeEmpty();
      }
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public <Q> IList<Q> map(@NotNull Function<? super T, ? extends Q> f) {
      return make(f.apply(headVal), tailVal.map(f));
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public <Q> IList<Q> flatmap(@NotNull Function<? super T, ? extends IList<? extends Q>> f) {
      return map(f).foldl(makeEmpty(), IList::concat);
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public IList<T> updateNth(int n, @NotNull Function<? super T, Option<? extends T>> updateFunc) {
      if (n < 0) {
        return this; // not really even sure what n<0 means, so doing nothing seems reasonable
      }
      if (n == 0) {
        return updateFunc.apply(headVal).match(() -> tailVal, tailVal::add);
      } else {
        return make(headVal, tailVal.updateNth(n - 1, updateFunc));
      }
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public IList<T> limit(int n) {
      if (n < 1) {
        return makeEmpty();
      }
      return make(headVal, tailVal.limit(n - 1));
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object x) {
      if (!(x instanceof IList)) {
        return false;
      }

      IList<?> otherList = (IList<?>) x;
      return Helpers.equalsHelper(this, otherList);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public String toString() {
      return Helpers.toStringHelper(this);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public <Q> IList<Q> makeEmptySameType() {
      return makeEmpty();
    }
  }

  /**
   * Note that this class is not public. Empty lists will be instances of this class, and we'll have
   * precisely one of them. Clients of the List class can use List.makeEmpty().
   * @see Cons#makeEmpty()
   */
  class Empty<T> implements List<T>, IList.Empty<T> {
    private static final IList<?> SINGLETON = new List.Empty<>();

    // don't call this; use makeEmpty()
    private Empty() { }

    @Override
    @NotNull
    @Contract(pure = true)
    public <Q> IList<Q> makeEmptySameType() {
      return makeEmpty();
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object x) {
      if (!(x instanceof IList)) {
        return false;
      }

      IList<?> list = (IList<?>) x;
      return list.empty(); // any empty list will be equal to this one
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
      return Helpers.hashHelper(this);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public String toString() {
      return Helpers.toStringHelper(this);
    }
  }
}
