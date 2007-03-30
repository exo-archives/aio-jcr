/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.version;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.jcr.ItemNotFoundException;
import javax.jcr.MergeException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: MergeVisitor.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class MergeVisitor extends TraversingItemVisitor {
  
  private static int NONE = -1;

  private static int LEAVE = 0;

  private static int FAIL = 1;

  private static int UPDATE = 2;

  private boolean bestEffort;

  private Map<String, String> failed;

  private Set<VersionableState> versionableStates;
  
  private SessionImpl destSession;
  
  private NodeImpl destNode;
  
  protected Stack<NodeImpl> parents;
  
  private List<ItemImpl> cleanupList = new ArrayList<ItemImpl>();


  public MergeVisitor(NodeImpl node, Map<String, String> failed, boolean bestEffort) {
    //super(context, true);
    this.destSession = (SessionImpl)node.getSession();
    //this.parent = parent;

    this.failed = failed;
    this.bestEffort = bestEffort;
    this.versionableStates = new TreeSet<VersionableState>(new VersionableStateComparator());
    this.parents = new Stack<NodeImpl>();
    parents.push(node);
  }

  protected void toLog(String message) {
    System.err.println(message);
  }
  
  /* (non-Javadoc) DONT WORK TRUE
   * @see javax.jcr.util.TraversingItemVisitor#visit(javax.jcr.Node)
   */
//  @Override
//  public void visit(Node node) throws RepositoryException {
//    
//    super.visit(node);
//    // Refresh both session and ws cache
//    for (ItemImpl cItem: getCleanupList()) {
//      SessionDataManager dataManager = 
//        ((SessionImpl) cItem.getSession()).getTransientNodesManager();
//      dataManager.rollbackCache(cItem.getLocation());
//      dataManager.removeFromCache(cItem);
//      dataManager.getWorkspaceDataManager().removeFromCache(cItem);
//    }
//  }


  private void cleanItemCache(ItemImpl item) throws RepositoryException {
    cleanupList.add(item);
  }

  protected void entering(Node node, int level) throws RepositoryException {
    

    //toLog("MergeVisitor entering node "+node.getPath()+" "+node.getSession().getWorkspace().getName());
    //createNode(node, level);
    try {
      destNode = ((NodeImpl)node).getCorrespondingNode(destSession);
      cleanItemCache(destNode);
    } catch (ItemNotFoundException e) {
      return;
    }
    
    // TODO (one more pass) 
    // If N (destNode) does not have a corresponding node then the merge
    // result for N is leave.

    if(destNode.isNodeType("mix:versionable")) {
      VersionImpl srcBase = (VersionImpl)node.getBaseVersion();
      VersionImpl destBase = (VersionImpl)destNode.getBaseVersion();
      //toLog("MERGE compare >>> " + destNode.getPath() + " " + bestEffort +
      //    " src ver:"+srcBase.getName()+" dest Ver:"+destBase.getName()+" "+destBase.isSuccessorOrSameOf(srcBase));
      
      if (destNode.isCheckedOut()) { // CHECKED-OUT
        if (destBase.getName().equals(srcBase.getName())
            || destBase.isSuccessorOrSameOf(srcBase)) {
          addNode(destNode, level);
          versionableStates.add(new VersionableState(destNode.getLocation(), LEAVE));
        } else {
          addNode(destNode, level);
          if (bestEffort) {
            failed.put(destNode.getUUID(), srcBase.getUUID());
          } else
            throw new MergeException("Merging of node " + node.getPath()
                + " failed");
          versionableStates.add(new VersionableState(destNode.getLocation(), FAIL));
        }
      } else { // CHECKED-IN
        if (srcBase.isSuccessorOrSameOf(destBase)) {
          addNode((NodeImpl) node, level);
          //super.entering(node, level);
          versionableStates.add(new VersionableState(destNode.getLocation(), UPDATE));
        } else if (destBase.getName().equals(srcBase.getName())
            || destBase.isSuccessorOrSameOf(srcBase)) {
          addNode(destNode, level);
          versionableStates.add(new VersionableState(destNode.getLocation(), LEAVE));
        } else {
          addNode(destNode, level);
          if (bestEffort) {
            failed.put(destNode.getUUID(), srcBase.getUUID());
          } else
            throw new MergeException("Merging of node " + node.getPath()
                + " failed");
          versionableStates.add(new VersionableState(destNode.getLocation(), FAIL));
        }
      }
    } else { // node is not versionable
      int nearestState = getNearestVersionableState(destNode);      
      if(nearestState == NONE || nearestState == UPDATE)
        addNode((NodeImpl)node, level);
        //super.entering(node, level);
      else
        addNode(destNode, level);
    }
  }

  protected void entering(Property property, int level)
      throws RepositoryException {
    
//    NodeImpl destPropertyParent = null;
//    try {
//      destPropertyParent = curParent().getCorrespondingNode(destSession);
//    } catch (ItemNotFoundException e) {
//      return;
//    }
    
    PropertyImpl propImpl = (PropertyImpl) property;      
        
    int nvs = getNearestVersionableState(propImpl);    
    if (nvs == UPDATE || nvs == NONE) {
      addProperty(propImpl);
      //toLog("MergeVisitor update property "+propImpl.dump()+" "+nvs);
    } else {      
      addProperty((PropertyImpl)destNode.getProperty(property.getName()));
      //toLog("MergeVisitor repeat property "+
      //    ((PropertyImpl)destNode.getProperty(property.getName())).dump()+" "+nvs);
    }
      
  }

  protected void leaving(Property property, int level)
      throws RepositoryException {
  }

  protected void leaving(Node node, int level) throws RepositoryException {
    parents.pop();
  }
  
  //private void createNode(Node node, int level) throws RepositoryException {
  //}
  
  private void addNode(NodeImpl node, int level) throws RepositoryException {
    //NodeImpl cParent;
    if(level != 0) {
      
      // [PN] 18.04.06 use actual data
      NodeData nodeData = (NodeData) node.getData();
      
      // [PN] 18.04.06 use existing node
      NodeImpl existingNode = null;
      String uuid = null; 
      try {
        existingNode = (NodeImpl) destSession.getTransientNodesManager().getItem(node.getLocation().getInternalPath(), true);
        //existingNode = (NodeImpl) curParent().getNode(node.getName()); // Node.getNode() don't work if node is new but exists (merge) 
        uuid = existingNode.getInternalUUID();
      } catch (PathNotFoundException e) {
        uuid = nodeData.getUUID();
      }      
      
//      NodeImpl newNode = curParent().createChildNodeInmemory(
//          nodeData.getQPath().getName(), 
//          nodeData.getPrimaryTypeName(),
//          nodeData.getMixinTypeNames(),
//          uuid,
//          nodeData.getQPath().getIndex(),
//        false, false, true); // [PN] 18.04.06 addAutoCreated=true
      throw new RepositoryException("createChildNodeInmemory");
      
//      if (existingNode != null) {
//        // In fact, existing node and new created does present the same item 
//        existingNode.loadData(newNode.getData());
//      }
//      
//      //toLog("MergeVisitor new node "+newNode.getPath()+" "+newNode.getSession().getWorkspace().getName());
//
//      parents.push(newNode);

    }
  }
  
  private void addProperty(PropertyImpl prop) throws RepositoryException {
    //curParent().createChildProperty(prop.getName(), 
    //    prop.getValueArray(), prop.getType());
    
//    // [PN] 21.02.06 prop.isMultiValued() used insteed prop.getDefinition().isMultiple()
//    curParent().createChildPropertyInmemory(prop.getData().getQPath().getName(),
//        prop.getType(), prop.getValueArray(), prop.isMultiValued(),
//        false, false); 

    throw new RepositoryException("createChildPropertyInmemory");
    //toLog("MergeVisitor CREATE property "+prop.dump() );
  } 

  private int getNearestVersionableState(ItemImpl item) throws RepositoryException {
    JCRPath path = item.getLocation();
    NodeImpl targetNode = curParent();
    if (item.isNode()) {      
      targetNode = (NodeImpl) item;
    } 
    if (!targetNode.isNodeType("mix:versionable")) {
      //toLog("  destNodeIsNotVersionable: " + " parent:" + targetNode.getSession().getWorkspace().getName() + ":" + targetNode.getPath() + " " + targetNode.getInternalUUID());
      //toLog("  destNodeIsNotVersionable: " + " item:" + item.getSession().getWorkspace().getName() + ":" + item.getPath() + " " + item.getInternalUUID());
      return UPDATE;
    }    
    
    for(VersionableState state: versionableStates) {  
      if(path.isDescendantOf(state.path, false))
        return state.result;
    }
    return NONE;
  }
  
  protected NodeImpl curParent() {
    return parents.peek();
  }

  private class VersionableState {
    private int result;

    private JCRPath path;

    private VersionableState(JCRPath path, int result) {
      this.path = path;
      this.result = result;
    }
  }
  
  private class VersionableStateComparator implements Comparator<VersionableState> {
    public int compare(VersionableState nc1, VersionableState nc2) {
      return nc1.path.getAsString(true).compareTo(nc2.path.getAsString(true));
    }
  }

  /**
   * @return Returns the cleanupList.
   */
  public List<ItemImpl> getCleanupList() {
    return cleanupList;
  }

}