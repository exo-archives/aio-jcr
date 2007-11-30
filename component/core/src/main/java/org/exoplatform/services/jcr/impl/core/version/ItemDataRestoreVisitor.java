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
import org.exoplatform.services.jcr.impl.dataflow.ItemDataCopyVisitor;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.jcr.impl.dataflow.version.VersionHistoryDataHelper;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 *
 * 14.12.2006
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ItemDataRestoreVisitor extends ItemDataTraversingVisitor {

  private final Log log = ExoLogger.getLogger("jcr.ItemDataRestoreVisitor");

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
      // no REFERENCE validation here
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

  private ItemData findDelegated(String identifier) {
    if (delegatedChanges != null)
      for (ItemState state: delegatedChanges.getAllStates()) {
        if (state.getData().getIdentifier().equals(identifier))
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

    PropertyData frozenIdentifier = (PropertyData) dataManager.getItemData(frozen,new QPathEntry(Constants.JCR_FROZENUUID,0));
    
    String fidentifier = null;
    NodeData existing = null;
    // make new node from frozen
    try {
      fidentifier = new String(frozenIdentifier.getValues().get(0).getAsByteArray());
      NodeData sameIdentifierNodeRestored = (NodeData) findDelegated(fidentifier);
      if (sameIdentifierNodeRestored != null) {
        // already restored from delegated call, remove it as we interested in this version state
        deleteDelegated(sameIdentifierNodeRestored.getQPath());
      } else {
        NodeData sameIdentifierNode = (NodeData) dataManager.getItemData(fidentifier);
        if (sameIdentifierNode != null) {
          QPath sameIdentifierPath = sameIdentifierNode.getQPath();
          if (sameIdentifierPath.makeParentPath().equals(nodePath.makeParentPath()) && // same parent
              sameIdentifierPath.getName().equals(nodePath.getName()) ) { // same name

            if (sameIdentifierPath.getIndex() != nodePath.getIndex())
              // but different index, see below... fix it
              nodePath = QPath.makeChildPath(parentData.getQPath(), name, sameIdentifierPath.getIndex());

            // if it's a target node
            existing = sameIdentifierNode;

            // remove existed node, with validation
            ItemDataRemoveVisitor removeVisitor = new RemoveVisitor();
            removeVisitor.visit(existing);

            changes.addAll(removeVisitor.getRemovedStates());
          } else if (!sameIdentifierPath.isDescendantOf(nodePath, false)) {
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
              removeVisitor.visit(sameIdentifierNode);

              changes.addAll(removeVisitor.getRemovedStates());
            } else {
              throw new ItemExistsException("Item with the same UUID as restored node " + nodePath.getAsString()
                + " already exists and removeExisting=false. Existed " + sameIdentifierPath.getAsString()
                + " " + sameIdentifierNode.getIdentifier());
            }
          }
        }
      }
    } catch (IllegalStateException e) {
      throw new RepositoryException("jcr:frozenUuid, error of data read " + frozenIdentifier.getQPath().getAsString(), e);
    } catch (IOException e) {
      throw new RepositoryException("jcr:frozenUuid, error of data read " + frozenIdentifier.getQPath().getAsString(), e);
    }

    
    PropertyData frozenPrimaryType = (PropertyData) dataManager.getItemData(frozen,
        new QPathEntry(Constants.JCR_FROZENPRIMARYTYPE, 0));
    
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
        fidentifier,
        (existing != null ? existing.getPersistedVersion() : -1),
        ptName,
        mixins == null ? new InternalQName[0] : mixins,
        0, parentData.getIdentifier(), parentData.getACL());

    changes.add(ItemState.createAddedState(restoredData));

    pushCurrent(restoredData);
  }

  @Override
  protected void entering(NodeData frozen, int level) throws RepositoryException {

    if (frozen == null) { 
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

        String vhIdentifier = new String(
            ((PropertyData) dataManager.getItemData(frozen,new QPathEntry(Constants.JCR_CHILDVERSIONHISTORY,0))).getValues().get(0).getAsByteArray());

        NodeData cHistory = null;
        if ((cHistory = (NodeData) dataManager.getItemData(vhIdentifier)) == null)
          throw new RepositoryException("Version history is not found with uuid " + vhIdentifier);

        childHistory = new VersionHistoryDataHelper(cHistory, dataManager, ntManager);
      } catch (IllegalStateException e) {
        throw new RepositoryException("jcr:childVersionHistory, error of data read " + cvhpPropPath.getAsString(), e);
      } catch (IOException e) {
        throw new RepositoryException("jcr:childVersionHistory, error of data read " + cvhpPropPath.getAsString(), e);
      }

      String versionableIdentifier = null;
      try {
        versionableIdentifier = new String(
            ((PropertyData) dataManager.getItemData(childHistory,new QPathEntry(Constants.JCR_VERSIONABLEUUID,0))).getValues().get(0).getAsByteArray());
        
      } catch (IOException e) {
        throw new RepositoryException("jcr:childVersionHistory, error of data read " + cvhpPropPath.getAsString(), e);
      }

      NodeData versionable = (NodeData) dataManager.getItemData(versionableIdentifier);
      if (versionable != null) {
        // exists,
        // On restore of VN, if the workspace currently has an already
        // existing node corresponding to C’s version history and the
        // removeExisting flag of the restore is set to true, then that
        // instance of C becomes the child of the restored N.
        if (!removeExisting) {
          throw new ItemExistsException("Item with the same UUID " + versionableIdentifier
              + " as versionable child node " + versionable.getQPath().getAsString()
              + " already exists and removeExisting=false");
        }
        // else - leaving existed unchanged
      } else {
        // not found,
        // gets last version (by time of creation) and restore it
        NodeData lastVersionData = childHistory.getLastVersionData();
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
            jcrUuid = existing.getIdentifier();
          } else {
            jcrUuid = IdGenerator.generate();
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
              removeVisitor.visit(existing);

              changes.addAll(removeVisitor.getRemovedStates());
            } else {
              throw new ItemExistsException("Node with the same UUID as restored child node " + restoredPath.getAsString()
                  + " already exists and removeExisting=false. Existed " + existing.getQPath().getAsString()
                  + " " + existing.getIdentifier());
            }
          }
        }

        NodeData restoredData = new TransientNodeData(restoredPath,
            jcrUuid,
            frozen.getPersistedVersion(),
            frozen.getPrimaryTypeName(),
            frozen.getMixinTypeNames(),
            frozen.getOrderNumber(),
            currentNode().getIdentifier(), // parent
            frozen.getACL());

        changes.add(ItemState.createAddedState(restoredData));
        pushCurrent(restoredData);
      } else if (action == OnParentVersionAction.INITIALIZE || action == OnParentVersionAction.COMPUTE) {
        // current C in the workspace will be left unchanged,
        NodeData existed = (NodeData) dataManager.getItemData(currentNode(),new QPathEntry(frozen.getQPath().getName(),0));

        if (existed != null) {
          // copy existed - i.e. left unchanged
          ItemDataCopyVisitor copyVisitor = new ItemDataCopyVisitor(currentNode(), frozen.getQPath().getName(),
              ntManager, userSession.getTransientNodesManager(), true);
          existed.accept(copyVisitor);
          changes.addAll(copyVisitor.getItemAddStates());
        } // else - nothing to do, i.e. left unchanged

        pushCurrent(null); // JCR-193, skip any childs of that node now
      }
    }
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {

    // TODO what to do if REFERENCE property target doesn't exists in workspace

    if (currentNode() != null) {

      NodeData frozenParent = (NodeData) dataManager.getItemData(property.getParentIdentifier());

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
        
        PropertyData tagetProperty = null;
        if (qname.equals(Constants.JCR_PREDECESSORS)){
          tagetProperty = TransientPropertyData.createPropertyData(
            currentNode(),
            qname,
            property.getType(),
            property.isMultiValued(),
            new ArrayList<ValueData>());
        }else{
          tagetProperty = TransientPropertyData.createPropertyData(
            currentNode(),
            qname,
            property.getType(),
            property.isMultiValued(),
            property.getValues());
        }

        changes.add(ItemState.createAddedState(tagetProperty));

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
          new TransientValueData(frozen.getParentIdentifier()));

      PropertyData isCheckedOut = TransientPropertyData.createPropertyData(
          restored,
          Constants.JCR_ISCHECKEDOUT,
          PropertyType.BOOLEAN,
          false,
          new TransientValueData(false));

      NodeData existing = (NodeData) dataManager.getItemData(restored.getIdentifier());
      if (existing != null && !existing.getQPath().isDescendantOf(Constants.JCR_VERSION_STORAGE_PATH, false)) {
        // copy childs/properties with OnParentVersionAction.IGNORE to the restored node
        ItemDataCopyIgnoredVisitor copyIgnoredVisitor = new ItemDataCopyIgnoredVisitor(
            (NodeData) dataManager.getItemData(restored.getParentIdentifier()),
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
