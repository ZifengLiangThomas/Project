/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.week7newspaper;

import edu.rice.json.Value;
import edu.rice.list.IList;
import edu.rice.list.KeyValue;
import edu.rice.list.List;
import edu.rice.tree.IMap;
import edu.rice.tree.TreapMap;
import edu.rice.util.Log;
import edu.rice.util.Option;
import edu.rice.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static edu.rice.json.Builders.*;
import static edu.rice.json.Operations.ogetPath;
import static edu.rice.json.Parser.parseJsonObject;
import static edu.rice.json.Value.*;

/**
 * A super-basic newspaper article database, validates that the JSON is well-formed as part of building up an author
 * database (i.e., a mapping from strings to Author records) and a list of Articles (which have a list of authors,
 * a title, and a body).
 *
 * <p>Note that if you were doing this for real, there are sophisticated JSON rules enforcement
 * engines where you describe all the data requirements up front. <a href="http://json-schema.org/">JSON-Schema</a>
 * is a standard for this sort of thing, and there are several validators out there that can use it.
 */
public interface DB {
  /**
   * This is the main entry point: given a raw string of JSON data, parseJsonObject it as JSON, then load up the authors
   * and articles.
   */
  @NotNull
  @Contract(pure = true)
  static Option<Pair<IMap<String, Author>, IList<Article>>> load(@NotNull String contents) {
    final String TAG = "DB.load";

    return parseJsonObject(contents).match(
        () -> Log.evalue(TAG, "error loading JSON data", Option.none()),
        results -> makeAuthorDB(results).match(
            () -> Log.evalue(TAG, "error loading author data", Option.none()),
            authorDB -> makeArticleList(results, authorDB).match(
                () -> Log.evalue(TAG, "error loading articles", Option.none()),
                articles -> Option.some(new Pair<>(authorDB, articles)))));
  }

  /**
   * Given the root of the JSON database, extracts the authors and returns the author database.
   */
  @NotNull
  @Contract(pure = true)
  static Option<IMap<String, Author>> makeAuthorDB(@NotNull Value input) {
    final String TAG = "DB.makeAuthorDB";

    // we require an authors field which has an array of authors inside

    IList<Value> jauthorList =
        ogetPath(input, "authors")
            .flatmap(Value::asOJArray)
            .match(List::makeEmpty, JArray::getList);

    IList<Author> authors = jauthorList.oflatmap(DB::makeAuthor);

    // if any of the authors failed, then that's an error for us
    if (authors.length() != jauthorList.length()) {
      Log.e(TAG, () ->
          String.format("failed to read authorDB: only %d of %d authors were complete",
              authors.length(),
              jauthorList.length()));
      return Option.none();
    }

    // we require that there are authors!
    if (authors.empty()) {
      Log.e(TAG, "failed to read authorDB: no authors found");
      return Option.none();
    }

    IMap<String, Author> authorDB = TreapMap.fromList(
        authors.map(author -> KeyValue.make(author.email, author)),
        (kv1, kv2) -> {
          Log.e(TAG, () -> String.format("the same author appears more than once: %s vs. %s",
              kv1.toString(), kv2.toString()));
          return kv1; // because we don't want to actually merge anything
        });

    // what if the same email happened twice? then the authorDB would only have it once, and that's an error
    if (authorDB.toList().length() != authors.length()) {
      return Option.none();
    }

    return Option.some(authorDB);
  }

  /**
   * Given the root of the JSON database and the author database, extracts a list of articles, ensuring that each
   * article has a valid author.
   */
  @NotNull
  @Contract(pure = true)
  static Option<IList<Article>> makeArticleList(@NotNull Value input, @NotNull IMap<String, Author> authorDB) {
    final String TAG = "DB.makeArticleList";

    // we require an articles field which has an array of articles inside
    return
        ogetPath(input, "articles")
            .flatmap(Value::asOJArray)
            .flatmap(jarticles -> {
              IList<Value> jarticleList = jarticles.getList();
              IList<Article> articles = jarticleList.oflatmap(val -> makeArticle(val, authorDB));

              // if any of the articles failed, then that's an error for us
              if (articles.length() != jarticleList.length()) {
                Log.e(TAG,
                    () -> String.format("failed to read articles: only %d of %d were complete",
                        articles.length(), jarticleList.length()));
                return Option.none();
              }

              return Option.some(articles);
            });
  }

