/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.audit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.Uuid;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: AuditServiceImpl.java 12164 2007-01-22 08:39:22Z geaz $
 */

public class AuditServiceImpl implements AuditService {

  private static Log log = ExoLogger.getLogger("jcr.AuditService");
  private final ManageableRepository repository;
  private final String workspaceName;
  
  public AuditServiceImpl(InitParams params, RepositoryService repService) 
  throws RepositoryException, RepositoryConfigurationException {
    ValueParam valParam = null;
    if(params != null)
      valParam = params.getValueParam("repository");
    if(valParam != null)
      repository = repService.getRepository(valParam.getValue());
    else
      repository = repService.getRepository();
    workspaceName = repository.getConfiguration().getSystemWorkspaceName();
  }

  public void addRecord(Item item, int eventType) throws RepositoryException {

    checkIfAuditable(item);

    AuditSession auditSession = new AuditSession(item);
    NodeImpl storage = auditSession.getAuditStorage();
    SessionDataManager dm = auditSession.getDataManager();
    SessionImpl session = (SessionImpl) item.getSession();

    //LocationFactory locationFactory = session.getLocationFactory();
    // add /jcr:system/exo:auditStorage/itemID/recordId (exo:auditRecord)

    // here should be added to SessionDataManager:
    // nodeData: /exo:audit/itemUUID/<get lastRecord + 1>
    // its primaryType exo:auditRecord
    // exo:user = session.getUserId()
    // exo:created = current date
    // exo:event = eventType
    // exo:propertyName - name of changed property if any

    NodeData auditHistory = auditSession.getAuditHistoryNodeData();

    // make path to the AUDITHISTORY_LASTRECORD property
    InternalQPath path = InternalQPath.makeChildPath(auditHistory.getQPath(),
        AuditService.EXO_AUDITHISTORY_LASTRECORD);
    // searching last name of node
    PropertyData pData = (PropertyData) dm.getItemData(path);
    String auditRecordName = "";
    try {
      auditRecordName = String.valueOf(new Integer(new String(pData.getValues().get(0)
          .getAsByteArray(), Constants.DEFAULT_ENCODING)) + 1);
    } catch (Exception e) {
      throw new RepositoryException(
          "Error on add audit record. Problem in calculating new record name. "
              + e.getLocalizedMessage());
    }
    
    
    // exo:auditRecord
    TransientNodeData arNode = TransientNodeData.createNodeData(auditHistory, 
        new InternalQName(null, auditRecordName), AuditService.EXO_AUDITRECORD);
    // exo:auditRecord
    session.getTransientNodesManager().update(
        new ItemState(arNode, ItemState.ADDED, true, ((ItemImpl) item).getInternalPath()), true);
    
    // jcr:primaryType
    TransientPropertyData arPrType = TransientPropertyData.createPropertyData(arNode,
        Constants.JCR_PRIMARYTYPE, PropertyType.NAME, false);
    arPrType.setValue(new TransientValueData(arNode.getPrimaryTypeName()));
    // exo:user
    TransientPropertyData arUser = TransientPropertyData.createPropertyData(arNode,
        AuditService.EXO_AUDITRECORD_USER, PropertyType.STRING, false);
    arUser.setValue(new TransientValueData(session.getUserID()));
    // exo:created
    TransientPropertyData arCreated = TransientPropertyData.createPropertyData(arNode,
        AuditService.EXO_AUDITRECORD_CREATED, PropertyType.DATE, false);
    arCreated.setValue(new TransientValueData(dm.getTransactManager().getStorageDataManager()
        .getCurrentTime()));
    // exo:eventType
    TransientPropertyData arEventType = TransientPropertyData.createPropertyData(arNode,
        AuditService.EXO_AUDITRECORD_EVENTTYPE, PropertyType.LONG, false);
    arEventType.setValue(new TransientValueData(eventType));


    // jcr:primaryType
    session.getTransientNodesManager().update(
        new ItemState(arPrType, ItemState.ADDED, true, ((ItemImpl) item).getInternalPath()), true);
    // exo:user
    session.getTransientNodesManager().update(
        new ItemState(arUser, ItemState.ADDED, true, ((ItemImpl) item).getInternalPath()), true);
    // exo:created
    session.getTransientNodesManager().update(
        new ItemState(arCreated, ItemState.ADDED, true, ((ItemImpl) item).getInternalPath()), true);
    // exo:eventType
    session.getTransientNodesManager().update(
        new ItemState(arEventType, ItemState.ADDED, true, ((ItemImpl) item).getInternalPath()),
        true);
    // exo:propertyName
    if(!item.isNode()) {
      TransientPropertyData propertyNameData = TransientPropertyData.createPropertyData(arNode,
          EXO_AUDITRECORD_PROPERTYNAME, PropertyType.STRING, false);
      propertyNameData.setValue(new TransientValueData(((ItemImpl)item).getInternalName()));
      session.getTransientNodesManager().update(
          new ItemState(propertyNameData, ItemState.ADDED, true, ((ItemImpl) item).getInternalPath()),
          true);
      

    }

    // Update lastRecord
    
    TransientPropertyData pLastRecord = (TransientPropertyData)auditSession.getDataManager().getItemData(InternalQPath.
        makeChildPath(auditHistory.getQPath(), EXO_AUDITHISTORY_LASTRECORD));
    
    pLastRecord.setValue(new TransientValueData(String
      .valueOf(auditRecordName)));
    
    // Update lastRecord
    session.getTransientNodesManager().update(
        new ItemState(pLastRecord, ItemState.UPDATED, true, ((ItemImpl) item)
            .getInternalPath()), true); 
  
    
    if (log.isDebugEnabled())
      log.debug("Add audit record: "
          +" Item path="+((ItemImpl)item).getLocation().getInternalPath().getAsString()
          +" User="+session.getUserID()
          +" EventType="+eventType
        );

  }

