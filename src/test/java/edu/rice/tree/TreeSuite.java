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

import edu.rice.util.Pair;
import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.list.List;

import java.util.Random;

import static edu.rice.util.Performance.nanoBenchmark;
import static edu.rice.util.Performance.nanoBenchmarkVal;
import static org.junit.Assert.*;

/**
 * Static methods useful for testing Trees, Treaps, or anything else that implements ITree.
 * Typically you pass these the empty tree that's used for subsequent work.
 */
class TreeSuite {
  //
  // All of the "static" methods here are intended to be reused for testing both trees and treaps, which is
  // why they're all passed an empty tree as an argument. These should all pass for Tree, right away, and you'll
  // have to make sure they pass for Treap.
  //
  // Note the absence of @Test annotations here. These are called from TreeTest and TreapTest, which is where
  // you'll find the @Test annotations.
  //

  static void testInsertSimple(ITree<String> emptyTree) {
    // Write a unit test that inserts two strings into a tree, then queries the tree to see whether those
    // values are present. Also, very that a third string is absent from the tree.

    ITree<String> testTree = emptyTree.insert("Alice").insert("Bob");
    assertTrue(testTree.find("Alice").isSome());
    assertTrue(testTree.find("Bob").isSome());
    assertTrue(testTree.find("Charlie").isNone());

//    throw new RuntimeException("testInsert not implemented yet (project 4)");
  }

  static void testRemoveSimple(ITree<String> emptyTree) {
    // Write a unit test that inserts two strings into a tree, then removes one of them. Verify that
    // both strings are still present in the original tree, and that the post-removal tree is indeed
    // missing the value you removed.

    ITree<String> testTree = emptyTree.insert("Alice").insert("Bob").remove("Bob");
    assertTrue(testTree.find("Alice").isSome());
    assertTrue(testTree.find("Bob").isNone());
    assertTrue(testTree.find("Charlie").isNone());

//    throw new RuntimeException("testInsert not implemented yet (project 4)");
  }

  static void testGreaterThanSimple(ITree<String> emptyTree) {
    // Write a unit test that inserts five strings into the tree, then make two queries against the
    // tree, against one of those strings. Use "inclusive" for one query and don't use it for the other.
    // Verify that the results have the correct members.

    final IList<String> testVectorList = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final ITree<String> testTree = testVectorList.foldl(emptyTree, ITree::insert);
    final ITree<String> geqTree = testTree.greaterThan("Charlie", true);
    final ITree<String> gtrTree = testTree.greaterThan("Charlie", false);

    assertTrue(geqTree.find("Alice").isNone());
    assertTrue(geqTree.find("Bob").isNone());
    assertTrue(geqTree.find("Charlie").isSome());
    assertTrue(geqTree.find("Dorothy").isSome());
    assertTrue(geqTree.find("Eve").isSome());

    assertTrue(gtrTree.find("Alice").isNone());
    assertTrue(gtrTree.find("Bob").isNone());
    assertTrue(gtrTree.find("Charlie").isNone());
    assertTrue(gtrTree.find("Dorothy").isSome());
    assertTrue(gtrTree.find("Eve").isSome());

//    throw new RuntimeException("testInsert not implemented yet (project 4)");
  }

  static void testLessThanSimple(ITree<String> emptyTree) {
    // Same as GreaterThan, but do it for LessThan.

    final IList<String> testVectorList = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final ITree<String> testTree = testVectorList.foldl(emptyTree, ITree::insert);
    final ITree<String> leqTree = testTree.lessThan("Charlie", true);
    final ITree<String> lessTree = testTree.lessThan("Charlie", false);

    assertTrue(leqTree.find("Alice").isSome());
    assertTrue(leqTree.find("Bob").isSome());
    assertTrue(leqTree.find("Charlie").isSome());
    assertTrue(leqTree.find("Dorothy").isNone());
    assertTrue(leqTree.find("Eve").isNone());

    assertTrue(lessTree.find("Alice").isSome());
    assertTrue(lessTree.find("Bob").isSome());
    assertTrue(lessTree.find("Charlie").isNone());
    assertTrue(lessTree.find("Dorothy").isNone());
    assertTrue(lessTree.find("Eve").isNone());

//    throw new RuntimeException("testInsert not implemented yet (project 4)");
  }

  static void testInsertList(ITree<String> emptyTree) {
    IList<String> testVectorList = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    ITree<String> tree1 = emptyTree.insert("Alice").insert("Bob").insert("Charlie").insert("Dorothy").insert("Eve");
    ITree<String> tree2 = emptyTree.insertList(testVectorList);

    // we have to convert to a list, first, because treaps might have different memory layouts, but toList() does an
    // in-order traversal, which we expect to give us consistent results.
    assertEquals(tree1.toList().toString(), tree2.toList().toString());

    assertTrue(tree1.toList().equals(tree2.toList()));
    assertTrue(tree2.toList().equals(tree1.toList()));

    ITree<String> tree3 = tree1.removeList(List.of("Alice", "Charlie"));

    assertEquals(List.of("Bob", "Dorothy", "Eve"), tree3.toList());
  }


