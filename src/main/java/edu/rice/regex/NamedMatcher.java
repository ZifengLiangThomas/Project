/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.regex;

import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.tree.IMap;
import edu.rice.tree.TreapMap;
import edu.rice.util.Log;
import edu.rice.util.Option;
import edu.rice.util.Try;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static edu.rice.util.Strings.objectToEscapedString;

/**
 * <p> Given a Java enum which defines regexes for scanning tokens, this class builds a regex with
 * "named-capturing groups" and then lets you use it to tokenize your input. The names of the tokens
 * will come from the Enum, and the regular expressions for the tokens will come by fetching the
 * pattern associated with each token in the enum. If you have a token type that doesn't ever occur in
 * your input, such as "FAIL" in the example below, make sure its pattern is the empty string, and it
 * will be ignored while constructing the regular expressions. </p> <p> To make this work, make sure
 * your enum implements the <code>NamedMatcher.TokenPatterns</code> interface, which means it will have an
 * extra method, <code>pattern()</code> which returns the regex pattern. </p> <p> Example: </p>
 * <pre>
 * <code>
 * public enum CurlyLanguagePatterns implements NamedMatcher.TokenPatterns {
 *     OPENCURLY("\\{"),
 *     CLOSECURLY("\\}"),
 *     WHITESPACE("\\s+"),
 *     FAIL("");
 *
 *     public final String pattern;
 *
 *     CurlyLanguagePatterns(String pattern) {
 *         this.pattern = pattern;
 *     }
 *
 *     public String value() { return pattern; }
 * }
 * </code>
 * </pre>
 * <p> Typical usage: </p>
 * <pre>
 * <code>
 * NamedMatcher&lt;CurlyLanguagePatterns&gt; nm = new NamedMatcher&lt;&gt;(CurlyLanguagePatterns.class);
 * IList&lt;Token&lt;CurlyLanguagePatterns&gt;&gt; results = nm.tokenize(inputString, new
 * Token&lt;&gt;(CurlyLanguagePatterns.FAIL, ""));
 * </code>
 * </pre>
 */
public class NamedMatcher<PatternT extends Enum<PatternT> & NamedMatcher.TokenPatterns> {
  private static final String TAG = "NamedMatcher";

  @NotNull
  private final Pattern pattern;
  @NotNull
  private final IMap<String, PatternT> nameToTokenMap;
  @NotNull
  private final IList<String> groupNames;

