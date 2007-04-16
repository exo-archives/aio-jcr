/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.Uuid;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.jcr.impl.dataflow.version.VersionHistoryDataHelper;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: VersionImpl.java 13421 2007-03-15 10:46:47Z geaz $
 */

public class VersionImpl extends VersionStorageDescendantNode implements Version {
  
  
  public VersionImpl(NodeData data, SessionImpl session)
      throws PathNotFoundException, RepositoryException {

    super(data, session);
    
    if (!this.isNodeType(Constants.NT_VERSION))
      throw new RepositoryException("Node " + getLocation().getAsString(true)
          + " is not nt:version type");
  }


  /* (non-Javadoc)
   * @see javax.jcr.version.Version#getCreated()
   */
  public Calendar getCreated() throws RepositoryException {
    
    checkValid();
    
//    QPath createdPath = QPath.makeChildPath(getData().getQPath(), Constants.JCR_CREATED);
//    
//    PropertyData pdata = (PropertyData) dataManager.getItemData(createdPath);
    PropertyData pdata = (PropertyData) dataManager.getItemData(nodeData(),new QPathEntry( Constants.JCR_CREATED,0));
    
    
    if (pdata == null)
      throw new VersionException("jcr:created property is not found for version " + getPath());
    
    Value created = session.getValueFactory().loadValue((TransientValueData) pdata.getValues().get(0), pdata.getType());
    
    return created.getDate();
    
    //return getProperty("jcr:created").getDate();
  }

  /* (non-Javadoc)
   * @see javax.jcr.version.Version#getSuccessors()
   */
  public Version[] getSuccessors() throws RepositoryException {
    
    checkValid();
    
//    QPath successorsPath = QPath.makeChildPath(getData().getQPath(), Constants.JCR_SUCCESSORS);
//    
//    PropertyData successorsData = (PropertyData) dataManager.getItemData(successorsPath);
    
    PropertyData successorsData = (PropertyData) dataManager.getItemData(nodeData(),
        new QPathEntry(Constants.JCR_SUCCESSORS, 0));
    
    if (successorsData == null)
      return new Version[0];
    
    List<ValueData> successorsValues = successorsData.getValues();
    Version[] successors = new Version[successorsValues.size()];
    
    try {
      for (int i=0; i<successorsValues.size(); i++) {
        String vuuid = new String(successorsValues.get(i).getAsByteArray());
        VersionImpl version = (VersionImpl) dataManager.getItemByUUID(vuuid, false);
        if (version != null)
          successors[i] = version;
        else
          throw new RepositoryException("Successor version is not found " + vuuid + ", this version " + getPath());
      }
    } catch (IOException e) {
      throw new RepositoryException("Successor value read error " + e, e);
    }
    
    return successors;
    
  }

  /* (non-Javadoc)
   * @see javax.jcr.version.Version#getPredecessors()
   */
  public Version[] getPredecessors() throws RepositoryException {
    
    checkValid();
    
//    QPath predecessorsPath = QPath.makeChildPath(getData().getQPath(), Constants.JCR_PREDECESSORS);
//    
//    PropertyData predecessorsData = (PropertyData) dataManager.getItemData(predecessorsPath);
    //QPath predecessorsPath = QPath.makeChildPath(getData().getQPath(), Constants.JCR_PREDECESSORS);
    
    PropertyData predecessorsData = (PropertyData) dataManager.getItemData(nodeData(),
        new QPathEntry(Constants.JCR_PREDECESSORS, 0));

    
    if (predecessorsData == null)
      return new Version[0];
    
    List<ValueData> predecessorsValues = predecessorsData.getValues();
    Version[] predecessors = new Version[predecessorsValues.size()];
    
    try {
      for (int i=0; i<predecessorsValues.size(); i++) {
        String vuuid = new String(predecessorsValues.get(i).getAsByteArray());
        VersionImpl version = (VersionImpl) dataManager.getItemByUUID(vuuid, false);
        if (version != null)
          predecessors[i] = version;
        else
          throw new RepositoryException("Predecessor version is not found " + vuuid + ", this version " + getPath());
      }
    } catch (IOException e) {
      throw new RepositoryException("Predecessor value read error " + e, e);
    }
    
    return predecessors;
    
  }

