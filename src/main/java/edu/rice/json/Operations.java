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
import edu.rice.list.KeyValue;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.util.Option;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static edu.rice.json.Builders.*;
import static edu.rice.util.Strings.stringToTryInteger;

/**
 * Helpful utility operations for querying and updating JSON Values.
 */
public interface Operations {
  /**
   * Given a list of strings, fetch the Value corresponding to this path. For a JSON object, the values are interpreted
   * as the keys of a key-value tuple in the JSON object. For a JSON array, the values are interpreted as decimal (base
   * 10) integers. When the JSON data structure matches up with the path, the Value at the end will be returned.
   *
   * @return Option.some of the query result, if it's there, or Option.none if it's absent.
   */
  @NotNull
  @Contract(pure = true)
  static Option<Value> ogetPath(@NotNull Value value, @NotNull IList<String> pathList) {
    return pathList.match(
        emptyList -> Option.some(value),

        (pathHead, pathTail) ->
          value.match(
              jObject -> jObject.oget(pathHead),
              jArray -> stringToTryInteger(pathHead).toOption().flatmap(jArray::nth),
              Option::none,
              Option::none,
              Option::none,
              Option::none)

              .flatmap(resultValue -> ogetPath(resultValue, pathTail)));
  }

  /**
   * Given a forward-slash-separated path, fetch the Value corresponding to this path. For a JSON object, the values
   * between the slashes are interpreted as the keys of a key-value tuple in the JSON object. For a JSON array, the
   * values are interpreted as decimal (base 10) integers. When the JSON data structure matches up with the path, the
   * Value at the end will be returned.
   *
   * @return Option.some of the query result, if it's there, or Option.none if it's absent.
   */
  @NotNull
  @Contract(pure = true)
  static Option<Value> ogetPath(@NotNull Value value, @NotNull String path) {
    return ogetPath(value, LazyList.fromArray(path.split("/")));
  }

  /**
   * Given a list of regular expressions, fetch a list of Values which match this path. Each regex matches the key in a
   * JSON object's key/value tuples. For JSON arrays, the "key" is treated as a decimal (base 10) integer, then
   * converted to a string for the regex match.
   */
  @NotNull
  @Contract(pure = true)
  static IList<Value> getPathMatchesRegex(
      @NotNull Value value,
      @NotNull IList<String> pathRegexList) {

    return getPathMatches(value, pathRegexList.map(Pattern::compile).map(Pattern::asPredicate));
  }

  /**
   * Given a list of predicates over strings, fetch list of Values which match these predicates. Each predicate matches
   * the key in a JSON object's key/value tuples. For JSON arrays, the "key" is treated as a decimal (base 10) integer,
   * then converted to a string for the predicate.
   */
  @NotNull
  @Contract(pure = true)
  static IList<Value> getPathMatches(
      @NotNull Value value,
      @NotNull IList<Predicate<String>> pathPredicateList) {

    IList<Value> emptyList = List.makeEmpty();

    return pathPredicateList.match(
        empty -> List.of(value),

        (pathHead, pathTail) -> value.match(
            jObject -> jObject.getMatching(pathHead).map(KeyValue::getValue),
            jArray -> jArray.getMatching(num -> pathHead.test(num.toString())),
            jString -> emptyList,
            jNumber -> emptyList,
            jBoolean -> emptyList,
            jNull -> emptyList)

            .flatmap(val -> getPathMatches(val, pathTail)));
  }

  /**
   * Given a slash-separated path and a starting Value, this will replace the ultimate value that this path leads to
   * with the result of the update-function (updateFunc) applied to the previous value. If there's no value already there,
   * an option.none will be passed as the argument to updateFunc. If an option.none is returned, that will be treated
   * as an instruction to remove the value.
   *
   * @see Value.JObject#updateKeyValue(String, UnaryOperator)
   * @return Option.some of the updated JSON value, or option.none in the case that the update removed the whole value
   */
  @NotNull
  @Contract(pure = true)
  static Option<Value> updatePath(
      @NotNull Value value,
      @NotNull String path,
      @NotNull UnaryOperator<Option<Value>> updateFunc) {

    return updatePath(value, LazyList.fromArray(path.split("/")), updateFunc);
  }

  /**
   * Given a list of strings as a path and a starting Value, this will replace the ultimate value that this path leads
   * to with the result of the update-function (updateFunc) applied to the previous value. If there's no value already
   * there, an option.none will be passed as the argument to updateFunc. If an option.none is returned, that will be
   * treated as an instruction to remove the value.
   *
   * @see Value.JObject#updateKeyValue(String, UnaryOperator)
   * @return Option.some of the updated JSON value, or option.none in the case that the update removed the whole value
   */
  @NotNull
  @Contract(pure = true)
  static Option<Value> updatePath(
      @NotNull Value value,
      @NotNull IList<String> pathList, UnaryOperator<Option<Value>> updateFunc) {

    return updatePath(Option.some(value), pathList, updateFunc);
  }

