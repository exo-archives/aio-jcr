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

import java.io.File;
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
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableItemStatesStorage;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: AddMerger.java 24880 2008-12-11 11:49:03Z tolusha $
 */
public class UpdateMerger implements ChangesMerger {

  protected final boolean             localPriority;

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  public UpdateMerger(boolean localPriority,
                      RemoteExporter exporter,
                      DataManager dataManager,
                      NodeTypeDataManager ntManager) {
    this.localPriority = localPriority;
    this.exporter = exporter;
    this.dataManager = dataManager;
    this.ntManager = ntManager;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isLocalPriority() {
    return localPriority;
  }

  /**
   * {@inheritDoc}
   * 
   * @throws RepositoryException
   */
  public ChangesStorage<ItemState> merge(ItemState itemChange,
                                         ChangesStorage<ItemState> income,
                                         ChangesStorage<ItemState> local) throws RepositoryException,
                                                                         RemoteExportException,
                                                                         IOException {
    boolean itemChangeProcessed = false;

    // incomeState is DELETE state and nextIncomeState is UPDATE state
    ItemState incomeState = itemChange;
    ItemState nextIncomeState = null;
    if (incomeState.getData().isNode()) {
      nextIncomeState = income.getNextItemState(incomeState);
    }

    EditableChangesStorage<ItemState> resultEmptyState = new EditableItemStatesStorage<ItemState>(new File("./target")); // TODO
    // path
    EditableChangesStorage<ItemState> resultState = new EditableItemStatesStorage<ItemState>(new File("./target")); // TODO
    // path

    for (Iterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();
      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          break;
        case ItemState.UPDATED:
          if (!incomeData.isNode() && incomeData.getIdentifier().equals(localData.getIdentifier())) {
            return resultEmptyState;
          }
          break;
        case ItemState.DELETED:
          ItemState nextLocalState = local.getNextItemState(localState);

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (incomeData.isNode()
                && (income.getNextItemStateByUUIDOnUpdate(incomeState, localData.getIdentifier()) != null)) {
              return resultEmptyState;
            } else if (!incomeData.isNode()
                && incomeData.getParentIdentifier().equals(localData.getIdentifier())) {
              return resultEmptyState;
            }
            break;
          }

          // UPDATE
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            // same node updated
            ItemState nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                                      incomeState.getData()
                                                                                 .getIdentifier());
            if (incomeData.isNode() && nextItem != null) {
              return resultEmptyState;
            }

            // parent updated for node
            nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                            incomeState.getData()
                                                                       .getParentIdentifier());
            if (incomeData.isNode() && nextItem != null) {
              List<ItemState> incomeUpdateSequence = new ArrayList<ItemState>();
              incomeUpdateSequence.add(incomeState);
              incomeUpdateSequence.addAll(income.getUpdateSequence(incomeState));
              for (ItemState item : incomeUpdateSequence) {
                NodeData node = (NodeData) item.getData();
                QPath name = QPath.makeChildPath(localData.getQPath(),
                                                 node.getQPath().getEntries()[node.getQPath()
                                                                                  .getEntries().length - 1]);

                TransientNodeData newNode = new TransientNodeData(name,
                                                                  node.getIdentifier(),
                                                                  node.getPersistedVersion(),
                                                                  node.getPrimaryTypeName(),
                                                                  node.getMixinTypeNames(),
                                                                  node.getOrderNumber(),
                                                                  node.getParentIdentifier(),
                                                                  node.getACL());
                resultState.add(new ItemState(newNode,
                                              item.getState(),
                                              item.isEventFire(),
                                              name,
                                              item.isInternallyCreated(),
                                              item.isPersisted()));
              }
              itemChangeProcessed = true;
              break;
            }

