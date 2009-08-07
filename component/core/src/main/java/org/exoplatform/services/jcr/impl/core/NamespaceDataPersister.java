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
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.NodeDataReader;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id$
 */

public class NamespaceDataPersister {

  public static Log       log = ExoLogger.getLogger("jcr.NamespaceDataPersister");

  private DataManager     dataManager;

  private PlainChangesLog changesLog;

  private NodeData        nsRoot;

  public NamespaceDataPersister(DataManager dataManager) {
    this.dataManager = dataManager;
    this.changesLog = new PlainChangesLogImpl();
    try {
      NodeData jcrSystem = (NodeData) dataManager.getItemData(Constants.SYSTEM_UUID);
      if (jcrSystem != null)
        this.nsRoot = (NodeData) dataManager.getItemData(jcrSystem,
                                                         new QPathEntry(Constants.EXO_NAMESPACES, 1));
    } catch (RepositoryException e) {
      log.warn("Namespace storage (/jcr:system/exo:namespaces node) is not initialized");
    }
  }

  /**
   * Creates namespaces storage and fill it with given namespaces.
   * 
   * @param nsSystem
   * @param addACL
   * @param namespaces
   * @throws RepositoryException
   */
  public void initStorage(NodeData nsSystem, boolean addACL, Map<String, String> namespaces) throws RepositoryException {

    TransientNodeData exoNamespaces = TransientNodeData.createNodeData(nsSystem,
                                                                       Constants.EXO_NAMESPACES,
                                                                       Constants.NT_UNSTRUCTURED);

    TransientPropertyData primaryType = TransientPropertyData.createPropertyData(exoNamespaces,
                                                                                 Constants.JCR_PRIMARYTYPE,
                                                                                 PropertyType.NAME,
                                                                                 false);
    primaryType.setValue(new TransientValueData(exoNamespaces.getPrimaryTypeName()));

    changesLog.add(ItemState.createAddedState(exoNamespaces))
              .add(ItemState.createAddedState(primaryType));

    if (addACL) {
      AccessControlList acl = new AccessControlList();

      InternalQName[] mixins = new InternalQName[] { Constants.EXO_OWNEABLE,
          Constants.EXO_PRIVILEGEABLE };
      exoNamespaces.setMixinTypeNames(mixins);

      // jcr:mixinTypes
      List<ValueData> mixValues = new ArrayList<ValueData>();
      for (InternalQName mixin : mixins) {
        mixValues.add(new TransientValueData(mixin));
      }
      TransientPropertyData exoMixinTypes = TransientPropertyData.createPropertyData(exoNamespaces,
                                                                                     Constants.JCR_MIXINTYPES,
                                                                                     PropertyType.NAME,
                                                                                     true,
                                                                                     mixValues);

      TransientPropertyData exoOwner = TransientPropertyData.createPropertyData(exoNamespaces,
                                                                                Constants.EXO_OWNER,
                                                                                PropertyType.STRING,
                                                                                false,
                                                                                new TransientValueData(acl.getOwner()));

      List<ValueData> permsValues = new ArrayList<ValueData>();
      for (int i = 0; i < acl.getPermissionEntries().size(); i++) {
        AccessControlEntry entry = acl.getPermissionEntries().get(i);
        permsValues.add(new TransientValueData(entry));
      }
      TransientPropertyData exoPerms = TransientPropertyData.createPropertyData(exoNamespaces,
                                                                                Constants.EXO_PERMISSIONS,
                                                                                ExtendedPropertyType.PERMISSION,
                                                                                true,
                                                                                permsValues);

      changesLog.add(ItemState.createAddedState(exoMixinTypes))
                .add(ItemState.createAddedState(exoOwner))
                .add(ItemState.createAddedState(exoPerms));
      changesLog.add(new ItemState(exoNamespaces, ItemState.MIXIN_CHANGED, false, null));
    }

    nsRoot = exoNamespaces;

    Iterator<String> i = namespaces.keySet().iterator();
    while (i.hasNext()) {
      String nsKey = i.next();
      if (nsKey != null) {
        if (log.isDebugEnabled())
          log.debug("Namespace " + nsKey + " " + namespaces.get(nsKey));
        addNamespace(nsKey, namespaces.get(nsKey));
        if (log.isDebugEnabled())
          log.debug("Namespace " + nsKey + " is initialized.");
      } else {
        log.warn("Namespace is " + nsKey + " " + namespaces.get(nsKey));
      }
    }
    saveChanges();
  }

