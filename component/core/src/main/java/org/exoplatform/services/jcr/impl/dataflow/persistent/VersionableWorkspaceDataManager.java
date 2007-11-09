/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
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
 * @version $Id$
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
    QPath path = nodeData.getQPath();
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
    QPath path = nodeData.getQPath();
    if(isSystemDescendant(path) && !this.equals(versionDataManager)) {
      return versionDataManager.getChildPropertiesData(nodeData);
    }
    return super.getChildPropertiesData(nodeData);
  }
  
  public List<PropertyData> listChildPropertiesData(NodeData nodeData) throws RepositoryException {
    QPath path = nodeData.getQPath();
    if(isSystemDescendant(path) && !this.equals(versionDataManager)) {
      return versionDataManager.listChildPropertiesData(nodeData);
    }
    return super.listChildPropertiesData(nodeData);
  }
  
  public ItemData getItemData(NodeData parentData, QPathEntry name) throws RepositoryException {
    ItemData data = super.getItemData(parentData,name);
    if(data != null)
      return data;
    else if(!this.equals(versionDataManager)) { 
      // try from version storage if not the same
      data = versionDataManager.getItemData(parentData,name);
      if(data != null && isSystemDescendant(data.getQPath()))
        return data;
    } 
    return null;
  }
  /**
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager#getItemData(java.lang.String)
   */
  public ItemData getItemData(String identifier) throws RepositoryException {
    ItemData data = super.getItemData(identifier);
    if(data != null)
      return data;
    else if(!this.equals(versionDataManager)) { 
      // try from version storage if not the same
      data = versionDataManager.getItemData(identifier);
      if(data != null && isSystemDescendant(data.getQPath()))
        return data;
    } 
    return null;
  }

  public void save(CompositeChangesLog changesLog) throws RepositoryException, InvalidItemStateException {
    
    ChangesLogIterator logIterator = changesLog.getLogIterator();
    
    boolean saveVersions = false;
    TransactionChangesLog versionLog = new TransactionChangesLog();
    boolean saveNonVersions = false;
    TransactionChangesLog nonVersionLog = new TransactionChangesLog();
    
    while(logIterator.hasNextLog()) {
      List <ItemState> vstates = new ArrayList<ItemState>();
      List <ItemState> nvstates = new ArrayList<ItemState>();
      
      PlainChangesLog changes = logIterator.nextLog();
      for(ItemState change: changes.getAllStates()) {
        if (isSystemDescendant(change.getData().getQPath()) && !this.equals(versionDataManager))
          vstates.add(change);
        else
          nvstates.add(change);
      }
      
      if (vstates.size()>0) {
        versionLog.addLog(new PlainChangesLogImpl(vstates, changes.getSessionId(),changes.getEventType()));
        saveVersions = true;
      }
      
      if (nvstates.size()>0) {
        nonVersionLog.addLog(new PlainChangesLogImpl(nvstates, changes.getSessionId(),changes.getEventType()));
        saveNonVersions = true;
      }
    }
    
    if (saveVersions)
      versionDataManager.save(versionLog);

    if (saveNonVersions)
      super.save(nonVersionLog);
  }
  
  private boolean isSystemDescendant(QPath path) {
    return path.equals(Constants.JCR_SYSTEM_PATH) || path.isDescendantOf(Constants.JCR_SYSTEM_PATH, false);
  }

}
