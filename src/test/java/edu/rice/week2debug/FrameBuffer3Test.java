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

import org.junit.Test;

import edu.rice.util.Log;

import static edu.rice.util.Performance.nanoBenchmark;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test cases for FrameBuffer3.  This time around, notice how we're much more serious
 * in testPlot about understanding what happens with off-screen points.
 */
public class FrameBuffer3Test {

  @Test
  public void testOffscreen() {
    FrameBuffer3 fb = new FrameBuffer3(10, 10);

    /*
     * basic tests of onscreen()
     */
    assertFalse(fb.offscreen(5, 5));
    assertFalse(fb.offscreen(0, 0));
    assertFalse(fb.offscreen(9, 9));

    assertTrue(fb.offscreen(-1, 1));
    assertTrue(fb.offscreen(1, -1));
    assertTrue(fb.offscreen(-1, -1));
    assertTrue(fb.offscreen(10, 1));
    assertTrue(fb.offscreen(1, 10));
    assertTrue(fb.offscreen(10, 10));
  }

  @Test
  public void testPlot() {
    FrameBuffer3 fb = new FrameBuffer3(10, 10);

    /*
     * make sure we can plot a point and have it
     * appear on-screen
     */
    assertFalse(fb.isPlotted(3, 4));
    fb.plot(3, 4);
    assertTrue(fb.isPlotted(3, 4));

    /*
     * now, what about off-screen points?  if we
     * plot something off the screen to the right,
     * then that might normally iwrap around onto the
     * next scan line.  Check for that explicitly.
     */
    assertFalse(fb.isPlotted(15, 4));
    fb.plot(13, 4); /* might map to (3,5) */
    assertFalse(fb.isPlotted(3, 5));

    /*
     * no point aside from 3,4 should have been plotted
     */
    for (int y = 0; y < 10; y++) {
      for (int x = 0; x < 10; x++) {
        /*
         * we could have written this as one logical
         * operation, but it would have been harder to
         * understand
         */
        if (x == 3 && y == 4) {
          assertTrue(fb.isPlotted(x, y));
        } else {
          assertFalse(fb.isPlotted(x, y));
        }
      }
    }

    System.out.println("testPlot:");
    fb.print(System.out);
  }

  @Test
  public void testLine() {
    FrameBuffer3 fb = new FrameBuffer3(10, 10);

        /* test if endpoints and midpoint are plotted */
    assertFalse(fb.isPlotted(1, 1));
    assertFalse(fb.isPlotted(6, 4));
    assertFalse(fb.isPlotted(3, 2));
    fb.line(1, 1, 6, 4);
    assertTrue(fb.isPlotted(1, 1));
    assertTrue(fb.isPlotted(6, 4));
    assertTrue(fb.isPlotted(3, 2));

    System.out.println("testLine:");
    fb.print(System.out);
  }

  @Test
  public void testPerformance() {
    FrameBuffer3 fb = new FrameBuffer3(1000, 1000);

    // disable logging output, otherwise too slow
    Log.setLogLevel(Log.NOTHING);


    System.out.println(String.format("Performance: %.3f µs / line",
        1.0e-7 * nanoBenchmark(() -> {
          for (int i = 0; i < 10000; i++) {
            // lots of different start/end pairs; we don't really care
            fb.line(i % 1000, (i * 3) % 1000, (i * 5) % 1000, (i * 7) % 1000);
          }
        })));

    // reenable logging out
    Log.setLogLevel(Log.ALL);

  }
}
