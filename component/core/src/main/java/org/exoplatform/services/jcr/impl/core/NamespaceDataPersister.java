/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.InvalidItemStateException;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.NodeDataReader;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: NamespaceDataPersister.java 13716 2007-03-23 10:42:43Z rainf0x $
 */

public class NamespaceDataPersister {
  
  public static Log log = ExoLogger.getLogger("jcr.NamespaceDataPersister");
  
  private DataManager dataManager;
  
  private PlainChangesLog changesLog;
  
  private NodeData nsRoot;
  
  public NamespaceDataPersister(DataManager dataManager) {
    this.dataManager = dataManager;
    this.changesLog = new PlainChangesLogImpl();
    try {
      this.nsRoot = (NodeData) dataManager.getItemData(Constants.EXO_NAMESPACES_PATH);
    } catch (RepositoryException e) {
      log.warn("Namespace storage (/jcr:system/exo:namespaces node) is not initialized");
    }
  }
  
  
//  void initStorage(NodeData nsSystem, boolean addACL, Map<InternalQName, String> namespaces) {
  public void initStorage(NodeData nsSystem, boolean addACL, Map<String, String> namespaces) 
  throws RepositoryException {
    
    TransientNodeData root = TransientNodeData.createNodeData(
        nsSystem, Constants.EXO_NAMESPACES, Constants.NT_UNSTRUCTURED);
    
    TransientPropertyData primaryType = TransientPropertyData.createPropertyData(
        root, Constants.JCR_PRIMARYTYPE, PropertyType.NAME, false);
    primaryType.setValue(new TransientValueData(root.getPrimaryTypeName()));
    
    changesLog.add(ItemState.createAddedState(root))
              .add(ItemState.createAddedState(primaryType));
    
    if(addACL) {
      AccessControlList acl = new AccessControlList(); 
      root.setMixinTypeNames(new InternalQName[] {Constants.EXO_ACCESS_CONTROLLABLE} );
      // jcr:mixinTypes
      TransientPropertyData rootMixinTypes = TransientPropertyData
          .createPropertyData(root, Constants.JCR_MIXINTYPES, PropertyType.NAME, false);
      rootMixinTypes.setValue(new TransientValueData(Constants.EXO_ACCESS_CONTROLLABLE));

      TransientPropertyData exoOwner = TransientPropertyData.createPropertyData(
          root, Constants.EXO_OWNER, PropertyType.STRING, false);
      exoOwner.setValue(new TransientValueData(acl.getOwner()));
      TransientPropertyData exoPerms = TransientPropertyData.createPropertyData(
          root, Constants.EXO_PERMISSIONS, ExtendedPropertyType.PERMISSION, true);
      List<ValueData> perms = new ArrayList<ValueData>();
      for(int i=0; i<acl.getPermissionEntries().size(); i++) {
        AccessControlEntry entry = acl.getPermissionEntries().get(i);
        perms.add(new TransientValueData(entry));
      }
      exoPerms.setValues(perms);
      changesLog.add(ItemState.createAddedState(rootMixinTypes))
                .add(ItemState.createAddedState(exoOwner))
                .add(ItemState.createAddedState(exoPerms));
      changesLog.add(new ItemState(root, ItemState.MIXIN_CHANGED, false, null));
    }
    
    nsRoot = root;
    
//    Iterator<InternalQName> i = namespaces.keySet().iterator();
    Iterator<String> i = namespaces.keySet().iterator();
    while (i.hasNext()) {
//      InternalQName nsKey = i.next();
      String nsKey = i.next();
      if (nsKey != null) {
        log.debug("Namespace " + nsKey + " " + namespaces.get(nsKey));
        addNamespace(nsKey, namespaces.get(nsKey));
        log.info("Namespace " + nsKey + " is initialized.");
      } else {
        log.warn("Namespace is " + nsKey + " " + namespaces.get(nsKey));
      }
    }
    saveChanges();
  }
  
  
  ///** @deprecated */
  public void addNamespace(String prefix, String uri)  
      throws RepositoryException, InvalidItemStateException {

    if (!isInialized()) {
      log.warn("Namespace storage (/jcr:system/exo:namespaces node) is not initialized");
      return;
    }
    
    TransientNodeData nsNode = TransientNodeData.createNodeData(nsRoot,
        new InternalQName("", prefix),
        Constants.EXO_NAMESPACE);
    
    TransientPropertyData primaryType = TransientPropertyData.createPropertyData(
        nsNode, Constants.JCR_PRIMARYTYPE, PropertyType.NAME, false);
    primaryType.setValue(new TransientValueData(nsNode.getPrimaryTypeName()));

    TransientPropertyData exoUri = TransientPropertyData.createPropertyData(
        nsNode, Constants.EXO_URI_NAME, PropertyType.STRING, false);
    exoUri.setValue(new TransientValueData(uri));

    TransientPropertyData exoPrefix = TransientPropertyData.createPropertyData(
        nsNode, Constants.EXO_PREFIX, PropertyType.STRING, false);
    exoPrefix.setValue(new TransientValueData(prefix));

    changesLog.add(ItemState.createAddedState(nsNode))
      .add(ItemState.createAddedState(primaryType))
      .add(ItemState.createAddedState(exoUri))
      .add(ItemState.createAddedState(exoPrefix));
    
  }

