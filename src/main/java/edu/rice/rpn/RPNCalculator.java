/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.rpn;

import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.List;
import edu.rice.regex.NamedMatcher;
import edu.rice.tree.IMap;
import edu.rice.tree.TreapMap;
import edu.rice.util.Log;
import edu.rice.util.Option;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static edu.rice.regex.NamedMatcher.*;

/**
 * The actual guts of the RPN Calculator go here.
 *
 * <p>Fun fact: "Polish notation" was indeed invented by a Polish mathematician
 * https://en.wikipedia.org/wiki/Jan_%C5%81ukasiewicz
 */
public class RPNCalculator {
  private static final IMap<RPNTokenPatterns, CalcOp> REGISTRY =
      TreapMap.of(
          KeyValue.make(RPNTokenPatterns.PLUS, RPNCalculator::add),
          KeyValue.make(RPNTokenPatterns.TIMES, RPNCalculator::multiply),
          KeyValue.make(RPNTokenPatterns.MINUS, RPNCalculator::subtract),
          KeyValue.make(RPNTokenPatterns.DIVIDE, RPNCalculator::divide),
          KeyValue.make(RPNTokenPatterns.DUP, RPNCalculator::dup),
          KeyValue.make(RPNTokenPatterns.DROP, RPNCalculator::drop),
          KeyValue.make(RPNTokenPatterns.SWAP, RPNCalculator::swap),
          KeyValue.make(RPNTokenPatterns.EQUALS, RPNCalculator::noop),
          KeyValue.make(RPNTokenPatterns.FAIL, RPNCalculator::fail),
          KeyValue.make(RPNTokenPatterns.CLEAR, RPNCalculator::clear));

  @SuppressWarnings("unused")
  private static final String TAG = "RPNCalculator";
  @NotNull
  private IList<Double> rpnStack; // we'll be mutating this, so not final!

  /**
   * Construct an instance of an RPN calculator. This will maintain internal state that evolves as its
   * asked to do computation.
   */
  public RPNCalculator() {
    rpnStack = List.makeEmpty(); // initially empty
  }

  //
  // And now, here are all the primitive functions that operate on optional stacks. Obviously,
  // if you're given an empty stack, there isn't much to do but return another one, thus the
  // flatmap calls. This structure means that all of these functions are UnaryOperators (and also
  // CalcOp's), which makes it easy to fold over them or otherwise use them with our functional lists.
  //
  // Fun fact: most of the methods here are package-scoped rather than private-scope. This makes
  // them accessible to unit tests from within the same package, which we need to get test coverage.
  //

  @NotNull
  @Contract(pure = true)
  static OStack add(@NotNull OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two elements replaced
    // with their sum.

    // if the input is none(), or if the stack doesn't have at least two elements,
    // the output will be none().

    return ostack.flatmap(
        stack -> stack.match(
            empty -> OStack.none(),
            (head, empty) -> OStack.none(),
            (head, second, tail) -> OStack.some(tail.add(head + second))));
  }

  @NotNull
  @Contract(pure = true)
  static OStack multiply(@NotNull OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two elements replaced
    // with their product.

    // if the input is none(), or if the stack doesn't have at least two elements,
    // the output will be none().

    return ostack.flatmap(
        stack -> stack.match(
            empty -> OStack.none(),
            (head, empty) -> OStack.none(),
            (head, second, tail) -> OStack.some(tail.add(head * second))));
  }

  @NotNull
  @Contract(pure = true)
  static OStack subtract(@NotNull OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two elements replaced
    // with the top element subtracted from the element below it.

    // if the input is none(), or if the stack doesn't have at least two elements,
    // the output will be none().

    return ostack.flatmap(
        stack -> stack.match(
            empty -> OStack.none(),
            (head, empty) -> OStack.none(),
            (head, second, tail) -> OStack.some(tail.add(second - head)))); // ordering matters! "3 2 -" should yield 1
  }

