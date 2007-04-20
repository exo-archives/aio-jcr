/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCStorageConnection;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;

/**
 * Created by The eXo Platform SARL        . </br>
 * 
 * Concrete JDBC based data container that uses "table-set per Workspace policy"
 * i.e each JCR Workspace storage is placed in dedicated DB like:
 * table JCR_MITEM (ID varchar(255) not null, VERSION integer not null, PATH varchar(255) not null, primary key (ID))
 * table JCR_MNODE (ID varchar(255) not null, ORDER_NUM integer, PARENT_ID varchar(255), primary key (ID))
 * table JCR_MPROPERTY (ID varchar(255) not null, TYPE integer not null, PARENT_ID varchar(255) not null, MULTIVALUED bit not null, primary key (ID))
 * table JCR_MVALUE (ID bigint not null, DATA varbinary(65535) not null, ORDER_NUM integer, PROPERTY_ID varchar(255) not null, primary key (ID))
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: MultiDbJDBCConnection.java 13869 2007-03-28 13:50:50Z peterit $
 * 
 * CREATE INDEX JCR_IDX_MITEM_NAME ON JCR_MITEM(NAME, ID, VERSION); 
 */

public class MultiDbJDBCConnection extends JDBCStorageConnection {
  
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

  //protected PreparedStatement insertItem;
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
   
  public MultiDbJDBCConnection(Connection dbConnection,
      String containerName, ValueStoragePluginProvider valueStorageProvider,
      int maxBufferSize, File swapDirectory, FileCleaner swapCleaner) throws SQLException {
  
    super(dbConnection, containerName, valueStorageProvider, 
        maxBufferSize, swapDirectory, swapCleaner);
  }
  
  protected String getUuid(final String internalId) {
    return internalId;
  }
  
  protected String getInternalId(final String uuid) {
    return uuid;
  }


  /**
   * Prepared queries at start time
   * @throws SQLException
   */
  @Override
  protected void prepareQueries() throws SQLException {
    JCR_FK_NODE_PARENT = "JCR_FK_MNODE_PARENT";
    JCR_FK_NODE_ITEM = "JCR_FK_MNODE_ITEM";
    JCR_FK_PROPERTY_NODE = "JCR_FK_MPROPERTY_N";
    JCR_FK_PROPERTY_ITEM = "JCR_FK_MPROPERTY_I";
    JCR_FK_VALUE_PROPERTY = "JCR_FK_MVALUE_PROP";
    JCR_PK_ITEM = "JCR_MITEM_PKEY";
     
    FIND_ITEM_BY_ID = "select * from JCR_MITEM where ID=?";

    FIND_ITEM_BY_PATH = "select * from JCR_MITEM where PATH=? order by VERSION DESC";
    
    FIND_ITEM_BY_NAME = "select * from JCR_MITEM"
      + " where PARENT_ID=? and NAME=? and I_INDEX=? order by I_CLASS, VERSION DESC";
    
    // TODO unuse JCR_IDX_MITEM_PARENT_PATH
    FIND_CHILD_PROPERTY_BY_PATH = "select *" 
      + " from JCR_MPROPERTY"
      + " where PARENT_ID=? and PATH=? order by VERSION DESC";
    
    FIND_PROPERTY_BY_NAME = "select *" 
      + " from JCR_MPROPERTY"
      + " where PARENT_ID=? and NAME=? order by VERSION DESC";
   
    FIND_REFERENCES = "select P.ID, P.PARENT_ID, P.VERSION, P.P_TYPE, P.P_MULTIVALUED, P.NAME, P.PATH" +
        " from JCR_MREF R, JCR_MPROPERTY P" +
        " where R.NODE_ID=? and R.PROPERTY_ID=P.ID";
    
    FIND_VALUES_BY_PROPERTYID = "select * from JCR_MVALUE where PROPERTY_ID=? order by ORDER_NUM";
    FIND_VALUE_BY_PROPERTYID_OREDERNUMB = "select DATA from JCR_MVALUE where PROPERTY_ID=? and ORDER_NUM=?";
    
    // TODO Index PARENT_ID, N_ORDER_NUM
    FIND_NODES_BY_PARENTID = "select * from JCR_MNODE"
      + " where PARENT_ID=?"
      + " order by N_ORDER_NUM";
    
    // TODO Index PARENT_ID, ID    
    FIND_PROPERTIES_BY_PARENTID = "select * from JCR_MPROPERTY"
      + " where PARENT_ID=?" 
      + " order by ID";
    
    //INSERT_ITEM = "insert into JCR_MITEM(ID, NAME, VERSION, PATH) VALUES(?,?,?,?)";
  /*
  ID VARCHAR(96) NOT NULL PRIMARY KEY,
  PARENT_ID VARCHAR(96) NOT NULL,
  NAME VARCHAR(512) NOT NULL,
  VERSION INTEGER NOT NULL, 
  PATH VARCHAR(4096) NOT NULL,
  I_CLASS INTEGER NOT NULL,
  I_INDEX INTEGER NOT NULL,
  N_ORDER_NUM INTEGER NOT NULL,
  P_TYPE INTEGER NOT NULL, 
  P_MULTIVALUED BOOLEAN NOT NULL, */
    INSERT_NODE = "insert into JCR_MITEM(ID, PARENT_ID, NAME, PATH, VERSION, I_CLASS, I_INDEX, N_ORDER_NUM) VALUES(?,?,?,?,?," + I_CLASS_NODE + ",?,?)";

    //INSERT_PROPERTY = "insert into JCR_MPROPERTY(ID, PARENT_ID, TYPE, MULTIVALUED) VALUES(?,?,?,?)";    
    INSERT_PROPERTY = "insert into JCR_MITEM(ID, PARENT_ID, NAME, PATH, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?," + I_CLASS_PROPERTY + ",?,?,?)";
    
    INSERT_VALUE = "insert into JCR_MVALUE(DATA, ORDER_NUM, PROPERTY_ID) VALUES(?,?,?)";
    INSERT_REF = "insert into JCR_MREF(NODE_ID, PROPERTY_ID, ORDER_NUM) VALUES(?,?,?)";

    //UPDATE_ITEM = "update JCR_MITEM set VERSION=? where ID=?";
    //UPDATE_ITEM_PATH = "update JCR_MITEM set NAME=?, PATH=?, VERSION=? where ID=?";
    UPDATE_NODE = "update JCR_MITEM set VERSION=?, I_INDEX=?, N_ORDER_NUM=? where ID=?";
    UPDATE_PROPERTY = "update JCR_MITEM set VERSION=?, P_TYPE=? where ID=?";
    
    DELETE_ITEM = "delete from JCR_MITEM where ID=?";
    //DELETE_NODE = "delete from JCR_MNODE where ID=?";
    //DELETE_PROPERTY = "delete from JCR_MPROPERTY where ID=?";
    DELETE_VALUE = "delete from JCR_MVALUE where PROPERTY_ID=?";
    DELETE_REF = "delete from JCR_MREF where PROPERTY_ID=?";
  }
  