  public void addSuccessor(String successorUuid, PlainChangesLog changesLog) throws RepositoryException {
    ValueData successorRef = new TransientValueData(new Uuid(successorUuid));
    
//    QPath successorsPath = QPath.makeChildPath(this.getInternalPath(), Constants.JCR_SUCCESSORS);
//    TransientPropertyData successorsProp = (TransientPropertyData) dataManager.getItemData(successorsPath);
    TransientPropertyData successorsProp = (TransientPropertyData) dataManager
    .getItemData(nodeData(), new QPathEntry(Constants.JCR_SUCCESSORS, 0));
    
    if (successorsProp == null) {
      // create a property now
      List<ValueData> successors = new ArrayList<ValueData>(); 
      successors.add(successorRef);
      successorsProp = TransientPropertyData.createPropertyData(nodeData(), Constants.JCR_SUCCESSORS, PropertyType.REFERENCE, true, successors);
      changesLog.add(ItemState.createAddedState(successorsProp));
    } else {
      // add successor in existed one
      TransientPropertyData newSuccessorsProp = successorsProp.clone();
      newSuccessorsProp.getValues().add(successorRef);
      changesLog.add(ItemState.createUpdatedState(newSuccessorsProp));
    }
  }

  public void addPredecessor(String predeccessorUuid, PlainChangesLog changesLog) throws RepositoryException {
    
    ValueData predeccessorRef = new TransientValueData(new Uuid(predeccessorUuid));
    
    //QPath predeccessorssPath = QPath.makeChildPath(this.getInternalPath(), Constants.JCR_PREDECESSORS);
    //TransientPropertyData predeccessorsProp = (TransientPropertyData) dataManager.getItemData(predeccessorssPath);
    TransientPropertyData predeccessorsProp = (TransientPropertyData) dataManager
        .getItemData(nodeData(), new QPathEntry(Constants.JCR_PREDECESSORS, 0));

    
    if (predeccessorsProp == null) {
      List<ValueData> predeccessors = new ArrayList<ValueData>(); 
      predeccessors.add(predeccessorRef);
      predeccessorsProp = TransientPropertyData.createPropertyData(
          nodeData(), Constants.JCR_PREDECESSORS, PropertyType.REFERENCE, true, predeccessors);
      changesLog.add(ItemState.createAddedState(predeccessorsProp));
    } else {
      // add successor in existed one
      TransientPropertyData newPredeccessorsProp = predeccessorsProp.clone();
      newPredeccessorsProp.getValues().add(predeccessorRef);
      changesLog.add(ItemState.createUpdatedState(newPredeccessorsProp));
    }
  }
  
  void removeSuccessor(String successorUuid, PlainChangesLog changesLog) throws RepositoryException {
    //QPath successorsPath = QPath.makeChildPath(this.getInternalPath(), Constants.JCR_SUCCESSORS);
    //PropertyData successorsProp = (PropertyData) dataManager.getItemData(successorsPath);
    TransientPropertyData successorsProp = (TransientPropertyData) dataManager
        .getItemData(nodeData(), new QPathEntry(Constants.JCR_SUCCESSORS, 0));
    if (successorsProp != null) {
      List<ValueData> newSuccessors = new ArrayList<ValueData>();
      
      try {
        for (ValueData sdata: successorsProp.getValues()) {
          if (!successorUuid.equals(new String(sdata.getAsByteArray())))
            newSuccessors.add(sdata);
        }
      } catch (IOException e) {
        throw new RepositoryException("A jcr:successors property read error " + e, e);
      }
      
      TransientPropertyData newSuccessorsProp = new TransientPropertyData(
          QPath.makeChildPath(nodeData().getQPath(), Constants.JCR_SUCCESSORS, successorsProp.getQPath().getIndex()),
          successorsProp.getUUID(),
          successorsProp.getPersistedVersion(), 
          PropertyType.REFERENCE,
          nodeData().getUUID(), 
          true);
      newSuccessorsProp.setValues(newSuccessors);
      changesLog.add(ItemState.createUpdatedState(newSuccessorsProp));
    } else {
      throw new RepositoryException("A jcr:successors property is not found, version " + getPath());
    }
  }
  
