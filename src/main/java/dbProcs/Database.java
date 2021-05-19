package dbProcs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import utils.PropertyNotFoundException;

/**
 * Used to create database connections using the FileInputProperties.readfile method to gather
 * property information Initiated by Getter.java, Setter.java <br>
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
public class Database {
  private static org.apache.log4j.Logger log = Logger.getLogger(Database.class);

  /**
   * This method is used by the application to open an connection to the database
   *
   * @param conn The connection to close
   * @throws SQLException
   */
  public static Connection getConnection(
      String driverType,
      String connectionURL,
      String dbOptions,
      String dbUsername,
      String dbPassword)
      throws SQLException {

    try {
      Class.forName(driverType).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    if (dbOptions.length() > 0) {
      connectionURL += "?" + dbOptions;
    }

    Connection conn = DriverManager.getConnection(connectionURL, dbUsername, dbPassword);

    return conn;
  }

  /**
   * This method is used by the application to close an open connection to a database server
   *
   * @param conn The connection to close
   */
  public static void closeConnection(Connection conn) {

    // log.debug("Closing database connection");
    try {
      conn.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method is used by the application to get a connection to the secure database sever based
   * on the input path to a specific properties file.
   *
   * @param ApplicationRoot The running context of the application.
   * @param path The path to the properties file to use for this connection. this is filtered for
   *     path traversal attacks
   * @return A connection to the secure database server
   */
  public static Connection getChallengeConnection(String ApplicationRoot, String path) {
    // Some over paranoid input validation never hurts.
    path = path.replaceAll("\\.", "").replaceAll("/", "");
    log.debug("Path = " + path);
    Connection conn = null;

    // Pull Driver and DB URL out of database.properties
    String props = Constants.DBPROP;

    String connectionURL = "";
    String driverType = "";
    String dbOptions = "";
    String username = "";
    String password = "";

    try {
      connectionURL = FileInputProperties.readfile(props, "databaseConnectionURL");
      driverType = FileInputProperties.readfile(props, "DriverType");
    } catch (FileNotFoundException e) {
      // db props file doesn't exist
      log.fatal("Did not find db properties file!");
      throw new RuntimeException(e);
    } catch (IOException e) {

      throw new RuntimeException(e);
    } catch (PropertyNotFoundException e) {
      // props file didn't contain this property
      throw new RuntimeException(e);
    }

    // Pull DB Schema, Schema User name and Schema Password from level specific
    // properties File

    props =
        new File(Database.class.getResource("/challenges/" + path + ".properties").getFile())
            .getAbsolutePath();
    log.debug("Level Properties File = " + path + ".properties");
    // Add DB Schema to the end of the connectionURL
    try {
      try {
        connectionURL += FileInputProperties.readfile(props, "databaseConnectionURL");
        username = FileInputProperties.readfile(props, "databaseUsername");
        password = FileInputProperties.readfile(props, "databasePassword");
      } catch (PropertyNotFoundException e) {
        // props file didn't contain this property
        log.fatal("Did not find db properties file!");

        throw new RuntimeException(e);
      }

      try {
        dbOptions = FileInputProperties.readfile(props, "databaseOptions");
      } catch (PropertyNotFoundException e) {
        // TODO Auto-generated catch block
        log.debug("Did not find database options, defaulting to empty");

        dbOptions = "";
      }
    } catch (FileNotFoundException e) {
      // challenge db props file doesn't exist
      // throw e;
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // log.debug("Connection URL: " + connectionURL);

    try {
      conn = getConnection(driverType, connectionURL, dbOptions, username, password);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return conn;
  }

  public static Connection getCoreConnection() {
    Connection conn = getCoreConnection("");

    return conn;
  }

  /**
   * Returns connection to core schema in database
   *
   * @param ApplicationRoot
   * @return Connection to core schema with admin privileges
   * @throws FileNotFoundException
   * @throws SQLException
   */
  public static Connection getCoreConnection(String ApplicationRoot) {
    Connection conn = null;

    // Pull Driver and DB URL out of database.properties

    String props = Constants.DBPROP;

    String connectionURL = "";
    String driverType = "";
    String dbOptions = "";
    String username = "";
    String password = "";

    try {

      try {
        connectionURL = FileInputProperties.readfile(props, "databaseConnectionURL");
        connectionURL += FileInputProperties.readfile(props, "databaseSchema");

        driverType = FileInputProperties.readfile(props, "DriverType");
        username = FileInputProperties.readfile(props, "databaseUsername");
        password = FileInputProperties.readfile(props, "databasePassword");
      } catch (PropertyNotFoundException e) {
        log.fatal("Could not find requested parameter in props file: " + e.toString());
        throw new RuntimeException(e);
      }

      try {
        dbOptions = FileInputProperties.readfile(props, "databaseOptions");
      } catch (PropertyNotFoundException e) {
        log.debug("Did not find database options, defaulting to empty");
        dbOptions = "";
      }

    } catch (FileNotFoundException e) {
      // db props file doesn't exist
      log.fatal("Could not find db props file!");
      throw new RuntimeException(e);

    } catch (IOException e) {
      log.fatal("Could not open db props file!");
      throw new RuntimeException(e);
    }

    try {
      conn = getConnection(driverType, connectionURL, dbOptions, username, password);
    } catch (SQLException e) {

      throw new RuntimeException(e);
    }

    return conn;
  }

  public static Connection getDatabaseConnection(String ApplicationRoot)
      throws SQLException, FileNotFoundException {
    return getDatabaseConnection(ApplicationRoot, false);
  }

  /**
   * This method is used by the application to get a connection to the secure database sever
   *
   * @param ApplicationRoot The running context of the application.
   * @return A connection to the secure database server
   * @throws SQLException
   * @throws FileNotFoundException
   */
  public static Connection getDatabaseConnection(String ApplicationRoot, boolean allowMulti)
      throws SQLException, FileNotFoundException {
    Connection conn = null;

    String props = Constants.DBPROP;

    String connectionURL = "";
    String driverType = "";
    String dbOptions = "";
    String username = "";
    String password = "";

    try {

      try {
        connectionURL = FileInputProperties.readfile(props, "databaseConnectionURL");
        driverType = FileInputProperties.readfile(props, "DriverType");

        username = FileInputProperties.readfile(props, "databaseUsername");
        password = FileInputProperties.readfile(props, "databasePassword");
      } catch (PropertyNotFoundException e) {
        log.fatal("Could not find requested parameter in props file: " + e.toString());
        throw new RuntimeException(e);
      }

      try {
        dbOptions = FileInputProperties.readfile(props, "databaseOptions");
      } catch (PropertyNotFoundException e) {
        log.debug("Did not find database options, defaulting to empty");
        dbOptions = "";
      }

    } catch (FileNotFoundException e) {
      // db props file doesn't exist
      throw e;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (allowMulti) {
      if (dbOptions.length() > 0) {
        dbOptions += "&";
      }
      dbOptions += "allowMultiQueries=yes";
    }

    conn = getConnection(driverType, connectionURL, dbOptions, username, password);

    return conn;
  }

  /**
   * This method is used by the application to get a connection to the secure database sever's SQL
   * injection Lesson schema
   *
   * @param ApplicationRoot The running context of the application.
   * @return A connection to the secure database server
   * @throws FileNotFoundException
   * @throws SQLException
   */
  public static Connection getSqlInjLessonConnection(String ApplicationRoot)
      throws FileNotFoundException, SQLException {
    Connection conn = null;

    // Pull Driver and DB URL out of database.properties
    String props = Constants.DBPROP;

    String connectionURL = "";
    String driverType = "";
    String username = "";
    String password = "";

    try {

      try {
        connectionURL = FileInputProperties.readfile(props, "databaseConnectionURL");
        driverType = FileInputProperties.readfile(props, "DriverType");

      } catch (PropertyNotFoundException e) {
        log.fatal("Could not find requested parameter in props file: " + e.toString());
        throw new RuntimeException(e);
      }

    } catch (FileNotFoundException e) {
      // db props file doesn't exist
      throw e;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Pull Schema, User name and Password from SqlInjLesson.properties
    props = ApplicationRoot + "/WEB-INF/classes/lessons/SqlInjLesson.properties";

    try {

      try {
        connectionURL += FileInputProperties.readfile(props, "databaseConnectionURL");
        username = FileInputProperties.readfile(props, "databaseUsername");
        password = FileInputProperties.readfile(props, "databasePassword");
      } catch (PropertyNotFoundException e) {
        log.fatal("Could not find requested parameter in props file: " + e.toString());
        throw new RuntimeException(e);
      }

    } catch (FileNotFoundException e) {
      // sql injection lesson file does not exist
      throw e;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    conn = getConnection(driverType, connectionURL, "", username, password);

    return conn;
  }
}