  /**
   * Add new namespace.
   * 
   * @param prefix
   *          NS prefix
   * @param uri
   *          NS URI
   * @throws RepositoryException
   *           Repository error
   */
  public void addNamespace(String prefix, String uri) throws RepositoryException {

    if (!isInialized()) {
      log.warn("Namespace storage (/jcr:system/exo:namespaces node) is not initialized");
      return;
    }

    TransientNodeData nsNode = TransientNodeData.createNodeData(nsRoot,
                                                                new InternalQName("", prefix),
                                                                Constants.EXO_NAMESPACE);

    TransientPropertyData primaryType = TransientPropertyData.createPropertyData(nsNode,
                                                                                 Constants.JCR_PRIMARYTYPE,
                                                                                 PropertyType.NAME,
                                                                                 false);
    primaryType.setValue(new TransientValueData(nsNode.getPrimaryTypeName()));

    TransientPropertyData exoUri = TransientPropertyData.createPropertyData(nsNode,
                                                                            Constants.EXO_URI_NAME,
                                                                            PropertyType.STRING,
                                                                            false);
    exoUri.setValue(new TransientValueData(uri));

    TransientPropertyData exoPrefix = TransientPropertyData.createPropertyData(nsNode,
                                                                               Constants.EXO_PREFIX,
                                                                               PropertyType.STRING,
                                                                               false);
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
                                                                InternalQName.parse(prefix),
                                                                Constants.EXO_NAMESPACE);
    changesLog.add(ItemState.createDeletedState(nsNode));
  }

  @Deprecated
  Map<String, String> loadNamespaces() throws PathNotFoundException, RepositoryException {

    Map<String, String> nsMap = new HashMap<String, String>();

    if (isInialized()) {
      NodeDataReader nsReader = new NodeDataReader(nsRoot, dataManager, null);
      nsReader.setRememberSkiped(true);
      nsReader.forNodesByType(Constants.EXO_NAMESPACE);
      nsReader.read();

      List<NodeDataReader> nsData = nsReader.getNodesByType(Constants.EXO_NAMESPACE);
      for (NodeDataReader nsr : nsData) {
        nsr.forProperty(Constants.EXO_URI_NAME, PropertyType.STRING)
           .forProperty(Constants.EXO_PREFIX, PropertyType.STRING);
        nsr.read();

        String exoUri = nsr.getPropertyValue(Constants.EXO_URI_NAME).getString();
        String exoPrefix = nsr.getPropertyValue(Constants.EXO_PREFIX).getString();
        nsMap.put(exoPrefix, exoUri);
        log.info("Namespace " + exoPrefix + " is loaded");
      }

      for (NodeData skipedNs : nsReader.getSkiped()) {
        log.warn("Namespace node "
            + skipedNs.getQPath().getName().getAsString()
            + " (primary type '"
            + skipedNs.getPrimaryTypeName().getAsString()
            + "') is not supported for loading. Nodes with 'exo:namespace' node type is supported only now.");
      }
    } else {
      log.warn("Namespace storage (/jcr:system/exo:namespaces node) is not initialized. No namespaces loaded.");
    }
    return nsMap;
  }

  void loadNamespaces(Map<String, String> namespacesMap, Map<String, String> urisMap) throws RepositoryException {

    if (!isInialized()) {
      NodeData jcrSystem = (NodeData) dataManager.getItemData(Constants.SYSTEM_UUID);
      if (jcrSystem != null)
        this.nsRoot = (NodeData) dataManager.getItemData(jcrSystem,
                                                         new QPathEntry(Constants.EXO_NAMESPACES, 1));
      else
        throw new RepositoryException("/jcr:system is not found. Possible the workspace is not initialized properly");
    }

    if (isInialized()) {
      NodeDataReader nsReader = new NodeDataReader(nsRoot, dataManager, null);
      nsReader.setRememberSkiped(true);
      nsReader.forNodesByType(Constants.EXO_NAMESPACE);
      nsReader.read();

      List<NodeDataReader> nsData = nsReader.getNodesByType(Constants.EXO_NAMESPACE);
      for (NodeDataReader nsr : nsData) {
        nsr.forProperty(Constants.EXO_URI_NAME, PropertyType.STRING)
           .forProperty(Constants.EXO_PREFIX, PropertyType.STRING);
        nsr.read();

        String exoUri = nsr.getPropertyValue(Constants.EXO_URI_NAME).getString();
        String exoPrefix = nsr.getPropertyValue(Constants.EXO_PREFIX).getString();
        namespacesMap.put(exoPrefix, exoUri);
        urisMap.put(exoUri, exoPrefix);
        if (log.isDebugEnabled())
          log.debug("Namespace " + exoPrefix + " is loaded");
      }

      for (NodeData skipedNs : nsReader.getSkiped()) {
        log.warn("Namespace node "
            + skipedNs.getQPath().getName().getAsString()
            + " (primary type '"
            + skipedNs.getPrimaryTypeName().getAsString()
            + "') is not supported for loading. Nodes with 'exo:namespace' node type is supported only now.");
      }
    } else
      log.warn("Namespace storage (/jcr:system/exo:namespaces node) is not initialized. No namespaces loaded.");
  }

  void saveChanges() throws RepositoryException, InvalidItemStateException {
    dataManager.save(new TransactionChangesLog(changesLog));
    changesLog.clear();
  }

  private boolean isInialized() {
    return nsRoot != null;
  }

}