  static void testInorder(ITree<String> emptyTree) {
    IList<String> testVectorList = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    ITree<String> tree = emptyTree.insertList(testVectorList);

    StringBuilder result = new StringBuilder();
    tree.inorder(result::append);

    assertEquals("AliceBobCharlieDorothyEve", result.toString());
  }

  static void testToList(ITree<String> emptyTree) {
    final IList<String> testVectorList1 = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    final IList<String> testVectorList2 = List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve");
    final IList<String> testVectorList3 = List.of("Eve", "Bob", "Alice", "Dorothy");
    final ITree<String> tree1 = emptyTree.insertList(testVectorList1);
    final ITree<String> tree2 = emptyTree.insertList(testVectorList2);
    final ITree<String> tree3 = emptyTree.insertList(testVectorList3);

    final IList<String> elist = emptyTree.toList();
    final IList<String> list1 = tree1.toList();
    final IList<String> list2 = tree2.toList();
    final IList<String> list3 = tree3.toList();

    final IList<String> lelist = emptyTree.toLazyList();
    final IList<String> llist1 = tree1.toLazyList();
    final IList<String> llist2 = tree2.toLazyList();
    final IList<String> llist3 = tree3.toLazyList();

    // regular lists should be equal
    assertEquals(list1, list2);
    assertEquals(list2, list1);
    assertEquals(elist, elist);
    assertNotEquals(list1, elist);
    assertNotEquals(list1, list3);
    assertNotEquals(list3, list1);

    // lazy lists should also be equal
    assertEquals(llist1, llist2);
    assertEquals(llist2, llist1);
    assertEquals(lelist, lelist);
    assertNotEquals(llist1, lelist);
    assertNotEquals(llist1, llist3);
    assertNotEquals(llist3, llist1);

    // and the lists should equal each other
    assertEquals(elist, lelist);
    assertEquals(list1, llist1);
    assertEquals(list2, llist2);
    assertEquals(list3, llist3);
  }

  static void testRemove(ITree<String> emptyTree) {
    final IList<String> testVectorList = List.of("Charlie", "Hao", "Eve", "Gerald", "Bob", "Alice", "Frank", "Dorothy");
    final ITree<String> tree = emptyTree.insertList(testVectorList);
    final ITree<String> treeR1 = tree.remove("Alice");
    final ITree<String> treeR2 = tree.remove("Bob");
    final ITree<String> treeR3 = tree.remove("Gerald");

    assertEquals(List.of("Bob", "Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"), treeR1.toList());
    assertEquals(List.of("Alice", "Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"), treeR2.toList());
    assertEquals(List.of("Alice", "Bob", "Charlie", "Dorothy", "Eve", "Frank", "Hao"), treeR3.toList());
    assertEquals(emptyTree, emptyTree.remove("Alice"));
    assertEquals(tree, tree.remove("Nobody"));

    assertTrue(emptyTree.valid());
    assertTrue(tree.valid());
    assertTrue(treeR1.valid());
    assertTrue(treeR2.valid());
    assertTrue(treeR3.valid());
  }

  static void testRange(ITree<String> emptyTree) {
    // first, do it for trees
    IList<String> testVectorList = List.of("Charlie", "Hao", "Eve", "Gerald", "Bob", "Alice", "Frank", "Dorothy");
    ITree<String> tree = emptyTree.insertList(testVectorList);

    assertEquals(List.of("Alice", "Bob", "Charlie"), tree.lessThan("Charlie", true).toList());
    assertEquals(List.of("Alice", "Bob"), tree.lessThan("Charlie", false).toList());
    assertEquals(List.of("Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"),
        tree.greaterThan("Charlie", true).toList());
    assertEquals(List.of("Dorothy", "Eve", "Frank", "Gerald", "Hao"), tree.greaterThan("Charlie", false).toList());
    assertEquals(List.of("Dorothy", "Eve", "Frank"),
        tree.greaterThan("Charlie", false).lessThan("Frank", true).toList());

    ITree<String> tmp;
    tmp = tree.lessThan("Charlie", true);
    assertEquals(List.of("Alice", "Bob", "Charlie"), tmp.toList());
    assertTrue(tmp.valid());

    tmp = tree.lessThan("Charlie", false);
    assertEquals(List.of("Alice", "Bob"), tmp.toList());
    assertTrue(tmp.valid());

    tmp = tree.greaterThan("Charlie", true);
    assertEquals(List.of("Charlie", "Dorothy", "Eve", "Frank", "Gerald", "Hao"), tmp.toList());
    assertTrue(tmp.valid());

    tmp = tree.greaterThan("Charlie", false);
    assertEquals(List.of("Dorothy", "Eve", "Frank", "Gerald", "Hao"), tmp.toList());
    assertTrue(tmp.valid());

    tmp = tree.greaterThan("Charlie", false).lessThan("Frank", true);
    assertEquals(List.of("Dorothy", "Eve", "Frank"), tmp.toList());
    assertTrue(tmp.valid());
  }

