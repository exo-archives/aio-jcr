/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedNodeData;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedPropertyData;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CleanableFileStreamValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: JDBCStorageConnection.java 13869 2007-03-28 13:50:50Z peterit $
 */

abstract public class JDBCStorageConnection extends DBConstants implements WorkspaceStorageConnection {

  protected static Log log = ExoLogger.getLogger("jcr.JDBCStorageConnection");

  protected final ValueStoragePluginProvider valueStorageProvider;
  protected final int maxBufferSize;
  protected final File swapDirectory;
  protected final FileCleaner swapCleaner;

  protected final Connection dbConnection;

  protected final String containerName;

  protected final SQLExceptionHandler exceptionHandler;

  protected JDBCStorageConnection(Connection dbConnection,
      String containerName, ValueStoragePluginProvider valueStorageProvider,
      int maxBufferSize, File swapDirectory, FileCleaner swapCleaner) throws SQLException {

    this.valueStorageProvider = valueStorageProvider;

    this.maxBufferSize = maxBufferSize;
    this.swapDirectory = swapDirectory;
    this.swapCleaner = swapCleaner;
    this.containerName = containerName;

    this.dbConnection = dbConnection;

    // Fix for Sybase jConnect JDBC driver bug.
    // Which throws SQLException(JZ016: The AutoCommit option is already set to false)
    // if conn.setAutoCommit(false) called twise or more times with value 'false'.
    if (dbConnection.getAutoCommit()) {
      dbConnection.setAutoCommit(false);
    }

    prepareQueries();
    this.exceptionHandler = new SQLExceptionHandler(containerName, this);
  }

  /**
   * For test only
   * Return JDBC connection obtained from initialized data source.
   * NOTE: Helper can obtain one new connection per each call of the method  or return one obtained once.
   *
   * @throws SQLException
   */
  public Connection getJdbcConnection() throws SQLException {
    return dbConnection;
  }

  /**
   * Prepared queries at start time
   * @throws SQLException
   */
  abstract protected void prepareQueries() throws SQLException;


  /**
   * Used in Single Db Connection classes for UUID related queries
   * @param uuid
   * @return
   */
  protected abstract String getInternalId(String uuid);

  /**
   * Used in loadXYZRecord methods for extract real UUID from container value.
   * @param internalId
   * @return
   */
  protected abstract String getUuid(String internalId);

  // ---------------- WorkspaceStorageConnection -------------

  /**
   * @throws IllegalStateException if connection is closed
   */
  protected void checkIfOpened() throws IllegalStateException {
    if(!isOpened())
      throw new IllegalStateException("Connection is closed");
  }

  /** Will be used as implementation of
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#isOpened()
   */
  public boolean isOpened() {
    try {
      return !dbConnection.isClosed();
    } catch (SQLException e) {
      log.error(e);
      return false;
    }
  }