  @Override
  protected void addNodeRecord(NodeData data) throws SQLException {
//    if (insertItem == null)
//      insertItem = dbConnection.prepareStatement(INSERT_ITEM);
//    else
//      insertItem.clearParameters();
    if (insertNode == null)
      insertNode = dbConnection.prepareStatement(INSERT_NODE);
    else
      insertNode.clearParameters();
    
    // INSERT_ITEM = "insert into JCR_MITEM(ID, NAME, VERSION, PATH) VALUES(?,?,?,?)";
//    insertItem.setString(1, data.getUUID());
//    insertItem.setString(2, data.getQPath().getName().getAsString());
//    insertItem.setInt(3, data.getPersistedVersion());
//    insertItem.setString(4, data.getQPath().getAsString()); // TODO deprecated
//    insertItem.executeUpdate();

    // INSERT_NODE = "insert into JCR_MITEM(ID, PARENT_ID, NAME, PATH, VERSION, I_CLASS, I_INDEX, N_ORDER_NUM) VALUES(?,?,?,?,?," + I_CLASS_NODE + ",?,?)";
    insertNode.setString(1, data.getUUID());
    insertNode.setString(2, data.getParentUUID() == null ? "" : data.getParentUUID()); // if root then parent uuid equals empty string 
    insertNode.setString(3, data.getQPath().getName().getAsString());
    insertNode.setString(4, data.getQPath().getAsString());
    insertNode.setInt(5, data.getPersistedVersion());
    insertNode.setInt(6, data.getQPath().getIndex());
    insertNode.setInt(7, data.getOrderNumber());
    insertNode.executeUpdate();    
  }

