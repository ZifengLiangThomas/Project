/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week2debug;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Basic test cases for the FrameBuffer. The @Test directives are commented out for now, but if you
 * turn them on, they'll fail. The purpose of the lecture corresponding to this code was to find and
 * fix the bugs.
 */
public class FrameBuffer1Test {

// @Test
  /**
   * First, verify we can plot a point.
   */
  public void testPlot() {
    FrameBuffer1 fb = new FrameBuffer1(10, 10);
    assertFalse(fb.isPlotted(3, 5));
    fb.plot(3, 5);
    assertTrue(fb.isPlotted(3, 5));
    assertFalse(fb.isPlotted(5, 3));

    System.out.println("Test Plot (3,5):");
    fb.print(System.out);
  }

// @Test
  /**
   * Basic test of line plotting - verify we can get the endpoints and median.
   */
  public void testLine() {
    FrameBuffer1 fb = new FrameBuffer1(10, 10);

    /* test if endpoints and midpoint are plotted */
    assertFalse(fb.isPlotted(1, 1));
    assertFalse(fb.isPlotted(6, 4));
    assertFalse(fb.isPlotted(3, 2));
    fb.line(1, 1, 6, 4);
    assertTrue(fb.isPlotted(1, 1));
    assertTrue(fb.isPlotted(6, 4));
    assertTrue(fb.isPlotted(3, 2));

    System.out.println("Test Line (1,1, 6,4):");
    fb.print(System.out);
  }
}
