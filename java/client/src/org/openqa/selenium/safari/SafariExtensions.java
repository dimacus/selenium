/*
Copyright 2012 Selenium committers
Copyright 2012 Software Freedom Conservancy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.openqa.selenium.safari;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.openqa.selenium.Platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the installation of the SafariDriver browser extension. This class will backup and
 * uninstall all extensions before installing the SafariDriver browser extension. The SafariDriver
 * may currently installed from one of two locations: <ol> <li>A pre-built extension included with
 * this jar.</li> <li>A pre-packaged Safari .safariextz file specified through the {@link
 * #EXTENSION_LOCATION_PROPERTY} system property.</li> </ol> To use a pre-installed version of the
 * SafariDriver, set the {@link #NO_INSTALL_EXTENSION_PROPERTY} to {@code true}.
 */
class SafariExtensions {

  private static final Logger logger = Logger.getLogger(SafariExtensions.class.getName());

  /**
   * System property that defines the location of an existing, pre-packaged SafariDriver extension
   * to install.
   */
  public static final String EXTENSION_LOCATION_PROPERTY = "webdriver.safari.driver";

  /**
   * System property that disables installing a prebuilt SafariDriver extension on start up.
   */
  public static final String NO_INSTALL_EXTENSION_PROPERTY = "webdriver.safari.noinstall";

  private static final String EXTENSION_RESOURCE_PATH = String.format(
      "/%s/SafariDriver.safariextz",
      SafariExtensions.class.getPackage().getName().replace('.', '/'));


  private final Runtime runtime;
  private final Optional<File> customDataDir;
  private final boolean installExtension;
  private final List<File> safariExtensionFiles;

  private List<File> installedExtensions;
  private File extensionPlist;

  /**
   * Installs the Driver extension and/or other user-defined extensions using a non-standard
   * directory for the system's Safari installation. The configuration is derived from the {@link
   * SafariOptions} instance. This configuration can be overridden by explicitly setting the {@link
   * #NO_INSTALL_EXTENSION_PROPERTY} system property, which prevents the SafariDriver from
   * installing or removing extensions.
   *
   * @param options A {@link SafariOptions} instance, which provides the configuration.
   * @see SafariOptions#dataDir
   * @see SafariOptions#useCustomDriverExtension
   * @see SafariOptions#skipExtensionInstallation
   * @see SafariOptions#extensionFiles
   */
  SafariExtensions(SafariOptions options) {
    this.runtime = Runtime.getRuntime();
    this.customDataDir = options.getDataDir();
    this.installExtension = !Boolean.getBoolean(NO_INSTALL_EXTENSION_PROPERTY) &&
                            !options.getUseCustomDriverExtension();
    this.safariExtensionFiles = options.getExtensions();
  }

  /**
   * @return Safari's application data directory for the current platform.
   * @throws IllegalStateException If the current platform is unsupported.
   */
  private static File getSafariDataDirectory() {
    Platform current = Platform.getCurrent();
    if (!current.is(Platform.MAC)) {
      throw new IllegalStateException("The current platform is not supported: " + current);
    }
    return new File("/Users/" + System.getenv("USER"), "Library/Safari");

  }

  /**
   * @param customDataDir Location of the data directory for a custom Safari installation. If
   *                      omitted, the {@link #getSafariDataDirectory() default data directory} is
   *                      used
   * @return The directory that the SafariDriver extension should be installed to for the current
   *         platform.
   * @throws IllegalStateException If the extension cannot be installed on the current platform.
   * @throws IOException           If an I/O error occurs.
   */
  private static File getInstallDirectory(Optional<File> customDataDir) throws IOException {
    File dataDir = customDataDir.or(getSafariDataDirectory());
    checkState(dataDir.isDirectory(),
               "The expected Safari data directory does not exist: %s",
               dataDir.getAbsolutePath());

    File extensionsDir = new File(dataDir, "Extensions");
    if (!extensionsDir.isDirectory()) {
      extensionsDir.mkdir();
    }
    return extensionsDir;
  }

  /**
   * Checks if SafariDriver extension is located in the Safari Extensions folder for current user
   *
   * @throws SafariDriverNotInstalledException
   *          if extension is not present
   */
  public void checkIfExtensionIsInstalled() throws IOException {
    File extensionPath = new File(getSafariDataDirectory(), "WebDriver.safariextz");
    if (!extensionPath.exists()) {
      throw new SafariDriverNotInstalledException(
          String.format(
              "Failed to locate WebDriver Safari Extension in %s",
              extensionPath.getAbsolutePath()
          )
      );
    }


  }

  private static Optional<ByteSource> getExtensionFromSystemProperties()
      throws FileNotFoundException {
    String extensionPath = System.getProperty(EXTENSION_LOCATION_PROPERTY);
    if (Strings.isNullOrEmpty(extensionPath)) {
      return Optional.absent();
    }

    File extensionSrc = new File(extensionPath);
    checkState(extensionSrc.isFile(),
               "The SafariDriver extension specified through the %s system property does not exist: %s",
               EXTENSION_LOCATION_PROPERTY, extensionPath);
    checkState(extensionSrc.canRead(),
               "The SafariDriver extension specified through the %s system property is not readable: %s",
               EXTENSION_LOCATION_PROPERTY, extensionPath);

    logger.info("Using extension " + extensionSrc.getAbsolutePath());

    return Optional.<ByteSource>of(Files.asByteSource(extensionSrc));
  }

  private static ByteSource getExtensionResource() {
    URL url = SafariExtensions.class.getResource(EXTENSION_RESOURCE_PATH);
    checkNotNull(url, "Unable to locate extension resource, %s", EXTENSION_RESOURCE_PATH);
    return Resources.asByteSource(url);
  }


}