            // parent updated for property
            nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                            incomeState.getData()
                                                                       .getParentIdentifier());
            if (!incomeData.isNode() && nextItem != null) {
              QPath name = QPath.makeChildPath(nextItem.getData().getQPath(),
                                               incomeData.getQPath().getEntries()[incomeData.getQPath()
                                                                                            .getEntries().length - 1]);
              PropertyData prop = (PropertyData) incomeData;
              TransientPropertyData item = new TransientPropertyData(name,
                                                                     prop.getIdentifier(),
                                                                     prop.getPersistedVersion(),
                                                                     prop.getType(),
                                                                     prop.getParentIdentifier(),
                                                                     prop.isMultiValued());
              item.setValues(prop.getValues());

              incomeState = new ItemState(item, ItemState.UPDATED, incomeState.isEventFire(), name);
              resultState.add(incomeState);
              itemChangeProcessed = true;
              break;
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (income.getNextItemStateByUUIDOnUpdate(incomeState, localState.getData()
                                                                             .getIdentifier()) != null) {
              return resultEmptyState;
            }
          } else {
            if (incomeData.getIdentifier().equals(localData.getIdentifier())) {
              return resultEmptyState;
            }
          }
          break;
        case ItemState.RENAMED:
          break;
        case ItemState.MIXIN_CHANGED:
          break;
        }
      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          ItemState nextLocalState = local.getNextItemState(localState);

          // UPDATE
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            // same node update
            ItemState nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                                      incomeState.getData()
                                                                                 .getIdentifier());
            if (incomeData.isNode() && nextItem != null) {
              // restore original order
              List<ItemState> localUpdateSequence = new ArrayList<ItemState>();

              localUpdateSequence.add(localState);
              localUpdateSequence.addAll(local.getUpdateSequence(localState));

              for (int i = localUpdateSequence.size() - 1; i >= 0; i--) {
                ItemState item = localUpdateSequence.get(i);
                NodeData node = (NodeData) item.getData();
                if (i == localUpdateSequence.size() - 1) {
                  resultState.add(new ItemState(item.getData(),
                                                ItemState.DELETED,
                                                item.isEventFire(),
                                                item.getData().getQPath()));
                } else {
                  QPath name = QPath.makeChildPath(node.getQPath().makeParentPath(),
                                                   node.getQPath().getName(),
                                                   i == 0
                                                       ? node.getQPath().getIndex()
                                                       : node.getQPath().getIndex() - 1);
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
                                                name));
                }
              }
              break;
            }

            // parent updated for node
            nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                            incomeState.getData()
                                                                       .getParentIdentifier());
            if (incomeData.isNode() && nextItem != null) {
              List<ItemState> incomeUpdateSequence = new ArrayList<ItemState>();
              incomeUpdateSequence.add(incomeState);
              incomeUpdateSequence.addAll(income.getUpdateSequence(incomeState));
              for (ItemState item : incomeUpdateSequence) {
                NodeData node = (NodeData) item.getData();
                QPath name = QPath.makeChildPath(localData.getQPath(),
                                                 node.getQPath().getEntries()[node.getQPath()
                                                                                  .getEntries().length - 1]);

                TransientNodeData newNode = new TransientNodeData(name,
                                                                  node.getIdentifier(),
                                                                  node.getPersistedVersion(),
                                                                  node.getPrimaryTypeName(),
                                                                  node.getMixinTypeNames(),
                                                                  node.getOrderNumber(),
                                                                  node.getParentIdentifier(),
                                                                  node.getACL());
                resultState.add(new ItemState(newNode,
                                              item.getState(),
                                              item.isEventFire(),
                                              name,
                                              item.isInternallyCreated(),
                                              item.isPersisted()));
              }
              itemChangeProcessed = true;
              break;
            }

            // parent updated for property
            nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                            incomeState.getData()
                                                                       .getParentIdentifier());
            if (!incomeData.isNode() && nextItem != null) {
              QPath name = QPath.makeChildPath(nextItem.getData().getQPath(),
                                               incomeData.getQPath().getEntries()[incomeData.getQPath()
                                                                                            .getEntries().length - 1]);
              PropertyData prop = (PropertyData) incomeData;
              TransientPropertyData item = new TransientPropertyData(name,
                                                                     prop.getIdentifier(),
                                                                     prop.getPersistedVersion(),
                                                                     prop.getType(),
                                                                     prop.getParentIdentifier(),
                                                                     prop.isMultiValued());
              item.setValues(prop.getValues());

              incomeState = new ItemState(item, ItemState.UPDATED, incomeState.isEventFire(), name);
              resultState.add(incomeState);
              itemChangeProcessed = true;
              break;
            }
            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            if (!incomeData.isNode()
                && localData.getIdentifier().equals(incomeData.getParentIdentifier())) {
              // delete node on new place
              resultState.add(new ItemState(nextLocalState.getData(),
                                            ItemState.DELETED,
                                            nextLocalState.isEventFire(),
                                            nextLocalState.getData().getQPath()));
              // restore parent
              resultState.addAll(exporter.exportItem(localData.getIdentifier()));

              itemChangeProcessed = true;
            } else if ((localData.isNode() && income.getNextItemStateByUUIDOnUpdate(incomeState,
                                                                                    localData.getIdentifier()) != null)
                || (!localData.isNode() && income.getNextItemStateByUUIDOnUpdate(incomeState,
                                                                                 localData.getParentIdentifier()) != null)) {

              List<ItemState> rename = local.getRenameSequence(localState);
              List<ItemState> update = income.getUpdateSequence(incomeState);
              for (int i = rename.size() - 1; i >= 0; i--) {
                ItemState item = rename.get(i);
                if (item.getState() == ItemState.RENAMED) { // generate delete state for new place
                  resultState.add(new ItemState(item.getData(),
                                                ItemState.DELETED,
                                                item.isEventFire(),
                                                item.getData().getQPath()));
                } else if (item.getState() == ItemState.DELETED) { // generate add state for old
                  // place
                  if (item.getData().isNode()) {
                    QPath name = QPath.makeChildPath(item.getData().getQPath().makeParentPath(),
                                                     item.getData().getQPath().getEntries()[item.getData()
                                                                                                .getQPath()
                                                                                                .getEntries().length - 1],
                                                     update.size());

                    NodeData node = (NodeData) item.getData();
                    TransientNodeData nodeData = new TransientNodeData(name,
                                                                       node.getIdentifier(),
                                                                       node.getPersistedVersion(),
                                                                       node.getPrimaryTypeName(),
                                                                       node.getMixinTypeNames(),
                                                                       node.getOrderNumber(),
                                                                       node.getParentIdentifier(),
                                                                       node.getACL());

                    resultState.add(new ItemState(nodeData,
                                                  ItemState.ADDED,
                                                  item.isEventFire(),
                                                  name));

                  } else {
                    QPath name = QPath.makeChildPath(QPath.makeChildPath(item.getData()
                                                                             .getQPath()
                                                                             .makeParentPath()
                                                                             .makeParentPath(),
                                                                         item.getData()
                                                                             .getQPath()
                                                                             .makeParentPath()
                                                                             .getEntries()[item.getData()
                                                                                               .getQPath()
                                                                                               .makeParentPath()
                                                                                               .getEntries().length - 1],
                                                                         update.size()),
                                                     item.getData().getQPath().getEntries()[item.getData()
                                                                                                .getQPath()
                                                                                                .getEntries().length - 1]);

                    PropertyData prop = (PropertyData) item.getData();
                    TransientPropertyData propData = new TransientPropertyData(name,
                                                                               prop.getIdentifier(),
                                                                               prop.getPersistedVersion(),
                                                                               prop.getType(),
                                                                               prop.getParentIdentifier(),
                                                                               prop.isMultiValued());
                    propData.setValues(((PropertyData) rename.get(rename.size() - i - 1).getData()).getValues());
                    resultState.add(new ItemState(propData,
                                                  ItemState.ADDED,
                                                  item.isEventFire(),
                                                  name));
                  }
                }
              }

              // apply income changes
              resultState.add(incomeState);
              if (nextIncomeState != null) {
                for (ItemState st : update)
                  resultState.add(st);
              }

              return resultState;
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (localData.getIdentifier().equals(incomeData.getParentIdentifier())) {
              resultState.addAll(exporter.exportItem(localData.getIdentifier()));
              itemChangeProcessed = true;
            } else if (income.getNextItemStateByUUIDOnUpdate(incomeState, localData.getIdentifier()) != null) {
              // generate ADD state from DELETE
              resultState.add(new ItemState(localData,
                                            ItemState.ADDED,
                                            localState.isEventFire(),
                                            localData.getQPath()));
            }
          } else {
            if (localData.getIdentifier().equals(incomeData.getIdentifier())) {
              // generate ADD state from DELETE
              resultState.add(new ItemState(localData,
                                            ItemState.ADDED,
                                            localState.isEventFire(),
                                            localData.getQPath()));
              itemChangeProcessed = true;
            }
          }
          break;
        case ItemState.RENAMED:
          break;
        case ItemState.MIXIN_CHANGED:
          break;
        }
      }
    }

    // apply income changes if not processed
    if (!itemChangeProcessed) {
      resultState.add(incomeState);
      if (nextIncomeState != null) {
        for (ItemState st : income.getUpdateSequence(incomeState))
          resultState.add(st);
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
}
