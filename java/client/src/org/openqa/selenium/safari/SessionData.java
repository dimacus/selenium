package org.openqa.selenium.safari;

import com.google.common.collect.ImmutableList;

import org.openqa.selenium.Platform;
import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.io.IOException;

/**
 * Provides access to Safari's session data files.
 */
class SessionData {

  private final Iterable<File> sessionDataFiles;

  private SessionData(Iterable<File> sessionDataFiles) {
    this.sessionDataFiles = sessionDataFiles;
  }

  /**
   * @return The SessionData container for the current platform.
   */
  public static SessionData forCurrentPlatform() {
    Platform current = Platform.getCurrent();

    if (!current.is(Platform.MAC)) {
      throw new IllegalStateException("The current platform is not supported: " + current);
    }

    File libraryDir = new File("/Users", System.getenv("USER") + "/Library");
    Iterable<File> files = ImmutableList.of(
        new File(libraryDir, "Caches/com.apple.Safari/Cache.db"),
        new File(libraryDir, "Cookies/Cookies.binarycookies"),
        new File(libraryDir, "Cookies/Cookies.plist"),
        new File(libraryDir, "Safari/History.plist"),
        new File(libraryDir, "Safari/LastSession.plist"),
        new File(libraryDir, "Safari/LocalStorage"),
        new File(libraryDir, "Safari/Databases"));

    return new SessionData(files);
  }

  /**
   * Deletes all of the existing session data.
   *
   * @throws IOException If an I/O error occurs.
   */
  public void clear() throws IOException {
    for (File file : sessionDataFiles) {
      FileHandler.delete(file);
    }
  }
}
