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

import java.io.PrintStream;

/**
 * Original version of a simple line-drawing program with lots of bugs inside it.
 */
public class FrameBuffer1 {
  private char[] buffer;
  private int width;
  private int height;

  /**
   * Build a frame buffer of the given width and height.
   */
  public FrameBuffer1(int width, int height) {
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
    buffer[y * width + x] = '@';
  }

  /**
   * Check if a point has been plotted or not.
   */
  public boolean isPlotted(int x, int y) {
    return buffer[y * width + x] == '@';
  }

  /**
   * draw a line from (x0,y0)-(x1,y1).
   */
  public void line(int x0, int y0, int x1, int y1) {
    int xmid;
    int ymid;

    xmid = (x0 + x1) / 2;
    ymid = (y0 + y1) / 2;

    plot(xmid, ymid);

    if (x0 != xmid || y0 != ymid) {
      line(x0, y0, xmid, ymid);
    }
    if (x1 != xmid || y1 != ymid) {
      line(x1, y1, xmid, ymid);
    }
  }
}
