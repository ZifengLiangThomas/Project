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
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A Try acts a bit like an {@link Option}, except it's useful for wrapping things in normal Java that might return a
 * value if they succeed, and might throw an exception of whatever sort of if they fail. Consequently, you get something
 * that works much like an Option, but on the failure side you have more information to understand what went wrong.
 *
 * <p>This code is heavily influenced by Javaslang's Try
 * <a href="https://github.com/javaslang/javaslang/blob/master/javaslang/src/main/java/javaslang/control/Try.java">(Github link)</a>
 * which was in turn influenced by Scala's Try
 * <a href="https://github.com/scala/scala/blob/2.12.x/src/library/scala/util/Try.scala">(Github link)</a>.
 */
public interface Try<T> {
  //
  // Data definition:
  //
  // There are three different kinds of Try types, with three corresponding Java classes below.
  //
  // -- Try.Success: analogous to an Option.Some, contains a successful result from running something.
  //
  // -- Try.Failure: analogous to an Option.None, but contains an exception that resulted from running something.
  //
  // -- Try.VoidSuccess: a special case of Try.Success that deals with Try<Void>, which has no value when
  //    the thing being run succeeded, but has an exception when it fails. (We need this to use Try with
  //    lambdas that return nothing. See the ofRunnable() static method.)
  //

  /**
   * If the input is non-null, this returns success(input), otherwise
   * if the input is null, then failure(NullPointerException) is returned.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Try<T> ofNullable(@Nullable T value) {
    if (value == null) {
      @SuppressWarnings("unchecked")
      Try<T> nullFailure = (Try<T>) Failure.NULL_POINTER_EXCEPTION_SINGLETON;
      return nullFailure;
    }

    return success(value);
  }

  /**
   * Given a lambda returning some type T, which might throw any exception, runs
   * the lambda and returns Try.success of the result, unless there's an exception,
   * which will cause Try.failure of that exception to be returned. Also, if null
   * is returned, the result will be a Try.failure with a NullPointerException.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Try<T> of(@NotNull CheckedSupplier<? extends T> supplier) {
    try {
      return ofNullable(supplier.get());
    } catch (Throwable throwable) {
      return failure(throwable);
    }
  }

  /**
   * Given a lambda returning nothing, which might throw any exception, runs
   * the lambda and returns Try.success of Void, unless there's an exception,
   * which will cause Try.failure of that exception to be returned.
   */
  @NotNull
  @Contract(pure = true)
  static Try<Void> ofRunnable(@NotNull CheckedRunnable runnable) {
    try {
      runnable.run();
      return success();
    } catch (Throwable throwable) {
      return failure(throwable);
    }
  }

  /**
   * Given a Try of whatever type, returns a Try&lt;Void&gt;, i.e., discarding
   * a successful value, but keeping a failure's exception around. Useful when
   * the code you were trying had a side-effect you wanted, and you don't want
   * or need your clients to see the return value.
   */
  @NotNull
  @Contract(pure = true)
  default Try<Void> toTryVoid() {
    return match(Try::failure, successVal -> success());
  }

  /**
   * Given a Try.success(), returns the successful value. Given a Try.failure(),
   * returns failureVal instead. Works similar to {@link Option#getOrElse(Object)}.
   */
  @NotNull
  @Contract(pure = true)
  default T getOrElse(@NotNull T failureVal) {
    return match(exception -> failureVal, successVal -> successVal);
  }

  /**
   * Given a Try.success(), returns the successful value. Given a Try.failure(),
   * returns failureVal instead. Works similar to {@link Option#getOrElse(Supplier)}
   */
  @NotNull
  @Contract(pure = true)
  default T getOrElse(@NotNull Supplier<? extends T> failureFunc) {
    return match(exception -> failureFunc.get(), successVal -> successVal);
  }

  /**
   * Given a Try.success(), returns the successful value. Given a Try.failure(),
   * throws the internal exception. We don't know in advance what the type of the
   * exception might be, so we have a broad declaration here of its possible type.
   */
  @NotNull
  @Contract(pure = true)
  default T getOrElseThrow() throws Throwable {
    // Engineering note: we could have tried to make the Try class be parametric
    // on the type of the exception, but this would be a huge pain without having
    // union types in the language (i.e., some way of saying FooException|BarException).
    // Java only allows that sort of declaration at the site where you catch an exception.

    if (isSuccess()) {
      return get();
    } else {
      throw getException();
    }
  }


