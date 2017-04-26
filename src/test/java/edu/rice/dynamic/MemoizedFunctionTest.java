/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.dynamic;

import org.junit.Test;
import org.mockito.AdditionalAnswers;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class MemoizedFunctionTest {
  private final Function<Integer,Integer> incrementer = x -> x + 1;

  @Test
  public void makeBasics() throws Exception {
    final Function<Integer,Integer> memoizedIncrementer = MemoizedFunction.make(incrementer);
    assertEquals((Integer) 2, memoizedIncrementer.apply(1));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));
  }

  @Test
  public void makeOnlyOnce() throws Exception {
    // we'd ideally like a way to spy on a lambda directly, rather than doing this delegatesTo thing, but
    // Mockito doesn't support this yet, even in the newest versions. https://github.com/Eedanna/mockito/issues/481

    @SuppressWarnings("unchecked")
    Function<Integer, Integer> spyIncrementer = mock(Function.class, AdditionalAnswers.delegatesTo(incrementer));

    final Function<Integer,Integer> memoizedIncrementer = MemoizedFunction.make(spyIncrementer);

    verify(spyIncrementer, never()).apply(1);
    verify(spyIncrementer, never()).apply(2);
    verify(spyIncrementer, never()).apply(3);
    verify(spyIncrementer, never()).apply(4);

    assertEquals((Integer) 2, memoizedIncrementer.apply(1));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));
    assertEquals((Integer) 5, memoizedIncrementer.apply(4));

    verify(spyIncrementer, atMost(1)).apply(1);
    verify(spyIncrementer, never()).apply(2);
    verify(spyIncrementer, never()).apply(3);
    verify(spyIncrementer, atMost(1)).apply(4);
  }

  private final BiFunction<Function<Long, Long>, Long, Long> fibonacci = (self, n) -> {
    // 1 1 2 3 5 8 13 ...
    if (n < 2) {
      return 1L;
    } else {
      return self.apply(n - 1) + self.apply(n - 2);
    }
  };


  @Test
  public void makeRecursive() throws Exception {
    final Function<Long, Long> memoFibonacci = MemoizedFunction.makeRecursive(fibonacci);

    assertEquals((Long) 13L, memoFibonacci.apply(6L));
  }

  @Test
  public void makeRecursiveOnlyOnce() throws Exception {
    // we'd ideally like a way to spy on a lambda directly, rather than doing this delegatesTo thing, but
    // Mockito doesn't support this yet, even in the newest versions. https://github.com/Eedanna/mockito/issues/481

    @SuppressWarnings("unchecked")
    final BiFunction<Function<Long, Long>, Long, Long> spyFibonacci =
        mock(BiFunction.class, AdditionalAnswers.delegatesTo(fibonacci));

    final Function<Long, Long> memoFibonacci = MemoizedFunction.makeRecursive(spyFibonacci);

    verify(spyFibonacci, never()).apply(any(), any());

    assertEquals((Long) 13L, memoFibonacci.apply(6L));

    verify(spyFibonacci, atMost(1)).apply(any(), eq((Long) 0L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq((Long) 1L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq((Long) 2L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq((Long) 3L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq((Long) 4L));
    verify(spyFibonacci, atMost(1)).apply(any(), eq((Long) 5L));
  }

}