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

import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BinaryHeapTest {

  @Test
  public void test1() throws Exception {
    BinaryHeap<Integer> heap1 = BinaryHeap.of(5, 2, 9, 7, 3, 10, 100, 4, -2);
    BinaryHeap<Integer> heap2 = BinaryHeap.of(5, 2, 9, 7, 5, 10, 100, 5, -2); // note that "5" occurs three times

    assertEquals(9, heap1.size());
    assertEquals(9, heap2.size());
    assertTrue(heap1.validHeap());
    assertTrue(heap2.validHeap());

    assertEquals(-2, (int) heap1.getMin());
    assertEquals(-2, (int) heap2.getMin());
    assertTrue(heap1.validHeap());
    assertTrue(heap2.validHeap());

    assertEquals(2, (int) heap1.getMin());
    assertEquals(2, (int) heap2.getMin());
    assertTrue(heap1.validHeap());
    assertTrue(heap2.validHeap());

    assertEquals(3, (int) heap1.getMin());
    assertEquals(5, (int) heap2.getMin());
    assertTrue(heap1.validHeap());
    assertTrue(heap2.validHeap());

    assertEquals(4, (int) heap1.getMin());
    assertEquals(5, (int) heap2.getMin());
    assertTrue(heap1.validHeap());
    assertTrue(heap2.validHeap());

    assertEquals(5, (int) heap1.getMin());
    assertEquals(5, (int) heap2.getMin());
    assertTrue(heap1.validHeap());
    assertTrue(heap2.validHeap());

    assertEquals(7, (int) heap1.getMin());
    assertEquals(7, (int) heap2.getMin());
    assertTrue(heap1.validHeap());
    assertTrue(heap2.validHeap());

    assertEquals(3, heap1.size());
    assertEquals(3, heap2.size());
    assertTrue(heap1.validHeap());
    assertTrue(heap2.validHeap());

    BinaryHeap<Integer> heap3 = BinaryHeap.of(5, 2, 9, 7, 3, 10, 100, 4, -2);
    // we'll verify the toString() method while we're at it: note that we're getting the internal order, not in-order
    assertEquals("PriorityQueue(-2, 2, 9, 3, 5, 10, 100, 7, 4)", heap3.toString());
    // toString() shouldn't change the internal state of the heap
    assertEquals("PriorityQueue(-2, 2, 9, 3, 5, 10, 100, 7, 4)", heap3.toString());

    // now we're going to extract eight things, leaving one behind
    assertEquals(List.of(-2,2,3,4,5,7,9,10), LazyList.generate(heap3::getMin).limit(8));
    assertEquals("PriorityQueue(100)", heap3.toString());

    // now we stress-test generating the list when it empties out at the end
    BinaryHeap<Integer> heap4 = BinaryHeap.of(5, 2, 9, 7, 3, 10, 100, 4, -2);
    assertEquals("PriorityQueue(-2, 2, 9, 3, 5, 10, 100, 7, 4)", heap4.toString());
    assertEquals(List.of(-2,2,3,4,5,7,9,10,100), LazyList.generate(heap4::getMin).limit(9));
    assertEquals("PriorityQueue()", heap4.toString());

    // binary heaps are a convenient way to sort a list, right?
    IList<Integer> numbers = List.of(5, 2, 9, 7, 3, 10, 100, 4, -2);
    IPriorityQueue<Integer> heap = new BinaryHeap<>((a, b) -> a < b);
    numbers.foreach(heap::insert);
    IList<Integer> sortedNumbers = LazyList.generate(heap::getMin);
    assertEquals(List.of(-2,2,3,4,5,7,9,10,100), sortedNumbers);

    // now try the new list.sort() version
    IList<Integer> sortedNumbers2 = numbers.sort((a, b) -> a < b);
    assertEquals(List.of(-2,2,3,4,5,7,9,10,100), sortedNumbers2);
  }
}