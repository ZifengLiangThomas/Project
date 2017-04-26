/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week5lab;

import edu.rice.list.KeyValue;
import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.tree.IMap;
import edu.rice.tree.TreapMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Week 5 lab assignment.
 */
public class FreqCount {
  /**
   * Given a string of text as input, this will tokenize the string into its component words in a
   * fairly simplistic way, by splitting on whitespace or punctuation. Capitalization is ignored as
   * well. The result will be a map from those words to the integer frequency of their occurrence.
   */
  @NotNull
  @Contract(pure = true)
  public static IMap<String, Integer> count(String input) {
    IList<String> inputList = LazyList
        .fromArray(input.split("[\\s,.;:]+"))
        .map(String::toLowerCase);

    return TreapMap.fromList(inputList, string -> 1, (a, b) -> a + b);
  }

  /**
   * Given a mapping from strings to integers, such as count() might return, return a list of
   * KeyValue tuples sorted from most frequent to least frequent.
   */
  @NotNull
  @Contract(pure = true)
  public static IList<KeyValue<String, Integer>> mostFrequent(IMap<String, Integer> freqMap) {
    return freqMap.toList().sort((kv1, kv2) -> kv1.getValue() > kv2.getValue());
  }
}
