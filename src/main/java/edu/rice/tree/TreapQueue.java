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

import edu.rice.util.Option;
import edu.rice.util.Pair;
import edu.rice.list.*;
import edu.rice.util.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * A functional FIFO queue implemented using a treap as its backing data structure. If you care about performance,
 * you should probably use {@link ListQueue}.
 */
public interface TreapQueue<T> extends IQueue<T> {
  /**
   * Create an empty queue of the given type parameter.
   */
  @NotNull
  @Contract(pure = true)
  static <T> IQueue<T> makeEmpty() {
    @SuppressWarnings("unchecked")
    IQueue<T> typedEmpty = (IQueue<T>) Empty.SINGLETON;
    return typedEmpty;
  }

  /**
   * Helper function: constructs a queue of the given values, in order (i.e., the first argument will be at the front
   * of the queue).
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  @NotNull
  @Contract(pure = true)
  static <T> IQueue<T> of(@Nullable T... values) {
    IList<T> args = LazyList.fromArray(values);
    return args.foldl(TreapQueue.makeEmpty(), IQueue::insert);
  }

  class NonEmptyQueue<T> implements TreapQueue<T> {
    private final int maxPriority;

    @NotNull
    private final ITree<KeyValue<Integer, T>> treap;

    // We're using Memo's to store the result of oget(), because it's O(log n) to compute and we're not necessarily
    // going to need it. This way, it'll only ever be computed once, on demand, and then the result will be saved.
    // Curiously, this means that the first you iterate a Queue it will be O(n log n) but subsequent times, if
    // you iterate it again, it will be O(n).
    @NotNull
    private final Memo<Option<Pair<T, IQueue<T>>>> ogetMemo;

    // not for public use; instead, use a static helper method or insert into an empty version
    private NonEmptyQueue(int maxPriority, @NotNull ITree<KeyValue<Integer, T>> treap) {
      this.maxPriority = maxPriority;
      this.treap = treap;
      this.ogetMemo = new Memo<>(this::ogetHelper);
    }

    @NotNull
    @Override
    public IQueue<T> insert(@NotNull T t) {
      return new NonEmptyQueue<>(maxPriority + 1, treap.insert(KeyValue.make(maxPriority + 1, t)));
    }

    @NotNull
    @Contract(pure = true)
    private Option<Pair<T, IQueue<T>>> ogetHelper() {
      if (treap.empty()) {
        return Option.none();
      }

      // we could check if the optional that comes from removeMin() is absent, but that *shouldn't* happen
      // if the treap has contents within it, so instead we'll let it blow up with an exception below,
      // which *shouldn't* ever happen because we're already checking for that above.

      return Option.some(treap.removeMin().get().match(
          (minValue, remainingQueue) ->
              new Pair<>(minValue.getValue(),
                  (remainingQueue.empty())
                      ? makeEmpty()
                      : new NonEmptyQueue<>(maxPriority, remainingQueue))));
    }

    @NotNull
    @Override
    public T head() {
      return oget()
          .map(pair -> pair.a)
          .getOrElseThrow(() -> new NoSuchElementException("can't take head() of an empty queue"));
    }

    @NotNull
    @Override
    public String toString() {
      return "Queue(" +
          treap.toLazyList().map(KeyValue::getValue).map(Strings::objectToEscapedString).join(", ") +
          ")";
    }

    @NotNull
    @Override
    public IQueue<T> tail() {
      return oget().get().b;
    }

    @NotNull
    @Override
    public Option<Pair<T, IQueue<T>>> oget() {
      // will call the helper method below, but only once
      return ogetMemo.get();
    }

    @Override
    public int size() {
      return treap.size();
    }
  }

  class Empty<T> implements TreapQueue<T>, IQueue.Empty<T> {
    private static final IQueue<?> SINGLETON = new TreapQueue.Empty<>();

    // external user: don't call this; use makeEmpty()
    private Empty() { }

    @NotNull
    @Override
    public IQueue<T> insert(@NotNull T t) {
      return new NonEmptyQueue<>(0, Treap.of(KeyValue.make(0, t)));
    }

    @NotNull
    @Override
    public String toString() {
      return "";
    }
  }
}