  public void createHistory(Node node) throws RepositoryException {

    checkIfAuditable(node);
    
    AuditSession auditSession = new AuditSession(node);
    NodeImpl storage = auditSession.getAuditStorage();
    // add /jcr:system/exo:auditStorage/itemID (exo:auditHistory)

    // here should be added to SessionDataManager:
    // nodeData: /exo:audit/itemUUID 
    // its primaryType exo:auditHistory
    // exo:targetNode (ref to item)
    // exo:lastRecord = "0"
    // in itemData/auditHistory - pointer to history (UUID)
    
    SessionImpl session = (SessionImpl)node.getSession();
     
    InternalQName aiName = new InternalQName(null, ((ItemImpl)node).getData().getUUID());
    //exo:auditHistory
    TransientNodeData ahNode = TransientNodeData.createNodeData((NodeData) storage.getData(),
        aiName, AuditService.EXO_AUDITHISTORY);
    
    // jcr:primaryType
    TransientPropertyData aPrType = TransientPropertyData
        .createPropertyData(ahNode, Constants.JCR_PRIMARYTYPE, PropertyType.NAME, false);
    aPrType.setValue(new TransientValueData(ahNode.getPrimaryTypeName()));

    // jcr:uuid
    TransientPropertyData ahUuid = TransientPropertyData.createPropertyData(
        ahNode, Constants.JCR_UUID, PropertyType.STRING, false);
    ahUuid.setValue(new TransientValueData(ahNode.getUUID()));

    // jcr:mixinTypes
    TransientPropertyData ahMixinTypes = TransientPropertyData
        .createPropertyData(ahNode, Constants.JCR_MIXINTYPES, PropertyType.NAME, false);
    ahMixinTypes.setValue(new TransientValueData(Constants.MIX_REFERENCEABLE));

    //exo:targetNode
    TransientPropertyData ahTargetNode = TransientPropertyData.createPropertyData(
        ahNode, AuditService.EXO_AUDITHISTORY_TARGETNODE, PropertyType.REFERENCE, false);
    ahTargetNode.setValue(new TransientValueData(((ItemImpl)node).getData().getUUID()));
    
    //exo:lastRecord
    TransientPropertyData ahLastRecord = TransientPropertyData.createPropertyData(
        ahNode, AuditService.EXO_AUDITHISTORY_LASTRECORD, PropertyType.STRING, false);
    ahLastRecord.setValue(new TransientValueData("0"));
    
    //node exo:auditHistory
    TransientPropertyData pAuditHistory = TransientPropertyData
    .createPropertyData((NodeData) ((ItemImpl)node).getData(), AuditService.EXO_AUDITHISTORY,PropertyType.STRING, false);
    pAuditHistory.setValue(new TransientValueData(new Uuid(ahNode.getUUID())));

    
    session.getTransientNodesManager().update(new ItemState(ahNode, ItemState.ADDED, 
        true, ((ItemImpl) node).getInternalPath()), true);

    session.getTransientNodesManager().update(new ItemState(aPrType, ItemState.ADDED, 
        true, ((ItemImpl) node).getInternalPath()), true);
        
    session.getTransientNodesManager().update(new ItemState(ahUuid, ItemState.ADDED, 
        true, ((ItemImpl) node).getInternalPath()), true);
    
    session.getTransientNodesManager().update(new ItemState(ahMixinTypes, ItemState.ADDED, 
        true, ((ItemImpl) node).getInternalPath()), true);
    
//    session.getTransientNodesManager().update(new ItemState(ahCreated, ItemState.ADDED, 
//        true, ((ItemImpl) item).getInternalPath()), true);
    
    session.getTransientNodesManager().update(new ItemState(ahTargetNode, ItemState.ADDED, 
        true, ((ItemImpl) node).getInternalPath()), true);
    
    session.getTransientNodesManager().update(new ItemState(ahLastRecord, ItemState.ADDED, 
        true, ((ItemImpl) node).getInternalPath()), true);
    
    session.getTransientNodesManager().update(new ItemState(pAuditHistory, ItemState.ADDED, 
        true, ((ItemImpl) node).getInternalPath()), true);

  }
  public AuditHistory getHistory(Node node) throws RepositoryException, UnsupportedOperationException {
    
    // check: if item (if node) or parent (if property) is exo:auditable
//    NodeImpl node = (item.isNode())?(NodeImpl)item:(NodeImpl)item.getParent();
//    if(!node.isNodeType(EXO_AUDITABLE))
//      throw new UnsupportedOperationException("Node is not auditable "+node.getPath());
    
    // get history for this item and create AuditHistory object
    AuditSession auditSession = new AuditSession(node);
    SessionDataManager dm = auditSession.getDataManager();
    NodeData storage = auditSession.getAuditHistoryNodeData();
    List<AuditRecord> auditRecords = new ArrayList<AuditRecord>();
    //AuditRecord aRecord = null; 
    ValueFactoryImpl vf = (ValueFactoryImpl)node.getSession().getValueFactory();
    //Search all auditRecords
    List<NodeData>  auditRecordsNodeData = dm.getChildNodesData(storage);
    for (NodeData nodeData : auditRecordsNodeData) {
      //Serching properties
      List<PropertyData> auditRecordNodeData = dm.getChildPropertiesData(nodeData);
      //define variables
      String user = null;
      InternalQName propertyName = null;
      int eventType = -1;
      Calendar date = null;
      //loading data
      for (PropertyData propertyData : auditRecordNodeData) {
        if (propertyData.getQPath().getName().equals(AuditService.EXO_AUDITRECORD_USER)){
          user = vf.loadValue((TransientValueData) propertyData.getValues().get(0),PropertyType.STRING).getString();
        }else if (propertyData.getQPath().getName().equals(AuditService.EXO_AUDITRECORD_EVENTTYPE)){
          eventType = (int) vf.loadValue((TransientValueData) propertyData.getValues().get(0),PropertyType.LONG).getLong();
        }else if (propertyData.getQPath().getName().equals(AuditService.EXO_AUDITRECORD_CREATED)){
          date =  vf.loadValue((TransientValueData) propertyData.getValues().get(0),PropertyType.DATE).getDate();
        }else if (propertyData.getQPath().getName().equals(AuditService.EXO_AUDITRECORD_PROPERTYNAME)){
          try {
            propertyName = InternalQName.parse(new String (propertyData.getValues().get(0).getAsByteArray()));
          } catch (Exception e) {
            throw new RepositoryException(e);
          }
//          ((TransientValueData) propertyData.getValues().get(0),PropertyType.NAME)
//          propertyName =  ((BaseValue)vf.loadValue((TransientValueData) propertyData.getValues().get(0),PropertyType.NAME));
        }
      }
      //add audit record
      auditRecords.add(new AuditRecord(user,eventType,date,propertyName));
    }
    return new AuditHistory(node,auditRecords);
  }
  