  void removeNamespace(String prefix) throws IllegalNameException {
    if (!isInialized()) {
      log.warn("Namespace storage (/jcr:system/exo:namespaces node) is not initialized");
      return;
    }  
    
    TransientNodeData nsNode = TransientNodeData.createNodeData(nsRoot,
        InternalQName.parse(prefix), Constants.EXO_NAMESPACE);
    changesLog.add(ItemState.createDeletedState(nsNode));
  }
  
  Map<String, String> loadNamespaces() throws PathNotFoundException, RepositoryException {
    
    Map<String, String> nsMap = new HashMap<String, String>();
    
    if (isInialized()) {
      NodeDataReader nsReader = new NodeDataReader(nsRoot, dataManager, null);
      nsReader.setRememberSkiped(true);
      nsReader.forNodesByType(Constants.EXO_NAMESPACE);
      nsReader.read();
      
      List<NodeDataReader> nsData = nsReader.getNodesByType(Constants.EXO_NAMESPACE);
      for (NodeDataReader nsr: nsData) {
        nsr.forProperty(Constants.EXO_URI_NAME, PropertyType.STRING)
           .forProperty(Constants.EXO_PREFIX, PropertyType.STRING);
        nsr.read();
        
        String exoUri = nsr.getPropertyValue(Constants.EXO_URI_NAME).getString();
        String exoPrefix = nsr.getPropertyValue(Constants.EXO_PREFIX).getString();
        nsMap.put(exoPrefix, exoUri);
        log.info("Namespace " + exoPrefix + " is loaded");
      }
      
      for (NodeData skipedNs: nsReader.getSkiped()) {
        log.warn("Namespace node " + skipedNs.getQPath().getName().getAsString()
            + " (primary type '" + skipedNs.getPrimaryTypeName().getAsString()
            + "') is not supported for loading. Nodes with 'exo:namespace' node type is supported only now.");
      }
    } else {
      log.warn("Namespace storage (/jcr:system/exo:namespaces node) is not initialized. No namespaces loaded.");
    }
    return nsMap;
  }

  
  void saveChanges() throws RepositoryException, InvalidItemStateException {
    dataManager.save(new TransactionChangesLog(changesLog));
    changesLog.clear();
  }

//  void discardChanges() throws RepositoryException, InvalidItemStateException {
//    changesLog.clear();
//  }
  
  
  private boolean isInialized() {
    return nsRoot != null;
  }

}


