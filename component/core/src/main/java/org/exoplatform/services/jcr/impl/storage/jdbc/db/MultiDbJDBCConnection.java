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
package org.exoplatform.services.jcr.impl.storage.jdbc.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
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
 * Created by The eXo Platform SAS. </br> Concrete JDBC based data container
 * that uses "table-set per Workspace policy" i.e each JCR Workspace storage is
 * placed in dedicated DB.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id$
 */

public class MultiDbJDBCConnection extends JDBCStorageConnection {

  /**
   * Multidatabase JDBC Connection constructor.
   * 
   * @param dbConnection
   *          JDBC connection, shoudl be opened before
   * @param readOnly
   *          , boolean if true the dbConnection was marked as READ-ONLY.
   * @param containerName
   *          Workspace Storage Container name (see configuration)
   * @param valueStorageProvider
   *          External Value Storages provider
   * @param maxBufferSize
   *          Maximum buffer size (see configuration)
   * @param swapDirectory
   *          Swap directory (see configuration)
   * @param swapCleaner
   *          Swap cleaner (internal FileCleaner).
   * @throws SQLException
   * @see org.exoplatform.services.jcr.impl.util.io.FileCleaner
   */
  public MultiDbJDBCConnection(Connection dbConnection,
                               boolean readOnly,
                               String containerName,
                               ValueStoragePluginProvider valueStorageProvider,
                               int maxBufferSize,
                               File swapDirectory,
                               FileCleaner swapCleaner) throws SQLException {

    super(dbConnection,
          readOnly,
          containerName,
          valueStorageProvider,
          maxBufferSize,
          swapDirectory,
          swapCleaner);
  }

  /**
   * {@inheritDoc}
   */
  protected String getIdentifier(final String internalId) {
    return internalId;
  }

