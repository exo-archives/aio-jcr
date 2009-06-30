/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.storage.jdbc.init;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.services.log.Log;

import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS 12.03.2007 Generic db initializer.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class DBInitializer {

  static public String       SQL_DELIMITER                = ";";

  static public String       SQL_DELIMITER_COMMENT_PREFIX = "/*$DELIMITER:";

  static public String       SQL_CREATETABLE              = "^(CREATE(\\s)+TABLE(\\s)+(IF(\\s)+NOT(\\s)+EXISTS(\\s)+)*){1}";

  static public String       SQL_CREATEVIEW               = "^(CREATE(\\s)+VIEW(\\s)+(IF(\\s)+NOT(\\s)+EXISTS(\\s)+)*){1}";

  static public String       SQL_OBJECTNAME               = "((JCR_[A-Z_]+){1}(\\s*?|(\\(\\))*?)+)+?";

  static public String       SQL_CREATEINDEX              = "^(CREATE(\\s)+(UNIQUE(\\s)+)*INDEX(\\s)+){1}";

  static public String       SQL_ONTABLENAME              = "(ON(\\s)+(JCR_[A-Z_]+){1}(\\s*?|(\\(\\))*?)+){1}";

  static public String       SQL_CREATESEQUENCE           = "^(CREATE(\\s)+SEQUENCE(\\s)+){1}";

  static public String       SQL_CREATETRIGGER            = "^(CREATE(\\s)+(OR(\\s){1}REPLACE(\\s)+)*TRIGGER(\\s)+){1}";

  static public String       SQL_TRIGGERNAME              = "(([A-Z_]+JCR_[A-Z_]+){1}(\\s*?|(\\(\\))*?)+)+?";

  protected final Log        log                          = ExoLogger.getLogger("jcr.DBInitializer");

  protected final Connection connection;

  protected final String     containerName;

  protected final String     script;

  protected final boolean    multiDb;

  protected final Pattern    creatTablePattern;

  protected final Pattern    creatViewPattern;

  protected final Pattern    dbObjectNamePattern;

  protected final Pattern    creatIndexPattern;

  protected final Pattern    onTableNamePattern;

  protected final Pattern    creatSequencePattern;

  protected final Pattern    creatTriggerPattern;

  protected final Pattern    dbTriggerNamePattern;

  public DBInitializer(String containerName,
                       Connection connection,
                       String scriptPath,
                       boolean multiDb) throws IOException {
    this.connection = connection;
    this.containerName = containerName;
    this.script = script(scriptPath);
    this.multiDb = multiDb;

    this.creatTablePattern = Pattern.compile(SQL_CREATETABLE, Pattern.CASE_INSENSITIVE);
    this.creatViewPattern = Pattern.compile(SQL_CREATEVIEW, Pattern.CASE_INSENSITIVE);
    this.dbObjectNamePattern = Pattern.compile(SQL_OBJECTNAME, Pattern.CASE_INSENSITIVE);
    this.creatIndexPattern = Pattern.compile(SQL_CREATEINDEX, Pattern.CASE_INSENSITIVE);
    this.onTableNamePattern = Pattern.compile(SQL_ONTABLENAME, Pattern.CASE_INSENSITIVE);
    this.creatSequencePattern = Pattern.compile(SQL_CREATESEQUENCE, Pattern.CASE_INSENSITIVE);
    this.creatTriggerPattern = Pattern.compile(SQL_CREATETRIGGER, Pattern.CASE_INSENSITIVE);
    this.dbTriggerNamePattern = Pattern.compile(SQL_TRIGGERNAME, Pattern.CASE_INSENSITIVE);
  }

  protected String script(String scriptPath) throws IOException {
    return readScriptResource(scriptPath);
  }

  protected String readScriptResource(String path) throws IOException {
    InputStream is = this.getClass().getResourceAsStream(path);
    InputStreamReader isr = new InputStreamReader(is);
    try {
      StringBuilder sbuff = new StringBuilder();
      char[] buff = new char[is.available()];
      int r = 0;
      while ((r = isr.read(buff)) > 0) {
        sbuff.append(buff, 0, r);
      }

      return sbuff.toString();
    } finally {
      try {
        is.close();
      } catch (IOException e) {
      }
    }
  }

  public String cleanWhitespaces(String string) {
    if (string != null) {
      char[] cc = string.toCharArray();
      for (int ci = cc.length - 1; ci > 0; ci--) {
        if (Character.isWhitespace(cc[ci])) {
          cc[ci] = ' ';
        }
      }
      return new String(cc);
    }
    return string;
  }

  protected boolean isTableExists(Connection conn, String tableName) throws SQLException {
    ResultSet trs = conn.getMetaData().getTables(null, null, tableName, null);
    boolean res = false;
    while (trs.next()) {
      res = true; // check for columns/table type matching etc.
    }
    return res;
  }

  protected boolean isIndexExists(Connection conn, String tableName, String indexName) throws SQLException {
    ResultSet irs = conn.getMetaData().getIndexInfo(null, null, tableName, false, true);
    boolean res = false;
    while (irs.next()) {
      if (irs.getShort("TYPE") != DatabaseMetaData.tableIndexStatistic
          && irs.getString("INDEX_NAME").equalsIgnoreCase(indexName))
        res = true; // check for index params matching etc.
    }
    return res;
  }

  protected boolean isSequenceExists(Connection conn, String sequenceName) throws SQLException {
    return false;
  }

  protected boolean isTriggerExists(Connection conn, String triggerName) throws SQLException {
    return false;
  }

  public boolean isObjectExists(Connection conn, String sql) throws SQLException {
    Matcher tMatcher = creatTablePattern.matcher(sql);
    if (tMatcher.find()) {
      // CREATE TABLE
      tMatcher = dbObjectNamePattern.matcher(sql);
      if (tMatcher.find()) {
        // got table name
        String tableName = sql.substring(tMatcher.start(), tMatcher.end());
        if (isTableExists(conn, tableName)) {
          if (log.isDebugEnabled())
            log.debug("Table is already exists " + tableName);
          return true;
        }
      }
    } else if ((tMatcher = creatViewPattern.matcher(sql)).find()) {
      // CREATE VIEW
      tMatcher = dbObjectNamePattern.matcher(sql);
      if (tMatcher.find()) {
        // got view name
        String tableName = sql.substring(tMatcher.start(), tMatcher.end());
        if (isTableExists(conn, tableName)) {
          if (log.isDebugEnabled())
            log.debug("View is already exists " + tableName);
          return true;
        }
      }
    } else if ((tMatcher = creatIndexPattern.matcher(sql)).find()) {
      // CREATE INDEX
      tMatcher = dbObjectNamePattern.matcher(sql);
      if (tMatcher.find()) {
        // got index name
        String indexName = sql.substring(tMatcher.start(), tMatcher.end());
        if ((tMatcher = onTableNamePattern.matcher(sql)).find()) {
          String onTableName = sql.substring(tMatcher.start(), tMatcher.end());
          if ((tMatcher = dbObjectNamePattern.matcher(onTableName)).find()) {
            String tableName = onTableName.substring(tMatcher.start(), tMatcher.end());
            if (isIndexExists(conn, tableName, indexName)) {
              if (log.isDebugEnabled())
                log.debug("Index is already exists " + indexName);
              return true;
            }
          } else {
            log.warn("Index found but $TABLE_NAME is not detected '" + sql + "'");
          }
        } else {
          log.warn("Index found but ON $TABLE_NAME clause is not detected '" + sql + "'");
        }
      } else {
        log.warn("Create index clause found but $INDEX_NAME is not detected '" + sql + "'");
      }
    } else if ((tMatcher = creatSequencePattern.matcher(sql)).find()) {
      tMatcher = dbObjectNamePattern.matcher(sql);
      if (tMatcher.find()) {
        // got sequence name
        String sequenceName = sql.substring(tMatcher.start(), tMatcher.end());
        if (isSequenceExists(conn, sequenceName)) {
          if (log.isDebugEnabled())
            log.debug("Sequence is already exists " + sequenceName);
          return true;
        }
      }
    } else if ((tMatcher = creatTriggerPattern.matcher(sql)).find()) {
      tMatcher = dbTriggerNamePattern.matcher(sql);
      if (tMatcher.find()) {
        // got trigger name
        String triggerName = sql.substring(tMatcher.start(), tMatcher.end());
        if (isTriggerExists(conn, triggerName)) {
          if (log.isDebugEnabled())
            log.debug("Trigger is already exists " + triggerName);
          return true;
        }
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("Command is not detected for check '" + sql + "'");
    }

    return false;
  }

  public void init() throws DBInitializerException {
    String[] scripts = null;
    if (script.startsWith(SQL_DELIMITER_COMMENT_PREFIX)) {
      // read custom prefix
      try {
        String s = script.substring(SQL_DELIMITER_COMMENT_PREFIX.length());
        int endOfDelimIndex = s.indexOf("*/");
        String delim = s.substring(0, endOfDelimIndex).trim();
        s = s.substring(endOfDelimIndex + 2).trim();
        scripts = s.split(delim);
      } catch (IndexOutOfBoundsException e) {
        log.warn("Error of parse SQL-script file. Invalid DELIMITER configuration. Valid format is '"
                     + SQL_DELIMITER_COMMENT_PREFIX
                     + "XXX*/' at begin of the SQL-script file, where XXX - DELIMITER string."
                     + " Spaces will be trimed. ",
                 e);
        log.info("Using DELIMITER:[" + SQL_DELIMITER + "]");
        scripts = script.split(SQL_DELIMITER);
      }
    } else {
      scripts = script.split(SQL_DELIMITER);
    }

    String sql = null;
    try {
      connection.setAutoCommit(false);

      for (String scr : scripts) {
        String s = cleanWhitespaces(scr.trim());
        if (s.length() > 0) {
          if (isObjectExists(connection, sql = s))
            continue;

          if (log.isDebugEnabled())
            log.debug("Execute script: \n[" + sql + "]");

          connection.createStatement().executeUpdate(sql);
        }
      }

      rootInit(connection);

      optionalInit();

      connection.commit();
      log.info("DB schema of DataSource: '" + containerName + "' initialized succesfully");
    } catch (SQLException e) {
      try {
        connection.rollback();
      } catch (SQLException re) {
        log.error("Rollback error " + e, e);
      }

      SQLException next = e.getNextException();
      String errorTrace = "";
      while (next != null) {
        errorTrace += next.getMessage() + "; ";
        next = e.getNextException();
      }
      Throwable cause = e.getCause();
      String msg = "Could not create db schema of DataSource: '" + containerName + "'. Reason: "
          + e.getMessage() + "; " + errorTrace
          + (cause != null ? " (Cause: " + cause.getMessage() + ")" : "") + ". Last command: "
          + sql;

      throw new DBInitializerException(msg, e);
    } finally {
      try {
        connection.close();
      } catch (SQLException e) {
        log.error("Error of a connection closing. " + e, e);
      }
    }
  }

  /**
   * Empty here but may be overriden
   */
  protected void optionalInit() throws DBInitializerException {

  }

  /**
   * Init root node parent record
   */
  protected void rootInit(Connection connection) throws SQLException {
    final String MDB = (multiDb ? "M" : "S");
    String select = "select * from JCR_" + MDB + "ITEM where ID='" + Constants.ROOT_PARENT_UUID
        + "' and PARENT_ID='" + Constants.ROOT_PARENT_UUID + "'";

    if (!connection.createStatement().executeQuery(select).next()) {
      String insert = "insert into JCR_" + MDB + "ITEM(ID, PARENT_ID, NAME, "
          + (multiDb ? "" : "CONTAINER_NAME, ") + "VERSION, I_CLASS, I_INDEX, N_ORDER_NUM)"
          + " VALUES('" + Constants.ROOT_PARENT_UUID + "', '" + Constants.ROOT_PARENT_UUID
          + "', '__root_parent', " + (multiDb ? "" : "'__root_parent_container', ") + "0, 0, 0, 0)";

      connection.createStatement().executeUpdate(insert);
    }
  }
}
