/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.jcr.ext.replication.async.storage.StorageRuntimeException;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public abstract class AbstractMerger implements ChangesMerger {

  protected final boolean             localPriority;

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;
  
  protected final ResourcesHolder resHolder;

  public AbstractMerger(boolean localPriority,
                        RemoteExporter exporter,
                        DataManager dataManager,
                        NodeTypeDataManager ntManager,
                        ResourcesHolder resHolder) {
    this.localPriority = localPriority;
    this.exporter = exporter;
    this.dataManager = dataManager;
    this.ntManager = ntManager;
    this.resHolder = resHolder;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isLocalPriority() {
    return localPriority;
  }

  /**
   * {@inheritDoc}
   */
  public abstract EditableChangesStorage<ItemState> merge(ItemState itemChange,
                                                          ChangesStorage<ItemState> income,
                                                          ChangesStorage<ItemState> local,
                                                          String mergeTempDir,
                                                          List<QPath> skippedList,
                                                          List<QPath> restoredOrder) throws RepositoryException,
                                                                                    RemoteExportException,
                                                                                    IOException,
                                                                                    ClassCastException,
                                                                                    ClassNotFoundException,
                                                                                    StorageRuntimeException;

  /**
   * generateDeleleLockProperties.
   * 
   * @param node
   * @return
   * @throws RepositoryException
   */
  protected List<ItemState> generateDeleleLockProperties(NodeData node) throws RepositoryException {
    List<ItemState> result = new ArrayList<ItemState>();

    if (ntManager.isNodeType(Constants.MIX_LOCKABLE,
                             node.getPrimaryTypeName(),
                             node.getMixinTypeNames())) {

      ItemData item = dataManager.getItemData(node, new QPathEntry(Constants.JCR_LOCKISDEEP, 1));
      if (item != null)
        result.add(new ItemState(item, ItemState.DELETED, true, node.getQPath()));

      item = dataManager.getItemData(node, new QPathEntry(Constants.JCR_LOCKOWNER, 1));
      if (item != null)
        result.add(new ItemState(item, ItemState.DELETED, true, node.getQPath()));
    }

    return result;
  }

  /**
   * isPropertyAllowed.
   * 
   * @param propertyName
   * @param parent
   * @return
   */
  protected boolean isPropertyAllowed(InternalQName propertyName, NodeData parent) {
    PropertyDefinitionDatas pdef = ntManager.findPropertyDefinitions(propertyName,
                                                                     parent.getPrimaryTypeName(),
                                                                     parent.getMixinTypeNames());
    return pdef != null;
  }

  // TODO use HashMap instead of List
  /**
   * isOrderResotred.
   * 
   * @param restoredOrder
   * @param path
   * @return
   */
  protected boolean isOrderRestored(List<QPath> restoredOrder, QPath path) {
    for (QPath inPath : restoredOrder) {
      if (inPath.equals(path))
        return true;
    }

    return false;
  }

  /**
   * addSkippedVS.
   * 
   * @param skippedState
   * @param skippedList
   */
  protected void skipVSChanges(ItemState skippedState, List<QPath> skippedList) {
    if (!skippedState.getData().isNode())
      return;

    QPath sysPath = QPath.makeChildPath(Constants.ROOT_PATH, Constants.JCR_SYSTEM);
    QPath vsPath = QPath.makeChildPath(sysPath, Constants.JCR_VERSIONSTORAGE);

    QPath skippedPath = QPath.makeChildPath(vsPath, new InternalQName(null,
                                                                      skippedState.getData()
                                                                                  .getIdentifier()));

    skippedList.add(skippedPath);
  }
}
