package edu.rice.adventure;

/**
 * Created by liangzifeng on 10/26/16.
 */

import edu.rice.util.Log;

import static j2html.TagCreator.html;
import static edu.rice.json.Builders.*;
import static edu.rice.web.Utils.*;
import static spark.Spark.*;


public class AdventureServer {
  private static final String TAG = "gameServer";

  /**
   * Main method to initialize the web server; args are ignored.
   */
  public static void main(String[] args) {
    Log.i(TAG, "Starting!");

    Adventure newGame = new Adventure();
    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG); // set up an exception handler
    launchBrowser("http://localhost:4567/game/"); // help users find our server

    get("/gameServer/", (request, response) -> {
      logSparkRequest(TAG, request);

      String commandLine = request.queryParams("input");

      if (commandLine != null) {
        response.status(200); // okay!
        response.header("cache-control", "no-cache"); // because we're regenerating it every time
        return jobject(jpair("response", newGame.response(commandLine))).toString();
      }
      // if we got here, the command line we wanted was absent
      Log.i(TAG, "empty command line");
      response.status(400); // bad request
      return jobject().toString(); // empty JSON object
    });

    get("/game/", (request, response) ->
        html().with(
            muicssHeader("Adventure", "/game.js", "/commandline.css"),
            muicssCommandLineBody("Adventure")));
  }

}

