/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.tree;

import org.junit.Test;

public class TreapTest {
  @Test
  public void testInsertSimple() throws Exception {
    TreeSuite.testInsertSimple(Treap.makeEmpty());
  }

  @Test
  public void testRemoveSimple() throws Exception {
    TreeSuite.testRemoveSimple(Treap.makeEmpty());
  }

  @Test
  public void testGreaterThanSimple() throws Exception {
    TreeSuite.testGreaterThanSimple(Treap.makeEmpty());
  }

  @Test
  public void testLessThanSimple() throws Exception {
    TreeSuite.testLessThanSimple(Treap.makeEmpty());
  }

  @Test
  public void testInsertList() throws Exception {
    TreeSuite.testInsertList(Treap.makeEmpty());
  }

  @Test
  public void testInorder() throws Exception {
    TreeSuite.testInorder(Treap.makeEmpty());
  }

  @Test
  public void testToList() throws Exception {
    TreeSuite.testToList(Treap.makeEmpty());
  }

  @Test
  public void testRemove() throws Exception {
    TreeSuite.testRemove(Treap.makeEmpty());
  }

  @Test
  public void testRange() throws Exception {
    TreeSuite.testRange(Treap.makeEmpty());
  }

  @Test
  public void testSize() throws Exception {
    TreeSuite.testSize(Treap.makeEmpty());
  }

  // two treaps with the same values will convert to very different strings, because of the randomness of their insertion,
  // so testing for equivalence of their toString() methods, or calling equals(), which does deep structural equality,
  // will not yield passing tests.

//  @Test
//  public void testEquals() throws Exception {
//    TreeSuite.testEquals(Treap.makeEmpty());
//  }

//  @Test
//  public void testToString() throws Exception {
//    TreeSuite.testToString(Treap.makeEmpty());
//  }

  @Test
  public void testRemoveMin() throws Exception {
    TreeSuite.testRemoveMin(Treap.makeEmpty());
  }

  @Test
  public void testMaxDepth() throws Exception {
    TreeSuite.testMaxDepth(Treap.makeEmpty());
  }

  @Test
  public void testMaxDepth2() throws Exception {
    // insert 1000 sequential numbers, expected 5 <= maxDepth <= 30
    TreeSuite.testMaxDepth2(Treap.makeEmpty(), 1000, 5, 30);

    // Note to the student: if this test failed, then your treap isn't doing the probabilistic rebalancing.
  }

  @Test
  public void testPerformance() throws Exception {
    TreeSuite.testPerformance("treap", Treap.makeEmpty());
  }
}