  public void removeHistory(Node node) throws RepositoryException {
    AuditSession auditSession = new AuditSession(node);
    NodeData storage = auditSession.getAuditHistoryNodeData();
    // remove /jcr:system/exo:auditStorage/itemID
    // (delete in SessionDataManager)
    SessionImpl session = (SessionImpl)node.getSession();
    session.getTransientNodesManager().delete(storage);
  }
  
  public boolean hasHistory(Node node) {
    NodeData data;
    try {
      AuditSession auditSession = new AuditSession(node);
      data = auditSession.getAuditHistoryNodeData();
    } catch (RepositoryException e) {
      return false;
    }
    return (data == null)?false:true;
  }

  
  private void checkIfAuditable(Item item) throws RepositoryException,  UnsupportedOperationException {
    NodeImpl node = (item.isNode())?(NodeImpl)item:(NodeImpl)item.getParent();
    if (!node.isNodeType("exo:auditable"))
      throw new ConstraintViolationException("exo:auditable node expected at: "
          + node.getPath());
  }

  private class AuditSession {
    
    private SessionImpl session;
    private SessionDataManager dm ;
    private ExtendedNode node;
    private AuditSession(Item item) throws RepositoryException {
      session = (SessionImpl)item.getSession();
      if(item.isNode())
        node = (ExtendedNode) item;
      else
        node = (ExtendedNode) item.getParent();
      if(!node.isNodeType(AuditService.EXO_AUDITABLE))
        throw new RepositoryException("Node is not exo:auditable "+node.getPath());
      
      dm = session.getTransientNodesManager();
    }
    
