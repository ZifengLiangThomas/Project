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

import edu.rice.list.IList;
import edu.rice.list.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This interface handles the Comp215 variant of java.util.Optional. Our version is more flexible than Oracle's.
 * For an extended rant on this problem, see 
 * <a href="https://developer.atlassian.com/blog/2015/08/optional-broken/">Atlassian's discussion of Optional</a>.
 * This code we're using here is heavily influenced by 
 * <a href="https://github.com/javaslang/javaslang/blob/master/javaslang/src/main/java/javaslang/control/Option.java">Javaslang's Option</a>.
 */
public interface Option<T> {
  //
  // Data definition:
  //
  // There are two kinds of options: Some and None, which have their corresponding classes below.
  // Some classes wrap a value, which can be fetched later. None has no corresponding value.
  //

  /**
   * Creates an option holding the value within.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Option<T> some(@NotNull T value) {
    return new Some<>(value);
  }

  /**
   * If the input is non-null, this returns some(input), otherwise
   * if the input is null, then None is returned.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Option<T> ofNullable(@Nullable T value) {
    return (value == null)
        ? none()
        : some(value);
  }

  /**
   * Creates an empty option.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Option<T> none() {
    @SuppressWarnings("unchecked")
    Option<T> typedNone = (Option<T>) None.SINGLETON;
    return typedNone;
  }

  /**
   * Creates an empty optional, ignores its argument. (Useful in places where you want to mention
   * Optional::none as a function taking one input and ignoring it. Also allows type inference to
   * flow from the input type to the output type, so fewer places where you need to decorate your
   * code with explicit type parameters.)
   */
  @NotNull
  @Contract(pure = true)
  @SuppressWarnings("unused")
  static <T> Option<T> none(T ignored) {
    return none();
  }

  /**
   * Converts a java.util.Optional to our own Option.
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @NotNull
  @Contract(pure = true)
  static <T> Option<T> fromOptional(@NotNull Optional<T> optional) {
    return optional.map(Option::some).orElse(Option.none());
  }

  /**
   * Converts an Option to an IList of zero or one elements.
   */
  @NotNull
  @Contract(pure = true)
  default IList<T> toList() {
    return match(List::makeEmpty, List::of);
  }

  /**
   * Returns whether there's something present here (i.e., it's an Option.Some) or whether there's
   * nothing here (i.e., it's an Option.None).
   */
  @Contract(pure = true)
  boolean isSome();

  /**
   * The opposite of {@link #isSome()}: returns true if there's Option.None.
   */
  @Contract(pure = true)
  default boolean isNone() {
    return !isSome();
  }

  /**
   * For an Option.Some, this returns the internal value. For an Option.None, this throws an exception.
   */
  @NotNull
  @Contract(pure = true)
  T get();

  /**
   * Takes two lambdas: one to call if it's an Option.None, and the the other to call if
   * it's an Option.Some. The latter is given the internal value as a parameter.
   */
  @NotNull
  @Contract(pure = true)
  default <Q> Q match(@NotNull Supplier<? extends Q> noneFunc, @NotNull Function<? super T, ? extends Q> someFunc) {
    if (isSome()) {
      return someFunc.apply(get());
    } else {
      return noneFunc.get();
    }
  }

  /**
   * Returns the internal value, if it's present, otherwise it returns the alternative.
   */
  @NotNull
  @Contract(pure = true)
  default T getOrElse(@NotNull T alternative) {
    return match(() -> alternative, val -> val);
  }

  /**
   * Returns the internal value, if it's present, otherwise it returns the alternative.
   */
  @NotNull
  @Contract(pure = true)
  default T getOrElse(@NotNull Supplier<? extends T> alternativeFunc) {
    return match(alternativeFunc::get, val -> val);
  }

  /**
   * This is a variant on {@link #getOrElse(Object)}. If we're starting with an Option.Some, then this
   * returns the original Option value. If we're starting with an Option.None, then the
   * <code>alternative</code> is returned. Very useful for chaining together options.
   */
  @NotNull
  @Contract(pure = true)
  default Option<T> orElse(@NotNull Option<? extends T> alternative) {
    if (isSome()) {
      return this;
    } else {
      return Option.narrow(alternative);
    }
  }