  /**
   * Given a list of strings as a path and an optional starting Value, this will replace the ultimate value that this
   * path leads to with the result of the update-function (updateFunc) applied to the previous value. If there's no value
   * already there, an option.none will be passed as the argument to updateFunc. If an option.none is returned, that
   * will be treated as an instruction to remove the value.
   *
   * @see Value.JObject#updateKeyValue(String, UnaryOperator)
   * @return Option.some of the updated JSON value, or option.none in the case that the update removed the whole value
   */
  @NotNull
  @Contract(pure = true)
  static Option<Value> updatePath(
      @NotNull Option<Value> oValue,
      @NotNull IList<String> pathList, UnaryOperator<Option<Value>> updateFunc) {

    // Here's a curious thing: we're using an Option<Value> as the thing that the updateFunc function returns.
    // This works great for updating an "object" (i.e., a mapping) because if the option is none,
    // then we can interpret that as deleting the key/value tuple from the mapping. We're going to interpret
    // this same way for arrays, but it will have the side-effect of shifting the array over by one, acting
    // a bit like IList.filter().

    return pathList.match(
        // In this particular case, we've hit the end of the pathList and we don't care if the oValue is present or not;
        // either way, the update-function will deal with it.
        emptyList -> updateFunc.apply(oValue),

        (pathHead, pathTail) -> oValue.match(
            // in this case, there's nothing present in our input JSON object, but we still want to do the
            // recursive update, which will mean building up new JSON objects, recursively; However, the
            // ultimate lambda that gives us the leaf value might well return its own Option.none(), so
            // we need to condition this whole mess on the Option.some()'s coming back to us... recursively.
            () -> updatePath(Option.none(), pathTail, updateFunc)
                .map(childVal -> jobject(jpair(pathHead, childVal))),

            value -> value.match(
                jObject -> jObject.updateKeyValue(
                    pathHead, // the key (from the path)
                    oval -> updatePath(oval, pathTail, updateFunc)),

                jArray -> Option.some(
                    stringToTryInteger(pathHead).match(
                        exception -> jArray,
                        number -> jArray.updateNth(number, listElem -> updatePath(listElem, pathTail, updateFunc)))),

                // for all of the scalar types (number, string, etc.) we're not changing them, since they don't
                // match the path; instead, we're returning them as-is
                Option::some,
                Option::some,
                Option::some,
                Option::some)));
  }

  /**
   * Given a list of regular expressions, find all Values which match this path, then apply the function to each one,
   * returning a new JSON object. The matching process works the same as getPathMatchesRegex, and the overall effect is
   * analogous to mapping a function on a list, returning a new list. Any contents, unmatched by the expressions, will
   * be unchanged.
   *
   * @see #getPathMatchesRegex(Value, IList)
   * @return Option.some of the updated JSON value, or option.none in the case that the update removed the whole value
   */
  @NotNull
  @Contract(pure = true)
  static Option<Value> updatePathMatchesRegex(
      @NotNull Value value,
      @NotNull IList<String> pathRegexList,
      @NotNull UnaryOperator<Option<Value>> updateFunc) {

    return updatePathMatches(value, pathRegexList.map(Pattern::compile).map(Pattern::asPredicate), updateFunc);
  }

  /**
   * Given a list of predicates over strings, find all Values which match these predicates, then apply the function to
   * each one, returning a new JSON object. The matching process works the same as getPathMatches, and the overall
   * effect is analogous to mapping a function to a list, returning a new list. Any contents, unmatched by the
   * predicates, will be unchanged.
   *
   * @see #getPathMatches(Value, IList)
   * @return Option.some of the updated JSON value, or option.none in the case that the update removed the whole value
   */
  @NotNull
  @Contract(pure = true)
  static Option<Value> updatePathMatches(
      @NotNull Value value,
      @NotNull IList<Predicate<String>> pathPredicateList,
      @NotNull UnaryOperator<Option<Value>> updateFunc) {

    return pathPredicateList.match(
        emptyList -> updateFunc.apply(Option.some(value)),

        (pathHead, pathTail) -> value.match(
            jObject -> jObject.getMatching(pathHead)
                // IList<Value> - all key/value pairs in the object whose keys match the predicate

                .foldl(
                    // we start the fold with the accumulator equal to the original jObject value
                    Option.some(jObject),

                    // For each of those keyvalues, recursively do an update to the value then fold it in.
                    (oj, kv) ->
                        kv.match((key, val) ->
                            oj.flatmap(
                                // safe to cast to a jobject because we're only editing objects here
                                j -> j.asJObject()

                                    // this is the real meat of the fold: we're updating the jobject
                                    .updateKeyValue(key,

                                        // the recursive part
                                        ignored -> updatePathMatches(val, pathTail, updateFunc))))),


            jArray -> Option.some(jarray(
                // we're making a new jarray to replace this one
                jArray.getKVList()
                    // -> IList<KeyValue<Integer,Value>>, where the ints are sequential numbers
                    .oflatmap(kv ->
                        // test if the predicate matches for the key
                        kv.match((key, val) ->
                            (pathHead.test(key.toString()))

                                // if the predicate matches, we recursively work our way down
                                ? updatePathMatches(val, pathTail, updateFunc)

                                // otherwise we don't have to do anything
                                : Option.some(val))))),


            // for all of the scalar types (number, string, etc.) we're not changing them, since they don't match the path,
            // so we're returning them as-is
            Option::some,
            Option::some,
            Option::some,
            Option::some));
  }
}
