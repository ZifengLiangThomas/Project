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

import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.regex.NamedMatcher;
import edu.rice.util.Log;
import edu.rice.util.Option;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static edu.rice.sexpr.Scanner.*;
import static edu.rice.sexpr.Value.*;

import static edu.rice.regex.NamedMatcher.*;

/**
 * S-Expression Recursive-Descent Parser. The way we're doing it here is a bit like:
 * <br>Value ::= Word | SExpr
 * <br>SExpr ::= ( list-of-Values )
 *
 * <p>That second line isn't exactly a proper statement in a BNF grammar, but it's very convenient. The
 * recursive-descent parser returns a list of values on its way out, since that's ultimately the most
 * convenient format for operating on s-expressions once you've read them in.
 */
public class Parser {
  private static final String TAG = "SexprParser";

  private Parser() { }

  /**
   * Given a String input, this will attempt to parse it and give you back an
   * S-Expression which can then be interrogated for its internal contents.
   *
   * @see Value.Sexpr#nth(int)
   * @return Option.some of the S-Expression, if the parse operation succeeded, or option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  public static Option<Value> parseSexpr(@NotNull String input) {
    // Engineering note: we're not exposing Result outside of this file. From the outside, you say parseSexpr
    // and you get back an optional Value. The details of what kind of Value can be found within, and the
    // remaining tokens are dealt with here. If there *are* remaining tokens, then the input might well have
    // *started* with a valid s-expression, but the string, as a whole, is *not* an s-expression, so we'll
    // return Option.none().

    return makeValue(Scanner.scan(input))
        .flatmap(result -> result.tokens.match(
            // this is what we want: no remaining tokens after we're done parsing the Value
            emptyList -> Option.some(result.production),

            (head, tail) -> {
              // adding explicit logging because otherwise the programmer may get really confused wondering why
              Log.e(TAG, "tokens remaining in the stream after end of the s-expression; parser failure");
              return Option.none();
            }));
  }

  // This internal class is the result of calling each production. It's got a type parameter, because each
  // production returns something different, but they always return the resulting production, and a list
  // of remaining tokens. That pairing is handled here. Yes, we could have used Pair instead, but then the
  // type parameters would start getting really ugly. Better to be specific for our needs here.
  static class Result<T> {
    @NotNull
    public final T production;
    @NotNull
    public final IList<NamedMatcher.Token<Scanner.SexprPatterns>> tokens;

    Result(@NotNull T production, @NotNull IList<NamedMatcher.Token<Scanner.SexprPatterns>> tokens) {
      this.production = production;
      this.tokens = tokens;
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public String toString() {
      return String.format("Result(production: %s, tokens: %s)", production.toString(), tokens.toString());
    }
  }

  private static final IList<Function<IList<Token<SexprPatterns>>, Option<Result<Value>>>> MAKERS = List.of(
      Parser::makeSexpr,
      Parser::makeWord);

  /**
   * General-purpose maker for all value types; will internally try all the concrete JSON builders
   * and return the result of whichever one succeeds.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeValue(@NotNull IList<Token<SexprPatterns>> tokenList) {
    // Returns the first non-empty result, if it exists. If we wanted to be paranoid,
    // we could try them all and yell if more than one succeeds, since that would indicate
    // an ambiguous grammar.
    return MAKERS.oflatmap(x -> x.apply(tokenList)).ohead();
  }

  /**
   * Attempts to construct a S-Expression from a list of tokens.
   *
   * @return Option.some of the Result, which includes the S-Expression Value and a list of the remaining tokens;
   *     option.none if it failed.
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeSexpr(@NotNull IList<Token<SexprPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> (token.type == SexprPatterns.OPEN)
            // next, we recursively add together the list; this will consume the close square and return to us a
            // list of tokens, which we'll then convert into an Sexpr.
            ? makeSexprHelper(remainingTokens)
                .map(result -> new Result<>(new Sexpr(result.production), result.tokens))

            : Option.none());
  }

  // This helper function deals with everything after the open-paren, recursively gobbling tokens until it hits
  // the close-paren, and then building an IList of values on the way back out.
  @NotNull
  @Contract(pure = true)
  private static Option<Result<IList<Value>>> makeSexprHelper(@NotNull IList<Token<SexprPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> (token.type == SexprPatterns.CLOSE)

            ? Option.some(new Result<>(List.makeEmpty(), remainingTokens))

            // recursively continue consuming the rest of the input and then prepend the current value to the front of the
            // list that's returned from the recursive call and pass along the remaining unconsumed tokens
            : makeValue(tokenList)
                .flatmap(headResult -> makeSexprHelper(headResult.tokens)
                    .map(tailResults -> new Result<>(tailResults.production.add(headResult.production), tailResults.tokens))));
  }

  /**
   * Attempts to construct a JString from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeWord(@NotNull IList<Token<SexprPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> (token.type == SexprPatterns.WORD)
            ? Option.some(new Result<>(new Word(token.data), remainingTokens))
            : Option.none());
  }
}
