/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

/**
 * Created by The eXo Platform SARL
 *
 * 20.03.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: MySQLMultiDbJDBCConnection.java 13869 2007-03-28 13:50:50Z peterit $
 */
public class MySQLMultiDbJDBCConnection extends MultiDbJDBCConnection {

  public MySQLMultiDbJDBCConnection(Connection dbConnection,
      String containerName, ValueStoragePluginProvider valueStorageProvider,
      int maxBufferSize, File swapDirectory, FileCleaner swapCleaner) throws SQLException {
  
    super(dbConnection, containerName, valueStorageProvider, 
        maxBufferSize, swapDirectory, swapCleaner);
  }

  @Override
  protected void prepareQueries() throws SQLException {
    
    super.prepareQueries(); // INSERT, UPDATE, DELETE
    
    FIND_ITEM_BY_ID = "select SQL_CACHE I.*, N.ID as NID, N.ORDER_NUM as NORDER_NUM, N.PARENT_ID as NPARENT_ID, P.ID as PID, P.TYPE as PTYPE, P.PARENT_ID as PPARENT_ID, P.MULTIVALUED as PMULTIVALUED" 
      + " from JCR_MITEM I LEFT JOIN JCR_MNODE N ON I.ID=N.ID LEFT JOIN JCR_MPROPERTY P ON I.ID=P.ID"
      + " where I.ID=?";

    // select item(s) 
    FIND_ITEM_BY_PATH = "select SQL_CACHE I.*, N.ID as NID, N.ORDER_NUM as NORDER_NUM, N.PARENT_ID as NPARENT_ID, P.ID as PID, P.TYPE as PTYPE, P.PARENT_ID as PPARENT_ID, P.MULTIVALUED as PMULTIVALUED" 
      + " from JCR_MITEM I LEFT JOIN JCR_MNODE N on I.ID=N.ID LEFT JOIN JCR_MPROPERTY P on I.ID=P.ID"
      + " where I.PATH=? order by I.VERSION DESC"; 
    
    FIND_CHILD_PROPERTY_BY_PATH = "select SQL_CACHE I.*, P.ID as PID, P.TYPE as PTYPE, P.PARENT_ID as PPARENT_ID, P.MULTIVALUED as PMULTIVALUED" 
      + " from JCR_MPROPERTY P, JCR_MITEM I"
      + " where P.PARENT_ID=? and I.PATH=? and I.ID=P.ID order by I.VERSION DESC";
    
    FIND_REFERENCES = "select SQL_CACHE R.NODE_ID as NID, R.PROPERTY_ID as PID, I.PATH, I.VERSION, P.TYPE as PTYPE, P.PARENT_ID as PPARENT_ID, P.MULTIVALUED as PMULTIVALUED" +
        " from JCR_MREF R, JCR_MITEM I, JCR_MPROPERTY P" +
        " where R.PROPERTY_ID=I.ID and R.PROPERTY_ID=P.ID and I.ID=P.ID and R.NODE_ID=?";
    
    FIND_VALUES_BY_PROPERTYID = "select SQL_CACHE * from JCR_MVALUE where PROPERTY_ID=? order by ORDER_NUM";
    FIND_VALUE_BY_PROPERTYID_OREDERNUMB = "select SQL_CACHE DATA from JCR_MVALUE where PROPERTY_ID=? and ORDER_NUM=?";
    
    FIND_NODES_BY_PARENTID = "select SQL_CACHE I.*, N.ID as NID, N.ORDER_NUM as NORDER_NUM, N.PARENT_ID as NPARENT_ID" 
      + " from JCR_MNODE N, JCR_MITEM I"
      + " where I.ID=N.ID and N.PARENT_ID=?"
      // [PN] 26.12.06
      //+ " order by I.ID"; 
      + " order by NORDER_NUM"; 
    
    FIND_PROPERTIES_BY_PARENTID = "select SQL_CACHE I.*, P.ID as PID, P.TYPE as PTYPE, P.PARENT_ID as PPARENT_ID, P.MULTIVALUED as PMULTIVALUED" 
      + " from JCR_MPROPERTY P, JCR_MITEM I"
      + " where I.ID=P.ID and P.PARENT_ID=?" 
      + " order by I.ID";
    
    FIND_ITEM_BY_ID = FIND_ITEM_BY_ID.replace("?", "%s");
    FIND_ITEM_BY_PATH = FIND_ITEM_BY_PATH.replace("?", "%s");
    FIND_CHILD_PROPERTY_BY_PATH = FIND_CHILD_PROPERTY_BY_PATH.replace("?", "%s");
    FIND_REFERENCES = FIND_REFERENCES.replace("?", "%s");
    FIND_VALUES_BY_PROPERTYID = FIND_VALUES_BY_PROPERTYID.replace("?", "%s");
    FIND_VALUE_BY_PROPERTYID_OREDERNUMB = FIND_VALUE_BY_PROPERTYID_OREDERNUMB.replace("?", "%s");
    FIND_NODES_BY_PARENTID = FIND_NODES_BY_PARENTID.replace("?", "%s");
    FIND_PROPERTIES_BY_PARENTID = FIND_PROPERTIES_BY_PARENTID.replace("?", "%s");
  }
  
  @Override
  protected ResultSet findChildNodesByParentUUID(String parentUUID) throws SQLException {
    return dbConnection.createStatement().executeQuery(
        String.format(FIND_NODES_BY_PARENTID, "'" + parentUUID + "'"));
  }

  @Override
  protected ResultSet findChildPropertiesByParentUUID(String parentUUID) throws SQLException {
    return dbConnection.createStatement().executeQuery(
        String.format(FIND_PROPERTIES_BY_PARENTID, "'" + parentUUID + "'"));
  }

  @Override
  protected ResultSet findItemByPath(String path) throws SQLException {
    return dbConnection.createStatement().executeQuery(
        String.format(FIND_ITEM_BY_PATH, "'" + path + "'"));
  }

  @Override
  protected ResultSet findItemByUUID(String uuid) throws SQLException {
    return dbConnection.createStatement().executeQuery(
        String.format(FIND_ITEM_BY_ID, "'" + uuid + "'"));
  }

  @Override
  protected ResultSet findPropertyByPath(String parentId, String path) throws SQLException {
    return dbConnection.createStatement().executeQuery(
        String.format(FIND_CHILD_PROPERTY_BY_PATH, "'" + parentId + "'", "'" + path + "'"));
  }

  @Override
  protected ResultSet findReferences(String nodeUuid) throws SQLException {
    return dbConnection.createStatement().executeQuery(
        String.format(FIND_REFERENCES, "'" + nodeUuid + "'"));
  }

  @Override
  protected ResultSet findValueByPropertyIdOrderNumber(String cid, int orderNumb)
      throws SQLException {
    return dbConnection.createStatement().executeQuery(
        String.format(FIND_VALUE_BY_PROPERTYID_OREDERNUMB, "'" + cid + "'", "'" + orderNumb + "'"));
  }

  @Override
  protected ResultSet findValuesByPropertyId(String cid) throws SQLException {
    return dbConnection.createStatement().executeQuery(
        String.format(FIND_VALUES_BY_PROPERTYID, "'" + cid + "'"));
  }
  
  
  
}
