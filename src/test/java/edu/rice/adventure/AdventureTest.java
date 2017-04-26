package edu.rice.adventure;


import edu.rice.json.Operations;
import edu.rice.json.Parser;
import edu.rice.json.Value;
import edu.rice.json.Scanner;
import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.List;
import edu.rice.regex.NamedMatcher;
import edu.rice.tree.IMap;
import edu.rice.tree.TreapMap;
import edu.rice.util.Log;
import edu.rice.util.Option;
import edu.rice.regex.NamedMatcher;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.security.Key;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import edu.rice.list.LazyList;

import java.util.function.UnaryOperator;
import org.junit.Test;

import static edu.rice.json.Builders.*;
import static edu.rice.json.Builders.jobject;
import static edu.rice.json.Builders.jpair;
import static edu.rice.json.Operations.*;
import static org.junit.Assert.*;
import edu.rice.io.Files;

/**
 * Created by liangzifeng on 10/29/16.
 */

public class AdventureTest {
  @Test
  public void test() throws Exception {
    Adventure newGame = new Adventure();
    //ending 1
    assertTrue(newGame.response("north").contains("corridor A"));
    assertTrue(newGame.response("north").contains("corridor B"));
    assertTrue(newGame.response("north").contains("living room"));
    assertTrue(newGame.response("talk to old man").contains("talked"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("threw"));
    assertTrue(newGame.response("look").contains("dark"));
    assertTrue(newGame.response("start").contains("check"));


    //ending 2
    assertTrue(newGame.response("south").contains("no"));
    assertTrue(newGame.response("talk to thief").contains("talk"));
    assertTrue(newGame.response("time").contains("3"));
    assertTrue(newGame.response("take nothing").contains("no"));
    assertTrue(newGame.response("say hi").contains("nothing"));
    assertTrue(newGame.response("say hi to dog").contains("no"));
    assertTrue(newGame.response("drop apple").contains("dropped"));
    assertTrue(newGame.response("drop match").contains("match"));
    assertTrue(newGame.response("take match").contains("match"));
    assertTrue(newGame.response("take match").contains("match"));
    assertTrue(newGame.response("take match").contains("match"));
    assertTrue(newGame.response("take apple").contains("apple"));
    assertTrue(newGame.response("take match").contains("no"));
    assertTrue(newGame.response("look match").contains("can"));
    assertTrue(newGame.response("use apple").contains("used"));
    assertTrue(newGame.response("look sword").contains("couldn't"));
    assertTrue(newGame.response("take coin").contains("coin"));
    assertTrue(newGame.response("drop coin").contains("coin"));
    assertTrue(newGame.response("drop sword").contains("no"));
    assertTrue(newGame.response("w").contains("no"));
    assertTrue(newGame.response("north").contains("corridor A"));
    assertTrue(newGame.response("south").contains("base"));
    assertTrue(newGame.response("north").contains("corridor A"));
    assertTrue(newGame.response("look").contains("dark"));
    assertTrue(newGame.response("use match").contains("struck"));
    assertTrue(newGame.response("take coin").contains("took"));
    assertTrue(newGame.response("look").contains("anything"));
    assertTrue(newGame.response("east").contains("no exit"));
    assertTrue(newGame.response("west").contains("black market"));
    assertTrue(newGame.response("talk to merchant").contains("talked"));
    assertTrue(newGame.response("give coin to merchant").contains("bought"));
    assertTrue(newGame.response("give coin to merchant").contains("bought"));
    assertTrue(newGame.response("give coin to merchant").contains("enough"));
    assertTrue(newGame.response("use potion").contains("no"));
    assertTrue(newGame.response("say hi to merchant").contains("merchant"));
    assertTrue(newGame.response("east").contains("east"));
    assertTrue(newGame.response("n").contains("corridor B"));
    assertFalse(newGame.response("take can").contains("pea"));
    assertTrue(newGame.response("use match").contains("struck"));
    assertFalse(newGame.response("drop match").contains("use"));
    assertTrue(newGame.response("drop nothing").contains("no"));
    assertFalse(newGame.response("use apple").contains("pea"));
    assertFalse(newGame.response("use coin").contains("used"));
    assertTrue(newGame.response("drop coin").contains("dropped"));
    assertTrue(newGame.response("look").contains("look"));
    assertTrue(newGame.response("look coin").contains("coin"));
    assertTrue(newGame.response("look apple").contains("apple"));
    assertTrue(newGame.response("inventory").contains("inventory"));
    assertTrue(newGame.response("use potion on dog").contains("there is"));
    assertFalse(newGame.response("give gold to old man").contains("There is no"));
    assertFalse(newGame.response("take gold from bag").contains("took"));
    assertTrue(newGame.response("north").contains("living room"));
    assertTrue(newGame.response("west").contains("study"));
    assertTrue(newGame.response("look").contains("afraid"));
    assertTrue(newGame.response("look coin").contains("afraid"));
    assertTrue(newGame.response("use potion on dog").contains("sleep"));
    assertTrue(newGame.response("take sword").contains("took"));
    assertTrue(newGame.response("east").contains("living room"));
    assertTrue(newGame.response("north").contains("bedroom"));
    assertTrue(newGame.response("north").contains("no"));
    assertTrue(newGame.response("use match").contains("struck"));
    assertTrue(newGame.response("take liquid").contains("took"));
    assertTrue(newGame.response("use liquid").contains("dizzy"));
    assertTrue(newGame.response("north").contains("living room"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("score").contains("0"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("z").contains("hour"));
    assertTrue(newGame.response("east").contains("corridor C"));
    assertTrue(newGame.response("talk to thief").contains("talk"));
    assertTrue(newGame.response("talk to thief").contains("talk"));
    assertTrue(newGame.response("talk to thief").contains("talk"));
    assertTrue(newGame.response("a bc c d e").contains("understand"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("use sword on thief").contains("sword"));
    assertTrue(newGame.response("take gold from bag").contains("take"));
    assertTrue(newGame.response("take gold from bag").contains("take"));
    assertTrue(newGame.response("w").contains("living room"));
    assertTrue(newGame.response("talk to old man").contains("bomb"));
    assertTrue(newGame.response("give gold to old man").contains("give"));
    assertTrue(newGame.response("talk to old man").contains("talk"));
    assertTrue(newGame.response("east").contains("corridor C"));
    assertTrue(newGame.response("east").contains("lobby"));
    assertTrue(newGame.response("say 1234 to lock").contains("password"));
    assertTrue(newGame.response("say 3358 to lock").contains("correct"));




  }


}

