/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc;

import java.io.ByteArrayInputStream;
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
import org.exoplatform.services.jcr.impl.storage.value.ValueDataNotFoundException;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SwapFile;
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

  public static final int I_CLASS_NODE = 1;
  public static final int I_CLASS_PROPERTY = 2;
  
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
   * Return JDBC connection obtained from initialized data source.
   * NOTE: Helper can obtain one new connection per each call of the method  or return one obtained once.
   */
  public Connection getJdbcConnection() {
    return dbConnection;
  }

  /**
   * Prepared queries at start time
   * @throws SQLException
   */
  abstract protected void prepareQueries() throws SQLException;


  /**
   * Used in Single Db Connection classes for Identifier related queries
   * @param identifier
   * @return
   */
  protected abstract String getInternalId(String identifier);

  /**
   * Used in loadXYZRecord methods for extract real Identifier from container value.
   * @param internalId
   * @return
   */
  protected abstract String getIdentifier(String internalId);

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
        log.debug("Node added " + data.getQPath().getAsString() + ", " + data.getIdentifier() + ", " + data.getPrimaryTypeName().getAsString());

    } catch (SQLException e) {
      if (log.isDebugEnabled())
        log.error("Node add. Database error: " + e);
      
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
          throw new RepositoryException("Can't read REFERENCE property ("+data.getQPath()+" "+data.getIdentifier()+") value: " + e.getMessage(), e);
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
      renameNode(data);
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
  public void delete(NodeData data) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException {
    checkIfOpened();

    final String cid = getInternalId(data.getIdentifier());

    try {
      int nc = deleteItemByIdentifier(cid);
      if (nc <= 0)
        throw new InvalidItemStateException("(delete) Node "
            + data.getQPath().getAsString() + " " + data.getIdentifier()
            + " not found. Probably was deleted by another session ");

      if (log.isDebugEnabled())
        log.debug("Node deleted " + data.getQPath().getAsString() + ", " + data.getIdentifier() + ", " + ((NodeData) data).getPrimaryTypeName().getAsString());

    } catch (SQLException e) {
      if (log.isDebugEnabled())
        log.error("Node remove. Database error: " + e, e);
      exceptionHandler.handleDeleteException(e, data);
    }    
  }
  
  public void delete(PropertyData data) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException {
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
        throw new InvalidItemStateException("(delete) Property "
            + data.getQPath().getAsString() + " " + data.getIdentifier()
            + " not found. Probably was deleted by another session ");

      if (log.isDebugEnabled())
        log.debug("Property deleted " + data.getQPath().getAsString() + ", " + data.getIdentifier()
            + (((PropertyData) data).getValues() != null ? ", values count: " + ((PropertyData) data).getValues().size() : ", NULL data"));      
    
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

  public void reindex(NodeData oldData, NodeData data) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException {
    // TODO remove it
    log.warn("Nodes reordering is not supported currently");
    return;    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#update(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public void update(NodeData data) throws RepositoryException, UnsupportedOperationException, InvalidItemStateException, IllegalStateException {
    checkIfOpened();
    try {
      String cid = getInternalId(data.getIdentifier());
      // order numb update
      updateNodeByIdentifier(data.getPersistedVersion(), data.getQPath().getIndex(), data.getOrderNumber(), cid);

      if (log.isDebugEnabled())
        log.debug("Node updated " + data.getQPath().getAsString() + ", " + data.getIdentifier() + ", " + data.getPrimaryTypeName().getAsString());

    } catch (SQLException e) {
      if (log.isDebugEnabled())
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
      String cid = getInternalId(data.getIdentifier());

      // update type
      updatePropertyByIdentifier(data.getPersistedVersion(), data.getType(), cid);

      // update reference
      try {
        deleteReference(cid);

        if (data.getType() == PropertyType.REFERENCE) {
          addReference(data);
        }
      } catch(IOException e) {
        throw new RepositoryException("Can't update REFERENCE property ("+data.getQPath()+" "+data.getIdentifier()+") value: " + e.getMessage(), e);
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

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getChildNodesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet node = findChildNodesByParentIdentifier(getInternalId(parent.getIdentifier()));
      List<NodeData> childrens = new ArrayList<NodeData>();
      while(node.next()) {
        childrens.add((NodeData) itemData(parent.getQPath(), node, I_CLASS_NODE));        
      }
      return childrens;
    } catch (SQLException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getChildPropertiesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet prop = findChildPropertiesByParentIdentifier(getInternalId(parent.getIdentifier()));
      List<PropertyData> children = new ArrayList<PropertyData>();
      while(prop.next()) {
        children.add((PropertyData) itemData(parent.getQPath(), prop, I_CLASS_PROPERTY));
      }
      return children;
    } catch (SQLException e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getItemData(java.lang.String)
   */
  public ItemData getItemData(String identifier) throws RepositoryException, IllegalStateException {
    return getItemByIdentifier(getInternalId(identifier));
  }
  
  public ItemData getItemData(NodeData parentData, QPathEntry name) throws RepositoryException,
      IllegalStateException {
    
    if (parentData != null) {
      return getItemByName(parentData.getQPath(), getInternalId(parentData.getIdentifier()), name);
    } 

    // it's a root node
    return getItemByName(null, null, name);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.storage.WorkspaceStorageConnection#getReferencesData(java.lang.String)
   */
  public List<PropertyData> getReferencesData(String nodeIdentifier) throws RepositoryException, IllegalStateException {
    checkIfOpened();
    try {
      ResultSet refProps = findReferences(getInternalId(nodeIdentifier));
      List<PropertyData> references = new ArrayList<PropertyData>();
      while(refProps.next()) {
        references.add((PropertyData) itemData(null, refProps, I_CLASS_PROPERTY));
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
        if(item.next()) {
          return itemData(null, item, item.getInt(COLUMN_CLASS));
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
     * Gets an item data from database.
     * 
     * @param parentPath - parent QPath 
     * @param parentId - parent container internal id (depends on Multi/Single DB)
     * @param name - item name
     * @return - ItemData instance
     * 
     * @throws RepositoryException
     * @throws IllegalStateException
   */
    protected ItemData getItemByName(QPath parentPath, String parentId, QPathEntry name) throws RepositoryException,
        IllegalStateException {
      checkIfOpened();
      ResultSet item = null;
      try {
        item = findItemByName(parentId,
            name.getAsString(),
            name.getIndex());
        if (item.next())
          return itemData(parentPath, item, item.getInt(COLUMN_CLASS));
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
  
    private QPath traverseQPath(String cpid) throws SQLException, InvalidItemStateException, IllegalNameException {
      // get item by Identifier usecase:
      // find parent path in db by cpid
      if (cpid == null) {
        // root node
        return null; // Constants.ROOT_PATH
      } else {
        List<QPathEntry> qrpath = new ArrayList<QPathEntry>(); // reverted path
        String caid = cpid; // container ancestor id
        do { 
          ResultSet parent = null;
          try {
            parent = findItemByIdentifier(caid);
            if (!parent.next())
              throw new InvalidItemStateException("Parent not found, uuid: " + getIdentifier(caid));
            
            QPathEntry qpe = new QPathEntry(
                InternalQName.parse(parent.getString(COLUMN_NAME)), 
                parent.getInt(COLUMN_INDEX));
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
    }
    
    /**
     * @param parentPath
     * @param item
     * @return item data
     * 
     * @throws RepositoryException
     * @throws SQLException
     */
    private ItemData itemData(QPath parentPath, ResultSet item, int itemClass) throws RepositoryException, SQLException, IOException {
      String cid = item.getString(COLUMN_ID);
      String cname = item.getString(COLUMN_NAME);
      int cversion = item.getInt(COLUMN_VERSION);
      
      String cpid = item.getString(COLUMN_PARENTID);
      // if parent ID is empty string - it's a root node  
      cpid = cpid.equals(Constants.ROOT_PARENT_UUID) ? null : cpid;
      
      try {
        if (itemClass == I_CLASS_NODE) {
          int cindex = item.getInt(COLUMN_INDEX);
          int cnordernumb = item.getInt(COLUMN_NORDERNUM);
          // QPath parentPath, String cname, String cid, String cpid, int cindex, int cversion, int cnordernumb
          return loadNodeRecord(parentPath == null ? traverseQPath(cpid) : parentPath, 
              cname, cid, cpid, cindex, cversion, cnordernumb);
        }
        
        int cptype = item.getInt(COLUMN_PTYPE);
        boolean cpmultivalued = item.getBoolean(COLUMN_PMULTIVALUED);
        return loadPropertyRecord(parentPath == null ? traverseQPath(cpid) : parentPath, 
            cname, cid, cpid, cversion, cptype, cpmultivalued);
      } catch (InvalidItemStateException e) {
        throw new InvalidItemStateException("FATAL: Can't build item path for name " + cname 
            + " id: " + getIdentifier(cid) + ". " + e);
      } catch (IllegalNameException e) {
        throw new RepositoryException(e);
      }
    }

    protected PersistedNodeData loadNodeRecord(QPath parentPath, String cname, String cid, String cpid, int cindex, int cversion, int cnordernumb) throws RepositoryException, SQLException {

      try {
        InternalQName qname = InternalQName.parse(cname);
        QPath qpath = parentPath != null ? QPath.makeChildPath(parentPath, qname, cindex) : Constants.ROOT_PATH;
        
        // PRIMARY
        ResultSet ptProp = findPropertyByName(cid, Constants.JCR_PRIMARYTYPE.getAsString());

        if (!ptProp.next())
          throw new PrimaryTypeNotFoundException("FATAL ERROR primary type record not found. Node "
              + qpath.getAsString() + ", id " + cid + ", container " + this.containerName, 
              null);

        ResultSet ptValue = findValuesDataByPropertyId(ptProp.getString(COLUMN_ID));

        if (!ptValue.next())
          throw new RepositoryException("FATAL ERROR primary type value not found. Node "
              + qpath.getAsString() + ", id " + ptProp.getString(COLUMN_ID) + ", container "
              + this.containerName);

        byte[] data = ptValue.getBytes(COLUMN_VDATA);
        InternalQName ptName = InternalQName.parse(new String((data != null ? data : new byte[] {})));

        // MIXIN
        ResultSet mtProp = findPropertyByName(cid, Constants.JCR_MIXINTYPES.getAsString());

        InternalQName[] mixinNames = new InternalQName[0];
        if (mtProp.next()) {
          List<byte[]> mts = new ArrayList<byte[]>();
          ResultSet mtValues = findValuesDataByPropertyId(mtProp.getString(COLUMN_ID));
          while (mtValues.next()) {
            mts.add(mtValues.getBytes(COLUMN_VDATA));
          }
          mixinNames = new InternalQName[mts.size()];
          for (int i = 0; i < mts.size(); i++) {
            mixinNames[i] = InternalQName.parse(new String(mts.get(i)));
          }
        }

        // ACL
        AccessControlList acl;
        if (isAccessControllable(mixinNames)) {

          PropertyData ownerData = (PropertyData) getItemByName(qpath, cid, new QPathEntry(Constants.EXO_OWNER, 1));
          PropertyData permData = (PropertyData) getItemByName(qpath, cid, new QPathEntry(Constants.EXO_PERMISSIONS, 1));
          acl = new AccessControlList(ownerData, permData);
        } else {
          if (parentPath == null) {
            // make default ACL for root
            acl = new AccessControlList();
          } else {
            acl = null;
          }
        }

        return new PersistedNodeData(getIdentifier(cid), qpath, getIdentifier(cpid), cversion, cnordernumb,
            ptName, mixinNames, acl);

      } catch (IllegalNameException e) {
        throw new RepositoryException(e);
      }    
  }
    
  protected PersistedPropertyData loadPropertyRecord(QPath parentPath, String cname, String cid, String cpid, int cversion, int cptype, boolean cpmultivalued) throws RepositoryException, SQLException, IOException {

    // NOTE parentPath - never is null 
    
    List<ValueData> values = new ArrayList<ValueData>();
    
    InternalQName qname;
    try {
      qname = InternalQName.parse(cname);
    } catch (IllegalNameException e) {
      throw new RepositoryException(e);
    }
    QPath qpath = QPath.makeChildPath(parentPath, qname);
    
    String identifier = getIdentifier(cid);
    PersistedPropertyData pdata = new PersistedPropertyData(identifier,
        qpath,
        getIdentifier(cpid),
        cversion,
        cptype,
        cpmultivalued
    );

    pdata.setValues(readValues(cid, pdata));
    return pdata;
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
          ValueData vdata = valueRecords.wasNull() ? 
              readValueData(cid, orderNum, pdata.getPersistedVersion()) : 
                readValueData(pdata, orderNum, storageId);
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

  protected ValueData readValueData(PropertyData pdata, int orderNumber, String storageId) throws SQLException, IOException, ValueDataNotFoundException {
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
              swapFile = SwapFile.get(swapDirectory, cid+orderNumber+"."+version);
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

    if(buffer == null)
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
      addValueData(getInternalId(data.getIdentifier()),i, stream, streamLength, storageId); 
    }
  }
  // ---- Data access methods (query wrappers) to override in concrete connection ------

  protected abstract void addNodeRecord(NodeData data) throws SQLException;
  protected abstract void addPropertyRecord(PropertyData prop) throws SQLException;

  protected abstract ResultSet findItemByIdentifier(String identifier) throws SQLException;
  protected abstract ResultSet findPropertyByName(String parentId, String name) throws SQLException;
  protected abstract ResultSet findItemByName(String parentId, String name, int index) throws SQLException;

  protected abstract ResultSet findChildNodesByParentIdentifier(String parentIdentifier) throws SQLException;
  protected abstract ResultSet findChildPropertiesByParentIdentifier(String parentIdentifier) throws SQLException;

  protected abstract void addReference(PropertyData data) throws SQLException, IOException;
  protected abstract void renameNode(NodeData data) throws SQLException,
      IOException;
  protected abstract void deleteReference(String propertyIdentifier) throws SQLException;
  protected abstract ResultSet findReferences(String nodeIdentifier) throws SQLException;

  protected abstract int deleteItemByIdentifier(String identifier) throws SQLException;
  
  protected abstract int updateNodeByIdentifier(int version, int index, int orderNumb, String identifier) throws SQLException;
  protected abstract int updatePropertyByIdentifier(int version, int type, String identifier) throws SQLException;
  
  // -------- values processing ------------
  protected abstract void addValueData(String cid, int orderNumber, InputStream stream, int streamLength, String storageId) throws SQLException, IOException;
  protected abstract void deleteValues(String cid) throws SQLException;
  protected abstract ResultSet findValuesByPropertyId(String cid) throws SQLException;
  protected abstract ResultSet findValuesDataByPropertyId(String cid) throws SQLException;
  protected abstract ResultSet findValueByPropertyIdOrderNumber(String cid, int orderNumb) throws SQLException;
}