  /**
   * {@inheritDoc}
   */
  protected String getInternalId(final String identifier) {
    return identifier;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void prepareQueries() throws SQLException {
    JCR_PK_ITEM = "JCR_PK_MITEM";
    JCR_FK_ITEM_PARENT = "JCR_FK_MITEM_PARENT";
    JCR_IDX_ITEM_PARENT = "JCR_IDX_MITEM_PARENT";
    JCR_IDX_ITEM_PARENT_NAME = "JCR_IDX_MITEM_PARENT_NAME";
    JCR_IDX_ITEM_PARENT_ID = "JCR_IDX_MITEM_PARENT_ID";
    JCR_PK_VALUE = "JCR_PK_MVALUE";
    JCR_FK_VALUE_PROPERTY = "JCR_FK_MVALUE_PROPERTY";
    JCR_IDX_VALUE_PROPERTY = "JCR_IDX_MVALUE_PROPERTY";
    JCR_PK_REF = "JCR_PK_MREF";
    JCR_IDX_REF_PROPERTY = "JCR_IDX_MREF_PROPERTY";

    FIND_ITEM_BY_ID = "select * from JCR_MITEM where ID=?";

    FIND_ITEM_BY_NAME = "select * from JCR_MITEM"
        + " where PARENT_ID=? and NAME=? and I_INDEX=? order by I_CLASS, VERSION DESC";

    FIND_PROPERTY_BY_NAME = "select V.DATA"
        + " from JCR_MITEM I, JCR_MVALUE V"
        + " where I.I_CLASS=2 and I.PARENT_ID=? and I.NAME=? and I.ID=V.PROPERTY_ID order by V.ORDER_NUM";

    FIND_REFERENCES = "select P.ID, P.PARENT_ID, P.VERSION, P.P_TYPE, P.P_MULTIVALUED, P.NAME"
        + " from JCR_MREF R, JCR_MITEM P"
        + " where R.NODE_ID=? and P.ID=R.PROPERTY_ID and P.I_CLASS=2";

    FIND_VALUES_BY_PROPERTYID = "select PROPERTY_ID, ORDER_NUM, DATA, STORAGE_DESC from JCR_MVALUE where PROPERTY_ID=? order by ORDER_NUM";

    FIND_VALUES_VSTORAGE_DESC_BY_PROPERTYID = "select distinct STORAGE_DESC from JCR_MVALUE where PROPERTY_ID=?";

    FIND_VALUE_BY_PROPERTYID_OREDERNUMB = "select DATA from JCR_MVALUE where PROPERTY_ID=? and ORDER_NUM=?";

    FIND_NODES_BY_PARENTID = "select * from JCR_MITEM" + " where I_CLASS=1 and PARENT_ID=?"
        + " order by N_ORDER_NUM";

    FIND_NODES_COUNT_BY_PARENTID = "select count(ID) from JCR_MITEM"
        + " where I_CLASS=1 and PARENT_ID=?";

    FIND_PROPERTIES_BY_PARENTID = "select * from JCR_MITEM" + " where I_CLASS=2 and PARENT_ID=?"
        + " order by ID";

    INSERT_NODE = "insert into JCR_MITEM(ID, PARENT_ID, NAME, VERSION, I_CLASS, I_INDEX, N_ORDER_NUM) VALUES(?,?,?,?,"
        + I_CLASS_NODE + ",?,?)";
    INSERT_PROPERTY = "insert into JCR_MITEM(ID, PARENT_ID, NAME, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,"
        + I_CLASS_PROPERTY + ",?,?,?)";

    INSERT_VALUE = "insert into JCR_MVALUE(DATA, ORDER_NUM, PROPERTY_ID, STORAGE_DESC) VALUES(?,?,?,?)";
    INSERT_REF = "insert into JCR_MREF(NODE_ID, PROPERTY_ID, ORDER_NUM) VALUES(?,?,?)";

    RENAME_NODE = "update JCR_MITEM set PARENT_ID=?, NAME =?, VERSION=?, I_INDEX =?, N_ORDER_NUM =? where ID=?";

    UPDATE_NODE = "update JCR_MITEM set VERSION=?, I_INDEX=?, N_ORDER_NUM=? where ID=?";
    UPDATE_PROPERTY = "update JCR_MITEM set VERSION=?, P_TYPE=? where ID=?";

    DELETE_ITEM = "delete from JCR_MITEM where ID=?";
    DELETE_VALUE = "delete from JCR_MVALUE where PROPERTY_ID=?";
    DELETE_REF = "delete from JCR_MREF where PROPERTY_ID=?";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int addNodeRecord(NodeData data) throws SQLException {
    if (insertNode == null)
      insertNode = dbConnection.prepareStatement(INSERT_NODE);
    else
      insertNode.clearParameters();

    insertNode.setString(1, data.getIdentifier());
    insertNode.setString(2, data.getParentIdentifier() == null ? Constants.ROOT_PARENT_UUID
                                                              : data.getParentIdentifier());
    insertNode.setString(3, data.getQPath().getName().getAsString());
    insertNode.setInt(4, data.getPersistedVersion());
    insertNode.setInt(5, data.getQPath().getIndex());
    insertNode.setInt(6, data.getOrderNumber());
    return insertNode.executeUpdate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int addPropertyRecord(PropertyData data) throws SQLException {
    if (insertProperty == null)
      insertProperty = dbConnection.prepareStatement(INSERT_PROPERTY);
    else
      insertProperty.clearParameters();

    insertProperty.setString(1, data.getIdentifier());
    insertProperty.setString(2, data.getParentIdentifier());
    insertProperty.setString(3, data.getQPath().getName().getAsString());
    insertProperty.setInt(4, data.getPersistedVersion());
    insertProperty.setInt(5, data.getQPath().getIndex());
    insertProperty.setInt(6, data.getType());
    insertProperty.setBoolean(7, data.isMultiValued());

    return insertProperty.executeUpdate();
  }

  /**
   * For REFERENCE properties only
   */
  /**
   * {@inheritDoc}
   */
  @Override
  protected int addReference(PropertyData data) throws SQLException, IOException {
    if (insertReference == null)
      insertReference = dbConnection.prepareStatement(INSERT_REF);
    else
      insertReference.clearParameters();

    if (data.getQPath().getAsString().indexOf("versionableUuid") > 0)
      LOG.info("add ref versionableUuid " + data.getQPath().getAsString());

    List<ValueData> values = data.getValues();
    int added = 0;
    for (int i = 0; i < values.size(); i++) {
      ValueData vdata = values.get(i);
      String refNodeIdentifier = new String(vdata.getAsByteArray());

      insertReference.setString(1, refNodeIdentifier);
      insertReference.setString(2, data.getIdentifier());
      insertReference.setInt(3, i);
      added += insertReference.executeUpdate();
    }

    return added;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int deleteReference(String propertyIdentifier) throws SQLException {
    if (deleteReference == null)
      deleteReference = dbConnection.prepareStatement(DELETE_REF);
    else
      deleteReference.clearParameters();

    deleteReference.setString(1, propertyIdentifier);
    return deleteReference.executeUpdate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int deleteItemByIdentifier(String identifier) throws SQLException {
    if (deleteItem == null)
      deleteItem = dbConnection.prepareStatement(DELETE_ITEM);
    else
      deleteItem.clearParameters();

    deleteItem.setString(1, identifier);
    return deleteItem.executeUpdate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int updateNodeByIdentifier(int version, int index, int orderNumb, String identifier) throws SQLException {
    if (updateNode == null)
      updateNode = dbConnection.prepareStatement(UPDATE_NODE);
    else
      updateNode.clearParameters();

    updateNode.setInt(1, version);
    updateNode.setInt(2, index);
    updateNode.setInt(3, orderNumb);
    updateNode.setString(4, identifier);
    return updateNode.executeUpdate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int updatePropertyByIdentifier(int version, int type, String identifier) throws SQLException {
    if (updateProperty == null)
      updateProperty = dbConnection.prepareStatement(UPDATE_PROPERTY);
    else
      updateProperty.clearParameters();

    updateProperty.setInt(1, version);
    updateProperty.setInt(2, type);
    updateProperty.setString(3, identifier);
    return updateProperty.executeUpdate();
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResultSet findItemByIdentifier(String identifier) throws SQLException {
    if (findItemById == null)
      findItemById = dbConnection.prepareStatement(FIND_ITEM_BY_ID);
    else
      findItemById.clearParameters();

    findItemById.setString(1, identifier);
    return findItemById.executeQuery();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResultSet findReferences(String nodeIdentifier) throws SQLException {
    if (findReferences == null)
      findReferences = dbConnection.prepareStatement(FIND_REFERENCES);
    else
      findReferences.clearParameters();

    findReferences.setString(1, nodeIdentifier);
    return findReferences.executeQuery();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResultSet findChildNodesByParentIdentifier(String parentIdentifier) throws SQLException {
    if (findNodesByParentId == null)
      findNodesByParentId = dbConnection.prepareStatement(FIND_NODES_BY_PARENTID);
    else
      findNodesByParentId.clearParameters();

    findNodesByParentId.setString(1, parentIdentifier);
    return findNodesByParentId.executeQuery();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResultSet findChildNodesCountByParentIdentifier(String parentIdentifier) throws SQLException {
    if (findNodesCountByParentId == null)
      findNodesCountByParentId = dbConnection.prepareStatement(FIND_NODES_COUNT_BY_PARENTID);
    else
      findNodesCountByParentId.clearParameters();

    findNodesCountByParentId.setString(1, parentIdentifier);
    return findNodesCountByParentId.executeQuery();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResultSet findChildPropertiesByParentIdentifier(String parentIdentifier) throws SQLException {
    if (findPropertiesByParentId == null)
      findPropertiesByParentId = dbConnection.prepareStatement(FIND_PROPERTIES_BY_PARENTID);
    else
      findPropertiesByParentId.clearParameters();

    findPropertiesByParentId.setString(1, parentIdentifier);
    return findPropertiesByParentId.executeQuery();
  }

  // -------- values processing ------------

  /**
   * {@inheritDoc}
   */
  protected int addValueData(String cid,
                             int orderNumber,
                             InputStream stream,
                             int streamLength,
                             String storageDesc) throws SQLException, IOException {

    if (insertValue == null)
      insertValue = dbConnection.prepareStatement(INSERT_VALUE);
    else
      insertValue.clearParameters();

    if (stream == null) {
      // [PN] store vd reference to external storage etc.
      insertValue.setNull(1, Types.BINARY);
      insertValue.setString(4, storageDesc);
    } else {
      insertValue.setBinaryStream(1, stream, streamLength);
      insertValue.setNull(4, Types.VARCHAR);
    }

    insertValue.setInt(2, orderNumber);
    insertValue.setString(3, cid);
    return insertValue.executeUpdate();
  }

  /**
   * {@inheritDoc}
   */
  protected int deleteValues(String cid) throws SQLException {
    if (deleteValue == null)
      deleteValue = dbConnection.prepareStatement(DELETE_VALUE);
    else
      deleteValue.clearParameters();

    deleteValue.setString(1, cid);
    return deleteValue.executeUpdate();
  }

  /**
   * {@inheritDoc}
   */
  protected ResultSet findValuesByPropertyId(String cid) throws SQLException {
    if (findValuesByPropertyId == null)
      findValuesByPropertyId = dbConnection.prepareStatement(FIND_VALUES_BY_PROPERTYID);
    else
      findValuesByPropertyId.clearParameters();

    findValuesByPropertyId.setString(1, cid);
    return findValuesByPropertyId.executeQuery();
  }

  /**
   * {@inheritDoc}
   */
  protected ResultSet findValueByPropertyIdOrderNumber(String cid, int orderNumb) throws SQLException {
    if (findValueByPropertyIdOrderNumber == null)
      findValueByPropertyIdOrderNumber = dbConnection.prepareStatement(FIND_VALUE_BY_PROPERTYID_OREDERNUMB);
    else
      findValueByPropertyIdOrderNumber.clearParameters();

    findValueByPropertyIdOrderNumber.setString(1, cid);
    findValueByPropertyIdOrderNumber.setInt(2, orderNumb);
    return findValueByPropertyIdOrderNumber.executeQuery();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int renameNode(NodeData data) throws SQLException, IOException {
    if (renameNode == null)
      renameNode = dbConnection.prepareStatement(RENAME_NODE);
    else
      renameNode.clearParameters();

    renameNode.setString(1, data.getParentIdentifier() == null ? Constants.ROOT_PARENT_UUID
                                                              : data.getParentIdentifier());
    renameNode.setString(2, data.getQPath().getName().getAsString());
    renameNode.setInt(3, data.getPersistedVersion());
    renameNode.setInt(4, data.getQPath().getIndex());
    renameNode.setInt(5, data.getOrderNumber());
    renameNode.setString(6, data.getIdentifier());
    return renameNode.executeUpdate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResultSet findValuesStorageDescriptorsByPropertyId(String cid) throws SQLException {
    if (findValuesStorageDescriptorsByPropertyId == null)
      findValuesStorageDescriptorsByPropertyId = dbConnection.prepareStatement(FIND_VALUES_VSTORAGE_DESC_BY_PROPERTYID);
    else
      findValuesStorageDescriptorsByPropertyId.clearParameters();

    findValuesStorageDescriptorsByPropertyId.setString(1, cid);
    return findValuesStorageDescriptorsByPropertyId.executeQuery();
  }
}
