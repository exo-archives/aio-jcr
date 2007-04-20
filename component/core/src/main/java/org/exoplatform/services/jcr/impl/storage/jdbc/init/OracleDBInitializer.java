/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc.init;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by The eXo Platform SARL
 *
 * 22.03.2007
 *
 * For statistic compute on a user schema (PL/SQL):
 * exec DBMS_STATS.GATHER_SCHEMA_STATS(ownname=>'exoadmin')
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: OracleDBInitializer.java 13869 2007-03-28 13:50:50Z peterit $
 */
public class OracleDBInitializer extends DBInitializer {

  public OracleDBInitializer(String containerName, Connection connection, String scriptPath, boolean multiDb) throws IOException {
    super(containerName, connection, scriptPath, multiDb);
  }

  @Override
  protected boolean isSequenceExists(Connection conn, String sequenceName) throws SQLException {
    try {
      ResultSet srs = conn.createStatement().executeQuery("SELECT " + sequenceName + ".nextval FROM DUAL");
      if (srs.next()) {
        // TODO Does we have to back the sequence to previous value?
        //int nextVal = srs.getInt(1);
        //try {
        //  conn.createStatement().executeQuery("ALTER SEQUENCE " + sequenceName + " START WITH " + (nextVal - 1));
        //} catch(SQLException e) {
        //  log.warn("Sequence " + sequenceName + " detect error " + e);
        //}
        return true;
      }
      srs.close();
      return false;
    } catch(SQLException e) {
      // check: ORA-02289: sequence does not exist
      if (e.getMessage().indexOf("ORA-02289")>=0)
        return false;
      throw e;
    }
  }

  @Override
  protected boolean isTriggerExists(Connection conn, String triggerName) throws SQLException {
    //try {
      //conn.createStatement().executeUpdate("ALTER TRIGGER " + triggerName + " ENABLE");
      String sql = "SELECT COUNT(trigger_name) FROM all_triggers WHERE trigger_name = '" + triggerName + "'";
      ResultSet r = conn.createStatement().executeQuery(sql);
      if (r.next())
        return r.getInt(1)>0;
      else
        return false;
    //} catch(SQLException e) {
      // check: ORA-04080: trigger 'xxx' does not exist
      //if (e.getMessage().indexOf("ORA-04080")>=0)
      //  return false;
      //throw e;
    //}
  }

  @Override
  protected boolean isTableExists(Connection conn, String tableName) throws SQLException {
    try {
      conn.createStatement().executeUpdate("SELECT 1 FROM " + tableName);
      return true;
    } catch(SQLException e) {
      // check: ORA-00942: table or view does not exist
      if (e.getMessage().indexOf("ORA-00942")>=0)
        return false;
      throw e;
    }
  }

  @Override
  protected boolean isIndexExists(Connection conn, String tableName, String indexName)
      throws SQLException {

    //try {
      // CRASHES WITH ERR: Reason: ORA-02243: invalid ALTER INDEX or ALTER MATERIALIZED VIEW option
      // conn.createStatement().executeUpdate("ALTER INDEX " + indexName + " ENABLE");

      // use of oracle system view
      String sql = "SELECT COUNT(index_name) FROM all_indexes WHERE index_name='" + indexName + "'";
      ResultSet r = conn.createStatement().executeQuery(sql);
      if (r.next())
        return r.getInt(1)>0;
      else
        return false;
    //} catch(SQLException e) {
      // check: ORA-01418: specified index does not exist
      //if (e.getMessage().indexOf("ORA-01418")>=0)
      //return false;
      //throw e;
    //}
  }
}
