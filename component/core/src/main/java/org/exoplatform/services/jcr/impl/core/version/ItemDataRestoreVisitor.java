/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.jcr.ItemExistsException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.version.OnParentVersionAction;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataCopyVisitor1;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.jcr.impl.dataflow.version.VersionHistoryDataHelper;
import org.exoplatform.services.jcr.util.UUIDGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 *
 * 14.12.2006
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ItemDataRestoreVisitor.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class ItemDataRestoreVisitor extends ItemDataTraversingVisitor {

  private static Log log = ExoLogger.getLogger("jcr.ItemDataRestoreVisitor");

  protected final boolean removeExisting;
  protected final Stack<NodeDataContext> parents = new Stack<NodeDataContext>();
  protected final NodeData context;
  protected final NodeData history;
  protected final InternalQName restoringName;

  protected NodeData restored;

  protected final NodeTypeManagerImpl ntManager;
  protected final SessionImpl userSession;

  protected final SessionChangesLog changes;

  /**
   * Usecase of Workspace.restore(Version[], boolean), for not existing versionable nodes in the target workspace.
   */
  protected final SessionChangesLog delegatedChanges;
  
  protected class NodeDataContext {

    private final NodeData node;
    private final boolean existing;

    protected NodeDataContext(NodeData node) {
      this.node = node;
      this.existing = false;
    }

    protected NodeDataContext(NodeData node, boolean existing) {
      this.node = node;
      this.existing = existing;
    }

    protected NodeData getNode() {
      return this.node;
    }

    public boolean isExisting() {
      return existing;
    }
  }

  protected class RemoveVisitor extends ItemDataRemoveVisitor {
    RemoveVisitor() {
      super(userSession, true);
    }

    protected void validateReferential(NodeData node) throws RepositoryException {
      // TODO no REFERENCE validation here
    }
  };

  ItemDataRestoreVisitor(NodeData context, InternalQName restoringName, NodeData history, SessionImpl userSession, boolean removeExisting) throws RepositoryException {
    this(context, restoringName, history, userSession, removeExisting, null);
  }

  ItemDataRestoreVisitor(NodeData context, InternalQName restoringName, NodeData history,
      SessionImpl userSession, boolean removeExisting, SessionChangesLog delegatedChanges) throws RepositoryException {
    super(userSession.getTransientNodesManager().getTransactManager());

    this.userSession = userSession;
    this.changes = new SessionChangesLog(userSession.getId());
    this.context = context;
    this.restoringName = restoringName;
    this.history = history;
    this.parents.push(new NodeDataContext(context));
    this.removeExisting = removeExisting;
    this.ntManager = userSession.getWorkspace().getNodeTypeManager();
    this.delegatedChanges = delegatedChanges;
  }

  private NodeData currentNode() {
    return parents.peek().getNode();
  }

  private NodeData pushCurrent(NodeData node) {
    return parents.push(new NodeDataContext(node)).getNode();
  }

  private ItemData findDelegated(String uuid) {
    if (delegatedChanges != null)
      for (ItemState state: delegatedChanges.getAllStates()) {
        if (state.getData().getUUID().equals(uuid))
          return state.getData();
      }
    return null;
  }

  private ItemData findDelegated(QPath path) {
    if (delegatedChanges != null)
      for (ItemState state: delegatedChanges.getAllStates()) {
        if (state.getData().getQPath().equals(path))
          return state.getData();
      }
    return null;
  }

  private void deleteDelegated(QPath path) {
    if (delegatedChanges != null) {
      List<ItemState> removed = new ArrayList<ItemState>();
      for (ItemState state: delegatedChanges.getAllStates()) { 
        if (state.getData().getQPath().equals(path) || state.getData().getQPath().isDescendantOf(path, false))
          removed.add(state);
      }

      for (ItemState state: removed) {
        delegatedChanges.remove(state.getData().getQPath());
      }
    }
  }

  protected void initRestoreRoot(NodeData parentData, InternalQName name, NodeData frozen) throws RepositoryException {

    // TODO fix JCRPath in exceptions

    // WARNING: path with index=1
    QPath nodePath = QPath.makeChildPath(parentData.getQPath(), name);

    if (log.isDebugEnabled())
      log.debug("Restore: " + nodePath.getAsString() + ", removeExisting=" + removeExisting);

    //InternalQPath frozenPath = InternalQPath.makeChildPath(historyData.getQPath(), Constants.JCR_FROZENNODE);
//    QPath frozenPath = frozen.getQPath();
//
//    QPath frozenUuidPath = QPath.makeChildPath(frozenPath, Constants.JCR_FROZENUUID);
//    PropertyData frozenUuid = (PropertyData) dataManager.getItemData(frozenUuidPath);
    PropertyData frozenUuid = (PropertyData) dataManager.getItemData(frozen,new QPathEntry(Constants.JCR_FROZENUUID,0));
    
    String fuuid = null;
    NodeData existing = null;
    // make new node from frozen
    try {
      fuuid = new String(frozenUuid.getValues().get(0).getAsByteArray());
      NodeData sameUuidNodeRestored = (NodeData) findDelegated(fuuid);
      if (sameUuidNodeRestored != null) {
        // already restored from delegated call, remove it as we interested in this version state
        deleteDelegated(sameUuidNodeRestored.getQPath());
      } else {
        NodeData sameUuidNode = (NodeData) dataManager.getItemData(fuuid);
        //final NodeData sameUuidNode = (NodeData) findExistingItemData(fuuid);
        if (sameUuidNode != null) {
          QPath sameUuidPath = sameUuidNode.getQPath();
          if (sameUuidPath.makeParentPath().equals(nodePath.makeParentPath()) && // same parent
              sameUuidPath.getName().equals(nodePath.getName()) ) { // same name

            if (sameUuidPath.getIndex() != nodePath.getIndex())
              // but different index, see below... fix it
              // [PN] 05.02.07
              //nodePath.getEntries()[nodePath.getLength() - 1].setIndex(sameUuidPath.getIndex());
              nodePath = QPath.makeChildPath(parentData.getQPath(), name, sameUuidPath.getIndex());

            // if it's a target node
            existing = sameUuidNode;

            // remove existed node, with validation
            ItemDataRemoveVisitor removeVisitor = new RemoveVisitor();
//            existing.accept(removeVisitor); //  removeVisitor.visit(existing)
            removeVisitor.visit(existing);

            changes.addAll(removeVisitor.getRemovedStates());
          } else if (!sameUuidPath.isDescendantOf(nodePath, false)) {
            if (removeExisting) {
              final QPath restorePath = nodePath; 
              // remove same uuid node, with validation
              class RemoveVisitor extends ItemDataRemoveVisitor {
                RemoveVisitor() {
                  super(userSession, true);
                }

                protected boolean isRemoveDescendant(ItemData item) throws RepositoryException {
                  return item.getQPath().isDescendantOf(removedRoot.getQPath(), false) ||
                    item.getQPath().isDescendantOf(restorePath, false);
                }
              };

              ItemDataRemoveVisitor removeVisitor = new RemoveVisitor();
              removeVisitor.visit(sameUuidNode);

              changes.addAll(removeVisitor.getRemovedStates());
            } else {
              throw new ItemExistsException("Item with the same UUID as restored node " + nodePath.getAsString()
                + " already exists and removeExisting=false. Existed " + sameUuidPath.getAsString()
                + " " + sameUuidNode.getUUID());
            }
          }
        }
      }
    } catch (IllegalStateException e) {
      throw new RepositoryException("jcr:frozenUuid, error of data read " + frozenUuid.getQPath().getAsString(), e);
    } catch (IOException e) {
      throw new RepositoryException("jcr:frozenUuid, error of data read " + frozenUuid.getQPath().getAsString(), e);
    }

    
//    QPath frozenPrimaryTypePath = QPath.makeChildPath(frozenPath, Constants.JCR_FROZENPRIMARYTYPE);
//    PropertyData frozenPrimaryType = (PropertyData) dataManager.getItemData(frozenPrimaryTypePath);

    PropertyData frozenPrimaryType = (PropertyData) dataManager.getItemData(frozen,
        new QPathEntry(Constants.JCR_FROZENPRIMARYTYPE, 0));
    
//    QPath frozenMixinTypesPath = QPath.makeChildPath(frozenPath, Constants.JCR_FROZENMIXINTYPES);
//    PropertyData frozenMixinTypes = (PropertyData) dataManager.getItemData(frozenMixinTypesPath);

    PropertyData frozenMixinTypes = (PropertyData) dataManager.getItemData(frozen,
        new QPathEntry(Constants.JCR_FROZENMIXINTYPES, 0));
    
    InternalQName[] mixins = null;
    if (frozenMixinTypes != null) {
      try {
        List<ValueData> mvs = frozenMixinTypes.getValues();
        mixins = new InternalQName[mvs.size()];
        for (int i=0; i<mvs.size(); i++) {
          ValueData mvd = mvs.get(i);
          mixins[i] = InternalQName.parse(new String(mvd.getAsByteArray()));
        }
      } catch (IllegalNameException e) {
          throw new RepositoryException("jcr:frozenMixinTypes, error of data read " + frozenMixinTypes.getQPath().getAsString(), e);  
      } catch (IllegalStateException e) {
        throw new RepositoryException("jcr:frozenMixinTypes, error of data read " + frozenMixinTypes.getQPath().getAsString(), e);
      } catch (IOException e) {
        throw new RepositoryException("jcr:frozenMixinTypes, error of data read " + frozenMixinTypes.getQPath().getAsString(), e);
      }
    }

    InternalQName ptName = null;
    try {
      ptName = InternalQName.parse(new String(frozenPrimaryType.getValues().get(0).getAsByteArray()));
    } catch (IllegalNameException e) {
      throw new RepositoryException("jcr:frozenPrimaryType, error of data read " + frozenPrimaryType.getQPath().getAsString(), e);
    } catch (IllegalStateException e) {
      throw new RepositoryException("jcr:frozenPrimaryType, error of data read " + frozenPrimaryType.getQPath().getAsString(), e);
    } catch (IOException e) {
      throw new RepositoryException("jcr:frozenPrimaryType, error of data read " + frozenPrimaryType.getQPath().getAsString(), e);
    }

    // create restored version of the node
    NodeData restoredData = new TransientNodeData(nodePath,
        fuuid,
        (existing != null ? existing.getPersistedVersion() : -1),
        ptName,
        mixins == null ? new InternalQName[0] : mixins,
        0, parentData.getUUID(), parentData.getACL());

//    changes.add(new ItemState(restoredData,
//        (existing != null && updateExisting ? ItemState.UPDATED : ItemState.ADDED), true, null));
    changes.add(ItemState.createAddedState(restoredData));

    pushCurrent(restoredData);
  }

  @Override
  protected void entering(NodeData frozen, int level) throws RepositoryException {

    if (frozen == null) { //changes.dump().substring(3048)
      if (log.isDebugEnabled())
        log.debug("Visit node " + frozen.getQPath().getAsString() + ", HAS NULL FROZEN NODE");
      return;
    }

    InternalQName qname = frozen.getQPath().getName();

    if(qname.equals(Constants.JCR_FROZENNODE) && level == 0) {

      // child props/nodes will be restored
      if (log.isDebugEnabled())
        log.debug("jcr:frozenNode " + frozen.getQPath().getAsString());

      // init target node
      initRestoreRoot(currentNode(), restoringName, frozen);

      restored = currentNode();

    } else if (ntManager.isNodeType(Constants.NT_VERSIONEDCHILD, frozen.getPrimaryTypeName())) {

      QPath cvhpPropPath = QPath.makeChildPath(frozen.getQPath(), Constants.JCR_CHILDVERSIONHISTORY);

      if (log.isDebugEnabled())
        log.debug("Versioned child node " + cvhpPropPath.getAsString());

      VersionHistoryDataHelper childHistory = null;
      try {

//        String vhUuid = new String(
//            ((PropertyData) dataManager.getItemData(cvhpPropPath)).getValues().get(0).getAsByteArray());

        String vhUuid = new String(
            ((PropertyData) dataManager.getItemData(frozen,new QPathEntry(Constants.JCR_CHILDVERSIONHISTORY,0))).getValues().get(0).getAsByteArray());

        NodeData cHistory = null;
        if ((cHistory = (NodeData) dataManager.getItemData(vhUuid)) == null)
          throw new RepositoryException("Version history is not found with uuid " + vhUuid);

        childHistory = new VersionHistoryDataHelper(cHistory, dataManager, ntManager);
      } catch (IllegalStateException e) {
        throw new RepositoryException("jcr:childVersionHistory, error of data read " + cvhpPropPath.getAsString(), e);
      } catch (IOException e) {
        throw new RepositoryException("jcr:childVersionHistory, error of data read " + cvhpPropPath.getAsString(), e);
      }

      //QPath cvhVersionableUuidPath = QPath.makeChildPath(childHistory.getQPath(), Constants.JCR_VERSIONABLEUUID);
      String versionableUuid = null;
      try {
//        versionableUuid = new String(
//          ((PropertyData) dataManager.getItemData(cvhVersionableUuidPath)).getValues().get(0).getAsByteArray());
        versionableUuid = new String(
            ((PropertyData) dataManager.getItemData(childHistory,new QPathEntry(Constants.JCR_VERSIONABLEUUID,0))).getValues().get(0).getAsByteArray());
        
      } catch (IOException e) {
        throw new RepositoryException("jcr:childVersionHistory, error of data read " + cvhpPropPath.getAsString(), e);
      }

      //if (log.isDebugEnabled())
      //  log.debug("jcr:versionableUuid:  " + frozenNode.getQPath().getAsString() + " " + versionableUuid);

      NodeData versionable = (NodeData) dataManager.getItemData(versionableUuid);
      //NodeData versionable = (NodeData) findExistingItemData(versionableUuid);
      if (versionable != null) {
        // exists,
        // On restore of VN, if the workspace currently has an already
        // existing node corresponding to C’s version history and the
        // removeExisting flag of the restore is set to true, then that
        // instance of C becomes the child of the restored N.
        if (!removeExisting) {
          throw new ItemExistsException("Item with the same UUID " + versionableUuid
              + " as versionable child node " + versionable.getQPath().getAsString()
              + " already exists and removeExisting=false");
        }
        // else - leaving existed unchanged
      } else {
        // not found,
        // gets last version (by time of creation) and restore it
        NodeData lastVersionData = childHistory.getLastVersionData();
        //QPath cvFrozenPath = QPath.makeChildPath(lastVersionData.getQPath(), Constants.JCR_FROZENNODE);
        //NodeData cvFrozen = (NodeData) dataManager.getItemData(cvFrozenPath);
        NodeData cvFrozen = (NodeData) dataManager.getItemData(lastVersionData,new QPathEntry(Constants.JCR_FROZENNODE,0));

        ItemDataRestoreVisitor restoreVisitor = new ItemDataRestoreVisitor(
            currentNode(), qname, childHistory, userSession, removeExisting, changes);
        cvFrozen.accept(restoreVisitor);
        changes.addAll(restoreVisitor.getRestoreChanges().getAllStates());
      }
      pushCurrent(null); // skip any childs of that node
    } else if (currentNode() != null) {
      // ordinary node for copy under nt:frozenNode
      // [PN] 10.04.06 In case of COPY - copy node, otherwise we don't
      // 8.2.11.3 INITIALIZE; 8.2.11.4 COMPUTE
      // On restore of VN, the C stored as its child will be ignored, and the
      // current C in the workspace will be left unchanged.

      int action = ntManager.findNodeDefinition(qname, currentNode().getPrimaryTypeName(), currentNode().getMixinTypeNames()).getOnParentVersion();

      if (log.isDebugEnabled())
        log.debug("Stored node " + frozen.getQPath().getAsString() + ", " + OnParentVersionAction.nameFromValue(action));

      if (action == OnParentVersionAction.COPY || action == OnParentVersionAction.VERSION) {
        // copy
        QPath restoredPath = QPath.makeChildPath(currentNode().getQPath(),
            frozen.getQPath().getName(), frozen.getQPath().getIndex());

        // jcr:uuid
        String jcrUuid = null;
        NodeData existing = null;
        if (ntManager.isNodeType(Constants.MIX_REFERENCEABLE, frozen.getPrimaryTypeName(), frozen.getMixinTypeNames())) {
          // copy uuid from frozen state of mix:referenceable,
          // NOTE: mix:referenceable stored in frozen state with genereted ID (JCR_XITEM PK) as UUID must be unique,
          // but jcr:uuid property containts real UUID.
          QPath jcrUuidPath = QPath.makeChildPath(frozen.getQPath(), Constants.JCR_UUID);
          try {
//            jcrUuid = new String(
//              ((PropertyData) dataManager.getItemData(jcrUuidPath)).getValues().get(0).getAsByteArray());
          jcrUuid = new String(
          ((PropertyData) dataManager.getItemData(frozen,new QPathEntry(Constants.JCR_UUID,0))).getValues().get(0).getAsByteArray());

          } catch (IOException e) {
            throw new RepositoryException("jcr:uuid, error of data read " + jcrUuidPath.getAsString(), e);
          }
          existing = (NodeData) dataManager.getItemData(jcrUuid);
        } else {
          // try to use existing node uuid, otherwise to generate one new
          existing = (NodeData) dataManager.getItemData(currentNode(), new QPathEntry(frozen
              .getQPath().getName(), frozen.getQPath().getIndex()));
          if (existing != null) {
            jcrUuid = existing.getUUID();
          } else {
            jcrUuid = UUIDGenerator.generate();
          }
        }

        if (existing != null && !existing.getQPath().isDescendantOf(restored.getQPath(), false)) {
          NodeData existingDelegared = (NodeData) findDelegated(existing.getQPath());
          if (existingDelegared != null) {
            // was restored by previous restore (Workspace.restore(...)), remove it from delegated log
            deleteDelegated(existing.getQPath());
          } else {
            // exists in workspace
            if (removeExisting) {
              // remove existed node, with validation (same as for restored root)
              ItemDataRemoveVisitor removeVisitor = new RemoveVisitor();
//              existing.accept(removeVisitor); //  removeVisitor.visit(existing)
              removeVisitor.visit(existing);

              changes.addAll(removeVisitor.getRemovedStates());
            } else {
              throw new ItemExistsException("Node with the same UUID as restored child node " + restoredPath.getAsString()
                  + " already exists and removeExisting=false. Existed " + existing.getQPath().getAsString()
                  + " " + existing.getUUID());
            }
          }
        }

        NodeData restoredData = new TransientNodeData(restoredPath,
            jcrUuid,
            frozen.getPersistedVersion(),
            frozen.getPrimaryTypeName(),
            frozen.getMixinTypeNames(),
            frozen.getOrderNumber(),
            currentNode().getUUID(), // parent
            frozen.getACL());

//        changes.add(new ItemState(restoredData,
//            (existing != null && updateExisting ? ItemState.UPDATED : ItemState.ADDED), true, null));
        changes.add(ItemState.createAddedState(restoredData));
        pushCurrent(restoredData);
      } else if (action == OnParentVersionAction.INITIALIZE || action == OnParentVersionAction.COMPUTE) {
        // current C in the workspace will be left unchanged,
        // TODO [PN] 20.12.06 JCR-193

//        QPath existedPath = QPath.makeChildPath(currentNode().getQPath(), frozen.getQPath().getName());
//        NodeData existed = (NodeData) dataManager.getItemData(existedPath);

        //QPath existedPath = QPath.makeChildPath(currentNode().getQPath(), frozen.getQPath().getName());
        NodeData existed = (NodeData) dataManager.getItemData(currentNode(),new QPathEntry(frozen.getQPath().getName(),0));

        if (existed != null) {
          // copy existed - i.e. left unchanged
          ItemDataCopyVisitor1 copyVisitor = new ItemDataCopyVisitor1(currentNode(), frozen.getQPath().getName(),
              ntManager, userSession.getTransientNodesManager(), true);
          existed.accept(copyVisitor);
          changes.addAll(copyVisitor.getItemAddStates());
        } // else - nothing to do, i.e. left unchanged

        pushCurrent(null); // JCR-193, skip any childs of that node now
      }
    }

//    if(node.isNodeType("nt:frozenNode") || curParent() == null) {
//      // can be either root or referenced from versionedChild
//      if (log.isDebugEnabled())
//        log.debug("frozenNode "+node.getPath());
//    } else if(node.isNodeType("nt:versionedChild")) {
//      // i.e. action == OnParentVersionAction.VERSION
//      String ref = node.getProperty("jcr:childVersionHistory").getString();
//      VersionHistoryImpl history = (VersionHistoryImpl)((SessionImpl)node.getSession()).getNodeByUUID(ref);
//      String versionableUuid = history.getVersionableUUID();
//
//      if (log.isDebugEnabled())
//        log.debug("versionedChild node >> "+node.getPath()+" "+versionableUuid);
//
//      try {
//        NodeImpl childNode = (NodeImpl)history.getSession().getNodeByUUID(versionableUuid);
//        if (log.isDebugEnabled())
//          log.debug("versionable child found >> "+childNode.getPath());
//        if(removeExisting) {
//          // [PN] 16.02.05
//          NodeImpl newNode = createChildNode(curParent(), childNode, removeExisting);
//
//          if (log.isDebugEnabled())
//            log.debug("copy node >> parent: "+curParent().getPath());
//
//          NodeCopyVisitor copyVisitor = new NodeCopyVisitor(newNode, false, false);
//          childNode.accept(copyVisitor);
//
//          parents.push(newNode); // [PN] 24.02.06 -- newNode
//        } else {
//          // 2. exists and not a child and removeEx = false (ItemExistsException)
//          throw new ItemExistsException("Node "+versionableUuid+" already exists "+childNode.getPath()+" and removeExisting==false");
//        }
//      } catch (ItemNotFoundException e) {
//        if (log.isDebugEnabled())
//          log.debug("versionable UUID not found >> "+versionableUuid+" "+node.getPath());
//        // 3. not exists
//        // gets last version (by time of creation)
//        VersionImpl lastVersion = (VersionImpl) history.getLastVersion();
//        NodeImpl frozen = (NodeImpl) lastVersion.getNode("jcr:frozenNode");
//
//        String ptName = frozen.getProperty("jcr:frozenPrimaryType").getString();
//        Value[] mtValues = frozen.getProperty("jcr:frozenMixinTypes").getValues();
//        String[] mtNames = new String[mtValues.length];
//        for(int i=0; i<mtNames.length; i++) {
//          mtNames[i] = mtValues[i].getString();
//        }
//        String uuid = frozen.getProperty("jcr:frozenUuid").getString(); // [PN] 26.02.06 did node
//
//        // [PN] 16.02.06
//        NodeImpl nodeImpl = (NodeImpl) node;
//        SessionImpl nodeSession = (SessionImpl) node.getSession();
//        TransientNodeData nodeData = (TransientNodeData) nodeImpl.getData();
//        InternalQName nodeQName = nodeData.getQName();
//        InternalQName ptQName = nodeSession.getLocationFactory().parseJCRName(ptName).getInternalName();
//        InternalQName[] mtQNames = new InternalQName[mtNames.length];
//        for (int i=0; i<mtNames.length; i++) {
//          String mtName = mtNames[i];
//          mtQNames[i] = nodeSession.getLocationFactory().parseJCRName(mtName).getInternalName();
//        }
//
//        // [PN] 04.08.06 Add autocreated items - true
//        NodeImpl restoredNode = curParent().createChildNodeInmemory(nodeQName, ptQName, mtQNames,
//            uuid, nodeImpl.getIndex(), false, false, true);
//
//        if (log.isDebugEnabled())
//          log.debug("frozen child node>> "+node.getPath()+" "+node.getPrimaryNodeType().getName());
//
//        RestoreVisitor childVisitor = new RestoreVisitor(restoredNode, removeExisting);
//        frozen.accept(childVisitor);
//
//        // [PN] 03.08.06 Add 'jcr:baseVersion', 'jcr:isCheckedOut' properties to the restored versioned child
//        restoredNode.createChildPropertyInmemory(
//            Constants.JCR_BASEVERSION,
//            PropertyType.REFERENCE,
//            new Value[] { restoredNode.getSession().getValueFactory().createValue(lastVersion) },
//            false,  // registerInDataManager
//            false); // doExternalValidation
//        //restoredNode.createChildProperty("jcr:baseVersion", new Value[] { restoredNode.getSession().getValueFactory().createValue(lastVersion) }, PropertyType.REFERENCE);
//
//        restoredNode.createChildPropertyInmemory(
//            Constants.JCR_ISCHECKEDOUT,
//            PropertyType.BOOLEAN,
//            new Value[] { restoredNode.getSession().getValueFactory().createValue(false) },
//            false,  // registerInDataManager
//            false); // doExternalValidation
//        //restoredNode.createChildProperty("jcr:isCheckedOut", new Value[] { restoredNode.getSession().getValueFactory().createValue(false) }, PropertyType.BOOLEAN);
//
//        parents.push(restoredNode);
//      }
//
//    } else { // ordinary node for copy under nt:frozenNode
//
//      // [PN] 10.04.06 In case of COPY - copy node, otherwise we don't
//      // 8.2.11.3 INITIALIZE; 8.2.11.4 COMPUTE
//      // On restore of VN, the C stored as its child will be ignored, and the
//      // current C in the workspace will be left unchanged.
//      if (log.isDebugEnabled())
//        log.debug("frozen child node>> "+node.getPath()+" "+node.getPrimaryNodeType().getName());
//      NodeImpl newNode = createChildNode(curParent(), (NodeImpl) node, removeExisting);
//      if (newNode != null) {
//        if (log.isDebugEnabled())
//          log.debug("frozen child node restored>> "+newNode.getPath()+" "+newNode.getPrimaryNodeType().getName());
//        parents.push(newNode);
//      }
//    }
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {

    // TODO what to do if REFERENCE property target doesn't exists in workspace

    if (currentNode() != null) {

      NodeData frozenParent = (NodeData) dataManager.getItemData(property.getParentUUID());

      InternalQName qname = property.getQPath().getName();

      if (ntManager.isNodeType(Constants.NT_FROZENNODE, frozenParent.getPrimaryTypeName()))
        if (qname.equals(Constants.JCR_FROZENPRIMARYTYPE)) {
          qname = Constants.JCR_PRIMARYTYPE;
        } else if (qname.equals(Constants.JCR_FROZENUUID)) {
          qname = Constants.JCR_UUID;
        } else if (qname.equals(Constants.JCR_FROZENMIXINTYPES)) {
          qname = Constants.JCR_MIXINTYPES;
        } else if (qname.equals(Constants.JCR_PRIMARYTYPE) || qname.equals(Constants.JCR_UUID) || qname.equals(Constants.JCR_MIXINTYPES)) {
          // skip these props, as they are a nt:frozenNode special props
          return;
        }

//      int action = ntManager.findPropertyDefinitions(
//          qname,
//          currentNode().getPrimaryTypeName(),
//          currentNode().getMixinTypeNames()).getAnyDefinition().getOnParentVersion();
      int action = ntManager.findPropertyDefinition(
          qname,
          currentNode().getPrimaryTypeName(),
          currentNode().getMixinTypeNames()).getOnParentVersion();

      if (log.isDebugEnabled()) {
        log.debug("Visit property " + property.getQPath().getAsString()
            + " " + currentNode().getQPath().getAsString() + " " + OnParentVersionAction.nameFromValue(action));
      }

      if (action == OnParentVersionAction.COPY || action == OnParentVersionAction.VERSION ||
          action == OnParentVersionAction.INITIALIZE || action == OnParentVersionAction.COMPUTE) {
        // In case of COPY, VERSION - copy property
        PropertyData tagetProperty = TransientPropertyData.createPropertyData(
            currentNode(),
            qname,
            property.getType(),
            property.isMultiValued(),
            property.getValues());

//        changes.add(new ItemState(tagetProperty,
//            (isCurrentExisting() && updateExisting ? ItemState.UPDATED : ItemState.ADDED), true, null));
        changes.add(ItemState.createAddedState(tagetProperty));

//      } else if (action == OnParentVersionAction.INITIALIZE || action == OnParentVersionAction.COMPUTE) {
        // 8.2.11.3 INITIALIZE; 8.2.11.4 COMPUTE
        //On restore of VN, the P stored as its child will be ignored, and the
        //current P in the workspace will be left unchanged.
//        InternalQPath existedPath = InternalQPath.makeChildPath(currentNode().getQPath(), qname);
//        PropertyData existedProperty = (PropertyData) dataManager.getItemData(existedPath);
//        if (existedProperty != null) {
//          PropertyData tagetProperty = TransientPropertyData.createPropertyData(
//              currentNode(),
//              qname,
//              existedProperty.getType(),
//              existedProperty.isMultiValued(),
//              existedProperty.getValues());
//          changes.add(ItemState.createAddedState(tagetProperty));
//        } else if (level > 0) {
//          // for jcr:frozenNode childs use frozen state (logic same as for COPY)
//          PropertyData tagetProperty = TransientPropertyData.createPropertyData(
//              currentNode(),
//              qname,
//              property.getType(),
//              property.isMultiValued(),
//              property.getValues());
//          changes.add(ItemState.createAddedState(tagetProperty));
//        }
//        PropertyData tagetProperty = TransientPropertyData.createPropertyData(
//            currentNode(),
//            qname,
//            property.getType(),
//            property.isMultiValued(),
//            property.getValues());
//        changes.add(ItemState.createAddedState(tagetProperty));
        // else - nothing to do, i.e. left unchanged

      } else if (log.isDebugEnabled()) {
        log.debug("Visit property " + property.getQPath().getAsString() + " HAS "
            + OnParentVersionAction.nameFromValue(action) + " action");
      }
    } else if (log.isDebugEnabled()) {
      log.debug("Visit property " + property.getQPath().getAsString() + " HAS NULL PARENT. Restore of this property is impossible.");
    }
  }

  @Override
  protected void leaving(NodeData frozen, int level) throws RepositoryException {

    InternalQName qname = frozen.getQPath().getName();

    if (qname.equals(Constants.JCR_FROZENNODE) && level == 0) {

      if (log.isDebugEnabled())
        log.debug("leaving jcr:frozenNode " + frozen.getQPath().getAsString());

      // post init of a restored node

      PropertyData baseVersion = TransientPropertyData.createPropertyData(
          restored,
          Constants.JCR_BASEVERSION,
          PropertyType.REFERENCE,
          false,
          new TransientValueData(frozen.getParentUUID()));

      PropertyData isCheckedOut = TransientPropertyData.createPropertyData(
          restored,
          Constants.JCR_ISCHECKEDOUT,
          PropertyType.BOOLEAN,
          false,
          new TransientValueData(false));

      NodeData existing = (NodeData) dataManager.getItemData(restored.getUUID());
      if (existing != null && !existing.getQPath().isDescendantOf(Constants.JCR_VERSION_STORAGE_PATH, false)) {
        // copy childs/properties with OnParentVersionAction.IGNORE to the restored node
        ItemDataCopyIgnoredVisitor copyIgnoredVisitor = new ItemDataCopyIgnoredVisitor(
            (NodeData) dataManager.getItemData(restored.getParentUUID()),
            restored.getQPath().getName(),
            ntManager,
            userSession.getTransientNodesManager(), // TODO to use transact manager
            changes); 

        existing.accept(copyIgnoredVisitor);
        changes.addAll(copyIgnoredVisitor.getItemAddStates());
      }

      changes.add(ItemState.createAddedState(baseVersion));
      changes.add(ItemState.createAddedState(isCheckedOut));
    }

    if (parents.size()<=0)
      log.error("Empty parents stack");

    parents.pop();
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }

  public SessionChangesLog getRestoreChanges() {
    return changes;
  }

  public NodeData getRestoreRoot() {
    return restored;
  }
}
