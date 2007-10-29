/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL 19.12.2006 Helper class. Contains some
 * functions for a version history operations. Actually it's a wrapper for
 * NodeData with additional methods. For use instead a VersionHistoryImpl.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: VersionHistoryDataHelper.java 17564 2007-07-06 15:26:07Z
 *          peterit $
 */
public class VersionHistoryDataHelper extends TransientNodeData {

  protected final ItemDataConsumer    dataManager;

  protected final NodeTypeManagerImpl ntManager;

  private final String versionHistoryIdentifier;

  private final String baseVersionIdentifier;

  /**
   * Create helper using existed version history node data
   * 
   * @param source - existed version history node data
   * @param dataManager
   * @param ntManager
   */
  public VersionHistoryDataHelper(NodeData source,
                                  ItemDataConsumer dataManager,
                                  NodeTypeManagerImpl ntManager) {
    super(source.getQPath(),
          source.getIdentifier(),
          source.getPersistedVersion(),
          source.getPrimaryTypeName(),
          source.getMixinTypeNames(),
          source.getOrderNumber(),
          source.getParentIdentifier(),
          source.getACL());

    this.dataManager = dataManager;
    this.ntManager = ntManager;
    this.versionHistoryIdentifier = IdGenerator.generate();
    this.baseVersionIdentifier = IdGenerator.generate();
  }

  /**
   * Create helper as we create a new version history. All changes will be
   * placed into changes log. No persisted changes will be performed.
   * 
   * @param versionable - mix:versionable node data
   * @param changes - changes log
   * @param dataManager
   * @param ntManager
   * @throws RepositoryException
   */
  public VersionHistoryDataHelper(NodeData versionable,
                                  PlainChangesLogImpl changes,
                                  ItemDataConsumer dataManager,
                                  NodeTypeManagerImpl ntManager) throws RepositoryException {
    this.dataManager = dataManager;
    this.ntManager = ntManager;
    this.versionHistoryIdentifier = IdGenerator.generate();
    this.baseVersionIdentifier = IdGenerator.generate();

    TransientNodeData vh = init(versionable, changes);

    // TransientItemData
    this.parentIdentifier = vh.getParentIdentifier().intern();
    this.identifier = vh.getIdentifier().intern();
    this.qpath = vh.getQPath();
    this.persistedVersion = vh.getPersistedVersion();

    // TransientNodeData
    this.primaryTypeName = vh.getPrimaryTypeName();
    this.mixinTypeNames = vh.getMixinTypeNames();
    this.orderNum = vh.getOrderNumber();
    this.acl = vh.getACL();
  }

  /**
   * Create helper as we create a new version history. All changes will be
   * placed into changes log. No persisted changes will be performed.
   * 
   * @param versionable - mix:versionable node data
   * @param changes - changes log
   * @param dataManager
   * @param ntManager
   * @throws RepositoryException
   */
  public VersionHistoryDataHelper(NodeData versionable,
                                  PlainChangesLogImpl changes,
                                  ItemDataConsumer dataManager,
                                  NodeTypeManagerImpl ntManager,
                                  String versionHistoryIdentifier,
                                  String baseVersionIdentifier) throws RepositoryException {
    this.dataManager = dataManager;
    this.ntManager = ntManager;
    this.versionHistoryIdentifier = versionHistoryIdentifier;
    this.baseVersionIdentifier = baseVersionIdentifier;

    TransientNodeData vh = init(versionable, changes);

    // TransientItemData
    this.parentIdentifier = vh.getParentIdentifier().intern();
    this.identifier = vh.getIdentifier().intern();
    this.qpath = vh.getQPath();
    this.persistedVersion = vh.getPersistedVersion();

    // TransientNodeData
    this.primaryTypeName = vh.getPrimaryTypeName();
    this.mixinTypeNames = vh.getMixinTypeNames();
    this.orderNum = vh.getOrderNumber();
    this.acl = vh.getACL();
  }

  public List<NodeData> getAllVersionsData() throws RepositoryException {

    NodeData vData = (NodeData) dataManager.getItemData(getIdentifier());

    NodeData rootVersion = (NodeData) dataManager.getItemData(vData,
                                                              new QPathEntry(Constants.JCR_ROOTVERSION,
                                                                             0));

    List<NodeData> vChilds = new ArrayList<NodeData>();

    // should be first in list
    vChilds.add(rootVersion);

    for (NodeData cnd : dataManager.getChildNodesData(vData)) {
      if (!cnd.getQPath().getName().equals(Constants.JCR_ROOTVERSION)
          && ntManager.isNodeType(Constants.NT_VERSION, cnd.getPrimaryTypeName()))
        vChilds.add(cnd);
    }

    return vChilds;
  }

