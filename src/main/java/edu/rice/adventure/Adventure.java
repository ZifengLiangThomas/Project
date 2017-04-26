package edu.rice.adventure;

import edu.rice.json.Operations;
import edu.rice.json.Parser;
import edu.rice.json.Value;
import edu.rice.list.IList;
import edu.rice.list.List;
import edu.rice.util.Option;
import edu.rice.json.Builders;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import edu.rice.io.Files;
import java.util.function.Function;
import java.util.function.Supplier;
import edu.rice.list.LazyList;
import java.util.Random;

/**
 * Engineering note: the game's initial state is structured in the JSON file, world.json
 * under the resources Directory. Each command put by the user is parsed and turned into
 * composition of monadic functions. Those monadic functions update the the JSON file
 * and return respective responses on the dialogue. For further information, please look
 * up the texts README, Walkthrough, and Design under the root directory.
 */

public class Adventure {
  //read the json file to check the game state
  static final String JsonRoom = Files.readResource("world.json").getOrElse("");
  static Value all = Parser.parseJsonObject(JsonRoom).get();
  //track the player's position
  static Value pos = Operations.ogetPath(all, "rooms/base").get();

  //initial game state
  static double timer = -1;
  static double pansyTime = -1;
  static String stolen = "";
  static Integer gameOver = 0;
  static Integer remain = 12;

  /**
   *
   * @param oWorld current operations on stack
   * @return OWorld operation that update's the player's position
   */
  static OWorld east(@NotNull OWorld oWorld) {
    //Ostack operation: player heading East
    return oWorld.flatmap(
        world -> {
          //get the OWorld Operation's result for current world state
          if (Operations.ogetPath(oWorld.get(), "exits/East").isSome()) {
            String
                desName =
                world.asJObject().oget("exits").get().asJObject().oget("East").get().asJString().toUnescapedString();
            //OWorld operation that reports new location
            return OWorld.some(all.asJObject().oget("rooms").get().asJObject().oget(desName).get().asJObject());
          } else {
            return OWorld.none();
          }
        }
    );

  }

  /**
   *
   * @param oWorld current operations on stack
   * @return OWorld operation that updates the player's position
   */
  static OWorld west(@NotNull OWorld oWorld) {
    //Ostack operation: player heading West
    return oWorld.flatmap(
        world -> {
          if (Operations.ogetPath(oWorld.get(), "exits/West").isSome()) {
            String
                desName =
                world.asJObject().oget("exits").get().asJObject().oget("West").get().asJString().toUnescapedString();
            return OWorld.some(all.asJObject().oget("rooms").get().asJObject().oget(desName).get().asJObject());
          } else {
            return OWorld.none();
          }
        }
    );

  }

  /**
   *
   * @param oWorld current operations on stack
   * @return OWorld operation that updates the player's position
   */
  static OWorld north(@NotNull OWorld oWorld) {
    //Ostack operation: player heading North
    return oWorld.flatmap(
        world -> {
          if (Operations.ogetPath(oWorld.get(), "exits/North").isSome()) {
            String
                desName =
                world.asJObject().oget("exits").get().asJObject().oget("North").get().asJString().toUnescapedString();
            return OWorld.some(all.asJObject().oget("rooms").get().asJObject().oget(desName).get().asJObject());
          } else {
            return OWorld.none();
          }
        }
    );

  }

  /**
   *
   * @param oWorld current operations on stack
   * @return OWorld operation that updates the player's position
   */
  static OWorld south(@NotNull OWorld oWorld) {
    //Ostack operation: player heading South
    return oWorld.flatmap(
        world -> {
          if (Operations.ogetPath(oWorld.get(), "exits/South").isSome()) {
            String desName = world.asJObject().oget("exits")
                .get().asJObject().oget("South").get().asJString().toUnescapedString();
            return OWorld.some(all.asJObject().oget("rooms").get().asJObject().oget(desName).get().asJObject());
          } else {
            return OWorld.none();
          }
        }
    );

  }

