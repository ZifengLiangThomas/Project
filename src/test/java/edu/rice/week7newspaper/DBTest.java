/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week7newspaper;

import edu.rice.util.Option;
import edu.rice.util.Pair;
import edu.rice.io.Files;
import edu.rice.json.Value;
import edu.rice.list.IList;
import edu.rice.tree.IMap;
import org.junit.Test;

import static edu.rice.json.Builders.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class DBTest {
  @Test
  public void testBuildAuthorDB() throws Exception {
    Value.JObject jObject = jobject(
        jpair("authors",
            jarray(
                jobject(
                    jpair("name", "Alice"),
                    jpair("email", "alice@alice")),
                jobject(
                    jpair("name", "Bob"),
                    jpair("email", "bob@bob")))));
    Option<IMap<String, DB.Author>> oresult = DB.makeAuthorDB(jObject);

    assertTrue(oresult.isSome());
    assertEquals(Option.some("Alice"), oresult.get().oget("alice@alice").map(author -> author.name));
    assertEquals(Option.some("Bob"), oresult.get().oget("bob@bob").map(author -> author.name));
  }

  @Test
  public void testLoad() throws Exception {
    String thresherJson = Files.readResource("thresher.json").getOrElse("failed!");
    assertNotEquals("failed!", thresherJson);
    Option<Pair<IMap<String, DB.Author>, IList<DB.Article>>> oresult = DB.load(thresherJson);

    assertTrue(oresult.isSome());
    assertEquals(3, oresult.get().b.length()); // three articles
  }
}
