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
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.util.Option;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * A relatively pleasant wrapper around the entirely unpleasant java.util.regex.* library for a
 * variety of common use-case scenarios.
 */
public class Matcher {
  @SuppressWarnings("unused")
  private static final String TAG = "Matcher";
  @NotNull
  private final Pattern pattern;

  /**
   * Builds a regular expression matcher using the supplied regular expression.
   */
  public Matcher(@NotNull String regex) {
    this.pattern = Pattern.compile(regex);
  }

  /**
   * If you just want to find all the places in your input that match the regex, then this is the
   * method for you.
   */
  @NotNull
  @Contract(pure = true)
  public IList<String> getMatches(@NotNull String input) {
    java.util.regex.Matcher jmatcher = pattern.matcher(input);
    return LazyList.ogenerate(() -> {
      if (jmatcher.find()) {
        return Option.some(jmatcher.group());
      } else {
        return Option.none();
      }
    });
  }

  /**
   * If your regex has groups in it, this will find the first instance in your regex where it
   * matches and return a list of all the strings matching the corresponding groups.
   *
   * <p>Warning, per java.util.regex.Matcher.group: "If the match was successful but the group
   * specified failed to match any part of the input sequence, then null is returned. Note that some
   * groups, for example (a*), match the empty string. This method will return the empty string when
   * such a group successfully matches the empty string in the input."
   *
   * <p>If we get "null" back from java.util.regex.Matcher, we'll replace it with the empty string.
   * @see java.util.regex.Matcher#group(int)
   */
  @NotNull
  @Contract(pure = true)
  public IList<String> getGroupMatches(@NotNull String input) {
    java.util.regex.Matcher jmatcher = pattern.matcher(input);
    if (!jmatcher.find()) {
      return List.makeEmpty();
    }
    int numGroups = jmatcher.groupCount();
    if (numGroups == 0) {
      return List.makeEmpty();
    }
    return LazyList.rangeInt(1, numGroups).map((i) -> {
      String result = jmatcher.group(i);
      return (result == null) ? "" : result;
    });
  }
}