  void removeAddSuccessor(String removedSuccessorUuid, String addedSuccessorUuid, PlainChangesLog changesLog) throws RepositoryException {
//    QPath successorsPath = QPath.makeChildPath(this.getInternalPath(), Constants.JCR_SUCCESSORS);
//    PropertyData successorsProp = (PropertyData) dataManager.getItemData(successorsPath);
    
    TransientPropertyData successorsProp = (TransientPropertyData) dataManager
    .getItemData(nodeData(), new QPathEntry(Constants.JCR_SUCCESSORS, 0));
    
    if (successorsProp != null) {
      List<ValueData> newSuccessors = new ArrayList<ValueData>();
      
      try {
        for (ValueData sdata: successorsProp.getValues()) {
          if (!removedSuccessorUuid.equals(new String(sdata.getAsByteArray())))
            newSuccessors.add(sdata);
        }
      } catch (IOException e) {
        throw new RepositoryException("A jcr:successors property read error " + e, e);
      }

      newSuccessors.add(new TransientValueData(new Uuid(addedSuccessorUuid)));
      
      TransientPropertyData newSuccessorsProp = new TransientPropertyData(
          QPath.makeChildPath(nodeData().getQPath(), Constants.JCR_SUCCESSORS, successorsProp.getQPath().getIndex()),
          successorsProp.getUUID(),
          successorsProp.getPersistedVersion(), 
          PropertyType.REFERENCE,
          nodeData().getUUID(), 
          true);
      newSuccessorsProp.setValues(newSuccessors);
      changesLog.add(ItemState.createUpdatedState(newSuccessorsProp));
    } else {
      throw new RepositoryException("A jcr:successors property is not found, version " + getPath());
    }
  }
  
  void removePredecessor(String predecessorUuid, PlainChangesLog changesLog) throws RepositoryException {
//    QPath predeccessorsPath = QPath.makeChildPath(this.getInternalPath(), Constants.JCR_PREDECESSORS);
//    PropertyData predeccessorsProp = (PropertyData) dataManager.getItemData(predeccessorsPath);
    TransientPropertyData predeccessorsProp = (TransientPropertyData) dataManager
    .getItemData(nodeData(), new QPathEntry(Constants.JCR_PREDECESSORS, 0));
    
    if (predeccessorsProp != null) {
      List<ValueData> newPredeccessors = new ArrayList<ValueData>();
      
      try {
        for (ValueData sdata: predeccessorsProp.getValues()) {
          if (!predecessorUuid.equals(new String(sdata.getAsByteArray())))
            newPredeccessors.add(sdata);
        }
      } catch (IOException e) {
        throw new RepositoryException("A jcr:predecessors property read error " + e, e);
      }
      
      TransientPropertyData newPredecessorsProp = new TransientPropertyData(
          QPath.makeChildPath(nodeData().getQPath(), Constants.JCR_PREDECESSORS, predeccessorsProp.getQPath().getIndex()),
          predeccessorsProp.getUUID(),
          predeccessorsProp.getPersistedVersion(), 
          PropertyType.REFERENCE,
          nodeData().getUUID(), 
          true);
      newPredecessorsProp.setValues(newPredeccessors);
      changesLog.add(ItemState.createUpdatedState(newPredecessorsProp));
    } else {
      throw new RepositoryException("A jcr:predecessors property is not found, version " + getPath());
    }
  }
  
