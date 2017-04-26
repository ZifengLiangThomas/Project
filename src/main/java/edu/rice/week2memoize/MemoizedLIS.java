package edu.rice.week2memoize;

import java.util.LinkedList;

public class MemoizedLIS {

  /**
   * recLIS calls lisHelper to determine the LIS for the input array a. Since this is the initial call
   * to lisHelper, the current array index is 0. The current high number is set to -1 to indicate no
   * array elements have been visited
   * @param a array of non-negative integers.
   * @return LIS list
   */
  public static LinkedList<Integer> recLIS(int[] a) {
    return lisHelper(-1, a, 0);
  }

  /**
   * lisHelper finds the LIS of a[i] to the end of array a.
   * @param k current high number seen so far.
   * @param a array of non-negative integers.
   * @param i index of array.
   * @return LIS list
   */
  private static LinkedList<Integer> lisHelper(int k, int[] a, int i) {
    LinkedList<Integer> l;

    // i is larger than array length
    if (i >= a.length) {
      return new LinkedList<>();
    } else {
      l = lisHelper(k, a, i + 1);

      // check if array index should be included in LIS list
      if (a[i] > k) {
        LinkedList<Integer> lprime = new LinkedList<>();
        lprime.addLast(i);
        lprime.addAll(lisHelper(a[i], a, i + 1));

        // keep the largest LIS list
        if (lprime.size() > l.size()) {
          l = lprime;
        }
      }
    }
    // return the largest LIS list
    return l;
  }
}
