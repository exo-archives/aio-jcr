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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.AccessControlPolicy;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeDataPersister;
import org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.util.EntityCollection;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/> The class responsible for workspace
 * storage initialization including: - root node - for all workspaces -
 * /jcr:system - for system workspace /exo:namespaces /jcr:nodetypes
 * /jcr:versionStorage - search index root if configured
 * 
 * @author Gennady Azarenkov
 * @version $Id: WorkspaceInitializer.java 13716 2007-03-23 10:42:43Z rainf0x $
 */

public class WorkspaceInitializer {

  protected static Log            log = ExoLogger.getLogger("jcr.WorkspaceInitializer");

  private String                  systemWorkspaceName;

  private String                  workspaceName;

  private DataManager             dataManager;

  private String                  accessControlType;

  private NamespaceDataPersister  nsPersister;

  private ExtendedNodeTypeManager ntRegistry;

  private NodeTypeDataPersister   ntPersister;

  private SearchIndex             searchIndex;

  private String                  autoInitPermissions;

  public WorkspaceInitializer(WorkspaceEntry config,
      RepositoryEntry repConfig,
      CacheableWorkspaceDataManager dataManager,
      NamespaceDataPersister nsPersister,
      ExtendedNodeTypeManager ntRegistry,
      NodeTypeDataPersister ntPersister) {

    this.workspaceName = config.getName();
    this.autoInitPermissions = config.getAutoInitPermissions();
    this.systemWorkspaceName = repConfig.getSystemWorkspaceName();
    this.dataManager = dataManager;
    this.nsPersister = nsPersister;
    this.ntRegistry = ntRegistry;
    this.ntPersister = ntPersister;
    this.accessControlType = repConfig.getAccessControl();

  }

  public WorkspaceInitializer(WorkspaceEntry config,
      RepositoryEntry repConfig,
      CacheableWorkspaceDataManager dataManager,
      NamespaceDataPersister nsPersister,
      ExtendedNodeTypeManager ntRegistry,
      NodeTypeDataPersister ntPersister,
      SearchIndex searchIndex) {

    this.workspaceName = config.getName();
    this.autoInitPermissions = config.getAutoInitPermissions();
    this.systemWorkspaceName = repConfig.getSystemWorkspaceName();
    this.dataManager = dataManager;
    this.nsPersister = nsPersister;
    this.ntRegistry = ntRegistry;
    this.ntPersister = ntPersister;
    this.accessControlType = repConfig.getAccessControl();
    this.searchIndex = searchIndex;
  }

  public NodeData initWorkspace(InternalQName rootNodeType) throws RepositoryException {

    if (isWorkspaceInitialized()) {
      return (NodeData) dataManager.getItemData(Constants.ROOT_UUID);
    }

    NodeData root = initRootNode(rootNodeType);

    if (log.isDebugEnabled())
      log.debug("Root node for " + workspaceName + " initialized. NodeType: " + rootNodeType
          + " system workspace: " + systemWorkspaceName);

    // Init system workspace
    if (workspaceName.equals(systemWorkspaceName)) {
      // initialize /jcr:system
      NodeData sys = initJcrSystemNode(root);
    }

    return root;
  }

  /**
   * Workspace jobs. Will start after the repository initialization.
   */
  public void startWorkspace() throws RepositoryException {
    initSearchIndex();
  }

  private void initSearchIndex() throws RepositoryException {

    if (searchIndex != null) {
      try {
        searchIndex.init();
      } catch (IOException e) {
        e.printStackTrace();
        throw new RepositoryException(e);
      }
    }
  }

  public boolean isWorkspaceInitialized() {
    try {
      return dataManager.getItemData(Constants.ROOT_UUID) == null ? false : true;
    } catch (RepositoryException e) {
      return false;
    }
  }

  private NodeData initRootNode(InternalQName rootNodeType) throws RepositoryException {

    PlainChangesLog changesLog = new PlainChangesLogImpl();

    TransientNodeData rootNode = new TransientNodeData(Constants.ROOT_PATH,
        Constants.ROOT_UUID,
        -1,
        rootNodeType,
        new InternalQName[0],
        0,
        null,
        new AccessControlList());
    changesLog.add(new ItemState(rootNode, ItemState.ADDED, false, null));

    TransientPropertyData primaryType = new TransientPropertyData(QPath.makeChildPath(rootNode
        .getQPath(), Constants.JCR_PRIMARYTYPE),
        IdGenerator.generate(),
        -1,
        PropertyType.NAME,
        rootNode.getIdentifier(),
        false);
    primaryType.setValue(new TransientValueData(rootNodeType));

    changesLog.add(new ItemState(primaryType, ItemState.ADDED, false, null)); // 

    boolean addACL = !accessControlType.equals(AccessControlPolicy.DISABLE);

    if (addACL) {
      AccessControlList acl = new AccessControlList();

      if (autoInitPermissions != null) {
        acl.removePermissions(SystemIdentity.ANY);
        acl.addPermissions(autoInitPermissions);
      }

      rootNode.setACL(acl);

      InternalQName[] mixins = new InternalQName[] { Constants.EXO_OWNEABLE, Constants.EXO_PRIVILEGEABLE };
      rootNode.setMixinTypeNames(mixins);

      // jcr:mixinTypes
      List<ValueData> mixValues = new ArrayList<ValueData>();
      for (InternalQName mixin: mixins) {
        mixValues.add(new TransientValueData(mixin));
      }
      TransientPropertyData exoMixinTypes = TransientPropertyData.createPropertyData(rootNode,
          Constants.JCR_MIXINTYPES,
          PropertyType.NAME,
          true, 
          mixValues);
      
      TransientPropertyData exoOwner = TransientPropertyData.createPropertyData(rootNode,
          Constants.EXO_OWNER,
          PropertyType.STRING,
          false,
          new TransientValueData(acl.getOwner()));
      
      List<ValueData> permsValues = new ArrayList<ValueData>();
      for (int i = 0; i < acl.getPermissionEntries().size(); i++) {
        AccessControlEntry entry = acl.getPermissionEntries().get(i);
        permsValues.add(new TransientValueData(entry));
      }
      TransientPropertyData exoPerms = TransientPropertyData.createPropertyData(rootNode,
          Constants.EXO_PERMISSIONS,
          ExtendedPropertyType.PERMISSION,
          true,
          permsValues);      
      
      changesLog.add(ItemState.createAddedState(exoMixinTypes)).add(ItemState
          .createAddedState(exoOwner)).add(ItemState.createAddedState(exoPerms));
      changesLog.add(new ItemState(rootNode, ItemState.MIXIN_CHANGED, false, null));
    }
    
    dataManager.save(new TransactionChangesLog(changesLog));

    return rootNode;
  }

