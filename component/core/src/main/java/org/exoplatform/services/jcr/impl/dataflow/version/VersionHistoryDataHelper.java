/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.util.DateFormatHelper;

/**
 * Created by The eXo Platform SARL
 *
 * 19.12.2006
 *
 * Helper class. Contains some functions for a version history operations.
 * Actually it's a wrapper for NodeData with additional methods. 
 * For use instead a VersionHistoryImpl.
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: VersionHistoryDataHelper.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class VersionHistoryDataHelper extends TransientNodeData {

  protected final ItemDataConsumer dataManager;
  
  protected final NodeTypeManagerImpl ntManager;
  
  public VersionHistoryDataHelper(NodeData source, ItemDataConsumer dataManager, NodeTypeManagerImpl ntManager) {
    /*InternalQPath path, String uuid, int version,
      InternalQName primaryTypeName, InternalQName[] mixinTypeNames,
      int orderNum, String parentUUID, AccessControlList acl*/
    
    super(source.getQPath(), source.getUUID(), source.getPersistedVersion(), 
        source.getPrimaryTypeName(), source.getMixinTypeNames(), source.getOrderNumber(), 
        source.getParentUUID(), source.getACL());
    
    this.dataManager = dataManager;
    this.ntManager = ntManager;
  }
  
  public List<NodeData> getAllVersionsData() throws RepositoryException {
    
    NodeData vData = (NodeData) dataManager.getItemData(getQPath());
    
    NodeData rootVersion = (NodeData) dataManager.getItemData(
        InternalQPath.makeChildPath(getQPath(), Constants.JCR_ROOTVERSION));
    
    List<NodeData> vChilds = new ArrayList<NodeData>();
    
    // should be first in list
    vChilds.add(rootVersion);
    
    for (NodeData cnd: dataManager.getChildNodesData(vData)) {
      if (!cnd.getQPath().getName().equals(Constants.JCR_ROOTVERSION) &&
          ntManager.isNodeType(Constants.NT_VERSION, cnd.getPrimaryTypeName())) // !cnd.getQPath().getName().equals(Constants.JCR_VERSIONLABELS)
        vChilds.add(cnd);
    }
    
    return vChilds;
  }
  
  public NodeData getLastVersionData() throws RepositoryException {
    List<NodeData> versionsData = getAllVersionsData();
    
    NodeData lastVersionData = null;
    Calendar lastCreated = null;
    for (NodeData vd: versionsData) {
      PropertyData createdData = (PropertyData) dataManager.getItemData(
          InternalQPath.makeChildPath(vd.getQPath(), Constants.JCR_CREATED));
      if (createdData == null)
        throw new VersionException("jcr:created is not found, version: " + vd.getQPath().getAsString()); 

      Calendar created = null;
      try {
        created = new DateFormatHelper().deserialize(new String(createdData.getValues().get(0).getAsByteArray()));
      } catch(IOException e) {
        throw new RepositoryException(e);
      }
      
      if(lastVersionData == null || created.after(lastCreated)) { 
        lastCreated = created;
        lastVersionData = vd;
      }
    }
    return lastVersionData;
  }  
  
  public NodeData getVersionData(InternalQName versionQName) throws VersionException, RepositoryException {
    InternalQPath versionPath = InternalQPath.makeChildPath(getQPath(), versionQName);
    
    return (NodeData) dataManager.getItemData(versionPath);
  }
  
  public NodeData getVersionLabelsData() throws VersionException, RepositoryException {
    InternalQPath labelsPath = InternalQPath.makeChildPath(getQPath(), Constants.JCR_VERSIONLABELS);
    
    return (NodeData) dataManager.getItemData(labelsPath);
  }
  
  public List<PropertyData> getVersionLabels() throws VersionException, RepositoryException {
    List<PropertyData> labelsList = dataManager.getChildPropertiesData(getVersionLabelsData());
    
    return labelsList;
  }
  
  public NodeData getVersionDataByLabel(InternalQName labelQName) throws VersionException, RepositoryException {
    
    List<PropertyData> labelsList = getVersionLabels();
    for (PropertyData prop: labelsList) {
      if (prop.getQPath().getName().equals(labelQName)) {
        // label found
        try {
          String versionUuid = new String(prop.getValues().get(0).getAsByteArray());
          return (NodeData) dataManager.getItemData(versionUuid);
        } catch (IllegalStateException e) {
          throw new RepositoryException("Version label data error: " + e.getMessage(), e);
        } catch (IOException e) {
          throw new RepositoryException("Version label data reading error: " + e.getMessage(), e);
        }
      }
    }
    
    return null;
  }
 
}