  /**
   * Given a Try.success(), returns it without doing anything. Given a Try.failure(),
   * returns the alternative instead. Works similar to {@link Option#orElse(Option)}.
   */
  @NotNull
  @Contract(pure = true)
  default Try<T> orElse(@NotNull Try<? extends T> alternative) {
    if (isSuccess()) {
      return this;
    } else {
      return Try.narrow(alternative);
    }
  }

  /**
   * Given a Try.success(), returns it without doing anything. Given a Try.failure(),
   * returns the alternative instead. Works similar to {@link Option#orElse(Supplier)}.
   */
  @NotNull
  @Contract(pure = true)
  default Try<T> orElse(@NotNull Supplier<? extends Try<? extends T>> alternativeFunc) {
    if (isSuccess()) {
      return this;
    } else {
      return Try.narrow(alternativeFunc.get());
    }
  }

  /**
   * Sometimes, you want to generate a log when you got an error. This log method
   * calls {@link Log#e(String, Object)} with the given tag, and passes the exception along to your lambda
   * for conversion to a custom log string. The exception stacktrace is not logged.
   * (If you want that, use {@link #logIfFailureVerbose(String, Function)})
   *
   * <p>The original Try is returned, no matter what, making it easy to pipeline
   * calls to the log method with whatever else you're doing.
   */
  @NotNull
  @Contract
  default Try<T> logIfFailure(@NotNull String tag, @NotNull Function<Throwable, String> func) {
    if (isFailure()) {
      Log.e(tag, func.apply(getException()));
    }

    return this;
  }

  /**
   * Sometimes, you want to generate a log when you got an error. This log method
   * calls {@link Log#e(String, Object, Throwable)} with the given tag, and passes the exception along to your lambda
   * for conversion to a custom log string. A stacktrace of the exception is also logged.
   *
   * <p>The original Try is returned, no matter what, making it easy to pipeline
   * calls to the log method with whatever else you're doing.
   */
  @NotNull
  @Contract
  default Try<T> logIfFailureVerbose(@NotNull String tag, @NotNull Function<Throwable, String> func) {
    if (isFailure()) {
      Throwable th = getException();
      Log.e(tag, func.apply(th), th);
    }

    return this;
  }

  /**
   * Creates an option holding the value within. Note that if the given value is null,
   * then the result will be a Try.failure.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Try<T> success(@NotNull T value) {
    return new Success<>(value);
  }

  /**
   * If you want a Try&lt;Void&gt;, representing a successful result with no return value, then here you go.
   * Note that calling get() or match() on this will cause an exception to be thrown because there's no
   * value within for the success case.
   */
  @NotNull
  @Contract(pure = true)
  static Try<Void> success() {
    return VoidSuccess.SINGLETON;
  }

  /**
   * Creates an empty option.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Try<T> failure(@NotNull Throwable exception) {
    return new Failure<>(exception);
  }

  /**
   * Converts an Option to a Try, wherein Option.some becomes Try.success
   * and Option.none becomes Try.failure with the given exception.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Try<T> fromOption(@NotNull Option<T> option, @NotNull Supplier<Throwable> exceptionSupplier) {
    return option.match(() -> failure(exceptionSupplier.get()), Try::success);
  }

  /**
   * Converts this try to an {@link Option}. If it's a Try.success(), an Option.some() is returned.
   * If it's a Try.failure(), an Option.none() is returned. If the Try was a Try&lt;Void&gt;
   * this will throw an exception because there's no equivalent Option type.
   */
  @NotNull
  @Contract(pure = true)
  default Option<T> toOption() {
    return match(no -> Option.none(), Option::some);
  }

  /**
   * Returns whether the Try succeeded (true) or whether it failed (false).
   */
  @Contract(pure = true)
  boolean isSuccess();

  /**
   * Returns whether the Try failed (true) or whether it succeeded (false).
   */
  @Contract(pure = true)
  default boolean isFailure() {
    return !isSuccess();
  }

  /**
   * For a Try.success, this returns the internal value. For a Try.failure or a Try&lt;Void&gt;, this throws an exception.
   */
  @NotNull
  @Contract(pure = true)
  T get();

  /**
   * For a Try.success, this throws an exception. For a Try.failure, this returns the internal exception value.
   * IntelliJ may generate a warning when you call this, saying "hey, you didn't rethrow the exception." You
   * can suppress this by suppressing the "ThrowableInstanceNeverThrown" warning.
   */
  @NotNull
  @Contract(pure = true)
  @SuppressWarnings("ThrowableInstanceNeverThrown") // IntelliJ freaks out if you don't rethrow the exception
  Throwable getException();

