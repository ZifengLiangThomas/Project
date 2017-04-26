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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatcherTest {

  @Test
  public void testGetMatches() throws Exception {
    String test1 = "Hello, world, how are you doing? This is a beautiful day.";
    String re1 = "[hH]\\S*";   // starting with 'h' or 'H' then non-space chars

    Matcher matcher1 = new Matcher(re1);
    IList<String> results1 = matcher1.getMatches(test1);
    assertEquals(List.of("Hello,","how","his"), results1);
  }

  @Test
  public void testGetGroupMatches() throws Exception {
    // A test like this exercises our code, of course, but if you're learning a non-trivial concept like regular
    // expressions, it's helpful to write unit tests to test your *understanding* of the concept itself.

    String test2 = "dwallach:odfkjewhglx:24:97:/bin/tcsh:/Users/dwallach";
    String re2 = "([^:]*):([^:]*):(\\d+):(\\d+):([^:]*):([^:]*)";

    Matcher matcher2 = new Matcher(re2);
    IList<String> results2 = matcher2.getGroupMatches(test2);
    assertEquals(List.of("dwallach","odfkjewhglx","24","97","/bin/tcsh","/Users/dwallach"), results2);
  }
}