  @Override
  protected void addPropertyRecord(PropertyData data) throws SQLException {
//    if (insertItem == null)
//      insertItem = dbConnection.prepareStatement(INSERT_ITEM);
//    else
//      insertItem.clearParameters();
    
    if (insertProperty == null)
      insertProperty = dbConnection.prepareStatement(INSERT_PROPERTY);
    else
      insertProperty.clearParameters();
        
    // INSERT_ITEM = "insert into JCR_MITEM(ID, NAME, VERSION, PATH) VALUES(?,?,?,?)";
//    insertItem.setString(1, data.getUUID());
//    insertItem.setString(2, data.getQPath().getName().getAsString());
//    insertItem.setInt(3, data.getPersistedVersion());
//    insertItem.setString(4, data.getQPath().getAsString()); // TODO deprecated
//    insertItem.executeUpdate();
    
    // INSERT_PROPERTY = "insert into JCR_MITEM(ID, PARENT_ID, NAME, PATH, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?," + I_CLASS_NODE + ",?,?,?)";
    insertProperty.setString(1, data.getUUID());
    insertProperty.setString(2, data.getParentUUID());
    insertProperty.setString(3, data.getQPath().getName().getAsString());
    insertProperty.setString(4, data.getQPath().getAsString());
    insertProperty.setInt(5, data.getPersistedVersion());
    insertProperty.setInt(6, data.getQPath().getIndex());
    insertProperty.setInt(7, data.getType());
    insertProperty.setBoolean(8, data.isMultiValued());
    
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
    
    if (data.getQPath().getAsString().indexOf("versionableUuid")>0)
      log.info("add ref versionableUuid " + data.getQPath().getAsString());
    
    List<ValueData> values = data.getValues();
    for (int i=0; i<values.size(); i++) {
      ValueData vdata = values.get(i);
//      String refNodeUuid = new String(BLOBUtil.readValue(vdata));
      String refNodeUuid = new String(vdata.getAsByteArray());

      insertReference.setString(1, refNodeUuid);
      insertReference.setString(2, data.getUUID());
      insertReference.setInt(3, i);
      insertReference.executeUpdate();
    }
  }  
  
  /**
   * For REFERENCE properties only
   */
  @Override
  protected void deleteReference(String propertyUuid) throws SQLException {
    if (deleteReference == null)
      deleteReference = dbConnection.prepareStatement(DELETE_REF);
    else
      deleteReference.clearParameters();
    
    deleteReference.setString(1, propertyUuid);
    int r = deleteReference.executeUpdate();
//    if (r > 0)
//      log.info("deleteReference " + propertyUuid + " " + r);
  }

  @Override
  protected int deleteItemByUUID(String uuid) throws SQLException {
    if (deleteItem == null)
      deleteItem = dbConnection.prepareStatement(DELETE_ITEM);
    else
      deleteItem.clearParameters();

//    ResultSet rs = dbConnection.createStatement().executeQuery("select * from jcr_mref where node_id='" + uuid+"'");
//    while (rs.next()) {
//      log.info(rs.getString("PROPERTY_ID"));
//    }
    
    deleteItem.setString(1, uuid);
    return deleteItem.executeUpdate();
  }

//  @Override
//  protected int deleteNodeByUUID(String uuid) throws SQLException {
//    if (deleteNode == null)
//      deleteNode = dbConnection.prepareStatement(DELETE_NODE);
//    else
//      deleteNode.clearParameters();
//    
//    deleteNode.setString(1, uuid);
//    return deleteNode.executeUpdate();
//  }
//
//  @Override
//  protected int deletePropertyByUUID(String uuid) throws SQLException {
//    if (deleteProperty == null)
//      deleteProperty = dbConnection.prepareStatement(DELETE_PROPERTY);
//    else
//      deleteProperty.clearParameters();
//    
//    deleteProperty.setString(1, uuid);
//    return deleteProperty.executeUpdate();
//  }

//  @Override
//  protected int updateItemPathByUUID(String qname, String qpath, int version, String uuid) throws SQLException {
//    if (updateItemPath == null)
//      updateItemPath = dbConnection.prepareStatement(UPDATE_ITEM_PATH);
//    else
//      updateItemPath.clearParameters();
//    
//    // UPDATE_ITEM_PATH = "update JCR_MITEM set NAME=?, PATH=?, VERSION=? where ID=?";
//    updateItemPath.setString(1, qname);
//    updateItemPath.setString(2, qpath);
//    updateItemPath.setInt(3, version);
//    updateItemPath.setString(4, uuid);
//    return updateItemPath.executeUpdate();
//  }  
  
//  @Override
//  protected int updateItemVersionByUUID(int versionValue, String uuid) throws SQLException {
//    if (updateItem == null)
//      updateItem = dbConnection.prepareStatement(UPDATE_ITEM);
//    else
//      updateItem.clearParameters();
//    
//    updateItem.setInt(1, versionValue);
//    updateItem.setString(2, uuid);
//    return updateItem.executeUpdate();
//  }
  
  @Override
  protected int updateNodeByUUID(int version, int index, int orderNumb, String uuid) throws SQLException {
    if (updateNode == null)
      updateNode = dbConnection.prepareStatement(UPDATE_NODE);
    else
      updateNode.clearParameters();
    
    updateNode.setInt(1, version);
    updateNode.setInt(2, index);
    updateNode.setInt(3, orderNumb);
    updateNode.setString(4, uuid);
    return updateNode.executeUpdate();
  }
  
  @Override
  protected int updatePropertyByUUID(int version, int type, String uuid) throws SQLException {
    if (updateProperty == null)
      updateProperty = dbConnection.prepareStatement(UPDATE_PROPERTY);
    else
      updateProperty.clearParameters();
    
    updateProperty.setInt(1, version);
    updateProperty.setInt(2, type);
    updateProperty.setString(3, uuid);
    return updateProperty.executeUpdate();
  }
  
