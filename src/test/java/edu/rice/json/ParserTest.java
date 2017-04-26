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

import edu.rice.io.Files;
import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.regex.NamedMatcher;
import edu.rice.util.Option;
import org.junit.Test;

import static edu.rice.json.Builders.*;
import static edu.rice.json.Value.*;
import static edu.rice.json.Parser.*;
import static org.junit.Assert.*;

public class ParserTest {
  @Test
  public void simpleParserNullProductionTest() throws Exception {
    // first, some tests that should succeed

    JNull jnullExpected = jnull();
    IList<NamedMatcher.Token<Scanner.JsonPatterns>> emptyList = List.makeEmpty();
    Option<Result<Value>> jnullScanned = makeNull(Scanner.scan("null"));

    // Normally, we'd have to worry about these Option.get() calls failing, but in a testing situation, we
    // *expect* them to succeed. If they fail, then JUnit makes the whole test fail, which is exactly what we want.
    assertEquals(jnullExpected, jnullScanned.get().production);
    assertEquals(emptyList, jnullScanned.get().tokens);

    Option<Result<Value>> multiNulls = makeNull(Scanner.scan("null null null"));
    assertEquals(jnullExpected, multiNulls.get().production);
    assertEquals(
        List.of(
            new NamedMatcher.Token<>(Scanner.JsonPatterns.NULL, "null"),
            new NamedMatcher.Token<>(Scanner.JsonPatterns.NULL, "null")),
        multiNulls.get().tokens);

    // now, some tests that should fail: if you try to run the JNull production on a token-list that starts with
    // something other than a null, then it should return Option.none()
    Option<Result<Value>> trueNullNull = makeNull(Scanner.scan("true null null"));
    assertFalse(trueNullNull.isSome());

    Option<Result<Value>> emptyStringParsed = makeNull(Scanner.scan(""));
    assertFalse(emptyStringParsed.isSome());
  }

  static final String BIG_JSON = Files.readResource("bigJson.json").getOrElse("");

  // basically, this is what we expect when we try to pretty-print BIG_COMPARISON:
  // - object key/values are sorted by key
  // - optional backslashes are required
  static final String BIG_JSON_NORMALIZED = Files.readResource("bigJsonNormalized.json").getOrElse("");

  static final JObject BIG_COMPARISON =
      jobject(
          jpair("itemCount", jnumber(2)),
          jpair("subtotal", javaString("$15.50")),
          jpair("items",
              jarray(
                  jobject(
                      jpair("title", "The Big Book of Foo"),
                      jpair("description", "Bestselling book of Foo by A.N. Other"),
                      jpair("imageUrl", "/images/books/12345.gif"),
                      jpair("price", "$10.00"),
                      jpair("qty", jnumber(1))),

                  jobject(
                      jpair("title", "Javascript Pocket Reference"),
                      jpair("description", "Handy pocket-sized reference for the Javascript language"),
                      jpair("imageUrl", "/images/books/56789.gif"),
                      jpair("price", "$5.50"),
                      jpair("qty", jnumber(2))))));


  static final String BASIC_OBJECT_JSON = "{\n" +
      "  \"x\" : 1,\n" +
      "  \"y\" : 2\n" +
      "}";

