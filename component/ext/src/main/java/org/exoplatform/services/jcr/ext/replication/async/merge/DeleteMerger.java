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
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.BufferedItemStatesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.StorageRuntimeException;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: AddMerger.java 25356 2008-12-18 09:54:16Z tolusha $
 */
public class DeleteMerger extends AbstractMerger {

  public DeleteMerger(boolean localPriority,
                      RemoteExporter exporter,
                      DataManager dataManager,
                      NodeTypeDataManager ntManager) {
    super(localPriority, exporter, dataManager, ntManager);
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
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   */
  public EditableChangesStorage<ItemState> merge(ItemState itemChange,
                                                 ChangesStorage<ItemState> income,
                                                 ChangesStorage<ItemState> local,
                                                 String mergeTempDir,
                                                 List<QPath> skippedList,
                                                 List<QPath> restoredOrder) throws RepositoryException,
                                                                           RemoteExportException,
                                                                           IOException,
                                                                           ClassCastException,
                                                                           ClassNotFoundException,
                                                                           StorageRuntimeException {

    boolean itemChangeProcessed = false;

    ItemState incomeState = itemChange;
    EditableChangesStorage<ItemState> resultState = new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                                             null);

    for (Iterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();

      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      ItemState incParentNodeState = income.findNextState(incomeState,
                                                          incomeData.getParentIdentifier(),
                                                          incomeData.getQPath().makeParentPath(),
                                                          ItemState.DELETED);

      // skip lock properties
      if (!localData.isNode()) {
        if (localData.getQPath().getName().equals(Constants.JCR_LOCKISDEEP)
            || localData.getQPath().getName().equals(Constants.JCR_LOCKOWNER)) {
          continue;
        }
      }

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeState.getData().isNode() || incParentNodeState != null) {
            QPath incNodePath = incomeData.isNode()
                ? incomeData.getQPath()
                : incParentNodeState.getData().getQPath();

            if (localData.getQPath().isDescendantOf(incNodePath)) {
              skippedList.add(incNodePath);
              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
            }
          } else {
            if (!localData.isNode()) {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            if (incomeState.getData().isNode() || incParentNodeState != null) {
              QPath incNodePath = incomeData.isNode()
                  ? incomeData.getQPath()
                  : incParentNodeState.getData().getQPath();

              List<ItemState> updateSeq = local.getUpdateSequence(localState);
              for (ItemState item : updateSeq) {
                if (item.getData().getQPath().isDescendantOf(incNodePath)
                    || incNodePath.equals(item.getData().getQPath())
                    || incNodePath.isDescendantOf(item.getData().getQPath())) {

                  skippedList.add(incNodePath);
                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }
            } else {
              List<ItemState> updateSeq = local.getUpdateSequence(localState);
              for (ItemState item : updateSeq) {
                if (incomeData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  skippedList.add(incomeData.getQPath());
                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }
            }
            break;
          }

          // RENAMED sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            if (incomeState.getData().isNode() || incParentNodeState != null) {
              QPath incNodePath = incomeData.isNode()
                  ? incomeData.getQPath()
                  : incParentNodeState.getData().getQPath();

              QPath locNodePath = localData.isNode()
                  ? localData.getQPath()
                  : local.findNextState(localState,
                                        localData.getParentIdentifier(),
                                        localData.getQPath().makeParentPath(),
                                        ItemState.DELETED).getData().getQPath();

              QPath nextLocNodePath = localData.isNode()
                  ? nextLocalState.getData().getQPath()
                  : local.findNextState(localState,
                                        nextLocalState.getData().getParentIdentifier(),
                                        nextLocalState.getData().getQPath().makeParentPath(),
                                        ItemState.RENAMED).getData().getQPath();

              if (incNodePath.isDescendantOf(locNodePath) || incNodePath.equals(locNodePath)
                  || nextLocNodePath.isDescendantOf(incNodePath)) {
                skippedList.add(incNodePath);
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }
            break;
          }

          // DELETE
          if (incomeData.isNode() || incParentNodeState != null) {
            QPath incNodePath = incomeData.isNode()
                ? incomeData.getQPath()
                : incParentNodeState.getData().getQPath();

            if (incNodePath.isDescendantOf(localData.getQPath())
                || incNodePath.equals(localData.getQPath())) {
              skippedList.add(incNodePath);
              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
            }
          } else {
            if (localData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }
          }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (incomeData.isNode() || incParentNodeState != null) {
              QPath incNodePath = incomeData.isNode()
                  ? incomeData.getQPath()
                  : incParentNodeState.getData().getQPath();

              if (localData.getQPath().isDescendantOf(incNodePath)) {
                skippedList.add(incNodePath);
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            } else {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }
          }

          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.isNode() || incParentNodeState != null) {
            QPath incNodePath = incomeData.isNode()
                ? incomeData.getQPath()
                : incParentNodeState.getData().getQPath();

            if (localData.getQPath().equals(incNodePath)
                || localData.getQPath().isDescendantOf(incNodePath)) {
              skippedList.add(incNodePath);
              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
            }
          } else {
            List<ItemState> mixinSeq = local.getMixinSequence(localState);

            for (int i = 0; i < mixinSeq.size(); i++) {
              ItemState item = mixinSeq.get(i);
              if (!item.getData().isNode()) {
                if (item.getData().getQPath().equals(incomeData.getQPath())) {
                  skippedList.add(incomeData.getQPath());
                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }
            }
          }
          break;
        }

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:

          if (local.findNextState(localState,
                                  localData.getIdentifier(),
                                  localData.getQPath(),
                                  ItemState.DELETED) != null) {
            break;
          }

          if ((incomeData.isNode() || incParentNodeState != null)) {

            QPath incNodePath = incomeData.isNode()
                ? incomeData.getQPath()
                : incParentNodeState.getData().getQPath();

            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)) {

              List<ItemState> items = local.getChanges(localState, incNodePath, true);
              for (int i = items.size() - 1; i >= 0; i--) {
                ItemState item = items.get(i);

                if (local.findLastState(item.getData().getQPath()) != ItemState.DELETED) {

                  // delete lock properties if present
                  if (item.getData().isNode()) {
                    for (ItemState st : generateDeleleLockProperties((NodeData) item.getData()))
                      resultState.add(st);
                  }

                  resultState.add(new ItemState(item.getData(),
                                                ItemState.DELETED,
                                                item.isEventFire(),
                                                item.getData().getQPath()));
                }
              }

              // apply income changes for all subtree
              for (ItemState st : income.getChanges(incomeState, incNodePath)) {

                // delete lock properties if present
                if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                    resultState.add(inSt);
                }

                resultState.add(st);
              }

              return resultState;
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState st : updateSeq) {
              if (incomeData.getQPath().isDescendantOf(st.getData().getQPath())
                  || incomeData.getQPath().equals(st.getData().getQPath())) {

                if (!isOrderRestored(restoredOrder, localData.getQPath().makeParentPath())) {
                  restoredOrder.add(localData.getQPath().makeParentPath());

                  // restore original order
                  List<ItemState> locUpdateSeq = local.getUpdateSequence(localState);
                  for (int i = locUpdateSeq.size() - 1; i >= 0; i--) {
                    ItemState item = locUpdateSeq.get(i);
                    NodeData node = (NodeData) item.getData();
                    if (i == locUpdateSeq.size() - 1) {
                      resultState.add(new ItemState(item.getData(),
                                                    ItemState.DELETED,
                                                    item.isEventFire(),
                                                    item.getData().getQPath(),
                                                    item.isInternallyCreated(),
                                                    false));
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
                                                    name,
                                                    item.isInternallyCreated()));
                    }
                  }
                }
              }
            }
            break;
          }

          // RENAMED sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            if (local.findNextState(nextLocalState,
                                    nextLocalState.getData().getIdentifier(),
                                    nextLocalState.getData().getQPath(),
                                    ItemState.DELETED) != null) {
              break;
            }

            if ((incomeData.isNode() || incParentNodeState != null)) {

              if (localData.isNode()) {
                if (incomeData.getQPath().equals(localData.getQPath())) {

                  // delete lock properties if present
                  if (nextLocalState.getData().isNode())
                    for (ItemState st : generateDeleleLockProperties((NodeData) nextLocalState.getData()))
                      resultState.add(st);

                  resultState.add(new ItemState(nextLocalState.getData(),
                                                ItemState.DELETED,
                                                nextLocalState.isEventFire(),
                                                nextLocalState.getData().getQPath()));

                  itemChangeProcessed = true;
                  break;
                }
              }
            } else {
              if (localData.isNode()) {
                if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {

                  // delete lock properties if present
                  for (ItemState st : generateDeleleLockProperties((NodeData) nextLocalState.getData()))
                    resultState.add(st);

                  resultState.add(new ItemState(nextLocalState.getData(),
                                                ItemState.DELETED,
                                                nextLocalState.isEventFire(),
                                                nextLocalState.getData().getQPath()));
                  itemChangeProcessed = true;
                  break;
                }
              } else {
                if (incomeData.getQPath().equals(localData.getQPath())) {

                  resultState.add(new ItemState(nextLocalState.getData(),
                                                ItemState.DELETED,
                                                nextLocalState.isEventFire(),
                                                nextLocalState.getData().getQPath()));

                  itemChangeProcessed = true;
                  break;
                }
              }
            }
            break;
          }

          // DELETE
          if (local.findNextState(localState,
                                  localData.getIdentifier(),
                                  localData.getQPath(),
                                  ItemState.ADDED) != null) {
            break;
          }

          if (incomeData.isNode()) {
            if (localData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(localData.getQPath())) {
                return resultState;
              }
            }
          } else {
            if (localData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                return resultState;
              }
            } else {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                return resultState;
              }
            }
          }
          break;

        case ItemState.UPDATED:
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

      // delete lock properties if present
      if (incomeState.getData().isNode()) {
        for (ItemState st : generateDeleleLockProperties((NodeData) incomeState.getData()))
          resultState.add(st);
      }

      resultState.add(incomeState);
    }

    return resultState;
  }
}
