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

import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * These static utility functions are helpful when converting arbitrary Java objects to strings.
 */
public interface Strings {
  /**
   * This helper function converts any object to a String, with special handling for null
   * and for String. Strings will be "escaped" and surrounded by quotation marks.
   * All the rest just get Object.toString() called on them.
   */
  @NotNull
  @Contract(pure = true)
  static String objectToEscapedString(Object o) {
    if (o instanceof String) {
      // StringEscapeUtils comes to us from the Apache Commons-Lang3 library.
      return "\"" + StringEscapeUtils.escapeJava((String) o) + "\"";
    } else {
      return objectToString(o);
    }
  }

  /**
   * This helper function converts any object to a String, with special handling for null.
   * All the rest just get Object.toString() called on them.
   */
  @NotNull
  @Contract(pure = true)
  static String objectToString(Object o) {
    if (o == null) {
      return "null";
    } else {
      return o.toString();
    }
  }

  /**
   * This helper function converts from a string to a long, with error handling using
   * our Try class.
   */
  @NotNull
  @Contract(pure = true)
  static Try<Long> stringToTryLong(@NotNull String s) {
    return Try.of(() -> Long.decode(s));
  }

  /**
   * This helper function converts from a string to a long, or returns an Option.none
   * if there's a failure. If you want to be able to get the exception, then use
   * {@link #stringToTryLong(String)}.
   */
  @NotNull
  @Contract(pure = true)
  static Option<Long> stringToOptionLong(@NotNull String s) {
    return stringToTryLong(s).toOption();
  }

  /**
   * This helper function converts from a string to an integer, with error handling using
   * our Try class.
   */
  @NotNull
  @Contract(pure = true)
  static Try<Integer> stringToTryInteger(@NotNull String s) {
    return Try.of(() -> Integer.decode(s));
  }

  /**
   * This helper function converts from a string to an integer, or returns an Option.none
   * if there's a failure. If you want to be able to get the exception, then use
   * {@link #stringToTryInteger(String)}.
   */
  @NotNull
  @Contract(pure = true)
  static Option<Integer> stringToOptionInteger(@NotNull String s) {
    return stringToTryInteger(s).toOption();
  }

  /**
   * This helper function converts from a string to a double, with error handling using
   * our Try class.
   */
  @NotNull
  @Contract(pure = true)
  static Try<Double> stringToTryDouble(@NotNull String s) {
    return Try.of(() -> Double.valueOf(s));
  }

  /**
   * This helper function converts from a string to a double, or returns an Option.none
   * if there's a failure. If you want to be able to get the exception, then use
   * {@link #stringToTryDouble(String)}.
   */
  @NotNull
  @Contract(pure = true)
  static Option<Double> stringToOptionDouble(@NotNull String s) {
    return stringToTryDouble(s).toOption();
  }
}
