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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import edu.rice.list.IList;
import edu.rice.regex.NamedMatcher;

import static edu.rice.regex.NamedMatcher.*;

/**
 * This class takes a string and tokenizes it for JSON. {@link NamedMatcher} does all the
 * heavy lifting. Note that String tokens coming from the tokenizer will <b>not</b> have
 * quotation marks around them. We remove those. They <b>may have escape characters</b> within,
 * which you'll want to deal with elsewhere.
 */
public interface Scanner {
  /**
   * Given a string, return a list of JSON tokens. If there's a failure in the tokenizer,
   * there will be a FAIL token at the point of the failure. Also note that whitespace tokens
   * are filtered out. You don't have to worry about them.
   * @see JsonPatterns#FAIL
   */
  @NotNull
  @Contract(pure = true)
  static IList<Token<JsonPatterns>> scan(@NotNull String input) {
    NamedMatcher<JsonPatterns> nm = new NamedMatcher<>(JsonPatterns.class);
    IList<Token<JsonPatterns>> tokens = nm.tokenize(input, new Token<>(JsonPatterns.FAIL, ""));

    return tokens
        .filter(x -> x.type != JsonPatterns.WHITESPACE) // remove whitespace tokens; we don't care about them
        .map(x -> (x.type == JsonPatterns.STRING)       // remove leading and trailing quotation marks from strings
            ? new Token<>(JsonPatterns.STRING, x.data.substring(1, x.data.length() - 1))
            : x);
  }

  enum JsonPatterns implements TokenPatterns {
    STRING("\"" +                 // a leading quotation mark followed by...
        "([^\"\\\\\\p{Cntrl}]" +  // any unicode char except " or \ or control char
        "|\\\\(" +                // or a backslash followed by one of:
        "([\"\\\\\"/bfnrt]" +     // - a series of acceptable single characters
        "|u[0123456789abcdefABCDEF]{4})" + // - or the code for a unicode quad-hex thing
        "))*\""),                 // zero or more of the things inside the quotation marks followed by "
    NUMBER("(-)?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?\\b"), // word boundary checker at the end
    TRUE("true"),
    FALSE("false"),
    NULL("null"),
    OPENCURLY("\\{"),
    CLOSECURLY("\\}"),
    COLON(":"),
    COMMA(","),
    OPENSQUARE("\\["),
    CLOSESQUARE("\\]"),
    WHITESPACE("[\\s]+"),
    FAIL("");                    // if the matcher fails, you get one of these

    public final String pattern;

    JsonPatterns(String pattern) {
      this.pattern = pattern;
    }

    @NotNull
    @Override
    public String pattern() {
      return pattern;
    }
  }
}
