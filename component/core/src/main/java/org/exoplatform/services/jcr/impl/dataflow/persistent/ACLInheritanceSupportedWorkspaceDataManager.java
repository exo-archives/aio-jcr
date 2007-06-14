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
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.SharedDataManager;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * Data Manager supported ACL Inheritance
 * @author Gennady Azarenkov
 * @version $Id$
 */
public class ACLInheritanceSupportedWorkspaceDataManager implements ItemDataConsumer, SharedDataManager {

  private static Log log = ExoLogger.getLogger("jcr.ACLInheritanceSupportedWorkspaceDataManager");
  
  private final CacheableWorkspaceDataManager persistentManager;
  
  public ACLInheritanceSupportedWorkspaceDataManager(CacheableWorkspaceDataManager persistentManager) {
    this.persistentManager = persistentManager;
  }

  /* 
   * Guaranteed ACL  
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager#getACL(org.exoplatform.services.jcr.datamodel.InternalQPath)
   */
  @Deprecated
  public AccessControlList getACL(QPath qpath) throws RepositoryException {
    AccessControlList acl = persistentManager.getACL(qpath);
    if(acl == null) {
      NodeData data = getNearestACAncestor(qpath);
      if(data == null)
        throw new RepositoryException("FATAL: Node Data not found for ACL:  "+qpath);
      acl = data.getACL(); 
    }
    return acl;
  }
  
  public AccessControlList getACL(NodeData parent, QPathEntry name) throws RepositoryException {
    // TODO
    return null;
  }

  /**
   * finds the nearest access controllable ancestor
   * @param qpath
   * @return access controllable node data or root node data(anyway)
   * @throws RepositoryException
   */
  private NodeData getNearestACAncestor(QPath qpath) throws RepositoryException {
    ItemData item = persistentManager.getItemData(qpath);
    if (item == null || !item.isNode())
      return getNearestACAncestor(qpath.makeParentPath());
    else if (((NodeData) item).getACL() != null) 
//    root or AccessControllable node (root always has ACL)
      return (NodeData) item;
    else
      // Not AccessControllable nor Root node
      return getNearestACAncestor(qpath.makeParentPath());
  }
  
  private ItemData initACL(ItemData data) throws RepositoryException {
    if (data != null && data.isNode()) {
      NodeData nData = (NodeData) data;
      boolean haveParent = data.getQPath().getDepth()>1;
      if (nData.getACL() == null && haveParent)
        ((NodeData) data).setACL(persistentManager.getACL(data.getQPath().makeParentPath()));
      AccessControlList parentACL;
      if (nData.getACL() != null && nData.getACL().getOwner() == null && haveParent) {
        parentACL = persistentManager.getACL(nData.getQPath().makeParentPath());
        if (parentACL != null) {
          nData.getACL().setOwner(parentACL.getOwner());
        }else{
          log.warn("!!!!Parent path "+nData.getQPath().makeParentPath()+" ACL == null;!!!!");
        }
      }
    }

    return data;
  }

  // ------------ ItemDataConsumer impl ------------

  public List<NodeData> getChildNodesData(NodeData nodeData) throws RepositoryException {
    List<NodeData> nodes = persistentManager.getChildNodesData(nodeData);
    for(NodeData node: nodes) 
      initACL(node);
    return nodes;
  }
  public ItemData getItemData(NodeData parentData,QPathEntry name) throws RepositoryException {
    return initACL(persistentManager.getItemData(parentData,name));
  }
  
  public ItemData getItemData(QPath qpath) throws RepositoryException {
    //throw new RepositoryException("getItemData(QPath path) is deprecated");
    
    return initACL(persistentManager.getItemData(qpath));
  }

  public ItemData getItemData(String identifier) throws RepositoryException {
    return initACL(persistentManager.getItemData(identifier));
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
