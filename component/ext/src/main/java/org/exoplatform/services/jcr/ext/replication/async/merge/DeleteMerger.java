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
 * @version $Id: AddMerger.java 25356 2008-12-18 09:54:16Z tolusha $
 */
public class DeleteMerger extends AbstractMerger {

  public DeleteMerger(boolean localPriority,
                      RemoteExporter exporter,
                      DataManager dataManager,
                      NodeTypeDataManager ntManager,
                      ResourcesHolder resHolder) {
    super(localPriority, exporter, dataManager, ntManager, resHolder);
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
                                                                                             null,
                                                                                             resHolder);

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

              skipVSChanges(incomeData.isNode() ? incomeState : incParentNodeState,
                            income,
                            skippedList);
              skippedList.add(incNodePath);
              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                              null,
                                                              resHolder);
            }
          } else {
            if (!localData.isNode()) {
              if (localData.getQPath().equals(incomeData.getQPath())) {

                skipVSChanges(incomeState, income, skippedList);
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                null,
                                                                resHolder);
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

                  skipVSChanges(incomeData.isNode() ? incomeState : incParentNodeState,
                                income,
                                skippedList);
                  skippedList.add(incNodePath);
                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                  null,
                                                                  resHolder);
                }
              }
            } else {
              List<ItemState> updateSeq = local.getUpdateSequence(localState);
              for (ItemState item : updateSeq) {
                if (incomeData.getQPath().isDescendantOf(item.getData().getQPath())) {

                  skipVSChanges(incomeState, income, skippedList);
                  skippedList.add(incomeData.getQPath());
                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                  null,
                                                                  resHolder);
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

                skipVSChanges(incomeData.isNode() ? incomeState : incParentNodeState,
                              income,
                              skippedList);
                skippedList.add(incNodePath);
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                null,
                                                                resHolder);
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {

                skipVSChanges(incomeState, income, skippedList);
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                null,
                                                                resHolder);
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

              skipVSChanges(incomeData.isNode() ? incomeState : incParentNodeState,
                            income,
                            skippedList);
              skippedList.add(incNodePath);
              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                              null,
                                                              resHolder);
            }
          } else {
            if (localData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(localData.getQPath())) {

                skipVSChanges(incomeState, income, skippedList);
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                null,
                                                                resHolder);
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {

                skipVSChanges(incomeState, income, skippedList);
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                null,
                                                                resHolder);
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

                skipVSChanges(incomeData.isNode() ? incomeState : incParentNodeState,
                              income,
                              skippedList);
                skippedList.add(incNodePath);
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                null,
                                                                resHolder);
              }
            } else {
              if (localData.getQPath().equals(incomeData.getQPath())) {

                skipVSChanges(incomeState, income, skippedList);
                skippedList.add(incomeData.getQPath());
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                null,
                                                                resHolder);
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

              skipVSChanges(incomeData.isNode() ? incomeState : incParentNodeState,
                            income,
                            skippedList);
              skippedList.add(incNodePath);
              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                              null,
                                                              resHolder);
            }
          } else {
            List<ItemState> mixinSeq = local.getMixinSequence(localState);

            for (int i = 0; i < mixinSeq.size(); i++) {
              ItemState item = mixinSeq.get(i);
              if (!item.getData().isNode()) {
                if (item.getData().getQPath().equals(incomeData.getQPath())) {

                  skipVSChanges(incomeState, income, skippedList);
                  skippedList.add(incomeData.getQPath());
                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                  null,
                                                                  resHolder);
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

              List<String> deletedUUID = new ArrayList<String>();

              List<ItemState> items = local.getUniqueTreeChanges(localState, incNodePath);
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

                  deletedUUID.add(item.getData().getIdentifier());
                }
              }

              // apply income changes for all subtree
              outer: for (ItemState st : income.getTreeChanges(incomeState, incNodePath)) {

                // skip already deleted items in previous block
                for (int i = 0; i < deletedUUID.size(); i++)
                  if (resultState.hasState(deletedUUID.get(i),
                                           st.getData().getQPath(),
                                           ItemState.DELETED))
                    continue outer;

                // delete lock properties if present
                if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                    resultState.add(inSt);
                }

                resultState.add(st);
              }

              addToSkipList(incomeState, incNodePath, income, skippedList);
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
                  for (ItemState inSt : generateRestoreOrder(localState, local))
                    resultState.add(inSt);
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

              QPath incNodePath = incomeData.isNode()
                  ? incomeData.getQPath()
                  : incParentNodeState.getData().getQPath();

              QPath locParentMoved = null;

              List<ItemState> locRenSeq = local.getRenameSequence(localState);

              // find parent moved node
              for (int i = 0; i < locRenSeq.size(); i++)
                if (locRenSeq.get(i).getState() != ItemState.DELETED) {
                  locParentMoved = locRenSeq.get(i - 1).getData().getQPath();
                  break;
                }

              if (locParentMoved.equals(incNodePath) || locParentMoved.isDescendantOf(incNodePath)
                  || incNodePath.isDescendantOf(locParentMoved)) {

                // Delete all local changes
                List<ItemState> items = local.getUniqueTreeChanges(localState, locParentMoved);
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
          if (local.findNextState(localState, localData.getQPath(), ItemState.ADDED) != null) {
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
