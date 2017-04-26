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

/**
 * This interface describes a *mutating* priority queue, where the priorities come from the objects
 * being inserted (which are required to be Comparable). The implementation in
 * edu.rice.tree.BinaryHeap is a classical binary heap.
 */
public interface IPriorityQueue<T> {
  /**
   * Inserts a new element into the priority queue. <b>Warning: THIS IS A MUTATING OPERATION.</b>
   */
  void insert(@NotNull T val);

  /**
   * Returns a list of the elements in sorted order. <b>Warning: THIS IS A MUTATING OPERATION.</b> When
   * this is complete, the priority queue will be empty. If you insert to the queue while also
   * iterating on the lazy list, the results are wildly undefined. This method is best used if
   * you've added everything you're ever going to add to the queue and you just want to get its
   * values out in sorted order and then discard the queue. Of course, the resulting list is
   * functional and will have all the usual properties of any functional list.
   */
  @NotNull
  default IList<T> drainToLazyList() {
    return LazyList.ogenerate(() ->
        empty()
            ? Option.none()
            : Option.some(getMin()));
  }

  /**
   * Returns the number of elements in the queue.
   */
  @Contract(pure = true)
  int size();

  /**
   * Returns whether the queue has elements or not.
   */
  @Contract(pure = true)
  default boolean empty() {
    return size() == 0;
  }

  /**
   * Returns the lowest-priority item in the queue, and removes it from the queue. <b>Warning: THIS IS A MUTATING OPERATION.</b>
   */
  @NotNull
  T getMin();
}