  @Test
  public void basicTest() throws Exception {
    Option<JObject> oresult = parseJsonObject(BASIC_OBJECT_JSON);
    assertTrue(oresult.isSome());

    JObject result = oresult.get();

    assertEquals("1", result.oget("x").map(Object::toString).getOrElse("fail"));
    assertEquals("2", result.oget("y").map(Object::toString).getOrElse("fail"));

    assertEquals("{ \"x\": 1, \"y\": 2 }", result.toString());

    assertTrue(!BIG_JSON.equals(""));
    assertTrue(!BIG_JSON_NORMALIZED.equals(""));

    Option<JObject> oresult2 = parseJsonObject(BIG_JSON);
    assertTrue(oresult2.isSome());
    JObject result2 = oresult2.get();
    Value items = result2.oget("items").getOrElseThrow(() -> new RuntimeException("failed to get items"));
    assertTrue(items instanceof JArray);

    IList<Value> itemList = items.asJArray().getList();

    Value item1 = itemList.nth(0).get();
    assertTrue(item1 instanceof JObject);

    JObject result3 = item1.asJObject();

    assertEquals("The Big Book of Foo", result3.oget("title").map(x -> x.asJString().toUnescapedString()).getOrElse("fail"));
    assertEquals("\"The Big Book of Foo\"", result3.oget("title").map(x -> x.asJString().toString()).getOrElse("fail"));
    assertEquals("$10.00", result3.oget("price").map(x -> x.asJString().toUnescapedString()).getOrElse("fail"));
    assertEquals("\"$10.00\"", result3.oget("price").map(x -> x.asJString().toString()).getOrElse("fail"));
    assertEquals(1.0, result3.oget("qty").map(x -> x.asJNumber().get()).getOrElse(Double.NaN), 0.01);

    // how you might write it all in one go... Note all the awful type casting!
    assertEquals(((JString) result3.oget("title").get()).toUnescapedString(),
        ((JString)
            ((JObject)
                ((JArray) result2.oget("items").get())
                    .getList().head())
                .getMap().oget("title").get()).toUnescapedString());

    // a little method sugar to make the medicine go down
    assertEquals(
        result3.oget("title").map(x -> x.asJString().toUnescapedString()).getOrElse("absent title"),
        result2.oget("items")
            .map(x -> x
                .asJArray().getList() // returns an IList<Value>
                .head() // returns an Value
                .asJObject() // which we want to treat as a JObject
                .oget("title").map(y -> y // from which we'll extract a specific Value
                    .asJString() // which happens to be a JString, so cast it
                    .toUnescapedString())
                .getOrElse("another title fail")) // and finally extract the String within, with escapes fixed
            .getOrElse("items fail"));

    // let's test the asOJ*() methods
    assertEquals("The Big Book of Foo",
        result3.oget("title")
            .flatmap(Value::asOJString)
            .match(() -> "fail", JString::toUnescapedString));
    assertEquals("fail",
        result3.oget("title")
            .flatmap(Value::asOJBoolean)
            .match(() -> "fail", JBoolean::toString));

    assertEquals("fail",
        result3.oget("title")
            .flatmap(Value::asOJNull)
            .match(() -> "fail", JNull::toString));

    assertEquals("fail",
        result3.oget("title")
            .flatmap(Value::asOJArray)
            .match(() -> "fail", JArray::toString));

    assertEquals("fail",
        result3.oget("title")
            .flatmap(Value::asOJObject)
            .match(() -> "fail", JObject::toString));

    assertEquals("fail",
        result3.oget("title")
            .flatmap(Value::asOJNumber)
            .match(() -> "fail", JNumber::toString));
  }

  @Test
  public void indentationTest() throws Exception {
    // we should be able to convert BIG_COMPARISON back to be exactly the same as BIG_JSON
    assertEquals(BIG_JSON_NORMALIZED, BIG_COMPARISON.toIndentedString());
  }

  @Test
  public void builderTest() throws Exception {
    JObject basicObject = parseJsonObject(BASIC_OBJECT_JSON).get();
    JObject basicComparison =
        jobject(
            jpair("x", jnumber(1)),
            jpair("y", jnumber(2)));

    assertEquals(basicComparison, basicObject);


    JObject bigObject = parseJsonObject(BIG_JSON).get();
    assertEquals(BIG_COMPARISON, bigObject);

    JObject otherStuffObject = parseJsonObject("{ \"stuff\" : [ \"hello\", 23, true, false, null ] }").get();
    JObject otherStuffComparison =
        jobject(
            jpair("stuff", jarray(
                javaString("hello"),
                jnumber(23),
                jboolean(true),
                jboolean(false),
                jnull())));

    assertEquals(otherStuffComparison, otherStuffObject);
  }