  public NodeData getLastVersionData() throws RepositoryException {
    List<NodeData> versionsData = getAllVersionsData();

    NodeData lastVersionData = null;
    Calendar lastCreated = null;
    for (NodeData vd : versionsData) {

      PropertyData createdData = (PropertyData) dataManager.getItemData(vd,
                                                                        new QPathEntry(Constants.JCR_CREATED,
                                                                                       0));

      if (createdData == null)
        throw new VersionException("jcr:created is not found, version: "
            + vd.getQPath().getAsString());

      Calendar created = null;
      try {
        created = new JCRDateFormat().deserialize(new String(createdData.getValues()
                                                                        .get(0)
                                                                        .getAsByteArray()));
      } catch (IOException e) {
        throw new RepositoryException(e);
      }

      if (lastVersionData == null || created.after(lastCreated)) {
        lastCreated = created;
        lastVersionData = vd;
      }
    }
    return lastVersionData;
  }

  public NodeData getVersionData(InternalQName versionQName) throws VersionException,
      RepositoryException {
    return (NodeData) dataManager.getItemData(this, new QPathEntry(versionQName, 0));
  }

  public NodeData getVersionLabelsData() throws VersionException, RepositoryException {
    return (NodeData) dataManager.getItemData(this, new QPathEntry(Constants.JCR_VERSIONLABELS, 0));
  }

  public List<PropertyData> getVersionLabels() throws VersionException, RepositoryException {
    List<PropertyData> labelsList = dataManager.getChildPropertiesData(getVersionLabelsData());

    return labelsList;
  }

  public NodeData getVersionDataByLabel(InternalQName labelQName) throws VersionException,
      RepositoryException {

    List<PropertyData> labelsList = getVersionLabels();
    for (PropertyData prop : labelsList) {
      if (prop.getQPath().getName().equals(labelQName)) {
        // label found
        try {
          String versionIdentifier = new String(prop.getValues().get(0).getAsByteArray());
          return (NodeData) dataManager.getItemData(versionIdentifier);
        } catch (IllegalStateException e) {
          throw new RepositoryException("Version label data error: " + e.getMessage(), e);
        } catch (IOException e) {
          throw new RepositoryException("Version label data reading error: " + e.getMessage(), e);
        }
      }
    }

    return null;
  }

