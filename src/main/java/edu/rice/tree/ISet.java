/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.tree;

import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.util.Option;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public interface ISet<T extends Comparable<? super T>> {
  /**
   * Returns a new set, adding the additional value. If the value is already present, the prior
   * value is replaced.
   */
  @NotNull
  @Contract(pure = true)
  ISet<T> add(@NotNull T value);

  /**
   * Returns a new set, without the value, if present. If the value is absent, this returns the same
   * set.
   */
  @NotNull
  @Contract(pure = true)
  ISet<T> remove(@NotNull T value);

  /**
   * Returns whether or not the set contains the given value.
   */
  @Contract(pure = true)
  default boolean contains(@NotNull T value) {
    return oget(value).isSome();
  }

  /**
   * Returns a value from the set "equal" to the input value, if present, otherwise
   * Option.none().
   */
  @NotNull
  @Contract(pure = true)
  Option<T> oget(@NotNull T value);

  /**
   * Returns whether there are values in the set.
   */
  @Contract(pure = true)
  boolean empty();

  /**
   * Returns the cardinality of the set.
   */
  @Contract(pure = true)
  int size();

  /**
   * Map a function over each element, returns a set of the results. Analogous to {@link IList#map}.
   */
  @NotNull
  @Contract(pure = true)
  <R extends Comparable<? super R>> ISet<R> map(@NotNull Function<? super T,? extends R> mapFunc);

  /**
   * Map a function over each element, each of which returns an ISet, and then merge the
   * sets together. Analogous to {@link IList#map}.
   */
  @NotNull
  @Contract(pure = true)
  <R extends Comparable<? super R>> ISet<R> flatmap(@NotNull Function<? super T, ? extends ISet<? extends R>> mapFunc);

  /**
   * Returns a new set corresponding to the set-union of the two sets. If the same value (i.e.,
   * they're "equal") exists in both sets, the union set's value will have the result of calling the
   * mergeOp operation on the two original values.
   */
  @NotNull
  @Contract(pure = true)
  default ISet<T> union(@NotNull ISet<T> otherSet, @NotNull BinaryOperator<T> mergeOp) {
    return toList().foldl(otherSet, (set, elem) -> set.merge(elem, mergeOp));
  }

  /**
   * Returns a new set corresponding to the set-union of the two sets. If the same value (i.e.,
   * they're "equal") exists in both sets, the value in "this" wins and the other is ignored.
   * If you want some other behavior, see {@link #union(ISet, BinaryOperator)}.
   */
  @NotNull
  @Contract(pure = true)
  default ISet<T> union(@NotNull ISet<T> otherSet) {
    return union(otherSet, (me, other) -> me);
  }

  /**
   * Returns a new set corresponding to the set-intersection of the two sets. If the same value
   * (i.e., they're "equal") exists in both sets, the intersection set's value will have the result
   * of calling the mergeOp operation on the two original values.
   */
  @NotNull
  @Contract(pure = true)
  default ISet<T> intersect(@NotNull ISet<T> otherSet, @NotNull BinaryOperator<T> mergeOp) {
    // we want to *filter* the current set, removing anything that shouldn't be in the set-intersection
    return otherSet
        .toList()
        .foldl(makeEmptySameType(), (set, otherVal) -> // fold the other list into this one, looking for overlap
            oget(otherVal).match( // lookup otherVal in the current set
                () -> set,  // nothing present -> the value is also absent from the set-intersection
                mv -> set.add(mergeOp.apply(mv, otherVal)))); // merge if it's there
  }

  /**
   * Returns a new set corresponding to the set-intersection of the two sets. If the same value
   * (i.e., they're "equal") exists in both sets, the intersection set's value will have the value
   * in "this" and the other is ignored. If you want some other behavior, see {@link #intersect(ISet, BinaryOperator)}.
   */
  @NotNull
  @Contract(pure = true)
  default ISet<T> intersect(@NotNull ISet<T> otherSet) {
    return intersect(otherSet, (me, other) -> me);
  }

  /**
   * Returns a new set corresponding to the current set with the set-intersection removed. This
   * is also called the set "complement" and is sometimes written "A \ B" or "A - B".
   */
  @NotNull
  @Contract(pure = true)
  default ISet<T> except(@NotNull ISet<T> otherSet) {
    ISet<T> intersection = intersect(otherSet, (a, b) -> a); // we'll take the element from "this" set as part of a merger
    return removeList(intersection.toList());
  }

  /**
   * Returns a new set with all values greater-than (or equal to) the query value.
   */
  @NotNull
  @Contract(pure = true)
  ISet<T> greaterThan(@NotNull T queryValue, boolean inclusive);

  /**
   * Returns a new set with all values lesser-than (or equal to) the query value.
   */
  @NotNull
  @Contract(pure = true)
  ISet<T> lessThan(@NotNull T queryKey, boolean inclusive);

  /**
   * Returns a list that iterates over the values, in *sorted* order on the values, based on their
   * internal compareTo method.
   */
  @NotNull
  @Contract(pure = true)
  IList<T> toSortedList();

  /**
   * Returns a list that iterates over the values, in no guaranteed order; may or may not be sorted.
   * Call this one if you might care about performance. Call the other one if you require ordering.
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> toList() {
    // a concrete implementation might be able to go faster than this
    return toSortedList();
  }

  /**
   * Adds all the values in the list into the set, returning a new set.
   */
  @NotNull
  @Contract(pure = true)
  default ISet<T> addList(@NotNull IList<? extends T> values) {
    return values.foldl(this, ISet::add);
  }

  /**
   * Adds all the values in the list into the set, returning a new set. If the same value (i.e.,
   * they're "equal") occurs more than once, they're merged with mergeOp.
   */
  @NotNull
  @Contract(pure = true)
  default ISet<T> addListMerge(@NotNull IList<? extends T> values, @NotNull BinaryOperator<T> mergeOp) {
    return values.foldl(this, (set, elem) -> set.merge(elem, mergeOp));
  }

  /**
   * Returns a new set, adding the additional set. If the value is already present, the prior value
   * is "removed" and the "merged" value, with mergeOp, is inserted.
   */
  @NotNull
  @Contract(pure = true)
  default ISet<T> merge(@NotNull T newVal, @NotNull BinaryOperator<T> mergeOp) {
    return oget(newVal).match(
        () -> add(newVal), // no prior value, so we just add the new value in; no merging necessary
        val -> remove(newVal).add(mergeOp.apply(val, newVal))); // remove the old, add the merged value
  }

  /**
   * Removes all the elements with the given value in the list from the set, returning a new set.
   */
  @NotNull
  @Contract(pure = true)
  default ISet<T> removeList(@NotNull IList<? extends T> values) {
    return values.foldl(this, ISet::remove);
  }

  /**
   * Sometimes you have an ISet and you want an empty ISet of the *same* concrete type. This method is a nice
   * shorthand that does it for you. Alternatively, you may of course use the static method TreapSet.makeEmpty().
   * @see TreapSet#makeEmpty()
   */
  @NotNull
  @Contract(pure = true)
  <Q extends Comparable<? super Q>> ISet<Q> makeEmptySameType();

  /**
   * Useful when converting from wildcard types to concrete types (e.g., from ISet&lt;? extends
   * T&gt; to ISet&lt;T&gt;). Also, note that this is a static method, not an instance method.
   *
   * <p>This is only allowable because our lists are immutable. If you tried to play this sort
   * of game with java.util.List and other such classes, you could violate static soundness and end
   * up with a runtime type error. (No mutation!)
   */
  @NotNull
  @Contract(pure = true)
  static <T extends Comparable<? super T>> ISet<T> narrow(@NotNull ISet<? extends T> input) {
    @SuppressWarnings("unchecked")
    ISet<T> result = (ISet<T>) input;

    return result;
  }

  /**
   * Empty sets, of all sorts, will share many of these default behaviors.
   */
  interface Empty<T extends Comparable<? super T>> extends ISet<T> {
    @NotNull
    @Override
    default ISet<T> remove(@NotNull T value) {
      return this;
    }

    @NotNull
    @Override
    default Option<T> oget(@NotNull T value) {
      return Option.none();
    }

    @Override
    default boolean contains(@NotNull T value) {
      return false;
    }

    @Override
    default boolean empty() {
      return true;
    }

    @Override
    default int size() {
      return 0;
    }

    @NotNull
    @Override
    default ISet<T> greaterThan(@NotNull T query, boolean inclusive) {
      return this;
    }

    @NotNull
    @Override
    default ISet<T> lessThan(@NotNull T query, boolean inclusive) {
      return this;
    }

    @NotNull
    @Override
    default IList<T> toSortedList() {
      return LazyList.makeEmpty();
    }
  }
}
