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

import edu.rice.util.*;
import edu.rice.tree.BinaryHeap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.function.*;

/**
 * All of our lists, whether lazy or eager, will implement this interface. A bunch of "default"
 * methods are provided which are quite useful and can, of course, be overriden by the concrete list
 * class if it has a more efficient strategy.
 *
 * <p>Unlike Java's lists, this list interface is engineered around being functional. No mutation! That
 * means there are no setter methods. Every list operation returns a new list, and the old list
 * doesn't change.
 */
public interface IList<T> {
  /**
   * Similar to head(), but returns an Option variant; if there's something in the list, then you
   * get the Option.Some of the value. If the list is empty, you get Option.None
   *
   * @see #head()
   */
  @NotNull
  @Contract(pure = true)
  default Option<T> ohead() {
    return match(
        emptyList -> Option.none(),
        (head, tail) -> Option.some(head));
  }

  /**
   * Return whether the list is empty or not.
   */
  @Contract(pure = true)
  boolean empty();

  /**
   * Return the first element of the list. Will fail with a NullPointerException if you try this on
   * an empty list. Use oget() if you want an Option variant which will be well-behaved on both
   * empty and non-empty lists.
   *
   * @see #ohead()
   */
  @NotNull
  @Contract(pure = true)
  T head();

  /**
   * Return a new list with this element in front of the current list.
   */
  @NotNull
  @Contract(pure = true)
  IList<T> add(@NotNull T t);
  //
  // Engineering note: we've got multiple IList implementations, so we can't offer a default add method.
  // See inside List.java and LazyList.java for how we still manage to avoid repeating ourselves.
  //

  /**
   * Returns the same list, but backwards.
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> reverse() {
    // Foldl prepends elements, one by one from the original list onto a results list, which starts from empty.
    return foldl(makeEmptySameType(), IList::add);
  }

  /**
   * Sometimes you have an IList, maybe it's eager or maybe it's lazy, but you want to get a new
   * empty list of the *same* concrete type (i.e., LazyList vs. List). This method is a nice shorthand that does it for you.
   * Alternatively, you may of course use the static methods {@link List#makeEmpty()} or {@link LazyList#makeEmpty()}.
   *
   * <p>For added flexibility, you can use a different type parameter than the type parameter of the
   * original list.
   */
  @NotNull
  @Contract(pure = true)
  <Q> IList<Q> makeEmptySameType();

  /**
   * Return a list of everything but the first element of the current list. Calling tail() on an
   * empty list is defined to return an empty list.
   */
  @NotNull
  @Contract(pure = true)
  IList<T> tail();

  /**
   * Return the length of the list.
   */
  @Contract(pure = true)
  default int length() {
    // Foldl here ignores the elements in the list, and just uses the folding function to increment the counter each time.
    return foldl(0, (count, elem) -> count + 1);
  }

  /**
   * Returns whether the requested element exists in the list.
   */
  @Contract(pure = true)
  default boolean contains(@NotNull T value) {
    // this default implementation will be efficient for lazy lists, and will end up doing extra work on eager lists
    // but we don't really care (for now)
    return !filter(x -> x.equals(value)).empty();
  }

  /**
   * Return a list of elements matching the predicate.
   */
  @NotNull
  @Contract(pure = true)
  IList<T> filter(@NotNull Predicate<? super T> predicate);

  /**
   * Returns a list of all elements matching the predicate, while the predicate is true.
   * Once the predicate is false, no subsequent elements are returned.
   */
  @NotNull
  @Contract(pure = true)
  IList<T> takeWhile(@NotNull Predicate<? super T> predicate);

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
  default <Q> Q match(@NotNull Function<? super IList<T>, ? extends Q> emptyFunc,
                      @NotNull BiFunction<? super T, ? super IList<T>, ? extends Q> nonEmptyFunc) {
    if (empty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(head(), tail());
    }
  }

