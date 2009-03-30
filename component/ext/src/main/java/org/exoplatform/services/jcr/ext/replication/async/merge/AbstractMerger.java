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
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.jcr.ext.replication.async.storage.StorageRuntimeException;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

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

  protected final ResourcesHolder     resHolder;
  
  protected final FileCleaner fileCleaner;
  protected final int maxBufferSize;
  
  protected final ReaderSpoolFileHolder holder;
  
  public AbstractMerger(boolean localPriority,
                        RemoteExporter exporter,
                        DataManager dataManager,
                        NodeTypeDataManager ntManager,
                        ResourcesHolder resHolder, FileCleaner fileCleaner, int maxBufferSize, ReaderSpoolFileHolder holder) {
    this.localPriority = localPriority;
    this.exporter = exporter;
    this.dataManager = dataManager;
    this.ntManager = ntManager;
    this.resHolder = resHolder;
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.holder = holder;
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
   * generateRestoreRenamedItem.
   * 
   * @param deleteState
   * @param renameState
   * @return
   * @throws ClassCastException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  protected ItemState generateRestoreRenamedItem(ItemState deleteState, ItemState renameState) throws ClassCastException,
                                                                                              IOException,
                                                                                              ClassNotFoundException {

    if (deleteState.getData().isNode()) {
      NodeData node = (NodeData) deleteState.getData();
      TransientNodeData newNode = new TransientNodeData(node.getQPath(),
                                                        node.getIdentifier(),
                                                        node.getPersistedVersion(),
                                                        node.getPrimaryTypeName(),
                                                        node.getMixinTypeNames(),
                                                        node.getOrderNumber(),
                                                        node.getParentIdentifier(),
                                                        node.getACL());

      return new ItemState(newNode, ItemState.ADDED, deleteState.isEventFire(), node.getQPath());
    }

    PropertyData prop = (PropertyData) deleteState.getData();
    TransientPropertyData newProp = new TransientPropertyData(prop.getQPath(),
                                                              prop.getIdentifier(),
                                                              prop.getPersistedVersion(),
                                                              prop.getType(),
                                                              prop.getParentIdentifier(),
                                                              prop.isMultiValued());
    newProp.setValues(((PropertyData) renameState.getData()).getValues());

    return new ItemState(newProp, ItemState.ADDED, deleteState.isEventFire(), prop.getQPath());
  }

  /**
   * Restore mixin changes.
   * 
   * @param firstState
   * @param storage
   * @return
   * @throws ClassCastException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws RepositoryException
   */
  protected List<ItemState> generateRestoreMixinChanges(ItemState firstState,
                                                        ChangesStorage<ItemState> storage) throws ClassCastException,
                                                                                          IOException,
                                                                                          ClassNotFoundException,
                                                                                          RepositoryException {

    List<ItemState> resultState = new ArrayList<ItemState>();

    // restore local changes
    resultState.add(firstState);

    List<ItemState> localMixinSeq = storage.getMixinSequence(firstState);
    for (int i = localMixinSeq.size() - 1; i > 0; i--) {
      ItemState item = localMixinSeq.get(i);

      if (item.getState() == ItemState.ADDED) {

        // delete lock properties if present
        if (item.getData().isNode() && item.getState() == ItemState.DELETED) {
          for (ItemState st : generateDeleleLockProperties((NodeData) item.getData()))
            resultState.add(st);
        }

        resultState.add(new ItemState(item.getData(),
                                      ItemState.DELETED,
                                      item.isEventFire(),
                                      item.getData().getQPath()));

      } else if (item.getState() == ItemState.DELETED) {
        resultState.add(new ItemState(item.getData(),
                                      ItemState.ADDED,
                                      item.isEventFire(),
                                      item.getData().getQPath()));
      }
    }

    return resultState;
  }

  /**
   * Restore original order for SNS nodes.
   * 
   * @param firstState
   * @param storage
   * @return
   * @throws ClassCastException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  protected List<ItemState> generateRestoreOrder(ItemState firstState,
                                                 ChangesStorage<ItemState> storage) throws ClassCastException,
                                                                                   IOException,
                                                                                   ClassNotFoundException {

    List<ItemState> resultState = new ArrayList<ItemState>();

    List<ItemState> updateSeq = storage.getUpdateSequence(firstState);
    for (int i = 1; i <= updateSeq.size() - 1; i++) {
      ItemState item = updateSeq.get(i);
      NodeData node = (NodeData) item.getData();
      if (i == 1) {
        resultState.add(new ItemState(item.getData(),
                                      ItemState.DELETED,
                                      item.isEventFire(),
                                      item.getData().getQPath(),
                                      item.isInternallyCreated(),
                                      false));
      } else {
        QPath name = QPath.makeChildPath(node.getQPath().makeParentPath(),
                                         node.getQPath().getName(),
                                         node.getQPath().getIndex() - 1);

        TransientNodeData newItem = new TransientNodeData(name,
                                                          node.getIdentifier(),
                                                          node.getPersistedVersion(),
                                                          node.getPrimaryTypeName(),
                                                          node.getMixinTypeNames(),
                                                          node.getOrderNumber(),
                                                          node.getParentIdentifier(),
                                                          node.getACL());
        resultState.add(new ItemState(newItem,
                                      ItemState.UPDATED,
                                      item.isEventFire(),
                                      name,
                                      item.isInternallyCreated()));

      }
      if (i == updateSeq.size() - 1) {
        item = updateSeq.get(1);
        node = (NodeData) item.getData();

        QPath name = QPath.makeChildPath(node.getQPath().makeParentPath(),
                                         node.getQPath().getName(),
                                         updateSeq.size() - 1);

        TransientNodeData newItem = new TransientNodeData(name,
                                                          node.getIdentifier(),
                                                          node.getPersistedVersion(),
                                                          node.getPrimaryTypeName(),
                                                          node.getMixinTypeNames(),
                                                          node.getOrderNumber(),
                                                          node.getParentIdentifier(),
                                                          node.getACL());
        resultState.add(new ItemState(newItem,
                                      ItemState.UPDATED,
                                      item.isEventFire(),
                                      name,
                                      item.isInternallyCreated()));
      }
    }

    return resultState;
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
   * isOrderResotred.
   * 
   * @param restoredOrder
   * @param path
   * @return
   */
  protected boolean isNodeRenamed(List<QPath> restoredOrder, QPath path) {
    for (QPath inPath : restoredOrder) {
      if (inPath.equals(path) || path.isDescendantOf(inPath))
        return true;
    }

    return false;
  }

  /**
   * accumulateSkippedList.
   * 
   * @param firstState
   * @param rootPath
   * @param storage
   * @param skippedList
   * @throws ClassCastException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  protected void accumulateSkippedList(ItemState firstState,
                                       QPath rootPath,
                                       ChangesStorage<ItemState> storage,
                                       List<QPath> skippedList) throws ClassCastException,
                                                               IOException,
                                                               ClassNotFoundException {

    Iterator<ItemState> changes = storage.getTreeChanges(firstState, rootPath).iterator();
    while (changes.hasNext()) {
      ItemState item = changes.next();
      skippedList.add(item.getData().getQPath());

      if (item.getData().isNode()) {
        QPath skippedPath = storage.findVSChanges(item.getData().getIdentifier());

        if (skippedPath != null)
          skippedList.add(skippedPath);
      }
    }
  }
}
