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

import edu.rice.list.KeyValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import edu.rice.list.IList;
import edu.rice.list.LazyList;

/**
 * These functions help you build JSON expressions in a "fluent" way. For example:
 * <pre>
 *     <code>
 *         import static edu.rice.json.Builders.*;
 *
 *         Value simpleObject =
 *             jobject(
 *                 jpair("x", jnumber(1)),
 *                 jpair("y", jnumber(2)));
 *     </code>
 * </pre>
 * Once you've made a Value this way, you might then convert it to JSON using the
 * .toString() or .toIndentedLines() methods, or you might compare it to something that you've parsed,
 * for equality/testing purposes.
 *
 * <p>Note about strings: there are two ways to build a jstring: from a "Java string" and from a
 * "JSON string". The former represents something you might have in a Java data structure somewhere.
 * The latter represents raw input such as you might have from a network or other source of JSON
 * data. The essential difference comes down to special characters like newline. If you expect the
 * input to have a literal backslash and a literal n, then use the JSON string version. If you
 * expect it to have a newline character, then use the Java version.
 *
 * <p>You'll also notice that there are several versions of the jpair() function. JSON defines an object's
 * key/value pairs as mapping from strings to arbitrary JSON values. For your convenience, you can either
 * use some sort of jstring directly (via jsonString or javaString) or you can give a literal Java
 * string to the jpair() function. In this case, it uses javaString internally.
 *
 * @see Value#toIndentedString()
 * @see Parser#parseJsonObject(String)
 * @see Value.JObject#toString()
 * @see #jsonString(String)
 * @see #javaString(String)
 */
public interface Builders {
  /**
   * Fluent builder for JSON value objects.
   *
   * @see Value
   */
  @NotNull
  @Contract(pure = true)
  static Value.JObject jobject(Value.JKeyValue... values) {
    return jobject(LazyList.fromArray(values));
  }

  /**
   * Fluent builder for JObject.
   *
   * @see Value.JObject
   */
  @NotNull
  @Contract(pure = true)
  static Value.JObject jobject(@NotNull IList<Value.JKeyValue> values) {
    return Value.JObject.fromList(values);
  }

  /**
   * Fluent builder for JKeyValue pairs; this convenience method uses the KeyValue class, used in
   * our list and map classes (not to be confused with JKeyValue), and assumes the key string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If you
   * want to start with raw JSON strings, with escaped special characters, then you need to use the
   * jsonString builder instead.
   *
   * @see KeyValue
   * @see Value.JKeyValue
   * @see Builders#jsonString(String)
   */
  @NotNull
  @Contract(pure = true)
  static Value.JKeyValue jpair(@NotNull KeyValue<String, Value> kv) {
    return Value.JKeyValue.fromKeyValue(kv);
  }

  /**
   * Fluent builder for JKeyValue pairs.
   *
   * @see Value.JKeyValue
   */
  @NotNull
  @Contract(pure = true)
  static Value.JKeyValue jpair(@NotNull Value.JString key, @NotNull Value value) {
    return Value.JKeyValue.fromKeyValue(key, value);
  }

  /**
   * Fluent builder for JKeyValue pairs; this convenience method assumes the key string is
   * <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If you
   * want to start with raw JSON strings, with escaped special characters, then you need to use the
   * jsonString builder instead.
   *
   * @see Value.JKeyValue
   * @see Builders#jsonString(String)
   */
  @NotNull
  @Contract(pure = true)
  static Value.JKeyValue jpair(@NotNull String key, @NotNull Value value) {
    return jpair(javaString(key), value);
  }

  /**
   * Fluent builder for JKeyValue pairs; this convenience method assumes the key and value strings
   * are <b>unescaped</b> (i.e., that backslashed special characters have already been converted). If you
   * want to start with raw JSON strings, with escaped special characters, then you need to use the
   * jsonString builder instead.
   *
   * @see Value.JKeyValue
   * @see Builders#jsonString(String)
   */
  @NotNull
  @Contract(pure = true)
  static Value.JKeyValue jpair(@NotNull String key, @NotNull String value) {
    return jpair(javaString(key), javaString(value));
  }

  /**
   * Fluent builder for JArray.
   *
   * @see Value.JArray
   */
  @NotNull
  @Contract(pure = true)
  static Value.JArray jarray(Value... values) {
    return jarray(LazyList.fromArray(values));
  }

  /**
   * Fluent builder for JArray.
   *
   * @see Value.JArray
   */
  @NotNull
  @Contract(pure = true)
  static Value.JArray jarray(@NotNull IList<Value> values) {
    return Value.JArray.fromList(values);
  }

  /**
   * Fluent builder for JString.
   *
   * <p>JSON has very particular rules about backslashes. This method takes as input a JSON string.
   * The JSON string might, for example, have a backslash "escape" followed by an 'n' character, which you
   * might prefer to have "unescaped" to a single newline character. If this is the behavior you want,
   * then <code>jsonString</code> is the method for you.
   *
   * @param string
   *     a raw JSON string, with escapes, such as you might get from a text file on disk or from a
   *     network message
   * @return a JString corresponding to the input
   * @see Value.JString
   */
  @NotNull
  @Contract(pure = true)
  static Value.JString jsonString(@NotNull String string) {
    return Value.JString.fromEscapedString(string);
  }

  /**
   * Fluent builder for JString.
   *
   * <p>JSON has very particular rules about backslashes. This method takes as input a Java string.
   * The Java string is assumed to <b>already be unescaped</b>. For example, it might have actual
   * newline characters in it rather than a backslash followed by a 'n'. If that's what you have,
   * then <code>javaString</code> is the method for you.
   *
   * @param string
   *     a fully unescaped string, such as you might naturally deal with in Java
   * @return a JString corresponding to the input
   * @see Value.JString
   */
  @NotNull
  @Contract(pure = true)
  static Value.JString javaString(@NotNull String string) {
    return Value.JString.fromUnescapedString(string);
  }

  /**
   * Fluent builder for JNumber.
   *
   * @see Value.JNumber
   */
  @NotNull
  @Contract(pure = true)
  static Value.JNumber jnumber(double number) {
    return new Value.JNumber(number);
  }

  /**
   * Fluent builder for JBoolean.
   *
   * @see Value.JBoolean
   */
  @NotNull
  @Contract(pure = true)
  static Value.JBoolean jboolean(boolean bool) {
    return Value.JBoolean.fromBoolean(bool);
  }

  /**
   * Fluent builder for JNull.
   *
   * @see Value.JNull
   */
  @NotNull
  @Contract(pure = true)
  static Value.JNull jnull() {
    return Value.JNull.make();
  }
}
