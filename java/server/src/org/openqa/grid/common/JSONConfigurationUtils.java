/*
Copyright 2011 Selenium committers
Copyright 2011 Software Freedom Conservancy

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

package org.openqa.grid.common;

import com.google.common.base.Throwables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.openqa.grid.common.exception.GridConfigurationException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class JSONConfigurationUtils {
  private static final Logger log = Logger.getLogger(JSONConfigurationUtils.class.getName());

  /**
   * load a JSON file from the resource or file system.
   * 
   * @param resource
   * @return A JsonObject representing the passed resource argument.
   */
  public static JsonObject loadJSON(String resource) {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
        JSONConfigurationUtils.class.getPackage().getName().replace('.', '/') + '/' + resource);

    if (in == null) {
      try {
        in = new FileInputStream(resource);
      } catch (FileNotFoundException e) {
        // ignore
      }
    }

    if (in == null) {
      throw new RuntimeException(resource + " is not a valid resource.");
    }

    StringBuilder b = new StringBuilder();
    InputStreamReader inputreader = new InputStreamReader(in);
    BufferedReader buffreader = new BufferedReader(inputreader);
    String line;

    try {
      while ((line = buffreader.readLine()) != null) {
        b.append(line);
      }
    } catch (IOException e) {
      throw new GridConfigurationException("Cannot read file " + resource + " , " + e.getMessage(), e);
    } finally {
      try {
        buffreader.close();
        inputreader.close();
        in.close();
      } catch (IOException e) {
        log.severe(String.format("Error closing buffer streams %s,\n%s", e.getMessage(),
                                 Throwables.getStackTraceAsString(e)));
      }
    }

    try {
      return new JsonParser().parse(b.toString()).getAsJsonObject();
    } catch (JsonSyntaxException e) {
      throw new GridConfigurationException("Wrong format for the JSON input : " + e.getMessage(), e);
    }
  }
}