  /**
   * Takes two lambdas: one to call if it's a Try.success, and the the other to call if
   * it's Try.failure. The former is given the value and the latter is given the exception.
   */
  @NotNull
  @Contract(pure = true)
  default <Q> Q match(@NotNull Function<Throwable, ? extends Q> failureFunc,
                      @NotNull Function<? super T, ? extends Q> successFunc) {
    if (isSuccess()) {
      return successFunc.apply(get());
    } else {
      return failureFunc.apply(getException());
    }
  }

  /**
   * For Try.success, run the predicate on its contents. If it's true, then you
   * get back the original Try.success. If the predicate returns false, or if it's
   * a Try.failure, then you get back a Try.failure, with the new exception as provided
   * by the lambda. Analogous to {@link Option#filter(Predicate)}.
   */
  @NotNull
  @Contract(pure = true)
  default Try<T> filter(@NotNull Predicate<? super T> predicate, @NotNull Supplier<Throwable> exceptionSupplier) {
    return flatmap(
        val -> (predicate.test(val))
            ? this
            : failure(exceptionSupplier.get()));
  }

  /**
   * Returns a new Try, with the function applied to the internal value, if it's a Try.success.
   * Otherwise, if it's a Try.failure, the same result is returned. If the function returns null,
   * a Try.failure() will be returned. Analogous to {@link Option#map(Function)}.
   */
  @NotNull
  @Contract(pure = true)
  default <Q> Try<Q> map(@NotNull CheckedFunction<? super T, ? extends Q> func) {
    return flatmap(val -> ofNullable(func.apply(val)));
  }

  /**
   * Returns a new Try, with the function applied to the internal value, if it's a Try.success.
   * If if's a Try.failure, then the original Try is returned. Analogous to {@link Option#flatmap(Function)}.
   */
  @NotNull
  @Contract(pure = true)
  default <Q> Try<Q> flatmap(@NotNull CheckedFunction<? super T, ? extends Try<? extends Q>> func) {
    return match(
        Try::failure, successVal -> {
          try {
            return narrow(func.apply(successVal));
          } catch (Throwable throwable) {
            return failure(throwable);
          }
        });
  }

  /**
   * Allows you to narrow a type parameter.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Try<T> narrow(@NotNull Try<? extends T> wideTry) {
    @SuppressWarnings("unchecked")
    Try<T> narrowTry = (Try<T>) wideTry;
    return narrowTry;
  }

  /**
   * Runs the lambda, returns the original Try without changes. Allows for something analogous to try/finally blocks.
   */
  @NotNull
  @Contract(pure = true)
  default Try<T> andThen(CheckedRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable throwable) {
      // At this point, we're in a curious situation. If the original try succeeded, but now the runnable
      // fails, should we return the original value, a success, or should we return the new exception,
      // a failure? In the case of Java, consider the case where we return a value in a try block, while
      // we've got a finally block:
      //
      //      try {
      //        DoSomethingThatMightThrowAnException();
      //        return foo;
      //      } finally {
      //        throw new BarException();
      //      }
      //
      // The finally block runs before the value is actually returned, and it overrides any exception that might
      // have been thrown in the try block. The exception from the finally block wins. For andThen(), the runnable
      // is analogous to the finally block, so if it blew up, then that's what we're returning.

      return failure(throwable);
    }

