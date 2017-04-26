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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Sets and class inheritance play together in a funny way, so it's worth having a test for it. Otherwise,
 * we test sets pretty thoroughly as they're used inside TreapMap.
 */
public class ISetTest {

  public static class Base<T extends Comparable<T>> implements Comparable<Base<T>> {
    @NotNull
    public final T val;

    public Base(@NotNull T val) {
      this.val = val;
    }

    @Override
    public int compareTo(@NotNull Base<T> o) {
      return val.compareTo(o.val);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Base<?>)) {
        return false;
      }
      Base<?> other = (Base<?>) o;

      return val.equals(other.val);
    }

    @Override
    public int hashCode() {
      return val.hashCode();
    }
  }

  //
  // Note: we're implementing Comparable<Base<T>> rather than Comparable<Extender<T>>
  // because we want our "Extender" to be able to be drop-in compatible with a "Base".
  // This is basically what the Liskov Substitution Principle requires of us.
  //
  public static class Extender<T extends Comparable<T>>
      extends Base<T>
      implements Comparable<Base<T>> {

    public Extender(T val) {
      super(val);
    }
  }

  @Test
  public void testNarrow() throws Exception {
    ISet<Base<Integer>> baseSet = TreapSet.of(new Base<>(1), new Base<>(3), new Base<>(5), new Extender<>(7));
    ISet<Extender<Integer>> extenderSet = TreapSet.of(new Extender<>(9));

    ISet<Base<Integer>> moreBase = baseSet.union(ISet.narrow(extenderSet), (a, b) -> a);

    ISet<Base<Integer>> expectedMerger1 =
        TreapSet.of(new Base<>(1), new Base<>(3), new Base<>(5), new Extender<>(7), new Extender<>(9));

    ISet<Base<Integer>> expectedMerger2 =
        TreapSet.of(new Base<>(1), new Base<>(3), new Base<>(5), new Base<>(7), new Base<>(9));

    assertEquals(expectedMerger1, moreBase);
    assertEquals(expectedMerger2, moreBase);
  }
}