  /**
   * General-purpose structural pattern matching on a list with deconstruction as well.
   *
   * @param emptyFunc
   *     called if the list is empty
   * @param oneElemFunc
   *     called if the list has exactly one value within; the head of the list is the first argument,
   *     then a list with the remainder
   * @param twoOrMoreFunc
   *     called if the list has two or more values within; the head of the list is the first argument,
   *     then the 2nd element, then a list with the remainder
   * @param <Q>
   *     the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of invoking whichever function matches
   */
  @NotNull
  @Contract(pure = true)
  default <Q> Q match(@NotNull Function<IList<T>, ? extends Q> emptyFunc,
                      @NotNull BiFunction<? super T, ? super IList<T>, ? extends Q> oneElemFunc,
                      @NotNull TriFunction<? super T, ? super T, ? super IList<T>, ? extends Q> twoOrMoreFunc) {

    //
    // Engineering note: You may wonder why we've got wildcard *result* types for these lambdas
    // but no wildcards on the input values. Well, if you try it, you'll find that type inference
    // on all the places where we use list matching generates a stream of errors. Java can't infer
    // good values for those types. In practice, this doesn't seem to get in the way of anything,
    // so we'll leave it as-is.
    //
    if (empty()) {
      return emptyFunc.apply(this);
    } else if (tail().empty()) {
      return oneElemFunc.apply(head(), tail());
    } else {
      return twoOrMoreFunc.apply(head(), tail().head(), tail().tail());
    }
  }

  /**
   * General-purpose structural pattern matching on a list with deconstruction as well. This
   * returns nothing and expects lambdas that return nothing.
   *
   * @param emptyFunc
   *     called if the list is empty
   * @param nonEmptyFunc
   *     called if the list has at least one value within
   */
  default void consume(@NotNull Consumer<? super IList<T>> emptyFunc,
                       @NotNull BiConsumer<? super T, ? super IList<T>> nonEmptyFunc) {
    if (empty()) {
      emptyFunc.accept(this);
    } else {
      nonEmptyFunc.accept(head(), tail());
    }
  }

  /**
   * General-purpose structural pattern matching on a list with deconstruction as well. This
   * returns nothing and expects lambdas that return nothing.
   *
   * @param emptyFunc
   *     called if the list is empty
   * @param oneElemFunc
   *     called if the list has exactly one value within; the head of the list is the first argument,
   *     then a list with the remainder
   * @param twoOrMoreFunc
   *     called if the list has two or more values within; the head of the list is the first argument,
   *     then the 2nd element, then a list with the remainder
   */
  default void consume(@NotNull Consumer<? super IList<T>> emptyFunc,
                       @NotNull BiConsumer<? super T, ? super IList<T>> oneElemFunc,
                       @NotNull TriConsumer<? super T, ? super T, ? super IList<T>> twoOrMoreFunc) {

    //
    // Engineering note: You may wonder why we've got wildcard *result* types for these lambdas
    // but no wildcards on the input values. Well, if you try it, you'll find that type inference
    // on all the places where we use list matching generates a stream of errors. Java can't infer
    // good values for those types. In practice, this doesn't seem to get in the way of anything,
    // so we'll leave it as-is.
    //
    if (empty()) {
      emptyFunc.accept(this);
    } else if (tail().empty()) {
      oneElemFunc.accept(head(), tail());
    } else {
      twoOrMoreFunc.accept(head(), tail().head(), tail().tail());
    }
  }

  /**
   * Given a function that returns a *list* of things, flatMap concatenates those lists. Zero-length
   * lists are handled correctly.
   */
  @NotNull
  @Contract(pure = true)
  <Q> IList<Q> flatmap(@NotNull Function<? super T, ? extends IList<? extends Q>> f);

  /**
   * Given a function that returns an optional result, applied to a list of values, oflatmap unpacks the
   * options and returns a list of the Option.some() values.
   *
   * <p>You can think of an Option as if it was a list that had zero or one things in it, so when we
   * have a lambda that returns an Option value, that's akin to having a lambda that returns a
   * list, which might be empty, and we want to concatenate those lists.
   *
   * <p>(In other functional programming languages, Options are literally lists with zero or one
   * element inside, so flatmap works without modification. In Java8, Option is a distinct type,
   * so we have oflatmap() as a variant on flatmap() to deal with it.)
   *
   * @see Option#flatmap(Function)
   */
  @NotNull
  @Contract(pure = true)
  default <Q> IList<Q> oflatmap(@NotNull Function<? super T, Option<? extends Q>> f) {
    return map(f).filter(Option::isSome).map(Option::get);
  }

  /**
   * Return a list with this function applied to each of the elements of the current list.
   */
  @NotNull
  @Contract(pure = true)
  <Q> IList<Q> map(@NotNull Function<? super T, ? extends Q> f);

  /**
   * Return a list with afterTail concatenate after it, (e.g., if a = [1,2,3] and b=[4,5,6],
   * a.concat(b) -&gt; [1,2,3,4,5,6]). If you need lazy concatenation, there are static methods
   * in LazyList to help you.
   * @see LazyList#lazyConcat(IList)
   * @see LazyList.Helpers#lazyConcat(IList, Supplier)
   */
  @NotNull
  @Contract(pure = true)
  IList<T> concat(@NotNull IList<? extends T> afterTail);