  /**
   * Given a JSON Value, which should be a JSON object with the name and email fields defined, return an Optional
   * Author. If the building process fails, an Option.None is returned.
   */
  @NotNull
  @Contract(pure = true)
  static Option<Author> makeAuthor(@NotNull Value input) {
    // we require a name and an email field
    Option<Author> result =
        ogetPath(input, "name")
            .flatmap(Value::asOJString)
            .map(JString::toUnescapedString) // now we have Option<String> for the author's name
            .flatmap(name ->
                ogetPath(input, "email")
                    .flatmap(Value::asOJString)
                    .map(JString::toUnescapedString) // now we have Option<String> for the email as well
                    .flatmap(email -> Option.some(new Author(email, name))));

    if (!result.isSome()) {
      Log.e(Author.TAG, () -> "failed to read valid author: " + input.toString());
    }

    return result;
  }

  /**
   * Given a JSON Value, which should be a JSON object with the title, body, and authors fields defined, return an
   * Optional Article. If the building process fails, an Option.None returned.
   */
  @NotNull
  @Contract(pure = true)
  static Option<Article> makeArticle(@NotNull Value input, @NotNull IMap<String, Author> authorDB) {
    // we require a list of authors, a title, and a body
    final Option<String> otitle =
        ogetPath(input, "title")
            .flatmap(Value::asOJString)
            .map(JString::toUnescapedString);

    final Option<String> obody =
        ogetPath(input, "body")
            .flatmap(Value::asOJString)
            .map(JString::toUnescapedString);

    final Option<IList<Value>> oauthors =
        ogetPath(input, "authors")
            .flatmap(Value::asOJArray)
            .map(JArray::getList);

    return oauthors.flatmap(authors -> {
      // next, we need to transform this into a list of Authors
      final IList<Author> authorList = authors
          .oflatmap(val ->
              val.asOJString()
                  .flatmap(jstr -> authorDB.oget(jstr.toUnescapedString())));

      // and make sure the list didn't shrink, otherwise there was an error
      if (authorList.length() != authors.length()) {
        Log.e(Article.TAG, () -> String.format("failed to read article, one or more authors unknown: %s", input.toString()));
        return Option.none();
      }

      return authorList.match(
          emptyAuthors -> Log.evalue(Article.TAG,
              () -> String.format("failed to read article, no authors present: %s", input.toString()),
              Option.none()),
          (firstAuthor, remainingAuthors) ->
              otitle.match(
                  () -> Log.evalue(Article.TAG,
                      () -> String.format("failed to read article, no title present: %s", input.toString()),
                      Option.none()),
                  title -> obody.match(
                      () -> Log.evalue(Article.TAG,
                          () -> String.format("failed to read article, no body present: %s", input.toString()),
                          Option.none()),
                      body -> Option.some(new Article(authorList, title, body)))));
    });
  }



  class Author {
    private static final String TAG = "DB.Author";
    public final String email;
    public final String name;

    private Author(String email, String name) {
      this.email = email;
      this.name = name;
      // if we were doing this "for real", we'd have author bio information and other such stuff in here,
      // but the email address is the "key" to all this data
    }

    /**
     * Given an Author, this returns a JSON string corresponding to that Author.
     */
    @NotNull
    @Contract(pure = true)
    public String toString() {
      return toValue().toString();
    }

    /**
     * Given an Author, this returns a JSON data structure corresponding to that Author.
     */
    @NotNull
    @Contract(pure = true)
    public Value toValue() {
      return jobject(jpair("email", email), jpair("name", name));
    }
  }

  class Article {
    private static final String TAG = "DB.Article";
    public final IList<Author> authors;
    public final String title;
    public final String body;

    private Article(@NotNull IList<Author> authors, @NotNull String title, @NotNull String body) {
      this.authors = authors;
      this.title = title;
      this.body = body;
      // if we were doing this "for real", we'd have a whole separate concept of articles having "sections",
      // "dates", and various other metadata. We'd most importantly have unique article IDs, to act as
      // primary keys for the articles, letting us declare that an "issue" is just a list of articles, or
      // that articles can cross-reference one another. Never mind that the "body" would become a lot more
      // complex. Would we do HTML markup inside the JSON structure? Would we do something else?
    }

    /**
     * Given an Article, this returns a JSON data-structure representation of that Article.
     */
    @Contract(pure = true)
    public @NotNull Value toValue() {
      return jobject(
          jpair("title", title),
          jpair("body", body),
          jpair("authors",
              jarray(authors.map(Author::toValue))));
    }

    /**
     * Given an Article, this returns a JSON string of that Article.
     */
    public String toString() {
      return toValue().toString();
    }
  }
}
