/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCStorageConnection;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

/**
 * Created by The eXo Platform SARL
 * 27.04.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SingleDbJDBCConnection.java 13869 2007-03-28 13:50:50Z peterit $
 */
public class SingleDbJDBCConnection extends JDBCStorageConnection {

  protected PreparedStatement findItemById;
  protected PreparedStatement findItemByPath;
  protected PreparedStatement findItemByName;

  protected PreparedStatement findChildPropertyByPath;
  protected PreparedStatement findPropertyByName;

  protected PreparedStatement findDescendantNodes;
  protected PreparedStatement findDescendantProperties;

  protected PreparedStatement findReferences;

  protected PreparedStatement findValuesByPropertyId;
  protected PreparedStatement findValueByPropertyIdOrderNumber;

  protected PreparedStatement findNodesByParentId;
  protected PreparedStatement findPropertiesByParentId;

  protected PreparedStatement insertItem;
  protected PreparedStatement insertNode;
  protected PreparedStatement insertProperty;
  protected PreparedStatement insertReference;
  protected PreparedStatement insertValue;

  protected PreparedStatement updateItem;
  protected PreparedStatement updateItemPath;
  protected PreparedStatement updateNode;
  protected PreparedStatement updateProperty;

  protected PreparedStatement deleteItem;
  protected PreparedStatement deleteNode;
  protected PreparedStatement deleteProperty;
  protected PreparedStatement deleteReference;
  protected PreparedStatement deleteValue;
  
  public SingleDbJDBCConnection(Connection dbConnection,
      String containerName, ValueStoragePluginProvider valueStorageProvider,
      int maxBufferSize, File swapDirectory, FileCleaner swapCleaner) throws SQLException {
  
    super(dbConnection, containerName, valueStorageProvider, 
        maxBufferSize, swapDirectory, swapCleaner);
  }
  
  protected String getInternalId(final String uuid) {
    return containerName + uuid;
  }

