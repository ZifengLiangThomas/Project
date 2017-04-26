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
import edu.rice.util.Option;
import org.junit.Test;

import static edu.rice.json.Builders.*;
import static edu.rice.json.Operations.*;
import static org.junit.Assert.*;

// Normally, we'd have to worry about these Option.get() calls failing, but in a testing situation, we
// *expect* them to succeed. If they fail, then JUnit makes the whole test fail, which is exactly what we want.
public class OperationsTest {
  @Test
  public void getPathTest() throws Exception {
    assertEquals("$10.00",
        ogetPath(ParserTest.BIG_COMPARISON, "items/0/price").get().asJString().toUnescapedString());
    assertEquals("$5.50",
        ogetPath(ParserTest.BIG_COMPARISON, "items/1/price").get().asJString().toUnescapedString());
    assertFalse(ogetPath(ParserTest.BIG_COMPARISON, "items/2/price").isSome());
    assertFalse(ogetPath(ParserTest.BIG_COMPARISON, "items/green/price").isSome());
    assertFalse(ogetPath(ParserTest.BIG_COMPARISON, "items/1/green").isSome());
    assertEquals((Double) 2.0, (Double) ogetPath(ParserTest.BIG_COMPARISON, "itemCount").get().asJNumber().get());
    assertFalse(ogetPath(ParserTest.BIG_COMPARISON, "itemCount/1").isSome());
  }

  @Test
  public void regexSearchTest() throws Exception {
    IList<Value> prices = getPathMatchesRegex(ParserTest.BIG_COMPARISON, List.of(".*", ".*", "price"));
    assertEquals(List.of(javaString("$10.00"), javaString("$5.50")), prices);
  }

  @Test
  public void testUpdate() throws Exception {
    Value bigComparison2 =
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
                        jpair("qty", jnumber(11))), // originally 1, we're adding 10 below

                    jobject(
                        jpair("title", "Javascript Pocket Reference"),
                        jpair("description", "Handy pocket-sized reference for the Javascript language"),
                        jpair("imageUrl", "/images/books/56789.gif"),
                        jpair("price", "$5.50"),
                        jpair("qty", jnumber(12)))))); // originally 2, we're adding 10 below

    Value tmp1 = updatePath(ParserTest.BIG_COMPARISON, "items/0/qty",
        oval -> oval.map(val -> jnumber(val.asJNumber().get() + 10))).get();

    Value tmp2 = updatePath(tmp1, "items/1/qty",
        oval -> oval.map(val -> jnumber(val.asJNumber().get() + 10))).get();

    assertEquals(bigComparison2, tmp2);
  }

  @Test
  public void testUpdateNewDepth() throws Exception {
    Value basics =
        jobject(
            jpair("itemCount", jnumber(2)),
            jpair("subtotal", javaString("$15.50")));

    Value testVal = updatePath(basics, "a/b/c/d",
        oval -> Option.some(javaString("Hello!")))
        .get();

    Value expected =
        jobject(
            jpair("itemCount", jnumber(2)),
            jpair("subtotal", javaString("$15.50")),
            jpair("a",
                jobject(
                    jpair("b",
                        jobject(
                            jpair("c",
                                jobject(
                                    jpair("d", "Hello!"))))))));

    assertEquals(expected, testVal);

    // should nuke the whole a/b/c/d stack
    Value nowRemoveItVal = updatePath(expected, "a", oval -> Option.none()).get();
    assertEquals(basics, nowRemoveItVal);
  }

  @Test
  public void testUpdatePathMatchesRegex() throws Exception {
    Value bigComparison2 =
        jobject(
            jpair("itemCount", jnumber(2)),
            jpair("subtotal", javaString("$15.50")),
            jpair("items",
                jarray(
                    jobject(
                        jpair("title", "THE BIG BOOK OF FOO"), // originally lower-case, we're forcing it to upper-case
                        jpair("description", "Bestselling book of Foo by A.N. Other"),
                        jpair("imageUrl", "/images/books/12345.gif"),
                        jpair("price", "$10.00"),
                        jpair("qty", jnumber(1))),

                    jobject(
                        jpair("title", "JAVASCRIPT POCKET REFERENCE"), // originally lower-case, we're forcing it to upper-case
                        jpair("description", "Handy pocket-sized reference for the Javascript language"),
                        jpair("imageUrl", "/images/books/56789.gif"),
                        jpair("price", "$5.50"),
                        jpair("qty", jnumber(2))))));

    Value testVal = updatePathMatchesRegex(ParserTest.BIG_COMPARISON,
        List.of(".*", ".*", "title"),
        oval -> oval.map(val -> javaString(val.asJString().toUnescapedString().toUpperCase())))
        .get();

    assertEquals(bigComparison2, testVal);

    // nothing should actually match this time, so the result should be unchanged
    Value testVal2 = updatePathMatchesRegex(ParserTest.BIG_COMPARISON,
        List.of("foo", "bar", "baz", "whee"),
        oval -> oval.map(val -> javaString(val.asJString().toUnescapedString().toUpperCase())))
        .get();

    assertEquals(ParserTest.BIG_COMPARISON, testVal2);
  }

  @Test
  public void testMissing() throws Exception {
    // First, we're starting a a simple object having one name/value pair ("name" -> "name"). We're going to update this
    // to add another name ("field" -> null). The first assertion, as part of the update, asserts that there was nothing
    // previously there for the "field" name. The second assertion verifies that the new thing that we inserted landed
    // where we expected.

    Option<Value> obj = updatePath(
        jobject(jpair("name", "name")),
        "field",
        val -> {
          assertTrue(!val.isSome());
          return Option.some(jnull());
        });

    assertEquals(
        jobject(
            jpair("name", "name"),
            jpair("field", jnull())),
        obj.get());
  }
}
