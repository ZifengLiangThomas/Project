/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it available
 * to future Comp215 students. Violations of this rule are considered Honor
 * Code violations and will result in your being reported to the Honor
 * Council, even after you've completed the class, and will result in
 * retroactive reductions to your grade.
 */

package edu.rice.tree;

import edu.rice.list.KeyValue;
import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.util.Option;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * TreapMap implements the IMap interface, supplying functional mappings from any comparable type (as the key)
 * to any other type (as the value). A variety of static methods are provided in TreapMap to help construct
 * a TreapMap instead of just having a constructor. Users of TreapMap will never declare variables of the TreapMap
 * type. Instead, everything will use IMap.
 * @see IMap
 */
public interface TreapMap<K extends Comparable<? super K>, V> extends IMap<K,V> {
  //
  // Data definition:
  //
  // A TreapMap can be one of two things: empty or non-empty.
  // These are represented as TreapMap.Empty and TreapMap.NonEmptyMap.
  // These are just wrappers around TreapSet. More details there.
  //

  /**
   * Construct an empty map from keys (K) to values (V).
   * @param <K> map key, which must be comparable
   * @param <V> map value, can be any type
   */
  @NotNull
  @Contract(pure = true)
  static <K extends Comparable<? super K>, V> IMap<K, V> makeEmpty() {
    @SuppressWarnings("unchecked")
    IMap<K, V> typedEmpty = (IMap<K, V>) Empty.SINGLETON;
    return typedEmpty;
  }

  /**
   * Given a bunch of key/values pairs passed as varargs to this function, return a map with those pairs.
   */
  @SuppressWarnings("varargs")
  @SafeVarargs
  @NotNull
  @Contract(pure = true)
  static <K extends Comparable<? super K>, V> IMap<K, V> of(@NotNull KeyValue<K, V>... pairs) {
    return TreapMap.<K, V>makeEmpty().addList(LazyList.fromArray(pairs));
  }

  /**
   * Given a list of key/value pairs, return a map with those pairs.
   */
  @NotNull
  @Contract(pure = true)
  static <K extends Comparable<? super K>, V> IMap<K, V> fromList(@NotNull IList<KeyValue<K, V>> list) {
    return TreapMap.<K, V>makeEmpty().addList(list);
  }

  /**
   * Given a list of key/value pairs, return a map with those pairs. If two elements in the list have the same key,
   * the resulting map will use the mergeOp to combine them.
   */
  @NotNull
  @Contract(pure = true)
  static <K extends Comparable<? super K>, V> IMap<K, V>
      fromList(@NotNull IList<KeyValue<K, V>> list, @NotNull BinaryOperator<V> mergeOp) {

    return TreapMap.<K, V>makeEmpty().addListMerge(list, mergeOp);
  }

  /**
   * Given a list of keys and a lambda that can convert those keys to values, return the resulting map from keys to
   * values.
   */
  @NotNull
  @Contract(pure = true)
  static <K extends Comparable<? super K>, V> IMap<K, V> fromList(@NotNull IList<K> list, @NotNull Function<K, V> mapFunc) {
    return TreapMap.<K, V>makeEmpty().addList(IList.mapkv(list, mapFunc));
  }

  /**
   * Given a list of keys and a lambda that can convert those keys to values, return the resulting map from keys to
   * values. If two elements in the list have the same key, the resulting map will use the mergeOp to combine them.
   */
  @NotNull
  @Contract(pure = true)
  static <K extends Comparable<? super K>, V> IMap<K, V>
      fromList(@NotNull IList<K> list, @NotNull Function<K, V> mapFunc, @NotNull BinaryOperator<V> mergeOp) {

    return TreapMap.<K, V>makeEmpty().addListMerge(IList.mapkv(list, mapFunc), mergeOp);
  }


  /**
   * Given a java.util.Map (hashmap, etc.), get back a functional map stored in our treap structure.
   */
  @NotNull
  @Contract(pure = true)
  static <K extends Comparable<? super K>, V> IMap<K, V> fromMap(@NotNull java.util.Map<K, V> inMap) {
    return TreapMap.fromList(
        LazyList.fromIterator(inMap.entrySet().iterator())
            .map(entry -> KeyValue.make(entry.getKey(), entry.getValue())));
  }

  /**
   * Here's the non-empty TreapMap implementation. External users will never call this directly.
   */
  class NonEmptyMap<K extends Comparable<? super K>, V> implements TreapMap<K, V> {
    //
    // Note: the general strategy of TreapMap is that it's just a TreapSet of KeyValue<K,V>, so we just
    // *delegate* to it. This is different from inheritance, because we explicitly decide how to pass
    // things along, rather than letting Java's method dispatch do it for us.
    //
    @NotNull
    private final ISet<KeyValue<K, V>> set;

    // not for external use; start from the empty TreapMap instead (see below)
    private NonEmptyMap(@NotNull ISet<KeyValue<K, V>> set) {
      this.set = set;
    }

    @NotNull
    @Override
    public <K2 extends Comparable<? super K2>, V2> IMap<K2, V2> makeEmptySameType() {
      return makeEmpty();
    }