  protected String getUuid(final String internalId) {
    
    if(internalId == null) // possible for root parent
      return null;

    return internalId.substring(containerName.length());
  }   

  
  /**
   * Prepared queries at start time
   * @throws SQLException
   */
  @Override
  protected final void prepareQueries() throws SQLException {
    /**
      CREATE INDEX JCR_IDX_SITEM_PARENT_ORD ON JCR_SITEM(I_CLASS, CONTAINER_NAME, PARENT_ID, N_ORDER_NUM, VERSION DESC)
      /

      CREATE VIEW JCR_SNODE AS 
      SELECT ID, PARENT_ID, NAME, VERSION, PATH, CONTAINER_NAME, I_INDEX, N_ORDER_NUM 
      FROM JCR_SITEM WHERE I_CLASS=1
      /
    
      CREATE VIEW JCR_SPROPERTY AS 
      SELECT ID, PARENT_ID, NAME, VERSION, PATH, CONTAINER_NAME, P_TYPE, P_MULTIVALUED FROM JCR_SITEM WHERE I_CLASS=2
      /
     * 
     */
    
    JCR_FK_NODE_PARENT = "JCR_FK_SNODE_PARENT";
    JCR_FK_NODE_ITEM = "JCR_FK_SNODE_ITEM";
    JCR_FK_PROPERTY_NODE = "JCR_FK_SPROPERTY_N";
    JCR_FK_PROPERTY_ITEM = "JCR_FK_SPROPERTY_I";
    JCR_FK_VALUE_PROPERTY = "JCR_FK_SVALUE_PROP";
    JCR_PK_ITEM = "JCR_SITEM_PKEY";
    
    FIND_ITEM_BY_ID = "select * from JCR_SITEM where ID=?";

    FIND_ITEM_BY_PATH = "select * from JCR_SITEM where CONTAINER_NAME=? and PATH=? order by VERSION DESC";
    
    FIND_ITEM_BY_NAME = "select * from JCR_SITEM"
      + " where CONTAINER_NAME=? and PARENT_ID=? and NAME=? and I_INDEX=? order by I_CLASS, VERSION DESC";
    
    // TODO unuse JCR_IDX_MITEM_PARENT_PATH
//    FIND_CHILD_PROPERTY_BY_PATH = "select *" 
//      + " from JCR_SPROPERTY"
//      + " where CONTAINER_NAME=? and PARENT_ID=? and PATH=? order by VERSION DESC";
    
    FIND_CHILD_PROPERTY_BY_PATH = "select *" 
      + " from JCR_SITEM"
      + " where I_CLASS=2 and CONTAINER_NAME=? and PARENT_ID=? and PATH=? order by VERSION DESC";
    
    FIND_PROPERTY_BY_NAME = "select *" 
      + " from JCR_SPROPERTY"
      + " where CONTAINER_NAME=? and PARENT_ID=? and NAME=? order by VERSION DESC";
    
//    FIND_PROPERTY_BY_NAME = "select *" 
//      + " from JCR_SITEM"
//      + " where I_CLASS=2 and CONTAINER_NAME=? and PARENT_ID=? and NAME=? order by VERSION DESC";
    
    FIND_REFERENCES = "select P.ID, P.PARENT_ID, P.VERSION, P.P_TYPE, P.P_MULTIVALUED, P.NAME, P.PATH" +
      " from JCR_SREF R, JCR_SPROPERTY P" +
      " where P.CONTAINER_NAME=? and R.NODE_ID=? and P.ID=R.PROPERTY_ID";
    
//    FIND_REFERENCES = "select P.ID, P.PARENT_ID, P.VERSION, P.P_TYPE, P.P_MULTIVALUED, P.NAME, P.PATH" +
//      " from JCR_SREF R, JCR_SITEM P" +
//      " where P.I_CLASS=2 and P.CONTAINER_NAME=? and R.NODE_ID=? and P.ID=R.PROPERTY_ID";

    FIND_VALUES_BY_PROPERTYID = "select * from JCR_SVALUE where PROPERTY_ID=? order by ORDER_NUM";
    FIND_VALUE_BY_PROPERTYID_OREDERNUMB = "select DATA from JCR_SVALUE where PROPERTY_ID=? and ORDER_NUM=?";
    
    // TODO Index CONTAINER_NAME, PARENT_ID, N_ORDER_NUM
    FIND_NODES_BY_PARENTID = "select * from JCR_SNODE"
      + " where CONTAINER_NAME=? and PARENT_ID=?"
      + " order by N_ORDER_NUM";
    
//    FIND_NODES_BY_PARENTID = "select * from JCR_SITEM"
//      + " where I_CLASS=1 and CONTAINER_NAME=? and PARENT_ID=?"
//      + " order by N_ORDER_NUM";
    
    // TODO Index CONTAINER_NAME, PARENT_ID, ID    
    FIND_PROPERTIES_BY_PARENTID = "select * from JCR_SPROPERTY"
      + " where CONTAINER_NAME=? and PARENT_ID=?" 
      + " order by ID";
    
//    FIND_PROPERTIES_BY_PARENTID = "select * from JCR_SITEM"
//      + " where I_CLASS=2 and CONTAINER_NAME=? and PARENT_ID=?" 
//      + " order by ID";
    
    /*
    ID VARCHAR(96) NOT NULL PRIMARY KEY,
    PARENT_ID VARCHAR(96) NOT NULL,
    NAME VARCHAR(512) NOT NULL,
    VERSION INTEGER NOT NULL, 
    PATH VARCHAR(4096) NOT NULL,
    CONTAINER_NAME VARCHAR2(96) NOT NULL,
    I_CLASS INTEGER NOT NULL,
    I_INDEX INTEGER NOT NULL,
    N_ORDER_NUM INTEGER NOT NULL,
    P_TYPE INTEGER NOT NULL, 
    P_MULTIVALUED BOOLEAN NOT NULL, */
    INSERT_NODE = "insert into JCR_SITEM(ID, PARENT_ID, NAME, PATH, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, N_ORDER_NUM) VALUES(?,?,?,?,?,?," + I_CLASS_NODE + ",?,?)";
    INSERT_PROPERTY = "insert into JCR_SITEM(ID, PARENT_ID, NAME, PATH, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?,?," + I_CLASS_PROPERTY + ",?,?,?)";
    
    INSERT_VALUE = "insert into JCR_SVALUE(DATA, ORDER_NUM, PROPERTY_ID) VALUES(?,?,?)";
    INSERT_REF = "insert into JCR_SREF(NODE_ID, PROPERTY_ID, ORDER_NUM) VALUES(?,?,?)";

    UPDATE_NODE = "update JCR_SITEM set VERSION=?, I_INDEX=?, N_ORDER_NUM=? where ID=?";
    UPDATE_PROPERTY = "update JCR_SITEM set VERSION=?, P_TYPE=? where ID=?";
    
    DELETE_ITEM = "delete from JCR_SITEM where ID=?";
    DELETE_VALUE = "delete from JCR_SVALUE where PROPERTY_ID=?";
    DELETE_REF = "delete from JCR_SREF where PROPERTY_ID=?";
  }

