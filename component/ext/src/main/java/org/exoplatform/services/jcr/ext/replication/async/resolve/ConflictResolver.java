/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.resolve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
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
import org.exoplatform.services.jcr.ext.replication.async.storage.MemberChangesStorage;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: ConflictCollector.java 111 2008-11-11 11:11:11Z $
 */
public class ConflictResolver {

  private final MemberChangesStorage<ItemState> local;

  private final MemberChangesStorage<ItemState> income;

  private final List<QPath>                     conflictedPathes;

  private final List<QPath>                     VSSkippedPathes;

  private final RemoteExporter                  exporter;

  private final DataManager                     dataManager;

  private final NodeTypeDataManager             ntManager;

  private final boolean                         isLocalPriority;

  /**
   * Log.
   */
  private static final Log                      LOG = ExoLogger.getLogger("jcr.ConflictResolver");

  /**
   * ConflictResolver constructor.
   * 
   * @param localPriority
   * @param changes
   */
  public ConflictResolver(boolean isLocalPriority,
                          MemberChangesStorage<ItemState> local,
                          MemberChangesStorage<ItemState> income,
                          RemoteExporter exporter,
                          DataManager dataManager,
                          NodeTypeDataManager ntManager) {
    this.VSSkippedPathes = new ArrayList<QPath>();
    this.conflictedPathes = new ArrayList<QPath>();
    this.local = local;
    this.income = income;
    this.exporter = exporter;
    this.isLocalPriority = isLocalPriority;
    this.dataManager = dataManager;
    this.ntManager = ntManager;
  }

  /**
   * addSkippedVSChanges.
   * 
   * @param identifier
   * @throws ClassCastException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void addSkippedVSChanges(String identifier) throws ClassCastException,
                                                    IOException,
                                                    ClassNotFoundException {
    QPath skippedPath = income.findVSChanges(identifier);
    if (skippedPath != null)
      VSSkippedPathes.add(skippedPath);
  }

  /**
   * add.
   * 
   * @param path
   */
  public void add(QPath path) {
    for (int i = conflictedPathes.size() - 1; i >= 0; i--) {
      if (path.equals(conflictedPathes.get(i)) || path.isDescendantOf(conflictedPathes.get(i)))
        return;
      else if (conflictedPathes.get(i).isDescendantOf(path)) {
        conflictedPathes.remove(i);
      }
    }

    conflictedPathes.add(path);
  }

  /**
   * addAll.
   * 
   * @param pathes
   */
  public void addAll(List<QPath> pathes) {
    outer: for (int j = 0; j < pathes.size(); j++) {
      QPath path = pathes.get(j);

      for (int i = conflictedPathes.size() - 1; i >= 0; i--) {
        if (path.equals(conflictedPathes.get(i)) || path.isDescendantOf(conflictedPathes.get(i)))
          continue outer;
        else if (conflictedPathes.get(i).isDescendantOf(path))
          conflictedPathes.remove(i);
      }

      conflictedPathes.add(path);
    }
  }