  /**
   *
   * @param oWorld  current operations on stack
   * @param item an item to be picked up
   * @return OWorld Operation that updates the Json file
   */
  static OWorld take(@NotNull OWorld oWorld, String item) {
    //Ostack operation: taking items on the ground
    String roomName = pos.asJObject().oget("name").get().asJString().toUnescapedString();
    IList<String> keys = Operations.ogetPath(all,"rooms/" + roomName + "/contents").get().asJObject().getMap().keys();
    IList<String> match = keys.filter(key -> key.contains(item));
    if (match.empty()) {
      return oWorld.flatmap(world -> OWorld.none());
    }
    String key = match.head();
    String decreasePath = "rooms/" + roomName + "/contents/" + key;
    String increasePath = "characters/player/inventory/" + key;
    return oWorld.flatmap(
        world -> {
          // if the number of item is 1, we make it option.none
          if (Operations.ogetPath(world, decreasePath).isSome()) {
            if (Operations.ogetPath(world, decreasePath).get().asJNumber().get() == 1) {
              OWorld decrease =
                  OWorld.some(Operations.updatePath(world, decreasePath, oval -> Option.none()).get().asJObject());
              if (Operations.ogetPath(decrease.get(), increasePath).isSome()) {
                return OWorld.some(Operations.updatePath(decrease.get(), increasePath,
                    oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
              } else {
                return OWorld.some(Operations.updatePath(decrease.get(), increasePath,
                    oval -> Option.some(Builders.jnumber(1))).get().asJObject());
              }
              // if the number of item is more than 1, then minus 1
            } else {
              OWorld decrease = OWorld.some(Operations.updatePath(world, decreasePath,
                  oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - 1))).get().asJObject());
              if (Operations.ogetPath(decrease.get(), increasePath).isSome()) {
                return OWorld.some(Operations.updatePath(decrease.get(), increasePath,
                    oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
              } else {
                return OWorld.some(Operations.updatePath(decrease.get(), increasePath,
                    oval -> Option.some(Builders.jnumber(1))).get().asJObject());
              }
            }
          } else {
            return OWorld.none();
          }
        }
    );
  }

  /**
   *
   * @param oWorld current operations on stack
   * @param item an item to be used without a target
   * @return OWorld Operation that updates the Json file
   */
  static OWorld use(@NotNull OWorld oWorld, String item) {
    IList<String> keys = Operations.ogetPath(all, "characters/player/inventory").get().asJObject().getMap().keys();
    IList<String> match = keys.filter(key -> key.contains(item));
    if (match.empty()) {
      return oWorld.flatmap(world -> OWorld.none());
    }
    String key = match.head();
    String decreasePath = "characters/player/inventory/" + key;
    return oWorld.flatmap(
        world -> {

          //if the item needs a target, return none
          if (Operations.ogetPath(all, "items/" + key + "/object").isSome()) {
            return OWorld.none();
          }

          //check if the item is present in the inventory
          if (Operations.ogetPath(world, decreasePath).isSome()
              && Operations.ogetPath(world, "items/" + item + "/object").isNone()
              && Operations.ogetPath(world, "items/" + item + "/target").isNone()) {
            if (Operations.ogetPath(world, decreasePath).get().asJNumber().get() == 1) {
              return OWorld.some(Operations.updatePath(world, decreasePath, oval -> Option.none()).get().asJObject());
            }
            if (Operations.ogetPath(world, decreasePath).get().asJNumber().get() > 1) {
              return OWorld.some(Operations.updatePath(world, decreasePath,
                  oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - 1))).get().asJObject());
            }
          }
          return OWorld.none();
        });
  }

