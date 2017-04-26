/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.json;

import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.regex.NamedMatcher;
import edu.rice.util.Log;
import edu.rice.util.Option;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static edu.rice.json.Scanner.JsonPatterns;
import static edu.rice.json.Value.*;
import static edu.rice.util.Strings.stringToOptionDouble;

import static edu.rice.regex.NamedMatcher.*;

/**
 * Parser for various JSON types. Everything public is a static method; this class is never instantiated.
 * If you're trying to convert a String to an arbitrary JSON value, then you probably want to use {@link #parseJsonValue(String)}.
 * If your String is something that you require to be a JSON Object or Array, then you probably want to use
 * {@link #parseJsonObject(String)} or {@link #parseJsonArray(String)}, respectively.
 */
public class Parser {
  private static final String TAG = "JsonParsers";

  // Engineering note: There's no particular reason for this to be a class vs. an interface. Either way, we
  // just want to export a bunch of static methods. If it were an interface, we wouldn't need to declare
  // the private constructor, as below, but then there would be no way to have the private helper methods
  // or the package-scope methods that we have here. (Java9 adds private but not package-scope methods to
  // interfaces.)  The approach here is pretty much the standard way of doing things before Java8, so
  // we'll use it here as well.

  // So why are some method public and others package-scope? The public static methods are meant to be the *external*
  // interface to our JSON code. The package-scope static methods are internal, but we want them visible to our
  // unit tests, which are all in the same edu.rice.json package.

  // On the other hand, the private methods (and the MAKERS field) aren't meant to be used by anybody
  // outside of this file. That's what "private" is meant to convey.

  private Parser() { } // never instantiate this class!

  /**
   * Given a String input, this will attempt to parse it and give you back a
   * JSON object, which can then be interrogated for its internal contents.
   *
   * @see Value.JObject#getMap()
   * @return Option.some of the JSON value, if the parse operation succeeded, or option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  public static Option<Value.JObject> parseJsonObject(@NotNull String input) {
    IList<NamedMatcher.Token<Scanner.JsonPatterns>> tokens = Scanner.scan(input);

    return makeObject(tokens)
        .flatmap(result -> result.tokens.match(
            // this is what we want: no remaining tokens after we're done parsing the Object
            emptyList -> result.production.asOJObject(),

            (head, tail) -> {
              // adding explicit logging because otherwise the programmer may get really confused wondering why
              Log.e(TAG, "tokens remaining in the stream after end of the JSON object; parser failure");
              return Option.none();
            }));
  }

  /**
   * Given a String input, this will attempt to parse it and give you back a
   * JSON value (of any type: object, array, string, etc.). You may then interrogate the result
   * for its concrete type and/or contents.
   *
   * @see Value#getType()
   * @return Option.some of the JSON value, if the parse operation succeeded, or option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  public static Option<Value> parseJsonValue(@NotNull String input) {
    IList<NamedMatcher.Token<Scanner.JsonPatterns>> tokens = Scanner.scan(input);

    return makeValue(tokens)
        .flatmap(result -> result.tokens.match(
            // this is what we want: no remaining tokens after we're done parsing the Value
            emptyList -> Option.some(result.production),

            (head, tail) -> {
              // adding explicit logging because otherwise the programmer may get really confused wondering why
              Log.e(TAG, "tokens remaining in the stream after end of the JSON value; parser failure");
              return Option.none();
            }));
  }

  /**
   * Given a String input, this will attempt to parse it and give you back a
   * JSON array which can then be interrogated for its internal contents.
   *
   * @see Value.JArray#nth(int)
   * @return Option.some of the JSON array, if the parse operation succeeded, or option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  public static Option<Value.JArray> parseJsonArray(@NotNull String input) {
    IList<NamedMatcher.Token<Scanner.JsonPatterns>> tokens = Scanner.scan(input);

    return makeArray(tokens)
       .flatmap(result -> result.tokens.match(
         // this is what we want: no remaining tokens after we're done parsing the Value
         emptyList -> result.production.asOJArray(),

         (head, tail) -> {
           // adding explicit logging because otherwise the programmer may get really confused wondering why
           Log.e(TAG, "tokens remaining in the stream after end of the JSON array; parser failure");
           return Option.none();
         }));
  }

  /**
   * Every internal make-method returns an Option&lt;Result&gt;, which inside contains
   * the Value produced as well as an IList of the remaining tokens. That Result
   * is parameterized. Commonly it's Result&lt;Value&gt; but some helper functions
   * and such return other things besides Value, while still returning a production
   * of some kind and a list of remaining tokens.
   */
  static class Result<T> {
    @NotNull
    public final T production;
    @NotNull
    public final IList<NamedMatcher.Token<Scanner.JsonPatterns>> tokens;