  /**
   * Given an enum type that includes String values (and implements the <code>TokenPatterns</code> interface), this builds
   * a regular expression using "named-capturing groups" and uses that to help tokenize input strings.
   *
   * @see TokenPatterns
   * @param enumPatternsClazz
   *     The enum's "Class"
   */
  public NamedMatcher(@NotNull Class<PatternT> enumPatternsClazz) {
    // Engineering note: the type constraints on PatternT have seemingly magical properties. If you poke around
    // on the Internet for code doing similar things, you'll see lots of wildcard types (i.e., Class<?>) and
    // typecasts. By constraining the argument to the constructor, enumPatternClazz, to be Class<PatternT>, and with all the
    // constraints on PatternT, the Java compiler will only allow you to pass in the Class for an Enum that implements
    // our desired String getPattern() method.

    // In essence, with these type constraints, there's no way to have the type parameter PatternT and the class object
    // enumPatternsClazz be anything other than one and the same. This does lead to the uncomfortable question of why
    // you have to pass both a type parameter and a class parameter. Why not pass just one? Sigh. That would be yet
    // another weakness of the Java language.

    // The "real" solution would be to use a programming language that has "reified generics", wherein the type
    // parameter PatternT is a thing that you can directly interact with, doing all the things that Java forces you
    // to do with these "Class" objects instead. Microsoft C# actually does this properly. Likewise, some of the
    // other languages that run on the JVM, like Kotlin, also have reified generics.

    // Further reading:
    // http://whyjavasucks.com/Blog/5/Java_By_Example/87/Type_Erasure
    // http://stackoverflow.com/questions/31876372/what-is-reification
    // https://kotlinlang.org/docs/reference/inline-functions.html#reified-type-parameters

    if (!enumPatternsClazz.isEnum()) {
      // This particular failure should never actually happen, because of the type constraint PatternT extends Enum<PatternT>.
      // Nonetheless, a bit of paranoia seems reasonable.
      throw new RuntimeException("NamedMatcher requires an enum class");
    }

    // this gets us an array of all the enum values in the type.
    final IList<PatternT> enumConstants = List.fromArray(enumPatternsClazz.getEnumConstants());

    final IList<KeyValue<String, String>> nameToRegexMap =
        enumConstants
            .map(e -> e.match(KeyValue::make)) // converts from name to name => pattern
            .filter(kv -> !kv.getValue().equals(""));  // get rid of non-parsing tokens, error/metadata tokens, etc.

    final int numPatterns = nameToRegexMap.length();
    nameToTokenMap = TreapMap.fromList(enumConstants.map(e -> KeyValue.make(e.name(), e)));

    groupNames = nameToRegexMap.map(KeyValue::getKey);

    // Before we build the "real" regular expression that combines all the individual ones,
    // we're first going to try compiling the individual expressions to make sure that they're
    // individually well-formed. This will result in better error-feedback to developers.

    final int numSuccess = nameToRegexMap.map(kv -> kv.match((name, pattern) ->
        KeyValue.make(name,
            Try.of(() -> Pattern.compile(pattern))
                .logIfFailure(TAG, throwable -> String.format("regular expression (%s) for (%s) is not valid: %s",
                    pattern, name, throwable.getMessage())))))
        .filter(kv -> kv.getValue().isSuccess())
        .length();

    if (numSuccess != numPatterns) {
      Log.e(TAG, () -> String.format("found only %d of %d valid regular expressions", numSuccess, numPatterns));
      throw new IllegalArgumentException("invalid regular expression");
    }

    // this is the final "group matching" regex pattern that we'll use in the tokenizer
    pattern = Pattern.compile(
        nameToRegexMap
            // build the named-capturing groups regular expression
            .map(kv -> String.format("(?<%s>%s)", kv.getKey(), kv.getValue()))
            .join("|"));
  }

  /**
   * This returns a list of pairs corresponding to the output of the tokenizer, where first element
   * of the pair is the enum value (you can later cast this to the type you used when making the
   * NamedMatcher) and the second element is the string that the regex matched.
   *
   * <p>If the tokenizer hits something for which there isn't a matching regex, the next element of the
   * resulting list of tokens will be the failToken.
   */
  @NotNull
  @Contract(pure = true)
  public IList<Token<PatternT>> tokenize(@NotNull String input, @NotNull Token<PatternT> failToken) {
    final java.util.regex.Matcher jmatcher = pattern.matcher(input);

    // We need a counter for how far into the string we are; we use this to detect skipped characters.
    // We also need to remember if we had a scanner error. We can't just have an int and a boolean hanging
    // around normally because that violates Java8's lambda lexical scope capture rules. So instead, we
    // wrap it into a class instance, which will then be within the lexical scope of the lambdas and where
    // we can freely do all the mutation we want.

    // Yes, this is seemingly inexplicable. What matters is that keeping state on the side, which mutates,
    // is not easily compatible with lambdas, yet we must do precisely this to work with the (awful) regex
    // APIs that we get from java.util.regex. All of this awful business is hidden from the user of our
    // NamedMatcher package, at least.

    // Basically, lambdas aren't capable of reaching back into their lexical scope and mutating values.
    // However, they can mutate values inside of other values, so fine, that's what we're doing.

    final NamedGroupState state = new NamedGroupState();
    state.matchOffset = 0;
    state.failure = false;

    return LazyList.ogenerate(() -> {
      if (state.failure) {
        return Option.none();
      }

      if (jmatcher.find(state.matchOffset)) {
        MatchResult mresult = jmatcher.toMatchResult();
        int matchStart = mresult.start();

        IList<String> namesFound = groupNames.filter(name -> jmatcher.group(name) != null);

        if (namesFound.length() == 0) {
          // this case (hopefully) won't happen because, if there are no matches, then
          // jmatcher.find() should return false. But in the interests of paranoia...
          Log.i(TAG, () -> String.format("no matching token found, scanner failed (context: %s)",
              safeSubstring(input, state.matchOffset, 10)));
          state.failure = true;
          return Option.some(failToken);
        }

        if (namesFound.length() > 1) {
          Log.e(TAG, () ->
              String.format(
                  "multiple matches (token types: [%s]), input patterns are ambiguous (error!), scanner failed (context: %s)",
                  namesFound.join(","), safeSubstring(input, state.matchOffset, 10)));
          state.failure = true;
          return Option.some(failToken);
        }

        if (matchStart > state.matchOffset) {
          Log.i(TAG, () -> String.format("matcher skipped some characters, scanner failed (context: %s)",
              safeSubstring(input, state.matchOffset, 10)));
          state.failure = true;
          return Option.some(failToken);
        }

        String matchName = namesFound.head(); // the token, we found it, hurrah!
        String matchString = mresult.group();
        state.matchOffset += matchString.length(); // advance the state for next time: mutation!

        if (matchString.length() == 0) {
          Log.e(TAG, () -> String.format("matcher found a zero-length string! bug in regex for token rule (%s)", matchName));
          state.failure = true;
          return Option.some(failToken);
        }

        return nameToTokenMap
            .oget(matchName) // go from the token string to the actual TokenPatterns enum
            .map(type -> new Token<>(type, matchString)) // then build a token around it
            .orElse(Option.some(failToken));

      } else {
        // two possibilities: either we hit the end of the input, or we failed to match any of the patterns
        if (state.matchOffset >= input.length()) {
          return Option.none(); // empty-list; we're done!
        }

        // otherwise, there are some characters remaining that we don't know what to do with
        Log.i(TAG, () -> String.format("no matching token found, scanner failed (context: %s)",
            safeSubstring(input, state.matchOffset, 10)));
        state.failure = true;
        return Option.some(failToken);
      }
    });
  }

