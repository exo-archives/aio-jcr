/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * 
 * Responsible for: 
 * *redirecting repository operations if item is descendant of
 * /jcr:system/jcr:versionStorage
 * *adding version history for newly added/assigned mix:versionable 
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: VersionableWorkspaceDataManager.java 13421 2007-03-15 10:46:47Z geaz $
 */

public class VersionableWorkspaceDataManager extends ACLInheritanceSupportedWorkspaceDataManager {

  private static Log log = ExoLogger.getLogger("jcr.VersionableWorkspaceDataManager");
  
  private ACLInheritanceSupportedWorkspaceDataManager versionDataManager;
  
  public VersionableWorkspaceDataManager(CacheableWorkspaceDataManager persistentManager) {
    super(persistentManager);
  }
  
  // called by WorkspaceContainer after repository initialization
  public void setSystemDataManager(DataManager systemDataManager) {

    this.versionDataManager = (ACLInheritanceSupportedWorkspaceDataManager) systemDataManager;
  }
  

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.WorkspaceDataManager#getChildNodes(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  @Override
  public List<NodeData> getChildNodesData(NodeData nodeData) throws RepositoryException {
    InternalQPath path = nodeData.getQPath();
    if(isSystemDescendant(path) && !this.equals(versionDataManager)) {
      return versionDataManager.getChildNodesData(nodeData);
    }
    return super.getChildNodesData(nodeData);
  }


  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.WorkspaceDataManager#getChildProperties(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  @Override
  public List<PropertyData> getChildPropertiesData(NodeData nodeData) throws RepositoryException {
    InternalQPath path = nodeData.getQPath();
    if(isSystemDescendant(path) && !this.equals(versionDataManager)) {
      return versionDataManager.getChildPropertiesData(nodeData);
    }
    return super.getChildPropertiesData(nodeData);
  }


  /**
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager#getItemData(java.lang.String)
   */
  public ItemData getItemData(InternalQPath qpath) throws RepositoryException {
    if(isSystemDescendant(qpath) && !this.equals(versionDataManager)) {
      return versionDataManager.getItemData(qpath);
    }
    return super.getItemData(qpath);
  }
  
  /**
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager#getItemData(java.lang.String)
   */
  public ItemData getItemData(String uuid) throws RepositoryException {
    ItemData data = super.getItemData(uuid);
    if(data != null)
      return data;
    else if(!this.equals(versionDataManager)) { 
      // try from version storage if not the same
      data = versionDataManager.getItemData(uuid);
      if(data != null && isSystemDescendant(data.getQPath()))
        return data;
    } 
    return null;
  }

//  public synchronized void save(ItemStateChangesLog changesLog) throws RepositoryException,
//      InvalidItemStateException {
//    List<ItemState> changes = changesLog.getAllStates();
//    List<ItemState> versionChanges = new ArrayList<ItemState>();
//    List<ItemState> nonVersionChanges = new ArrayList<ItemState>();
//
//    if (log.isDebugEnabled())
//      log.debug("save ver: " + changesLog.dump());
//
//    for (int i = 0; i < changes.size(); i++) {
//      if (isSystemDescendant(changes.get(i).getData().getQPath()) && !this.equals(versionDataManager))
//        versionChanges.add(changes.get(i));
//      else
//        nonVersionChanges.add(changes.get(i));
//    }
//
//    if (!versionChanges.isEmpty())
//      versionDataManager.save(new PlainChangesLogImpl(versionChanges, changesLog.getSessionId()));
//
//    if (!nonVersionChanges.isEmpty())
//      super.save(new PlainChangesLogImpl(nonVersionChanges, changesLog.getSessionId()));
//  }
  
  private boolean isSystemDescendant(InternalQPath path) {
    return path.equals(Constants.JCR_SYSTEM_PATH) || path.isDescendantOf(Constants.JCR_SYSTEM_PATH, false);
  }

}
