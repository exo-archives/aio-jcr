/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.Calendar;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.SharedDataManager;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * Data Manager supported ACL Inheritance
 * @author Gennady Azarenkov
 * @version $Id$
 */
public class ACLInheritanceSupportedWorkspaceDataManager implements SharedDataManager {

  private static Log log = ExoLogger.getLogger("jcr.ACLInheritanceSupportedWorkspaceDataManager");
  
  private final CacheableWorkspaceDataManager persistentManager;
  
  public ACLInheritanceSupportedWorkspaceDataManager(CacheableWorkspaceDataManager persistentManager) {
    this.persistentManager = persistentManager;
  }
  
  /**
   * Load ACL for given item from repository.
   * 
   * If the item hasn't one own, a nearest parent's ACL will be used.
   * 
   * Applicable for both Node and Property instances. A Property will have a ACL of the parent.
   */
  public AccessControlList getACL(NodeData parent, QPathEntry name) throws RepositoryException {
    // [PN] The ACL, here always exists by contract
    // get an item by a parent and name, return the item ACL if it's a Node
    // for Properety it's always is null
    //ItemData target = getItemData(parent, name);
    //return target.isNode() ? ((NodeData) target).getACL() : parent.getACL();
    
    throw new RepositoryException("getACL() is not usable");
    
//    AccessControlList acl = persistentManager.getACL(parent, name);
//    if(acl == null) {
//      NodeData data = getNearestACAncestor(qpath);
//      if(data == null)
//        throw new RepositoryException("FATAL: Node Data not found for ACL:  " + parent.getQPath().getAsString() + " " + name.getAsString());
//      acl = data.getACL(); 
//    }
//    return acl;
  }

  /**
   * finds the nearest access controllable ancestor
   * @param qpath
   * @return access controllable node data or root node data(anyway)
   * @throws RepositoryException
   */
//  private NodeData getNearestACAncestor_Old(QPath qpath) throws RepositoryException {
//    ItemData item = persistentManager.getItemData(qpath);
//    if (item == null || !item.isNode())
//      return getNearestACAncestor_Old(qpath.makeParentPath());
//    else if (((NodeData) item).getACL() != null) 
////    root or AccessControllable node (root always has ACL)
//      return (NodeData) item;
//    else
//      // Not AccessControllable nor Root node
//      return getNearestACAncestor_Old(qpath.makeParentPath());
//  }
  
  /**
   * Traverse items parents in persistent storage for ACL containing parent.
   * Same work is made in SessionDataManager.getItemData(NodeData, QPathEntry[])
   * but for session scooped items.
   * 
   * @param data - item
   * @return - parent or null
   * @throws RepositoryException
   */
  private NodeData getNearestACAncestor(ItemData data) throws RepositoryException {
    if (data.getParentIdentifier() != null) {
      NodeData parent = (NodeData) getItemData(data.getParentIdentifier());
      while (parent != null) {
        if (parent.getACL() != null) {
          // has an AC parent
          return parent;
        } 
        // going up to the root
        parent = (NodeData) getItemData(parent.getParentIdentifier());
      }
    }
    return null;
  }
  
//  private ItemData initACL(ItemData data) throws RepositoryException {
//    if (data != null && data.isNode()) {
//      
//      // ACL
//      NodeData nData = (NodeData) data;
//      boolean haveParent = data.getQPath().getDepth()>1;
//      if (nData.getACL() == null && haveParent) {
//        //((NodeData) data).setACL(persistentManager.getACL(data.getQPath().makeParentPath()));
//      }
//      
//      // owner
//      AccessControlList parentACL = null;
//      if (nData.getACL() != null && nData.getACL().getOwner() == null && haveParent) {
//        //parentACL = persistentManager.getACL(nData.getQPath().makeParentPath());
//        if (parentACL != null) {
//          nData.getACL().setOwner(parentACL.getOwner());
//        }else{
//          log.warn("!!!!Parent path "+nData.getQPath().makeParentPath()+" ACL == null;!!!!");
//        }
//      }
//    }
//
//    return data;
//  }
  
  /**
   * 
   * @param parent - a parent, can be null (get item by id)
   * @param data - an item data
   * @return - an item data with ACL was initialized
   * @throws RepositoryException
   */
  private ItemData initACL(NodeData parent, ItemData data) throws RepositoryException {
    if (data != null) { 
      if (data.isNode()) {
        // ACL
        NodeData nData = (NodeData) data;
        if (nData.getACL() == null) {
          if (parent != null) {
            nData.setACL(parent.getACL());  
          } else {
            // case of get by id
            NodeData rparent = getNearestACAncestor(data);
            nData.setACL(rparent.getACL());
          }
        }
        
        // owner
        if (nData.getACL() != null && nData.getACL().getOwner() == null) {
          if (parent != null) {
            nData.getACL().setOwner(parent.getACL().getOwner());
          } else {
            NodeData rparent = getNearestACAncestor(data);
            nData.getACL().setOwner(rparent.getACL().getOwner());
          }
        }
      } // no ACL for property
    }

    return data;
  }

  // ------------ ItemDataConsumer impl ------------

  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException {
    List<NodeData> nodes = persistentManager.getChildNodesData(parent);
    for(NodeData node: nodes) 
      //initACL(node);
      initACL(parent, node);
    return nodes;
  }
  public ItemData getItemData(NodeData parent, QPathEntry name) throws RepositoryException {
    return initACL(parent, persistentManager.getItemData(parent, name));
  }
  
  public ItemData getItemData(QPath qpath) throws RepositoryException {
    throw new RepositoryException("getItemData(QPath path) is deprecated");
    
    //return initACL(persistentManager.getItemData(qpath));
  }

  public ItemData getItemData(String uuid) throws RepositoryException {
    return initACL(null, persistentManager.getItemData(uuid));
  }
  
  public List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException {
    return persistentManager.getChildPropertiesData(parent);
  }

  public List<PropertyData> getReferencesData(String identifier) throws RepositoryException {
    return persistentManager.getReferencesData(identifier);
  }
  
  // ------------ SharedDataManager ----------------------
  
  public void save(ItemStateChangesLog changes) throws InvalidItemStateException, UnsupportedOperationException, RepositoryException {
    persistentManager.save(changes);
  }

  public Calendar getCurrentTime() {
    return persistentManager.getCurrentTime();
  }
}
