/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static edu.rice.util.Strings.objectToEscapedString;

/**
 * General-purpose functional tuple, useful for returning more than one thing at a time. The
 * variables a and b contain the elements, which you can read directly, but they're immutable.
 */
public class Pair<A, B> {
  @NotNull
  public final A a;
  @NotNull
  public final B b;

  public Pair(@NotNull A a, @NotNull B b) {
    this.a = a;
    this.b = b;
  }

  /**
   * Perhaps overkill, but helps make your code cleaner. This function lets you
   * deconstruct the pair with a lambda taking two parameters -- the two values
   * in the Pair -- and return whatever you want.
   */
  @NotNull
  @Contract(pure = true)
  public <T> T match(@NotNull BiFunction<A,B,T> func) {
    return func.apply(a,b);
  }

  /**
   * Perhaps overkill, but helps make your code cleaner. This function lets you
   * deconstruct the pair with a lambda taking two parameters -- the two values
   * in the Pair -- and return nothing.
   */
  public void consume(@NotNull BiConsumer<A,B> func) {
    func.accept(a,b);
  }

  @Override
  @Contract(pure = true)
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Pair)) {
      return false;
    }

    Pair<?, ?> pair = (Pair<?, ?>) o;

    return a.equals(pair.a) && b.equals(pair.b);

  }

  @Override
  @Contract(pure = true)
  public int hashCode() {
    return a.hashCode() * 31 + b.hashCode();
  }

  @Override
  @Contract(pure = true)
  public String toString() {
    return String.format("(%s, %s)", objectToEscapedString(a), objectToEscapedString(b));
  }
}
