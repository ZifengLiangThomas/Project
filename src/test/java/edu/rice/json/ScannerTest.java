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
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ScannerTest {
  // This is an example of a completely legal JSON expression.
  private static final String bigJson = Files.readResource("bigJson.json").getOrElse("");

  // this sort of thing shows up sometimes in JavaScript programs, but is *not* legal JSON
  // and should be rejected.
  private static final String noQuotesJson = Files.readResource("bigJsonMalformed.json").getOrElse("");

  @Test
  public void testJsonExamples() throws Exception {
    assertTrue(!bigJson.equals("")); // make sure the file read operations succeeded
    assertTrue(!noQuotesJson.equals(""));

    final IList<NamedMatcher.Token<Scanner.JsonPatterns>> tokenList = Scanner.scan(bigJson);

    final IList<NamedMatcher.Token<Scanner.JsonPatterns>> expectedTokens = List.of(
        new NamedMatcher.Token<>(Scanner.JsonPatterns.OPENCURLY, "{"),
        new NamedMatcher.Token<>(Scanner.JsonPatterns.STRING, "itemCount"),
        new NamedMatcher.Token<>(Scanner.JsonPatterns.COLON, ":"),
        new NamedMatcher.Token<>(Scanner.JsonPatterns.NUMBER, "2"),
        new NamedMatcher.Token<>(Scanner.JsonPatterns.COMMA, ","),
        new NamedMatcher.Token<>(Scanner.JsonPatterns.STRING, "subtotal"),
        new NamedMatcher.Token<>(Scanner.JsonPatterns.COLON, ":"),
        new NamedMatcher.Token<>(Scanner.JsonPatterns.STRING, "$15.50"));

    // there are more tokens after this, so we're only testing that the first ones are what we expect
    assertEquals(expectedTokens, tokenList.limit(expectedTokens.length()));

    // now, switch to the input version that's missing quotation marks. This should cause the
    // lexer to fail to find a token on the first non-quoted string.

    final IList<NamedMatcher.Token<Scanner.JsonPatterns>> tokenListNoQuotes = Scanner.scan(noQuotesJson);
    final IList<NamedMatcher.Token<Scanner.JsonPatterns>> expectedTokensNoQuotes = List.of(
        new NamedMatcher.Token<>(Scanner.JsonPatterns.OPENCURLY, "{"),
        new NamedMatcher.Token<>(Scanner.JsonPatterns.FAIL, ""));

    assertEquals(expectedTokensNoQuotes, tokenListNoQuotes);

    final IList<NamedMatcher.Token<Scanner.JsonPatterns>> uglyNumbersExpected = List.of(
        new NamedMatcher.Token<>(Scanner.JsonPatterns.NUMBER, "2"),
        new NamedMatcher.Token<>(Scanner.JsonPatterns.FAIL, ""));

    final IList<NamedMatcher.Token<Scanner.JsonPatterns>> uglyNumbers = Scanner.scan("2 0000 33 11");
    assertEquals(uglyNumbersExpected, uglyNumbers);
  }

  @Test
  public void testBasicRegexs() throws Exception {
    final Pattern stringPattern = Pattern.compile(Scanner.JsonPatterns.STRING.pattern);

    final Pattern numberPattern = Pattern.compile(Scanner.JsonPatterns.NUMBER.pattern);
    final Pattern truePattern = Pattern.compile(Scanner.JsonPatterns.TRUE.pattern);
    final Pattern falsePattern = Pattern.compile(Scanner.JsonPatterns.FALSE.pattern);
    final Pattern nullPattern = Pattern.compile(Scanner.JsonPatterns.NULL.pattern);
    final Pattern openCurlyPattern = Pattern.compile(Scanner.JsonPatterns.OPENCURLY.pattern);

    assertFalse(stringPattern.matcher("hello").matches());
    assertTrue(stringPattern.matcher("\"hello\"").matches());
    assertTrue(stringPattern.matcher("\"hello, world\"").matches());
    assertTrue(stringPattern.matcher("\"hello, world\\n\"").matches());
    assertTrue(stringPattern.matcher("\"hello, \\\"world\\\"\\n\"").matches());
    assertFalse(numberPattern.matcher("hello").matches());
    assertTrue(numberPattern.matcher("93.2").matches());
    assertTrue(numberPattern.matcher("93").matches());
    assertTrue(numberPattern.matcher("-93").matches());
    assertTrue(numberPattern.matcher("-93e24").matches());
    assertTrue(numberPattern.matcher("-0.2e24").matches());
    assertFalse(numberPattern.matcher("-.2e24").matches());
    assertFalse(truePattern.matcher("Hello").matches());
    assertFalse(truePattern.matcher("false").matches());
    assertTrue(truePattern.matcher("true").matches());
    assertFalse(falsePattern.matcher("Hello").matches());
    assertFalse(falsePattern.matcher("true").matches());
    assertTrue(falsePattern.matcher("false").matches());
    assertFalse(nullPattern.matcher("Hello").matches());
    assertFalse(nullPattern.matcher("true").matches());
    assertTrue(nullPattern.matcher("null").matches());
    assertFalse(openCurlyPattern.matcher("Hello").matches());
    assertFalse(openCurlyPattern.matcher("t{ue").matches());
    assertTrue(openCurlyPattern.matcher("{").matches());
  }
}