  /**
   *
   * @param oWorld current operations on stack
   * @param item an item to be dropped by the player
   * @return OWorld Operation that updates the Json file
   */
  static OWorld drop(@NotNull OWorld oWorld, String item) {
    String roomName = pos.asJObject().oget("name").get().asJString().toUnescapedString();
    IList<String> keys = Operations.ogetPath(all, "characters/player/inventory").get().asJObject().getMap().keys();
    IList<String> match = keys.filter(key -> key.contains(item));
    if (match.empty()) {
      return oWorld.flatmap(world -> OWorld.none());
    }
    String key = match.head();
    String decreasePath = "characters/player/inventory/" + key;
    String increasePath = "rooms/" + roomName + "/contents/" + key;

    return oWorld.flatmap(
        world -> {
          // if the number of item is 1, we make it option.none
          if (Operations.ogetPath(world, decreasePath).isSome()) {
            if (Operations.ogetPath(world, decreasePath).get().asJNumber().get() == 1) {
              OWorld decrease =
                  OWorld.some(Operations.updatePath(world, decreasePath, oval -> Option.none()).get().asJObject());
              if (Operations.ogetPath(decrease.get(), increasePath).isSome()) {
                return OWorld.some(Operations.updatePath(decrease.get(), increasePath,
                    oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
              } else {
                return OWorld.some(Operations.updatePath(decrease.get(), increasePath,
                    oval -> Option.some(Builders.jnumber(1))).get().asJObject());
              }
              // if the number of item is more than 1, then minus 1
            } else {
              OWorld decrease = OWorld.some(Operations.updatePath(world, decreasePath,
                  oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - 1))).get().asJObject());
              if (Operations.ogetPath(decrease.get(), increasePath).isSome()) {
                return OWorld.some(Operations.updatePath(decrease.get(), increasePath,
                    oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
              } else {
                return OWorld.some(Operations.updatePath(decrease.get(), increasePath,
                    oval -> Option.some(Builders.jnumber(1))).get().asJObject());
              }
            }
          }
          return OWorld.none();
        });
  }
  /**
   * Every command made by the player takes one time unit.
   * There is an NPC thief in the game who appears in a different random location as time passes
   * @param oWorld current operations on stack
   * @return OWorld Operation that update's the Json file
   */
  static OWorld oneSec(@NotNull OWorld oWorld) {
    String roomName = pos.asJObject().oget("name").get().asJString().toUnescapedString();
    return oWorld.flatmap(
        world -> {
          if (Operations.ogetPath(all, "characters/thief/status").get().asJNumber().get() == 1) {
            //random position for thief
            IList<String> empty = List.makeEmpty();
            IList<String> corridors = empty.add("corridor A").add("corridor B").add("corridor C");
            int length = corridors.length();
            Random rand = new Random();
            int index = rand.nextInt(length);
            String randPos = corridors.nth(index).get();
            //update thief position
            OWorld
                sub =
                OWorld.some(Operations.updatePath(world,
                    "characters/thief/position",
                    oval -> Option.some(Builders.jsonString(randPos))).get().asJObject());

            //meet the thief
            if (roomName.contains(Operations.ogetPath(sub.get(), "characters/thief/position")
                .get()
                .asJString()
                .toUnescapedString())) {
              IList<String>
                  items =
                  Operations.ogetPath(all, "characters/player/inventory")
                      .get()
                      .asJObject()
                      .getMap()
                      .keys()
                      .map(key -> key.toLowerCase());
              int len = items.length();
              Random rand2 = new Random();
              int index2 = rand2.nextInt(len);
              String item = items.nth(index2).get();
              //if the item is qualified for being stolen
              if (Operations.ogetPath(all, "characters/player/inventory/" + item).get().asJNumber().get() >= 1 &&
                  !item.contains("sword")) {
                //update player inventory
                OWorld
                    sub2 =
                    OWorld.some(Operations.updatePath(sub.get(),
                        "characters/player/inventory/" + item,
                        oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - 1))).get().asJObject());
                //update thief inventory
                OWorld sub3 =
                    OWorld.some(Operations.updatePath(sub2.get(),
                        "characters/thief/stolenItem/" + item,
                        oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
                stolen = item;
                return OWorld.some(Operations.updatePath(sub3.get(), "time", oval -> oval.map(val
                    -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
              }
              //if the item is not qualified for being stolen
              item = "coin";
              //you have nothing
              if (Operations.ogetPath(sub.get(), "characters/player/inventory/" + item).isSome()) {
                if (Operations.ogetPath(sub.get(), "characters/player/inventory/" + item).get().asJNumber().get() <=
                    0) {
                  stolen = "nothing";
                  return OWorld.some(Operations.updatePath(sub.get(), "time", oval -> oval.map(val
                      -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
                }
              }
              //update player inventory
              OWorld sub2 = OWorld.some(Operations.updatePath(sub.get(),
                  "characters/player/inventory/" + item,
                  oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - 1))).get().asJObject());
              //update thief inventory
              OWorld sub3 =
                  OWorld.some(Operations.updatePath(sub2.get(),
                      "characters/thief/stolenItem/" + item,
                      oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
              stolen = item;
              return OWorld.some(Operations.updatePath(sub3.get(), "time", oval -> oval.map(val
                  -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
            }
            //not in the same room
            return OWorld.some(Operations.updatePath(sub.get(), "time", oval -> oval.map(val
                -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
          }
          //the thief died
          return OWorld.some(Operations.updatePath(world, "time", oval -> oval.map(val
              -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
        });

  }

  /**
   * If the item, "match" is used, the current room's property, "match" will be updated
   * such that the player will be able to "look" around the room
   * @param oWorld  current operations on stack
   * @return OWorld Operation that update's the Json file
   */
  static OWorld bright(@NotNull OWorld oWorld) {
    return oWorld.flatmap(
        world -> OWorld.some(Operations.updatePath(world, "match", oval -> Option.some(Builders.jnumber(1)))
            .get()
            .asJObject()));
  }


  /**
   * When a match has been burned for certain time units, it goes out
   * The room property is updated and room becomes dark again
   * @param oWorld  current operations on stack
   * @return OWorld Operation that update's the Json file
   */
  static OWorld dark(@NotNull OWorld oWorld) {
    return oWorld.flatmap(
        world -> OWorld.some(Operations.updatePath(world, "match", oval -> Option.some(Builders.jnumber(0)))
            .get()
            .asJObject()));
  }

  /**
   *
   * @param oWorld current operations on stack
   * @param item item to be used
   * @param target use the item on the target
   * @return OWorld Operation that update's the Json file
   */
  static OWorld useOn(@NotNull OWorld oWorld, String item, String target) {
    String roomName = pos.asJObject().oget("name").get().asJString().toUnescapedString();
    Value.JObject object = Operations.ogetPath(all, "characters/" + target).get().asJObject();
    return oWorld.flatmap(
        world -> {
          if (Operations.ogetPath(all, "characters/player/inventory/" + item).isSome()
              && Operations.ogetPath(all, "characters/" + target + "/status").isSome()
              && roomName.contains(object.oget("position").get().asJString().toUnescapedString())
              && Operations.ogetPath(all, "items/" + item + "/object").get()
              .asJString().toUnescapedString().contains(target)) {
            if (Operations.ogetPath(world, "characters/" + target + "/status")
                .get().asJNumber().get() == 1) {
              return OWorld.some(Operations.updatePath(world, "characters/" + target + "/status",
                  oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - 1))).get().asJObject());
            }
          }
          return OWorld.none();
        }
    );
  }

  /**
   *  When a player encounter certain event, it becomes lunatic and lose sense of
   *  direction for certian units of time
   * @param oWorld current operations on stack
   * @return updated world state
   */
  static OWorld pansy(@NotNull OWorld oWorld) {
    return oWorld.flatmap(
        world -> OWorld.some(Operations.updatePath(world, "pansy",
            oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() * (-1)))
        ).get().asJObject()));
  }

  /**
   *  When a player achieves something, some points are earned
   * @param oWorld current operations on stack
   * @return updated world state
   */
  static OWorld addPoint(@NotNull OWorld oWorld) {
    return oWorld.flatmap(
        world -> OWorld.some(Operations.updatePath(world, "score",
            oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() + 1))
        ).get().asJObject()));
  }

  static OWorld giveTo(@NotNull OWorld oWorld, String item, String target) {
    String roomName = pos.asJObject().oget("name").get().asJString().toUnescapedString();
    return oWorld.flatmap(world -> {
      if (Operations.ogetPath(all, "characters/player/inventory/" + item).isSome()
          && Operations.ogetPath(all, "characters/" + target).isSome()) {
        Value.JObject object = Operations.ogetPath(all, "characters/" + target).get().asJObject();
        if (roomName.contains(object.oget("position").get().asJString().toUnescapedString())
            && Operations.ogetPath(all, "items/" + item + "/target").get()
            .asJString().toUnescapedString().contains(target)) {

          //give coin to NPC to buy potion
          if (item.contains("coin")) {
            if (Operations.ogetPath(all, "characters/player/inventory/" + item).isSome()) {
              //if the player has enough money
              if (Operations.ogetPath(all, "characters/player/inventory/" + item).get().asJNumber().get() >= 5) {
                Value.JObject lessMoney = Operations.updatePath(world, "characters/player/inventory/" + item,
                    oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - 5))).get().asJObject();
                if (Operations.ogetPath(all, "characters/player/inventory/potion").isSome()) {
                  return OWorld.some(Operations.updatePath(lessMoney, "characters/player/inventory/potion",
                      oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() + 1))).get().asJObject());
                } else {
                  return OWorld.some(Operations.updatePath(lessMoney, "characters/player/inventory/potion",
                      oval -> Option.some(Builders.jnumber(1))).get().asJObject());
                }

              }
              return OWorld.some(world);
            }
          }
          //give gold to an NPC
          if (item.contains("gold")) {
            if (Operations.ogetPath(all, "characters/player/inventory/" + item).isSome()) {
              OWorld sub = OWorld.some(Operations.updatePath(world, "characters/player/inventory/" + item,
                  oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - 1))).get().asJObject());
              OWorld sub2 = OWorld.some(Operations.updatePath(sub.get(), "characters/" + target + "/status",
                  oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - 1))).get().asJObject());
              return OWorld.some(Operations.updatePath(sub2.get(), "characters/" + target + "/talk",
                  oval -> oval.map(val -> Builders.jsonString(
                      "You can unlock the lobby's door by saying 3358 to the lock"))).get().asJObject());
            }
          }
        }
      }
      return OWorld.none();
    });
  }


  /**
   *
   * @param oWorld current operations on stack
   * @param item item to get from the target
   * @param target target NPC
   * @return OWorld Operation that update's the Json file
   */
  static OWorld takeFrom(@NotNull OWorld oWorld, String item, String target) {

    String roomName = pos.asJObject().oget("name").get().asJString().toUnescapedString();
    return oWorld.flatmap(world -> {
      if (!target.contains("bag")) {
        return OWorld.none();
      }
      if (roomName.contains(Operations.ogetPath(all, "characters/thief/position").get().asJString().toUnescapedString())
          && Operations.ogetPath(all, "characters/thief/status").get().asJNumber().get() == 0) {
        if (Operations.ogetPath(all, "characters/thief/stolenItem/" + item).isNone()) {
          return OWorld.some(world);
        }
        if (Operations.ogetPath(all, "characters/thief/stolenItem/" + item).get().asJNumber().get() > 0) {
          double increase = Operations.ogetPath(all, "characters/thief/stolenItem/" + item).get().asJNumber().get();
          if (Operations.ogetPath(world, "characters/player/inventory/" + item).isSome()) {
            OWorld sub = OWorld.some(Operations.updatePath(world, "characters/player/inventory/" + item,
                oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() + increase))).get().asJObject());
            return OWorld.some(Operations.updatePath(sub.get(), "characters/thief/stolenItem/" + item,
                oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - increase))).get().asJObject());
          } else {
            OWorld sub = OWorld.some(Operations.updatePath(world, "characters/player/inventory/" + item,
                oval -> Option.some(Builders.jnumber(increase))).get().asJObject());
            return OWorld.some(Operations.updatePath(sub.get(), "characters/thief/stolenItem/" + item,
                oval -> oval.map(val -> Builders.jnumber(val.asJNumber().get() - increase))).get().asJObject());
          }
        }
        return OWorld.some(world);
      }
      return OWorld.none();
    });
  }

  /**
   * We use this "option world" class to represent the input and output type of all of our
   * monadic functions, so we can write everything in Adventure in terms of OWorld
   * rather than Option. Those OWolrd operations can be stored on stack and apply to the
   * json file compositely as MonadOp
   *
   */
  static class OWorld {
    @NotNull
    private static final OWorld NONE_SINGLETON = new OWorld(Option.none());

    @NotNull
    private final Option<Value.JObject> oWorld;

    private OWorld(@NotNull Option<Value.JObject> oWorld) {
      this.oWorld = oWorld;
    }

    @NotNull
    @Contract(pure = true)
    public static OWorld some(Value.JObject world) {
      return new OWorld(Option.some(world));
    }

    @NotNull
    @Contract(pure = true)
    public static OWorld none() {
      return NONE_SINGLETON;
    }

    @Contract(pure = true)
    public boolean isSome() {
      return oWorld.isSome();
    }

    @NotNull
    @Contract(pure = true)
    public Option<Value.JObject> oget() {
      return oWorld;
    }

    @NotNull
    @Contract(pure = true)
    public Value.JObject get() {
      return oWorld.get();
    }

    @NotNull
    @Contract(pure = true)
    public <T> T match(@NotNull Supplier<? extends T> noneFunc,
                       @NotNull Function<? super Value.JObject, ? extends T> someFunc) {
      return oWorld.match(noneFunc, someFunc);
    }

    @NotNull
    @Contract(pure = true)
    public OWorld flatmap(@NotNull Function<? super Value.JObject, ? extends OWorld> func) {
      return match(OWorld::none, func);
    }
  }

  /**
   *  Parse the input command and call the monadic function defined above compositely.
   * @param inputList the list of words entered into the command line
   * @return a monadic composition of functions
   */
  static MonadOp getFunction(@NotNull IList<String> inputList) {
    //east,west,south,north
    String roomName = pos.asJObject().oget("name").get().asJString().toUnescapedString();
    //time unit update
    MonadOp time = oWorld -> oneSec(oWorld);


    if (inputList.length() == 1) {
      if (inputList.contains("east") | inputList.contains("west") |
          inputList.contains("south") | inputList.contains("north") |
          inputList.contains("e") | inputList.contains("w") |
          inputList.contains("s") | inputList.contains("n")) {
        MonadOp function = oWorld -> oWorld;
        return time.andThen(function);
      }
    }


    if (inputList.length() == 2) {
      double match = all.asJObject().oget("match").get().asJNumber().get();
      double darkness = Operations.ogetPath(all, "rooms/" + roomName + "/darkness").get().asJNumber().get();
      if (inputList.head().contains("take")) {
        if (darkness == 0 | match == 1) {
          MonadOp function = oWorld -> take(oWorld, inputList.tail().head());
          MonadOp addPoint = oWorld -> addPoint(oWorld);
          IList<MonadOp> empty = List.makeEmpty();
          IList<MonadOp> registry = empty.add(time).add(function).add(addPoint);
          return registry.foldl(MonadOp.identity(), MonadOp::andThen);
        }
      }
      if (inputList.head().contains("use")) {
        if (inputList.tail().head().contains("match") & all.asJObject().oget("match").get().asJNumber().get() == 0) {
          MonadOp bright = oWorld -> bright(oWorld);
          MonadOp function = oWorld -> use(oWorld, inputList.tail().head());
          MonadOp addPoint = oWorld -> addPoint(oWorld);
          IList<MonadOp> empty = List.makeEmpty();
          IList<MonadOp> registry = empty.add(function).add(bright).add(time).add(addPoint);
//          IList<CalcOp> registry = empty.add(function).add(bright).add(time);
          return registry.foldl(MonadOp.identity(),  MonadOp::andThen);
        }

        if ("glass of unknown liquids".contains(inputList.tail().head())) {
          MonadOp pansy = oWorld -> pansy(oWorld);
          MonadOp function = oWorld -> use(oWorld, inputList.tail().head());
          MonadOp addPoint = oWorld -> addPoint(oWorld);
          IList< MonadOp> empty = List.makeEmpty();
          IList< MonadOp> registry = empty.add(function).add(addPoint).add(pansy).add(time);
          return registry.foldl(MonadOp.identity(), MonadOp::andThen);
        }
        MonadOp function = oWorld -> use(oWorld, inputList.tail().head());
        MonadOp addPoint = oWorld -> addPoint(oWorld);
        IList<MonadOp> empty = List.makeEmpty();
        IList<MonadOp> registry = empty.add(function).add(addPoint).add(time);
        return registry.foldl(MonadOp.identity(), MonadOp::andThen);
      }
      if (inputList.head().contains("drop")) {
        MonadOp function = oWorld -> drop(oWorld, inputList.tail().head());
        return time.andThen(function);
      }
    }


    if (inputList.length() == 4) {
      if (inputList.head().contains("use") && inputList.tail().tail().head().contains("on")) {
        MonadOp addPoint = oWorld -> addPoint(oWorld);
        String item = inputList.tail().head();
        String target = inputList.tail().tail().tail().head();
        MonadOp function = oWorld -> useOn(oWorld, item, target);
        if (item.contains("sword") && target.contains("thief")) {
          IList< MonadOp> empty = List.makeEmpty();
          IList< MonadOp> registry = empty.add(addPoint).add(function);
          return registry.foldl( MonadOp.identity(),  MonadOp::andThen);
        }
        IList< MonadOp> empty = List.makeEmpty();
        IList< MonadOp> registry = empty.add(addPoint).add(time).add(function);
        return registry.foldl( MonadOp.identity(),  MonadOp::andThen);
      }
      if (inputList.head().contains("give") && inputList.tail().tail().head().contains("to")) {
        String item = inputList.tail().head();
        String target = inputList.tail().tail().tail().head();
        MonadOp addPoint = oWorld -> addPoint(oWorld);
        MonadOp function = oWorld -> giveTo(oWorld, item, target);
        IList< MonadOp> empty = List.makeEmpty();
        IList< MonadOp> registry = empty.add(function).add(addPoint).add(time);
        return registry.foldl( MonadOp.identity(),  MonadOp::andThen);
      }
      if (inputList.head().contains("take") && inputList.tail().tail().head().contains("from")) {
        String item = inputList.tail().head();
        String target = inputList.tail().tail().tail().head();
        MonadOp addPoint = oWorld -> addPoint(oWorld);
        MonadOp function = oWorld -> takeFrom(oWorld, item, target);
        IList< MonadOp> empty = List.makeEmpty();
        IList< MonadOp> registry = empty.add(function).add(addPoint).add(time);
        return registry.foldl( MonadOp.identity(),  MonadOp::andThen);
      }
    }
    return time;
  }



  static MonadOp move(@NotNull IList<String> inputList) {
    //east,west,south,north
    if (inputList.length() == 1 && inputList.contains("east") | inputList.contains("west")  |
        inputList.contains("south")  | inputList.contains("north") |
        inputList.contains("e") | inputList.contains("w")  |
        inputList.contains("s") | inputList.contains("n")) {
      if (inputList.contains("east") | inputList.contains("e")) {
        return oWorld -> east(oWorld);
      }
      if (inputList.contains("west") | inputList.contains("w")) {
        return oWorld -> west(oWorld);
      }
      if (inputList.contains("south") | inputList.contains("s")) {
        return oWorld -> south(oWorld);
      }
      if (inputList.contains("north") | inputList.contains("n")) {
        return oWorld -> north(oWorld);
      }
    }
    return oWorld -> oWorld;
  }

  /**
   *Return random exit.
   */
  @NotNull
  @Contract(pure = true)
  public String randomExit() {
    IList<String> exits = pos.asJObject().oget("exits").get().asJObject().getMap().keys().map(key -> key.toLowerCase());
    int length = exits.length();
    Random rand = new Random();
    int index = rand.nextInt(length);
    return exits.nth(index).get();
  }

  /**
    * Process the input.
   * Split each word by empty space and make them all lower case
   */
  @NotNull
  @Contract(pure = true)
  public IList<String> inputProcessor(String input) {
    //split the input with whiteSpace
    String[] inputArray = input.split("\\s+");
    IList<String> inputList = LazyList.fromArray(inputArray).map(x -> x.toLowerCase());
    if (inputList.length() > 1) {
      if (inputList.head().contains("take") | inputList.head().contains("drop") | inputList.head().contains("look")
          | inputList.head().contains("use") | inputList.head().contains("say") && !inputList.tail().contains("on")
          && !inputList.tail().contains("to") && !inputList.tail().contains("from") ) {
        return List.make(inputList.tail().join(" ")).add(inputList.head());
      }
      if ("talk".contains(inputList.head()) && "to".contains(inputList.tail().head())) {
        return List.make(inputList.tail().tail().join(" ")).add(inputList.tail().head()).add(inputList.head());
      }
      if ("give".contains(inputList.head()) && inputList.tail().tail().head().contains("to")) {
        return (inputList.limit(3)).concat(List.make(inputList.tail().tail().tail().join(" ")));
      }
    }

    //if the play is in lunatic state, it selects a random direction
    if (all.asJObject().oget("pansy").get().asJNumber().get() == 1) {
      if (inputList.contains("east") | inputList.contains("west") |
          inputList.contains("south") | inputList.contains("north") |
          inputList.contains("e") | inputList.contains("w") |
          inputList.contains("s") | inputList.contains("n")) {
        return List.make(randomExit());
      }
    }
    return inputList;
  }

  /**
   * Given a string of input, this will tokenize it then execute it on the internal stack. The
   * state of the game after being updated is returned
   *
   * <p>Note: this method mutates the state of the class! If the input has no errors, then the resulting
   * stack state is saved internally. If the input has errors, then the stack state will be unchanged.
   */
  @NotNull
  public String response(@NotNull String input) {
    Value copy = all;
    //player's command input is returned
    String inputReturn = "> " + input + "<br>";
    IList<String> inputList = inputProcessor(input);
    MonadOp f = getFunction(inputList);

    String comment = "";
    String added = "<br>You score has increased by 1 point";
    //end: cannot finish the task in time
    if (remain == 0) {
      pos = Operations.ogetPath(all, "rooms/cage").get();
      remain = 1;
      gameOver = 0;
      return "The angry old man threw you into his cage";
    }

    //count down
    if (gameOver == 1 && remain > 0) {
      remain -= 1;
      comment = comment.concat("<br>You still have " + remain + " hours to finish your task <br>");
    }

    //match burnt out
    if (timer == all.asJObject().oget("time").get().asJNumber().get()) {
      f = f.andThen(oWorld -> dark(oWorld));
      timer = -1;
      comment = comment.concat("<br>Your match burns out");
    }
    //pansy time is over
    if (pansyTime == all.asJObject().oget("time").get().asJNumber().get()) {
      f = f.andThen(oWorld -> pansy(oWorld));
      pansyTime = -1;
      comment = comment.concat("<br> You are able to tell directions now");
    }

    //update the world with the monadic composition of functions
    OWorld resultWorld = f.apply(OWorld.some(all.asJObject()));
    //mutation: apply the functions stored on the stack
    if (!resultWorld.isSome()) {
      return inputReturn + comment + "there is no such a command";
    }
    all = resultWorld.get();

    MonadOp moveFunc = move(inputList);
    OWorld resultPos = moveFunc.apply(OWorld.some(pos.asJObject()));
    if (!resultPos.isSome()) {
      return inputReturn + comment + "there is no exit on this side";
    }
    pos = resultPos.get();


    double point = all.asJObject().oget("score").get().asJNumber().get();
    Value.JObject thief = Operations.ogetPath(all, "characters/thief").get().asJObject();
    Value.JObject dog = Operations.ogetPath(all, "characters/dog").get().asJObject();
    if (" ".contains(stolen)) {
      stolen = "nothing";
    }
    String roomName = pos.asJObject().oget("name").get().asJString().toUnescapedString();
    String
        description =
        Operations.ogetPath(all, "rooms/" + roomName + "/description").get().asJString().toUnescapedString();
    if (roomName.contains(thief.oget("position").get().asJString().toUnescapedString())) {
      if (thief.oget("status").get().asJNumber().get() == 1) {
        String
            description1 =
            thief.oget("description1")
                .get()
                .asJString()
                .toUnescapedString()
                .concat(stolen)
                .concat(" from you inventory");
        comment = comment.concat("<br>" + description1);
      }
      if (thief.oget("status").get().asJNumber().get() == 0) {
        String description2 = thief.oget("description2").get().asJString().toUnescapedString();
        comment = comment.concat("<br>" + description2);
      }
    }
    if (roomName.contains(dog.oget("position").get().asJString().toUnescapedString())) {
      if (dog.oget("status").get().asJNumber().get() == 1) {
        String description1 = dog.oget("description1").get().asJString().toUnescapedString();
        comment = comment.concat("<br>" + description1);
      }
      if (dog.oget("status").get().asJNumber().get() == 0) {
        String description2 = dog.oget("description2").get().asJString().toUnescapedString();
        comment = comment.concat("<br>" + description2);
      }
    }
    Value.JObject oldMan = Operations.ogetPath(all, "characters/old man").get().asJObject();
    if (roomName.contains(oldMan.oget("position").get().asJString().toUnescapedString())) {
      if (oldMan.oget("status").get().asJNumber().get() == 1) {
        String description1 = oldMan.oget("description1").get().asJString().toUnescapedString();
        comment = comment.concat("<br>" + description1);
      }
      if (oldMan.oget("status").get().asJNumber().get() == 0) {
        String description2 = oldMan.oget("description2").get().asJString().toUnescapedString();
        comment = comment.concat("<br>" + description2);
      }
    }
    Value.JObject merchant = Operations.ogetPath(all, "characters/merchant").get().asJObject();
    if (roomName.contains(merchant.oget("position").get().asJString().toUnescapedString())) {
      String des = merchant.oget("description").get().asJString().toUnescapedString();
      comment = comment.concat("<br>" + des);
    }
    double match = all.asJObject().oget("match").get().asJNumber().get();
    double darkness = Operations.ogetPath(all, "rooms/" + roomName + "/darkness").get().asJNumber().get();
    //return Strings
    if (inputList.length() == 1) {
      //start
      if (inputList.contains("start")) {
        all = Parser.parseJsonObject(JsonRoom).get();
        pos = all.asJObject().oget("rooms").get().asJObject().oget("base").get().asJObject();
        String initialRoom = pos.asJObject().oget("name").get().asJString().toUnescapedString();
        gameOver = 0;
        remain = 12;
        String des = pos.asJObject().oget("description").get().asJString().toUnescapedString();
        return inputReturn + initialRoom + "<br>" + des + "<br>" +
            "You can check the contents by entering \"look\"." + "<br>" +
            "You can check your inventory by entering \"inventory\"." + "<br>" +
            "You can go to other rooms by entering their directions";
      }
      if (inputList.contains("z")) {
        return inputReturn + "one hour passed" + comment;
      }
      if (inputList.contains("score")) {
        return inputReturn + point + comment;
      }

      //look
      if (inputList.contains("look") | inputList.contains("l")) {
        if (roomName.contains(dog.oget("position").get().asJString().toUnescapedString()) &
            dog.oget("status").get().asJNumber().get() == 1) {
          return inputReturn + comment;
        }
        //bright
        if (darkness == 0 | match == 1) {
          Value contents = Operations.ogetPath(all, "rooms/" + roomName + "/contents").get();
          IList<String> keys = contents.asJObject().getMap().keys();
          //nothing
          if (keys.empty()) {
            return inputReturn + roomName + "<br>" + description + "<br>You didn't find anything in the " + roomName +
                "<br><br> Exits:" + "<br>" +
                Operations.ogetPath(pos, "exits").get().asJObject().getMap().keys().join("<br>") + "<br>" + comment;
          }
          //contents
          String things = keys.map(key ->
              contents.asJObject().oget(key).get().asJNumber().toString() + " " + key).join("<br>");
          return inputReturn + roomName + "<br>" + description + "<br> <br> Contents:" + "<br>" + things
              + "<br><br> Exits:" + "<br>" +
              Operations.ogetPath(pos, "exits").get().asJObject().getMap().keys().join("<br>") + comment;
        }
        //dark
        return inputReturn + roomName + "<br>" + "The room is dark." + comment;
      }
      //time
      if (inputList.contains("time")) {
        return inputReturn + all.asJObject().oget("time").get().asJNumber().toString() + comment;
      }
      if (inputList.contains("inventory") | inputList.contains("i") | inputList.contains("inv")) {
        Value inventory = Operations.ogetPath(all, "characters/player/inventory").get();
        IList<String> keys = inventory.asJObject().getMap().keys();
        String things = keys.map(key ->
            inventory.asJObject().oget(key).get().asJNumber().toString() + " " + key).join("<br>");
        if (keys.empty()) {
          return inputReturn + "You have nothing" + comment;
        }
        return inputReturn + "<br> Inventory<br>" + things + comment;
      }

      // direction
      if (inputList.contains("east") | inputList.contains("west") |
          inputList.contains("south") | inputList.contains("north") |
          inputList.contains("e") | inputList.contains("w") |
          inputList.contains("s") | inputList.contains("n")) {
        if (darkness == 0 | match == 1) {
          return inputReturn + "went " + inputList.head() + "<br>" + roomName + "<br>" + description + comment;
        }
        return inputReturn + "went " + inputList.head() + "<br>" + roomName + "<br>" + "The room is dark." + comment;

      }
    }

    //look something
    if (inputList.length() == 2) {
      if (inputList.head().contains("look")) {
        if (roomName.contains(dog.oget("position").get().asJString().toUnescapedString()) &
            dog.oget("status").get().asJNumber().get() == 1) {
          return inputReturn + comment;
        }
        if (darkness == 1 && match == 0) {
          return inputReturn + "You can't look anything" + comment;
        }
        String item = inputList.tail().head();
        IList<String> keys = all.asJObject().oget("items").get().asJObject().getMap().keys();
        IList<String> list = keys.filter(key -> key.contains(item));

        if (list.empty()) {
          return inputReturn + "There is no " + item + comment;
        }

        String matchItem = list.head();
        if (Operations.ogetPath(pos, "contents/" + matchItem).isSome()
            | Operations.ogetPath(all, "characters/player/inventory/" + matchItem).isSome()) {
          String
              lookItem =
              Operations.ogetPath(all, "items/" + matchItem + "/description").get().asJString().toUnescapedString();
          return inputReturn + lookItem + comment;
        }
        return inputReturn + "You couldn't find " + matchItem + comment;
      }

      //say something
      if (inputList.length() == 2) {
        if (inputList.head().contains("say")) {
          String contents = " \"" + inputList.tail().head() + "\"";
          return inputReturn + "You said" + contents + " , but nothing happened" + comment;
        }
      }

      //take something
      if (inputList.head().contains("take")) {
        if (roomName.contains(dog.oget("position").get().asJString().toUnescapedString()) &
            dog.oget("status").get().asJNumber().get() == 1) {
          return inputReturn + comment;
        }
        if (darkness == 1 && match == 0) {
          return inputReturn + comment + "You can't see anything to take";
        }
        String item = inputList.tail().head();
        IList<String> keys = Operations.ogetPath(copy, "rooms/" + roomName + "/contents").get().asJObject().getMap().keys();
        String matchItem = keys.filter(key -> key.contains(item)).head();
        return inputReturn + "You took one " + matchItem + comment + added;
      }

      //use something
      if (inputList.head().contains("use")) {
        if (inputList.tail().head().contains("match")) {
          timer = resultWorld.get().oget("time").get().asJNumber().get() + 3;
          return inputReturn + "You struck the match" + comment + added;
        }
        if ("glass of unknown liquids".contains(inputList.tail().head())) {
          pansyTime = resultWorld.get().oget("time").get().asJNumber().get() + 3;
          return inputReturn + "You feel too dizzy to tell any direction" + comment;
        }
        String item = inputList.tail().head();
        IList<String> keys = Operations.ogetPath(copy, "characters/player/inventory").get().asJObject().getMap().keys();
        String matchItem = keys.filter(key -> key.contains(item)).head();
        return inputReturn + "You used the " + matchItem + comment + added;
      }

      //drop something
      if (inputList.head().contains("drop")) {
        String item = inputList.tail().head();
        IList<String> keys = Operations.ogetPath(copy, "characters/player/inventory").get().asJObject().getMap().keys();
        String matchItem = keys.filter(key -> key.contains(item)).head();
        return inputReturn + "You dropped the " + matchItem + comment;
      }
    }

    if (inputList.length() == 3) {
      if (inputList.head().contains("talk") && inputList.tail().head().contains("to")) {
        String target = inputList.tail().tail().head();
      //talk to npc
        if (Operations.ogetPath(copy, "characters/" + target + "/talk").isNone() |
            (Operations.ogetPath(copy, "characters/" + target).isNone())) {
          return inputReturn + "There is no " + target + " you can talk to" + comment;
        }
        if (!Operations.ogetPath(copy, "characters/" + target + "/position")
            .get()
            .asJString()
            .toUnescapedString()
            .contains(
                roomName)) {
          return inputReturn + "There is no " + target + " you can talk to" + comment;
        }
        if ("old man".contains(target) &&
            Operations.ogetPath(all, "characters/thief/status").get().asJNumber().get() == 1) {
          gameOver = 1;
        }
        String
            answer =
            Operations.ogetPath(all, "characters/" + target + "/talk").get().asJString().toUnescapedString();
        return inputReturn + "You talked to the " + target + "<br>" + target + " : " + answer + comment;
      }
    }

    //use item on npc
    if (inputList.length() == 4) {
      if (inputList.head().contains("use") && inputList.tail().tail().head().contains("on")) {
        String item = inputList.tail().head();
        String target = inputList.tail().tail().tail().head();
        return inputReturn + "You used " + item + " on the " + target + comment + added;
      }
      if (inputList.head().contains("give") && inputList.tail().tail().head().contains("to")) {
        String item = inputList.tail().head();
        String target = inputList.tail().tail().tail().head();
        if (item.contains("coin") && target.contains("merchant")) {
          if (Operations.ogetPath(copy, "characters/player/inventory/" + item).get().asJNumber().get() >= 5) {
            return inputReturn + "You bought the potion" + added + comment;
          }
          return inputReturn + "You don't have enough coins" + comment;
        }
        if (item.contains("gold") && target.contains("old man")) {
          gameOver = 0;
          return inputReturn + "You gave the gold to the old man" + comment + added;
        }
      }

      //take item from the npc
      if (inputList.head().contains("take") && inputList.tail().tail().head().contains("from")) {
        String item = inputList.tail().head();
        String target = inputList.tail().tail().tail().head();
        if (Operations.ogetPath(copy, "characters/thief/stolenItem/" + item).isNone()) {
          return inputReturn + "There is no " + item + "in the " + target + comment;
        }
        if (Operations.ogetPath(copy, "characters/thief/stolenItem/" + item).get().asJNumber().get() > 0) {
          return inputReturn + "You took " + item + " from the " + target + comment + added;
        }
        return inputReturn + "There is no " + item + " in the " + target + comment;
      }
      if (inputList.head().contains("say") && inputList.tail().tail().head().contains("to")) {
        String item = inputList.tail().head();
        String target = inputList.tail().tail().tail().head();
        if (Operations.ogetPath(all, "characters/" + target).isSome()
            && roomName.contains(Operations.ogetPath(all, "characters/" + target + "/position")
            .get().asJString().toUnescapedString())) {
          return inputReturn + "You said " + item + " to " + target + ", but nothing happened" + comment;
        }
        if ("lock".contains(target) &&
            pos.asJObject().oget("name").get().asJString().toUnescapedString().contains("lobby")) {
          if (item.contains("3358") && "3358".contains(item)) {
            pos = all.asJObject().oget("rooms").get().asJObject().oget("East of the House").get().asJObject();
            return inputReturn + "You said the correct password, you successfully got out of the house";
          }
          return inputReturn + "wrong password" + comment;
        }
        return inputReturn + "there is no " + target;
      }
    }

    //undefined command
    return inputReturn + "I don't understand your command" + comment;
  }

  /**
   * Every command typed in by the player is declared in terms of this interface.
   * This interface defines monadic composition ("andThen") and an identity operation,
   * MonadOp acts as stack to store a series operations, and those operations can be composed
   * by calling andThen()
   */
  @FunctionalInterface
  interface MonadOp {
    @NotNull
    OWorld apply(@NotNull OWorld input);

    @NotNull
    @Contract(pure = true)
    static MonadOp identity() {
      return x -> x;
    }

    @NotNull
    @Contract(pure = true)
    default MonadOp andThen(@NotNull MonadOp op) {
      return stack -> op.apply(this.apply(stack));
    }


  }

}
