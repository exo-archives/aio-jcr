/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.version;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.jcr.ItemNotFoundException;
import javax.jcr.MergeException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataCopyVisitor;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 *
 * 06.02.2007
 *
 * Traverse through merging nodes (destenation) and do merge to correspondent version states.     
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ItemDataMergeVisitor.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class ItemDataMergeVisitor extends ItemDataTraversingVisitor {

  protected static int NONE = -1;
  protected static int LEAVE = 0;
  protected static int FAIL = 1;
  protected static int UPDATE = 2;  
  
  protected static Log log = ExoLogger.getLogger("jcr.ItemDataMergeVisitor");  
  
  //protected final NodeData mergeNode;
  protected final SessionImpl mergeSession;
  protected final SessionImpl corrSession;
  protected final Map<String, String> failed;
  protected final boolean bestEffort;
  
  protected final Stack<ContextParent> parents = new Stack<ContextParent>();
  protected final SessionChangesLog changes;
  
  //protected final Set<VersionableState> versionableStates = new TreeSet<VersionableState>(new VersionableStateComparator());
  
  //protected final Map<InternalQPath, List<NodeData>> corrChildNodes = new HashMap<InternalQPath, List<NodeData>>();
  //protected final Map<InternalQPath, List<PropertyData>> corrChildProperties = new HashMap<InternalQPath, List<PropertyData>>();
  
  private class VersionableState {
    private final int result;

    private final QPath path;

    private VersionableState(QPath path, int result) {
      this.path = path;
      this.result = result;
    }

    public int getResult() {
      return result;
    }

    public QPath getPath() {
      return path;
    }
  }
  
  protected class RemoveVisitor extends ItemDataRemoveVisitor {
    
    RemoveVisitor() {
      super(mergeSession, true);
    }

    protected void validateReferential(NodeData node) throws RepositoryException {
      // TODO no REFERENCE validation here
    }
  };
  
  private class ContextParent {
    private final NodeData parent;
    private final List<NodeData> corrChildNodes;
    private final int result;

    private ContextParent(NodeData parent, List<NodeData> corrChildNodes, int result) {
      this.parent = parent;
      this.corrChildNodes = corrChildNodes;
      this.result = result;
    }
    
    private ContextParent(NodeData parent, int result) {
      this(parent, null, result);
    }

    public NodeData getParent() {
      return parent;
    }

    public List<NodeData> getCorrChildNodes() {
      return corrChildNodes;
    }

    public int getResult() {
      return result;
    }
  }
  
  private class VersionableStateComparator implements Comparator<VersionableState> {
    public int compare(VersionableState nc1, VersionableState nc2) {
      return nc1.getPath().compareTo(nc2.getPath());
    }
  }
  
  public ItemDataMergeVisitor(SessionImpl mergeSession, SessionImpl corrSession, Map<String, String> failed, boolean bestEffort) {
    super(mergeSession.getTransientNodesManager().getTransactManager());
    
    this.corrSession = corrSession;
    this.mergeSession = mergeSession;
    //this.mergeNode = mergeNode;
    this.bestEffort = bestEffort;
    this.failed = failed;
    
    this.changes = new SessionChangesLog(mergeSession.getId());
  }

  @Override
  protected void entering(NodeData mergeNode, int level) throws RepositoryException {

//    if (level == 0) {
//      // init merge stack
//      parents.push(mergeNode);
//    }
    
    if (level == 0) {
      // initial - merge root node
      doMerge((TransientNodeData) mergeNode);
    } else if (parents.size()>0) {
      ContextParent context = parents.peek();
      if (context.getResult() == UPDATE) {
        // doUpdate() work...        
        if (context.getCorrChildNodes().remove(mergeNode)) {
          // let C be the set of nodes in S and in S'
          // for each child node m of n in C domerge(m).
          doMerge((TransientNodeData) mergeNode);
        } else {
          // let D be the set of nodes in S but not in S'.
          // remove from n all child nodes in D.
          changes.add(
            new ItemState(((TransientNodeData) mergeNode).clone(), 
                ItemState.DELETED, true, context.getParent().getQPath(), true)
            );
        }
      } else if (context.getResult() == LEAVE) {
        // doLeave() work...        
        // for each child node c of n domerge(c).
        doMerge((TransientNodeData) mergeNode);
      } else {
        // impossible...     
        log.warn("Result is undefined for merge node " + mergeNode.getQPath().getAsString());
      }
    } else {
      log.warn("Has no parent for merge node " + mergeNode.getQPath().getAsString());
    }
  }
  
//protected void entering(Node node, int level) throws RepositoryException {
//    
//
//    //toLog("MergeVisitor entering node "+node.getPath()+" "+node.getSession().getWorkspace().getName());
//    //createNode(node, level);
//    try {
//      destNode = ((NodeImpl)node).getCorrespondingNode(destSession);
//      cleanItemCache(destNode);
//    } catch (ItemNotFoundException e) {
//      return;
//    }
//    
//    // TODO (one more pass) 
//    // If N (destNode) does not have a corresponding node then the merge
//    // result for N is leave.
//
//    if(destNode.isNodeType("mix:versionable")) {
//      VersionImpl srcBase = (VersionImpl)node.getBaseVersion();
//      VersionImpl destBase = (VersionImpl)destNode.getBaseVersion();
//      //toLog("MERGE compare >>> " + destNode.getPath() + " " + bestEffort +
//      //    " src ver:"+srcBase.getName()+" dest Ver:"+destBase.getName()+" "+destBase.isSuccessorOrSameOf(srcBase));
//      
//      if (destNode.isCheckedOut()) { // CHECKED-OUT
//        if (destBase.getName().equals(srcBase.getName())
//            || destBase.isSuccessorOrSameOf(srcBase)) {
//          addNode(destNode, level);
//          versionableStates.add(new VersionableState(destNode.getLocation(),
//              LEAVE));
//        } else {
//          addNode(destNode, level);
//          if (bestEffort) {
//            failed.put(destNode.getUUID(), srcBase.getUUID());
//          } else
//            throw new MergeException("Merging of node " + node.getPath()
//                + " failed");
//          versionableStates.add(new VersionableState(destNode.getLocation(),
//              FAIL));
//        }
//      } else { // CHECKED-IN
//        if (srcBase.isSuccessorOrSameOf(destBase)) {
//          addNode((NodeImpl) node, level);
//          //super.entering(node, level);
//          versionableStates.add(new VersionableState(destNode.getLocation(),
//              UPDATE));
//        } else if (destBase.getName().equals(srcBase.getName())
//            || destBase.isSuccessorOrSameOf(srcBase)) {
//          addNode(destNode, level);
//          versionableStates.add(new VersionableState(destNode.getLocation(),
//              LEAVE));
//        } else {
//          addNode(destNode, level);
//          if (bestEffort) {
//            failed.put(destNode.getUUID(), srcBase.getUUID());
//          } else
//            throw new MergeException("Merging of node " + node.getPath()
//                + " failed");
//          versionableStates.add(new VersionableState(destNode.getLocation(),
//              FAIL));
//        }
//      }
//    } else { // node is not versionable
//      int nearestState = getNearestVersionableState(destNode);      
//      if(nearestState == NONE || nearestState == UPDATE)
//        addNode((NodeImpl)node, level);
//        //super.entering(node, level);
//      else
//        addNode(destNode, level);
//    }
//  }  

  
  @Override
  protected void entering(PropertyData mergeProperty, int level) throws RepositoryException {
    // remove any property, merged will be added in doMerge() --> doUpdate()
    
    // check if need to remove
//    final int state = getNearestVersionableState(mergeProperty);
//    if (state == UPDATE || state == NONE) {
//      changes.add(
//          new ItemState(((TransientPropertyData) mergeProperty).clone(), 
//              ItemState.DELETED, true, parents.peek().getQPath(), true)
//          );      
//    } 
  }

  @Override
  protected void leaving(PropertyData mergeProperty, int level) throws RepositoryException {
  }

  @Override
  protected void leaving(NodeData mergeNode, int level) throws RepositoryException {
    //parents.pop();
    //corrChildProperties.remove(mergedNode.getQPath());
    //corrChildNodes.remove(mergedNode.getQPath());
    
    if (parents.size()>0) {
      ContextParent context = parents.pop();
      if (context.getResult() == UPDATE) {
        // for each child node of n' in D' copy it (and its subtree) to n
        // as a new child node (if an incoming node has the same
        //    UUID as a node already existing in this workspace,
        //    the already existing node is removed).        
        SessionDataManager mergeDataManager = mergeSession.getTransientNodesManager();
        for (NodeData corrNode: context.getCorrChildNodes()) {
          TransientNodeData existedSameUUID = (TransientNodeData) mergeDataManager.getItemData(corrNode.getUUID());
          if (existedSameUUID != null) {
            //  if an incoming node has the same
            //  UUID as a node already existing in this workspace,
            //  the already existing node is removed
            
            //changes.add(new ItemState(existedSameUUID.clone(), ItemState.DELETED, true, context.getParent().getQPath(), true));
            
            RemoveVisitor remover = new RemoveVisitor();
            existedSameUUID.accept(remover);
            
            changes.addAll(remover.getRemovedStates());
          }
    
          //changes.add(new ItemState(((TransientNodeData) corrNode).clone(), ItemState.ADDED, true, context.getParent().getQPath(), true));
          
          ItemDataCopyVisitor copier = new ItemDataCopyVisitor(context.getParent(), 
              corrNode.getQPath().getName(), 
              mergeSession.getWorkspace().getNodeTypeManager(),
              mergeDataManager, true);      
          corrNode.accept(copier);
          
          changes.addAll(copier.getItemAddStates());
        }
      }
    }
  }
  
  // -------------------- merge actions ------------
  
  protected void doMerge(TransientNodeData mergeNode) throws RepositoryException {
    // let n' be the corresponding node of n in ws'.
    // find corr node for this node
    TransientNodeData corrNode = getCorrNodeData(mergeNode);
    if (corrNode != null) {
      
      TransientNodeData mergeVersion = getBaseVersionData(mergeNode, mergeSession);
      
      if (mergeVersion != null) {
      
        TransientNodeData corrVersion = getBaseVersionData(corrNode, corrSession);
        
        if (corrVersion != null) {
          // let v be base version of n.
          // let v' be base version of n'.
          
          SessionDataManager mergeDataManager = mergeSession.getTransientNodesManager();
          
//          PropertyData isCheckedOutProperty = (PropertyData) mergeDataManager.getItemData(
//              QPath.makeChildPath(mergeNode.getQPath(), Constants.JCR_ISCHECKEDOUT));
          PropertyData isCheckedOutProperty = (PropertyData) mergeDataManager.getItemData(mergeNode,new QPathEntry(Constants.JCR_ISCHECKEDOUT,0));
          
          try {
            if (!Boolean.valueOf(new String(isCheckedOutProperty.getValues().get(0).getAsByteArray()))
                && isSuccessor(mergeVersion, corrVersion)) {
              // if v' is a successor of v and
              // n is not checked-in doupdate(n, n'). 
              doUpdate(mergeNode, corrNode);
            } else if (mergeVersion.getQPath().equals(corrVersion.getQPath()) 
                || isPredecessor(mergeVersion, corrVersion)) {
              // else if v is equal to or a predecessor of v' doleave(n).
              doLeave(mergeNode);
            } else {
              // else dofail(n, v').
              doFail(mergeNode, corrVersion);
            }
          } catch (IOException e) {
            throw new RepositoryException("Merge. Get isCheckedOut error " + e.getMessage(), e);
          }
        } else {
          // else if n' is not versionable doleave(n)
          doLeave(mergeNode);
        }
      } else {
        // else if n is not versionable doupdate(n, n')
        doUpdate(mergeNode, corrNode);
      }
    } else {
      // if no such n' doleave(n).
      doLeave(mergeNode);
    }
  }
  
  protected void doLeave(TransientNodeData mergeNode) throws RepositoryException {
    // for each child node c of n domerge(c).
    // ...back to visitor
    //versionableStates.add(new VersionableState(mergeNode.getQPath(), LEAVE));
    
    parents.push(new ContextParent(mergeNode, LEAVE));
  }
  
  protected void doUpdate(TransientNodeData mergeNode, TransientNodeData corrNode) throws RepositoryException {

    DataManager mergeDataManager = mergeSession.getTransientNodesManager().getTransactManager();
    
    QPath mergePath = mergeNode.getQPath();
    
    // InternalQPath path, String uuid, int version, InternalQName primaryTypeName, InternalQName[] mixinTypeNames,
    // int orderNum, String parentUUID, AccessControlList acl
    TransientNodeData mergedNode = new TransientNodeData(
        mergePath, 
        mergeNode.getUUID(), 
        mergeNode.getPersistedVersion(),
        corrNode.getPrimaryTypeName(),
        corrNode.getMixinTypeNames(),
        mergeNode.getOrderNumber(),
        mergeNode.getParentUUID(),
        mergeNode.getACL());
    
    if (!mergeNode.getUUID().equals(corrNode.getUUID())) {
      // [PN] 12.02.07 fix for update work 
      //mergedNode.setUUID(mergeNode.getUUID()); // use uuid of the node already existed in merge workspace
      
      TransientNodeData existedSameUUID = (TransientNodeData) mergeDataManager.getItemData(corrNode.getUUID());
      if (existedSameUUID != null) {
        //  if an incoming node has the same
        //  UUID as a node already existing in this workspace,
        //  the already existing node is removed
        
        //changes.add(new ItemState(existedSameUUID.clone(), ItemState.DELETED, true, mergeNode.getQPath(), true));
        RemoveVisitor remover = new RemoveVisitor();
        existedSameUUID.accept(remover);
        
        changes.addAll(remover.getRemovedStates());
      }
    }
    
    changes.add(new ItemState(mergedNode, ItemState.UPDATED, true, mergeNode.getQPath(), true));
    
    // replace set of properties of n with those of n'.
    DataManager corrDataManager = corrSession.getTransientNodesManager().getTransactManager();
    List<PropertyData> corrChildProps = corrDataManager.getChildPropertiesData(corrNode);
    List<PropertyData> mergeChildProps = mergeDataManager.getChildPropertiesData(mergeNode);
    
    Map<InternalQName, PropertyData> existedProps = new HashMap<InternalQName, PropertyData>(); 
    for (PropertyData cp : mergeChildProps) {
      TransientPropertyData existed = ((TransientPropertyData) cp).clone();
      changes.add(new ItemState(existed, ItemState.DELETED, true, mergedNode.getQPath(), true));
      
      existedProps.put(existed.getQPath().getName(), existed);
    }
    
    for (PropertyData cp : corrChildProps) {
      // InternalQPath path, String uuid, int version, int type, String parentUUID, boolean multiValued
      PropertyData existed = existedProps.get(cp.getQPath().getName());
      TransientPropertyData mcp = new TransientPropertyData(
              QPath.makeChildPath(mergePath, cp.getQPath().getName()),
              existed != null ? existed.getUUID() : cp.getUUID(),
              existed != null ? existed.getPersistedVersion() : cp.getPersistedVersion(),
              cp.getType(), 
              mergedNode.getUUID(), 
              cp.isMultiValued());
      mcp.setValues(cp.getValues());
      
      changes.add(new ItemState(mcp, ItemState.ADDED, true, mergedNode.getQPath(), true));
    }
    
    List<NodeData> childNodes = corrDataManager.getChildNodesData(corrNode);
    parents.push(new ContextParent(mergedNode, childNodes, UPDATE));
    
    // let S be the set of child nodes of n.
    // let S' be the set of child nodes of n'.
    // judging by the name of the child node:
    //  let C be the set of nodes in S and in S'
    //  let D be the set of nodes in S but not in S'.
    //  let D' be the set of nodes in S' but not in S.
    // remove from n all child nodes in D.  <<< will occurs in doMerge() on particular child
    // for each child node of n' in D' copy it (and its subtree) to n
    //  as a new child node (if an incoming node has the same
    //  UUID as a node already existing in this workspace,
    //  the already existing node is removed)  <<< will occurs in doMerge() on particular child
    
    // for each child node m of n in C domerge(m).
  }
  
  protected void doFail(TransientNodeData mergeNode, TransientNodeData corrVersion) throws RepositoryException {
    if (bestEffort) {
      // else add UUID of v' (if not already present) to the
      // jcr:mergeFailed property of n,
      // add UUID of n to failedset,
      // doleave(n).
      failed.put(mergeNode.getUUID(), corrVersion.getUUID());
      //versionableStates.add(new VersionableState(mergeNode.getQPath(), FAIL));
      doLeave(mergeNode);
    } else {
      // if bestEffort = false throw MergeException      
      throw new MergeException("Merging of node " 
          + mergeSession.getLocationFactory().createJCRPath(mergeNode.getQPath()).getAsString(false)
          + " failed");
    }
  }

  // -------------------- utils --------------------
  
//  private int getNearestVersionableState(ItemData mergeItem) throws RepositoryException {
//    
//    NodeData targetNode = null;
//    if (mergeItem.isNode()) {      
//      targetNode = (NodeData) mergeItem;
//    } else {
//      targetNode = parents.peek().getParent();
//    }
//    
//    NodeTypeManagerImpl mergeNtManager = mergeSession.getWorkspace().getNodeTypeManager();
//    if (!mergeNtManager.isNodeType(Constants.MIX_REFERENCEABLE, 
//        targetNode.getPrimaryTypeName(), 
//        targetNode.getMixinTypeNames())) {
//      return UPDATE;
//    }
//    
//    final InternalQPath path = mergeItem.getQPath();
//    for(VersionableState state: versionableStates) {  
//      if(path.isDescendantOf(state.getPath(), false))
//        return state.getResult();
//    }
//    
//    return NONE;
//  }  
  
  protected TransientNodeData getBaseVersionData(final TransientNodeData node, final SessionImpl session) throws RepositoryException {
    
    NodeTypeManagerImpl ntManager = session.getWorkspace().getNodeTypeManager();
    if (ntManager.isNodeType(Constants.MIX_VERSIONABLE, 
        node.getPrimaryTypeName(), 
        node.getMixinTypeNames())) {

      SessionDataManager dmanager = session.getTransientNodesManager();
      
//      PropertyData bvProperty = (PropertyData) dmanager.getItemData(
//          QPath.makeChildPath(node.getQPath(), Constants.JCR_BASEVERSION));
    PropertyData bvProperty = (PropertyData) dmanager.getItemData(node,
          new QPathEntry(Constants.JCR_BASEVERSION, 0));
      
      try {
        return (TransientNodeData) dmanager.getItemData(
          new String(bvProperty.getValues().get(0).getAsByteArray()));
      } catch(IOException e) {
        throw new RepositoryException("Merge. Get base version error " + e.getMessage(), e);
      }
    } 
    
    return null; // non versionable
  }
  
  protected TransientNodeData getCorrNodeData(final TransientNodeData mergeNode) throws RepositoryException {

    final QPath mergePath = mergeNode.getQPath();
    
    SessionDataManager corrDataManager = corrSession.getTransientNodesManager();
    SessionDataManager mergeDataManager = mergeSession.getTransientNodesManager();
    NodeTypeManagerImpl mergeNtManager = mergeSession.getWorkspace().getNodeTypeManager();

    if (mergeNtManager.isNodeType(Constants.MIX_REFERENCEABLE, 
        mergeNode.getPrimaryTypeName(), 
        mergeNode.getMixinTypeNames())) {
      // by UUID
      return (TransientNodeData) corrDataManager.getItemData(mergeNode.getUUID());
    }
    
    // by location
    //for (int i = mergePath.getDepth(); i >= 0; i--) {
    for (int i = 1; i <= mergePath.getDepth(); i++) {
      final QPath ancesstorPath = mergePath.makeAncestorPath(i);
      NodeData mergeAncestor = (NodeData) mergeDataManager.getItemData(ancesstorPath);
      if (mergeAncestor != null && mergeNtManager.isNodeType(Constants.MIX_REFERENCEABLE,
          mergeAncestor.getPrimaryTypeName(), 
          mergeAncestor.getMixinTypeNames())) {

        NodeData corrAncestor = (NodeData) corrDataManager.getItemData(mergeAncestor.getUUID());
        if (corrAncestor != null) {
          QPathEntry[] relQPathEntries = mergePath.getRelPath(mergePath.getDepth() - i);
          QPath corrNodeQPath = QPath.makeChildPath(corrAncestor.getQPath(), relQPathEntries);
          return (TransientNodeData) corrDataManager.getItemData(corrNodeQPath);
        }
      }
    }

    return (TransientNodeData) corrDataManager.getItemData(mergePath);
//    NodeData ancestor = (NodeData) dataManager.getItemData(Constants.ROOT_UUID);
//    for (int i = 1; i < mergePath.getDepth(); i++) {
//      ancestor = (NodeData) dataManager.getItemData(ancestor, mergePath.getEntries()[i]);
//      if (corrSession.getWorkspace().getNodeTypeManager().isNodeType(Constants.MIX_REFERENCEABLE,
//          ancestor.getPrimaryTypeName(),
//          ancestor.getMixinTypeNames())) {
//        NodeData corrAncestor = (NodeData) corrDataManager.getItemData(ancestor.getUUID());
//        if (corrAncestor == null)
//          throw new ItemNotFoundException("No corresponding path for ancestor "
//              + ancestor.getQPath().getAsString() + " in " + corrSession.getWorkspace().getName());
//
//        NodeData corrNode = (NodeData) corrDataManager.getItemData(corrAncestor, mergePath
//            .getRelPath(mergePath.getDepth() - i));
//        if (corrNode != null)
//          return (TransientNodeData)corrNode;
//      }
//    }
//  
//  NodeData corrRoot = (NodeData) corrDataManager.getItemData(Constants.ROOT_UUID);
//  return (TransientNodeData) corrDataManager.getItemData(corrRoot,mergePath);
//  if (corrNode != null)
//    return (TransientNodeData)corrNode;
    

  }    
  
  /**
   * Is a predecessor of the merge version 
   */
  protected boolean isPredecessor(TransientNodeData mergeVersion, TransientNodeData corrVersion) throws RepositoryException {
    SessionDataManager mergeDataManager = mergeSession.getTransientNodesManager();
    
//    PropertyData predecessorsProperty = (PropertyData) mergeDataManager.getItemData(
//        QPath.makeChildPath(mergeVersion.getQPath(), Constants.JCR_PREDECESSORS));
    PropertyData predecessorsProperty = (PropertyData) mergeDataManager.getItemData(mergeVersion,
        new QPathEntry(Constants.JCR_PREDECESSORS, 0));
    
    if (predecessorsProperty != null)
      for (ValueData pv: predecessorsProperty.getValues()) {
        try {
          String puuid = new String(pv.getAsByteArray());
          
          if (puuid.equals(corrVersion.getUUID())) 
            return true; // got it
          
          // search in predecessors of the predecessor
          TransientNodeData predecessor = (TransientNodeData) mergeDataManager.getItemData(puuid);
          if (predecessor != null) {
            if (isPredecessor(predecessor, corrVersion)) {
              return true;
            }
          } else {
            throw new RepositoryException("Merge. Predecessor is not found by uuid " + puuid + ". Version " 
                + mergeSession.getLocationFactory().createJCRPath(mergeVersion.getQPath()).getAsString(false));
          }
        } catch(IOException e) {
          throw new RepositoryException("Merge. Get predecessors error " + e.getMessage(), e);
        }
      }
    // else it's a root version
    
    return false;
  }
  
  /**
   * Is a successor of the merge version 
   */
  protected boolean isSuccessor(TransientNodeData mergeVersion, TransientNodeData corrVersion) throws RepositoryException {
    SessionDataManager mergeDataManager = mergeSession.getTransientNodesManager();
    
//    PropertyData successorsProperty = (PropertyData) mergeDataManager.getItemData(
//        QPath.makeChildPath(mergeVersion.getQPath(), Constants.JCR_SUCCESSORS));
  PropertyData successorsProperty = (PropertyData) mergeDataManager.getItemData(mergeVersion,
        new QPathEntry(Constants.JCR_SUCCESSORS, 0));
    
    if (successorsProperty != null)
      for (ValueData sv: successorsProperty.getValues()) {
        try {
          String suuid = new String(sv.getAsByteArray());
          
          if (suuid.equals(corrVersion.getUUID())) 
            return true; // got it
          
          // search in successors of the successor
          TransientNodeData successor = (TransientNodeData) mergeDataManager.getItemData(suuid);
          if (successor != null) {
            if (isSuccessor(successor, corrVersion)) {
              return true;
            }
          } else {
            throw new RepositoryException("Merge. Ssuccessor is not found by uuid " + suuid + ". Version " 
                + mergeSession.getLocationFactory().createJCRPath(mergeVersion.getQPath()).getAsString(false));
          }
        } catch(IOException e) {
          throw new RepositoryException("Merge. Get successors error " + e.getMessage(), e);
        }
      }
    // else it's a end of version graph node
    
    return false;
  }

  public SessionChangesLog getMergeChanges() {
    return changes;
  }
}
