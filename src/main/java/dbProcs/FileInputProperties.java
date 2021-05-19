package dbProcs;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import utils.PropertyNotFoundException;

/**
 * Locates the database Properties File for Database manipulation methods. This file contains the
 * application sign on credentials for the database. <br>
 * <br>
 * This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 * The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.<br>
 * You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Mark
 */
public class FileInputProperties {

  private static org.apache.log4j.Logger log = Logger.getLogger(FileInputProperties.class);

  /**
   * Reads the properties file for a specific property and returns it as a string.
   *
   * @param filename The file to read
   * @param property The name of the property to be found
   * @return The value of the specified property to be found
   * @throws FileNotFoundException TODO
   * @throws IOException TODO
   * @throws PropertyNotFoundException
   */
  @SuppressWarnings("deprecation")
  public static String readfile(String filename, String property)
      throws FileNotFoundException, IOException, PropertyNotFoundException {
    // log.debug("Debug: Properties filename: "+filename);
    File file = new File(filename);
    String temp = "";
    String result = "";
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    DataInputStream dis = null;

    // log.debug("Debug: Looking for Property: "+Property);
    fis = new FileInputStream(file);
    bis = new BufferedInputStream(fis);
    dis = new DataInputStream(bis);
    boolean bool = false;
    while (dis.available() != 0) {
      temp = dis.readLine();
      if (temp.contains(property)) {
        result = temp.substring(property.length() + 1, temp.length());
        // log.debug("Debug: Property Found: "+result);
        bool = true;
      }
    }
    fis.close();
    bis.close();
    dis.close();

    if (!bool) {
      log.debug("Debug: Property not found: " + property);
      throw new PropertyNotFoundException("Property " + property + " not found");
    }

    return result;
  }

  /**
   * Read a properties file and return the string result from the property specified
   *
   * @param filename the file to be written to
   * @param property the property value to return
   */
  public static String readPropFileClassLoader(String filename, String property)
      throws IOException {

    InputStream input = FileInputProperties.class.getClassLoader().getResourceAsStream(filename);
    Properties prop = new Properties();
    prop.load(input);

    return prop.getProperty(property);
  }
}