  @Test
  public void escapedSlashTest() throws Exception {
    //
    // What's up with escaped forward slashes?
    //
    // http://stackoverflow.com/questions/1580647/json-why-are-forward-slashes-escaped
    // https://github.com/esnme/ultrajson/issues/110
    // https://code.google.com/p/json-simple/issues/detail?id=8
    //
    // In short, it's *allowed* to escape them, and there are some weird cases where it's *preferable*
    // to escape them, but it's also *allowed* not to escape them. Yeah, standards! This test checks
    // to make sure that, whether escaped or not, we still end up with the same result.
    //
    // What we're testing, really, is that we're using a single internal representation regardless of
    // the external differences.
    //
    JObject stringObjectWithSlashes = parseJsonObject("{ \"path\" : \"\\/foo\\/bar/baz\" }").get();
    JObject stringObjectWithSlashesComparison = jobject(jpair("path", "/foo/bar/baz"));

    assertEquals(stringObjectWithSlashesComparison, stringObjectWithSlashes);
  }

  @Test
  public void matcherTest() throws Exception {
    JObject peopleDB =
        jobject(
            jpair("Alice", jnumber(40)),
            jpair("Bob", "Yuck"),
            jpair("Charlie", jboolean(true)),
            jpair("Dorothy", jnumber(10)),
            jpair("Eve", jnull()));

    // filter out only the ones that have a jnumber (age)
    IList<Double> ages = peopleDB
        .getContents() // we're starting with IList<KeyValue<String,Value>>
        .oflatmap(kv -> kv.getValue().match(
            jObject -> Option.none(),
            jArray -> Option.none(),
            jString -> Option.none(),
            jNumber -> Option.some(jNumber.get()),
            jBoolean -> Option.none(),
            jNull -> Option.none()));

    assertEquals(2, ages.length());
    assertEquals(50.0, ages.foldl(0.0, (a, b) -> a + b), 0.01);
  }

  @Test
  public void testArrays() throws Exception {
    // these .get() methods will throw exceptions if the parser failed; we expect it to succeed
    JArray empty1 = parseJsonArray("[]").get();
    JArray empty2 = parseJsonArray("[  ]").get();
    JArray empty3 = parseJsonArray("   [   ]  ").get();

    assertEquals(0, empty1.getList().length());
    assertEquals(0, empty2.getList().length());
    assertEquals(0, empty3.getList().length());

    // now, for some various parsing failures
    assertFalse(parseJsonArray("[,]").isSome());
    assertFalse(parseJsonArray("[23,]").isSome());
    assertFalse(parseJsonArray("[,23]").isSome());
    assertFalse(parseJsonArray("[23,").isSome());
    assertFalse(parseJsonArray("[23").isSome());
    assertFalse(parseJsonArray("[23[").isSome());
    assertFalse(parseJsonArray("[23,94}").isSome());
    assertFalse(parseJsonArray("[23 94]").isSome());
    assertFalse(parseJsonArray("[23,94] false").isSome());

    // and, while we're at it, some recursive stuff
    assertEquals(jarray(jnumber(4), jnumber(2), jarray(jnumber(5), jnumber(3)), jnumber(9)),
        parseJsonArray("[4,2,[5,3],9]").get());
  }

  @Test
  public void testValues() throws Exception {
    // now, for some various parsing failures
    assertFalse(parseJsonValue("abcd1234").isSome());
    assertFalse(parseJsonValue("]").isSome());
    assertFalse(parseJsonValue("[").isSome());

    assertEquals(jboolean(true), parseJsonValue("true").get());
    assertEquals(jboolean(false), parseJsonValue("false").get());
    assertEquals(jnull(), parseJsonValue("null").get());
    assertEquals(jnumber(5.0), parseJsonValue("5").get());

    // parseJsonValue should be able to handle arrays and objects as well
    assertEquals(jarray(jnumber(4), jnumber(2), jarray(jnumber(5), jnumber(3)), jnumber(9)),
        parseJsonValue("[4,2,[5,3],9]").get());

    assertEquals(jobject( jpair("stuff", jarray( javaString("hello"), jnumber(23), jboolean(true), jboolean(false), jnull() ))),
        parseJsonValue("{ \"stuff\" : [ \"hello\", 23, true, false, null ] }").get());
  }
}
