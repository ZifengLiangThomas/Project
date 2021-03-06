/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.web;

import edu.rice.util.Log;

import static j2html.TagCreator.*;
import static spark.Spark.*;
import static edu.rice.json.Builders.*;
import static edu.rice.web.Utils.*;

public class SimpleServer {
  private static final String TAG = "SimpleServer";

  /**
   * Main entry point; args are ignored.
   */
  public static void main(String[] args) {
    Log.i(TAG, "Starting!");

    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG); // set up an exception handler
    launchBrowser("http://localhost:4567/lcserver/");

    get("/hello", (request, response) -> "Hello, world");
    get("/hello2", (request, response) ->
        html().with(
            head().with(title("Hello, world")),
            body().with(
                h1("Hello, world"),
                p("This is some introductory test"))));

    get("/lowercase/", (request, response) -> {
      logSparkRequest(TAG, request);

      String commandLine = request.queryParams("input");
      Log.i(TAG, "commandLine: " + commandLine);

      // unfortunately, SparkJava returns null rather than using Options to indicate things that are missing
      if (commandLine != null) {
        response.status(200); // okay!
        response.header("cache-control", "no-cache"); // because we're regenerating it every time
        return jobject(jpair("response", commandLine.toLowerCase())).toString();
      }

      // if we got here, then the command line we wanted was absent
      Log.i(TAG, "empty command line");
      response.status(400); // bad request
      return jobject().toString(); // empty JSON object
    });

    get("/lcserver/", (request, response) ->
        html().with(
            muicssHeader("Comp215 Lowercase Server!", "/commandline.js", "/commandline.css"),
                                                                 // alt: "/commandline-dark.css"
            muicssCommandLineBody("Comp215 Lowercase Server!")));
  }
}
