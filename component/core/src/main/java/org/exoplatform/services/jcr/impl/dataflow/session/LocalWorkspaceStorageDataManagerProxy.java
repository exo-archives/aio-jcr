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
package org.exoplatform.services.jcr.impl.dataflow.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientItemData;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.LocalWorkspaceDataManagerStub;
/**
 * Created by The eXo Platform SAS.<br/>
 * proxy of local workspace storage. "local" means that backended workspace data manager 
 * is located on the same JVM as session layer. WorkspaceStorageDataManagerProxy can 
 * be pluggable in a case of other storage-session transport applied (for ex RMI)
 * this implementation is responsible for making copy of persisted (shared) data objects
 * for session data manager and pass it on top (to TransactionableDM) (and vice versa?) 
 *     
 * @author Gennady Azarenkov
 * @version $Id: LocalWorkspaceStorageDataManagerProxy.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class LocalWorkspaceStorageDataManagerProxy implements WorkspaceStorageDataManagerProxy {

  protected final LocalWorkspaceDataManagerStub storageDataManager;
  protected final ValueFactoryImpl valueFactory;
  
  
  public LocalWorkspaceStorageDataManagerProxy(LocalWorkspaceDataManagerStub storageDataManager, ValueFactoryImpl valueFactory) {
    this.storageDataManager = storageDataManager;
    this.valueFactory = valueFactory;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#save(org.exoplatform.services.jcr.impl.dataflow.ItemDataChangesLog)
   */
  public void save(ItemStateChangesLog changesLog)
      throws InvalidItemStateException, UnsupportedOperationException,
      RepositoryException {
    
    ChangesLogIterator logIterator = ((CompositeChangesLog)changesLog).getLogIterator();
    TransactionChangesLog newLog = new TransactionChangesLog();
    
    while (logIterator.hasNextLog()) {
      List<ItemState> states = new ArrayList<ItemState>(changesLog.getSize());
      PlainChangesLog changes = logIterator.nextLog();
      for (ItemState change : changes.getAllStates()) {
        states.add(new ItemState(copyItemData(change.getData()),
                                 change.getState(),
                                 change.isEventFire(),
                                 change.getAncestorToSave(),
                                 change.isInternallyCreated(),
                                 change.isPersisted()));
      }
      
      newLog.addLog(new PlainChangesLogImpl(states, changes.getSessionId(),changes.getEventType()));
    }
    
    storageDataManager.save(newLog);
  }
  
  public ItemData getItemData(NodeData parentData, QPathEntry name) throws RepositoryException {
    return copyItemData(storageDataManager.getItemData(parentData,name));
  }
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#getItemData(java.lang.String)
   */
  public ItemData getItemData(String identifier) throws RepositoryException {
    return copyItemData(storageDataManager.getItemData(identifier));
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#getChildNodesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<NodeData> getChildNodesData(NodeData parent)
      throws RepositoryException {
    return copyNodes(storageDataManager.getChildNodesData(parent));
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#getChildPropertiesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<PropertyData> getChildPropertiesData(NodeData parent)
      throws RepositoryException {
    return copyProperties(storageDataManager.getChildPropertiesData(parent));
  }
  
  public List<PropertyData> listChildPropertiesData(NodeData parent) throws RepositoryException {
    return copyPropertiesWithoutValues(storageDataManager.listChildPropertiesData(parent));
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#getReferencesData(java.lang.String)
   */
  public List<PropertyData> getReferencesData(String identifier, boolean skipVersionStorage)
      throws RepositoryException {
    return copyProperties(storageDataManager.getReferencesData(identifier, skipVersionStorage));
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#getCurrentTime()
   */
  public Calendar getCurrentTime() {
    return storageDataManager.getCurrentTime();
  }
  
  private TransientItemData copyItemData(final ItemData item) throws RepositoryException {
    
    if(item == null)
      return null;

    // make a copy
    if (item.isNode()) {

      final NodeData node = (NodeData)item;
      
      // the node ACL can't be are null as ACL manager does care about this
      final AccessControlList acl = node.getACL();
      if(acl == null) {
        throw new RepositoryException("Node ACL is null. " + node.getQPath().getAsString() + " " + node.getIdentifier());
      }
      return new TransientNodeData(node.getQPath(), node.getIdentifier(), 
        node.getPersistedVersion(), node.getPrimaryTypeName(), node.getMixinTypeNames(),
        node.getOrderNumber(), node.getParentIdentifier(), acl);
    }

    // else - property
    final PropertyData prop = (PropertyData)item;
    // make a copy
    TransientPropertyData newData = new TransientPropertyData(
        prop.getQPath(), prop.getIdentifier(), prop.getPersistedVersion(),
        prop.getType(), prop.getParentIdentifier(), prop.isMultiValued());
    
    List <ValueData> values = null;
    // null is possible for deleting items
    if(prop.getValues() != null) {
      values = new ArrayList<ValueData>();
      for (ValueData val : prop.getValues()) {
        values.add(((AbstractValueData) val).createTransientCopy());
      }    
    }
    newData.setValues(values);
    return newData;
  }
  
  private TransientItemData copyPropertyDataWithoutValue(PropertyData property) throws RepositoryException {
    
    if(property == null)
      return null;

    // make a copy
    TransientPropertyData newData = new TransientPropertyData(
        property.getQPath(), property.getIdentifier(), property.getPersistedVersion(),
        property.getType(), property.getParentIdentifier(), property.isMultiValued());
    
    newData.setValues(new ArrayList<ValueData>());
    return newData;
  }
  
  private List<NodeData> copyNodes(final List<NodeData> childNodes) throws RepositoryException {
    final List<NodeData> copyOfChildsNodes = new LinkedList<NodeData>();
    synchronized (childNodes) {
      for (NodeData nodeData: childNodes) {
        copyOfChildsNodes.add((NodeData) copyItemData(nodeData));
      }
    }
    return copyOfChildsNodes;
  }

  private List<PropertyData> copyProperties(final List<PropertyData> traverseProperties) throws RepositoryException {
    final List<PropertyData> copyOfChildsProperties = new LinkedList<PropertyData>();
    synchronized (traverseProperties) {
      for (PropertyData nodeProperty: traverseProperties) {
        copyOfChildsProperties.add((PropertyData) copyItemData(nodeProperty));
      }
    }
    return copyOfChildsProperties;
  }
  
  private List<PropertyData> copyPropertiesWithoutValues(final List<PropertyData> traverseProperties) throws RepositoryException {
    final List<PropertyData> copyOfChildsProperties = new LinkedList<PropertyData>();
    synchronized (traverseProperties) {
      for (PropertyData nodeProperty: traverseProperties) {
        copyOfChildsProperties.add((PropertyData) copyPropertyDataWithoutValue(nodeProperty));
      }
    }
    return copyOfChildsProperties;
  }
}