    Result(@NotNull T production, @NotNull IList<NamedMatcher.Token<Scanner.JsonPatterns>> tokens) {
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

  //
  // ENGINEEERING NOTES / Data definition:
  //
  // Every different method here represents a different JSON production type that we might
  // be able to parse. If it successfully parses the thing it's looking for, it will return
  // an Option<Result<Value>> containing the production (JObject, JString, etc.) as well as a list
  // of the remaining unparsed tokens. If it fails, it returns Option.None.
  //
  // Each of the make-methods returns the wider type Option<Result<Value>> rather than something
  // more specific, like makeString returning Option<Result<JString>>, in order for all of the
  // make-methods to have the *same* type signature, which lets us make a list of them, as below,
  // and not worry about type compatibility.
  //
  // The only exception to this is makeKeyValue, which is only necessary as an internal helper
  // function to makeObject. You can get away with being more specific there.
  //

  //
  // Here is a list of the builders as lambdas; note the impressive type signature.
  //
  // <p>A value can be:
  //   string
  //   number
  //   object
  //   array
  //   true
  //   false
  //   null
  //
  private static final IList<Function<IList<Token<JsonPatterns>>, Option<Result<Value>>>> MAKERS = List.of(
      Parser::makeString,
      Parser::makeNumber,
      Parser::makeObject,
      Parser::makeArray,
      Parser::makeBoolean,
      Parser::makeNull);

  /**
   * General-purpose maker for all value types; will internally try all the concrete JSON builders
   * and return the result of whichever one succeeds.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeValue(@NotNull IList<Token<JsonPatterns>> tokenList) {
    // Returns the first Option.some() result, if it exists.
    return MAKERS.oflatmap(x -> x.apply(tokenList)).match(

        // none of the builders succeeded, so we'll pass that along
        emptyList -> Option.none(),

        // we got exactly one success, which is exactly what we want
        (head, emptyTail) -> Option.some(head),

        // oops, multiple successful builders!
        (first, second, remainder) -> {
          Log.e(TAG, "Ambiguous parser! Only one production should be successful.");
          throw new RuntimeException("Ambiguous parser! Only one production should be successful.");
        });
  }

  /**
   * Maker for JSON Objects.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeObject(@NotNull IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> {
          switch (token.type) {
            // we first require an open curly brace
            case OPENCURLY:
              // next, we recursively add together the list of key-value pairs; this will consume the close curly
              return makeObjectHelper(remainingTokens, true)
                  .map(result -> new Result<>(new Value.JObject(result.production), result.tokens));

            default:
              return Option.none();
          }
        });
  }

  // recursive helper function
  @NotNull
  @Contract(pure = true)
  private static Option<Result<IList<JKeyValue>>>
      makeObjectHelper(@NotNull IList<Token<JsonPatterns>> tokenList, boolean firstTime) {

    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> {
          Option<Result<JKeyValue>> oKeyValue;

          switch (token.type) {
            // if we find a close bracket, then we're done!
            case CLOSECURLY:
              return Option.some(new Result<>(List.makeEmpty(), remainingTokens));

            case COMMA:
              // we require a comma between key-value tuples, but not the first time
              if (firstTime) {
                return Option.none();
              }
              oKeyValue = makeKeyValue(remainingTokens);
              break;

            default:
              // we required a comma, but didn't get it
              if (!firstTime) {
                return Option.none();
              }
              oKeyValue = makeKeyValue(tokenList);
              break;
          }

          // recursively continue consuming the rest of the input and then prepend the current key/value pair to
          // the front of the list that's returned from the recursive call (pair.production) and pass along the
          // remaining unconsumed tokens
          return oKeyValue.flatmap(headResult ->
              makeObjectHelper(headResult.tokens, false)
                  .map(tailResults -> new Result<>(tailResults.production.add(headResult.production), tailResults.tokens)));
        });
  }

  /**
   * Attempts to construct a JKeyValue from a list of tokens.
   *
   * @return Option.some of the Result, which includes the JKeyValue and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<JKeyValue>> makeKeyValue(@NotNull IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (head, emptyTail) -> Option.none(),

        (string, colon, remainingTokens) -> {
          if (string.type != JsonPatterns.STRING || colon.type != JsonPatterns.COLON) {
            return Option.none();
          }

          // We could alternatively call into JString.build(), but we've already verified
          // the token type, and it's a terminal token, so we'll take a short-cut.
          JString jstring = JString.fromEscapedString(string.data);

          // and finally grab the value and turn it into a pair
          return makeValue(remainingTokens)
              .map(value -> new Result<>(JKeyValue.fromKeyValue(jstring, value.production), value.tokens));
        });
  }

  /**
   * Attempts to construct a JArray from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeArray(@NotNull IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> {
          switch (token.type) {
            case OPENSQUARE:
              // next, we recursively add together the list; this will consume the close square
              return makeArrayHelper(remainingTokens, true)
                  .map(result -> new Result<>(new JArray(result.production), result.tokens));

            default:
              return Option.none();
          }
        });
  }

  // this helper function is only necessary because we want to have the "firstTime" argument to deal with commas
  @NotNull
  @Contract(pure = true)
  private static Option<Result<IList<Value>>> makeArrayHelper(
      @NotNull IList<Token<JsonPatterns>> tokenList, boolean firstTime) {

    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> {

          Option<Result<Value>> nextValue; // the next value in the JSON array will go here

          // but before we try to grab that value, we first need to deal with the requirements of
          // JSON, namely that if we hit a close-square-bracket, we're done, and we have to deal with
          // commas, which are required between array elements. Note the use of the firstTime boolean
          // to distinguish the two cases while we're doing our parsing.

          switch (token.type) {
            case CLOSESQUARE:
              return Option.some(new Result<>(List.makeEmpty(), remainingTokens));

            case COMMA:
              // Comma must be absent if it's the first time through.
              if (firstTime) {
                return Option.none();
              }

              // But if it's not, then it's required! Try to get the next JValue.
              nextValue = makeValue(remainingTokens);
              break;

            default:
              // if it's not the first time, we required a comma.
              if (!firstTime) {
                return Option.none();
              }

              // Otherwise, the current token needs to be reused, so we're using tokenList rather than remainingTokens
              nextValue = makeValue(tokenList);
              break;
          }

          // recursively continue consuming the rest of the input and then prepend the current value to the front of the
          // list that's returned from the recursive call and pass along the remaining unconsumed tokens
          return nextValue
              .flatmap(headResult ->
                  makeArrayHelper(headResult.tokens, false)
                      .map(tailResults ->
                          new Result<>(tailResults.production.add(headResult.production), tailResults.tokens)));
        });
  }

  /**
   * Attempts to construct a JString from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeString(@NotNull IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> {
          switch (token.type) {
            case STRING:
              return Option.some(new Result<>(JString.fromEscapedString(token.data), remainingTokens));

            default:
              return Option.none();
          }
        });
  }

  /**
   * Attempts to construct a JNumber from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeNumber(@NotNull IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> {
          switch (token.type) {
            case NUMBER:
              return stringToOptionDouble(token.data)
                  .map(number -> new Result<>(new JNumber(number), remainingTokens));

            default:
              return Option.none();
          }
        });
  }

  /**
   * Attempts to construct a JBoolean from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeBoolean(@NotNull IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> {
          switch (token.type) {
            case TRUE:
              return Option.some(new Result<>(JBoolean.fromBoolean(true), remainingTokens));

            case FALSE:
              return Option.some(new Result<>(JBoolean.fromBoolean(false), remainingTokens));

            default:
              return Option.none();
          }
        });
  }

  /**
   * Attempts to construct a JNull from a list of tokens.
   *
   * @return Option.some of the Result, which includes the Value and a list of the remaining tokens; option.none if it failed
   */
  @NotNull
  @Contract(pure = true)
  static Option<Result<Value>> makeNull(@NotNull IList<Token<JsonPatterns>> tokenList) {
    return tokenList.match(
        emptyList -> Option.none(),

        (token, remainingTokens) -> {
          switch (token.type) {
            case NULL:
              return Option.some(new Result<>(JNull.make(), remainingTokens));

            default:
              return Option.none();
          }
        });
  }
}