  @Override
  protected void addNodeRecord(NodeData data) throws SQLException {
    if (insertNode == null)
      insertNode = dbConnection.prepareStatement(INSERT_NODE);
    else
      insertNode.clearParameters();
    
    insertNode.setString(1, getInternalId(data.getUUID()));
    // if root then parent uuid equals space string
    insertNode.setString(2, data.getParentUUID() == null ? Constants.ROOT_PARENT_UUID : getInternalId(data.getParentUUID()));  
    insertNode.setString(3, data.getQPath().getName().getAsString());
    insertNode.setString(4, data.getQPath().getAsString());
    insertNode.setString(5, containerName);
    insertNode.setInt(6, data.getPersistedVersion());
    insertNode.setInt(7, data.getQPath().getIndex());
    insertNode.setInt(8, data.getOrderNumber());
    insertNode.executeUpdate();    
  }

  @Override
  protected void addPropertyRecord(PropertyData data) throws SQLException {
    if (insertProperty == null)
      insertProperty = dbConnection.prepareStatement(INSERT_PROPERTY);
    else
      insertProperty.clearParameters();
    
    insertProperty.setString(1, getInternalId(data.getUUID()));
    insertProperty.setString(2, getInternalId(data.getParentUUID()));
    insertProperty.setString(3, data.getQPath().getName().getAsString());
    insertProperty.setString(4, data.getQPath().getAsString());
    insertProperty.setString(5, containerName);
    insertProperty.setInt(6, data.getPersistedVersion());
    insertProperty.setInt(7, data.getQPath().getIndex());
    insertProperty.setInt(8, data.getType());
    insertProperty.setBoolean(9, data.isMultiValued());
    
    insertProperty.executeUpdate();
  }

  /**
   * For REFERENCE properties only
   */
  @Override
  protected void addReference(PropertyData data) throws SQLException, IOException {
    if (insertReference == null)
      insertReference = dbConnection.prepareStatement(INSERT_REF);
    else
      insertReference.clearParameters();
    
    List<ValueData> values = data.getValues();
    for (int i=0; i<values.size(); i++) {
      ValueData vdata = values.get(i);
      String refNodeUuid = new String(vdata.getAsByteArray());

      insertReference.setString(1, getInternalId(refNodeUuid));
      insertReference.setString(2, getInternalId(data.getUUID()));
      insertReference.setInt(3, i);
      insertReference.executeUpdate();
    }
  }  
  
  /**
   * For REFERENCE properties only
   */
  @Override
  protected void deleteReference(String propertyCid) throws SQLException {
    if (deleteReference == null)
      deleteReference = dbConnection.prepareStatement(DELETE_REF);
    else
      deleteReference.clearParameters();
    
    deleteReference.setString(1, propertyCid);
    deleteReference.executeUpdate();    
  }

