/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.dynamic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * MemoizedFunction for use in dynamic programming. You build one of these with a lambda for the function you're
 * trying to evaluate, and it will store all pairs of input/output, so the lambda never get evaluated twice for
 * the same input.
 */
public interface MemoizedFunction<InputT, OutputT> extends Function<InputT, OutputT> {
  //
  // Engineering notes: MemoizedFunction extends Function, so the resulting instance types can
  // be used anywhere that a lambda might be expected. Also, we're internally using java.util.HashMap.
  // In week4, we'll introduce an alternative, "TreapMap". The note below will make more sense then, but
  // anyway: We're using Java's HashMap for two reasons.
  //
  // One: fundamentally we're mutating the map and we never need an old version. There's no benefit to using a functional map.
  //
  // Two: TreapMap requires its keys to be Comparable, which would be unnecessarily constraining. HeapMap only requires
  // its keys to override equals() and hashCode(), which are part of java.lang.Object. This means we can define MemoizedFunction
  // without any type constraints on InputT.
  //

  /**
   * Given a lambda that maps from inputs to outputs, returns a Memoized version of that lambda. If you need
   * recursion, use {@link #makeRecursive(BiFunction)} instead.
   */
  @NotNull
  @Contract(pure = true)
  static <InputT, OutputT> MemoizedFunction<InputT, OutputT> make(@NotNull Function<InputT, OutputT> function) {
    return new Standard<>(function);
  }

  /**
   * If you want to memoize a recursive function, then your function needs something to call besides itself.
   * So, rather than using a lambda that maps input to output, here we expect a lambda that takes two arguments,
   * the first of which will be something you can call when you want to be recursive. The second argument is
   * the usual argument to your function.
   *
   * <p>As an example, say you were using this to implement a memoized Fibonacci function, you could define
   * that function like so:
   *
   * <pre>
   * <code>
   * Function&lt;Long, Long&gt; memoFibonacci =
   *     MemoizedFunction.makeRecursive((self, n) -&gt; {
   *
   *   if (n &lt; 2) {
   *     return 1L;
   *   } else {
   *     return self.apply(n - 1) + self.apply(n - 2);
   *   }
   * });
   * </code>
   * </pre>
   *
   * <p>Calls to <code>memoFibonacci</code> will run in linear time because the underlying recursive calls are
   * memoized.
   */
  @NotNull
  @Contract(pure = true)
  static <InputT, OutputT> MemoizedFunction<InputT, OutputT>
      makeRecursive(@NotNull BiFunction<Function<InputT, OutputT>, InputT, OutputT> function) {

    return new Recursive<>(function);
  }

  class Standard<InputT, OutputT> implements MemoizedFunction<InputT, OutputT> {
    @NotNull
    private final Function<InputT, OutputT> function;
    @NotNull
    private Map<InputT, OutputT> map = new HashMap<>(); // yes, a mutating hashmap!

    private Standard(@NotNull Function<InputT, OutputT> function) {
      this.function = function;
    }

    @NotNull
    @Override
    public OutputT apply(@NotNull InputT input) {
      return map.computeIfAbsent(input, function); // basically does everything we need, a standard feature of Java8
    }
  }

  class Recursive<InputT, OutputT> implements MemoizedFunction<InputT, OutputT> {
    @NotNull
    private final BiFunction<Function<InputT, OutputT>, InputT, OutputT> function;
    @NotNull
    private Map<InputT, OutputT> map = new HashMap<>(); // yes, a mutating hashmap!

    private Recursive(@NotNull BiFunction<Function<InputT, OutputT>, InputT, OutputT> function) {
      this.function = function;
    }

    @NotNull
    @Override
    public OutputT apply(@NotNull InputT input) {
      // This is a little more complicated than the "Standard" version, above, because we need to pass along the "recursive"
      // function to the lambda. In this case, the recursive function is simply "this" (i.e., this::apply).

      return map.computeIfAbsent(input, funcIn -> function.apply(this, funcIn));
    }
  }
}
