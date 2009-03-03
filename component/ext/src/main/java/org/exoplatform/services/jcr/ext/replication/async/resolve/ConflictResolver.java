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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
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
   * remove.
   * 
   * @param path
   */
  public void remove(QPath path) {
    for (int i = conflictedPathes.size() - 1; i >= 0; i--) {
      if (path.equals(conflictedPathes.get(i)))
        conflictedPathes.remove(i);
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
  public void resolve(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                  IOException,
                                                                  ClassNotFoundException,
                                                                  RemoteExportException,
                                                                  RepositoryException {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      System.out.println(conflictedPathes.get(i).getAsString());
    }
    System.out.println('\n');

    if (!isLocalPriority) {
      // resolve conflicts
      // TODO every method use changesStorage.getChanges(conflictedPathes.get(i));
      restoreAddedItems(iteration);
      restoreDeletedItems(iteration);
      // restoreMixinChanges(iteration);
      restoreUpdatesItems(iteration);
      restoreUpdatesSNSItems(iteration);
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
        if ((item.getState() == ItemState.ADDED && !item.isInternallyCreated())
            || item.getState() == ItemState.RENAMED) {

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

      List<ItemState> restoredItems = new ArrayList<ItemState>();
      QPath rootRestoredItem = null;

      Map<QPath, ItemState> needToExport = new HashMap<QPath, ItemState>();
      ItemData rootExportData = null;

      for (int j = changes.size() - 1; j >= 0; j--) {
        ItemState item = changes.get(j);
        if (item.getState() != ItemState.DELETED || item.isInternallyCreated())
          continue;

        ItemState nextItem = local.findNextState(item, item.getData().getIdentifier());
        if (nextItem != null && nextItem.getState() == ItemState.UPDATED)
          continue;

        if (getPrevState(changes, j) != -1)
          continue;

        // add to export
        needToExport.put(item.getData().getQPath(), item);
        if (rootExportData == null
            || rootExportData.getQPath().isDescendantOf(item.getData().getQPath()))
          rootExportData = item.getData();

        // restore deleted node from rename
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

          if (rootRestoredItem == null
              || rootRestoredItem.isDescendantOf(item.getData().getQPath()))
            rootRestoredItem = item.getData().getQPath();

          continue;
        }

        // restore deleted node
        ItemState incomeChange = income.findItemState(item.getData().getIdentifier(),
                                                      item.getData().getQPath(),
                                                      ItemState.DELETED);
        if (incomeChange != null) {
          restoredItems.add(new ItemState(item.getData(), ItemState.ADDED, true, item.getData()
                                                                                     .getQPath()));

          if (rootRestoredItem == null
              || rootRestoredItem.isDescendantOf(item.getData().getQPath()))
            rootRestoredItem = item.getData().getQPath();

          ItemState nextIncomeChange = income.findNextState(incomeChange,
                                                            incomeChange.getData().getIdentifier());
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

        // restore deleted node
        if (!item.getData().isNode()
            && income.hasState(item.getData().getIdentifier(),
                               item.getData().getQPath(),
                               ItemState.UPDATED)) {
          restoredItems.add(new ItemState(item.getData(), ItemState.ADDED, true, item.getData()
                                                                                     .getQPath()));

          if (rootRestoredItem == null
              || rootRestoredItem.isDescendantOf(item.getData().getQPath()))
            rootRestoredItem = item.getData().getQPath();

          continue;
        }
      }

      // export
      if (rootExportData != null) {
        if (rootRestoredItem == null || !rootExportData.getQPath().equals(rootRestoredItem)) {
          ChangesStorage<ItemState> exportedItems = exporter.exportItem(rootExportData.isNode()
              ? rootExportData.getIdentifier()
              : rootExportData.getParentIdentifier());

          Iterator<ItemState> itemStates = exportedItems.getChanges();
          while (itemStates.hasNext()) {
            ItemState item = itemStates.next();
            if (needToExport.get(item.getData().getQPath()) != null) {
              iteration.add(item);
            }
          }
        }
      }

      for (int k = 0; k < restoredItems.size(); k++)
        iteration.add(restoredItems.get(k));
    }
  }

  /**
   * restoreUpdatesItems.
   * 
   * @param iteration
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws ClassCastException
   */
  private void restoreUpdatesItems(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                               IOException,
                                                                               ClassNotFoundException {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      List<ItemState> changes = local.getChanges(conflictedPathes.get(i));

      for (int j = changes.size() - 1; j >= 0; j--) {
        ItemState item = changes.get(j);

        if (item.getState() == ItemState.UPDATED && !item.getData().isNode()) {
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
              iteration.add(new ItemState(nextIncomeState.getData(),
                                          ItemState.UPDATED,
                                          true,
                                          item.getData().getQPath()));
            }
          }
        }
      }
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
  private void restoreUpdatesSNSItems(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
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
