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

import edu.rice.util.Log;

import java.io.PrintStream;

/**
 * Final version of a simple line-drawing program.  Now, off-screen plotting is handled correctly.
 * Also, note the differences between FrameBuffer2 and FrameBuffer3.  You'll see that a subtle bug
 * in debug indentation was fixed.  A 'return' statement in lineHelper didn't fix the indentation.
 */
public class FrameBuffer3 {
  private static final String TAG = "fb3";
  private final char[] buffer;
  private final int width;
  private final int height;

  /**
   * Build a frame buffer of the given width and height.
   */
  public FrameBuffer3(int width, int height) {
        /* check that frame buffer size is sensible */
    if (width <= 0 || height <= 0) {
      throw new RuntimeException("width and height must be positive");
    }

    this.width = width;
    this.height = height;
    buffer = new char[width * height];

    for (int i = 0; i < width * height; i++) {
      buffer[i] = '.';
    }
  }

  /**
   * Print the contents of the frame buffer to the FILE interface (i.e., stdout, stderr).
   */
  public void print(PrintStream out) {
    int x;
    int y;
    int loc;

    loc = 0;
    for (y = 0; y < height; y++) {
      for (x = 0; x < width; x++) {
        out.print(buffer[loc++]);
      }
      out.println();
    }
  }

  /**
   * plot a point in the FrameBuffer.
   */
  public void plot(int x, int y) {
    Log.i(TAG, "plot(" + x + "," + y + ")");
    if (!offscreen(x, y)) {
      buffer[y * width + x] = '@';
    }
  }

  /**
   * Check if a point has been plotted or not.
   */
  public boolean isPlotted(int x, int y) {
    if (offscreen(x, y)) {
      return false;
    }
        /*
         * we could have thrown an exception, but it
         * makes some sense that off-screen points are,
         * by definition, not plotted
         */

    return buffer[y * width + x] == '@';
  }

  public boolean offscreen(int x, int y) {
    return x < 0 || y < 0 || x >= width || y >= height;
  }

  /**
   * draw a line from (x0,y0)-(x1,y1).
   */
  public void line(int x0, int y0, int x1, int y1) {
    Log.i(TAG, "line(" + x0 + "," + y0 + "->" + x1 + "," + y1 + ")");

        /* get the endpoints */
    plot(x0, y0);
    plot(x1, y1);

        /* recursively get everything in the middle */
    lineHelper(x0, y0, x1, y1);
  }

  private void lineHelper(int x0, int y0, int x1, int y1) {
    int xmid;
    int ymid;
    Log.i(TAG, "lineHelper(" + x0 + "," + y0 + "->" + x1 + "," + y1 + ")");

    xmid = (x0 + x1) / 2;
    ymid = (y0 + y1) / 2;

        /* if the midpoint is a point we've already seen, we're done */
    if ((xmid == x0 && ymid == y0) || (xmid == x1 && ymid == y1)) {
      return;
    }

        /* otherwise, plot the midpoint and recursively draw the line */
    plot(xmid, ymid);
    lineHelper(x0, y0, xmid, ymid);
    lineHelper(x1, y1, xmid, ymid);
  }
}