  /**
   * Return at most the first n elements of the list, could be fewer.
   */
  @NotNull
  @Contract(pure = true)
  IList<T> limit(int n);

  /**
   * Return Option.Some of the nth element in the list, if present, otherwise Option.None (the head of the
   * list would correspond to n=0). Warning: this function runs in O(n) time. If you try to implement a
   * classic for-loop over the list using this, it will run in O(n^2) time. You don't want that.
   */
  @NotNull
  @Contract(pure = true)
  default Option<T> nth(int n) {
    // We could write this recursively, and it would be much cleaner, but also it would be slower and would
    // run out of memory. We could alternatively do something complicated with map and find, but this function
    // is something that's going to be called often, so we care about keeping it fast.

    if (n < 0) {
      return Option.none();
    }

    IList<T> list = this;
    for (;;) {
      if (list.empty()) {
        return Option.none();
      }
      if (n == 0) {
        return Option.some(list.head());
      }
      n--;
      list = list.tail();
    }
  }

  /**
   * Skips N elements of the list, returning whatever remains of the list after that.
   * If the list has fewer than N elements, an empty list will be returned.
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> skipN(int n) {
    // written here in a mutating style because otherwise we
    // could have a stack overflow, because Java can't do tail-call optimization...

    IList<T> currentList = this;

    for (int i = 0; i < n; i++) {
      currentList = currentList.tail();
      if (currentList.empty()) {
        return currentList;
      }
    }

    return currentList;
  }

  /**
   * Returns a new list equivalent to the old list, but with the nth item replaced by the (option)
   * value. If the option is none, then we're replacing that list element with nothing --
   * deleting it.
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> updateNth(int n, @NotNull Option<? extends T> newVal) {
    return updateNth(n, val -> newVal);  // lambda ignores the old value
  }

  /**
   * Returns a new list equivalent to the old list, but with the nth item replaced by function
   * applied to the old value. If the option is none, then we're replacing that list element with
   * nothing -- deleting it.
   */
  @NotNull
  @Contract(pure = true)
  IList<T> updateNth(int n, @NotNull Function<? super T, Option<? extends T>> updateF);

  /**
   * Return a subsequence of the list, starting at first and ending at last, inclusive, where first
   * and last correspond to the same elements returned by nth. If the requested range goes beyond the
   * end of the list, as many values as are in range will be returned. If the entire range is outside
   * of the list, then an empty list will be returned.
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> sublist(int first, int last) {
    if (first > last) {
      return makeEmptySameType();
    }
    if (first < 0) {
      first = 0;
    }
    if (last < 0) {
      return makeEmptySameType();
    }

    return skipN(first).limit(last - first + 1);
  }

  /**
   * Returns a new list equal to the old list in sorted order (lowest to highest) based on the
   * lessThanFunction that's passed. For a list of integers, this might be
   * <pre>
   * <code>(a,b) -&gt; a&lt;b</code>
   * </pre>
   * while for a general-purpose comparable it would be
   * <pre>
   * <code>(a,b) -&gt; a.compareTo(b)&lt;0</code>.
   * </pre>
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> sort(@NotNull BiPredicate<? super T, ? super T> lessThanFunction) {
    BinaryHeap<T> heap = new BinaryHeap<>(lessThanFunction);

    foreach(heap::insert);
    return heap.drainToLazyList();
  }

  /**
   * Foreach operator: runs the consumer once on each item, start to finish; returns nothing.
   */
  default void foreach(@NotNull Consumer<? super T> consumer) {
    //
    // as with foldl, there's a serious boost to be had by converting this to a loop
    //
    IList<T> current = this;
    for (;;) {
      if (current.empty()) {
        return;
      }
      consumer.accept(current.head());
      current = current.tail();
    }
  }

  /**
   * Given another list of the same length as this list, "zip" it together with the current list,
   * using the zipFunc to combine the elements of the two lists together, and yielding a list of the
   * results. Whichever list is shorter will determine the ultimate length of the zipped result. Any
   * extra entries in one list without a corresponding entry in the other will be ignored.
   */
  @NotNull
  @Contract(pure = true)
  <U, V> IList<V> zip(@NotNull IList<? extends U> list, @NotNull BiFunction<? super T, ? super U, ? extends V> zipFunc);