  /**
   * The real {@link String#substring(int, int)} will throw an exception if you ask for anything beyond the end
   * of the string. This method will truncate at the end. No exceptions.
   */
  @NotNull
  @Contract(pure = true)
  private static String safeSubstring(@NotNull String input, int offset, int length) {
    if (offset + length > input.length()) {
      return input.substring(offset, input.length());
    } else {
      return input.substring(offset, offset + length);
    }
  }

  // used as state by the LazyList generator, see above
  private class NamedGroupState {
    int matchOffset;
    boolean failure;
  }

  /**
   * When we're implementing tokenizers, we're going to use enums to specify the token names and regular
   * expressions. By default, every enum has a <code>String code()</code> method which can get the token
   * name, but there isn't a method to get the regex pattern you associate with that token.
   * For that, you'll use the TokenPatterns interface.
   */
  public interface TokenPatterns {
    @NotNull
    @Contract(pure = true)
    String name(); // this comes from the enum

    @NotNull
    @Contract(pure = true)
    String pattern(); // you have to implement this one

    /**
     * General-purpose "deconstructing" match on a token. Takes a lambda with two arguments, which will be the name
     * and regex pattern associated with this particular token rule and returns whatever that function returns.
     */
    @NotNull
    @Contract(pure = true)
    default <T> T match(BiFunction<? super String,? super String, ? extends T> func) {
      return func.apply(name(), pattern());
    }
  }

  /**
   * General-purpose parsing tokens.
   */
  public static class Token<PatternT extends Enum<PatternT> & TokenPatterns> {
    @NotNull
    public final PatternT type;

    @NotNull
    public final String data;

    public Token(@NotNull PatternT type, @NotNull String data) {
      this.type = type;
      this.data = data;
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
      return toString().hashCode(); // a kludge, but hopefully useful
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object t) {
      if (!(t instanceof Token)) {
        return false;
      }

      // we're doing an unchecked type cast here, but it's okay because if the TokenPatterns differ,
      // the equals() test will sort it out
      Token<? extends Enum<PatternT>> tt = (Token<? extends Enum<PatternT>>) t;
      return this.data.equals(tt.data) && this.type.equals(tt.type);
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public String toString() {
      return String.format("(%s: %s)", type.name(), objectToEscapedString(data));
    }
  }
}
