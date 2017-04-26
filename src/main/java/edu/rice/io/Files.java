/*
 * This code is part of Rice Comp215 and is made available for your use
 * as a student in Comp215. You are specifically forbidden from posting
 * this code online (e.g., on Github) or otherwise making it, or any derivative
 * of it, available to future Comp215 students. Violations of this rule are
 * considered Honor Code violations and will result in your being reported to
 * the Honor Council, even after you've completed the class, and will result
 * in retroactive reductions to your grade.
 */

package edu.rice.io;

import edu.rice.list.IList;
import edu.rice.list.LazyList;
import edu.rice.list.List;
import edu.rice.util.Log;
import edu.rice.util.Option;
import edu.rice.util.Try;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import spark.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * This class contains simple alternatives to all the crazy ways of reading/writing files and resources in Java.
 * Unlike the official Java ways of doing file IO, these static methods never throw an exception. Instead, they
 * use Try, which either has the successful value inside (much like an {@link edu.rice.util.Option#some(Object)}
 * or has an exception within (akin to {@link Option#none()}, but with the exception). This structure keeps your
 * code cleaner than with classical Java try/catch blocks.
 */
public class Files {
  private static final String TAG = "Files";

  private Files() { } // this class should never be instantiated

  @Contract(pure = true)
  private static @NotNull Try<InputStream> resourceToStream(@NotNull String resourceName) {
    // If ClassLoader.getSystemResourceAsStream finds nothing, it returns null, which ofNullable
    // will translate to Option.none().
    return Try.ofNullable(ClassLoader.getSystemResourceAsStream(resourceName))
      .logIfFailure(TAG, err -> "getSystemResources failed for resource(" + resourceName + ")");
  }

  /**
   * Given a resource name, which typically maps to a file in the "resources" directory, read it in and return a
   * String. This method assumes that the resource file is encoded as a UTF-8 string.
   * If you want to get raw bytes rather than a string, use {@link #readResourceBytes(String)} instead.
   *
   * @return a Try.success of the file contents as a String, or a Try.failure indicating what went wrong
   */
  @NotNull
  @Contract(pure = true)
  public static Try<String> readResource(@NotNull String resourceName) {
    return readResourceBytes(resourceName).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
  }

  /**
   * Given a resource name, which typically maps to a file in the "resources" directory, read it in and return an
   * array of bytes. This method assumes that the resource name is encoded as a UTF-8 string.
   * If you want the result as a String rather than an array of raw bytes, use {@link #readResource(String)} instead.
   *
   * @return a Try.success of the file contents as a byte array, or a Try.failure indicating what went wrong
   */
  @NotNull
  @Contract(pure = true)
  public static Try<byte[]> readResourceBytes(@NotNull String resourceName) {
    return resourceToStream(resourceName)
        .flatmap(stream ->
            Try.of(() -> IOUtils.toByteArray(stream))
                .andThen(stream::close));
  }

  /**
   * Given a directory path into the resources, returns a list of resource names suitable for then passing
   * to {@link #readResource(String)}, {@link #resourceToStream(String)}, etc.
   *
   * @return a Try.success of the list of resource names, or a Try.failure indicating what went wrong
   */
  @NotNull
  @Contract(pure = true)
  public static Try<IList<String>> readResourceDir(@NotNull String dirPath) {
    // Solution adapted from here:
    // http://www.uofr.net/~greg/java/get-resource-listing.html
    Log.i(TAG, "starting readResourceDir(" + dirPath + ")");

    return Try.of(() -> LazyList.fromEnumeration(ClassLoader.getSystemResources(dirPath)))
        .logIfFailure(TAG, err -> "getSystemResources failed for path(" + dirPath + ")")

        // map and then flatmap? ClassLoader.getSystemResources gives us a *list* of URLs
        // (for example, one directory from the main resources and another from the test resources).
        // The first map() just unpacks the IList<URL> from within the Try. The flatmap() after
        // that operates on the IList, where we map each URL to a list of files available at that URL.
        // For that reason, you'll notice that we eat errors below here, since we might succeed for
        // one URL and we might fail for another. We just want to return a list of the successful
        // files. Errors will be logged and that's it.

        .map(dirUrls -> dirUrls.flatmap(dirUrl -> {
          final String rawUrlPath = dirUrl.getPath();
          switch (dirUrl.getProtocol()) {
            case "file":

              // On Windows, we get URL paths like file:/C:/Users/dwallach/....
              // On Macs, we get URL paths like file:/Users/dwallach/...

              // With those Windows URLs, getPath() will give us /C:/Users/... which doesn't
              // work when we try to actually open the files. The solution? Match a regular
              // expression and then remove the leading slash.

              final String urlPath = (rawUrlPath.matches("^/\\p{Upper}:/.*$"))
                      ? rawUrlPath.substring(1)
                      : rawUrlPath;

              return readdir(urlPath).getOrElse(List.makeEmpty())

                  // readdir is going to give us fully qualified paths (starting with a /),
                  // because that's what we're getting out of the URLs. That's okay-ish,
                  // but it's preferable to get back to the relative paths that we started with.
                  // So, if we see the original dirPath at the end of the urlPath (which is
                  // a directory-name, not a file-name --- the file name will be pathString),
                  // then we can truncate the front-matter of the path.

                  .map(pathString -> (urlPath.endsWith(dirPath))
                          ? pathString.substring(urlPath.length() - dirPath.length())
                          : pathString);

            case "jar":
              String jarPath = rawUrlPath.substring(5, rawUrlPath.indexOf("!")); //strip out only the JAR file

              try {
                // This code is somewhat likely to work, but could be slow for huge JAR files.
                // Testing & optimization would be necessary, but Comp215 isn't going to
                // use Jar files for its resources, so we'll leave this as "good enough" for now.
                return LazyList.fromEnumeration(
                    new JarFile(URLDecoder.decode(jarPath, "UTF-8")).entries())
                    .map(ZipEntry::getName)
                    .filter(name -> name.startsWith(dirPath));

              } catch (IOException exception) {
                Log.e(TAG, "trouble reading " + dirUrl + ", ignoring and marching onward", exception);
                return LazyList.makeEmpty();
              }

            default:
              Log.e(TAG, "unknown protocol in " + dirUrl);
              return LazyList.makeEmpty();
          }
        }));
  }

  /**
   * Given a filename, read it in and return a String. This method
   * assumes that the file is encoded as a UTF-8 string. If you want the result as an array of raw
   * bytes rather than a String, use {@link #readBytes(String)} instead.
   *
   * @return a Try.success of the file contents as a String, or a Try.failure indicating what went wrong
   */
  @NotNull
  @Contract(pure = true)
  public static Try<String> read(@NotNull String filePath) {
    return readBytes(filePath).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
  }

  /**
   * Given a filename, read it in and return the contents as an array of bytes. If you want the result
   * as a String, use {@link #read(String)} instead.
   *
   * @return a Try.success of the file contents as an array of bytes, or a Try.failure indicating what went wrong
   */
  @NotNull
  @Contract(pure = true)
  public static Try<byte[]> readBytes(@NotNull String filePath) {
    return Try.of(() -> java.nio.file.Files.readAllBytes(Paths.get(filePath)))
        .logIfFailure(TAG, ex -> "failed to read file(" + filePath + ")");
  }

  /**
   * The given string of data is written to a file of the requested name, all at once.
   *
   * @return an empty Try.success if everything goes well, or a Try.failure indicating what went wrong
   */
  @NotNull
  public static Try<Void> write(@NotNull String filePath, @NotNull String data) {
    return writeBytes(filePath, data.getBytes());
  }

  /**
   * The given byte-array of data is written to a file of the requested name, all at once.
   *
   * @return an empty Try.success if everything goes well, or a Try.failure indicating what went wrong
   */
  @NotNull
  public static Try<Void> writeBytes(@NotNull String filePath, @NotNull byte[] rawData) {
    return Try.of(() ->
        java.nio.file.Files.write(Paths.get(filePath),
            rawData,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING))
        .logIfFailure(TAG, ex -> "failed to write file(" + filePath + ")")
        .toTryVoid();
  }

  /**
   * If the file is present, it's removed.
   *
   * @return an empty Try.success if everything goes well, or a Try.failure indicating what went wrong
   */
  @NotNull
  public static Try<Void> remove(@NotNull String filePath) {
    return Try.ofRunnable(() -> java.nio.file.Files.delete(Paths.get(filePath)))
        .logIfFailure(TAG, ex -> "failed to remove file(" + filePath + ")");
  }

  /**
   * Given a directory path, this returns a list of all files (or subdirectories) in that directory,
   * excluding "." and "..". If nothing is actually there, then an empty list will be returned.
   *
   * @return a Try.success of the list of resource names, or a Try.failure indicating what went wrong
   */
  @NotNull
  @Contract(pure = true)
  public static Try<IList<String>> readdir(@NotNull String filePath) {
    return Try.of(() -> java.nio.file.Files.newDirectoryStream(Paths.get(filePath)))
        .logIfFailure(TAG, ex -> "failed to read directory(" + filePath + ")")
        .map(dirs -> List.fromIterator(dirs.iterator()).map(Path::toString));
  }
}