  @NotNull
  @Contract(pure = true)
  static OStack divide(@NotNull OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two elements replaced
    // with the second element divided by the top element.

    // if the input is none(), or if the stack doesn't have at least two elements,
    // the output will be none().

    // in the event where division-by-zero would have occurred, none() is returned
    // to indicate the error.

    return ostack.flatmap(
        stack -> stack.match(
            empty -> OStack.none(),
            (head, empty) -> OStack.none(),

            // note to the reader: division by zero is not defined over the "real" numbers that
            // you all know and love. Technically, it *is* defined in IEEE floating point arithmetic
            // as a special value: "not-a-number" (NaN), and all the floating-point arithmetic is defined with
            // NaN in ways that actually makes sense. That said, for the purposes of this project, we'll make
            // the executive decision that we don't want to deal with NaN and we'll handle the error ourselves.
            (denominator, numerator, tail) -> (denominator == 0.0)
                ? OStack.none()
                : OStack.some(tail.add(numerator / denominator))));
  }

  @NotNull
  @Contract(pure = true)
  static OStack dup(@NotNull OStack ostack) {
    // required: a stack with one or more elements

    // returns: a new stack equal to the old one, with its top element duplicated.

    // if the input is none(), or if the stack doesn't have at least one element,
    // the output will be none().

    return ostack.flatmap(
        stack -> stack.match(
            empty -> OStack.none(),
            (head, tail) -> OStack.some(stack.add(head))));
  }

  @NotNull
  @Contract(pure = true)
  static OStack drop(@NotNull OStack ostack) {
    // required: a stack with one or more elements

    // returns: a new stack equal to the old one, with its top element removed.

    // if the input is none(), or if the stack doesn't have at least one element,
    // the output will be none().

    return ostack.flatmap(
        stack -> stack.match(
            empty -> OStack.none(),
            (head, tail) -> OStack.some(tail)));
  }

  @NotNull
  @Contract(pure = true)
  static OStack swap(@NotNull OStack ostack) {
    // required: a stack with two or more elements

    // returns: a new stack equal to the old one, with its top two elements swapped.

    // if the input is none(), or if the stack doesn't have at least two elements,
    // the output will be none().

    return ostack.flatmap(
        stack -> stack.match(
            empty -> OStack.none(),
            (head, empty) -> OStack.none(),
            (head, second, tail) -> OStack.some(tail.add(head).add(second))));
  }

  @NotNull
  @Contract(pure = true)
  static OStack noop(@NotNull OStack ostack) {
    // required: no requirements.

    // returns: the output is the same as the input. (This is used by the "=" command,
    // which makes no changes to the stack at all, but is useful for the user to
    // type something and see what's remaining on the top of the stack.)

    return ostack;
  }

  @NotNull
  @Contract(pure = true)
  static OStack fail(@SuppressWarnings("UnusedParameters") @NotNull OStack ostack) {
    // required: no requirements.

    // returns: none(), no matter what. (This is used when the user enters an unknown command.)

    return OStack.none();
  }

  @NotNull
  @Contract(pure = true)
  static OStack clear(@SuppressWarnings("UnusedParameters") @NotNull OStack ostack) {
    // required: no requirements.

    // returns: an empty stack, no matter what.

    return of(); // empty stack
  }

  @NotNull
  @Contract(pure = true)
  static CalcOp numberPusher(double number) {
    // This function is a little different from all the others: given a double, it returns a CalcOp.
    // (Yes, it's a function that returns functions.) Like all the other CalcOps, the one you get here
    // takes an optional stack and returns one. This operation happens to push the given number on top,
    // and it does nothing if the stack is an Option.none.
    return ostack -> ostack.map(stack -> stack.add(number));
  }

  @NotNull
  @Contract(pure = true)
  static IList<Token<RPNTokenPatterns>> scan(@NotNull String input) {
    NamedMatcher<RPNTokenPatterns> nm = new NamedMatcher<>(RPNTokenPatterns.class);
    IList<Token<RPNTokenPatterns>> tokens = nm.tokenize(input, new Token<>(RPNTokenPatterns.FAIL, ""));
    return tokens.filter(x -> x.type != RPNTokenPatterns.WHITESPACE); // remove whitespace tokens; we don't care about them
  }

