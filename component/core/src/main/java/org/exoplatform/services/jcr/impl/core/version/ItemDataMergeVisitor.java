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

import javax.jcr.MergeException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
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
 * @version $Id$
 */
public class ItemDataMergeVisitor extends ItemDataTraversingVisitor {

  protected static int NONE = -1;
  protected static int LEAVE = 0;
  protected static int FAIL = 1;
  protected static int UPDATE = 2;  
  
  protected static Log log = ExoLogger.getLogger("jcr.ItemDataMergeVisitor");  
  
  protected final SessionImpl mergeSession;
  protected final SessionImpl corrSession;
  protected final Map<String, String> failed;
  protected final boolean bestEffort;
  
  protected final Stack<ContextParent> parents = new Stack<ContextParent>();
  protected final SessionChangesLog changes;
  
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
      // no REFERENCE validation here
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
    this.bestEffort = bestEffort;
    this.failed = failed;
    
    this.changes = new SessionChangesLog(mergeSession.getId());
  }

  @Override
  protected void entering(NodeData mergeNode, int level) throws RepositoryException {

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
  
  @Override
  protected void entering(PropertyData mergeProperty, int level) throws RepositoryException {
    // remove any property, merged will be added in doMerge() --> doUpdate()
  }

  @Override
  protected void leaving(PropertyData mergeProperty, int level) throws RepositoryException {
  }

  @Override
  protected void leaving(NodeData mergeNode, int level) throws RepositoryException {
    if (parents.size()>0) {
      ContextParent context = parents.pop();
      if (context.getResult() == UPDATE) {
        // for each child node of n' in D' copy it (and its subtree) to n
        // as a new child node (if an incoming node has the same
        //    UUID as a node already existing in this workspace,
        //    the already existing node is removed).        
        SessionDataManager mergeDataManager = mergeSession.getTransientNodesManager();
        for (NodeData corrNode: context.getCorrChildNodes()) {
          TransientNodeData existedSameIdentifier = (TransientNodeData) mergeDataManager.getItemData(corrNode.getIdentifier());
          if (existedSameIdentifier != null) {
            //  if an incoming node has the same
            //  UUID as a node already existing in this workspace,
            //  the already existing node is removed
            
            RemoveVisitor remover = new RemoveVisitor();
            existedSameIdentifier.accept(remover);
            
            changes.addAll(remover.getRemovedStates());
          }
    
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
    parents.push(new ContextParent(mergeNode, LEAVE));
  }
  
  protected void doUpdate(TransientNodeData mergeNode, TransientNodeData corrNode) throws RepositoryException {

    DataManager mergeDataManager = mergeSession.getTransientNodesManager().getTransactManager();
    
    QPath mergePath = mergeNode.getQPath();
    
    TransientNodeData mergedNode = new TransientNodeData(
        mergePath, 
        mergeNode.getIdentifier(), 
        mergeNode.getPersistedVersion(),
        corrNode.getPrimaryTypeName(),
        corrNode.getMixinTypeNames(),
        mergeNode.getOrderNumber(),
        mergeNode.getParentIdentifier(),
        mergeNode.getACL());
    
    if (!mergeNode.getIdentifier().equals(corrNode.getIdentifier())) {
      
      TransientNodeData existedSameIdentifier = (TransientNodeData) mergeDataManager.getItemData(corrNode.getIdentifier());
      if (existedSameIdentifier != null) {
        //  if an incoming node has the same
        //  UUID as a node already existing in this workspace,
        //  the already existing node is removed
        
        RemoveVisitor remover = new RemoveVisitor();
        existedSameIdentifier.accept(remover);
        
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
      PropertyData existed = existedProps.get(cp.getQPath().getName());
      TransientPropertyData mcp = new TransientPropertyData(
              QPath.makeChildPath(mergePath, cp.getQPath().getName()),
              existed != null ? existed.getIdentifier() : cp.getIdentifier(),
              existed != null ? existed.getPersistedVersion() : cp.getPersistedVersion(),
              cp.getType(), 
              mergedNode.getIdentifier(), 
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
      failed.put(mergeNode.getIdentifier(), corrVersion.getIdentifier());
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
    
  protected TransientNodeData getBaseVersionData(final TransientNodeData node, final SessionImpl session) throws RepositoryException {
    
    NodeTypeManagerImpl ntManager = session.getWorkspace().getNodeTypeManager();
    if (ntManager.isNodeType(Constants.MIX_VERSIONABLE, 
        node.getPrimaryTypeName(), 
        node.getMixinTypeNames())) {

      SessionDataManager dmanager = session.getTransientNodesManager();
      
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
      return (TransientNodeData) corrDataManager.getItemData(mergeNode.getIdentifier());
    }
    
    // by location
    for (int i = 1; i <= mergePath.getDepth(); i++) {
      final QPath ancesstorPath = mergePath.makeAncestorPath(i);
      NodeData mergeAncestor = (NodeData) mergeDataManager.getItemData(ancesstorPath);
      if (mergeAncestor != null && mergeNtManager.isNodeType(Constants.MIX_REFERENCEABLE,
          mergeAncestor.getPrimaryTypeName(), 
          mergeAncestor.getMixinTypeNames())) {

        NodeData corrAncestor = (NodeData) corrDataManager.getItemData(mergeAncestor.getIdentifier());
        if (corrAncestor != null) {
          QPathEntry[] relPathEntries = mergePath.getRelPath(mergePath.getDepth() - i);
          return (TransientNodeData) corrDataManager.getItemData(corrAncestor, relPathEntries);
        }
      }
    }

    return (TransientNodeData) corrDataManager.getItemData(mergePath);
  }   
 
  /**
   * Is a predecessor of the merge version 
   */
  protected boolean isPredecessor(TransientNodeData mergeVersion, TransientNodeData corrVersion) throws RepositoryException {
    SessionDataManager mergeDataManager = mergeSession.getTransientNodesManager();
    
    PropertyData predecessorsProperty = (PropertyData) mergeDataManager.getItemData(mergeVersion,
        new QPathEntry(Constants.JCR_PREDECESSORS, 0));
    
    if (predecessorsProperty != null)
      for (ValueData pv: predecessorsProperty.getValues()) {
        try {
          String pidentifier = new String(pv.getAsByteArray());
          
          if (pidentifier.equals(corrVersion.getIdentifier())) 
            return true; // got it
          
          // search in predecessors of the predecessor
          TransientNodeData predecessor = (TransientNodeData) mergeDataManager.getItemData(pidentifier);
          if (predecessor != null) {
            if (isPredecessor(predecessor, corrVersion)) {
              return true;
            }
          } else {
            throw new RepositoryException("Merge. Predecessor is not found by uuid " + pidentifier + ". Version " 
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
    
  PropertyData successorsProperty = (PropertyData) mergeDataManager.getItemData(mergeVersion,
        new QPathEntry(Constants.JCR_SUCCESSORS, 0));
    
    if (successorsProperty != null)
      for (ValueData sv: successorsProperty.getValues()) {
        try {
          String sidentifier = new String(sv.getAsByteArray());
          
          if (sidentifier.equals(corrVersion.getIdentifier())) 
            return true; // got it
          
          // search in successors of the successor
          TransientNodeData successor = (TransientNodeData) mergeDataManager.getItemData(sidentifier);
          if (successor != null) {
            if (isSuccessor(successor, corrVersion)) {
              return true;
            }
          } else {
            throw new RepositoryException("Merge. Ssuccessor is not found by uuid " + sidentifier + ". Version " 
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