  /**
   * This is a variant on {@link #getOrElse(Object)}. If we're starting with an Option.Some, then this
   * returns the original Option value. If we're starting with an Option.None, then the
   * <code>alternativeFunc</code> is invoked and its result value returned.
   */
  @NotNull
  @Contract(pure = true)
  default Option<T> orElse(@NotNull Supplier<? extends Option<? extends T>> alternativeFunc) {
    if (isSome()) {
      return this;
    } else {
      return Option.narrow(alternativeFunc.get());
    }
  }

  /**
   * Returns the internal value, if it's present, otherwise it throws the exception
   * supplied by the lambda.
   */
  @NotNull
  @Contract(pure = true)
  default T getOrElseThrow(@NotNull Supplier<? extends RuntimeException> exceptionSupplier) {
    return match(
        () -> {
          throw exceptionSupplier.get();
        },
        val -> val);
  }

  /**
   * For an Option.Some, run the predicate on its contents. If it's true, then you
   * get back the original Option.Some. If the predicate returns false, or if it's
   * an Option.None, then you get back Option.None.
   *
   * <p>Analogous to filtering a list with zero or one elements.
   * @see IList#filter(Predicate)
   */
  @NotNull
  @Contract(pure = true)
  default Option<T> filter(@NotNull Predicate<? super T> predicate) {
    return flatmap(
        val -> (predicate.test(val))
            ? this
            : Option.none());
  }

  /**
   * Returns a new Option, with the function applied to the internal value, if it's present.
   *
   * <p>Analogous to mapping over a list with zero or one elements.
   * @see IList#map(Function)
   */
  @NotNull
  @Contract(pure = true)
  default <R> Option<R> map(@NotNull Function<? super T, ? extends R> func) {
    return flatmap(val -> some(func.apply(val)));
  }

  /**
   * Returns a new Option, with the function applied to the internal value, if it's present.
   * This function is expected to return an Option.
   *
   * <p>Analogous to flatmapping over a list with zero or one elements.
   * @see IList#flatmap(Function)
   */
  @NotNull
  @Contract(pure = true)
  default <R> Option<R> flatmap(@NotNull Function<? super T, ? extends Option<? extends R>> func) {
    return match(Option::none, val -> narrow(func.apply(val)));
  }

  /**
   * Allows you to narrow a type parameter.
   */
  @NotNull
  @Contract(pure = true)
  static <T> Option<T> narrow(@NotNull Option<? extends T> option) {
    @SuppressWarnings("unchecked")
    Option<T> castOption = (Option<T>) option;
    return castOption;
  }

  /**
   * Sometimes, you want to generate a log when you have an Option.none, to indicate
   * an error of whatever sort, and you want to do nothing otherwise. This log method
   * calls Log.e with the given tag, and then evaluates the lambda for the logging
   * string. If it's an Option.some, this is a no-op.
   *
   * <p>The original Option is returned, no matter what, making it easy to pipeline
   * calls to the log method with whatever else you're doing.
   * @see Log#e(String, Object)
   */
  @NotNull
  @Contract
  default Option<T> logIfNone(@NotNull String tag, @NotNull Supplier<String> func) {
    if (isNone()) {
      Log.e(tag, func.get());
    }

    return this;
  }

  class Some<T> implements Option<T> {
    @NotNull
    private final T contents;

    // don't call this externally
    private Some(@NotNull T contents) {
      this.contents = contents;
    }

    @Override
    @Contract(pure = true)
    public boolean isSome() {
      return true;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public T get() {
      return contents;
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object o) {
      if (o == null || !(o instanceof Some<?>)) {
        return false;
      }

      Some<?> otherSome = (Some<?>) o;

      return contents.equals(otherSome.contents);
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public String toString() {
      return "Option.Some(" + Strings.objectToEscapedString(contents) + ")";
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
      return 0;
    }
  }

  class None<T> implements Option<T> {
    private static final @NotNull Option<?> SINGLETON = new None<>();

    // don't call this externally
    private None() { }

    @Override
    @Contract(pure = true)
    public boolean isSome() {
      return false;
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public T get() {
      throw new NoSuchElementException("can't get() from Option.None");
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object o) {
      return o != null && o instanceof None<?>;
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public String toString() {
      return "Option.None()";
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
      return 0;
    }
  }
}
