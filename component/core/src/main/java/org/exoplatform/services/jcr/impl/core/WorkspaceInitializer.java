/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeDataPersister;
import org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.util.EntityCollection;
import org.exoplatform.services.jcr.util.UUIDGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL . <br/> The class responsible for workspace
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
    this.accessControlType = repConfig.getAuthenticationPolicy();

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
    this.accessControlType = repConfig.getAuthenticationPolicy();
    this.searchIndex = searchIndex;
  }

  public NodeData initWorkspace(InternalQName rootNodeType) throws RepositoryException {

    if (isWorkspaceInitialized()) {
      // [PN] 21.02.07 moved to startWorkspace()
      // initSearchIndex();
      // [PN] 25.04.07 use UUID instead path
      return (NodeData) dataManager.getItemData(Constants.ROOT_UUID);
    }

    // Init root (/) node
    NodeData root = initRootNode(rootNodeType);
    // [PN] 21.02.07 moved to startWorkspace()
    // initSearchIndex();

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

    TransientNodeData nodeData = new TransientNodeData(Constants.ROOT_PATH,
        Constants.ROOT_UUID,
        -1,
        rootNodeType,
        new InternalQName[0],
        0,
        null,
        new AccessControlList());
    changesLog.add(new ItemState(nodeData, ItemState.ADDED, false, null));

    TransientPropertyData primaryType = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE),
        UUIDGenerator.generate(),
        -1,
        PropertyType.NAME,
        nodeData.getUUID(),
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

      nodeData.setACL(acl);

      nodeData.setMixinTypeNames(new InternalQName[] { Constants.EXO_ACCESS_CONTROLLABLE });

      // jcr:mixinTypes
      TransientPropertyData exoMixinTypes = TransientPropertyData.createPropertyData(nodeData,
          Constants.JCR_MIXINTYPES,
          PropertyType.NAME,
          false);
      exoMixinTypes.setValue(new TransientValueData(Constants.EXO_ACCESS_CONTROLLABLE));

      TransientPropertyData exoOwner = TransientPropertyData.createPropertyData(nodeData,
          Constants.EXO_OWNER,
          PropertyType.STRING,
          false,
          new TransientValueData(acl.getOwner()));
      TransientPropertyData exoPerms = TransientPropertyData.createPropertyData(nodeData,
          Constants.EXO_PERMISSIONS,
          ExtendedPropertyType.PERMISSION,
          true);

      List<ValueData> perms = new ArrayList<ValueData>();
      for (int i = 0; i < acl.getPermissionEntries().size(); i++) {
        AccessControlEntry entry = acl.getPermissionEntries().get(i);
        perms.add(new TransientValueData(entry));
      }
      exoPerms.setValues(perms);
      changesLog.add(ItemState.createAddedState(exoMixinTypes)).add(ItemState
          .createAddedState(exoOwner)).add(ItemState.createAddedState(exoPerms));
      changesLog.add(new ItemState(nodeData, ItemState.MIXIN_CHANGED, false, null));
    }
    dataManager.save(new TransactionChangesLog(changesLog));

    return nodeData;
  }

  private NodeData initJcrSystemNode(NodeData root) throws RepositoryException {

    PlainChangesLog changesLog = new PlainChangesLogImpl();

    TransientNodeData sysNodeData = TransientNodeData.createNodeData(root,
        Constants.JCR_SYSTEM,
        Constants.NT_UNSTRUCTURED,
        Constants.SYSTEM_UUID);

    TransientPropertyData primaryType = TransientPropertyData.createPropertyData(sysNodeData,
        Constants.JCR_PRIMARYTYPE,
        PropertyType.NAME,
        false);
    primaryType.setValue(new TransientValueData(sysNodeData.getPrimaryTypeName()));

    changesLog.add(ItemState.createAddedState(sysNodeData)).add(ItemState
        .createAddedState(primaryType));

    boolean addACL = !accessControlType.equals(AccessControlPolicy.DISABLE);

    if (addACL) {
      AccessControlList acl = new AccessControlList();

      sysNodeData.setMixinTypeNames(new InternalQName[] { Constants.EXO_ACCESS_CONTROLLABLE });

      // jcr:mixinTypes
      TransientPropertyData exoMixinTypes = TransientPropertyData.createPropertyData(sysNodeData,
          Constants.JCR_MIXINTYPES,
          PropertyType.NAME,
          false);
      exoMixinTypes.setValue(new TransientValueData(Constants.EXO_ACCESS_CONTROLLABLE));

      TransientPropertyData exoOwner = TransientPropertyData.createPropertyData(sysNodeData,
          Constants.EXO_OWNER,
          PropertyType.STRING,
          false,
          new TransientValueData(acl.getOwner()));
      TransientPropertyData exoPerms = TransientPropertyData.createPropertyData(sysNodeData,
          Constants.EXO_PERMISSIONS,
          ExtendedPropertyType.PERMISSION,
          true);
      List<ValueData> perms = new ArrayList<ValueData>();
      for (int i = 0; i < acl.getPermissionEntries().size(); i++) {
        AccessControlEntry entry = acl.getPermissionEntries().get(i);
        perms.add(new TransientValueData(entry));
      }
      exoPerms.setValues(perms);
      changesLog.add(ItemState.createAddedState(exoMixinTypes)).add(ItemState
          .createAddedState(exoOwner)).add(ItemState.createAddedState(exoPerms));
      changesLog.add(new ItemState(sysNodeData, ItemState.MIXIN_CHANGED, false, null));
    }

    // init version storage
    TransientNodeData versionStorageNodeData = TransientNodeData.createNodeData(sysNodeData,
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
    changesLog.clear();

    nsPersister.initStorage(sysNodeData, addACL, NamespaceRegistryImpl.DEF_NAMESPACES);

    // TODO make one method!
    ntPersister.initNodetypesRoot(sysNodeData, addACL);
    ntPersister.initStorage(((EntityCollection) ntRegistry.getAllNodeTypes()).getList());

    // TODO To catch exceptions caused by already existing nodes (jcr:system,
    // jcr:versionStorage)
    return sysNodeData;
  }
}
