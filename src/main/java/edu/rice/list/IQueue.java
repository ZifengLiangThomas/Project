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

import edu.rice.util.Log;
import edu.rice.util.Option;
import edu.rice.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This is the interface for a *functional* FIFO (first in, first out) queue.
 */
public interface IQueue<T> {
  /**
   * Adds the new element to the end of the queue.
   */
  @NotNull
  @Contract(pure = true)
  IQueue<T> insert(@NotNull T t);

  /**
   * Gets the head of the queue; throws an exception if empty.
   */
  @NotNull
  @Contract(pure = true)
  T head();

  /**
   * Gets everything but the head of the queue; returns an empty queue if empty.
   */
  @NotNull
  @Contract(pure = true)
  IQueue<T> tail();

  /**
   * Optional getter: returns the head of the queue, and the remainder without the head, or Option.none() if
   * it's an empty queue. You may prefer the structural pattern matching variant {@link IQueue#match(Function, BiFunction)}.
   */
  @NotNull
  @Contract(pure = true)
  Option<Pair<T, IQueue<T>>> oget();

  /**
   * Returns how many elements are in the queue.
   */
  @Contract(pure = true)
  int size();

  /**
   * General-purpose deconstructing structural pattern matching on a queue.
   *
   * @param emptyFunc
   *     called if the queue is empty
   * @param nonEmptyFunc
   *     called if the queue is non-empty, gives the front element of the queue and a queue with the remainder
   * @param <Q>
   *     the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of whichever function matches
   */
  @NotNull
  @Contract(pure = true)
  default <Q> Q match(Function<? super IQueue<T>, ? extends Q> emptyFunc,
                      BiFunction<? super T, ? super IQueue<T>, ? extends Q> nonEmptyFunc) {
    if (empty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(head(), tail());
    }
  }

  /**
   * Returns whether or not there are any contents in the queue.
   */
  @Contract(pure = true)
  default boolean empty() {
    return false;
  }

  /**
   * Returns a lazy list that iterates over the queue in FIFO order.
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> toLazyList() {
    // As part of your week3 project, you need to make a LazyList from your Queue. You should write this
    // such that it can work with *any* implementation of IQueue, not just the ListQueue you're implementing
    // in week3. You'll be using LazyList.make() and/or LazyList.makeEmpty(). Also, because you're up here
    // in IQueue, all you can do is call other methods from IQueue. You can't (and shouldn't try to) see
    // anything specific to ListQueue.

    // throw new RuntimeException("toLazyList not implemented yet");
    if (empty()) {
      return LazyList.makeEmpty();
    }
    return LazyList.make(head(), () -> tail().toLazyList());
  }

  interface Empty<T> extends IQueue<T> {
    @Override
    @Contract(pure = true)
    default boolean empty() {
      return true;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default T head() {
      Log.e("IQueue.Empty", "can't take head() of an empty queue");
      throw new NoSuchElementException("can't take head() of an empty queue");
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default IQueue<T> tail() {
      return this;
    }

    @Override
    @Contract(pure = true)
    default @NotNull Option<Pair<T, IQueue<T>>> oget() {
      return Option.none();
    }

    @Override
    @Contract(pure = true)
    default int size() {
      return 0;
    }
  }
}
