/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
import org.exoplatform.services.jcr.datamodel.QPath;
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
 * Created by The eXo Platform SARL        .<br/>
 * proxy of local workspace storage. "local" means that backended workspace data manager 
 * is located on the same JVM as session layer. WorkspaceStorageDataManagerProxy can 
 * be pluggable in a case of other storage-session transport applied (for ex RMI)
 * this implementation is responsible for making copy of persisted (shared) data objects
 * for session data manager and pass it on top (to TransactionableDM) (and vice versa?) 
 *     
 * @author Gennady Azarenkov
 * @version $Id$
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
    
    while(logIterator.hasNextLog()) {
      List <ItemState> states = new ArrayList<ItemState>(changesLog.getSize());
      PlainChangesLog changes = logIterator.nextLog();
      for(ItemState change: changes.getAllStates()) {
        states.add(new ItemState(
            copyItemData(change.getData()), change.getState(),
            change.isEventFire(), change.getAncestorToSave(),change.isInternallyCreated()));
      }
      
      newLog.addLog(new PlainChangesLogImpl(states, changes.getSessionId(),changes.getEventType()));
    }
    
    storageDataManager.save(newLog);
    
    //storageDataManager.save(new PlainChangesLogImpl(states, changes.getSessionId()));
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#getItemData(org.exoplatform.services.jcr.datamodel.InternalQPath)
   */
  public ItemData getItemData(QPath path) throws RepositoryException {
    return copyItemData(storageDataManager.getItemData(path));
  }
  public ItemData getItemData(NodeData parentData,QPathEntry name) throws RepositoryException {
    return copyItemData(storageDataManager.getItemData(parentData,name));
  }
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#getItemData(java.lang.String)
   */
  public ItemData getItemData(String uuid) throws RepositoryException {
    return copyItemData(storageDataManager.getItemData(uuid));
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

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#getReferencesData(java.lang.String)
   */
  public List<PropertyData> getReferencesData(String uuid)
      throws RepositoryException {
    return copyProperties(storageDataManager.getReferencesData(uuid));

  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getACL(org.exoplatform.services.jcr.datamodel.InternalQPath)
   */
  @Deprecated
  public AccessControlList getACL(QPath path)
      throws RepositoryException {
    AccessControlList acl = storageDataManager.getACL(path);
    return new AccessControlList(acl.getOwner(), acl.getPermissionEntries());
  }

  public AccessControlList getACL(NodeData parent, QPathEntry name) throws RepositoryException {
    // TODO
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy#getCurrentTime()
   */
  public Calendar getCurrentTime() {
    return storageDataManager.getCurrentTime();
  }

  
  private TransientItemData copyItemData(ItemData item) throws RepositoryException {
    
    if(item == null)
      return null;

    // make a copy
    if (item.isNode()) {

      NodeData node = (NodeData)item;
      
      // it can be null in a case of copying from persisted non AC node
      AccessControlList acl = node.getACL();
      if(acl == null) {
        acl = getACL(node.getQPath());
      }
      return new TransientNodeData(node.getQPath(), node.getUUID(), 
        node.getPersistedVersion(), node.getPrimaryTypeName(), node.getMixinTypeNames(),
        node.getOrderNumber(), node.getParentUUID(), acl);
      
    }

    // else - property
    PropertyData prop = (PropertyData)item;
    // make a copy
    TransientPropertyData newData = new TransientPropertyData(
        prop.getQPath(), prop.getUUID(), prop.getPersistedVersion(),
        prop.getType(), prop.getParentUUID(), prop.isMultiValued());
    
    List <ValueData> values = null;
    // null is possible for deleting items
    if(prop.getValues() != null) {
      values = new ArrayList<ValueData>();
      for (ValueData val : prop.getValues()) {
        values.add(((AbstractValueData)val).createTransientCopy());
      }    
    }
    newData.setValues(values);
    return newData;
  }
  
  private List<NodeData> copyNodes(final List<NodeData> childNodes) throws RepositoryException {
    final List<NodeData> copyOfChildsNodes = new LinkedList<NodeData>();
    synchronized (childNodes) {
      // TODO [PN] There are a problem with concurrent modification if to use iterator
      for (NodeData nodeData: childNodes) {
        copyOfChildsNodes.add((NodeData) copyItemData(nodeData));
      }
    }
    return copyOfChildsNodes;
  }

  private List<PropertyData> copyProperties(final List<PropertyData> traverseProperties) throws RepositoryException {
    final List<PropertyData> copyOfChildsProperties = new LinkedList<PropertyData>();
    synchronized (traverseProperties) {
      // TODO [PN] There are a problem with concurrent modification if to use iterator
      for (PropertyData nodeProperty: traverseProperties) {
        copyOfChildsProperties.add((PropertyData) copyItemData(nodeProperty));
      }
    }
    return copyOfChildsProperties;
  }
}
