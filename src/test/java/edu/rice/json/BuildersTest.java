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

import static org.junit.Assert.*;
import static edu.rice.json.Builders.*;

import edu.rice.list.List;
import org.junit.Test;

public class BuildersTest {
  @Test
  public void listsVsVarargs() throws Exception {
    assertEquals(jarray(jnull(), jboolean(true), jboolean(false)),
        jarray(List.of(jnull(), jboolean(true), jboolean(false))));
  }

  @Test
  public void jsonVsJavaString() throws Exception {
    assertEquals(javaString("simple"), jsonString("simple"));
    assertEquals(javaString("simple\n"), jsonString("simple\\n"));
    assertEquals(jpair("simple", jboolean(true)),
        jpair(jsonString("simple"), jboolean(true)));
    assertEquals(jpair("sim\nple", jboolean(true)),
        jpair(jsonString("sim\\nple"), jboolean(true)));
    assertEquals(jobject(jpair("sim\nple", jboolean(true))),
        jobject(jpair(jsonString("sim\\nple"), jboolean(true))));
  }
}