  private TransientNodeData init(NodeData versionable, PlainChangesLogImpl changes) throws RepositoryException {


    // ----- VERSION STORAGE nodes -----
    // ----- version history -----
    NodeData rootItem = (NodeData) dataManager.getItemData(Constants.SYSTEM_UUID);

    NodeData versionStorageData = (NodeData) dataManager.getItemData(rootItem,
                                                                     new QPathEntry(Constants.JCR_VERSIONSTORAGE,
                                                                                    1)); // Constants.JCR_VERSION_STORAGE_PATH

    InternalQName vhName = new InternalQName(null, versionHistoryIdentifier);

    TransientNodeData versionHistory = TransientNodeData.createNodeData(versionStorageData,
                                                                        vhName,
                                                                        Constants.NT_VERSIONHISTORY);
    versionHistory.setIdentifier(versionHistoryIdentifier);

    // jcr:primaryType
    TransientPropertyData vhPrimaryType = TransientPropertyData.createPropertyData(versionHistory,
                                                                                   Constants.JCR_PRIMARYTYPE,
                                                                                   PropertyType.NAME,
                                                                                   false);
    vhPrimaryType.setValue(new TransientValueData(versionHistory.getPrimaryTypeName()));

    // jcr:uuid
    TransientPropertyData vhUuid = TransientPropertyData.createPropertyData(versionHistory,
                                                                            Constants.JCR_UUID,
                                                                            PropertyType.STRING,
                                                                            false);
    vhUuid.setValue(new TransientValueData(versionHistoryIdentifier));

    // jcr:versionableUuid
    TransientPropertyData vhVersionableUuid = TransientPropertyData
    // [PN] 10.04.07 VERSIONABLEUUID isn't referenceable!!!
    .createPropertyData(versionHistory, Constants.JCR_VERSIONABLEUUID, PropertyType.STRING, false);
    vhVersionableUuid.setValue(new TransientValueData(new Identifier(versionable.getIdentifier())));

    // ------ jcr:versionLabels ------
    NodeData vhVersionLabels = TransientNodeData.createNodeData(versionHistory,
                                                                Constants.JCR_VERSIONLABELS,
                                                                Constants.NT_VERSIONLABELS);

    // jcr:primaryType
    TransientPropertyData vlPrimaryType = TransientPropertyData.createPropertyData(vhVersionLabels,
                                                                                   Constants.JCR_PRIMARYTYPE,
                                                                                   PropertyType.NAME,
                                                                                   false);
    vlPrimaryType.setValue(new TransientValueData(vhVersionLabels.getPrimaryTypeName()));

    // ------ jcr:rootVersion ------
    NodeData rootVersionData = TransientNodeData.createNodeData(versionHistory,
                                                                Constants.JCR_ROOTVERSION,
                                                                Constants.NT_VERSION,
                                                                baseVersionIdentifier);

    // jcr:primaryType
    TransientPropertyData rvPrimaryType = TransientPropertyData.createPropertyData(rootVersionData,
                                                                                   Constants.JCR_PRIMARYTYPE,
                                                                                   PropertyType.NAME,
                                                                                   false);
    rvPrimaryType.setValue(new TransientValueData(rootVersionData.getPrimaryTypeName()));

    // jcr:uuid
    TransientPropertyData rvUuid = TransientPropertyData.createPropertyData(rootVersionData,
                                                                            Constants.JCR_UUID,
                                                                            PropertyType.STRING,
                                                                            false);
    rvUuid.setValue(new TransientValueData(baseVersionIdentifier));

    // jcr:mixinTypes
    TransientPropertyData rvMixinTypes = TransientPropertyData.createPropertyData(rootVersionData,
                                                                                  Constants.JCR_MIXINTYPES,
                                                                                  PropertyType.NAME,
                                                                                  true);
    rvMixinTypes.setValue(new TransientValueData(Constants.MIX_REFERENCEABLE));

    // jcr:created
    TransientPropertyData rvCreated = TransientPropertyData.createPropertyData(rootVersionData,
                                                                               Constants.JCR_CREATED,
                                                                               PropertyType.DATE,
                                                                               false);

    // TODO Current time source
    // rvCreated.setValue(new
    // TransientValueData(dataManager.getTransactManager()
    // .getStorageDataManager().getCurrentTime()));
    rvCreated.setValue(new TransientValueData(Calendar.getInstance()));

    // ----- VERSIONABLE properties -----
    // jcr:versionHistory
    TransientPropertyData vh = TransientPropertyData.createPropertyData(versionable,
                                                                        Constants.JCR_VERSIONHISTORY,
                                                                        PropertyType.REFERENCE,
                                                                        false);
    vh.setValue(new TransientValueData(new Identifier(versionHistoryIdentifier)));

    // jcr:baseVersion
    TransientPropertyData bv = TransientPropertyData.createPropertyData(versionable,
                                                                        Constants.JCR_BASEVERSION,
                                                                        PropertyType.REFERENCE,
                                                                        false);
    bv.setValue(new TransientValueData(new Identifier(baseVersionIdentifier)));

    // jcr:predecessors
    TransientPropertyData pd = TransientPropertyData.createPropertyData(versionable,
                                                                        Constants.JCR_PREDECESSORS,
                                                                        PropertyType.REFERENCE,
                                                                        true);
    pd.setValue(new TransientValueData(new Identifier(baseVersionIdentifier)));

    // update all
    QPath vpath = versionable.getQPath();
    changes.add(new ItemState(versionHistory, ItemState.ADDED, true, vpath));
    changes.add(new ItemState(vhPrimaryType, ItemState.ADDED, true, vpath));
    changes.add(new ItemState(vhUuid, ItemState.ADDED, true, vpath));
    changes.add(new ItemState(vhVersionableUuid, ItemState.ADDED, true, vpath));

    changes.add(new ItemState(vhVersionLabels, ItemState.ADDED, true, vpath));
    changes.add(new ItemState(vlPrimaryType, ItemState.ADDED, true, vpath));

    changes.add(new ItemState(rootVersionData, ItemState.ADDED, true, vpath));
    changes.add(new ItemState(rvPrimaryType, ItemState.ADDED, true, vpath));
    changes.add(new ItemState(rvMixinTypes, ItemState.ADDED, true, vpath));
    changes.add(new ItemState(rvUuid, ItemState.ADDED, true, vpath));
    changes.add(new ItemState(rvCreated, ItemState.ADDED, true, vpath));

    changes.add(ItemState.createAddedState(vh));
    changes.add(ItemState.createAddedState(bv));
    changes.add(ItemState.createAddedState(pd));

    return versionHistory;
  }
}