  /** Will be used as implementation of
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#rollback()
   */
  public final void rollback() throws IllegalStateException, RepositoryException {
    checkIfOpened();
    try {
      dbConnection.rollback();
      dbConnection.close();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** Will be used as implementation of
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#commit()
   */
  public final void commit() throws IllegalStateException, RepositoryException {
    checkIfOpened();
    try {
      dbConnection.commit();
      dbConnection.close();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#add(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public void add(NodeData data) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException {
    checkIfOpened();
    try {
      addNodeRecord(data);
      if (log.isDebugEnabled())
        log.debug("Node added " + data.getQPath().getAsString() + ", " + data.getUUID() + ", " + data.getPrimaryTypeName().getAsString());

    } catch (SQLException e) {
      log.error("Node add. Database error: " + e, e);
      exceptionHandler.handleAddException(e, data);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#add(org.exoplatform.services.jcr.datamodel.PropertyData)
   */
  public void add(PropertyData data) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException {
    checkIfOpened();

    try {
      addPropertyRecord(data);

      if (data.getType() == PropertyType.REFERENCE) {
        try {
          addReference(data);
        } catch(IOException e) {
          throw new RepositoryException("Can't read REFERENCE property ("+data.getQPath()+" "+data.getUUID()+") value: " + e.getMessage(), e);
        }
      }

      ValueIOChannel channel = valueStorageProvider.getApplicableChannel(data);
      if (channel != null) {
        channel.write(data.getUUID(), data.getValues());
        channel.close();
      } else {
        addValues(getInternalId(data.getUUID()), data.getValues());
      }

      if (log.isDebugEnabled())
        log.debug("Property added " + data.getQPath().getAsString() + ", " + data.getUUID()
            + (data.getValues() != null ? ", values count: " + data.getValues().size() : ", NULL data")
            + ", use channel "+channel);

    } catch (IOException e) {
      log.error("Property add. IO error: " + e, e);
      exceptionHandler.handleAddException(e, data);
    } catch (SQLException e) {
      log.error("Property add. Database error: " + e, e);
      exceptionHandler.handleAddException(e, data);
    }
  }


  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#delete(org.exoplatform.services.jcr.datamodel.ItemData)
   */
  public void delete(ItemData data) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException {
    checkIfOpened();

    final String cid = getInternalId(data.getUUID());

    try {
      if (data.isNode()) {
        if (deleteNode(cid) <= 0) {
          // [PN] it's error state, as we actually didn't delete the node (with sub-nodes)
          log.warn("Error state, the node (with sub-nodes) is not deleted " + data.getQPath().getAsString());
        }
      } else {
        if (deleteProperty(cid) <= 0) {
          // [PN] it's error state, as we actually didn't delete the property
          log.warn("Error state, the property item is not deleted " + data.getQPath().getAsString());
        }
      }

      if (log.isDebugEnabled())
        if (data.isNode())
          log.debug("Node deleted " + data.getQPath().getAsString() + ", " + data.getUUID() + ", " + ((NodeData) data).getPrimaryTypeName().getAsString());
        else
          log.debug("Property deleted " + data.getQPath().getAsString() + ", " + data.getUUID()
              + (((PropertyData) data).getValues() != null ? ", values count: " + ((PropertyData) data).getValues().size() : ", NULL data"));

    } catch (SQLException e) {
      log.error("Item remove. Database error: " + e, e);
      exceptionHandler.handleDeleteException(e, data);
    }
  }

  /**
   * @param cid, must be ready for container (e.g. if single-db then with prefix)
   */
  protected int deleteNode(String cid) throws SQLException {
    int deleted = 0;

    // deleting from NODE table
    if (deleteNodeByUUID(cid) <= 0) {
      // [PN] it's error state, as we actually didn't delete the node
      log.warn("Error state, the node actually not deleted " + cid);
    }

    // deleting from ITEM table
    int nc = deleteItemByUUID(cid);
    if (nc <= 0) {
      // [PN] it's error state, as we actually didn't delete a item corresponding the node
      log.warn("Error state, a item corresponding the node actually not deleted " + cid);
    }
    deleted += nc;
    return deleted;
  }

  protected int deleteProperty(String cid) throws SQLException, RepositoryException {

    PropertyData data = (PropertyData) getItemByUUID(cid); // by container id

    try {
      // delete value
      ValueIOChannel channel = valueStorageProvider.getApplicableChannel(data);
      if (channel != null) {
        channel.delete(data.getUUID()); // by API UUI, not by cid
        channel.close();
      } else {
        deleteValues(cid);
      }

      //if (log.isDebugEnabled())
      //  log.debug("values deleted " + data.getUUID() + " use channel " + channel);

      deleteReference(cid);

      // delete property
      if (deletePropertyByUUID(cid) <= 0) {
        log.warn("Error state, the property actually is not deleted " + cid);
      }

      // delete item
      return deleteItemByUUID(cid);
    } catch (IOException e) {
      exceptionHandler.handleDeleteException(e, data);
      return 0; // will newer returns
    }
  }

  public void doReindex(String itemUuid, String oldQPath, int indexDelimPos, int oldIndexLength, String newIndexStr) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException, SQLException {

    // internal class for storing reindex result and freeing JDBC statement (i.e. client-cursor)
    class ItemId {
      private final String cid;
      private final String path;
      private final int version;
      
      ItemId(String cid, String path, int version) {
        this.cid = cid;
        this.path = path;
        this.version = version;
      }

      public String getCid() {
        return cid;
      }

      public String getPath() {
        return path;
      }

      public int getVersion() {
        return version;
      }
    }
    
    // child nodes
    final ResultSet dnrs = findDescendantNodes(itemUuid, oldQPath);
    final List<ItemId> descendantNodes = new ArrayList<ItemId>(); 
    while (dnrs.next()) {
      descendantNodes.add(new ItemId(dnrs.getString(COLUMN_ID), dnrs.getString(COLUMN_PATH), dnrs.getInt(COLUMN_VERSION)));
    }
    dnrs.close();
          
    for (ItemId item: descendantNodes) {  
      String newDescPath = item.getPath().substring(0, indexDelimPos) + newIndexStr
        + item.getPath().substring(indexDelimPos + oldIndexLength);
      if (updateItemPathByUUID(newDescPath, item.getVersion() + 1, item.getCid()) <= 0)
        log.warn("No nodes was updated during reindex " + item.getPath() + " -> " + newDescPath + ", uuid:" + item.getCid());
      else {
        // DO IT RECURSIVE
        doReindex(item.getCid(), oldQPath, indexDelimPos, oldIndexLength, newIndexStr);

        if (log.isDebugEnabled())
          log.debug("Reindex node " + item.getPath() + " -> " + newDescPath + ", " + item.getCid());
      }
    }

    // child properties
    final ResultSet dprs = findDescendantProperties(itemUuid, oldQPath);
    final List<ItemId> descendantProperties = new ArrayList<ItemId>();
    while (dprs.next()) {
      descendantProperties.add(new ItemId(dprs.getString(COLUMN_ID), dprs.getString(COLUMN_PATH), dprs.getInt(COLUMN_VERSION)));
    }
    dprs.close();
      
    for (ItemId item: descendantProperties) {
      final String newDescPath = item.getPath().substring(0, indexDelimPos) + newIndexStr
        + item.getPath().substring(indexDelimPos + oldIndexLength);
      if (updateItemPathByUUID(newDescPath, item.getVersion() + 1, item.getCid()) <= 0)
        log.warn("No nodes was updated during reindex " + item.getPath() + " -> " + newDescPath + ", uuid:" + item.getCid());
      else if (log.isDebugEnabled())
        log.debug("Reindex property " + item.getPath() + " -> " + newDescPath + ", " + item.getCid());
    }
  }

  public void reindex(NodeData oldData, NodeData data) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException {
    checkIfOpened();
    try {
      String cid = getInternalId(data.getUUID());

      String oldQPath = oldData.getQPath().getAsString();
      String newQPath = data.getQPath().getAsString();

      int indexDelimPos = oldQPath.lastIndexOf(":");
      if (newQPath.charAt(indexDelimPos) != ':')
        throw new RepositoryException("Reindex. An old and a new node has a different locations in workspace. "
            + oldQPath + ", " + newQPath);

      indexDelimPos++; // pos of the first char of a index string
      
      String newIndexStr = newQPath.substring(indexDelimPos);
      int oldIndexLength = oldQPath.length() - indexDelimPos;

      // reindex node
      if (updateItemPathByUUID(newQPath, data.getPersistedVersion() + 1, cid) <= 0)
        log.warn("No nodes was updated during reindex " + oldQPath + " -> " + newQPath + ", uuid:" + cid);
      else if (log.isDebugEnabled())
        log.debug("Reindex root node " + oldQPath + " -> " + newQPath + ", " + cid + ", " + data.getPrimaryTypeName().getAsString());

      // reindex childs
      doReindex(cid, oldQPath, indexDelimPos, oldIndexLength, newIndexStr);

    } catch (RepositoryException e) {
      throw new RepositoryException(e);
    } catch (Exception e) { // SQL
      log.error("Node reindex. Database error: " + e, e);
      exceptionHandler.handleUpdateException(e, data);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#update(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public void update(NodeData data) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException {
    checkIfOpened();
    try {
      String cid = getInternalId(data.getUUID());
      // version update
      updateItemVersionByUUID(data.getPersistedVersion(), cid);
      // order numb update
      updateNodeOrderNumbByUUID(data.getOrderNumber(), cid);

      if (log.isDebugEnabled())
        log.debug("Node updated " + data.getQPath().getAsString() + ", " + data.getUUID() + ", " + data.getPrimaryTypeName().getAsString());

    } catch (SQLException e) {
      log.error("Node update. Database error: " + e, e);
      exceptionHandler.handleUpdateException(e, data);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#update(org.exoplatform.services.jcr.datamodel.PropertyData)
   */
  public void update(PropertyData data) throws RepositoryException, UnsupportedOperationException,
  InvalidItemStateException, IllegalStateException {
    checkIfOpened();

    try {
      String cid = getInternalId(data.getUUID());

      // version update
      updateItemVersionByUUID(data.getPersistedVersion(), cid);

      // update type
      updatePropertyTypeByUUID(data.getType(), cid);

      // update reference
      try {
        deleteReference(cid);

        if (data.getType() == PropertyType.REFERENCE) {
          addReference(data);
        }
      } catch(IOException e) {
        throw new RepositoryException("Can't update REFERENCE property ("+data.getQPath()+" "+data.getUUID()+") value: " + e.getMessage(), e);
      }

      ValueIOChannel channel = valueStorageProvider.getApplicableChannel(data);
      if (channel != null) {
        channel.delete(data.getUUID()); // by API UUID, not by cid
        channel.write(data.getUUID(), data.getValues());
        channel.close();
      } else {
        deleteValues(cid);
        addValues(cid, data.getValues());
      }

      if (log.isDebugEnabled())
        log.debug("Property updated " + data.getQPath().getAsString() + ", " + data.getUUID()
            + (data.getValues() != null ? ", values count: " + data.getValues().size() : ", NULL data")
            + ", use channel "+channel);

    } catch (IOException e) {
      log.error("Property update. IO error: " + e, e);
      exceptionHandler.handleUpdateException(e, data);
    } catch (SQLException e) {
      log.error("Property update. Database error: " + e, e);
      exceptionHandler.handleUpdateException(e, data);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getChildNodesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet node = findChildNodesByParentUUID(getInternalId(parent.getUUID()));
      //List<NodeData> childrens = new ItemsArrayList<NodeData>(); // [PN] Debug
      List<NodeData> childrens = new ArrayList<NodeData>();
      while(node.next()) {
        if (node.getString(COLUMN_NID) != null) {
          childrens.add(loadNodeRecord(node));
        } else {
          throw new RepositoryException("FATAL: Not found child node for parent "+parent.getQPath().getAsString()
              + ", but child item found " + node.getString(COLUMN_PATH) + " " + getUuid(node.getString(COLUMN_ID)));
        }
      }
      return childrens;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RepositoryException(e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getChildPropertiesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet prop = findChildPropertiesByParentUUID(getInternalId(parent.getUUID()));
      List<PropertyData> children = new ArrayList<PropertyData>();
      while(prop.next()) {
        if (prop.getString(COLUMN_PID) != null) {
          children.add(loadPropertyRecord(prop));
        } else {
          throw new RepositoryException("FATAL: Not found child property for parent "+parent.getQPath().getAsString()
              + ", but child item found " + prop.getString(COLUMN_PATH) + " " + getUuid(prop.getString(COLUMN_ID)));
        }

      }
      return children;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RepositoryException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RepositoryException(e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getItemData(org.exoplatform.services.jcr.datamodel.InternalQPath)
   */
  public ItemData getItemData(QPath qPath) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    ResultSet item = null;
    try {
      item = findItemByPath(qPath.getAsString());
      if (item.next())
        return itemData(item);
      return null;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RepositoryException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RepositoryException(e);
    } finally {
      try {
        if (item != null)
          item.close();
      } catch(SQLException e) {
        log.error("getItemData() Error close resultset " + e.getMessage());
      }
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getItemData(java.lang.String)
   */
  public ItemData getItemData(String uuid) throws RepositoryException, IllegalStateException {
    return getItemByUUID(getInternalId(uuid));
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getReferencesData(java.lang.String)
   */
  public List<PropertyData> getReferencesData(String nodeUUID) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet refProps = findReferences(getInternalId(nodeUUID));
      List<PropertyData> references = new ArrayList<PropertyData>();
      while(refProps.next()) {
        references.add(loadPropertyRecord(refProps));
      }
      return references;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RepositoryException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RepositoryException(e);
    }
  }

  // ------------------ Private methods ---------------


  protected ItemData getItemByUUID(String cid) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    ResultSet item = null;
    try {
      item = findItemByUUID(cid);
      if(item.next()) {
        return itemData(item);
      }
      return null;
    } catch (SQLException e) {
      throw new RepositoryException("getItemData() error", e);
    } catch (IOException e) {
      throw new RepositoryException("getItemData() error", e);
    } finally {
        try {
          if (item != null)
            item.close();
        } catch(SQLException e) {
          log.error("getItemData() Error close resultset " + e.getMessage());
        }
    }
  }

  /**
   * @param itemRecord
   * @return
   * @throws RepositoryException
   * @throws SQLException
   */
  private ItemData itemData(ResultSet itemRecord) throws RepositoryException, SQLException, IOException {
    String id = itemRecord.getString(COLUMN_ID);

    boolean isNode = itemRecord.getString(COLUMN_NID) != null;
    boolean isProperty = itemRecord.getString(COLUMN_PID) != null;

    if (isNode && isProperty) {
      // inconsistent data stored in db
      throw new RepositoryException("FATAL: Item stored as node and as property "+itemRecord.getString(COLUMN_PATH) + " " + getUuid(id));
    }

    if (!isNode && !isProperty) {
      // no data stored for item - node or property
      throw new RepositoryException("FATAL: Not found corresponding node nor property for item "+itemRecord.getString(COLUMN_PATH) + " " + getUuid(id));
    }

    if (isNode)
      return loadNodeRecord(itemRecord);

    // property
    return loadPropertyRecord(itemRecord);
  }

  protected PersistedNodeData loadNodeRecord(ResultSet item) throws RepositoryException, SQLException {

    String cNID = item.getString(COLUMN_NID);
    String cNPARENTID = item.getString(COLUMN_NPARENTID);
    int cVERSION = item.getInt(COLUMN_VERSION);
    int cNORDERNUM = item.getInt(COLUMN_NORDERNUM);
    QPath qpath = QPath.parse(item.getString(COLUMN_PATH));

    // PRIMARY
    QPath ptPath = QPath.makeChildPath(qpath, Constants.JCR_PRIMARYTYPE);

    ResultSet ptProp = findPropertyByPath(cNID, ptPath.getAsString());

    if (!ptProp.next())
    //if (idPrimaryType == null)
      throw new PrimaryTypeNotFoundException("FATAL ERROR primary type record not found "
          + ptPath.getAsString() + ", parent id " + cNID +  ", container "+this.containerName, ptPath);

    ResultSet ptValue = findValuesByPropertyId(ptProp.getString(COLUMN_PID));

    if (!ptValue.next())
      throw new RepositoryException("FATAL ERROR primary type value not found "
          + ptPath.getAsString() + ", id " + ptProp.getString(COLUMN_ID) + ", container " + this.containerName);

    byte[] data = ptValue.getBytes(COLUMN_DATA);
    InternalQName ptName = InternalQName.parse(new String((data != null ? data : new byte[] {})));

    // MIXIN
    QPath mtPath = QPath.makeChildPath(qpath, Constants.JCR_MIXINTYPES);
    ResultSet mtProp = findPropertyByPath(cNID, mtPath.getAsString());

    InternalQName[] mixinNames = new InternalQName[0];
    if(mtProp.next()) {
      List<byte[]> mts = new ArrayList<byte[]>();
      ResultSet mtValues = findValuesByPropertyId(mtProp.getString(COLUMN_PID));
      while (mtValues.next()) {
        mts.add(mtValues.getBytes(COLUMN_DATA));
      }
      mixinNames = new InternalQName[mts.size()];
      for (int i = 0; i < mts.size(); i++) {
        mixinNames[i] = InternalQName.parse(new String(mts.get(i)));
      }
    }

    // ACL
    AccessControlList acl;
    if (isAccessControllable(mixinNames)) {

      QPath ownerPath = QPath.makeChildPath(qpath, Constants.EXO_OWNER);

      PropertyData ownerData = (PropertyData) getItemData(ownerPath);

      QPath permPath = QPath.makeChildPath(qpath, Constants.EXO_PERMISSIONS);

      PropertyData permData = (PropertyData) getItemData(permPath);

      acl = new AccessControlList(ownerData, permData);
    } else {
      if(qpath.equals(Constants.ROOT_PATH)) {
        // make default ACL for root
        acl = new AccessControlList();
      } else {
        acl = null;
      }
    }

    return new PersistedNodeData(getUuid(cNID),
        qpath,
        getUuid(cNPARENTID),
        cVERSION,
        cNORDERNUM,
        ptName, mixinNames, acl);
  }

  private boolean isAccessControllable(InternalQName[] mixinNames) {
    for (int i = 0; i < mixinNames.length; i++) {
      if (mixinNames[i].getAsString().equals(Constants.ACCESS_TYPE_URI)
          || mixinNames[i].getAsString().equals(Constants.PRIVILEGABLE_TYPE_URI)) {
        return true;
      }
    }
    return false;
  }

  protected PersistedPropertyData loadPropertyRecord(ResultSet item) throws RepositoryException, SQLException, IOException {

    List<ValueData> values = new ArrayList<ValueData>();
    QPath path = QPath.parse(item.getString(COLUMN_PATH));
    String cid = item.getString(COLUMN_PID);
    String uuid = getUuid(cid);
    PersistedPropertyData pdata = new PersistedPropertyData(uuid,
        path,
        getUuid(item.getString(COLUMN_PPARENTID)),
        item.getInt(COLUMN_VERSION),
        item.getInt(COLUMN_PTYPE),
        item.getBoolean(COLUMN_PMULTIVALUED)
    );

    ValueIOChannel channel = valueStorageProvider.getApplicableChannel(pdata);
    if (channel != null) {
      values = channel.read(uuid, this.maxBufferSize);
      channel.close();
    } else {
      values = readValues(cid);
    }

    pdata.setValues(values);
    return pdata;
  }

  private List<ValueData> readValues(String cid) throws IOException {

    List<ValueData> data = new ArrayList<ValueData>();

    try {

      final ResultSet valueRecords = findValuesByPropertyId(cid);
      try {
        while (valueRecords.next()) {
          final int orderNum = valueRecords.getInt(COLUMN_ORDERNUM);
          ValueData vdata = readValueData(cid, orderNum);
          data.add(vdata);
        }
      } finally {
        valueRecords.close();
      }

    } catch (SQLException e) {
      String msg = "Can't read value data of property with id " + cid + ", error:" + e;
      log.error(msg, e);
      throw new IOException(msg);
    }

    return data;
  }

  protected ValueData readValueData(String cid, int orderNumber) throws SQLException, IOException {

    byte[] buffer = new byte[0];
    byte[] spoolBuffer = new byte[0x2000];
    int read;
    int len = 0;
    OutputStream out = null;
    File spoolFile = null;
    ResultSet valueResultSet = null;
    try {
      // stream from database
      valueResultSet = findValueByPropertyIdOrderNumber(cid, orderNumber);
      if (valueResultSet.next()) {
        final InputStream in = valueResultSet.getBinaryStream(COLUMN_DATA);
        if (in != null) 
          while ((read = in.read(spoolBuffer)) > 0) {
            if (out != null) {
              // spool to temp file
              out.write(spoolBuffer, 0, read);
              len += read;
            } else if (len + read > maxBufferSize) {
              // threshold for keeping data in memory exceeded;
              // create temp file and spool buffer contents
              spoolFile = new File(swapDirectory, cid+orderNumber);
              out = new FileOutputStream(spoolFile);
              out.write(buffer, 0, len);
              out.write(spoolBuffer, 0, read);
              buffer = null;
              len += read;
            } else {
              // reallocate new buffer and spool old buffer contents
              byte[] newBuffer = new byte[len + read];
              System.arraycopy(buffer, 0, newBuffer, 0, len);
              System.arraycopy(spoolBuffer, 0, newBuffer, len, read);
              buffer = newBuffer;
              len += read;
            }
          }
      }
    } finally {
      if (valueResultSet != null)
        valueResultSet.close();
      if (out != null) {
        out.close();
      }
    }

    if(buffer == null)
      return new CleanableFileStreamValueData(spoolFile, orderNumber, false, swapCleaner);

    return new ByteArrayPersistedValueData(buffer, orderNumber);
  }

  // ---- Data access methods (query wrappers) to override in concrete connection ------

  protected abstract void addNodeRecord(NodeData data) throws SQLException;
  protected abstract void addPropertyRecord(PropertyData prop) throws SQLException;

  protected abstract ResultSet findItemByPath(String path) throws SQLException;
  protected abstract ResultSet findItemByUUID(String uuid) throws SQLException;
  protected abstract ResultSet findPropertyByPath(String parentId, String path) throws SQLException;

  protected abstract ResultSet findChildNodesByParentUUID(String parentUUID) throws SQLException;
  protected abstract ResultSet findChildPropertiesByParentUUID(String parentUUID) throws SQLException;

  protected abstract ResultSet findDescendantNodes(String parentId, String parentPath) throws SQLException;
  protected abstract ResultSet findDescendantProperties(String parentId, String parentPath) throws SQLException;

  protected abstract void addReference(PropertyData data) throws SQLException, IOException;
  protected abstract void deleteReference(String propertyUuid) throws SQLException;
  protected abstract ResultSet findReferences(String nodeUuid) throws SQLException;

  protected abstract int deleteItemByUUID(String uuid) throws SQLException;
  protected abstract int deleteNodeByUUID(String uuid) throws SQLException;
  protected abstract int deletePropertyByUUID(String uuid) throws SQLException;

  protected abstract int updateItemVersionByUUID(int versionValue, String uuid) throws SQLException;
  protected abstract int updateItemPathByUUID(String qpath, int version, String uuid) throws SQLException;
  protected abstract int updateNodeOrderNumbByUUID(int orderNumb, String uuid) throws SQLException;
  protected abstract int updatePropertyTypeByUUID(int type, String uuid) throws SQLException;
  
  // -------- values processing ------------
  protected abstract void addValues(String cid, List<ValueData> data) throws IOException, SQLException;
  protected abstract void deleteValues(String cid) throws SQLException;
  protected abstract ResultSet findValuesByPropertyId(String cid) throws SQLException;
  protected abstract ResultSet findValueByPropertyIdOrderNumber(String cid, int orderNumb) throws SQLException;
}
