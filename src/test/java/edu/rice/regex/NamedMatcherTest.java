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
import edu.rice.list.List;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NamedMatcherTest {

  @Test
  public void testGetNamedGroupMatches() throws Exception {
    NamedMatcher<SimpleTokenPatterns> nm = new NamedMatcher<>(SimpleTokenPatterns.class);
    IList<NamedMatcher.Token<SimpleTokenPatterns>> results =
        nm.tokenize("{ hello = fun; world=aw3some; }", new NamedMatcher.Token<>(SimpleTokenPatterns.FAIL, ""));

    IList<NamedMatcher.Token<SimpleTokenPatterns>> expectedResult = List.of(
        new NamedMatcher.Token<>(SimpleTokenPatterns.OPENCURLY, "{"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.WORD, "hello"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.EQUALS, "="),
        new NamedMatcher.Token<>(SimpleTokenPatterns.WORD, "fun"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.SEMICOLON, ";"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.WORD, "world"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.EQUALS, "="),
        new NamedMatcher.Token<>(SimpleTokenPatterns.WORD, "aw3some"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.SEMICOLON, ";"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.CLOSECURLY, "}"));

    assertEquals(expectedResult, results.filter((token) -> token.type != SimpleTokenPatterns.WHITESPACE));
  }

  @Test
  public void testGetNamedGroupMatchesWithFail() throws Exception {
    NamedMatcher<SimpleTokenPatterns> nm = new NamedMatcher<>(SimpleTokenPatterns.class);
    IList<NamedMatcher.Token<SimpleTokenPatterns>> results =
        nm.tokenize("{ hello = fun; !!! world=aw3some; }", new NamedMatcher.Token<>(SimpleTokenPatterns.FAIL, ""));

    IList<NamedMatcher.Token<SimpleTokenPatterns>> expectedResult = List.of(
        new NamedMatcher.Token<>(SimpleTokenPatterns.OPENCURLY, "{"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.WORD, "hello"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.EQUALS, "="),
        new NamedMatcher.Token<>(SimpleTokenPatterns.WORD, "fun"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.SEMICOLON, ";"),
        new NamedMatcher.Token<>(SimpleTokenPatterns.FAIL, ""));

    assertEquals(expectedResult, results.filter((token) -> token.type != SimpleTokenPatterns.WHITESPACE));
  }

  private enum SimpleTokenPatterns implements NamedMatcher.TokenPatterns {
    OPENCURLY("\\{"),
    CLOSECURLY("\\}"),
    WHITESPACE("\\s+"),
    EQUALS("="),
    SEMICOLON(";"),
    WORD("\\p{Alnum}+"),
    FAIL("");

    public final String pattern;

    SimpleTokenPatterns(String pattern) {
      this.pattern = pattern;
    }

    // required by NamedMatcher
    @NotNull
    public String pattern() {
      return pattern;
    }
  }
}