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
import org.jetbrains.annotations.Nullable;

import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * TreapSet implements the ISet interface, providing a functional set implementation
 * backed by a Treap.
 * @see ISet
 */
public interface TreapSet<T extends Comparable<? super T>> extends ISet<T> {
  //
  // Data definition:
  //
  // A TreapSet can be one of two things: empty or non-empty.
  // These are represented as TreapSet.Empty and TreapSet.NonEmptySet
  // These are just wrappers around Treap. More details there.
  //


  /**
   * Create an empty set fo the given type parameter.
   * @param <T> any comparable type
   */
  @NotNull
  @Contract(pure = true)
  static <T extends Comparable<? super T>> ISet<T> makeEmpty() {
    @SuppressWarnings("unchecked")
    ISet<T> typedEmpty = (ISet<T>) Empty.SINGLETON;
    return typedEmpty;
  }

  /**
   * Given a bunch of values passed as varargs to this function, return a set with those values.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  @NotNull
  @Contract(pure = true)
  static <T extends Comparable<? super T>> ISet<T> of(@Nullable T... values) {
    return TreapSet.<T>makeEmpty().addList(LazyList.fromArray(values));
  }

  /**
   * Given a list of values, return a set with those values.
   */
  @NotNull
  @Contract(pure = true)
  static <T extends Comparable<? super T>> ISet<T> fromList(@NotNull IList<T> list) {
    return TreapSet.<T>makeEmpty().addList(list);
  }

  /**
   * Given a list of values, return a set with those values. If two elements in the list have the same value, the
   * resulting set will use the mergeOp to combine them.
   */
  @NotNull
  @Contract(pure = true)
  static <T extends Comparable<? super T>> ISet<T> fromList(@NotNull IList<T> list, @NotNull BinaryOperator<T> mergeOp) {
    return TreapSet.<T>makeEmpty().addListMerge(list, mergeOp);
  }

  /**
   * Given a java.util.Set (hashset, etc.), get back a functional set stored in our treap structure.
   */
  @NotNull
  @Contract(pure = true)
  static <K extends Comparable<? super K>> ISet<K> fromSet(@NotNull java.util.Set<K> inSet) {
    return TreapSet.fromList(LazyList.fromIterator(inSet.iterator()));
  }

  @NotNull
  @Override
  default <R extends Comparable<? super R>> ISet<R> map(@NotNull Function<? super T,? extends R> mapFunc) {
    // we're converting the set to a list, doing the map, and then converting back to a set again
    return fromList(IList.narrow(toList().map(mapFunc)));
  }

  @NotNull
  @Override
  default <R extends Comparable<? super R>> ISet<R> flatmap(@NotNull Function<? super T,? extends ISet<? extends R>> mapFunc) {
    // We're converting the set to a list, doing the map, and then folding all the resulting sets together.
    // It's a bit annoying to have to map the ISet::narrow function, since it's really a no-op, but this
    // makes the types come out properly. Also of note, Java8 doesn't require the type annotation on ISet::narrow
    // but IntelliJ does. Type inference is fun.
    return IList.narrow(toList().map(mapFunc).map(ISet::<R>narrow))
        .foldl(makeEmpty(), ISet::union);
  }

  class NonEmptySet<T extends Comparable<? super T>> implements TreapSet<T> {
    @NotNull
    private final ITree<T> treap;

    // not for external use; start from the empty TreapSet instead
    private NonEmptySet(@NotNull ITree<T> treap) {
      this.treap = treap;
    }

    @NotNull
    @Override
    public <Q extends Comparable<? super Q>> ISet<Q> makeEmptySameType() {
      return makeEmpty();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null) {
        return false;
      }

      if (!(o instanceof ISet)) {
        return false;
      }

      ISet<?> set = (ISet<?>) o;

      if (set.empty()) {
        return false;
      }

      //
      // Because set equality is a different animal from structural equality, and each treap could well have
      // a very different structure (because of the randomness), we need to do something else, like converting
      // the sets to a sorted list, which means doing an in-order traversal. Because of laziness, this will
      // terminate early if they're not equal, so it should be reasonably efficient.
      //
      return toSortedList().equals(set.toSortedList());

    }

    @Override
    public int hashCode() {
      return treap.hashCode();
    }

    @NotNull
    @Override
    public ISet<T> add(@NotNull T value) {
      return new NonEmptySet<>(treap.insert(value));
    }

    @NotNull
    @Override
    public ISet<T> remove(@NotNull T value) {
      return new NonEmptySet<>(treap.remove(value)); // doesn't matter what the value is, only value equality is tested
    }

    @NotNull
    @Override
    public Option<T> oget(@NotNull T value) {
      return treap.find(value);
    }

    @Override
    public boolean empty() {
      return treap.empty();
    }

    @Override
    public int size() {
      return treap.size();
    }

    @NotNull
    @Override
    public ISet<T> greaterThan(@NotNull T query, boolean inclusive) {
      // just delegate to the internal treap
      return new NonEmptySet<>(treap.greaterThan(query, inclusive));
    }

    @NotNull
    @Override
    public ISet<T> lessThan(@NotNull T query, boolean inclusive) {
      // just delegate to the internal treap
      return new NonEmptySet<>(treap.lessThan(query, inclusive));
    }

    @Override
    @NotNull
    public IList<T> toSortedList() {
      return treap.toLazyList(); // this happens to sort its results, doing an in-order traversal
    }

    @NotNull
    @Override
    public String toString() {
      return "{" + toSortedList().join(", ") + "}";
    }
  }

  class Empty<T extends Comparable<? super T>> implements ISet.Empty<T>, TreapSet<T> {
    private static final ISet<?> SINGLETON = new TreapSet.Empty<>();

    // external user: don't call this; instead, call makeEmpty()
    private Empty() { }

    @NotNull
    @Override
    public <Q extends Comparable<? super Q>> ISet<Q> makeEmptySameType() {
      return makeEmpty();
    }

    @NotNull
    @Override
    public ISet<T> add(@NotNull T value) {
      return new NonEmptySet<>(Treap.<T>makeEmpty().insert(value));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null) {
        return false;
      }

      if (!(o instanceof ISet)) {
        return false;
      }

      ISet<?> set = (ISet<?>) o;

      return set.empty();
    }

    @Override
    public int hashCode() {
      return 1;
    }

    @NotNull
    @Override
    public String toString() {
      return "{}";
    }
  }
}