  /**
   * Fold left: folds from head to tail. Example: the list is {a,b,c,d,e} and the folding function is
   * plus, then this returns (((((zero + a) + b) + c) + d) + e). If either foldl or foldr would give you the
   * same answer (e.g., if the folding function f is associative), then foldl is preferable. The
   * runtime speed is about the same, but foldr will have a stack-overflow on very large lists and foldl
   * won't.
   *
   * <p>Note: when the result type is different from the list element type (e.g., if you're folding a
   * list of strings into a tree of strings) then the zero should be of the result type,
   * and the corresponding function f will take two arguments: the result type (first argument) and
   * the list element type (second argument).
   *
   * @param zero
   *     value when there's nothing else to fold (e.g., for strings, this might be the empty
   *     string)
   * @param f
   *     folding function (but this one might be used for accumulating things, where the left-arg to
   *     the function is the zero and the right-arg are the elements of the list -- think
   *     like adding elements from the list into a tree or something)
   */
  @NotNull
  @Contract(pure = true)
  default <U> U foldl(@NotNull U zero, @NotNull BiFunction<? super U, ? super T, ? extends U> f) {
    // recursive version
//    if(empty()) return zero;
//    return tail().foldl(f.apply(zero, head()), f);

    // iterative version
    IList<T> current = this;
    for (;;) {
      if (current.empty()) {
        return zero;
      }
      zero = f.apply(zero, current.head());
      current = current.tail();
    }
  }


  /**
   * Fold right: combines the head with the folded accumulation of the tail. Example: the list is
   * {a,b,c,d,e} and the folding function is plus, then this returns (a + (b + (c + (d + (e + zero))))).
   *
   * <p>Note: when the result type is different from the list element type (e.g., if you're folding a
   * list of strings into a tree of strings) then the zero should be of the result type,
   * and the corresponding function f will take two arguments: the result type (first argument) and
   * the list element type (second argument).
   *
   * @param zero
   *     value when there's nothing else to fold (e.g., for strings, this might be the empty
   *     string)
   * @param f
   *     folding function (e.g., for strings, this might be string concatenation)
   */
  @NotNull
  @Contract(pure = true)
  default <U> U foldr(@NotNull U zero, @NotNull BiFunction<? super T, ? super U, ? extends U> f) {
    if (empty()) {
      return zero;
    }
    return f.apply(head(), tail().foldr(zero, f));
  }

  /**
   * Converts each element of the list to a string, then concatenates them with the mergeStr between
   * each one.
   */
  @NotNull
  @Contract(pure = true)
  default String join(@NotNull String mergeStr) {
    //
    // original, uses lots of string concatenation
    //
//        return map(Object::toString)  // first convert everything to a string
//                .foldl((a, b) -> ((a == "") ? b : a + mergeStr + b), ""); // then concatenate

    //
    // better, uses StringBuilder
    //

    // the logic here is a bit messy: if we're an empty list, then we trivially return ""
    // otherwise, we'll treat the head specially then load in the tail, using the mergeStr
    // to glue it all together.

    if (empty()) {
      return "";
    }

    // if we get here, the list has a head, at the very least
    StringBuilder result = new StringBuilder();
    result.append(head().toString());
    tail().map(Object::toString)
        .foreach(str -> {
          result.append(mergeStr);
          result.append(str);
        });

    return result.toString();
  }

  /**
   * Sometimes, you want to make a lazylist "force" all of its elements to exist, rather than
   * waiting until they're lazily demanded. This matters for a number of algorithms defined over
   * lazy lists. For that, we have the force() method. It's a no-op for ordinary lists. Warning:
   * calling force() on an infinitely-long lazy list will never return.
   *
   * <p>For convenience, force() returns the same list as it was called on, making it easier
   * to use in method-calling pipelines.
   */
  @NotNull
  default IList<T> force() {
    return this; // for an eager list, we don't have to do anything; for a lazy list, we'll override this
  }

  /**
   * If we have a list and a comparison function on elements of the list, then this will say whether or not
   * they're sorted in natural order, according to the orderingFunc that's passed as a parameter.
   * (orderingFunc should act like "less-than-or-equals")
   *
   * <p>If your list is defined over a comparable type, then there's a static method available that will
   * use the built-in comparison for you.
   * @see IList#isSorted(IList)
   */
  @Contract(pure = true)
  default boolean isSorted(@NotNull BiPredicate<? super T,? super T> orderingFunc) {
    //
    // Note: we could have done this with match() and recursion, but we don't want to run out of memory,
    // during these computations. Similarly, we could have done it with foldl, but we want to terminate
    // early if the list isn't sorted. So, we're stuck with this hand-written loop instead.
    //
    if (empty()) {
      return true;
    }
    if (tail().empty()) {
      return true;
    }

    T prev = head();
    IList<T> list = tail();
    while (!list.empty()) {
      if (!orderingFunc.test(prev, list.head())) {
        return false;
      }
      prev = list.head();
      list = list.tail();
    }
    return true;
  }