    return this;
  }

  /**
   * For a Try.success, runs the given lambda with the successful value. For a Try.failure, returns
   * the original Try.failure without running the lambda. You might use this to chain together a sequence
   * of actions where you want each thing to happen only if the previous succeeded.
   */
  @NotNull
  @Contract(pure = true)
  default Try<T> andThen(@NotNull Consumer<? super T> consumer) {
    return andThenTry(consumer::accept); // coerce the Consumer to a CheckedConsumer
  }

  /**
   * For a Try.success, runs the given lambda with the successful value. You might use this to chain together
   * a sequence of actions where you want each thing to happen only if the previous succeeded.
   */
  @NotNull
  @Contract(pure = true)
  default Try<T> andThenTry(@NotNull CheckedConsumer<? super T> consumer) {
    try {
      if (isSuccess()) {
        consumer.accept(get());
      }
    } catch (Throwable throwable) {
      return failure(throwable);
    }

    return this;
  }

  /**
   * Special case: we'll have a singleton instance of Try&lt;Void&gt; for the success case. The essential difference
   * with the Success class is that there's no actual value to be returned here, since we're modeling that a lambda
   * with no return type ran successfully (i.e., had no exceptions).
   */
  class VoidSuccess implements Try<Void> {
    private VoidSuccess() {
    }

    private static final Try<Void> SINGLETON = new VoidSuccess();

    @Override
    public boolean isSuccess() {
      return true;
    }

    @NotNull
    @Override
    public Void get() {
      throw new NoSuchElementException("can't get() from a Try<Void>");
    }

    @NotNull
    @Override
    public Throwable getException() {
      throw new NoSuchElementException("can't get failure exception from a Try.Success");
    }

    @NotNull
    @Override
    public String toString() {
      return "Try.Success(Void)";
    }

    @Override
    public boolean equals(Object o) {
      return !(o == null | !(o instanceof VoidSuccess));
    }

    @Override
    public int hashCode() {
      return 1;
    }
  }

  /**
   * Standard case for a Try that succeeded. Stores the successful result value.
   */
  class Success<T> implements Try<T> {
    @NotNull
    private final T val;

    // non-public constructor
    Success(@NotNull T val) {
      this.val = val;
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @NotNull
    @Override
    public T get() {
      return val;
    }

    @NotNull
    @Override
    public Throwable getException() {
      throw new NoSuchElementException("can't get failure exception from a Try.Success");
    }

    @NotNull
    @Override
    public String toString() {
      return "Try.Success(" + Strings.objectToEscapedString(val) + ")";
    }

    @Override
    public boolean equals(Object o) {
      if (o == null | !(o instanceof Success)) {
        return false;
      }

      Success<?> other = (Success<?>) o;
      return val.equals(other.val);
    }

    @Override
    public int hashCode() {
      return val.hashCode();
    }
  }

  /**
   * Standard case for a Try that failed. Stores the exception.
   */
  class Failure<T> implements Try<T> {
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    private static final Failure<?> NULL_POINTER_EXCEPTION_SINGLETON =
        new Failure<>(new NullPointerException("Try.Failure: non-null values are required"));

    @NotNull
    private final Throwable exception;

    // non-public constructor
    Failure(@NotNull Throwable exception) {
      this.exception = exception;
    }

    @Override
    public boolean isSuccess() {
      return false;
    }

    @NotNull
    @Override
    public T get() {
      throw new NoSuchElementException("can't get success value from a Try.Failure");
    }

    @NotNull
    @Override
    public Throwable getException() {
      return exception;
    }

    @NotNull
    @Override
    public String toString() {
      return "Try.Failure(" + Strings.objectToEscapedString(exception) + ")";
    }

    @Override
    public boolean equals(Object o) {
      if (o == null | !(o instanceof Failure)) {
        return false;
      }

      Failure<?> other = (Failure<?>) o;
      return exception.equals(other.exception);
    }

    @Override
    public int hashCode() {
      return exception.hashCode();
    }
  }

  //
  // We need a bunch of functional interfaces that are basically the same as the normal functional interfaces
  // in java.util.function, except that they explicitly declare that they "throw Throwable". This means that
  // you can declare a lambda with a throws clause inside, for any exception type, and it will still fit just
  // fine with our Try class.
  //
  // Otherwise, you can only throw subtypes of RuntimeException. Because Java is annoying. At least you'll
  // never see these interfaces named in code outside of this file. They're just here for the Java8 type
  // inference engine to have something to hang onto.
  //

  /**
   * A {@linkplain java.util.function.Consumer} which may throw any exception.
   *
   * @param <T> the type of input expected by the Consumer
   */
  @FunctionalInterface
  interface CheckedConsumer<T> {

    /**
     * Accept an input.
     *
     * @throws Throwable if an error occurs
     */
    void accept(T r) throws Throwable;
  }

  /**
   * A {@linkplain java.util.function.Supplier} which may throw any exception.
   *
   * @param <T> the type of results supplied by this supplier
   */
  @FunctionalInterface
  interface CheckedSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws Throwable if an error occurs
     */
    T get() throws Throwable;
  }

  /**
   * A {@linkplain java.util.function.Function} which may throw any exception.
   *
   * @param <R> the type of results supplied by this supplier
   */
  @FunctionalInterface
  interface CheckedFunction<T,R> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws Throwable if an error occurs
     */
    R apply(@NotNull T val) throws Throwable;
  }

  /**
   * A {@linkplain java.lang.Runnable} which may throw any exception.
   */
  @FunctionalInterface
  interface CheckedRunnable {

    /**
     * Code to run.
     * @throws Throwable if an error occurs
     */
    void run() throws Throwable;
  }

}
