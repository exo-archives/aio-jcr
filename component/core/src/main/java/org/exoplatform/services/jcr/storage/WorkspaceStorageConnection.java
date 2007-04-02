/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.storage;

import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;

/**
 * Created by The eXo Platform SARL        . <br>
 * 
 * Includes methods for direct read (by uuid and qPath), node child traversing,
 * reference access as well as write single item methods (add, update, delete) and batch operations support.
 * Valid (workable) connection state is "opened" (isOpened() == true). Newly created connection should have "opened" state.
 * The connection becomes "closed" (invalid for using) after calling commit() or rollback() methods. In this case methods calling will cause an IllegalStateException  
 * 
 * Connection object intendend to be as "light" as possible i.e. connection creation SHOULD NOT be expensive operation, 
 * so better NOT to open/close potentially EXPENSIVE resources using by Connection (WorkspaceDataContainer should be responsible for that). 
 * The Connection IS NOT a thread-safe object and normally SHOULD NOT be pooled/cached.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: WorkspaceStorageConnection.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface WorkspaceStorageConnection {
  
  /**
   * @param qPath -
   *          item's location path
   * @return - stored PropertyData wich has exact the same path inside location;
   *         or stored NodeData which has exact the same path/index if index is
   *         initialized inside location or index=1 otherwise; null if such a
   *         location not found
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  ItemData getItemData(QPath qPath) throws RepositoryException, IllegalStateException;

  /**
   * @param uuid -
   *          unique identifier
   * @return corresponding stored ItemData or null. Basically used for
   *         Session.getNodeByUUID but not necessarily refers to jcr:uuid
   *         property (In fact, this uuid should not necessary be equal of
   *         referenceable node's UUID if any) thereby can return NodeData for
   *         not referenceable node data or PropertyData.
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  ItemData getItemData(String uuid) throws RepositoryException, IllegalStateException;
  
  
  /**
   * @param parent node data
   * @return child nodes data or empty list
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  List <NodeData> getChildNodesData(NodeData parent) throws RepositoryException, IllegalStateException;
  
  /**
   * @param parent node data
   * @return child properties data or empty list
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  List <PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException, IllegalStateException;

//  /**
//   * @param parentNode
//   * @return size of parent's child nodes in persistent storage. No actual data fetch doing.
//   * @-throws RepositoryException
//   */
//  int getChildNodesCount(NodeData nodeData) throws RepositoryException;
//  
//  /**
//   * @param parentNode
//   * @return size of parent's child properties in persistent storage. No actual data fetch doing.
//   * @-throws RepositoryException
//   */
//  int getChildPropertiesCount(NodeData nodeData) throws RepositoryException;  
  
  /**
   * @param UUID of referenceable node
   * @return list of referenced property data or empty list 
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  List <PropertyData> getReferencesData(String nodeUUID) throws RepositoryException, IllegalStateException;
  
  /**
   * Adds single NodeData. 
   * 
   * @param data - the new data
   * @throws InvalidItemStateException if the item already exists
   * @throws UnsupportedOperationException if operation is not supported (it is container for level 1)
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  void add(NodeData data) throws RepositoryException, UnsupportedOperationException,
      InvalidItemStateException, IllegalStateException;

  /**
   * Adds single property data
   * 
   * @param data - the new data
   * @throws InvalidItemStateException if the item already exists
   * @throws UnsupportedOperationException if operation is not supported (it is container for level 1)
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  void add(PropertyData data) throws  RepositoryException, UnsupportedOperationException, 
       InvalidItemStateException, IllegalStateException;

  /**
   * Updates NodeData. 
   * 
   * @param data - the new data
   * @throws InvalidItemStateException (1)if the data is already updated, i.e. persisted version value
   * of persisted data >= of new data's persisted version value
   * (2) if the persisted data is not NodeData (i.e. it is PropertyData). 
   * It means that some other proccess deleted original data and replace it with other type of data.
   * @throws UnsupportedOperationException if operation is not supported (it is container for level 1)
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  void update(NodeData data) throws RepositoryException, UnsupportedOperationException,
      InvalidItemStateException, IllegalStateException;

  /**
   * Reindex NodeData location and all its descendants. 
   * 
   * @param oldData - the old data
   * @param data - the new data
   * @throws InvalidItemStateException (1)if the data is already updated, i.e. persisted version value
   * of persisted data >= of new data's persisted version value
   * (2) if the persisted data is not NodeData (i.e. it is PropertyData). 
   * It means that some other proccess deleted original data and replace it with other type of data.
   * @throws UnsupportedOperationException if operation is not supported (it is container for level 1)
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  void reindex(NodeData oldData, NodeData data) throws RepositoryException, UnsupportedOperationException,
      InvalidItemStateException, IllegalStateException;
  
  /**
   * Updates PropertyData. 
   * 
   * @param data - the new data
   * @throws InvalidItemStateException (1)if the data is already updated, i.e. persisted version value
   * of persisted data >= of new data's persisted version value
   * (2) if the persisted data is not PropertyData (i.e. it is NodeData). 
   * It means that some other proccess deleted original data and replace it with other type of data.
   * @throws UnsupportedOperationException if operation is not supported (it is container for level 1)
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  void update(PropertyData data) throws RepositoryException, UnsupportedOperationException,
      InvalidItemStateException, IllegalStateException;

  /**
   * Deletes node or property data. 
   * @param data that identifies data to be deleted
   * 
   * @throws InvalidItemStateException if the data is already deleted
   * @throws UnsupportedOperationException if operation is not supported (it is container for level 1)
   * @throws RepositoryException if some exception occured
   * @throws IllegalStateException if connection is closed
   */
  void delete(ItemData data) throws RepositoryException, UnsupportedOperationException,
      InvalidItemStateException, IllegalStateException;


  /**
   * Accepts (in sense of persistent changes) connection job results and closes connection. It can be database transaction commit for instance etc.
   * @throws IllegalStateException if connection is already closed
   * @throws RepositoryException if some exception occured
   */
  void commit() throws IllegalStateException, RepositoryException;

  /**
   * Refuses (in sense of persistent changes) connection job results and closes connection. It can be database transaction rollback for instance etc.
   * @throws IllegalStateException if connection is already closed
   * @throws RepositoryException if some exception occured
   */
  void rollback() throws IllegalStateException, RepositoryException;
  
  
  /**
   * @return true if connection is opened
   */
  boolean isOpened();
}