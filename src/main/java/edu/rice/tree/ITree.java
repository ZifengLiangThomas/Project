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

import edu.rice.util.*;
import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ITree<T extends Comparable<? super T>> {
  /**
   * Returns the number of elements in the tree.
   */
  @Contract(pure = true)
  default int size() {
    return match(
      emptyTree -> 0,
      (elem, leftTree, rightTree) -> leftTree.size()  + rightTree.size() + 1);
  }

  /**
   * Returns the value at the root of the tree.
   */
  @NotNull
  @Contract(pure = true)
  T getValue();

  /**
   * Returns the left subtree.
   */
  @NotNull
  @Contract(pure = true)
  ITree<T> getLeft();

  /**
   * Returns the right subtree.
   */
  @NotNull
  @Contract(pure = true)
  ITree<T> getRight();

  /**
   * Returns a new tree equal to the current tree with the new element inserted into it. If the new
   * value is already present, it replaces the old value.
   */
  @NotNull
  @Contract(pure = true)
  ITree<T> insert(@NotNull T newbie);

  /**
   * General-purpose structural pattern matching with deconstruction on a tree.
   *
   * @param emptyFunc
   *     called if the node is empty
   * @param nonEmptyFunc
   *     called if the node has a value within
   * @param <Q>
   *     the return type of either emptyFunc or nonEmptyFunc
   * @return returns the value of whichever function matches
   */
  @NotNull
  @Contract(pure = true)
  default <Q> Q match(@NotNull Function<? super ITree<T>, ? extends Q> emptyFunc,
                      @NotNull TriFunction<? super T, ? super ITree<T>, ? super ITree<T>, ? extends Q> nonEmptyFunc) {
    if (empty()) {
      return emptyFunc.apply(this);
    } else {
      return nonEmptyFunc.apply(this.getValue(), this.getLeft(), this.getRight());
    }
  }

  /**
   * General-purpose structural pattern matching with deconstruction on a tree, except
   * with no return values.
   *
   * @param emptyFunc
   *     called if the node is empty
   * @param nonEmptyFunc
   *     called if the node has a value within
   */
  default void consume(@NotNull Consumer<? super ITree<T>> emptyFunc,
                       @NotNull TriConsumer<? super T, ? super ITree<T>, ? super ITree<T>> nonEmptyFunc) {
    if (empty()) {
      emptyFunc.accept(this);
    } else {
      nonEmptyFunc.accept(this.getValue(), this.getLeft(), this.getRight());
    }
  }

  /**
   * Visits the tree, in-order, running the consumer on each element.
   */
  default void inorder(@NotNull Consumer<T> consumer) {
    consume(
        emptyTree -> { },
        (val, left, right) -> {
          left.inorder(consumer);
          consumer.accept(val);
          right.inorder(consumer);
        });
  }

  /**
   * Returns whether or not the current tree is empty.
   */
  @Contract(pure = true)
  boolean empty();

  /**
   * Finds something that's equal to the query and returns it, if present.
   */
  @NotNull
  @Contract(pure = true)
  default Option<T> find(@NotNull T query) {
    return match(
        emptyTree -> Option.none(),

        (elem, leftTree, rightTree) -> {
          int comparison = query.compareTo(elem);
          if (comparison < 0) {
            return leftTree.find(query);
          }
          if (comparison > 0) {
            return rightTree.find(query);
          }
          return Option.some(elem);
        });
  }

  /**
   * Returns a new tree with all elements greater than the floor value, either inclusive or
   * exclusive.
   */
  @NotNull
  @Contract(pure = true)
  ITree<T> greaterThan(@NotNull T floor, boolean inclusive);

  /**
   * Returns a new tree with all elements lesser than the ceiling value, either inclusive or
   * exclusive.
   */
  @NotNull
  @Contract(pure = true)
  ITree<T> lessThan(@NotNull T ceiling, boolean inclusive);

  /**
   * Returns a new tree equivalent to the original, but absent the value if it's present.
   */
  @NotNull
  @Contract(pure = true)
  ITree<T> remove(@NotNull T value);

  /**
   * Returns a new tree equivalent to the original, without its minimum value; also returns the
   * minimum value. The result is optional because the tree might be empty.
   */
  @NotNull
  @Contract(pure = true)
  Option<Pair<T, ITree<T>>> removeMin();

  /**
   * Returns the priority, if it's a treap, otherwise max-int (grumble: this seems like an ugly
   * thing to have in the interface, but the alternative is to do a bunch of typecasting inside
   * Treap, along with the remote possibility of bug-induced runtime crashes).
   */
  @Contract(pure = true)
  default int getPriority() {
    return Integer.MAX_VALUE;
  }

  /**
   * Validates that the tree is well-formed, returns true if it's all good (generally for testing
   * purposes).
   */
  @Contract(pure = true)
  boolean valid();

  /**
   * Returns the maximum depth of the tree.
   */
  @Contract(pure = true)
  default int maxDepth() {
    return match(
        emptyTree -> 0,
        (elem, leftTree, rightTree) -> Integer.max(leftTree.maxDepth(), rightTree.maxDepth()) + 1);
  }

  /**
   * Inserts all the elements in the list into the tree, returning a new tree.
   */
  @NotNull
  @Contract(pure = true)
  default ITree<T> insertList(@NotNull IList<? extends T> values) {
    // Note the clever use of foldl here, which maps (tree,val)->tree, accumulating all of the
    // values in the input list, one by one, starting from the current state of the list (this).
    // Also, since foldl() has been optimized to avoid tail calls, this method will operate
    // efficiently, even if the list is really large.
    return values.foldl(this, ITree<T>::insert);
  }

  /**
   * Removes all the elements in the list from the tree, returning a new tree.
   */
  @NotNull
  @Contract(pure = true)
  default ITree<T> removeList(@NotNull IList<? extends T> values) {
    return values.foldl(this, ITree<T>::remove);
  }

  /**
   * Lazily constructs a list, in-order, from the underlying tree. If you only want a few elements
   * from the tree, this will be significantly faster than the eager version.
   * @see ITree#toList()
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> toLazyList() {
    return match(
        emptyTree -> LazyList.makeEmpty(),
        (elem, leftTree, rightTree) ->
            LazyList.lazyConcat(leftTree.toLazyList(), () -> LazyList.make(elem, rightTree::toLazyList)));
  }

  /**
   * Eagerly constructs a list, in-order, from the underlying tree.
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> toList() {
    return match(
        emptyTree -> List.makeEmpty(),
        (elem, leftTree, rightTree) -> leftTree.toList().concat(rightTree.toList().add(elem)));
  }

  /**
   * Empty trees have a lot of code in common, so we can put that all here.
   */
  interface Empty<T extends Comparable<? super T>> extends ITree<T> {
    @Override
    default void inorder(@NotNull Consumer<T> consumer) { }

    @Override
    @Contract(pure = true)
    default int size() {
      return 0;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default T getValue() {
      throw new NoSuchElementException("getValue() not defined on an empty tree");
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default ITree<T> getLeft() {
      return this;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default ITree<T> getRight() {
      return this;
    }

    @Override
    @Contract(pure = true)
    default boolean empty() {
      return true;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default ITree<T> greaterThan(@NotNull T floor, boolean inclusive) {
      return this;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default ITree<T> lessThan(@NotNull T ceiling, boolean inclusive) {
      return this;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    default ITree<T> remove(@NotNull T value) {
      return this;
    }

    @Override
    @Contract(pure = true)
    default @NotNull Option<Pair<T, ITree<T>>> removeMin() {
      return Option.none();
    }

    @Override
    @Contract(pure = true)
    default boolean valid() {
      return true;
    }
  }
}
