/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.sexpr;

import edu.rice.list.*;
import edu.rice.util.Option;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.*;

/**
 * S-Expression Value. The way we're doing it here is a bit like:
 * <br>Value ::= Word | SExpr
 * <br>SExpr ::= ( list-of-Values )
 *
 * <p>That second line isn't exactly a proper statement in a BNF grammar, but it's very convenient.
 * You'll see the benefit when interacting with s-expression values, where you can trivially convert
 * any s-expression to an IList of Values.
 */
public interface Value {
  /**
   * General-purpose structural pattern matching on an s-expression value, with one lambda per concrete
   * type of the Value. Typical usage:
   * <pre><code>
   *     Value val = ... ;
   *     Option&lt;Whatever&gt; oresult = val.match(
   *         word -&gt; Option.some(word.something()),
   *         sexpr -&gt; Option.none());
   * </code></pre>
   * If you are just wondering what the concrete type is and you don't need this fancy structural pattern matcher
   * with all the lambdas, then {@link #getType()} will tell you what you need.
   */
  @NotNull
  @Contract(pure = true)
  default <Q> Q match(@NotNull Function<Word, Q> wordF,
                      @NotNull Function<Sexpr, Q> sexprF) {

    // Engineering note: Some purists would say that we shouldn't do this switch statement here, but should instead
    // dispatch to a one-line method in each of the classes, in much the same way we do for getType(). If we did that,
    // then each class would need this huge match method (repeated code... yuck!). The counter-argument is that we
    // have to do all the casting here (asSexpr(), etc.) which wouldn't be necessary if we did it in
    // the concrete classes. Ultimately, it's cleaner to have it all here, in one place, ("don't repeat yourself!")
    // which trumps these concerns. Note that clients of the match() interface don't care one way or the other.

    ValueType vt = getType();
    switch (vt) {
      case WORD:
        return wordF.apply(asWord());

      case SEXPR:
        return sexprF.apply(asSexpr());

      default:
        // Why do we have a "default" line? Because the Java Language Standard states:
        // "Adding or reordering constants from an enum type will not break compatibility with pre-existing binaries."
        // So we're being forced to deal with a hypothetical future modification to the enum that, of course,
        // will never actually happen. Sigh.
        throw new RuntimeException("this should never happen! unexpected value type: " + vt);
    }
  }

  /**
   * Useful if, when you're given an Value, you're wondering what kind of value it might actually
   * be before using any of the helper methods above to cast to a specific type.
   */
  @NotNull
  @Contract(pure = true)
  ValueType getType();

  /**
   * If you know that this Value is <b>definitely</b> a Word, this method does the casting for you
   * in a nice, pipelined fashion.
   */
  @NotNull
  @Contract(pure = true)
  default Word asWord() {
    return (Word) this;
  }

  /**
   * If you know that this Value is <b>definitely</b> a Sexpr, this method does the casting for you
   * in a nice, pipelined fashion.
   */
  @NotNull
  @Contract(pure = true)
  default Sexpr asSexpr() {
    return (Sexpr) this;
  }

  /**
   * When you have an s-expression Value, but you're not sure what kind of value it is, you can call {@link #getType()}
   * which will return one of the constants from this enum.
   */
  enum ValueType {
    SEXPR, WORD
  }

  class Sexpr implements Value {
    @NotNull
    private final IList<Value> valueList;

    /**
     * This constructor is something you can use from edu.rice.sexpr.Parser, but it's not intended for public use.
     */
    Sexpr(@NotNull IList<Value> valueList) {
      this.valueList = valueList;
    }

    /**
     * Returns a list of the Values in the Sexpr.
     */
    @NotNull
    @Contract(pure = true)
    public IList<Value> getList() {
      return valueList;
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public ValueType getType() {
      return ValueType.SEXPR;
    }

    /**
     * Returns the nth value in the array, if present.
     */
    @NotNull
    @Contract(pure = true)
    public Option<Value> nth(int i) {
      return valueList.nth(i);
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public String toString() {
      return "( " + valueList.join(" ") + " )";
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Sexpr)) {
        return false;
      }

      Sexpr sexpr = (Sexpr) o;

      return valueList.equals(sexpr.valueList);
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
      return valueList.hashCode();
    }
  }

  class Word implements Value {
    private final String word;

    /**
     * This constructor is something you can use from edu.rice.sexpr.Parser, but it's not intended for public use.
     */
    Word(String word) {
      this.word = word;
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public ValueType getType() {
      return ValueType.WORD;
    }

    /**
     * Gives you back the number inside the JNumber as a double.
     */
    @Contract(pure = true)
    public String get() {
      return word;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public String toString() {
      return word;
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Word)) {
        return false;
      }

      Word ow = (Word) o;

      return ow.get().equals(word);
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
      return word.hashCode();
    }
  }
}