  static void testEquals(ITree<String> emptyTree) {
    final IList<String> testVectorList1 = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    final IList<String> testVectorList2 = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    final IList<String> testVectorList3 = List.of("Eve", "Bob", "Alice", "Dorothy");
    final ITree<String> tree1 = emptyTree.insertList(testVectorList1);
    final ITree<String> tree2 = emptyTree.insertList(testVectorList2);
    final ITree<String> tree3 = emptyTree.insertList(testVectorList3);

    //noinspection EqualsWithItself
    assertTrue(emptyTree.equals(emptyTree));
    assertTrue(tree1.equals(tree2));
    assertTrue(tree2.equals(tree1));
    assertFalse(emptyTree.equals(tree1));
    assertFalse(tree1.equals(emptyTree));
    assertFalse(tree1.equals(tree3));
  }

  static void testSize(ITree<String> emptyTree) {
    IList<String> testVectorList = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    ITree<String> tree = emptyTree.insertList(testVectorList);

    assertEquals(emptyTree.size(), 0);
    assertEquals(tree.size(), 5);
  }

  static void testToString(ITree<String> emptyTree) {
    IList<String> testVectorList = List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy");
    ITree<String> tree = emptyTree.insertList(testVectorList);

    assertEquals("Tree(Tree(Tree(\"Alice\"), \"Bob\", Tree()), \"Charlie\", Tree(Tree(\"Dorothy\"), \"Eve\", Tree()))",
        tree.toString());
  }

  static void testRemoveMin(ITree<String> emptyTree) {
    ITree<String> tree = emptyTree.insertList(List.of("Charlie", "Eve", "Bob", "Alice", "Dorothy"));
    Pair<String, ITree<String>> failure = new Pair<>("Fail", emptyTree);

    ITree<String> resultTree = tree.removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          assertEquals("Alice", minVal);
          assertFalse(remainingTree.empty());
          return remainingTree;
        })
        .removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          assertEquals("Bob", minVal);
          assertFalse(remainingTree.empty());
          return remainingTree;
        })
        .removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          assertEquals("Charlie", minVal);
          assertFalse(remainingTree.empty());
          return remainingTree;
        })
        .removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          assertEquals("Dorothy", minVal);
          assertFalse(remainingTree.empty());
          return remainingTree;
        })
        .removeMin().getOrElse(failure)
        .match((minVal, remainingTree) -> {
          assertEquals("Eve", minVal);
          assertTrue(remainingTree.empty());
          return remainingTree;
        });

    assertFalse(resultTree.removeMin().isSome());
  }

  static void testMaxDepth(ITree<String> emptyTree) {
    assertEquals(0, emptyTree.maxDepth());
    ITree<String> oneElem = emptyTree.insert("Hello");
    assertEquals(1, oneElem.maxDepth());
    ITree<String> twoElem = oneElem.insert("Rice");
    assertEquals(2, twoElem.maxDepth());

    // once we insert a third element, we might have a two-level tree or we might have a three-level tree, depending
    // on how the balancing went
    ITree<String> threeElem = twoElem.insert("Owls!");
    int depth = threeElem.maxDepth();
    assertTrue(depth == 2 || depth == 3);
  }

  static void testMaxDepth2(ITree<Integer> emptyTree, int ninserts, int minDepth, int maxDepth) {
    ITree<Integer> tree = emptyTree.insertList(LazyList.rangeInt(1, ninserts));

    int depth = tree.maxDepth();
    assertTrue("If this test fails, then your tree depth is too small; something is very wrong with your tree",
        depth >= minDepth);
    assertTrue("If this test fails, then your tree depth is too large; your tree rebalancing isn't working",
        depth <= maxDepth);
  }

  static void testPerformance(String name, ITree<Integer> emptyTree) {
    System.out.println("=========== " + name + " performance =========== ");
    // first, we'll insert random numbers; performance should be similar
    final Random random = new Random();
    final IList<Integer>
        numberList =
        LazyList.generate(random::nextInt).limit(1000000).force(); // one million random numbers
    final IList<Integer> numberList10K = numberList.limit(10000).force();
    final IList<Integer> integers10K = LazyList.rangeInt(0, 9999).force();

    System.out.println(String.format(" 1M random     inserts: %7.3f μs per insert",
        1e-9 * nanoBenchmark(() -> emptyTree.insertList(numberList))));

    System.out.println(String.format("10K random     inserts: %7.3f μs per insert",
        1e-7 * nanoBenchmark(() -> emptyTree.insertList(numberList10K))));

    nanoBenchmarkVal(
        () -> emptyTree.insertList(integers10K))
        .consume((time, result) -> {
          System.out.println(String.format("10K sequential inserts: %7.3f μs per insert", 1e-7 * time));
          assertTrue(result.valid());
        });
  }

  static void testFoo() throws Exception {
    ITree<String> tree = Tree.of("Alice", "Bob", "Charlie", "Dorothy", "Eve", "Frank", "Geoff", "Hao");
    IList<String> firstThree = tree.toLazyList().limit(3);
  }
}
