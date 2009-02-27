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
    this.conflictedPathes = new ArrayList<QPath>();
    this.local = local;
    this.income = income;
    this.exporter = exporter;
    this.isLocalPriority = isLocalPriority;
    this.dataManager = dataManager;
    this.ntManager = ntManager;
  }

  /**
   * add.
   * 
   * @param path
   */
  public void add(QPath path) {
    for (int i = conflictedPathes.size() - 1; i >= 0; i++) {
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

      for (int i = conflictedPathes.size() - 1; i >= 0; i++) {
        if (path.equals(conflictedPathes.get(i)) || path.isDescendantOf(conflictedPathes.get(i)))
          break outer;
        else if (conflictedPathes.get(i).isDescendantOf(path)) {
          conflictedPathes.remove(i);
        }
      }

      conflictedPathes.add(path);
    }
  }

  /**
   * isPathConflicted.
   * 
   * @param path
   * @return
   */
  public boolean isPathConflicted(QPath path) {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      if (path.equals(conflictedPathes.get(i)) || path.isDescendantOf(conflictedPathes.get(i))) {
        return true;
      }
    }

    return false;
  }

  /**
   * remove.
   * 
   * @param path
   */
  public void remove(QPath path) {
    for (int i = conflictedPathes.size() - 1; i >= 0; i++) {
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
    if (isLocalPriority) {
      // apply income changes that not conflicted with local
      Iterator<ItemState> itemStates = income.getChanges();
      while (itemStates.hasNext()) {
        ItemState item = itemStates.next();
        if (!isPathConflicted(item.getData().getQPath()))
          iteration.add(item);
      }
    } else {
      // resolve conflicts
      // TODO every method use changesStorage.getChanges(conflictedPathes.get(i));
      restoreAddedItems(iteration);
      restoreDeletedItems(iteration);
      // restoreMixinChanges(iteration);
      // restoreUpdatesItems(iteration);

      // apply income changes
      iteration.addAll(income);
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

          ItemState lastState = getLastItemState(changes, item.getData().getIdentifier());
          if (lastState.getState() == ItemState.DELETED)
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

      Map<QPath, ItemState> needToExport = new HashMap<QPath, ItemState>();
      ItemData rootExportData = null;

      for (int j = 0; j < changes.size(); j++) {
        ItemState item = changes.get(j);
        if (item.getState() != ItemState.DELETED || item.isInternallyCreated())
          continue;

        ItemState nextItem = local.findNextState(item, item.getData().getIdentifier());
        if (nextItem != null && nextItem.getState() == ItemState.UPDATED)
          continue;

        if (getPrevState(changes, j) == -1) {
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
              iteration.add(new ItemState(newNode, ItemState.ADDED, true, node.getQPath()));
            }

            PropertyData prop = (PropertyData) item.getData();
            TransientPropertyData newProp = new TransientPropertyData(prop.getQPath(),
                                                                      prop.getIdentifier(),
                                                                      prop.getPersistedVersion(),
                                                                      prop.getType(),
                                                                      prop.getParentIdentifier(),
                                                                      prop.isMultiValued());
            newProp.setValues(((PropertyData) nextItem.getData()).getValues());
            iteration.add(new ItemState(newProp, ItemState.ADDED, true, prop.getQPath()));
          } else {
            needToExport.put(item.getData().getQPath(), item);
            if (rootExportData == null
                || rootExportData.getQPath().isDescendantOf(item.getData().getQPath()))
              rootExportData = item.getData();
          }
        }
      }

      // export
      if (rootExportData != null) {
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
  }

  /**
   * restoreMixinChanges.
   * 
   * @param iteration
   */
  private void restoreMixinChanges(EditableChangesStorage<ItemState> iteration) {
    // TODO Auto-generated method stub
  }

  /**
   * restoreUpdatesItems.
   * 
   * @param iteration
   */
  private void restoreUpdatesItems(EditableChangesStorage<ItemState> iteration) {
    // TODO Auto-generated method stub

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
  protected List<ItemState> deleleLockProperties(NodeData node) throws RepositoryException {
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

}