  @Override
  protected int deleteItemByUUID(String cid) throws SQLException {
    if (deleteItem == null)
      deleteItem = dbConnection.prepareStatement(DELETE_ITEM);
    else
      deleteItem.clearParameters();
    
    deleteItem.setString(1, cid);
    return deleteItem.executeUpdate();
  }

//  @Override
//  protected int deleteNodeByUUID(String cid) throws SQLException {
//    if (deleteNode == null)
//      deleteNode = dbConnection.prepareStatement(DELETE_NODE);
//    else
//      deleteNode.clearParameters();
//    
//    deleteNode.setString(1, cid);
//    return deleteNode.executeUpdate();
//  }

//  @Override
//  protected int deletePropertyByUUID(String cid) throws SQLException {
//    if (deleteProperty == null)
//      deleteProperty = dbConnection.prepareStatement(DELETE_PROPERTY);
//    else
//      deleteProperty.clearParameters();
//    
//    deleteProperty.setString(1, cid);
//    return deleteProperty.executeUpdate();
//  }

  @Override
  protected ResultSet findChildNodesByParentUUID(String parentCid) throws SQLException {
    if (findNodesByParentId == null)
      findNodesByParentId = dbConnection.prepareStatement(FIND_NODES_BY_PARENTID);
    else
      findNodesByParentId.clearParameters();
    
    findNodesByParentId.setString(1, containerName);
    findNodesByParentId.setString(2, parentCid);
    return findNodesByParentId.executeQuery();
  }
  
  @Override
  protected ResultSet findChildPropertiesByParentUUID(String parentCid) throws SQLException {
    if (findPropertiesByParentId == null)
      findPropertiesByParentId = dbConnection.prepareStatement(FIND_PROPERTIES_BY_PARENTID);
    else
      findPropertiesByParentId.clearParameters();
    
    findPropertiesByParentId.setString(1, containerName);
    findPropertiesByParentId.setString(2, parentCid);
    return findPropertiesByParentId.executeQuery();
  }

  @Override
  protected ResultSet findItemByPath(String path) throws SQLException {
    if (findItemByPath == null)
      findItemByPath = dbConnection.prepareStatement(FIND_ITEM_BY_PATH);
    else
      findItemByPath.clearParameters();
      
    findItemByPath.setString(1, containerName);
    findItemByPath.setString(2, path);
    return findItemByPath.executeQuery();
  }

  @Override
  protected ResultSet findItemByName(String parentId, String name, int index) throws SQLException {
    if (findItemByName == null)
      findItemByName = dbConnection.prepareStatement(FIND_ITEM_BY_NAME);
    else
      findItemByName.clearParameters();
    
    findItemByName.setString(1, containerName);
    findItemByName.setString(2, parentId);
    findItemByName.setString(3, name);
    findItemByName.setInt(4, index);
    return findItemByName.executeQuery();
  }

  @Override
  protected ResultSet findPropertyByPath(String parentCid, String path) throws SQLException {
    if (findChildPropertyByPath == null)
      findChildPropertyByPath = dbConnection.prepareStatement(FIND_CHILD_PROPERTY_BY_PATH);
    else
      findChildPropertyByPath.clearParameters();

    findChildPropertyByPath.setString(1, containerName);
    findChildPropertyByPath.setString(2, parentCid);
    findChildPropertyByPath.setString(3, path);
    return findChildPropertyByPath.executeQuery();
  }  
  
  @Override
  protected ResultSet findPropertyByName(String parentCid, String name) throws SQLException {
    if (findPropertyByName == null)
      findPropertyByName = dbConnection.prepareStatement(FIND_PROPERTY_BY_NAME);
    else
      findPropertyByName.clearParameters();
    
    findPropertyByName.setString(1, containerName);
    findPropertyByName.setString(2, parentCid);
    findPropertyByName.setString(3, name);
    return findPropertyByName.executeQuery();
  }

  @Override
  protected ResultSet findItemByUUID(String cid) throws SQLException {
    if (findItemById == null)
      findItemById = dbConnection.prepareStatement(FIND_ITEM_BY_ID);
    else
      findItemById.clearParameters();
          
    findItemById.setString(1, cid);
    return findItemById.executeQuery();
  }
  
