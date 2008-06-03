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
package org.exoplatform.services.jcr.impl.storage.jdbc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.jcr.InvalidItemStateException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedNodeData;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedPropertyData;
import org.exoplatform.services.jcr.datamodel.IllegalACLException;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CleanableFileStreamValueData;
import org.exoplatform.services.jcr.impl.storage.JCRInvalidItemStateException;
import org.exoplatform.services.jcr.impl.storage.value.ValueDataNotFoundException;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SwapFile;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: JDBCStorageConnection.java 13366 2008-04-17 09:12:24Z pnedonosko $
 */

abstract public class JDBCStorageConnection extends DBConstants implements WorkspaceStorageConnection {

  protected static Log                       log              = ExoLogger.getLogger("jcr.JDBCStorageConnection");

  public static final int                    I_CLASS_NODE     = 1;

  public static final int                    I_CLASS_PROPERTY = 2;

  protected final ValueStoragePluginProvider valueStorageProvider;

  protected final int                        maxBufferSize;

  protected final File                       swapDirectory;

  protected final FileCleaner                swapCleaner;

  protected final Connection                 dbConnection;

  protected final String                     containerName;

  protected final SQLExceptionHandler        exceptionHandler;

  protected JDBCStorageConnection(Connection dbConnection,
                                  String containerName,
                                  ValueStoragePluginProvider valueStorageProvider,
                                  int maxBufferSize,
                                  File swapDirectory,
                                  FileCleaner swapCleaner) throws SQLException {

    this.valueStorageProvider = valueStorageProvider;

    this.maxBufferSize = maxBufferSize;
    this.swapDirectory = swapDirectory;
    this.swapCleaner = swapCleaner;
    this.containerName = containerName;

    this.dbConnection = dbConnection;

    // Fix for Sybase jConnect JDBC driver bug.
    // Which throws SQLException(JZ016: The AutoCommit option is already set to
    // false)
    // if conn.setAutoCommit(false) called twise or more times with value
    // 'false'.
    if (dbConnection.getAutoCommit()) {
      dbConnection.setAutoCommit(false);
    }

    prepareQueries();
    this.exceptionHandler = new SQLExceptionHandler(containerName, this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;

    if (obj instanceof JDBCStorageConnection) {
      JDBCStorageConnection another = (JDBCStorageConnection) obj;
      return getJdbcConnection() == another.getJdbcConnection();
    }

    return false;
  }

  /**
   * Return JDBC connection obtained from initialized data source. NOTE: Helper can obtain one new connection per each call of the method or return one obtained
   * once.
   */
  public Connection getJdbcConnection() {
    return dbConnection;
  }

  /**
   * Prepared queries at start time
   * 
   * @throws SQLException
   */
  abstract protected void prepareQueries() throws SQLException;

  /**
   * Used in Single Db Connection classes for Identifier related queries
   * 
   * @param identifier
   * @return
   */
  protected abstract String getInternalId(String identifier);

  /**
   * Used in loadXYZRecord methods for extract real Identifier from container value.
   * 
   * @param internalId
   * @return
   */
  protected abstract String getIdentifier(String internalId);

  // ---------------- WorkspaceStorageConnection -------------

  /**
   * @throws IllegalStateException if connection is closed
   */
  protected void checkIfOpened() throws IllegalStateException {
    if (!isOpened())
      throw new IllegalStateException("Connection is closed");
  }

  /**
   * Check if database connection is opened.
   * 
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

  /**
   * Commit database connection and close it.
   * 
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

  /**
   * Roll back database connection and close it.
   * 
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

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#add(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public void add(NodeData data) throws RepositoryException,
                                UnsupportedOperationException,
                                InvalidItemStateException,
                                IllegalStateException {
    checkIfOpened();
    try {
      addNodeRecord(data);
      if (log.isDebugEnabled())
        log.debug("Node added " + data.getQPath().getAsString() + ", " + data.getIdentifier() + ", "
            + data.getPrimaryTypeName().getAsString());

    } catch (SQLException e) {
      if (log.isDebugEnabled())
        log.error("Node add. Database error: " + e);

      exceptionHandler.handleAddException(e, data);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#add(org.exoplatform.services.jcr.datamodel.PropertyData)
   */
  public void add(PropertyData data) throws RepositoryException,
                                    UnsupportedOperationException,
                                    InvalidItemStateException,
                                    IllegalStateException {
    checkIfOpened();

    try {
      addPropertyRecord(data);

      if (data.getType() == PropertyType.REFERENCE) {
        try {
          addReference(data);
        } catch (IOException e) {
          throw new RepositoryException("Can't read REFERENCE property (" + data.getQPath() + " " + data.getIdentifier()
              + ") value: " + e.getMessage(), e);
        }
      }

      addValues(data);

      if (log.isDebugEnabled())
        log.debug("Property added " + data.getQPath().getAsString() + ", " + data.getIdentifier()
            + (data.getValues() != null ? ", values count: " + data.getValues().size() : ", NULL data"));

    } catch (IOException e) {
      if (log.isDebugEnabled())
        log.error("Property add. IO error: " + e, e);
      exceptionHandler.handleAddException(e, data);
    } catch (SQLException e) {
      if (log.isDebugEnabled())
        log.error("Property add. Database error: " + e, e);
      exceptionHandler.handleAddException(e, data);
    }
  }

  public void rename(NodeData data) throws RepositoryException,
                                   UnsupportedOperationException,
                                   InvalidItemStateException,
                                   IllegalStateException {

    checkIfOpened();
    try {
      if (renameNode(data) <= 0)
        throw new JCRInvalidItemStateException("(rename) Node " + data.getQPath().getAsString() + " " + data.getIdentifier()
            + " is not renamed. Probably was deleted by another session ", data.getIdentifier(), ItemState.RENAMED);
    } catch (IOException e) {
      if (log.isDebugEnabled())
        log.error("Property add. IO error: " + e, e);
      exceptionHandler.handleAddException(e, data);
    } catch (SQLException e) {
      if (log.isDebugEnabled())
        log.error("Property add. Database error: " + e, e);
      exceptionHandler.handleAddException(e, data);
    }
  }

  public void delete(NodeData data) throws RepositoryException,
                                   UnsupportedOperationException,
                                   InvalidItemStateException,
                                   IllegalStateException {
    checkIfOpened();

    final String cid = getInternalId(data.getIdentifier());

    try {
      int nc = deleteItemByIdentifier(cid);
      if (nc <= 0)
        throw new JCRInvalidItemStateException("(delete) Node " + data.getQPath().getAsString() + " " + data.getIdentifier()
            + " not found. Probably was deleted by another session ", data.getIdentifier(), ItemState.DELETED);

      if (log.isDebugEnabled())
        log.debug("Node deleted " + data.getQPath().getAsString() + ", " + data.getIdentifier() + ", "
            + ((NodeData) data).getPrimaryTypeName().getAsString());

    } catch (SQLException e) {
      if (log.isDebugEnabled())
        log.error("Node remove. Database error: " + e, e);
      exceptionHandler.handleDeleteException(e, data);
    }
  }

  public void delete(PropertyData data) throws RepositoryException,
                                       UnsupportedOperationException,
                                       InvalidItemStateException,
                                       IllegalStateException {
    checkIfOpened();

    final String cid = getInternalId(data.getIdentifier());

    try {
      deleteExternalValues(cid, data);
      deleteValues(cid);

      // delete references
      deleteReference(cid);

      // delete item
      int nc = deleteItemByIdentifier(cid);
      if (nc <= 0)
        throw new JCRInvalidItemStateException("(delete) Property " + data.getQPath().getAsString() + " " + data.getIdentifier()
            + " not found. Probably was deleted by another session ", data.getIdentifier(), ItemState.DELETED);

      if (log.isDebugEnabled())
        log.debug("Property deleted "
            + data.getQPath().getAsString()
            + ", "
            + data.getIdentifier()
            + (((PropertyData) data).getValues() != null ? ", values count: " + ((PropertyData) data).getValues().size()
                : ", NULL data"));

    } catch (IOException e) {
      if (log.isDebugEnabled())
        log.error("Property remove. IO error: " + e, e);
      exceptionHandler.handleDeleteException(e, data);
    } catch (SQLException e) {
      if (log.isDebugEnabled())
        log.error("Property remove. Database error: " + e, e);
      exceptionHandler.handleDeleteException(e, data);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#update(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public void update(NodeData data) throws RepositoryException,
                                   UnsupportedOperationException,
                                   InvalidItemStateException,
                                   IllegalStateException {
    checkIfOpened();
    try {
      String cid = getInternalId(data.getIdentifier());
      // order numb update
      if (updateNodeByIdentifier(data.getPersistedVersion(), data.getQPath().getIndex(), data.getOrderNumber(), cid) <= 0)
        throw new JCRInvalidItemStateException("(update) Node " + data.getQPath().getAsString() + " " + data.getIdentifier()
           + " is not updated. Probably was deleted by another session ", data.getIdentifier(), ItemState.UPDATED);

      if (log.isDebugEnabled())
        log.debug("Node updated " + data.getQPath().getAsString() + ", " + data.getIdentifier() + ", "
            + data.getPrimaryTypeName().getAsString());

    } catch (SQLException e) {
      if (log.isDebugEnabled())
        log.error("Node update. Database error: " + e, e);
      exceptionHandler.handleUpdateException(e, data);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#update(org.exoplatform.services.jcr.datamodel.PropertyData)
   */
  public void update(PropertyData data) throws RepositoryException,
                                       UnsupportedOperationException,
                                       InvalidItemStateException,
                                       IllegalStateException {
    checkIfOpened();

    try {
      String cid = getInternalId(data.getIdentifier());

      // update type
      if (updatePropertyByIdentifier(data.getPersistedVersion(), data.getType(), cid) <= 0)
        throw new JCRInvalidItemStateException("(update) Property " + data.getQPath().getAsString() + " " + data.getIdentifier()
          + " is not updated. Probably was deleted by another session ", data.getIdentifier(), ItemState.UPDATED);

      // update reference
      try {
        deleteReference(cid);

        if (data.getType() == PropertyType.REFERENCE) {
          addReference(data);
        }
      } catch (IOException e) {
        throw new RepositoryException("Can't update REFERENCE property (" + data.getQPath() + " " + data.getIdentifier()
            + ") value: " + e.getMessage(), e);
      }

      deleteExternalValues(cid, data);
      deleteValues(cid);

      addValues(data);

      if (log.isDebugEnabled())
        log.debug("Property updated " + data.getQPath().getAsString() + ", " + data.getIdentifier()
            + (data.getValues() != null ? ", values count: " + data.getValues().size() : ", NULL data"));

    } catch (IOException e) {
      if (log.isDebugEnabled())
        log.error("Property update. IO error: " + e, e);
      exceptionHandler.handleUpdateException(e, data);
    } catch (SQLException e) {
      if (log.isDebugEnabled())
        log.error("Property update. Database error: " + e, e);
      exceptionHandler.handleUpdateException(e, data);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getChildNodesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet node = findChildNodesByParentIdentifier(getInternalId(parent.getIdentifier()));
      List<NodeData> childrens = new ArrayList<NodeData>();
      while (node.next()) {
        childrens.add((NodeData) itemData(parent.getQPath(), node, I_CLASS_NODE, parent.getACL()));
      }
      return childrens;
    } catch (SQLException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getChildPropertiesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet prop = findChildPropertiesByParentIdentifier(getInternalId(parent.getIdentifier()));
      List<PropertyData> children = new ArrayList<PropertyData>();
      while (prop.next()) {
        children.add((PropertyData) itemData(parent.getQPath(), prop, I_CLASS_PROPERTY, null)); // property
        // doesn't
        // ACL
        // aware
      }
      return children;
    } catch (SQLException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  public List<PropertyData> listChildPropertiesData(NodeData parent) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet prop = findChildPropertiesByParentIdentifier(getInternalId(parent.getIdentifier()));
      List<PropertyData> children = new ArrayList<PropertyData>();
      while (prop.next()) {
        children.add(propertyData(parent.getQPath(), prop));
      }
      return children;
    } catch (SQLException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getItemData(java.lang.String)
   */
  public ItemData getItemData(String identifier) throws RepositoryException, IllegalStateException {
    return getItemByIdentifier(getInternalId(identifier));
  }

  public ItemData getItemData(NodeData parentData, QPathEntry name) throws RepositoryException, IllegalStateException {

    if (parentData != null) {
      return getItemByName(parentData, getInternalId(parentData.getIdentifier()), name);
    }

    // it's a root node
    return getItemByName(null, null, name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getReferencesData(java.lang.String)
   */
  public List<PropertyData> getReferencesData(String nodeIdentifier) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet refProps = findReferences(getInternalId(nodeIdentifier));
      List<PropertyData> references = new ArrayList<PropertyData>();
      while (refProps.next()) {
        references.add((PropertyData) itemData(null, refProps, I_CLASS_PROPERTY, null));
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

  protected ItemData getItemByIdentifier(String cid) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    ResultSet item = null;
    try {
      item = findItemByIdentifier(cid);
      if (item.next()) {
        return itemData(null, item, item.getInt(COLUMN_CLASS), null);
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
      } catch (SQLException e) {
        log.error("getItemData() Error close resultset " + e.getMessage());
      }
    }
  }

  /**
   * Gets an item data from database.
   * 
   * @param parentPath - parent QPath
   * @param parentId - parent container internal id (depends on Multi/Single DB)
   * @param name - item name
   * @return - ItemData instance
   * @throws RepositoryException
   * @throws IllegalStateException
   */
  protected ItemData getItemByName(NodeData parent, String parentId, QPathEntry name) throws RepositoryException,
                                                                                     IllegalStateException {
    checkIfOpened();
    ResultSet item = null;
    try {
      item = findItemByName(parentId, name.getAsString(), name.getIndex());
      if (item.next())
        return itemData(parent.getQPath(), item, item.getInt(COLUMN_CLASS), parent.getACL());
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
      } catch (SQLException e) {
        log.error("getItemData() Error close resultset " + e.getMessage());
      }
    }
  }

  /**
   * Find parent path in db by cpid
   * 
   * @param cpid - initial parent id
   * @return
   * @throws SQLException
   * @throws InvalidItemStateException
   * @throws IllegalNameException
   */
  private QPath traverseQPath(String cpid) throws SQLException, InvalidItemStateException, IllegalNameException {
    // get item by Identifier usecase
    List<QPathEntry> qrpath = new ArrayList<QPathEntry>(); // reverted path
    String caid = cpid; // container ancestor id
    do {
      ResultSet parent = null;
      try {
        parent = findItemByIdentifier(caid);
        if (!parent.next())
          throw new InvalidItemStateException("Parent not found, uuid: " + getIdentifier(caid));

        QPathEntry qpe = new QPathEntry(InternalQName.parse(parent.getString(COLUMN_NAME)), parent.getInt(COLUMN_INDEX));
        qrpath.add(qpe);
        caid = parent.getString(COLUMN_PARENTID);
      } finally {
        parent.close();
      }
    } while (!caid.equals(Constants.ROOT_PARENT_UUID));

    QPathEntry[] qentries = new QPathEntry[qrpath.size()];
    int qi = 0;
    for (int i = qrpath.size() - 1; i >= 0; i--) {
      qentries[qi++] = qrpath.get(i);
    }
    return new QPath(qentries);
  }

  class ItemLocationInfo {
    /**
     * Item qpath
     */
    final QPath        qpath;

    /**
     * All ancestors of the item with qpath
     */
    final List<String> ancestors;

    final String       itemId;

    ItemLocationInfo(QPath qpath, List<String> ancestors, String itemId) {
      this.qpath = qpath;
      this.ancestors = ancestors;
      this.itemId = itemId;
    }
  }

  /**
   * Find ancestor permissions by cpid. Will search till find the permissions or meet a root node.
   * 
   * @param cpid - initial parent node id
   * @return Collection<String>
   * @throws SQLException
   * @throws IllegalACLException
   * @throws IllegalNameException
   * @throws RepositoryException
   */
  private List<AccessControlEntry> traverseACLPermissions(String cpid) throws SQLException,
                                                                IllegalACLException,
                                                                IllegalNameException,
                                                                RepositoryException {
    String caid = cpid;
    while (!caid.equals(Constants.ROOT_PARENT_UUID)) {
      MixinInfo naMixins = readMixins(caid);
      if (naMixins.hasPrivilegeable())
        return readACLPermisions(caid);

      if (naMixins.parentId == null)
        caid = findParentId(caid);
      else
        caid = naMixins.parentId;
    }

    throw new IllegalACLException("Can not find permissions for a node with id " + getIdentifier(cpid));
  }

  protected String findParentId(String cid) throws SQLException, RepositoryException {
    ResultSet pidrs = findItemByIdentifier(cid);
    try {
      if (pidrs.next())
        return pidrs.getString(COLUMN_PARENTID);
      else
        throw new RepositoryException("Item not found id: " + getIdentifier(cid));
    } finally {
      pidrs.close();
    }
  }

  /**
   * Find ancestor owner by cpid. Will search till find the owner or meet a root node.
   * 
   * @param cpid - initial parent node id
   * @return owner name
   * @throws SQLException
   * @throws IllegalACLException
   * @throws IllegalNameException
   * @throws RepositoryException
   */
  private String traverseACLOwner(String cpid) throws SQLException,
                                              IllegalACLException,
                                              IllegalNameException,
                                              RepositoryException {
    String caid = cpid;

    while (!caid.equals(Constants.ROOT_PARENT_UUID)) {
      MixinInfo naMixins = readMixins(caid);
      if (naMixins.hasOwneable())
        return readACLOwner(caid);

      if (naMixins.parentId == null)
        caid = findParentId(caid);
      else
        caid = naMixins.parentId;
    }

    throw new IllegalACLException("Can not find owner for a node with id " + getIdentifier(cpid));
  }

  /**
   * Find ancestor ACL by cpid. Will search till find the ACL or meet a root node.
   * 
   * @param cpid - initial parent node id
   * @return owner name
   * @throws SQLException
   * @throws IllegalACLException
   * @throws IllegalNameException
   * @throws RepositoryException
   */
  private AccessControlList traverseACL(String cpid) throws SQLException,
                                                    IllegalACLException,
                                                    IllegalNameException,
                                                    RepositoryException {
    String naOwner = null;
    List<AccessControlEntry> naPermissions = null;

    String caid = cpid;

    while (!caid.equals(Constants.ROOT_PARENT_UUID)) {
      MixinInfo naMixins = readMixins(caid);
      if (naOwner == null && naMixins.hasOwneable()) {
        naOwner = readACLOwner(caid);
        if (naPermissions != null)
          break;
      }
      if (naPermissions == null && naMixins.hasPrivilegeable()) {
        naPermissions = readACLPermisions(caid);
        if (naOwner != null)
          break;
      }

      if (naMixins.parentId == null)
        caid = findParentId(caid);
      else
        caid = naMixins.parentId;
    }

    if (naOwner != null && naPermissions != null) {
      // got all
      return new AccessControlList(naOwner, naPermissions);
    } else if (naOwner == null && naPermissions == null) {
      // Default values (i.e. ACL is disabled in repository)
      return new AccessControlList();
    } else
      throw new IllegalACLException("ACL is not found for node with id " + getIdentifier(cpid)
          + " or for its ancestors. But repository is ACL enabled.");
  }

  /**
   * [PN] Experimental. Use SP for traversing Qpath on the database server side. Hm, I haven't a good result for that yet. Few seconds only for TCK execution.
   * PGSQL SP: CREATE OR REPLACE FUNCTION get_qpath(parentId VARCHAR) RETURNS SETOF record AS $$ DECLARE cur_item RECORD; cur_id varchar; BEGIN cur_id :=
   * parentId; WHILE NOT cur_id = ' ' LOOP SELECT id, name, parent_id, i_index INTO cur_item FROM JCR_SITEM WHERE ID=cur_id; IF NOT found THEN RETURN; END IF;
   * RETURN NEXT cur_item; cur_id := cur_item.parent_id; END LOOP; RETURN; END; $$ LANGUAGE plpgsql;
   * 
   * @param cpid
   * @return
   * @throws SQLException
   * @throws InvalidItemStateException
   * @throws IllegalNameException
   */
  private QPath traverseQPath_SP_PGSQL(String cpid) throws SQLException, InvalidItemStateException, IllegalNameException {
    // get item by Identifier usecase:
    // find parent path in db by cpid
    if (cpid == null) {
      // root node
      return null; // Constants.ROOT_PATH
    } else {
      List<QPathEntry> qrpath = new ArrayList<QPathEntry>(); // reverted path
      PreparedStatement cstmt = null;
      try {
        cstmt =
            dbConnection.prepareStatement("select * from get_qpath(?) AS (id varchar, name varchar, parent_id varchar, i_index int)");
        cstmt.setString(1, cpid);
        // cstmt.setString(2, caid);
        ResultSet parent = cstmt.executeQuery();

        while (parent.next()) {
          QPathEntry qpe = new QPathEntry(InternalQName.parse(parent.getString(COLUMN_NAME)), parent.getInt(COLUMN_INDEX));
          qrpath.add(qpe);
        }

        // parent = findItemByIdentifier(caid);
        if (qrpath.size() <= 0)
          throw new InvalidItemStateException("Parent not found, uuid: " + getIdentifier(cpid));
      } finally {
        if (cstmt != null)
          cstmt.close();
      }

      QPathEntry[] qentries = new QPathEntry[qrpath.size()];
      int qi = 0;
      for (int i = qrpath.size() - 1; i >= 0; i--) {
        qentries[qi++] = qrpath.get(i);
      }
      return new QPath(qentries);
    }
  }

  /**
   * @param parentPath
   * @param item
   * @return item data
   * @throws RepositoryException
   * @throws SQLException
   */
  private ItemData itemData(QPath parentPath, ResultSet item, int itemClass, AccessControlList parentACL) throws RepositoryException,
                                                                                                         SQLException,
                                                                                                         IOException {
    String cid = item.getString(COLUMN_ID);
    String cname = item.getString(COLUMN_NAME);
    int cversion = item.getInt(COLUMN_VERSION);

    String cpid = item.getString(COLUMN_PARENTID);
    // if parent ID is empty string - it's a root node
    // cpid = cpid.equals(Constants.ROOT_PARENT_UUID) ? null : cpid;

    try {
      if (itemClass == I_CLASS_NODE) {
        int cindex = item.getInt(COLUMN_INDEX);
        int cnordernumb = item.getInt(COLUMN_NORDERNUM);
        return loadNodeRecord(parentPath, cname, cid, cpid, cindex, cversion, cnordernumb, parentACL);
      }

      int cptype = item.getInt(COLUMN_PTYPE);
      boolean cpmultivalued = item.getBoolean(COLUMN_PMULTIVALUED);
      return loadPropertyRecord(parentPath, cname, cid, cpid, cversion, cptype, cpmultivalued);
    } catch (InvalidItemStateException e) {
      throw new InvalidItemStateException("FATAL: Can't build item path for name " + cname + " id: " + getIdentifier(cid) + ". "
          + e);
    }
  }

  /**
   * Read property data without value data. For listChildPropertiesData(NodeData).
   * 
   * @param parentPath
   * @param item
   * @return
   * @throws RepositoryException
   * @throws SQLException
   * @throws IOException
   */
  private PropertyData propertyData(QPath parentPath, ResultSet item) throws RepositoryException, SQLException, IOException {
    String cid = item.getString(COLUMN_ID);
    String cname = item.getString(COLUMN_NAME);
    int cversion = item.getInt(COLUMN_VERSION);
    String cpid = item.getString(COLUMN_PARENTID);
    int cptype = item.getInt(COLUMN_PTYPE);
    boolean cpmultivalued = item.getBoolean(COLUMN_PMULTIVALUED);

    try {
      InternalQName qname = InternalQName.parse(cname);

      QPath qpath = QPath.makeChildPath(parentPath == null ? traverseQPath(cpid) : parentPath, qname);

      PersistedPropertyData pdata =
          new PersistedPropertyData(getIdentifier(cid), qpath, getIdentifier(cpid), cversion, cptype, cpmultivalued);

      pdata.setValues(new ArrayList<ValueData>());
      return pdata;
    } catch (InvalidItemStateException e) {
      throw new InvalidItemStateException("FATAL: Can't build property path for name " + cname + " id: " + getIdentifier(cid)
          + ". " + e);
    } catch (IllegalNameException e) {
      throw new RepositoryException(e);
    }
  }

  // protected PersistedNodeData loadNodeRecord(QPath parentPath, String cname,
  // String cid, String cpid, int cindex, int cversion,
  // int cnordernumb) throws RepositoryException, SQLException {
  //
  // try {
  // InternalQName qname = InternalQName.parse(cname);
  // QPath qpath = parentPath != null ? QPath.makeChildPath(parentPath, qname,
  // cindex) : Constants.ROOT_PATH;
  //
  // // PRIMARY
  // ResultSet ptProp = findPropertyByName(cid,
  // Constants.JCR_PRIMARYTYPE.getAsString());
  //
  // if (!ptProp.next())
  // throw new PrimaryTypeNotFoundException("FATAL ERROR primary type record not
  // found. Node " + qpath.getAsString() + ", id "
  // + cid + ", container " + this.containerName, null);
  //
  // byte[] data = ptProp.getBytes(COLUMN_VDATA);
  // InternalQName ptName = InternalQName.parse(new String((data != null ? data
  // : new byte[] {})));
  //
  // // MIXIN
  // ResultSet mtProp = findPropertyByName(cid,
  // Constants.JCR_MIXINTYPES.getAsString());
  //
  // List<InternalQName> mts = new ArrayList<InternalQName>();
  // boolean mixOwneable = false;
  // boolean mixPrivilegeable = false;
  // while (mtProp.next()) {
  // //mts.add();
  // byte[] mxnb = mtProp.getBytes(COLUMN_VDATA);
  // if (mxnb != null) {
  // InternalQName mxn = InternalQName.parse(new String(mxnb));
  //          
  // if (Constants.EXO_PRIVILEGEABLE.equals(mxn))
  // mixPrivilegeable = true;
  // else if (Constants.EXO_OWNEABLE.equals(mxn))
  // mixOwneable = true;
  // } // else, if SQL NULL - skip it
  // }
  // InternalQName[] mixinNames = new InternalQName[mts.size()];
  // mts.toArray(mixinNames);
  //
  // // ACL
  // PropertyData ownerData = null;
  // PropertyData permData = null;
  //
  // if (mixPrivilegeable) {
  // permData = (PropertyData) getItemByName(qpath, cid, new
  // QPathEntry(Constants.EXO_PERMISSIONS, 1));
  // }
  //
  // if (mixOwneable) {
  // ownerData = (PropertyData) getItemByName(qpath, cid, new
  // QPathEntry(Constants.EXO_OWNER, 1));
  // }
  //
  // AccessControlList acl = null;
  // if (permData != null || ownerData != null)
  // acl = new AccessControlList(ownerData, permData);
  //
  // return new PersistedNodeData(getIdentifier(cid), qpath,
  // getIdentifier(cpid), cversion, cnordernumb, ptName, mixinNames,
  // acl, mixPrivilegeable, mixOwneable);
  //
  // } catch (IllegalNameException e) {
  // throw new RepositoryException(e);
  // }
  // }

  class MixinInfo {

    static final int          OWNEABLE               = 0x0001;                  // bits 0001

    static final int          PRIVILEGEABLE          = 0x0002;                  // bits 0010

    static final int          OWNEABLE_PRIVILEGEABLE = OWNEABLE | PRIVILEGEABLE; // bits 0011

    /**
     * Mixin types
     */
    final List<InternalQName> mixinTypes;

    final boolean             owneable;

    final boolean             privilegeable;

    final String              parentId               = null;

    MixinInfo(List<InternalQName> mixinTypes, boolean owneable, boolean privilegeable) {
      this.mixinTypes = mixinTypes;
      this.owneable = owneable;
      this.privilegeable = privilegeable;
    }

    InternalQName[] mixinNames() {
      if (mixinTypes != null) {
        InternalQName[] mns = new InternalQName[mixinTypes.size()];
        mixinTypes.toArray(mns);
        return mns;
      } else
        return new InternalQName[0];
    }

    boolean hasPrivilegeable() {
      return privilegeable;
    }

    boolean hasOwneable() {
      return owneable;
    }
  }

  protected MixinInfo readMixins(String cid) throws SQLException, IllegalNameException {
    ResultSet mtrs = findPropertyByName(cid, Constants.JCR_MIXINTYPES.getAsString());

    try {
      List<InternalQName> mts = null;
      boolean owneable = false;
      boolean privilegeable = false;
      if (mtrs.next()) {
        mts = new ArrayList<InternalQName>();
        do {
          byte[] mxnb = mtrs.getBytes(COLUMN_VDATA);
          if (mxnb != null) {
            InternalQName mxn = InternalQName.parse(new String(mxnb));
            mts.add(mxn);

            if (!privilegeable && Constants.EXO_PRIVILEGEABLE.equals(mxn))
              privilegeable = true;
            else if (!owneable && Constants.EXO_OWNEABLE.equals(mxn))
              owneable = true;
          } // else, if SQL NULL - skip it
        } while (mtrs.next());
      }

      return new MixinInfo(mts, owneable, privilegeable);
    } finally {
      mtrs.close();
    }
  }

  /**
   * Return permission values or throw an exception. We assume the node is mix:privilegeable.
   * 
   * @param cid
   * @return
   * @throws SQLException
   * @throws IllegalACLException
   */
  protected List<AccessControlEntry> readACLPermisions(String cid) throws SQLException, IllegalACLException {
    List<AccessControlEntry> naPermissions = new ArrayList<AccessControlEntry>();
    ResultSet exoPerm = findPropertyByName(cid, Constants.EXO_PERMISSIONS.getAsString());
    try {
      if (exoPerm.next()) {
        do {
          StringTokenizer parser = new StringTokenizer(new String(exoPerm.getBytes(COLUMN_VDATA)), AccessControlEntry.DELIMITER);
          naPermissions.add(new AccessControlEntry(parser.nextToken(), parser.nextToken()));
        } while (exoPerm.next());

        return naPermissions;
      } else
        throw new IllegalACLException("Property exo:permissions is not found for node with id: " + getIdentifier(cid));
    } finally {
      exoPerm.close();
    }
  }

  /**
   * Return owner value or throw an exception. We assume the node is mix:owneable.
   * 
   * @param cid
   * @return
   * @throws SQLException
   * @throws IllegalACLException
   */
  protected String readACLOwner(String cid) throws SQLException, IllegalACLException {
    ResultSet exoOwner = findPropertyByName(cid, Constants.EXO_OWNER.getAsString());
    try {
      if (exoOwner.next())
        return new String(exoOwner.getBytes(COLUMN_VDATA));
      else
        throw new IllegalACLException("Property exo:owner is not found for node with id: " + getIdentifier(cid));
    } finally {
      exoOwner.close();
    }
  }

  protected PersistedNodeData loadNodeRecord(QPath parentPath,
                                             String cname,
                                             String cid,
                                             String cpid,
                                             int cindex,
                                             int cversion,
                                             int cnordernumb,
                                             AccessControlList parentACL) throws RepositoryException, SQLException {

    try {
      InternalQName qname = InternalQName.parse(cname);

      QPath qpath;
      String parentCid;
      if (parentPath != null) {
        // get by parent and name
        qpath = QPath.makeChildPath(parentPath, qname, cindex);
        parentCid = cpid;
      } else {
        // get by id
        if (cpid.equals(Constants.ROOT_PARENT_UUID)) {
          // root node
          qpath = Constants.ROOT_PATH;
          parentCid = null;
        } else {
          qpath = QPath.makeChildPath(traverseQPath(cpid), qname, cindex);
          parentCid = cpid;
        }
      }

      try {
        // PRIMARY
        ResultSet ptProp = findPropertyByName(cid, Constants.JCR_PRIMARYTYPE.getAsString());

        if (!ptProp.next())
          throw new PrimaryTypeNotFoundException("FATAL ERROR primary type record not found. Node " + qpath.getAsString()
              + ", id " + cid + ", container " + this.containerName, null);

        byte[] data = ptProp.getBytes(COLUMN_VDATA);
        InternalQName ptName = InternalQName.parse(new String((data != null ? data : new byte[] {})));

        // MIXIN
        MixinInfo mixins = readMixins(cid);

        // ACL
        AccessControlList acl; // NO DEFAULT values!

        if (mixins.hasOwneable()) {
          // has own owner
          if (mixins.hasPrivilegeable()) {
            // and permissions
            acl = new AccessControlList(readACLOwner(cid), readACLPermisions(cid));
          } else if (parentACL != null) {
            // use permissions from existed parent
            acl = new AccessControlList(readACLOwner(cid), parentACL.hasPermissions() ? parentACL.getPermissionEntries() : null);
          } else {
            // have to search nearest ancestor permissions in ACL manager
            // acl = new AccessControlList(readACLOwner(cid), traverseACLPermissions(cpid));
            acl = new AccessControlList(readACLOwner(cid), null);
          }
        } else if (mixins.hasPrivilegeable()) {
          // has own permissions
          if (mixins.hasOwneable()) {
            // and owner
            acl = new AccessControlList(readACLOwner(cid), readACLPermisions(cid));
          } else if (parentACL != null) {
            // use owner from existed parent
            acl = new AccessControlList(parentACL.getOwner(), readACLPermisions(cid));
          } else {
            // have to search nearest ancestor owner in ACL manager
            // acl = new AccessControlList(traverseACLOwner(cpid), readACLPermisions(cid));
            acl = new AccessControlList(null, readACLPermisions(cid));
          }
        } else {
          if (parentACL != null)
            // construct ACL from existed parent ACL
            acl = new AccessControlList(parentACL.getOwner(), parentACL.hasPermissions() ? parentACL.getPermissionEntries() : null);
          else
            // have to search nearest ancestor owner and permissions in ACL manager
            // acl = traverseACL(cpid);
            acl = null;
        }

        return new PersistedNodeData(getIdentifier(cid),
                                     qpath,
                                     getIdentifier(parentCid),
                                     cversion,
                                     cnordernumb,
                                     ptName,
                                     mixins.mixinNames(),
                                     acl);
      } catch (IllegalACLException e) {
        throw new RepositoryException("FATAL ERROR Node " + getIdentifier(cid) + " " + qpath.getAsString()
            + " has wrong formed ACL. ", e);
      }
    } catch (IllegalNameException e) {
      throw new RepositoryException(e);
    }
  }

  protected PersistedPropertyData loadPropertyRecord(QPath parentPath,
                                                     String cname,
                                                     String cid,
                                                     String cpid,
                                                     int cversion,
                                                     int cptype,
                                                     boolean cpmultivalued) throws RepositoryException, SQLException, IOException {

    // NOTE: cpid never should be null or root parent (' ')

    try {
      QPath qpath = QPath.makeChildPath(parentPath == null ? traverseQPath(cpid) : parentPath, InternalQName.parse(cname));

      PersistedPropertyData pdata =
          new PersistedPropertyData(getIdentifier(cid), qpath, getIdentifier(cpid), cversion, cptype, cpmultivalued);

      pdata.setValues(readValues(cid, pdata));
      return pdata;
    } catch (IllegalNameException e) {
      throw new RepositoryException(e);
    }
  }

  private void deleteExternalValues(String cid, PropertyData pdata) throws IOException, ValueDataNotFoundException {

    try {

      final ResultSet valueRecords = findValuesByPropertyId(cid);
      try {
        // [PN] 12.07.07 if (... instead while (...
        // so, we don't need iterate throught each value of the property
        // IO channel will do this work according the existed files on FS
        if (valueRecords.next()) {
          final String storageId = valueRecords.getString(COLUMN_VSTORAGE_DESC);
          if (!valueRecords.wasNull()) {
            final ValueIOChannel channel = valueStorageProvider.getChannel(storageId, pdata);
            try {
              channel.delete(pdata.getIdentifier());
            } finally {
              channel.close();
            }
          }
        }
      } finally {
        valueRecords.close();
      }

    } catch (SQLException e) {
      String msg = "Can't read value data of property with id " + cid + ", error:" + e;
      log.error(msg, e);
      throw new IOException(msg);
    }
  }

  private List<ValueData> readValues(String cid, PropertyData pdata) throws IOException, ValueDataNotFoundException {

    List<ValueData> data = new ArrayList<ValueData>();

    try {

      final ResultSet valueRecords = findValuesByPropertyId(cid);
      try {
        while (valueRecords.next()) {
          final int orderNum = valueRecords.getInt(COLUMN_VORDERNUM);
          final String storageId = valueRecords.getString(COLUMN_VSTORAGE_DESC);
          ValueData vdata =
              valueRecords.wasNull() ? readValueData(cid, orderNum, pdata.getPersistedVersion()) : readValueData(pdata,
                                                                                                                 orderNum,
                                                                                                                 storageId);
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

  protected ValueData readValueData(PropertyData pdata, int orderNumber, String storageId) throws SQLException,
                                                                                          IOException,
                                                                                          ValueDataNotFoundException {
    ValueIOChannel channel = valueStorageProvider.getChannel(storageId, pdata);
    try {
      return channel.read(pdata.getIdentifier(), orderNumber, maxBufferSize);
    } finally {
      channel.close();
    }
  }

  protected ValueData readValueData(String cid, int orderNumber, int version) throws SQLException, IOException {

    ResultSet valueResultSet = null;

    byte[] buffer = new byte[0];
    byte[] spoolBuffer = new byte[2048];
    int read;
    int len = 0;
    OutputStream out = null;

    SwapFile swapFile = null;
    try {
      // stream from database
      valueResultSet = findValueByPropertyIdOrderNumber(cid, orderNumber);
      if (valueResultSet.next()) {
        final InputStream in = valueResultSet.getBinaryStream(COLUMN_VDATA);
        if (in != null)
          while ((read = in.read(spoolBuffer)) >= 0) {
            if (out != null) {
              // spool to temp file
              out.write(spoolBuffer, 0, read);
              len += read;
            } else if (len + read > maxBufferSize) {
              // threshold for keeping data in memory exceeded;
              // create temp file and spool buffer contents
              swapFile = SwapFile.get(swapDirectory, cid + orderNumber + "." + version);
              if (swapFile.isSpooled()) {
                // break, value already spooled
                buffer = null;
                break;
              }
              out = new FileOutputStream(swapFile);
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
        swapFile.spoolDone();
      }
    }

    if (buffer == null)
      return new CleanableFileStreamValueData(swapFile, orderNumber, swapCleaner);

    return new ByteArrayPersistedValueData(buffer, orderNumber);
  }

  protected void addValues(PropertyData data) throws IOException, SQLException {
    List<ValueData> vdata = data.getValues();

    for (int i = 0; i < vdata.size(); i++) {
      ValueData vd = vdata.get(i);
      vd.setOrderNumber(i);
      ValueIOChannel channel = valueStorageProvider.getApplicableChannel(data, i);
      InputStream stream = null;
      int streamLength = 0;
      String storageId = null;
      if (channel == null) {
        if (vd.isByteArray()) {
          byte[] dataBytes = vd.getAsByteArray();
          stream = new ByteArrayInputStream(dataBytes);
          streamLength = dataBytes.length;
        } else {
          stream = vd.getAsStream();
          streamLength = stream.available();
        }
      } else {
        channel.write(data.getIdentifier(), vd);
        storageId = channel.getStorageId();
      }
      addValueData(getInternalId(data.getIdentifier()), i, stream, streamLength, storageId);
    }
  }

  // ---- Data access methods (query wrappers) to override in concrete
  // connection ------

  protected abstract int addNodeRecord(NodeData data) throws SQLException;

  protected abstract int addPropertyRecord(PropertyData prop) throws SQLException;

  protected abstract ResultSet findItemByIdentifier(String identifier) throws SQLException;

  protected abstract ResultSet findPropertyByName(String parentId, String name) throws SQLException;

  protected abstract ResultSet findItemByName(String parentId, String name, int index) throws SQLException;

  protected abstract ResultSet findChildNodesByParentIdentifier(String parentIdentifier) throws SQLException;

  protected abstract ResultSet findChildPropertiesByParentIdentifier(String parentIdentifier) throws SQLException;

  protected abstract int addReference(PropertyData data) throws SQLException, IOException;

  protected abstract int renameNode(NodeData data) throws SQLException, IOException;

  protected abstract int deleteReference(String propertyIdentifier) throws SQLException;

  protected abstract ResultSet findReferences(String nodeIdentifier) throws SQLException;

  protected abstract int deleteItemByIdentifier(String identifier) throws SQLException;

  protected abstract int updateNodeByIdentifier(int version, int index, int orderNumb, String identifier) throws SQLException;

  protected abstract int updatePropertyByIdentifier(int version, int type, String identifier) throws SQLException;

  // -------- values processing ------------
  protected abstract int addValueData(String cid, int orderNumber, InputStream stream, int streamLength, String storageId) throws SQLException,
                                                                                                                           IOException;

  protected abstract int deleteValues(String cid) throws SQLException;

  protected abstract ResultSet findValuesByPropertyId(String cid) throws SQLException;

  protected abstract ResultSet findValueByPropertyIdOrderNumber(String cid, int orderNumb) throws SQLException;
}
