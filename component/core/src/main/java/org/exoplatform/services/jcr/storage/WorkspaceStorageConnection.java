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
package org.exoplatform.services.jcr.storage;

import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS. <br>
 * 
 * Includes methods for direct read (by identifier and qPath), node child traversing, reference
 * access as well as write single item methods (add, update, delete) and batch operations support.
 * Valid (workable) connection state is "opened" (isOpened() == true). Newly created connection
 * should have "opened" state. The connection becomes "closed" (invalid for using) after calling
 * commit() or rollback() methods. In this case methods calling will cause an IllegalStateException
 * 
 * Connection object intendend to be as "light" as possible i.e. connection creation SHOULD NOT be
 * expensive operation, so better NOT to open/close potentially EXPENSIVE resources using by
 * Connection (WorkspaceDataContainer should be responsible for that). The Connection IS NOT a
 * thread-safe object and normally SHOULD NOT be pooled/cached.
 * 
 * @author Gennady Azarenkov
 * @version $Id: WorkspaceStorageConnection.java 11907 2008-03-13 15:36:21Z ksm $
 */

public interface WorkspaceStorageConnection {
  /**
   * @param parentData
   *          - the item's parent node data
   * @param name
   *          - item's path entry (qname + index)
   * @return - stored ItemData wich has exact the same path Entry (name+index) inside the parent; or
   *         null if not such an item data found
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   */
  ItemData getItemData(NodeData parentData, QPathEntry name) throws RepositoryException,
                                                            IllegalStateException;

  /**
   * @param identifier
   *          - unique identifier
   * @return corresponding stored ItemData or null. Basically used for Session.getNodeByUUID but not
   *         necessarily refers to jcr:uuid property (In fact, this identifier should not necessary
   *         be equal of referenceable node's UUID if any) thereby can return NodeData for not
   *         referenceable node data or PropertyData.
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   */
  ItemData getItemData(String identifier) throws RepositoryException, IllegalStateException;

  /**
   * @param parent
   *          node data
   * @return child nodes data or empty list
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   */
  List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException,
                                                   IllegalStateException;

  /**
   * @param parent
   *          node data
   * @return child properties data or empty list
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   */
  List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException,
                                                            IllegalStateException;

  List<PropertyData> listChildPropertiesData(NodeData parent) throws RepositoryException,
                                                             IllegalStateException;

  /**
   * @param Identifier
   *          of referenceable node
   * @return list of referenced property data or empty list
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   * @throws UnsupportedOperationException
   *           if operation is not supported
   */
  List<PropertyData> getReferencesData(String nodeIdentifier) throws RepositoryException,
                                                             IllegalStateException,
                                                             UnsupportedOperationException;

  /**
   * Adds single NodeData.
   * 
   * @param data
   *          - the new data
   * @throws InvalidItemStateException
   *           if the item already exists
   * @throws UnsupportedOperationException
   *           if operation is not supported (it is container for level 1)
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   */
  void add(NodeData data) throws RepositoryException,
                         UnsupportedOperationException,
                         InvalidItemStateException,
                         IllegalStateException;

  /**
   * Adds single property data
   * 
   * @param data
   *          - the new data
   * @throws InvalidItemStateException
   *           if the item already exists
   * @throws UnsupportedOperationException
   *           if operation is not supported (it is container for level 1)
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   */
  void add(PropertyData data) throws RepositoryException,
                             UnsupportedOperationException,
                             InvalidItemStateException,
                             IllegalStateException;

  /**
   * Updates NodeData.
   * 
   * @param data
   *          - the new data
   * @throws InvalidItemStateException
   *           (1)if the data is already updated, i.e. persisted version value of persisted data >=
   *           of new data's persisted version value (2) if the persisted data is not NodeData (i.e.
   *           it is PropertyData). It means that some other proccess deleted original data and
   *           replace it with other type of data.
   * @throws UnsupportedOperationException
   *           if operation is not supported (it is container for level 1)
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   */
  void update(NodeData data) throws RepositoryException,
                            UnsupportedOperationException,
                            InvalidItemStateException,
                            IllegalStateException;

  /**
   * Updates PropertyData.
   * 
   * @param data
   *          - the new data
   * @throws InvalidItemStateException
   *           (1)if the data is already updated, i.e. persisted version value of persisted data >=
   *           of new data's persisted version value (2) if the persisted data is not PropertyData
   *           (i.e. it is NodeData). It means that some other proccess deleted original data and
   *           replace it with other type of data.
   * @throws UnsupportedOperationException
   *           if operation is not supported (it is container for level 1)
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   */
  void update(PropertyData data) throws RepositoryException,
                                UnsupportedOperationException,
                                InvalidItemStateException,
                                IllegalStateException;

  void rename(NodeData data) throws RepositoryException,
                            UnsupportedOperationException,
                            InvalidItemStateException,
                            IllegalStateException;

  /**
   * Deletes node or property data.
   * 
   * @param data
   *          that identifies data to be deleted
   * 
   * @throws InvalidItemStateException
   *           if the data is already deleted
   * @throws UnsupportedOperationException
   *           if operation is not supported (it is container for level 1)
   * @throws RepositoryException
   *           if some exception occured
   * @throws IllegalStateException
   *           if connection is closed
   */
  void delete(NodeData data) throws RepositoryException,
                            UnsupportedOperationException,
                            InvalidItemStateException,
                            IllegalStateException;

  void delete(PropertyData data) throws RepositoryException,
                                UnsupportedOperationException,
                                InvalidItemStateException,
                                IllegalStateException;

  /**
   * Accepts (in sense of persistent changes) connection job results and closes connection. It can
   * be database transaction commit for instance etc.
   * 
   * @throws IllegalStateException
   *           if connection is already closed
   * @throws RepositoryException
   *           if some exception occured
   */
  void commit() throws IllegalStateException, RepositoryException;

  /**
   * Refuses (in sense of persistent changes) connection job results and closes connection. It can
   * be database transaction rollback for instance etc.
   * 
   * @throws IllegalStateException
   *           if connection is already closed
   * @throws RepositoryException
   *           if some exception occured
   */
  void rollback() throws IllegalStateException, RepositoryException;
  
  /**
   * Close connection.
   * 
   * @throws IllegalStateException
   *           if connection is already closed
   * @throws RepositoryException
   *           if some exception occured
   */
  void close() throws IllegalStateException, RepositoryException;

  /**
   * @return true if connection is opened
   */
  boolean isOpened();
}