    @NotNull
    @Override
    public ISet<KeyValue<K, V>> getSet() {
      return set;
    }

    @Override
    public int hashCode() {
      return toSortedList().hashCode(); // not necessary efficient, but should be correct
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null) {
        return false;
      }

      if (!(o instanceof IMap<?, ?>)) {
        return false;
      }

      IMap<?, ?> map = (IMap<?, ?>) o;

      if (map.empty()) {
        return false;
      }

      // because set equality is a different animal from structural equality, we need to linearize the sets
      return getSet().equals(map.getSet());
    }

    @NotNull
    @Override
    public IMap<K, V> add(@NotNull KeyValue<K, V> kv) {
      return new NonEmptyMap<>(set.add(kv));
    }

    @NotNull
    @Contract(pure = true)
    private static <K extends Comparable<? super K>, V>
        BinaryOperator<KeyValue<K, V>> kvMergeOp(@NotNull BinaryOperator<V> mergeOp) {

      // There are several places where we have two IKeyValue pairs with the same key and we
      // wish to merge them together with an operator that merges the values. We're assuming
      // the keys are the same, so we'll just use the first one.

      // + Note how kvMergeOp is a "higher order function" that takes a lambda as a argument
      //   and returns another lambda.
      return (kv1, kv2) -> KeyValue.make(kv1.getKey(), mergeOp.apply(kv1.getValue(), kv2.getValue()));
    }

    @NotNull
    @Override
    public IMap<K, V> merge(@NotNull K key, @NotNull V value, @NotNull BinaryOperator<V> mergeOp) {
      return new NonEmptyMap<>(set.merge(KeyValue.make(key, value), kvMergeOp(mergeOp)));
    }

    @NotNull
    @Override
    public IMap<K, V> remove(@NotNull K key) {
      // doesn't matter what the value is, only key equality is tested
      return new NonEmptyMap<>(set.remove(KeyValue.makeNoValue(key)));
    }

    @NotNull
    @Override
    public Option<V> oget(@NotNull K key) {
      // look up the key in the set (this will be "equal"), then extract the value
      // + note clever use of Option.map()
      return set.oget(KeyValue.makeNoValue(key)).map(KeyValue::getValue);
    }

    @Override
    public boolean empty() {
      return set.empty();
    }

    @Override
    public int size() {
      return set.size();
    }

    @NotNull
    @Override
    public IMap<K, V> union(@NotNull IMap<K, V> otherMap, @NotNull BinaryOperator<V> mergeOp) {
      return new NonEmptyMap<>(set.union(otherMap.getSet(), kvMergeOp(mergeOp)));
    }

    @NotNull
    @Override
    public IMap<K, V> intersect(@NotNull IMap<K, V> otherMap, @NotNull BinaryOperator<V> mergeOp) {
      return new NonEmptyMap<>(set.intersect(otherMap.getSet(), kvMergeOp(mergeOp)));
    }

    @NotNull
    @Override
    public IMap<K, V> except(@NotNull IMap<K, V> otherMap) {
      // just delegate to the internal set
      return new NonEmptyMap<>(set.except(otherMap.getSet()));
    }

    @NotNull
    @Override
    public IMap<K, V> greaterThan(@NotNull K query, boolean inclusive) {
      // just delegate to the internal set
      return new NonEmptyMap<>(set.greaterThan(KeyValue.makeNoValue(query), inclusive));
    }

    @NotNull
    @Override
    public IMap<K, V> lessThan(@NotNull K query, boolean inclusive) {
      // just delegate to the internal set
      return new NonEmptyMap<>(set.lessThan(KeyValue.makeNoValue(query), inclusive));
    }

    @NotNull
    @Override
    public IList<KeyValue<K, V>> toSortedList() {
      return set.toSortedList();
    }

    @NotNull
    @Override
    public IList<KeyValue<K, V>> toList() {
      return set.toList();
    }

    @NotNull
    @Override
    public String toString() {
      return set.toString();
    }
  }

  /**
   * Here's the empty TreapMap implementation. External users will never call this directly.
   */
  class Empty<K extends Comparable<? super K>, V> implements TreapMap<K,V>, IMap.Empty<K, V> {
    private static final IMap<?, ?> SINGLETON = new TreapMap.Empty<>();

    // external user: don't call this; call make() instead
    private Empty() { }

    @NotNull
    @Override
    public <K2 extends Comparable<? super K2>, V2> IMap<K2, V2> makeEmptySameType() {
      return makeEmpty();
    }

    @NotNull
    @Override
    public ISet<KeyValue<K, V>> getSet() {
      return TreapSet.makeEmpty();
    }

    @NotNull
    @Override
    public IMap<K, V> add(@NotNull KeyValue<K, V> kv) {
      return new NonEmptyMap<>(TreapSet.<KeyValue<K,V>>makeEmpty().add(kv));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null) {
        return false;
      }

      if (!(o instanceof IMap)) {
        return false;
      }

      IMap<?, ?> map = (IMap<?, ?>) o;

      return map.empty();
    }

    @Override
    public int hashCode() {
      return 1;
    }

    @NotNull
    @Override
    public String toString() {
      return "{}";
    }
  }
}