  void removeAddPredecessor(String removedPredecessorUuid, String addedPredecessorUuid, PlainChangesLog changesLog) throws RepositoryException {
//    QPath predeccessorsPath = QPath.makeChildPath(this.getInternalPath(), Constants.JCR_PREDECESSORS);
//    PropertyData predeccessorsProp = (PropertyData) dataManager.getItemData(predeccessorsPath);

    TransientPropertyData predeccessorsProp = (TransientPropertyData) dataManager
    .getItemData(nodeData(), new QPathEntry(Constants.JCR_PREDECESSORS, 0));
    
    if (predeccessorsProp != null) {
      List<ValueData> newPredeccessors = new ArrayList<ValueData>();
      
      try {
        for (ValueData sdata: predeccessorsProp.getValues()) {
          if (!removedPredecessorUuid.equals(new String(sdata.getAsByteArray())))
            newPredeccessors.add(sdata);
        }
      } catch (IOException e) {
        throw new RepositoryException("A jcr:predecessors property read error " + e, e);
      }
      
      newPredeccessors.add(new TransientValueData(new Uuid(addedPredecessorUuid)));
      
      TransientPropertyData newPredecessorsProp = new TransientPropertyData(
          QPath.makeChildPath(nodeData().getQPath(), Constants.JCR_PREDECESSORS, predeccessorsProp.getQPath().getIndex()),
          predeccessorsProp.getUUID(),
          predeccessorsProp.getPersistedVersion(), 
          PropertyType.REFERENCE,
          nodeData().getUUID(), 
          true);
      newPredecessorsProp.setValues(newPredeccessors);
      changesLog.add(ItemState.createUpdatedState(newPredecessorsProp));
    } else {
      throw new RepositoryException("A jcr:predecessors property is not found, version " + getPath());
    }
  }

  public VersionHistoryImpl getContainingHistory() throws RepositoryException {
    
    checkValid();
    
 //   VersionHistoryImpl vhistory = (VersionHistoryImpl) dataManager.getItem(nodeData().makeParentPath(), true);
    
    VersionHistoryImpl vhistory = (VersionHistoryImpl) dataManager.getItemByUUID(nodeData().getParentUUID(), true);
     
    
    if (vhistory == null)
      throw new VersionException("Version history item is not found for version " + getPath());
    
    return vhistory;
  }


  public SessionChangesLog restoreLog(NodeData nodeData, VersionHistoryDataHelper historyData, 
      SessionImpl restoreSession, boolean removeExisting, SessionChangesLog delegatedLog) throws RepositoryException {

    if (log.isDebugEnabled())
      log.debug("Restore: " + nodeData.getQPath().getAsString() + ", removeExisting=" + removeExisting);
    
    DataManager dmanager = restoreSession.getTransientNodesManager().getTransactManager();
    
    NodeData parentData = (NodeData) dmanager.getItemData(nodeData.getParentUUID());
    
    QPath frozenPath = QPath.makeChildPath(getData().getQPath(), Constants.JCR_FROZENNODE);
    NodeData frozenData = (NodeData) dmanager.getItemData(frozenPath);
    
    ItemDataRestoreVisitor restoreVisitor = new ItemDataRestoreVisitor(
        parentData, 
        nodeData.getQPath().getName(),
        historyData, 
        restoreSession, 
        removeExisting,
        delegatedLog);
    
    frozenData.accept(restoreVisitor);
    
    return restoreVisitor.getRestoreChanges();
  }
  
  public void restore(NodeImpl node, boolean removeExisting) throws RepositoryException {

    // use restored node session
    SessionImpl restoreSession = node.getSession();
    NodeData nodeData = (NodeData) node.getData();
    VersionHistoryDataHelper historyData = node.getVersionHistory().getData();
    
    SessionChangesLog changesLog = restoreLog(nodeData, historyData, restoreSession, removeExisting, null);
    restoreSession.getTransientNodesManager().getTransactManager().save(changesLog);

  }
  
  public boolean isSuccessorOrSameOf(VersionImpl anotherVersion) 
  throws RepositoryException {
    Version[] prds = getPredecessors();
    for(int i=0; i<prds.length; i++) {
      if(prds[i].getUUID().equals(anotherVersion.getUUID()) ||
         ((VersionImpl)prds[i]).isSuccessorOrSameOf(anotherVersion))
        return true;
    }
    return false;
  }
 
}