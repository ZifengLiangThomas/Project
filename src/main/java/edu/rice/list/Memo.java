/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.list;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Memoization support.
 */
public class Memo<T> {
  // Engineering note: this is the only class, in the entirety of the Comp215 code library, that stores 'null' values.
  // We use this to make sure that we only call the supplier once, and thereafter forget about it and just return
  // the value that's saved in the contents field. It's important to drop our reference to the supplier so it
  // can be garbage collected after we're done with it.

  @Nullable
  private T contents;
  @Nullable
  private Supplier<T> supplier;

  /**
   * Build a memo with the lambda that supplies a value. The supplier will not be invoked until the {@link #get()} method
   * is called. The result of the supplier will be saved internally thereafter.
   */
  public Memo(@Nullable Supplier<T> supplier) {
    contents = null;
    this.supplier = supplier;
  }

  /**
   * This method will return the value that the supplier, used when the Memo was constructed, supplies.
   * The very first time get() is called, the supplier will be invoked. After that, the results are cached.
   */
  @NotNull
  @Contract(pure = true)
  public T get() {
    // If we've already computed the answer, then we'll just return it.
    if (contents != null) {
      return contents;
    }

    // At this point, we're going to have to fetch the result from the supplier-lambda. Comp215 students might
    // wish to ignore the "synchronized" keyword here, but it solves a concurrency problem we won't see until
    // the last week or two of the semester. All this craziness deals with the unfortunate case when we might
    // happen to have two separate threads here at the exact same time and we only want to do this work once.
    synchronized (this) {
      if (supplier != null) {
        contents = supplier.get();
        supplier = null;
      }

      // unfortunately, we can't make an up-front annotation that the supplier is required to return
      // a @NonNull value, which complicates things here because we want this method to be @NotNull.
      // We solve this by checking below and throwing a runtime exception if the Supplier was misbehaving.
      if (contents == null) {
        throw new NullPointerException("memo's supplier returned null contents!");
      }

      return contents;
    }
  }
}