  private NodeData initJcrSystemNode(NodeData root) throws RepositoryException {

    PlainChangesLog changesLog = new PlainChangesLogImpl();

    TransientNodeData jcrSystem = TransientNodeData.createNodeData(root,
        Constants.JCR_SYSTEM,
        Constants.NT_UNSTRUCTURED,
        Constants.SYSTEM_UUID);

    TransientPropertyData primaryType = TransientPropertyData.createPropertyData(jcrSystem,
        Constants.JCR_PRIMARYTYPE,
        PropertyType.NAME,
        false);
    primaryType.setValue(new TransientValueData(jcrSystem.getPrimaryTypeName()));

    changesLog.add(ItemState.createAddedState(jcrSystem)).add(ItemState
        .createAddedState(primaryType));

    boolean addACL = !accessControlType.equals(AccessControlPolicy.DISABLE);

    if (addACL) {
      AccessControlList acl = new AccessControlList();
      
      InternalQName[] mixins = new InternalQName[] { Constants.EXO_OWNEABLE, Constants.EXO_PRIVILEGEABLE };
      jcrSystem.setMixinTypeNames(mixins);
      
      // jcr:mixinTypes
      List<ValueData> mixValues = new ArrayList<ValueData>();
      for (InternalQName mixin: mixins) {
        mixValues.add(new TransientValueData(mixin));
      }
      TransientPropertyData exoMixinTypes = TransientPropertyData.createPropertyData(jcrSystem,
          Constants.JCR_MIXINTYPES,
          PropertyType.NAME,
          true, 
          mixValues);
      
      TransientPropertyData exoOwner = TransientPropertyData.createPropertyData(jcrSystem,
          Constants.EXO_OWNER,
          PropertyType.STRING,
          false,
          new TransientValueData(acl.getOwner()));
      
      List<ValueData> permsValues = new ArrayList<ValueData>();
      for (int i = 0; i < acl.getPermissionEntries().size(); i++) {
        AccessControlEntry entry = acl.getPermissionEntries().get(i);
        permsValues.add(new TransientValueData(entry));
      }
      TransientPropertyData exoPerms = TransientPropertyData.createPropertyData(jcrSystem,
          Constants.EXO_PERMISSIONS,
          ExtendedPropertyType.PERMISSION,
          true,
          permsValues);      
      
      changesLog.add(ItemState.createAddedState(exoMixinTypes)).add(ItemState
          .createAddedState(exoOwner)).add(ItemState.createAddedState(exoPerms));
      changesLog.add(new ItemState(jcrSystem, ItemState.MIXIN_CHANGED, false, null));
    }

    // init version storage
    TransientNodeData versionStorageNodeData = TransientNodeData.createNodeData(jcrSystem,
        Constants.JCR_VERSIONSTORAGE,
        Constants.EXO_VERSIONSTORAGE,
        Constants.VERSIONSTORAGE_UUID);

    TransientPropertyData vsPrimaryType = TransientPropertyData
        .createPropertyData(versionStorageNodeData,
            Constants.JCR_PRIMARYTYPE,
            PropertyType.NAME,
            false);
    vsPrimaryType.setValue(new TransientValueData(versionStorageNodeData.getPrimaryTypeName()));

    changesLog.add(ItemState.createAddedState(versionStorageNodeData)).add(ItemState
        .createAddedState(vsPrimaryType));

    dataManager.save(new TransactionChangesLog(changesLog));

    nsPersister.initStorage(jcrSystem, addACL, NamespaceRegistryImpl.DEF_NAMESPACES);

    // TODO make one method!
    ntPersister.initNodetypesRoot(jcrSystem, addACL);
    ntPersister.initStorage(((EntityCollection) ntRegistry.getAllNodeTypes()).getList());

    return jcrSystem;
  }
}
