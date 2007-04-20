/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
import org.exoplatform.services.jcr.util.UUIDGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: StorageUpdateManager.java 13867 2007-03-28 13:43:08Z peterit $
 */

public class StorageUpdateManager {
  
  protected static Log log = ExoLogger.getLogger("jcr.StorageUpdateManager");
  
  public static final String STORAGE_VERSION_1_0_0 = "1.0";
  public static final String STORAGE_VERSION_1_0_1 = "1.0.1";
  public static final String STORAGE_VERSION_1_1_0 = "1.1";
  public static final String STORAGE_VERSION_1_5_0 = "1.5";
  public static final String STORAGE_VERSION_1_6_0 = "1.6";
  
  public static final String FIRST_STORAGE_VERSION = STORAGE_VERSION_1_0_0;
  
  public static final String PREV_STORAGE_VERSION = STORAGE_VERSION_1_5_0;
  
  public static final String REQUIRED_STORAGE_VERSION = STORAGE_VERSION_1_6_0;
  
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

  private class JcrUuid {
    
    private final String path;
    private final String nodeUuid;
    private final String jcrUuid;
    private final String valueId;
    
    public JcrUuid(String path, String nodeUuid, String valueId, InputStream valueData) throws IOException {
      this.path = path;
      this.nodeUuid = nodeUuid;
      this.valueId = valueId;
      this.jcrUuid = new String(readUUIDStream(valueData));
    }

    public String getNodeUuid() {
      return nodeUuid;
    }

    public String getJcrUuid() {
      return jcrUuid;
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
      fixCopyUuidBug(conn); // to 1.0.1
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
  
//  static protected byte[] readInputStream(InputStream aStream) throws IOException {
//    byte[] buff = new byte[UUIDGenerator.UUID_LENGTH];
//
//    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//    int res = 0;
//    while ((res = aStream.read(buff)) > 0) {
//      baos.write(buff, 0, res);
//    }
//    BLOBUtil.readStream(aStream);
//    return baos.toByteArray();
//  }  
  
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
        //connection.setAutoCommit(true);
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
      
      if(STORAGE_VERSION_1_0_0.equals(curVersion)) {
        updater = new Updater100();
      } else if(STORAGE_VERSION_1_0_1.equals(curVersion)) {
        updater = new Updater101();
      }
      
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
      //e.printStackTrace();
      return FIRST_STORAGE_VERSION;
    } finally {
      if (version != null)
        version.close();
    }
    
    PreparedStatement insertVersion = connection.prepareStatement(SQL_INSERT_VERSION);
    insertVersion.setString(1, REQUIRED_STORAGE_VERSION);
    insertVersion.executeUpdate();
    // connection.commit(); will be done in checkVersion()
    return REQUIRED_STORAGE_VERSION;
  }

  /**
   * upadates from ver 1.0 to required
   * @throws Exception
   */
//  private void update100ToRequired() throws Exception {
//    
//    Connection conn = null;
//    try {
//      conn = ds.getConnection();
//      conn.setAutoCommit(false);
//      conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
//  
//      // fix before the version update
//      fixCopyUuidBug(conn);
//      
//      PreparedStatement insertVersion = conn.prepareStatement(SQL_UPDATE_VERSION);
//      insertVersion.setString(1, REQUIRED_STORAGE_VERSION);
//      insertVersion.executeUpdate();
//      
//      conn.commit();
//    } catch(Exception e) {
//      if (conn != null) {
//        try {
//          conn.rollback();
//        } catch(SQLException sqle) {
//          log.warn("Error of update rollback: " + sqle.getMessage(), sqle);
//        }
//      }
//    } finally {
//      if (conn != null)
//        conn.close();
//    }
//  }
  
