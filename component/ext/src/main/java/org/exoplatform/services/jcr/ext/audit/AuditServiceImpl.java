/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.ext.audit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: AuditServiceImpl.java 12164 2007-01-22 08:39:22Z geaz $
 */

public class AuditServiceImpl implements AuditService {

  private static Log log           = ExoLogger.getLogger("jcr.AuditService");

  private String     adminIdentity = null;

  public AuditServiceImpl(InitParams params, RepositoryService repService) throws RepositoryConfigurationException {
    ValueParam valParam = null;
    if (params != null) {
      valParam = params.getValueParam("adminIdentity");
      if (valParam != null)
        adminIdentity = valParam.getValue();
    }
    if (adminIdentity == null)
      throw new RepositoryConfigurationException("Admin identity is not configured");
  }

  public void addRecord(Item item, int eventType) throws RepositoryException {

    checkIfAuditable(item);

    AuditSession auditSession = new AuditSession(item);
    // NodeData storage = auditSession.getAuditStorage();
    SessionDataManager dm = auditSession.getDataManager();
    SessionImpl session = (SessionImpl) item.getSession();

    NodeData auditHistory = auditSession.getAuditHistoryNodeData();

    if (auditHistory == null) {
      throw new RepositoryException("Audit history for  " + item.getPath() + "not found");
    }

    // make path to the AUDITHISTORY_LASTRECORD property
    QPath path = QPath.makeChildPath(auditHistory.getQPath(),
                                     AuditService.EXO_AUDITHISTORY_LASTRECORD);
    // searching last name of node
    PropertyData pData = (PropertyData) dm.getItemData(path);
    String auditRecordName = "";
    try {
      auditRecordName = String.valueOf(new Integer(new String(pData.getValues()
                                                                   .get(0)
                                                                   .getAsByteArray(),
                                                              Constants.DEFAULT_ENCODING)) + 1);
    } catch (Exception e) {
      throw new RepositoryException("Error on add audit record. Problem in calculating new record name. "
          + e.getLocalizedMessage());
    }

    // exo:auditRecord
    TransientNodeData arNode = TransientNodeData.createNodeData(auditHistory,
                                                                new InternalQName(null,
                                                                                  auditRecordName),
                                                                AuditService.EXO_AUDITRECORD);
    List<AccessControlEntry> access = new ArrayList<AccessControlEntry>();
    access.add(new AccessControlEntry(SystemIdentity.ANY, PermissionType.SET_PROPERTY));
    access.add(new AccessControlEntry(SystemIdentity.ANY, PermissionType.READ));
    access.add(new AccessControlEntry(adminIdentity, PermissionType.REMOVE));

    AccessControlList exoAuditRecordAccessControlList = new AccessControlList(session.getUserID(),
                                                                              access);

    arNode.setACL(exoAuditRecordAccessControlList);

    arNode.setOrderNumber(Integer.parseInt(auditRecordName));
    // exo:auditRecord
    session.getTransientNodesManager().update(new ItemState(arNode,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) item).getInternalPath()),
                                              true);

    // jcr:primaryType
    TransientPropertyData arPrType = TransientPropertyData.createPropertyData(arNode,
                                                                              Constants.JCR_PRIMARYTYPE,
                                                                              PropertyType.NAME,
                                                                              false);
    arPrType.setValue(new TransientValueData(arNode.getPrimaryTypeName()));
    // exo:user
    TransientPropertyData arUser = TransientPropertyData.createPropertyData(arNode,
                                                                            AuditService.EXO_AUDITRECORD_USER,
                                                                            PropertyType.STRING,
                                                                            false);
    arUser.setValue(new TransientValueData(session.getUserID()));
    // exo:created
    TransientPropertyData arCreated = TransientPropertyData.createPropertyData(arNode,
                                                                               AuditService.EXO_AUDITRECORD_CREATED,
                                                                               PropertyType.DATE,
                                                                               false);
    arCreated.setValue(new TransientValueData(dm.getTransactManager()
                                                .getStorageDataManager()
                                                .getCurrentTime()));
    // exo:eventType
    TransientPropertyData arEventType = TransientPropertyData.createPropertyData(arNode,
                                                                                 AuditService.EXO_AUDITRECORD_EVENTTYPE,
                                                                                 PropertyType.LONG,
                                                                                 false);
    arEventType.setValue(new TransientValueData(eventType));

    // jcr:primaryType
    session.getTransientNodesManager().update(new ItemState(arPrType,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) item).getInternalPath()),
                                              true);
    // exo:user
    session.getTransientNodesManager().update(new ItemState(arUser,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) item).getInternalPath()),
                                              true);
    // exo:created
    session.getTransientNodesManager().update(new ItemState(arCreated,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) item).getInternalPath()),
                                              true);
    // exo:eventType
    session.getTransientNodesManager().update(new ItemState(arEventType,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) item).getInternalPath()),
                                              true);
    // exo:propertyName
    if (!item.isNode()) {
      TransientPropertyData propertyNameData = TransientPropertyData.createPropertyData(arNode,
                                                                                        EXO_AUDITRECORD_PROPERTYNAME,
                                                                                        PropertyType.STRING,
                                                                                        false);
      propertyNameData.setValue(new TransientValueData(((ItemImpl) item).getInternalName()));
      session.getTransientNodesManager().update(new ItemState(propertyNameData,
                                                              ItemState.ADDED,
                                                              true,
                                                              ((ItemImpl) item).getInternalPath()),
                                                true);

    }

    // Update lastRecord

    TransientPropertyData pLastRecord = (TransientPropertyData) auditSession.getDataManager()
                                                                            .getItemData(QPath.makeChildPath(auditHistory.getQPath(),
                                                                                                             EXO_AUDITHISTORY_LASTRECORD));

    pLastRecord.setValue(new TransientValueData(String.valueOf(auditRecordName)));

    // Update lastRecord
    session.getTransientNodesManager().update(new ItemState(pLastRecord,
                                                            ItemState.UPDATED,
                                                            true,
                                                            ((ItemImpl) item).getInternalPath()),
                                              true);

    if (log.isDebugEnabled())
      log.debug("Add audit record: " + " Item path="
          + ((ItemImpl) item).getLocation().getInternalPath().getAsString() + " User="
          + session.getUserID() + " EventType=" + eventType);

  }

  public void createHistory(Node node) throws RepositoryException {

    checkIfAuditable(node);

    AuditSession auditSession = new AuditSession(node);
    NodeData storage = auditSession.getAuditStorage();

    // here should be added to SessionDataManager:
    // nodeData: /exo:audit/itemUUID
    // its primaryType exo:auditHistory
    // exo:targetNode (ref to item)
    // exo:lastRecord = "0"
    // in itemData/auditHistory - pointer to history (UUID)

    SessionImpl session = (SessionImpl) node.getSession();

    InternalQName aiName = new InternalQName(null, ((ItemImpl) node).getData().getIdentifier());
    // exo:auditHistory
    TransientNodeData ahNode = TransientNodeData.createNodeData(storage,
                                                                aiName,
                                                                AuditService.EXO_AUDITHISTORY);

    List<AccessControlEntry> access = new ArrayList<AccessControlEntry>();
    access.add(new AccessControlEntry(SystemIdentity.ANY, PermissionType.ADD_NODE));
    access.add(new AccessControlEntry(SystemIdentity.ANY, PermissionType.READ));
    access.add(new AccessControlEntry(SystemIdentity.ANY, PermissionType.SET_PROPERTY));
    access.add(new AccessControlEntry(adminIdentity, PermissionType.REMOVE));

    AccessControlList exoAuditHistoryAccessControlList = new AccessControlList(session.getUserID(),
                                                                               access);

    ahNode.setACL(exoAuditHistoryAccessControlList);
    ahNode.setMixinTypeNames(new InternalQName[] { Constants.MIX_REFERENCEABLE,
        Constants.EXO_PRIVILEGEABLE });

    // jcr:primaryType
    TransientPropertyData aPrType = TransientPropertyData.createPropertyData(ahNode,
                                                                             Constants.JCR_PRIMARYTYPE,
                                                                             PropertyType.NAME,
                                                                             false);
    aPrType.setValue(new TransientValueData(ahNode.getPrimaryTypeName()));

    // jcr:uuid
    TransientPropertyData ahUuid = TransientPropertyData.createPropertyData(ahNode,
                                                                            Constants.JCR_UUID,
                                                                            PropertyType.STRING,
                                                                            false);
    ahUuid.setValue(new TransientValueData(ahNode.getIdentifier()));

    // jcr:mixinTypes
    TransientPropertyData ahMixinTypes = TransientPropertyData.createPropertyData(ahNode,
                                                                                  Constants.JCR_MIXINTYPES,
                                                                                  PropertyType.NAME,
                                                                                  false);

    List<ValueData> mixValues = new ArrayList<ValueData>();
    mixValues.add(new TransientValueData(Constants.MIX_REFERENCEABLE));
    mixValues.add(new TransientValueData(Constants.EXO_PRIVILEGEABLE));
    ahMixinTypes.setValues(mixValues);

    // EXO_PERMISSIONS

    List<ValueData> permsValues = new ArrayList<ValueData>();
    for (int i = 0; i < ahNode.getACL().getPermissionEntries().size(); i++) {
      AccessControlEntry entry = ahNode.getACL().getPermissionEntries().get(i);
      permsValues.add(new TransientValueData(entry));
    }
    TransientPropertyData exoAuditPerms = TransientPropertyData.createPropertyData(ahNode,
                                                                                   Constants.EXO_PERMISSIONS,
                                                                                   ExtendedPropertyType.PERMISSION,
                                                                                   true,
                                                                                   permsValues);

    // exo:targetNode
    TransientPropertyData ahTargetNode = TransientPropertyData.createPropertyData(ahNode,
                                                                                  AuditService.EXO_AUDITHISTORY_TARGETNODE,
                                                                                  PropertyType.REFERENCE,
                                                                                  false);
    ahTargetNode.setValue(new TransientValueData(((ItemImpl) node).getData().getIdentifier()));

    // exo:lastRecord
    TransientPropertyData ahLastRecord = TransientPropertyData.createPropertyData(ahNode,
                                                                                  AuditService.EXO_AUDITHISTORY_LASTRECORD,
                                                                                  PropertyType.STRING,
                                                                                  false);
    ahLastRecord.setValue(new TransientValueData("0"));

    // node exo:auditHistory
    TransientPropertyData pAuditHistory = TransientPropertyData.createPropertyData((NodeData) ((ItemImpl) node).getData(),
                                                                                   AuditService.EXO_AUDITHISTORY,
                                                                                   PropertyType.STRING,
                                                                                   false);
    pAuditHistory.setValue(new TransientValueData(new Identifier(ahNode.getIdentifier())));

    session.getTransientNodesManager().update(new ItemState(ahNode,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) node).getInternalPath()),
                                              true);

    session.getTransientNodesManager().update(new ItemState(aPrType,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) node).getInternalPath()),
                                              true);

    session.getTransientNodesManager().update(new ItemState(ahUuid,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) node).getInternalPath()),
                                              true);

    session.getTransientNodesManager().update(new ItemState(ahMixinTypes,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) node).getInternalPath()),
                                              true);

    session.getTransientNodesManager().update(new ItemState(exoAuditPerms,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) node).getInternalPath()),
                                              true);

    session.getTransientNodesManager().update(new ItemState(ahTargetNode,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) node).getInternalPath()),
                                              true);

    session.getTransientNodesManager().update(new ItemState(ahLastRecord,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) node).getInternalPath()),
                                              true);

    session.getTransientNodesManager().update(new ItemState(pAuditHistory,
                                                            ItemState.ADDED,
                                                            true,
                                                            ((ItemImpl) node).getInternalPath()),
                                              true);

  }

  public AuditHistory getHistory(Node node) throws RepositoryException,
                                           UnsupportedOperationException {

    // get history for this item and create AuditHistory object
    AuditSession auditSession = new AuditSession(node);
    SessionDataManager dm = auditSession.getDataManager();
    NodeData storage = auditSession.getAuditHistoryNodeData();
    if (storage == null) {
      throw new RepositoryException("Audit history for node " + node.getPath() + "not found");
    }
    List<AuditRecord> auditRecords = new ArrayList<AuditRecord>();
    // AuditRecord aRecord = null;
    ValueFactoryImpl vf = (ValueFactoryImpl) node.getSession().getValueFactory();
    // Search all auditRecords
    List<NodeData> auditRecordsNodeData = dm.getChildNodesData(storage);
    for (NodeData nodeData : auditRecordsNodeData) {
      // Serching properties
      List<PropertyData> auditRecordNodeData = dm.getChildPropertiesData(nodeData);
      // define variables
      String user = null;
      InternalQName propertyName = null;
      int eventType = -1;
      Calendar date = null;
      // loading data
      for (PropertyData propertyData : auditRecordNodeData) {
        if (propertyData.getQPath().getName().equals(AuditService.EXO_AUDITRECORD_USER)) {
          user = vf.loadValue((TransientValueData) propertyData.getValues().get(0),
                              PropertyType.STRING).getString();
        } else if (propertyData.getQPath().getName().equals(AuditService.EXO_AUDITRECORD_EVENTTYPE)) {
          eventType = (int) vf.loadValue((TransientValueData) propertyData.getValues().get(0),
                                         PropertyType.LONG).getLong();
        } else if (propertyData.getQPath().getName().equals(AuditService.EXO_AUDITRECORD_CREATED)) {
          date = vf.loadValue((TransientValueData) propertyData.getValues().get(0),
                              PropertyType.DATE).getDate();
        } else if (propertyData.getQPath()
                               .getName()
                               .equals(AuditService.EXO_AUDITRECORD_PROPERTYNAME)) {
          try {
            propertyName = InternalQName.parse(new String(propertyData.getValues()
                                                                      .get(0)
                                                                      .getAsByteArray()));
          } catch (IOException e) {
            throw new RepositoryException(e);
          } catch (IllegalNameException e) {
            throw new RepositoryException(e);
          }
        }
      }
      // add audit record
      auditRecords.add(new AuditRecord(user, eventType, date, propertyName));
    }
    return new AuditHistory(node, auditRecords);
  }

  public boolean hasHistory(Node node) {
    NodeData data;
    try {
      AuditSession auditSession = new AuditSession(node);
      data = auditSession.getAuditHistoryNodeData();
    } catch (RepositoryException e) {
      return false;
    }
    return (data == null) ? false : true;
  }

  public void removeHistory(Node node) throws RepositoryException {
    AuditSession auditSession = new AuditSession(node);
    NodeData storage = auditSession.getAuditHistoryNodeData();
    // remove /jcr:system/exo:auditStorage/itemID
    // (delete in SessionDataManager)
    if (storage != null) {
      SessionImpl session = (SessionImpl) node.getSession();
      session.getTransientNodesManager().delete(storage);

    } else
      throw new RepositoryException("Audit history for node " + node.getPath() + " not found");
  }

  private void checkIfAuditable(Item item) throws RepositoryException,
                                          UnsupportedOperationException {
    NodeImpl node = (item.isNode()) ? (NodeImpl) item : (NodeImpl) item.getParent();
    if (!node.isNodeType("exo:auditable"))
      throw new ConstraintViolationException("exo:auditable node expected at: " + node.getPath());
  }

  private class AuditSession {

    private final SessionImpl        session;

    private final SessionDataManager dm;

    private ExtendedNode             node;

    private AuditSession(Item item) throws RepositoryException {
      session = (SessionImpl) item.getSession();
      if (item.isNode())
        node = (ExtendedNode) item;
      else
        node = (ExtendedNode) item.getParent();
      if (!node.isNodeType(AuditService.EXO_AUDITABLE))
        throw new RepositoryException("Node is not exo:auditable " + node.getPath());

      dm = session.getTransientNodesManager();
    }

    private NodeData getAuditHistoryNodeData() throws RepositoryException {
      // searching uuid of corresponding EXO_AUDITHISTORY node
      PropertyData pData = (PropertyData) dm.getItemData((NodeData) ((NodeImpl) node).getData(),
                                                         new QPathEntry(AuditService.EXO_AUDITHISTORY,
                                                                        0));
      if (pData != null)
        try {
          String ahUuid = new String(pData.getValues().get(0).getAsByteArray(),
                                     Constants.DEFAULT_ENCODING);
          return (NodeData) dm.getItemData(ahUuid);
        } catch (UnsupportedEncodingException e) {
          throw new RepositoryException("Error getAuditHistory converting to string");
        } catch (IllegalStateException e) {
          throw new RepositoryException("Error getAuditHistory converting to string");
        } catch (IOException e) {
          throw new RepositoryException("Error getAuditHistory converting to string");
        }
      return null;
    }

    private NodeData getAuditStorage() throws RepositoryException {

      ItemData storage = session.getTransientNodesManager().getItemData(AUDIT_STORAGE_ID);

      if (storage == null) {
        SessionChangesLog changesLog = new SessionChangesLog(session.getId());

        // here should be added to TransactionalDataManager (i.e. saved
        // immediatelly!
        // nodeData: /exo:audit with UUID = AUDIT_STORAGE_ID
        // its primaryType exo:auditStorage
        TransientNodeData exoAuditNode = TransientNodeData.createNodeData((NodeData) (session.getRootNode()).getData(),
                                                                          AuditService.EXO_AUDIT,
                                                                          AuditService.EXO_AUDITSTORAGE,
                                                                          AuditService.AUDIT_STORAGE_ID);

        List<AccessControlEntry> access = new ArrayList<AccessControlEntry>();
        access.add(new AccessControlEntry(SystemIdentity.ANY, PermissionType.ADD_NODE));
        access.add(new AccessControlEntry(adminIdentity, PermissionType.READ));
        access.add(new AccessControlEntry(adminIdentity, PermissionType.REMOVE));

        AccessControlList exoAuditAccessControlList = new AccessControlList(SystemIdentity.SYSTEM,
                                                                            access);

        exoAuditNode.setACL(exoAuditAccessControlList);

        InternalQName[] mixins = new InternalQName[] { Constants.EXO_PRIVILEGEABLE,
            Constants.MIX_REFERENCEABLE };
        exoAuditNode.setMixinTypeNames(mixins);

        // jcr:primaryType
        TransientPropertyData exoAuditPrType = TransientPropertyData.createPropertyData(exoAuditNode,
                                                                                        Constants.JCR_PRIMARYTYPE,
                                                                                        PropertyType.NAME,
                                                                                        false);
        exoAuditPrType.setValue(new TransientValueData(exoAuditNode.getPrimaryTypeName()));

        // jcr:uuid
        TransientPropertyData exoAuditUuid = TransientPropertyData.createPropertyData(exoAuditNode,
                                                                                      Constants.JCR_UUID,
                                                                                      PropertyType.STRING,
                                                                                      false);
        exoAuditUuid.setValue(new TransientValueData(exoAuditNode.getIdentifier()));

        // jcr:mixinTypes
        TransientPropertyData exoAuditMixinTypes = TransientPropertyData.createPropertyData(exoAuditNode,
                                                                                            Constants.JCR_MIXINTYPES,
                                                                                            PropertyType.NAME,
                                                                                            true);
        List<ValueData> mixValues = new ArrayList<ValueData>();
        mixValues.add(new TransientValueData(Constants.MIX_REFERENCEABLE));
        mixValues.add(new TransientValueData(Constants.EXO_PRIVILEGEABLE));
        exoAuditMixinTypes.setValues(mixValues);

        // EXO_PERMISSIONS

        List<ValueData> permsValues = new ArrayList<ValueData>();
        for (int i = 0; i < exoAuditNode.getACL().getPermissionEntries().size(); i++) {
          AccessControlEntry entry = exoAuditNode.getACL().getPermissionEntries().get(i);
          permsValues.add(new TransientValueData(entry));
        }
        TransientPropertyData exoAuditPerms = TransientPropertyData.createPropertyData(exoAuditNode,
                                                                                       Constants.EXO_PERMISSIONS,
                                                                                       ExtendedPropertyType.PERMISSION,
                                                                                       true,
                                                                                       permsValues);

        changesLog.add(ItemState.createAddedState(exoAuditNode));
        changesLog.add(ItemState.createAddedState(exoAuditPrType));
        changesLog.add(ItemState.createAddedState(exoAuditUuid));
        changesLog.add(ItemState.createAddedState(exoAuditMixinTypes));
        changesLog.add(ItemState.createAddedState(exoAuditPerms));

        session.getTransientNodesManager().getTransactManager().save(changesLog);
        storage = session.getTransientNodesManager().getItemData(AUDIT_STORAGE_ID);
      }
      if (!storage.isNode())
        throw new RepositoryException("Item with uuid " + AUDIT_STORAGE_ID + " should be node  ");
      return (NodeData) storage;
    }

    private SessionDataManager getDataManager() {
      return dm;
    }
  }

}
