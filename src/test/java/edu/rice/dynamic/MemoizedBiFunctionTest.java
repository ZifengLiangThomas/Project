package edu.rice.dynamic;

import edu.rice.util.TriFunction;
import org.junit.Test;
import org.mockito.AdditionalAnswers;

import java.util.function.BiFunction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MemoizedBiFunctionTest {
  private final BiFunction<Integer,Integer,Integer> adder = (x, y) -> x + y;

  @Test
  public void makeBasics() throws Exception {
    final BiFunction<Integer,Integer,Integer> memoizedAdder = MemoizedBiFunction.make(adder);
    assertEquals((Integer) 2, memoizedAdder.apply(1, 1));
    assertEquals((Integer) 5, memoizedAdder.apply(4, 1));
    assertEquals((Integer) 5, memoizedAdder.apply(4, 1));
  }

  @Test
  public void makeOnlyOnce() throws Exception {
    // we'd ideally like a way to spy on a lambda directly, rather than doing this delegatesTo thing, but
    // Mockito doesn't support this yet, even in the newest versions. https://github.com/Eedanna/mockito/issues/481

    @SuppressWarnings("unchecked")
    BiFunction<Integer,Integer,Integer> spyAdder = mock(BiFunction.class, AdditionalAnswers.delegatesTo(adder));

    final BiFunction<Integer,Integer,Integer> memoizedAdder = MemoizedBiFunction.make(spyAdder);

    verify(spyAdder, never()).apply(1, 1);
    verify(spyAdder, never()).apply(4, 1);
    verify(spyAdder, never()).apply(1, 4);
    verify(spyAdder, never()).apply(4, 4);

    assertEquals((Integer) 2, memoizedAdder.apply(1, 1));
    assertEquals((Integer) 5, memoizedAdder.apply(4, 1));
    assertEquals((Integer) 5, memoizedAdder.apply(4, 1));

    verify(spyAdder, atMost(1)).apply(1, 1);
    verify(spyAdder, atMost(1)).apply(4, 1);
    verify(spyAdder, never()).apply(1, 4);
    verify(spyAdder, never()).apply(4, 4);
  }

  final TriFunction<BiFunction<Integer,Integer,Long>,Integer,Integer,Long> pascalInternal = (self, level, offset) -> {
    if (offset == 0 || offset >= level || offset < 0 || level < 0) {
      return 1L;
    } else {
      return self.apply(level - 1, offset) + self.apply(level - 1, offset - 1);
    }
  };

  private static long factorial(long n) {
    if (n == 0) {
      return 1;
    }

    long accumulator = 1;

    for (long i = 2; i <= n; i++) {
      accumulator *= i;
    }

    return accumulator;
  }

  /**
   * Computes n-choose-r = n! / (n-r)!r! .
   */
  private static long choose(long n, long r) {
    return factorial(n) / (factorial(n - r) * factorial(r));
  }


  //
  // Pascal's triangle: https://en.wikipedia.org/wiki/Pascal%27s_triangle
  // T[0,0] = 1
  // T[level, 0] = 1
  // T[level, level] = 1
  // T[level, offset] = T[level - 1, offset] + T[level - 1, offset - 1]
  @Test
  public void pascalsTriangle() throws Exception {
    BiFunction<Integer,Integer,Long> pascal = MemoizedBiFunction.makeRecursive(pascalInternal);

    for (int n = 0; n < 10; n++) {
      for (int r = 0; r <= n; r++) {
        assertEquals((Long) choose(n, r), pascal.apply(n, r));
      }
    }
  }

  @Test
  public void spyPascal() throws Exception {
    @SuppressWarnings("unchecked")
    final TriFunction<BiFunction<Integer,Integer,Long>,Integer,Integer,Long> spyPascal =
        mock(TriFunction.class, AdditionalAnswers.delegatesTo(pascalInternal));
    final BiFunction<Integer,Integer,Long> pascal = MemoizedBiFunction.makeRecursive(spyPascal);

    verify(spyPascal, never()).apply(anyObject(), eq(0), eq(0));
    verify(spyPascal, never()).apply(anyObject(), eq(0), eq(1));
    verify(spyPascal, never()).apply(anyObject(), eq(1), eq(0));
    verify(spyPascal, never()).apply(anyObject(), eq(1), eq(1));

    for (int n = 0; n < 10; n++) {
      for (int r = 0; r <= n; r++) {
        assertEquals((Long) choose(n, r), pascal.apply(n, r));
      }
    }

    verify(spyPascal, atMost(1)).apply(anyObject(), eq(0), eq(0));
    verify(spyPascal, atMost(1)).apply(anyObject(), eq(0), eq(1));
    verify(spyPascal, atMost(1)).apply(anyObject(), eq(1), eq(0));
    verify(spyPascal, atMost(1)).apply(anyObject(), eq(1), eq(1));
  }

}