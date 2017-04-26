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

import edu.rice.list.LazyList;
import edu.rice.util.Option;
import edu.rice.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * General-purpose randomly balanced tree, based on
 * <a href="https://faculty.washington.edu/aragon/treaps.html">Aragon and Seidel's Treap data structure</a>.
 */
public interface Treap<T extends Comparable<? super T>> extends ITree<T> {
  /**
   * Construct an empty treap of the given type parameter.
   *
   * @param <T> any comparable type
   */
  @NotNull
  @Contract(pure = true)
  static <T extends Comparable<? super T>> ITree<T> makeEmpty() {
    @SuppressWarnings("unchecked")
    ITree<T> typedEmpty = (ITree<T>) Empty.SINGLETON;
    return typedEmpty;
  }

  /**
   * Given a bunch of values passed as varargs to this function, returns a tree with those values.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  @Contract(pure = true)
  static <T extends Comparable<? super T>> ITree<T> of(@Nullable T... values) {
    return Treap.<T>makeEmpty().insertList(LazyList.fromArray(values));
  }


  class Node<T extends Comparable<? super T>> implements Treap<T> {
    private final int priority;
    @NotNull
    private final T value;
    @NotNull
    private final ITree<T> left;
    @NotNull
    private final ITree<T> right;

    // external users: don't use this
    Node(@NotNull T value, @NotNull ITree<T> left, @NotNull ITree<T> right, int priority) {
      this.value = value;
      this.left = left;
      this.right = right;
      this.priority = priority;
    }

    @NotNull
    @Override
    public T getValue() {
      return value;
    }

    @NotNull
    @Override
    public ITree<T> getLeft() {
      return left;
    }

    @NotNull
    @Override
    public ITree<T> getRight() {
      return right;
    }

    @NotNull
    @Override
    public ITree<T> insert(@NotNull T newbie) {
      int comparison = newbie.compareTo(value);
      if (comparison < 0) {
        return new Node<>(value, left.insert(newbie), right, priority).heapify();
      }
      if (comparison > 0) {
        return new Node<>(value, left, right.insert(newbie), priority).heapify();
      }

      // if it's exactly the same object that's already there, then no merge necessary
      if (this.value == newbie) {
        return this;
      }

      // This is a curious case. If we're equal (this.equals(newbie)), but not the same (this.value != newbie),
      // then we're going to update the value in place. This will be useful for key/value stores where the
      // equals method operates on the keys.

      // note that we use the same priority as before, and the merged so there's no need for rotation
      return new Node<>(newbie, left, right, priority);
    }

    /**
     * Returns a new tree that satisfies the min-heap property (smallest priority on top).
     */
    @NotNull
    private ITree<T> heapify() {
      int leftP = left.getPriority();
      int rightP = right.getPriority();

      // if we're satisfying the heap property, then we're done
      if (priority <= leftP && priority <= rightP) {
        return this;
      }

      // if we're here, then we know the current node is not the smallest, therefore either the left or right is
      // smaller; choose the smallest and rotate that way
      if (leftP < rightP) {
        return rotateRight();
      } else {
        return rotateLeft();
      }
    }

    @Override
    public boolean empty() {
      return false;
    }

    @NotNull
    @Contract(pure = true)
    ITree<T> rotateRight() {
      // this code is essentially the same as Tree.rotateRight and Tree.rotateLeft except that we're creating new Treaps
      // rather than new Trees. We could probably concoct something to share the code using Factories of some sort, but
      // then everybody would need to carry around an extra handle to their factory, and that would be gross.

      return left.match(
          emptyTree -> this,
          (lValue, lLeft, lRight) ->
              new Node<>(lValue, lLeft, new Node<>(value, lRight, right, priority), left.getPriority()));
    }

    @NotNull
    @Contract(pure = true)
    ITree<T> rotateLeft() {
      return right.match(
          emptyTree -> this,
          (rValue, rLeft, rRight) -> new Node<>(rValue,
              new Node<>(value, left, rLeft, priority), rRight, right.getPriority()));
    }