  /**
   * fix data in the container
   * @throws SQLException
   */
  private void fixCopyUuidBug(Connection conn) throws SQLException {
    // TODO fix copy()/import() UUID bug
    
    // need to search all referenceable nodes and fix their 
    // property jcr:uuid with valid value (ID column of JCR_xITEM)
    
    ResultSet refs = null;
    PreparedStatement update = null;
    try {
      refs = conn.createStatement().executeQuery(SQL_SELECT_JCRUUID);
      update = conn.prepareStatement(SQL_UPDATE_JCRUUID);
      while (refs.next()) {
        try {
          // TODO jcr:frozenUuid
          JcrUuid jcrUuid = new JcrUuid(refs.getString("PATH"), 
              refs.getString("NID"), 
              refs.getString("VID"), 
              refs.getBinaryStream("DATA"));
          //log.info("jcr:uuid: " + jcrUuid.getPath() + ", actual:" + jcrUuid.getNodeUuid() + ", existed: " + jcrUuid.getJcrUuid());
          if (!jcrUuid.getNodeUuid().equals(jcrUuid.getJcrUuid())) {
            log.info("STORAGE UPDATE >>>: Property jcr:uuid have to be updated with actual value. Property: " + jcrUuid.getPath() 
                + ", actual:" + jcrUuid.getNodeUuid() + ", existed: " + jcrUuid.getJcrUuid());

            update.clearParameters();
            update.setBinaryStream(1, 
                new ByteArrayInputStream(jcrUuid.getNodeUuid().getBytes()), jcrUuid.getNodeUuid().length());
            update.setString(2, jcrUuid.getValueId());
            
            if (update.executeUpdate() != 1) {
              log.warn("STORAGE UPDATE !!!: More than one jcr:uuid property values were updated. Updated value id: " + jcrUuid.getValueId());
            } else {
              log.info("STORAGE UPDATE <<<: Property jcr:uuid update successful. Property: " + jcrUuid.getPath());
            }
            
            // TODO [PN] 27.09.06 Need to be developed more with common versionHistory (of copied nodes) etc.
            // fixCopyFrozenUuidBug(jcrUuid, conn);
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
  
  private void fixCopyFrozenUuidBug(JcrUuid jcrUuid, Connection conn) throws SQLException {
    String searchCriteria = "'" + Constants.JCR_VERSION_STORAGE_PATH.getAsString() 
      + ":1[]" + jcrUuid.getNodeUuid() + "%" + Constants.JCR_FROZENUUID.getAsString() + "%' ";
    
    ResultSet refs = null;
    PreparedStatement update = null;
    try {
      String sql = SQL_SELECT_FROZENJCRUUID.replaceAll(FROZENJCRUUID, searchCriteria);
      refs = conn.createStatement().executeQuery(SQL_SELECT_FROZENJCRUUID);
      //update = conn.prepareStatement(SQL_UPDATE_FROZENJCRUUID);
      while (refs.next()) {
        try {
          JcrUuid frozenUuid = new JcrUuid(refs.getString("PATH"), 
              refs.getString("NID"), 
              refs.getString("VID"), 
              refs.getBinaryStream("DATA"));
          //log.info("frozenUuid: " + jcrUuid.getPath() + ", actual:" + jcrUuid.getNodeUuid() + ", existed: " + jcrUuid.getJcrUuid());
          if (!frozenUuid.getNodeUuid().equals(frozenUuid.getJcrUuid())) {
            log.info("VERSION STORAGE UPDATE >>>: Property jcr:frozenUuid have to be updated with actual value. Property: " 
                + frozenUuid.getPath() + ", actual:" + jcrUuid.getNodeUuid() + ", existed: " + frozenUuid.getJcrUuid());

//            update.clearParameters();
//            update.setBinaryStream(1, 
//                new ByteArrayInputStream(jcrUuid.getNodeUuid().getBytes()), jcrUuid.getNodeUuid().length());
//            update.setString(2, jcrUuid.getValueId());
//            
//            if (update.executeUpdate() != 1) {
//              log.warn("VERSION STORAGE UPDATE !!!: More than one jcr:frozenUuid property values were updated. Updated value id: " + jcrUuid.getValueId());
//            } else {
//              log.info("VERSION STORAGE UPDATE <<<: Property jcr:uuid update successful. Property: " + jcrUuid.getPath());
//            }
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
          String refNodeUuid = new String(readUUIDStream(refs.getBinaryStream("DATA")));
          //String refNodeUuid = new String(BLOBUtil.readStream(refs.getBinaryStream("DATA")));
          String refPropertyUuid = refs.getString("PROPERTY_ID");
          int refOrderNum = refs.getInt("ORDER_NUM");
          
          String refPropertyPath = refs.getString("PATH");
          
          log.info("INSERT REFERENCE >>> Property: " + refPropertyPath + ", " 
              + refPropertyUuid + ", " + refOrderNum + "; Node UUID: " + refNodeUuid);

          update.clearParameters();
          update.setString(1, refNodeUuid);
          update.setString(2, refPropertyUuid);
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
  
  private byte[] readUUIDStream(InputStream stream) throws IOException{
    byte[] buf = new byte[UUIDGenerator.UUID_LENGTH];
    stream.read(buf);
    return buf;
  }
}