  /**
   * Given a token, return a function (from optional stacks to optional stacks) corresponding to
   * that token.
   */
  @NotNull
  @Contract(pure = true)
  static CalcOp getFunction(@NotNull Token<RPNTokenPatterns> token) {
    return
        (token.type == RPNTokenPatterns.NUMBER)
            ? numberPusher(Double.parseDouble(token.data))
            : REGISTRY.oget(token.type).getOrElse(RPNCalculator::fail);
  }


  /**
   * Given a list of tokens, return a function (from optional stacks to optional stacks)
   * corresponding to each token applied in sequence.
   */
  @NotNull
  @Contract(pure = true)
  static CalcOp getFunction(@NotNull IList<Token<RPNTokenPatterns>> tokenList) {
    return tokenList
        .map(RPNCalculator::getFunction)
        .foldl(CalcOp.identity(), CalcOp::andThen);
  }

  /**
   * Given a string of input, this will tokenize it then execute it on the internal RPN stack. The
   * value on the head of the stack is returned. If an error occurs, a suitable error message is
   * returned instead and the stack will have the same value as its initial state before calc() was
   * called.
   *
   * <p>Note: this method mutates the state of the class! If the input has no errors, then the resulting
   * stack state is saved internally. If the input has errors, then the stack state will be unchanged.
   */
  @NotNull
  public String calc(@NotNull String input) {
    IList<Token<RPNTokenPatterns>> tokenList = scan(input);

    CalcOp f = getFunction(tokenList);
    OStack resultStack = f.apply(OStack.some(rpnStack));

    if (!resultStack.isSome()) {
      return "Error!";
    }

    rpnStack = resultStack.get(); // we're now officially done with the prior stack, so we overwrite it

    if (rpnStack.empty()) {
      return "Empty stack";
    }

    return rpnStack.head().toString();
  }

  /**
   * Useful for testing: creates a "stack" with the elements present on it. The order of the
   * elements as passed here will be the order on the stack from top to bottom.
   */
  @SuppressWarnings("varargs")
  @NotNull
  @Contract(pure = true)
  static OStack of(Double... elements) {
    return OStack.some(List.fromArray(elements));
  }

  //
  // First, the tokenizing machinery, borrowed from our JSON parser. Note that every Java enum
  // is Comparable, without needing to declare anything, therefore they're compatible with our IMap
  // interface, which requires the keys to be Comparable.
  //
  // Some details for the curious:
  // http://stackoverflow.com/questions/519788/why-is-compareto-on-an-enum-final-in-java
  //
  enum RPNTokenPatterns implements TokenPatterns {
    NUMBER("(-)?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?"),
    PLUS("\\+"),
    TIMES("\\*"),
    MINUS("-"),
    DIVIDE("/"),
    DUP("dup"),
    SWAP("swap"),
    DROP("drop"),
    CLEAR("clear"),
    EQUALS("="),
    WHITESPACE("\\s+"),
    FAIL("");                    // if the matcher fails, you get one of these

    public final String pattern;

    RPNTokenPatterns(String pattern) {
      this.pattern = pattern;
    }

    @NotNull
    public String pattern() {
      return pattern;
    }
  }

  /**
   * We use this "option stack" class to represent the input and output type of all of our
   * monadic RPN functions, delegating to an internal Option&lt;IList&lt;Double&gt;&gt;,
   * so we can write everything in RPNCalculator in terms of OStack rather than
   * Option&lt;IList&lt;Double&gt;&gt;. This makes our code cleaner.
   *
   * <p>Note that this class is <b>not public</b>. It's package scope so we can see it in our
   * unit tests, but it's not meant to be externally visible.
   */
  static class OStack {
    // Engineering note: You might be wondering why we've built OStack rather than just using
    // Option<IList<Double>> everywhere. The short answer is that it's really tiring to have
    // to write that big type signature everywhere. It's cleaner and simpler to just speak about
    // OStack, and we've defined all the same method names, so it feels the same and does the
    // same thing.

    // But hey, Option is an *interface* type, so why don't we just write OStack implements Option<IList<Double>>?
    // That seems attractive, as it would give us all the default methods from there. Functionality for free!
    // Unfortunately, if you do that, then the return types for map and flatmap would also need to be Option<IList<Double>>
    // rather than OStack, getting rid of some of the benefits of using OStack in the first place.

