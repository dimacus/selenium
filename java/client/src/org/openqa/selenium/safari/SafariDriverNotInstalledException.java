package org.openqa.selenium.safari;

import org.openqa.selenium.WebDriverException;

/**
 * Exception thrown when the SafariDriver is not properly installed on the system
 */
public class SafariDriverNotInstalledException extends WebDriverException{

  public SafariDriverNotInstalledException(String message){
    super(message);
  }

}