  /**
   * resolve.
   * 
   * @return
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws ClassCastException
   * @throws RemoteExportException
   * @throws RepositoryException
   */
  public void restore(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                  IOException,
                                                                  ClassNotFoundException,
                                                                  RemoteExportException,
                                                                  RepositoryException {
    if (!isLocalPriority) {
      restoreAddedItems(iteration);
      restoreDeletedItems(iteration);
      restoreUpdatedSNSItems(iteration);
    }
  }

  /**
   * applyIncomeChanges.
   * 
   * @param iteration
   * @throws ClassCastException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws RepositoryException
   */
  public void applyIncomeChanges(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                             IOException,
                                                                             ClassNotFoundException,
                                                                             RepositoryException {
    if (isLocalPriority) {
      // apply income changes that not conflicted with local
      Iterator<ItemState> itemStates = income.getChanges();
      while (itemStates.hasNext()) {
        ItemState item = itemStates.next();

        if (item.getData().getQPath().getName().equals(Constants.JCR_LOCKISDEEP)
            || item.getData().getQPath().getName().equals(Constants.JCR_LOCKOWNER)) {
          continue;
        }

        if (isChangesConflicted(item.getData().getQPath()))
          continue;

        if (isVSChangesConflicted(item.getData().getQPath()))
          continue;

        if (item.getState() == ItemState.DELETED && item.isPersisted() && item.isNode()) {
          for (ItemState itemState : deleleLockProperties((NodeData) item.getData()))
            iteration.add(itemState);
        }

        iteration.add(item);
      }
    } else {
      Iterator<ItemState> itemStates = income.getChanges();
      while (itemStates.hasNext()) {
        ItemState item = itemStates.next();

        if (item.getData().getQPath().getName().equals(Constants.JCR_LOCKISDEEP)
            || item.getData().getQPath().getName().equals(Constants.JCR_LOCKOWNER)) {
          continue;
        }

        if (item.getState() == ItemState.DELETED && item.isPersisted() && item.isNode()) {
          for (ItemState itemState : deleleLockProperties((NodeData) item.getData()))
            iteration.add(itemState);
        }

        iteration.add(item);
      }
    }
  }

  /**
   * restoreAddedItems.
   * 
   * @param iteration
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws ClassCastException
   * @throws RepositoryException
   */
  private void restoreAddedItems(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                             IOException,
                                                                             ClassNotFoundException,
                                                                             RepositoryException {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      List<ItemState> changes = local.getChanges(conflictedPathes.get(i));

      for (int j = changes.size() - 1; j >= 0; j--) {
        ItemState item = changes.get(j);
        if (item.getState() == ItemState.ADDED || item.getState() == ItemState.RENAMED) {

          if (hasDeleteState(changes, j, item.getData().getIdentifier()))
            continue;

          if (item.isNode()) {
            for (ItemState itemState : deleleLockProperties((NodeData) item.getData()))
              iteration.add(itemState);
          }

          iteration.add(new ItemState(item.getData(), ItemState.DELETED, true, item.getData()
                                                                                   .getQPath()));
        }
      }
    }
  }

  /**
   * restoreDeletedItems.
   * 
   * @param iteration
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws ClassCastException
   * @throws RemoteExportException
   */
  private void restoreDeletedItems(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                               IOException,
                                                                               ClassNotFoundException,
                                                                               RemoteExportException {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      List<ItemState> changes = local.getChanges(conflictedPathes.get(i));

      // restored items from changesLog
      List<ItemState> restoredItems = new ArrayList<ItemState>();

      // items need to export
      Set<QPath> needExportItem = new HashSet<QPath>();

      QPath rootExportPath = null;
      String rootExportIdentifier = null;

      // version history need to export
      Set<String> needExportItemVH = new HashSet<String>();

      for (int j = changes.size() - 1; j >= 0; j--) {
        ItemState item = changes.get(j);

        if (item.getState() == ItemState.DELETED) {

          ItemState nextItem = local.findNextState(item, item.getData().getIdentifier());
          if (nextItem != null && nextItem.getState() == ItemState.UPDATED)
            continue;

          if (getPrevState(changes, j) != -1)
            continue;

          // restore deleted node from local rename
          if (nextItem != null && nextItem.getState() == ItemState.RENAMED) {
            if (item.getData().isNode()) {
              NodeData node = (NodeData) item.getData();
              TransientNodeData newNode = new TransientNodeData(node.getQPath(),
                                                                node.getIdentifier(),
                                                                node.getPersistedVersion(),
                                                                node.getPrimaryTypeName(),
                                                                node.getMixinTypeNames(),
                                                                node.getOrderNumber(),
                                                                node.getParentIdentifier(),
                                                                node.getACL());
              restoredItems.add(new ItemState(newNode, ItemState.ADDED, true, node.getQPath()));
            } else {
              PropertyData prop = (PropertyData) item.getData();
              TransientPropertyData newProp = new TransientPropertyData(prop.getQPath(),
                                                                        prop.getIdentifier(),
                                                                        prop.getPersistedVersion(),
                                                                        prop.getType(),
                                                                        prop.getParentIdentifier(),
                                                                        prop.isMultiValued());
              newProp.setValues(((PropertyData) nextItem.getData()).getValues());
              restoredItems.add(new ItemState(newProp, ItemState.ADDED, true, prop.getQPath()));
            }
            continue;
          }

          // restore deleted node from income rename
          ItemState incomeChange = income.findItemState(item.getData().getIdentifier(),
                                                        item.getData().getQPath(),
                                                        ItemState.DELETED);
          if (incomeChange != null) {
            ItemState nextIncomeChange = income.findNextState(incomeChange,
                                                              incomeChange.getData()
                                                                          .getIdentifier());

            if (nextIncomeChange == null || nextIncomeChange.getState() != ItemState.UPDATED) {
              restoredItems.add(new ItemState(item.getData(),
                                              ItemState.ADDED,
                                              true,
                                              item.getData().getQPath()));

              if (nextIncomeChange != null && nextIncomeChange.getState() == ItemState.RENAMED) {
                if (incomeChange.getData().isNode()) {
                  NodeData node = (NodeData) incomeChange.getData();
                  TransientNodeData newNode = new TransientNodeData(node.getQPath(),
                                                                    node.getIdentifier(),
                                                                    node.getPersistedVersion(),
                                                                    node.getPrimaryTypeName(),
                                                                    node.getMixinTypeNames(),
                                                                    node.getOrderNumber(),
                                                                    node.getParentIdentifier(),
                                                                    node.getACL());
                  restoredItems.add(new ItemState(newNode, ItemState.UPDATED, true, item.getData()
                                                                                        .getQPath()));
                  needExportItemVH.add(incomeChange.getData().getIdentifier());
                } else {
                  PropertyData prop = (PropertyData) incomeChange.getData();
                  TransientPropertyData newProp = new TransientPropertyData(prop.getQPath(),
                                                                            prop.getIdentifier(),
                                                                            prop.getPersistedVersion(),
                                                                            prop.getType(),
                                                                            prop.getParentIdentifier(),
                                                                            prop.isMultiValued());
                  newProp.setValues(((PropertyData) nextIncomeChange.getData()).getValues());
                  restoredItems.add(new ItemState(newProp, ItemState.UPDATED, true, item.getData()
                                                                                        .getQPath()));
                }
              }

              continue;
            }
          }

          // restore deleted node
          if (!item.getData().isNode()
              && income.hasState(item.getData().getIdentifier(),
                                 item.getData().getQPath(),
                                 ItemState.UPDATED)) {
            restoredItems.add(new ItemState(item.getData(), ItemState.ADDED, true, item.getData()
                                                                                       .getQPath()));
            continue;
          }

          // add to export
          needExportItem.add(item.getData().getQPath());
          QPath nodePath = item.isNode() ? item.getData().getQPath() : item.getData()
                                                                           .getQPath()
                                                                           .makeParentPath();
          if (rootExportPath == null || rootExportPath.isDescendantOf(nodePath)) {
            rootExportPath = nodePath;
            rootExportIdentifier = item.isNode()
                ? item.getData().getIdentifier()
                : item.getData().getParentIdentifier();
          }

          // restore updated properties
        } else if (item.getState() == ItemState.UPDATED && !item.getData().isNode()) {
          ItemState lastState = getLastItemState(changes, item.getData().getIdentifier());
          if (lastState.getState() == ItemState.DELETED)
            continue;

          if (getPrevState(changes, j) != -1)
            continue;

          ItemState incomeChange = income.findItemState(item.getData().getIdentifier(),
                                                        item.getData().getQPath(),
                                                        ItemState.UPDATED);
          if (incomeChange != null)
            continue;

          incomeChange = income.findItemState(item.getData().getIdentifier(),
                                              item.getData().getQPath(),
                                              ItemState.DELETED);
          if (incomeChange != null) {
            ItemState nextIncomeState = income.findNextState(incomeChange,
                                                             incomeChange.getData().getIdentifier());
            if (nextIncomeState != null && nextIncomeState.getState() == ItemState.RENAMED) {
              restoredItems.add(new ItemState(nextIncomeState.getData(),
                                              ItemState.UPDATED,
                                              true,
                                              item.getData().getQPath()));
            }

            continue;
          }

          // add to export
          needExportItem.add(item.getData().getQPath());
          QPath nodePath = item.getData().getQPath().makeParentPath();
          if (rootExportPath == null || rootExportPath.isDescendantOf(nodePath)) {
            rootExportPath = nodePath;
            rootExportIdentifier = item.getData().getParentIdentifier();
          }
        }
      }

      // restore from export
      if (rootExportIdentifier != null) {
        ChangesStorage<ItemState> exportedItems = exporter.exportItem(rootExportIdentifier);

        Iterator<ItemState> itemStates = exportedItems.getChanges();
        while (itemStates.hasNext()) {
          ItemState item = itemStates.next();
          if (needExportItem.contains(item.getData().getQPath())) {
            iteration.add(item);
          }
        }
      }

      // restore versionhistory from export
      Iterator<String> uuids = needExportItemVH.iterator();
      while (uuids.hasNext()) {
        String uuid = uuids.next();
        String vhUuid = income.findVHProperty(uuid);
        if (vhUuid != null) {
          iteration.addAll(exporter.exportItem(vhUuid));
        }
      }

      // restore from changes log
      for (int k = 0; k < restoredItems.size(); k++)
        iteration.add(restoredItems.get(k));
    }
  }

  /**
   * restoreUpdatesSNSItems.
   * 
   * @param iteration
   * @throws ClassCastException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void restoreUpdatedSNSItems(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                                  IOException,
                                                                                  ClassNotFoundException {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      List<ItemState> changes = local.getChanges(conflictedPathes.get(i));

      for (int j = changes.size() - 1; j >= 0; j--) {
        ItemState curItem = changes.get(j);

        if (curItem.getState() == ItemState.DELETED && !curItem.isPersisted()) {
          ItemState nextItem = local.findNextState(curItem, curItem.getData().getIdentifier());

          if (nextItem != null && nextItem.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(curItem);
            for (int k = 1; k <= updateSeq.size() - 1; k++) {
              ItemState item = updateSeq.get(k);
              NodeData node = (NodeData) item.getData();
              if (k == 1) {
                iteration.add(new ItemState(item.getData(),
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
                iteration.add(new ItemState(newItem,
                                            ItemState.UPDATED,
                                            item.isEventFire(),
                                            name,
                                            item.isInternallyCreated()));

              }
              if (k == updateSeq.size() - 1) {
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
                iteration.add(new ItemState(newItem,
                                            ItemState.UPDATED,
                                            item.isEventFire(),
                                            name,
                                            item.isInternallyCreated()));
              }
            }
          }
        }
      }
    }
  }

  /**
   * getLastState.
   * 
   * @param changes
   * @param identifier
   * @return
   */
  private ItemState getLastItemState(List<ItemState> changes, String identifier) {
    for (int i = changes.size() - 1; i >= 0; i--)
      if (changes.get(i).getData().getIdentifier().equals(identifier))
        return changes.get(i);

    return null;
  }

  /**
   * getNextItemState.
   * 
   * @param changes
   * @param startIndex
   * @param identifier
   * @return
   */
  private boolean hasDeleteState(List<ItemState> changes, int startIndex, String identifier) {
    for (int i = startIndex + 1; i < changes.size(); i++)
      if (changes.get(i).getState() == ItemState.DELETED
          && changes.get(i).getData().getIdentifier().equals(identifier))
        return true;

    return false;
  }

  /**
   * getFirstState.
   * 
   * @param changes
   * @param identifier
   * @return
   */
  private int getPrevState(List<ItemState> changes, int index) {
    String identifier = changes.get(index).getData().getIdentifier();

    for (int i = index - 1; i >= 0; i--)
      if (changes.get(i).getData().getIdentifier().equals(identifier))
        return changes.get(i).getState();

    return -1;
  }

  /**
   * generateDeleleLockProperties.
   * 
   * @param node
   * @return
   * @throws RepositoryException
   */
  private List<ItemState> deleleLockProperties(NodeData node) throws RepositoryException {
    List<ItemState> result = new ArrayList<ItemState>();

    InternalQName[] mixinTypeNames = node.getMixinTypeNames();
    InternalQName primaryTypeName = node.getPrimaryTypeName();
    if (mixinTypeNames != null && primaryTypeName != null
        && ntManager.isNodeType(Constants.MIX_LOCKABLE, primaryTypeName, mixinTypeNames)) {

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
   * isPathConflicted.
   * 
   * @param path
   * @return
   */
  private boolean isChangesConflicted(QPath path) {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      if (path.equals(conflictedPathes.get(i)) || path.isDescendantOf(conflictedPathes.get(i))) {
        return true;
      }
    }

    return false;
  }

  /**
   * isPathConflicted.
   * 
   * @param path
   * @return
   */
  private boolean isVSChangesConflicted(QPath path) {
    for (int i = 0; i < VSSkippedPathes.size(); i++) {
      if (path.equals(VSSkippedPathes.get(i)) || path.isDescendantOf(VSSkippedPathes.get(i))) {
        return true;
      }
    }

    return false;
  }
}