    @NotNull
    @Override
    public ITree<T> remove(@NotNull T deadValue) {
      int comparison = deadValue.compareTo(this.value);
      if (comparison == 0) {
        // we need to remove the tree head; first see if we have an easy out
        if (left.empty()) {
          return right;
        }
        if (right.empty()) {
          return left;
        }

        // okay, both left and right are present, so we'll rotate and try again
        // (note the "priority" stuff only does something meaningful if it's a treap)
        if (left.getPriority() < right.getPriority()) {
          return rotateRight().remove(deadValue);
        } else {
          return rotateLeft().remove(deadValue);
        }

      } else if (comparison < 0) {
        // it's to the left
        return new Node<>(value, left.remove(deadValue), right, priority);
      } else {
        // it's to the right
        return new Node<>(value, left, right.remove(deadValue), priority);
      }
    }

    @Override
    public int hashCode() {
      return Tree.Helpers.hashCodeHelper(this);
    }

    @NotNull
    @Override
    public ITree<T> greaterThan(@NotNull T floor, boolean inclusive) {
      int comparison = floor.compareTo(value);
      if (comparison == 0 && !inclusive) {
        return right;
      }
      if (comparison == 0) {
        return new Node<>(value, makeEmpty(), right, priority);
      }

      // if the floor is entirely to the right
      if (comparison > 0) {
        return right.greaterThan(floor, inclusive);
      }

      // the floor is somewhere to the left
      return new Node<>(value, left.greaterThan(floor, inclusive), right, priority);
    }

    @NotNull
    @Override
    public ITree<T> lessThan(@NotNull T ceiling, boolean inclusive) {
      int comparison = ceiling.compareTo(value);
      if (comparison == 0 && !inclusive) {
        return left;
      }
      if (comparison == 0) {
        return new Node<>(value, left, makeEmpty(), priority);
      }

      // if the ceiling is entirely to the left
      if (comparison < 0) {
        return left.lessThan(ceiling, inclusive);
      }

      // the ceiling is somewhere to the right
      return new Node<>(value, left, right.lessThan(ceiling, inclusive), priority);
    }



    @Override
    public int getPriority() {
      return priority;
    }

    @Override
    public boolean valid() {
      boolean leftGood = left.empty() || (left.getPriority() >= priority && left.getValue().compareTo(value) <= 0);
      boolean rightGood = right.empty() || (right.getPriority() >= priority && value.compareTo(right.getValue()) <= 0);

      return leftGood && rightGood && left.valid() && right.valid();
    }

    @Override
    public @NotNull Option<Pair<T, ITree<T>>> removeMin() {
      // Recursively, removeMin() will never return an Option.none(), unless
      // it's called on on an empty tree, and that's handled in ITreeEmpty.
      // Consequently, we don't have to worry about removeMin.get() failing.

      return left.match(
          // if there are no left-subchildren, then we've found the minimum value,
          // and the tree without the minimum value is just the right subtree
          emptyTree -> Option.some(new Pair<>(value, right)),

          (lValue, lLeft, lRight) ->
              left.removeMin().get().match((minValue, remainingTree) ->
                  Option.some(new Pair<>(minValue, new Node<>(value, remainingTree, right, priority)))));
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof ITree<?>)) {
        return false;
      }
      ITree<?> otherTree = (ITree<?>) o;

      return Tree.Helpers.equalsHelper(this, otherTree);
    }


    @Override
    public String toString() {
      return Tree.Helpers.toStringHelper(this);
    }
  }

  class Empty<T extends Comparable<? super T>> implements Treap<T>, ITree.Empty<T> {
    private static final ITree<?> SINGLETON = new Treap.Empty<>();
    private static final Random RNG = new Random();

    // external user: don't call this; instead call makeEmpty()
    private Empty() { }

    @NotNull
    @Override
    public ITree<T> insert(@NotNull T value) {
      return new Node<>(value, this, this, RNG.nextInt());
    }

    @NotNull
    @Override
    public String toString() {
      return Tree.Helpers.toStringHelper(this);
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof ITree<?>)) {
        return false;
      }
      ITree<?> otherTree = (ITree<?>) o;

      return otherTree.empty();
    }

    @Override
    public int hashCode() {
      return Tree.Helpers.hashCodeHelper(this);
    }
  }
}
