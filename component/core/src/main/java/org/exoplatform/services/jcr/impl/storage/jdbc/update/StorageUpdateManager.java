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
package org.exoplatform.services.jcr.impl.storage.jdbc.update;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: StorageUpdateManager.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class StorageUpdateManager {
  
  protected static Log log = ExoLogger.getLogger("jcr.StorageUpdateManager");
  
  public static final String STORAGE_VERSION_1_0_0 = "1.0";
  public static final String STORAGE_VERSION_1_0_1 = "1.0.1";
  public static final String STORAGE_VERSION_1_1_0 = "1.1";
  public static final String STORAGE_VERSION_1_5_0 = "1.5";
  public static final String STORAGE_VERSION_1_6_0 = "1.6";
  public static final String STORAGE_VERSION_1_7_0 = "1.7";
  
  public static final String FIRST_STORAGE_VERSION = STORAGE_VERSION_1_0_0;
  
  public static final String PREV_STORAGE_VERSION = STORAGE_VERSION_1_6_0;
  
  public static final String REQUIRED_STORAGE_VERSION = STORAGE_VERSION_1_7_0;
  
  protected final String SQL_INSERT_VERSION;
  protected static final String SQL_INSERT_VERSION_MULTIDB = "insert into JCR_MCONTAINER(VERSION) values(?)";
  protected static final String SQL_INSERT_VERSION_SINGLEDB = "insert into JCR_SCONTAINER(VERSION) values(?)";
  
  protected final String SQL_UPDATE_VERSION;
  protected static final String SQL_UPDATE_VERSION_MULTIDB = "update JCR_MCONTAINER set VERSION=?";
  protected static final String SQL_UPDATE_VERSION_SINGLEDB = "update JCR_SCONTAINER set VERSION=?";
  
  protected final String SQL_SELECT_VERSION;
  protected static final String SQL_SELECT_VERSION_MULTIDB = "select VERSION from JCR_MCONTAINER";
  protected static final String SQL_SELECT_VERSION_SINGLEDB = "select VERSION from JCR_SCONTAINER";
  
  protected static final String SQL_UPDATE_JCRUUID_MULTIDB = "update JCR_MVALUE set DATA=? where ID=?";
  protected static final String SQL_UPDATE_JCRUUID_SINGLEDB = "update JCR_SVALUE set DATA=? where ID=?";
  
  protected static final String SQL_SELECT_JCRUUID_MULTIDB = 
    "select I.PATH, N.ID as NID, V.ID as VID, V.DATA from JCR_MITEM I, JCR_MNODE N, JCR_MPROPERTY P, JCR_MVALUE V " + 
    "WHERE I.ID = P.ID and N.ID = P.PARENT_ID and P.ID = V.PROPERTY_ID and " + 
    "I.PATH like '%" + Constants.JCR_UUID.getAsString() + "%' " + 
    "order by V.ID";
  
  protected static final String SQL_SELECT_JCRUUID_SINGLEDB = 
    "select I.PATH, N.ID as NID, V.ID as VID, V.DATA from JCR_SITEM I, JCR_SNODE N, JCR_SPROPERTY P, JCR_SVALUE V " + 
    "WHERE I.ID = P.ID and N.ID = P.PARENT_ID and P.ID = V.PROPERTY_ID and " + 
    "I.PATH like '%" + Constants.JCR_UUID.getAsString() + "%' " + 
    "order by V.ID";  
  
  protected static final String FROZENJCRUUID = "$FROZENJCRUUID$";
  
  protected static final String SQL_SELECT_FROZENJCRUUID_MULTIDB = 
    "select I.PATH, N.ID as NID, V.ID as VID, V.DATA from JCR_MITEM I, JCR_MNODE N, JCR_MPROPERTY P, JCR_MVALUE V " + 
    "WHERE I.ID = P.ID and N.ID = P.PARENT_ID and P.ID = V.PROPERTY_ID and " + 
    "I.PATH like '" + FROZENJCRUUID + "' " + 
    "order by V.ID";
  
  protected static final String SQL_SELECT_FROZENJCRUUID_SINGLEDB = 
    "select I.PATH, N.ID as NID, V.ID as VID, V.DATA from JCR_SITEM I, JCR_SNODE N, JCR_SPROPERTY P, JCR_SVALUE V " + 
    "WHERE I.ID = P.ID and N.ID = P.PARENT_ID and P.ID = V.PROPERTY_ID and " + 
    "I.PATH like '" + FROZENJCRUUID + "' " + 
    "order by V.ID";
  
  protected static final String SQL_SELECT_REFERENCES_MULTIDB = 
    "select I.PATH, V.PROPERTY_ID, V.ORDER_NUM, V.DATA"   
    + " from JCR_MITEM I, JCR_MPROPERTY P, JCR_MVALUE V"
    + " where I.ID=P.ID and P.ID=V.PROPERTY_ID and P.TYPE=" + PropertyType.REFERENCE
    + " order by I.ID, V.ORDER_NUM";
  
  protected static final String SQL_SELECT_REFERENCES_SINGLEDB = 
    "select I.PATH, V.PROPERTY_ID, V.ORDER_NUM, V.DATA"
    + " from JCR_SITEM I, JCR_SPROPERTY P, JCR_SVALUE V"
    + " where I.ID=P.ID and P.ID=V.PROPERTY_ID and P.TYPE=" + PropertyType.REFERENCE
    //+ " and I.CONTAINER_NAME=?" // An UUID contains container name as prefix
    + " order by I.ID, V.ORDER_NUM";
  
  protected static final String SQL_INSERT_REFERENCES_MULTIDB = "insert into JCR_MREF (NODE_ID, PROPERTY_ID, ORDER_NUM) values(?,?,?)";
  
  protected static final String SQL_INSERT_REFERENCES_SINGLEDB = "insert into JCR_SREF (NODE_ID, PROPERTY_ID, ORDER_NUM) values(?,?,?)";
  
  protected final String SQL_SELECT_JCRUUID;
  protected final String SQL_SELECT_FROZENJCRUUID;
  protected final String SQL_UPDATE_JCRUUID;
  protected final String SQL_SELECT_REFERENCES;
  protected final String SQL_INSERT_REFERENCES;

  private final Connection connection;
  
  private final String sourceName;
  
  private final boolean multiDB;

  private class JcrIdentifier {
    
    private final String path;
    private final String nodeIdentifier;
    private final String jcrIdentifier;
    private final String valueId;
    
    public JcrIdentifier(String path, String nodeIdentifier, String valueId, InputStream valueData) throws IOException {
      this.path = path;
      this.nodeIdentifier = nodeIdentifier;
      this.valueId = valueId;
      this.jcrIdentifier = new String(readIdentifierStream(valueData));
    }

    public String getNodeIdentifier() {
      return nodeIdentifier;
    }

    public String getJcrIdentifier() {
      return jcrIdentifier;
    }

    public String getPath() {
      return path;
    }

    public String getValueId() {
      return valueId;
    }
  }
  
  private abstract class Updater {
    
    protected abstract void updateBody(Connection conn) throws SQLException;
    
    public void update() throws SQLException {
      try {
        // fix before the version update
        updateBody(connection);
        
        PreparedStatement insertVersion = connection.prepareStatement(SQL_UPDATE_VERSION);
        insertVersion.setString(1, REQUIRED_STORAGE_VERSION);
        insertVersion.executeUpdate();
        
        connection.commit();
      } catch(Exception e) {
        try {
          connection.rollback();
        } catch(SQLException sqle) {
          log.warn("Error of update rollback: " + sqle.getMessage(), sqle);
        }
      }
    }
  }
  
  private class Updater100 extends Updater {
    
    @Override
    public void updateBody(Connection conn) throws SQLException {
      fixCopyIdentifierBug(conn); // to 1.0.1
      fillReferences(conn); // to 1.1
    }
  }
  
  private class Updater101 extends Updater {
    
    @Override
    public void updateBody(Connection conn) throws SQLException {
      fillReferences(conn);
    }
  }
  
  private StorageUpdateManager(String sourceName, Connection connection, boolean multiDB) throws SQLException {
    this.connection = connection;
    this.sourceName = sourceName;
    this.multiDB = multiDB;
    
    this.SQL_SELECT_VERSION = multiDB ? SQL_SELECT_VERSION_MULTIDB : SQL_SELECT_VERSION_SINGLEDB;
    this.SQL_INSERT_VERSION = multiDB ? SQL_INSERT_VERSION_MULTIDB : SQL_INSERT_VERSION_SINGLEDB;
    this.SQL_UPDATE_VERSION = multiDB ? SQL_UPDATE_VERSION_MULTIDB : SQL_UPDATE_VERSION_SINGLEDB;
    
    this.SQL_SELECT_JCRUUID = multiDB ? SQL_SELECT_JCRUUID_MULTIDB : SQL_SELECT_JCRUUID_SINGLEDB;
    this.SQL_SELECT_FROZENJCRUUID = multiDB ? SQL_SELECT_FROZENJCRUUID_MULTIDB : SQL_SELECT_FROZENJCRUUID_SINGLEDB;
    this.SQL_UPDATE_JCRUUID = multiDB ? SQL_UPDATE_JCRUUID_MULTIDB : SQL_UPDATE_JCRUUID_SINGLEDB;
    this.SQL_SELECT_REFERENCES = multiDB ? SQL_SELECT_REFERENCES_MULTIDB : SQL_SELECT_REFERENCES_SINGLEDB;
    this.SQL_INSERT_REFERENCES = multiDB ? SQL_INSERT_REFERENCES_MULTIDB : SQL_INSERT_REFERENCES_SINGLEDB;
  }
  
  /**
   * Check current storage version and update if updateNow==true 
   * @param ds
   * @param updateNow
   * @return
   * @throws RepositoryException
   */
  public static synchronized String checkVersion(String sourceName, Connection connection, boolean multiDB, boolean updateNow) throws RepositoryException {
    int transactIsolation = Connection.TRANSACTION_READ_COMMITTED;
    try {
      connection.setAutoCommit(false);
      transactIsolation = connection.getTransactionIsolation();
      connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      
      StorageUpdateManager manager = new StorageUpdateManager(sourceName, connection, multiDB);
      String version = manager.applyUpdate(updateNow);
      connection.commit();
      return version;
    } catch (Exception e) {
      try {
        connection.rollback();
      } catch(SQLException er) {
        log.warn("Error of connection rollback (close) " + er, er);
      }
      throw new RepositoryException(e);
    } finally {
      try {
        connection.setTransactionIsolation(transactIsolation);
        connection.close();
      } catch(SQLException e) {
        log.warn("Error of connection finalyzation (close) " + e, e);
      }
    }
  }
  
  
  /**
   *  (1) return current storage version if no updates required
   *  (2) return current storage version and print warning if updateNow==FALSE and NON CRITICAL updates required
   *  (3) throws Exception if updateNow==FALSE and CRITICAL updates required
   *  (4) apply updates, update and return updated version updateNow==TRUE and updates required
   *  
   * NOTE: after the update the JDBC connection will be closed
   * 
   * @param updateNow
   * @return
   * @throws Exception
   */
  private String applyUpdate(boolean updateNow) throws Exception {
    
      String curVersion = currentVersion();
          
      Updater updater = null; 
      
      
      if(curVersion.startsWith(STORAGE_VERSION_1_7_0)) {
        // ok
      } else {
        // warn
        log.warn("UPDATE IS NOT AVAILABLE from " + curVersion + " to " + STORAGE_VERSION_1_7_0 + 
            " using auto-update option. Use XML export/import to migrate to the next version of JCR. " +
        		"See for details: http://wiki.exoplatform.org/xwiki/bin/view/JCR/How+to+JCR+import+export. " +
        		"All data which were created prior (with " + curVersion + " and older) and stored in external value storage(s) will be unavailable with storage " + STORAGE_VERSION_1_7_0 + ". " +
        		"No auto-update changes was made to database.");
      }
      
      // was before 1.7
//      if(STORAGE_VERSION_1_0_0.equals(curVersion)) {
//        updater = new Updater100();
//      } else if(STORAGE_VERSION_1_0_1.equals(curVersion)) {
//        updater = new Updater101();
//      }
      
      if (updater != null)
        if(!updateNow) {
          log.warn("STORAGE VERSION OF " + sourceName + " IS " + curVersion 
              + " IT IS HIGHLY RECOMMENDED TO UPDATE IT TO " + REQUIRED_STORAGE_VERSION +
                   " ENABLE UPDATING in the CONFIGURATION:\n <container class='...'>\n"+
                   "  <properties> \n   <property name='update-storage' value='true'/> \n ...\n");
        } else {
          updater.update();
          curVersion = REQUIRED_STORAGE_VERSION;
        }
      
      return curVersion;
  }

  /**
   * @return current storage version
   * @throws SQLException
   */
  private String currentVersion() throws SQLException {
    ResultSet version = null;
    try {
      version = connection.createStatement().executeQuery(SQL_SELECT_VERSION);
      if(version.next())
        return version.getString("VERSION");
    } catch (SQLException e) {
      return FIRST_STORAGE_VERSION;
    } finally {
      if (version != null)
        version.close();
    }
    
    PreparedStatement insertVersion = connection.prepareStatement(SQL_INSERT_VERSION);
    insertVersion.setString(1, REQUIRED_STORAGE_VERSION);
    insertVersion.executeUpdate();
    return REQUIRED_STORAGE_VERSION;
  }
  
  /**
   * fix data in the container
   * @throws SQLException
   */
  private void fixCopyIdentifierBug(Connection conn) throws SQLException {
    // need to search all referenceable nodes and fix their 
    // property jcr:uuid with valid value (ID column of JCR_xITEM)
    
    ResultSet refs = null;
    PreparedStatement update = null;
    try {
      refs = conn.createStatement().executeQuery(SQL_SELECT_JCRUUID);
      update = conn.prepareStatement(SQL_UPDATE_JCRUUID);
      while (refs.next()) {
        try {
          JcrIdentifier jcrIdentifier = new JcrIdentifier(refs.getString("PATH"), 
              refs.getString("NID"), 
              refs.getString("VID"), 
              refs.getBinaryStream("DATA"));
          if (!jcrIdentifier.getNodeIdentifier().equals(jcrIdentifier.getJcrIdentifier())) {
            log.info("STORAGE UPDATE >>>: Property jcr:uuid have to be updated with actual value. Property: " + jcrIdentifier.getPath() 
                + ", actual:" + jcrIdentifier.getNodeIdentifier() + ", existed: " + jcrIdentifier.getJcrIdentifier());

            update.clearParameters();
            update.setBinaryStream(1, 
                new ByteArrayInputStream(jcrIdentifier.getNodeIdentifier().getBytes()), jcrIdentifier.getNodeIdentifier().length());
            update.setString(2, jcrIdentifier.getValueId());
            
            if (update.executeUpdate() != 1) {
              log.warn("STORAGE UPDATE !!!: More than one jcr:uuid property values were updated. Updated value id: " + jcrIdentifier.getValueId());
            } else {
              log.info("STORAGE UPDATE <<<: Property jcr:uuid update successful. Property: " + jcrIdentifier.getPath());
            }
            
            // [PN] 27.09.06 Need to be developed more with common versionHistory (of copied nodes) etc.
          }
        } catch(IOException e) {
          log.error("Can't read property value data: " + e.getMessage(), e);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (refs != null)
        refs.close();
      if (update != null)
        update.close();
    }
  }
  
  private void fixCopyFrozenIdentifierBug(JcrIdentifier jcrIdentifier, Connection conn) throws SQLException {
    String searchCriteria = "'" + Constants.JCR_VERSION_STORAGE_PATH.getAsString() 
      + ":1[]" + jcrIdentifier.getNodeIdentifier() + "%" + Constants.JCR_FROZENUUID.getAsString() + "%' ";
    
    ResultSet refs = null;
    PreparedStatement update = null;
    try {
      String sql = SQL_SELECT_FROZENJCRUUID.replaceAll(FROZENJCRUUID, searchCriteria);
      refs = conn.createStatement().executeQuery(SQL_SELECT_FROZENJCRUUID);
      while (refs.next()) {
        try {
          JcrIdentifier frozenIdentifier = new JcrIdentifier(refs.getString("PATH"), 
              refs.getString("NID"), 
              refs.getString("VID"), 
              refs.getBinaryStream("DATA"));
          if (!frozenIdentifier.getNodeIdentifier().equals(frozenIdentifier.getJcrIdentifier())) {
            log.info("VERSION STORAGE UPDATE >>>: Property jcr:frozenUuid have to be updated with actual value. Property: " 
                + frozenIdentifier.getPath() + ", actual:" + jcrIdentifier.getNodeIdentifier() + ", existed: " + frozenIdentifier.getJcrIdentifier());
          }
        } catch(IOException e) {
          log.error("Can't read property value data: " + e.getMessage(), e);
        }
      }
    } catch (SQLException e) {
      log.error("Fix of copy uuid bug. Storage update error: " + e.getMessage(), e);
    } finally {
      if (refs != null)
        refs.close();
      if (update != null)
        update.close();
    }
  }

  /**
   * fill JCR_XREF table with refernces values from JCR_XVALUE
   * @throws SQLException
   */
  private void fillReferences(Connection conn) throws SQLException {
    
    ResultSet refs = null;
    PreparedStatement update = null;
    try {
      refs = conn.createStatement().executeQuery(SQL_SELECT_REFERENCES);
      update = conn.prepareStatement(SQL_INSERT_REFERENCES);
      while (refs.next()) {
        try {
          String refNodeIdentifier = new String(readIdentifierStream(refs.getBinaryStream("DATA")));
          String refPropertyIdentifier = refs.getString("PROPERTY_ID");
          int refOrderNum = refs.getInt("ORDER_NUM");
          
          String refPropertyPath = refs.getString("PATH");
          
          log.info("INSERT REFERENCE >>> Property: " + refPropertyPath + ", " 
              + refPropertyIdentifier + ", " + refOrderNum + "; Node UUID: " + refNodeIdentifier);

          update.clearParameters();
          update.setString(1, refNodeIdentifier);
          update.setString(2, refPropertyIdentifier);
          update.setInt(3, refOrderNum);
          
          if (update.executeUpdate() != 1) {
            log.warn("INSERT REFERENCE !!!: More than one REFERENCE property was copied");
          } else {
            log.info("INSERT REFERENCE <<<: Done. Property: " + refPropertyPath);
          }
        } catch(IOException e) {
          log.error("Can't read property value data: " + e.getMessage(), e);
        }
      }
    } catch (SQLException e) {
      log.error("Fill references. Storage update error: " + e.getMessage(), e); 
    } finally {
      if (refs != null)
        refs.close();
      if (update != null)
        update.close();
    }
  }  
  
  private byte[] readIdentifierStream(InputStream stream) throws IOException{
    byte[] buf = new byte[IdGenerator.IDENTIFIER_LENGTH];
    stream.read(buf);
    return buf;
  }
}
