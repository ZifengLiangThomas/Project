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
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class SimpleScannerTest {

  @Test
  public void testSimpleScannerExamples() throws Exception {
    final IList<NamedMatcher.Token<Scanner.SexprPatterns>> tokenList = Scanner.scan("(add (multiply 3 4) 5)");
//        System.out.println(tokenList.toString());

    final IList<NamedMatcher.Token<Scanner.SexprPatterns>> expectedTokens = List.of(
        new NamedMatcher.Token<>(Scanner.SexprPatterns.OPEN, "("),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.WORD, "add"),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.OPEN, "("),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.WORD, "multiply"),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.WORD, "3"),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.WORD, "4"),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.CLOSE, ")"),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.WORD, "5"),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.CLOSE, ")"));

    assertEquals(expectedTokens, tokenList);

    //Let's also make sure that we deal with empty-sexprs properly

    final IList<NamedMatcher.Token<Scanner.SexprPatterns>> tokenList2 = Scanner.scan("(() hello)");
    final IList<NamedMatcher.Token<Scanner.SexprPatterns>> expectedTokens2 = List.of(
        new NamedMatcher.Token<>(Scanner.SexprPatterns.OPEN, "("),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.OPEN, "("),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.CLOSE, ")"),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.WORD, "hello"),
        new NamedMatcher.Token<>(Scanner.SexprPatterns.CLOSE, ")"));

    assertEquals(expectedTokens2, tokenList2);
  }

  @Test
  public void testBasicRegexs() throws Exception {
    final Pattern openPattern = Pattern.compile(Scanner.SexprPatterns.OPEN.pattern);
    final Pattern closePattern = Pattern.compile(Scanner.SexprPatterns.CLOSE.pattern);
    final Pattern wordPattern = Pattern.compile(Scanner.SexprPatterns.WORD.pattern);

    assertFalse(openPattern.matcher("[").matches());
    assertFalse(openPattern.matcher(")").matches());
    assertTrue(openPattern.matcher("(").matches());

    assertFalse(closePattern.matcher("]").matches());
    assertFalse(closePattern.matcher("(").matches());
    assertTrue(closePattern.matcher(")").matches());

    assertTrue(wordPattern.matcher("hello").matches());
    assertFalse(closePattern.matcher("27").matches());
  }
}