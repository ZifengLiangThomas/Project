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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import edu.rice.list.IList;
import edu.rice.regex.NamedMatcher;

import static edu.rice.regex.NamedMatcher.*;

/**
 * This class takes a string and tokenizes it for a simple s-expression parser. edu.rice.regex.NamedMatcher
 * does all the heavy lifting.
 *
 * @see NamedMatcher
 */
interface Scanner {
  /**
   * Given an input string, returns a list of tokens.
   */
  @NotNull
  @Contract(pure = true)
  static IList<Token<SexprPatterns>> scan(@NotNull String input) {
    NamedMatcher<SexprPatterns> nm = new NamedMatcher<>(SexprPatterns.class);
    IList<Token<SexprPatterns>> tokens = nm.tokenize(input, new Token<>(SexprPatterns.FAIL, ""));
    return tokens
        .filter(x -> x.type != SexprPatterns.WHITESPACE); // remove whitespace tokens; we don't care about them
  }

  enum SexprPatterns implements TokenPatterns {
    OPEN("\\("),
    CLOSE("\\)"),
    WORD("\\w+"), // one or more letters, numbers, and underscores
    WHITESPACE("\\s+"),
    FAIL("");                    // if the matcher fails, you get one of these

    public final String pattern;

    SexprPatterns(String pattern) {
      this.pattern = pattern;
    }

    @Override
    @NotNull
    public String pattern() {
      return pattern;
    }
  }
}