    private NodeImpl getAuditStorage() throws RepositoryException {
      NodeImpl storage;
      try {
        storage = (NodeImpl)session.getNodeByUUID(AUDIT_STORAGE_ID);
      } catch (ItemNotFoundException e) {
        SessionChangesLog changesLog = new SessionChangesLog(session.getId());
  
        // here should be added to TransactionalDataManager (i.e. saved immediatelly!):
        // nodeData: /exo:audit with UUID = AUDIT_STORAGE_ID
        // its primaryType exo:auditStorage
        TransientNodeData exoAuditNode = TransientNodeData.createNodeData(
            (NodeData) ((NodeImpl) session.getRootNode()).getData(), AuditService.EXO_AUDIT, AuditService.EXO_AUDITSTORAGE,
                AuditService.AUDIT_STORAGE_ID);
        
        // jcr:primaryType
        TransientPropertyData exoAuditPrType = TransientPropertyData
            .createPropertyData(exoAuditNode, Constants.JCR_PRIMARYTYPE, PropertyType.NAME, false);
        exoAuditPrType.setValue(new TransientValueData(exoAuditNode.getPrimaryTypeName()));

        // jcr:uuid
        TransientPropertyData exoAuditUuid = TransientPropertyData.createPropertyData(
            exoAuditNode, Constants.JCR_UUID, PropertyType.STRING, false);
        exoAuditUuid.setValue(new TransientValueData(exoAuditNode.getUUID()));
        
        
        // jcr:mixinTypes
        TransientPropertyData exoAuditMixinTypes = TransientPropertyData
            .createPropertyData(exoAuditNode, Constants.JCR_MIXINTYPES, PropertyType.NAME, true);
        exoAuditMixinTypes.setValue(new TransientValueData(Constants.MIX_REFERENCEABLE));

        changesLog.add(ItemState.createAddedState(exoAuditNode));
        changesLog.add(ItemState.createAddedState(exoAuditPrType));
        changesLog.add(ItemState.createAddedState(exoAuditUuid));
        changesLog.add(ItemState.createAddedState(exoAuditMixinTypes));
        
        session.getTransientNodesManager().getTransactManager().save(changesLog);
        storage = (NodeImpl)session.getNodeByUUID(AUDIT_STORAGE_ID);
      }
      return storage;
    }
    
    private SessionDataManager getDataManager() {
      return dm;
    }
    
    private NodeData getAuditHistoryNodeData() throws RepositoryException{
      InternalQPath path = null;
      //make path to the audithistory property 
      path = InternalQPath.makeChildPath(((NodeImpl)node).getData().getQPath(),AuditService.EXO_AUDITHISTORY);
      //searching uuid of corresponding EXO_AUDITHISTORY node
      PropertyData pData = (PropertyData) dm.getItemData(path);
      String ahUuid;
      try {
        ahUuid =  new String(pData.getValues().get(0).getAsByteArray(),Constants.DEFAULT_ENCODING);
      } catch (Exception e) {
        throw new RepositoryException("Error getAuditHistory converting to string");
      }
      return (NodeData) dm.getItemData(ahUuid);
    }
  }
  



}