  @Override
  protected ResultSet findItemByPath(String path) throws SQLException {
    if (findItemByPath == null)
      findItemByPath = dbConnection.prepareStatement(FIND_ITEM_BY_PATH);
    else
      findItemByPath.clearParameters();
    
    findItemByPath.setString(1, path);
    return findItemByPath.executeQuery();
  }
  
  protected ResultSet findItemByName(String parentId, String name, int index) throws SQLException {
    if (findItemByName == null)
      findItemByName = dbConnection.prepareStatement(FIND_ITEM_BY_NAME);
    else
      findItemByName.clearParameters();
    
    findItemByName.setString(1, parentId);
    findItemByName.setString(2, name);
    findItemByName.setInt(3, index);
    return findItemByName.executeQuery();
  }
  
  @Override
  protected ResultSet findPropertyByPath(String parentId, String path) throws SQLException {
    if (findChildPropertyByPath == null)
      findChildPropertyByPath = dbConnection.prepareStatement(FIND_CHILD_PROPERTY_BY_PATH);
    else
      findChildPropertyByPath.clearParameters();
    
    findChildPropertyByPath.setString(1, parentId);
    findChildPropertyByPath.setString(2, path);
    return findChildPropertyByPath.executeQuery();
  }
  
  @Override
  protected ResultSet findPropertyByName(String parentId, String name) throws SQLException {
    if (findPropertyByName == null)
      findPropertyByName = dbConnection.prepareStatement(FIND_PROPERTY_BY_NAME);
    else
      findPropertyByName.clearParameters();
    
    findPropertyByName.setString(1, parentId);
    findPropertyByName.setString(2, name);
    return findPropertyByName.executeQuery();
  }

  @Override
  protected ResultSet findItemByUUID(String uuid) throws SQLException {
    if (findItemById == null)
      findItemById = dbConnection.prepareStatement(FIND_ITEM_BY_ID);
    else
      findItemById.clearParameters();
    
    findItemById.setString(1, uuid);
    return findItemById.executeQuery();
  }

  @Override
  protected ResultSet findReferences(String nodeUuid) throws SQLException {
    if (findReferences == null)
      findReferences = dbConnection.prepareStatement(FIND_REFERENCES);
    else
      findReferences.clearParameters();
      
    findReferences.setString(1, nodeUuid);
    return findReferences.executeQuery();
  }


  @Override
  protected ResultSet findChildNodesByParentUUID(String parentUUID) throws SQLException {
    if (findNodesByParentId == null)
      findNodesByParentId = dbConnection.prepareStatement(FIND_NODES_BY_PARENTID);
    else
      findNodesByParentId.clearParameters();
    
    findNodesByParentId.setString(1, parentUUID);
    return findNodesByParentId.executeQuery();
  }

  @Override
  protected ResultSet findChildPropertiesByParentUUID(String parentUUID) throws SQLException {
    if (findPropertiesByParentId == null)
      findPropertiesByParentId = dbConnection.prepareStatement(FIND_PROPERTIES_BY_PARENTID);
    else
      findPropertiesByParentId.clearParameters();
    
    findPropertiesByParentId.setString(1, parentUUID);
    return findPropertiesByParentId.executeQuery();
  }
  
  // -------- values processing ------------
  
  protected void addValues(String cid, List<ValueData> data) throws IOException, SQLException {
    if(data == null) {
      log.warn("List of values data is NULL. Check JCR logic. PropertyId: " + getUuid(cid));
      return;
    }

    for (int i = 0; i < data.size(); i++) {
      addValueRecord(cid, data.get(i), i); // TODO data.get(i).getOrderNumber()
    }
  }  
  
  protected void addValueRecord(String cid, ValueData data, int orderNumber) throws SQLException, IOException {

    InputStream stream = null;
    int streamLength = 0;
    if (data.isByteArray()) {
      byte[] dataBytes = data.getAsByteArray();
      stream = new ByteArrayInputStream(dataBytes);
      streamLength = dataBytes.length;
    } else {
      stream = data.getAsStream();
      streamLength = stream.available(); // for FileInputStream can be used channel.size() result
    }

    if (insertValue == null)
      insertValue = dbConnection.prepareStatement(INSERT_VALUE);
    else
      insertValue.clearParameters();
    
    insertValue.setBinaryStream(1, stream, streamLength);

    data.setOrderNumber(orderNumber);
    insertValue.setInt(2, data.getOrderNumber());

    insertValue.setString(3, cid);
    try {
      insertValue.executeUpdate();
    } catch (SQLException e) {
      log.error("addValueRecord() ",  e);
      throw e;
    }
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