  /**
   * If we have a list of comparables, then this will say whether or not they're sorted in natural order.
   * Note that this is a static method, since it's only useful over lists of comparables. There's
   * also an instance method on IList that will check any list for being sorted based on a predicate
   * that specifies the natural ordering.
   * @see IList#isSorted(BiPredicate)
   */
  @Contract(pure = true)
  static <T extends Comparable<? super T>> boolean isSorted(@NotNull IList<? extends T> list) {
    return list.isSorted((a,b) -> a.compareTo(b) <= 0);
  }

  /**
   * Works like mapping a normal function on a list, but returns a list of key/value tuples, where
   * the values are the result of applying the mapping function f to the key. Note that this is a
   * <i>static method</i>, not an instance method, because it needs to restrict the list to be a list of
   * Comparable types. This restriction is necessary because IMap and TreapMap require their keys to
   * be comparable, and mapkv() is designed to play nicely with IMap and TreapMap.
   *
   * <p>If you want to convert the resulting list of key-value tuples into a map, you may prefer
   * the helper function built into TreapMap.
   *
   * @see edu.rice.tree.TreapMap#fromList(IList, Function)
   */
  @NotNull
  @Contract(pure = true)
  static <K extends Comparable<? super K>, V> IList<KeyValue<K, V>>
      mapkv(@NotNull IList<? extends K> list, @NotNull Function<? super K, ? extends V> f) {

    return list.map(key -> KeyValue.make(key, f.apply(key)));
  }

  /**
   * Useful when converting from wildcard types to concrete types (e.g., from IList&lt;? extends
   * T&gt; to IList&lt;T&gt;). Also, note that this is a static method, not an instance method.
   *
   * <p>This is only allowable because our lists are immutable. If you tried to play this sort
   * of game with java.util.List and other such classes, you could violate static soundness and end
   * up with a runtime type error. (No mutation!)
   */
  @NotNull
  @Contract(pure = true)
  static <T> IList<T> narrow(@NotNull IList<? extends T> input) {
    @SuppressWarnings("unchecked")
    IList<T> result = (IList<T>) input;

    // Cool trick: when the following line is type-safe, then you know it's safe to do the
    // above unchecked typecast, which runs in constant time versus the below line which is O(n).
    // Why does the line below type check? Liskov Substitution Principle! Then why can't we
    // just have this happen automatically? Why do we need narrow() at all? Mutation!

//    IList<T> result2 = input.map(x->x);

    return result;
  }


  /**
   * Empty lists, whether eager or lazy, have a lot of behaviors in common. Since we're big believers in
   * not repeating ourselves, both eager and empty list implementations will implement the IList.Empty
   * interface, and thus pick up all these default methods which override IList methods above.
   *
   * @see IList
   */
  interface Empty<T> extends IList<T> {
    @Override
    default boolean empty() {
      return true;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default T head() {
      Log.e("IListEmpty", "can't take head() of an empty list");
      throw new NoSuchElementException("can't take head() of an empty list");
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IList<T> tail() {
      Log.e("IListEmpty", "can't take tail() of an empty list");
      throw new NoSuchElementException("can't take tail() of an empty list");
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IList<T> reverse() {
      return this;
    }

    @Override
    @Contract(pure = true)
    default int length() {
      return 0;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default <Q> IList<Q> map(@NotNull Function<? super T, ? extends Q> f) {
      return makeEmptySameType();
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default <Q> IList<Q> flatmap(@NotNull Function<? super T, ? extends IList<? extends Q>> f) {
      return makeEmptySameType();
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IList<T> concat(@NotNull IList<? extends T> afterTail) {
      return narrow(afterTail);
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IList<T> filter(@NotNull Predicate<? super T> predicate) {
      return this;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IList<T> takeWhile(@NotNull Predicate<? super T> predicate) {
      return this;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IList<T> limit(int n) {
      return this;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default Option<T> nth(int n) {
      return Option.none();
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IList<T> skipN(int n) {
      return makeEmptySameType();
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IList<T> updateNth(int n, @NotNull Function<? super T, Option<? extends T>> updateFunc) {
      return this;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IList<T> sublist(int start, int end) {
      return this;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default <U, V> IList<V> zip(@NotNull IList<? extends U> list,
                                @NotNull BiFunction<? super T, ? super U, ? extends V> zipFunc) {
      return makeEmptySameType();
    }
  }
}
