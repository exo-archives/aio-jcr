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
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.BufferedItemStatesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.jcr.ext.replication.async.storage.StorageRuntimeException;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: AddMerger.java 24880 2008-12-11 11:49:03Z tolusha $
 */
public class RenameMerger extends AbstractMerger {

  public RenameMerger(boolean localPriority,
                      RemoteExporter exporter,
                      DataManager dataManager,
                      NodeTypeDataManager ntManager,
                      ResourcesHolder resHolder) {
    super(localPriority, exporter, dataManager, ntManager, resHolder);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @throws RepositoryException
   */
  public EditableChangesStorage<ItemState> merge(ItemState itemChange,
                                                 ChangesStorage<ItemState> income,
                                                 ChangesStorage<ItemState> local,
                                                 String mergeTempDir,
                                                 List<QPath> skippedList,
                                                 List<QPath> restoredOrder) throws RemoteExportException,
                                                                           IOException,
                                                                           ClassCastException,
                                                                           ClassNotFoundException,
                                                                           StorageRuntimeException,
                                                                           RepositoryException {
    boolean itemChangeProcessed = false;

    // incomeState is DELETE state and nextIncomeState is RENAME state
    ItemState incomeState = itemChange;
    ItemState nextIncomeState = income.findNextState(incomeState, incomeState.getData()
                                                                             .getIdentifier());

    ItemState parentNodeState;

    QPath incNodePath = incomeState.getData().isNode()
        ? incomeState.getData().getQPath()
        : incomeState.getData().getQPath().makeParentPath();

    QPath nextIncNodePath = nextIncomeState.getData().isNode()
        ? nextIncomeState.getData().getQPath()
        : nextIncomeState.getData().getQPath().makeParentPath();

    EditableChangesStorage<ItemState> resultState = new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                                             null,
                                                                                             resHolder);

    for (Iterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();

      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

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
          if (localData.isNode()) {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)
                || localData.getQPath().equals(nextIncNodePath)
                || nextIncNodePath.equals(localData.getQPath())
                || nextIncNodePath.isDescendantOf(localData.getQPath())) {

              for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {
                skipVSChanges(st, income, skippedList);
                skippedList.add(st.getData().getQPath());
              }

              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                              null,
                                                              resHolder);
            }
          } else {
            if (localData.getQPath().isDescendantOf(incNodePath)) {

              for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {
                skipVSChanges(st, income, skippedList);
                skippedList.add(st.getData().getQPath());
              }

              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                              null,
                                                              resHolder);
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // Update sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState item : updateSeq) {
              if (item.getData().getQPath().isDescendantOf(incNodePath)
                  || incNodePath.equals(item.getData().getQPath())
                  || incNodePath.isDescendantOf(item.getData().getQPath())
                  || nextIncNodePath.isDescendantOf(item.getData().getQPath())) {

                for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {
                  skipVSChanges(st, income, skippedList);
                  skippedList.add(st.getData().getQPath());
                }

                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                null,
                                                                resHolder);
              }
            }
            break;
          }

          // Rename sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            QPath localPath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocalPath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            if (localPath.isDescendantOf(incNodePath) || localPath.equals(incNodePath)
                || nextIncNodePath.isDescendantOf(localPath)
                || nextIncNodePath.equals(nextLocalPath)
                || nextLocalPath.isDescendantOf(incNodePath)) {

              for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {
                skipVSChanges(st, income, skippedList);
                skippedList.add(st.getData().getQPath());
              }

              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                              null,
                                                              resHolder);
            }
            break;
          }

          // simple DELETE
          // TODO common block
          if (localData.isNode()) {
            if (incNodePath.isDescendantOf(localData.getQPath())
                || incNodePath.equals(localData.getQPath())
                || localData.getQPath().equals(incNodePath)
                || localData.getQPath().isDescendantOf(incNodePath)
                || nextIncNodePath.isDescendantOf(localData.getQPath())) {

              for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {
                skipVSChanges(st, income, skippedList);
                skippedList.add(st.getData().getQPath());
              }

              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                              null,
                                                              resHolder);
            }
          } else {
            if (incNodePath.isDescendantOf(localData.getQPath().makeParentPath())
                || incNodePath.equals(localData.getQPath().makeParentPath())
                || localData.getQPath().makeParentPath().equals(incNodePath)
                || localData.getQPath().makeParentPath().isDescendantOf(incNodePath)
                || nextIncNodePath.isDescendantOf(localData.getQPath().makeParentPath())) {

              for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {
                skipVSChanges(st, income, skippedList);
                skippedList.add(st.getData().getQPath());
              }

              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                              null,
                                                              resHolder);
            }
          }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)) {

              for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {
                skipVSChanges(st, income, skippedList);
                skippedList.add(st.getData().getQPath());
              }

              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                              null,
                                                              resHolder);
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (localData.getQPath().equals(incNodePath)
              || localData.getQPath().isDescendantOf(incNodePath)) {

            for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {
              skipVSChanges(st, income, skippedList);
              skippedList.add(st.getData().getQPath());
            }

            return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null, resHolder);
          }
          break;
        }

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.isNode()) {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)
                || localData.getQPath().isDescendantOf(nextIncNodePath)
                || localData.getQPath().equals(nextIncNodePath)) {

              // add DELETE state
              List<ItemState> items = local.getChanges(localState, localData.getQPath(), true);

              for (int i = items.size() - 1; i >= 0; i--) {
                if (local.findLastState(items.get(i).getData().getQPath()) != ItemState.DELETED) {

                  // Delete lock properties
                  if (items.get(i).getData().isNode()) {
                    for (ItemState st : generateDeleleLockProperties((NodeData) items.get(i)
                                                                                     .getData()))
                      resultState.add(st);
                  }

                  resultState.add(new ItemState(items.get(i).getData(),
                                                ItemState.DELETED,
                                                items.get(i).isEventFire(),
                                                items.get(i).getData().getQPath()));
                }
              }

              // add all state from income changes
              List<ItemState> rename = income.getRenameSequence(incomeState);
              for (ItemState st : income.getChanges(rename.get(0), rename.get(0)
                                                                         .getData()
                                                                         .getQPath())) {

                // delete lock properties if present
                if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                    resultState.add(inSt);
                }

                resultState.add(st);
              }

              for (ItemState st : income.getChanges(rename.get(rename.size() / 2),
                                                    rename.get(rename.size() / 2)
                                                          .getData()
                                                          .getQPath())) {

                // delete lock properties if present
                if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                    resultState.add(inSt);
                }

                resultState.add(st);
              }

              itemChangeProcessed = true;
            }

          } else {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)
                || localData.getQPath().isDescendantOf(nextIncNodePath)
                || localData.getQPath().equals(nextIncNodePath)) {

              // add DELETE state
              List<ItemState> items = local.getChanges(localState, localData.getQPath(), true);

              for (int i = items.size() - 1; i >= 0; i--) {
                if (local.findLastState(items.get(i).getData().getQPath()) != ItemState.DELETED) {

                  // delete lock properties if present
                  if (items.get(i).getData().isNode()) {
                    for (ItemState inSt : generateDeleleLockProperties((NodeData) items.get(i)
                                                                                       .getData()))
                      resultState.add(inSt);
                  }

                  resultState.add(new ItemState(items.get(i).getData(),
                                                ItemState.DELETED,
                                                items.get(i).isEventFire(),
                                                items.get(i).getData().getQPath()));
                }
              }

              // add all state from income changes
              List<ItemState> rename = income.getRenameSequence(incomeState);
              for (ItemState st : income.getChanges(rename.get(0), rename.get(0)
                                                                         .getData()
                                                                         .getQPath()
                                                                         .makeParentPath())) {

                // delete lock properties if present
                if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                    resultState.add(inSt);
                }

                resultState.add(st);
              }

              for (ItemState st : income.getChanges(rename.get(rename.size() / 2),
                                                    rename.get(rename.size() / 2)
                                                          .getData()
                                                          .getQPath()
                                                          .makeParentPath())) {

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

          // Update sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {

            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState st : updateSeq) {

              if (incNodePath.isDescendantOf(st.getData().getQPath())
                  || incNodePath.equals(st.getData().getQPath())
                  || nextIncNodePath.isDescendantOf(st.getData().getQPath())) {

                if (!isOrderRestored(restoredOrder, localData.getQPath().makeParentPath())) {
                  restoredOrder.add(localData.getQPath().makeParentPath());

                  // restore original order
                  for (ItemState inSt : generateRestoreOrder(localState, local))
                    resultState.add(inSt);
                }
              }
            }

            break;
          }

          // Rename sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            QPath locNodePath = localData.isNode()
                ? localState.getData().getQPath()
                : localState.getData().getQPath().makeParentPath();

            QPath nextLocNodePath = nextLocalState.getData().isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            // rename same node
            if (incNodePath.equals(locNodePath)) {

              List<ItemState> locRenSeq = local.getRenameSequence(localState);

              // Delete all local changes
              List<ItemState> items = local.getUniqueTreeChanges(localState, locNodePath);
              for (int i = items.size() - 1; i >= 0; i--) {
                ItemState item = items.get(i);

                if (local.findLastState(item.getData().getQPath()) != ItemState.DELETED) {

                  // delete lock properties if present
                  if (item.getData().isNode()) {
                    for (ItemState st : generateDeleleLockProperties((NodeData) item.getData()))
                      resultState.add(st);
                  }

                  ItemState newState = new ItemState(item.getData(),
                                                     ItemState.DELETED,
                                                     item.isEventFire(),
                                                     item.getData().getQPath());
                  resultState.add(newState);
                }
              }

              // restore nodes
              for (int i = locRenSeq.size() - 1; i >= 0; i--) {
                ItemState item = locRenSeq.get(i);

                if (item.getState() == ItemState.DELETED) { // generate add state for old
                  // place
                  if (item.getData().isNode()) {
                    resultState.add(new ItemState(item.getData(),
                                                  ItemState.ADDED,
                                                  item.isEventFire(),
                                                  item.getData().getQPath()));

                  } else {
                    PropertyData prop = (PropertyData) item.getData();
                    TransientPropertyData propData = new TransientPropertyData(prop.getQPath(),
                                                                               prop.getIdentifier(),
                                                                               prop.getPersistedVersion(),
                                                                               prop.getType(),
                                                                               prop.getParentIdentifier(),
                                                                               prop.isMultiValued());
                    propData.setValues(((PropertyData) locRenSeq.get(locRenSeq.size() - i - 1)
                                                                .getData()).getValues());
                    resultState.add(new ItemState(propData,
                                                  ItemState.ADDED,
                                                  item.isEventFire(),
                                                  prop.getQPath()));
                  }
                }
              }

              // apply income changes for all subtree
              for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {

                // delete lock properties if present
                if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                    resultState.add(inSt);
                }

                resultState.add(st);
              }

              addToSkipList(incomeState, incNodePath, income, skippedList);
              return resultState;

              // destination node is renamed locally
            } else if (nextIncNodePath.isDescendantOf(locNodePath)) {
              // restore renamed node
              resultState.addAll(exporter.exportItem(localData.isNode()
                  ? localData.getIdentifier()
                  : localData.getParentIdentifier()));

              // remove node on new destination
              for (ItemState st : local.getRenameSequence(localState)) {
                if (st.getState() == ItemState.RENAMED) {

                  // delete lock properties if present
                  if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                    for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                      resultState.add(inSt);
                  }

                  resultState.add(new ItemState(st.getData(),
                                                ItemState.DELETED,
                                                st.isEventFire(),
                                                st.getData().getQPath()));
                }
              }

              // apply income rename
              for (ItemState st : income.getRenameSequence(incomeState)) {
                if (st.getState() == ItemState.DELETED) {

                  // delete lock properties if present
                  if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                    for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                      resultState.add(inSt);
                  }

                  resultState.add(st);
                }
              }

              List<ItemState> changes = income.getChanges(incomeState, locNodePath);
              for (int i = 0; i < changes.size(); i++)
                skipVSChanges(changes.get(i), income, skippedList);

              skippedList.add(locNodePath);
              return resultState;

              // move to same location
            } else if (nextLocalState.getData().getQPath().isDescendantOf(nextIncNodePath)
                || nextLocalState.getData().getQPath().equals(nextIncNodePath)) {

              List<ItemState> rename = local.getRenameSequence(localState);
              for (int i = rename.size() - 1; i >= 0; i--) {
                ItemState item = rename.get(i);
                if (item.getState() == ItemState.RENAMED) { // generate delete state for new place

                  // delete lock properties if present
                  if (item.getData().isNode()) {
                    for (ItemState inSt : generateDeleleLockProperties((NodeData) item.getData()))
                      resultState.add(inSt);
                  }

                  resultState.add(new ItemState(item.getData(),
                                                ItemState.DELETED,
                                                item.isEventFire(),
                                                item.getData().getQPath()));
                } else if (item.getState() == ItemState.DELETED) { // generate delete state for old
                  // place
                  if (item.getData().isNode()) {
                    resultState.add(new ItemState(item.getData(),
                                                  ItemState.ADDED,
                                                  item.isEventFire(),
                                                  item.getData().getQPath()));

                  } else {
                    PropertyData prop = (PropertyData) item.getData();
                    TransientPropertyData propData = new TransientPropertyData(prop.getQPath(),
                                                                               prop.getIdentifier(),
                                                                               prop.getPersistedVersion(),
                                                                               prop.getType(),
                                                                               prop.getParentIdentifier(),
                                                                               prop.isMultiValued());
                    propData.setValues(((PropertyData) rename.get(rename.size() - i - 1).getData()).getValues());
                    resultState.add(new ItemState(propData,
                                                  ItemState.ADDED,
                                                  item.isEventFire(),
                                                  prop.getQPath()));
                  }
                }
              }

              // apply income rename
              for (ItemState st : income.getRenameSequence(incomeState)) {

                // delete lock properties if present
                if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                    resultState.add(inSt);
                }

                resultState.add(st);
              }

              return resultState;
            }
            break;
          }

          // DELETE
          if (local.findNextState(localState, localData.getQPath(), ItemState.ADDED) != null) {
            break;
          }

          parentNodeState = local.findNextState(localState,
                                                localData.getParentIdentifier(),
                                                localData.getQPath().makeParentPath(),
                                                ItemState.DELETED);

          if (localData.isNode() || parentNodeState != null) {
            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : parentNodeState.getData().getQPath();

            if (incNodePath.equals(locNodePath)) {

              List<ItemState> rename = income.getRenameSequence(incomeState);
              for (int i = 0; i <= rename.size() - 1; i++) {
                ItemState item = rename.get(i);
                if (item.getState() == ItemState.DELETED) {
                  if (local.findNextState(localState,
                                          item.getData().getIdentifier(),
                                          item.getData().getQPath(),
                                          ItemState.DELETED) == null
                      && !ItemState.isSame(localState,
                                           item.getData().getIdentifier(),
                                           item.getData().getQPath(),
                                           ItemState.DELETED)) {

                    // delete lock properties if present
                    if (item.getData().isNode()) {
                      for (ItemState inSt : generateDeleleLockProperties((NodeData) item.getData()))
                        resultState.add(inSt);
                    }

                    resultState.add(new ItemState(item.getData(),
                                                  ItemState.DELETED,
                                                  item.isEventFire(),
                                                  item.getData().getQPath()));
                  }
                } else {
                  resultState.add(new ItemState(item.getData(),
                                                ItemState.ADDED,
                                                item.isEventFire(),
                                                item.getData().getQPath()));
                }
              }
              return resultState;
            } else if (nextIncNodePath.isDescendantOf(locNodePath)) {

              List<ItemState> rename = income.getRenameSequence(incomeState);

              for (int i = 0; i <= rename.size() - 1; i++) {
                ItemState item = rename.get(i);
                if (item.getState() == ItemState.DELETED) {
                  // delete lock properties if present
                  if (item.getData().isNode()) {
                    for (ItemState inSt : generateDeleleLockProperties((NodeData) item.getData()))
                      resultState.add(inSt);
                  }

                  resultState.add(item);
                }
              }

              List<ItemState> changes = income.getChanges(incomeState, localData.getQPath());
              for (int i = 0; i < changes.size(); i++)
                skipVSChanges(changes.get(i), income, skippedList);

              skippedList.add(localData.getQPath());

              resultState.addAll(exporter.exportItem(localData.isNode()
                  ? localData.getIdentifier()
                  : localData.getParentIdentifier()));

              return resultState;
            }

          } else {
            List<ItemState> rename = income.getRenameSequence(incomeState);

            for (int i = 0; i <= rename.size() - 1; i++) {
              ItemState item = rename.get(i);
              if (!item.isNode()) {
                if (item.getData().getQPath().equals(localData.getQPath())) {
                  resultState.add(new ItemState(item.getData(),
                                                ItemState.ADDED,
                                                item.isEventFire(),
                                                item.getData().getQPath()));
                }
              }
            }
          }
          // }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (local.findNextState(localState,
                                    localData.getIdentifier(),
                                    localData.getQPath(),
                                    ItemState.DELETED) != null) {
              break;
            }

            List<ItemState> rename = income.getRenameSequence(incomeState);
            for (int i = 0; i < rename.size(); i++) {
              ItemData itemData = rename.get(i).getData();
              if (!itemData.isNode()) {
                if (itemData.getQPath().equals(localData.getQPath())) {

                  // restore property
                  resultState.add(new ItemState(itemData,
                                                ItemState.UPDATED,
                                                localState.isEventFire(),
                                                localState.getData().getQPath(),
                                                localState.isInternallyCreated(),
                                                localState.isPersisted()));
                  break;
                }
              }
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.isNode()) {
            if (localData.getQPath().equals(incomeData.getQPath())) {

              // restore local changes
              resultState.add(localState);

              List<ItemState> localMixinSeq = local.getMixinSequence(localState);
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
            }
          }
          break;
        }
      }
    }

    // apply income changes if not processed
    if (!itemChangeProcessed) {
      for (ItemState st : income.getRenameSequence(incomeState)) {

        // delete lock properties if present
        if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
          for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
            resultState.add(inSt);
        }

        resultState.add(st);
      }
    }

    return resultState;
  }
}