  @Override
  protected ResultSet findReferences(String cid) throws SQLException {
    if (findReferences == null)
      findReferences = dbConnection.prepareStatement(FIND_REFERENCES);
    else
      findReferences.clearParameters();
    
    findReferences.setString(1, containerName);
    findReferences.setString(2, cid);
    return findReferences.executeQuery();
  }

//  protected int updateItemVersionByUUID(int versionValue, String cid) throws SQLException {
//    if (updateItem == null)
//      updateItem = dbConnection.prepareStatement(UPDATE_ITEM);
//    else
//      updateItem.clearParameters();
//          
//    updateItem.setInt(1, versionValue);
//    updateItem.setString(2, cid);
//    return updateItem.executeUpdate();
//  }
  
//  @Override
//  protected int updateItemPathByUUID(String qpath, int version, String cid) throws SQLException {
//    if (updateItemPath == null)
//      updateItemPath = dbConnection.prepareStatement(UPDATE_ITEM_PATH);
//    else
//      updateItemPath.clearParameters();
//          
//    updateItemPath.setString(1, qpath);
//    updateItemPath.setInt(2, version);
//    updateItemPath.setString(3, cid);
//    return updateItemPath.executeUpdate();
//  }    
  
  @Override
  protected int updateNodeByUUID(int version, int index, int orderNumb, String cid) throws SQLException {
    if (updateNode == null)
      updateNode = dbConnection.prepareStatement(UPDATE_NODE);
    else
      updateNode.clearParameters();
    
    updateNode.setInt(1, version);
    updateNode.setInt(2, index);
    updateNode.setInt(3, orderNumb);
    updateNode.setString(4, cid);
    return updateNode.executeUpdate();
  }
  
  @Override
  protected int updatePropertyByUUID(int version, int type, String cid) throws SQLException {
    if (updateProperty == null)
      updateProperty = dbConnection.prepareStatement(UPDATE_PROPERTY);
    else
      updateProperty.clearParameters();
    
    updateProperty.setInt(1, version);
    updateProperty.setInt(2, type);
    updateProperty.setString(3, cid);
    return updateProperty.executeUpdate();
  }
  
  // -------- values processing ------------
  
  protected void addValueData(String cid, int orderNumber, InputStream stream, int streamLength) throws SQLException, IOException {

    if (insertValue == null)
      insertValue = dbConnection.prepareStatement(INSERT_VALUE);
    else
      insertValue.clearParameters();      
    
    if (stream == null)
      insertValue.setNull(1, Types.BLOB); // null, i.e. reference
    else
      insertValue.setBinaryStream(1, stream, streamLength);

    insertValue.setInt(2, orderNumber);

    insertValue.setString(3, cid);
    insertValue.executeUpdate();
  }
  
  protected void deleteValues(String cid) throws SQLException {
    if (deleteValue == null)
      deleteValue = dbConnection.prepareStatement(DELETE_VALUE);
    else
      deleteValue.clearParameters();      
    
    deleteValue.setString(1, cid);
    deleteValue.executeUpdate();
  }

  protected ResultSet findValuesByPropertyId(String cid) throws SQLException {
    if (findValuesByPropertyId == null)
      findValuesByPropertyId = dbConnection.prepareStatement(FIND_VALUES_BY_PROPERTYID);
    else
      findValuesByPropertyId.clearParameters();
          
    findValuesByPropertyId.setString(1, cid);
    return findValuesByPropertyId.executeQuery();
  }

  protected ResultSet findValueByPropertyIdOrderNumber(String cid, int orderNumb) throws SQLException {
    if (findValueByPropertyIdOrderNumber == null)
      findValueByPropertyIdOrderNumber = dbConnection.prepareStatement(FIND_VALUE_BY_PROPERTYID_OREDERNUMB);
    else
      findValueByPropertyIdOrderNumber.clearParameters();
          
    findValueByPropertyIdOrderNumber.setString(1, cid);
    findValueByPropertyIdOrderNumber.setInt(2, orderNumb);
    return findValueByPropertyIdOrderNumber.executeQuery();
  }  
}