    // As described in the engineering note, below, for logWrap, what we *really*
    // want is a typedef, so we can just say that OStack is a *shorthand* for Option<IList<Double>>.
    // Java has no such feature now nor in its immediate future. Alas. So instead, we take a
    // delegation approach. And if you really want the Option<IList<Double>>, then you can just
    // call oget().

    @NotNull
    private static final OStack NONE_SINGLETON = new OStack(Option.none());

    @NotNull
    private final Option<IList<Double>> ostack;

    private OStack(@NotNull Option<IList<Double>> ostack) {
      this.ostack = ostack;
    }

    @NotNull
    @Contract(pure = true)
    public static OStack some(IList<Double> stack) {
      return new OStack(Option.some(stack));
    }

    @NotNull
    @Contract(pure = true)
    public static OStack none() {
      return NONE_SINGLETON;
    }

    @Contract(pure = true)
    public boolean isSome() {
      return ostack.isSome();
    }

    @NotNull
    @Contract(pure = true)
    public Option<IList<Double>> oget() {
      return ostack;
    }

    @NotNull
    @Contract(pure = true)
    public IList<Double> get() {
      return ostack.get();
    }

    @NotNull
    @Contract(pure = true)
    public <T> T match(@NotNull Supplier<? extends T> noneFunc, @NotNull Function<? super IList<Double>, ? extends T> someFunc) {
      return ostack.match(noneFunc, someFunc);
    }

    @NotNull
    @Contract(pure = true)
    public OStack map(@NotNull UnaryOperator<IList<Double>> func) {
      return new OStack(ostack.map(func));
    }

    @NotNull
    @Contract(pure = true)
    public OStack flatmap(@NotNull Function<? super IList<Double>, ? extends OStack> func) {
      return match(OStack::none, func);
    }

    @Override
    public String toString() {
      return ostack.toString();
    }

    @Override
    public int hashCode() {
      return ostack.hashCode();
    }
  }

  /**
   * Every RPNCalculator function, rather than being declared in terms of UnaryOperator or Function,
   * which gets a bit unwieldy, can instead be declared in terms of this much more straightforward
   * interface. This interface defines monadic composition ("andThen") and an identity operation,
   * allowing us to deal with calculator-operations as first-class objects, without having to
   * apply them to actual stacks of numbers until the last minute.
   */
  @FunctionalInterface
  interface CalcOp {
    @NotNull
    OStack apply(@NotNull OStack input);

    @NotNull
    @Contract(pure = true)
    static CalcOp identity() {
      return x -> x;
    }

    @NotNull
    @Contract(pure = true)
    default CalcOp andThen(@NotNull CalcOp op) {
      return stack -> op.apply(this.apply(stack));
    }

    /**
     * This is a front-end for Log.iwrap that turns any CalcOp into another CalcOp that
     * dumps its input and output to the log.
     * @see Log#iwrap
     */
    @NotNull
    @Contract(pure = true)
    default CalcOp logWrap(@NotNull String tag) {
      // Engineering note: what's with the ::apply things below? When you name the function
      // like this, you're saying "Hey, Java type system, try to coerce this particular function
      // into matching the functional type."

      // We can't just return Log.iwrap(tag, calcOp) because calcOp isn't a Function.
      // If we had declared it that way (i.e., CalcOp extends UnaryOperator<OStack>),
      // then we'd have type conflicts with all the default methods on UnaryOperator,
      // like "andThen", which expects to return another UnaryOperator, not a CalcOp.

      // What we *really* want are "typedefs", so we could declare "CalcOp" to be a *shorthand*
      // for UnaryOperator<OStack>, and we could declare OStack as an alias for Option<IList<Double>>.
      // Then the composition operators that are already there in UnaryOperator would
      // easily map back to CalcOp.

      // Java8 has no such feature, nor is it on the roadmap for Java9, which forces us to do this
      // messy stuff instead.
      return Log.iwrap(tag, this::apply)::apply;
    }
  }
}
