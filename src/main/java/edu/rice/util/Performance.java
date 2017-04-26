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

import java.util.function.Supplier;

/**
 * Helper functions for measuring the performance of various functions.
 */
public interface Performance {
  /**
   * Runs the given runnable, returns the number of nanoseconds it took to run.
   */
  @Contract(pure = true)
  static long nanoBenchmark(@NotNull Runnable runnable) {
    long startTime = System.nanoTime();
    runnable.run();
    long endTime = System.nanoTime();

    return endTime - startTime;
  }

  /**
   * Runs the given lambda, returns a pair of the number of nanoseconds it took to run as well as the result
   * of the lambda.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Pair<Long,T> nanoBenchmarkVal(@NotNull Supplier<T> supplier) {
    long startTime = System.nanoTime();
    T result = supplier.get();
    long endTime = System.nanoTime();

    return new Pair<>(endTime - startTime, result);
  }
}
