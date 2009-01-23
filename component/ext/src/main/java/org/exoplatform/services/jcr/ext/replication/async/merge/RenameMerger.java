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
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogReadException;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableItemStatesStorage;
import org.exoplatform.services.jcr.impl.Constants;
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
public class RenameMerger implements ChangesMerger {

  protected final boolean             localPriority;

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  public RenameMerger(boolean localPriority,
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
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @throws RepositoryException
   */
  public ChangesStorage<ItemState> merge(ItemState itemChange,
                                         ChangesStorage<ItemState> income,
                                         ChangesStorage<ItemState> local,
                                         String mergeTempDir,
                                         List<QPath> skippedList) throws RemoteExportException,
                                                                 IOException,
                                                                 ClassCastException,
                                                                 ClassNotFoundException,
                                                                 ChangesLogReadException,
                                                                 RepositoryException {
    boolean itemChangeProcessed = false;

    // incomeState is DELETE state and nextIncomeState is RENAME state
    ItemState incomeState = itemChange;
    ItemState nextIncomeState = income.findNextState(incomeState, incomeState.getData()
                                                                             .getIdentifier());

    QPath incNodePath = incomeState.getData().isNode()
        ? incomeState.getData().getQPath()
        : incomeState.getData().getQPath().makeParentPath();

    QPath nextIncNodePath = nextIncomeState.getData().isNode()
        ? nextIncomeState.getData().getQPath()
        : nextIncomeState.getData().getQPath().makeParentPath();

    EditableChangesStorage<ItemState> resultEmptyState = new EditableItemStatesStorage<ItemState>(new File(mergeTempDir));
    EditableChangesStorage<ItemState> resultState = new EditableItemStatesStorage<ItemState>(new File(mergeTempDir));

    for (Iterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();

      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (localData.isNode()) {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)
                || localData.getQPath().equals(nextIncNodePath)) {

              skippedList.add(incNodePath);
              skippedList.add(nextIncNodePath);

              return resultEmptyState;
            }
          } else {
            if (localData.getQPath().isDescendantOf(incNodePath)) {

              skippedList.add(incNodePath);
              skippedList.add(nextIncNodePath);

              return resultEmptyState;
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // Update sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            // TODO

            if (localData.getQPath().isDescendantOf(incomeData.getQPath())
                || (localData.getParentIdentifier().equals(incomeData.getParentIdentifier()) && localData.getQPath()
                                                                                                         .getName()
                                                                                                         .equals(incomeData.getQPath()
                                                                                                                           .getName()))
                || (local.getNextItemStateByUUIDOnUpdate(localState,
                                                         nextIncomeState.getData()
                                                                        .getParentIdentifier()) != null)) {
              skippedList.add(incomeData.getQPath());
              return resultEmptyState;
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

              skippedList.add(incNodePath);
              skippedList.add(nextIncNodePath);

              return resultEmptyState;
            }
            break;
          }

          // simple DELETE
          if (localData.isNode()) {
            if (incNodePath.isDescendantOf(localData.getQPath())
                || incNodePath.equals(localData.getQPath())
                || nextIncNodePath.isDescendantOf(localData.getQPath())) {

              skippedList.add(incNodePath);
              skippedList.add(nextIncNodePath);

              return resultEmptyState;
            }
          } else {
            if (incNodePath.isDescendantOf(localData.getQPath().makeParentPath())
                || incNodePath.equals(localData.getQPath().makeParentPath())) {

              skippedList.add(incNodePath);
              skippedList.add(nextIncNodePath);

              return resultEmptyState;
            }
          }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)) {

              skippedList.add(incNodePath);
              skippedList.add(nextIncNodePath);

              return resultEmptyState;
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (localData.getQPath().equals(incNodePath)
              || localData.getQPath().isDescendantOf(incNodePath)) {
            skippedList.add(incomeData.getQPath());
            return resultEmptyState;
          }
          break;
        }

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.isNode()
              && ((localData.getQPath().isDescendantOf(incomeData.getQPath())
                  || localData.getQPath().equals(incomeData.getQPath())
                  || localData.getQPath().isDescendantOf(nextIncomeState.getData().getQPath()) || localData.getQPath()
                                                                                                           .equals(nextIncomeState.getData()
                                                                                                                                  .getQPath())))) {

            // add DELETE state
            List<ItemState> items = local.getDescendantsChanges(localState,
                                                                localData.getQPath(),
                                                                true);
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

            if (local.findLastState(localData.getQPath()) != ItemState.DELETED) {

              if (localData.isNode()) {
                for (ItemState st : generateDeleleLockProperties((NodeData) localData))
                  resultState.add(st);
              }

              resultState.add(new ItemState(localData,
                                            ItemState.DELETED,
                                            localState.isEventFire(),
                                            localData.getQPath()));
            }

            // add all state from income changes
            List<ItemState> rename = income.getRenameSequence(incomeState);
            for (ItemState st : income.getChanges(rename.get(0), rename.get(0).getData().getQPath())) {

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
          } else if (!incomeData.isNode()
              && ((localData.getQPath().isDescendantOf(incomeData.getQPath().makeParentPath())
                  || localData.getQPath().equals(incomeData.getQPath().makeParentPath())
                  || localData.getQPath().isDescendantOf(nextIncomeState.getData()
                                                                        .getQPath()
                                                                        .makeParentPath()) || localData.getQPath()
                                                                                                       .equals(nextIncomeState.getData()
                                                                                                                              .getQPath()
                                                                                                                              .makeParentPath())))) {
            // add DELETE state
            List<ItemState> items = local.getDescendantsChanges(localState,
                                                                localData.getQPath(),
                                                                true);
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

            if (local.findLastState(localData.getQPath()) != ItemState.DELETED) {

              // delete lock properties if present
              if (localData.isNode()) {
                for (ItemState inSt : generateDeleleLockProperties((NodeData) localData))
                  resultState.add(inSt);
              }

              resultState.add(new ItemState(localData,
                                            ItemState.DELETED,
                                            localState.isEventFire(),
                                            localData.getQPath()));
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

          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // Update sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {

            ItemState nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                                      incomeData.getParentIdentifier());

            if (!incomeData.isNode() && nextItem != null) {
              List<ItemState> rename = income.getRenameSequence(incomeState);

              for (ItemState st : rename) {
                if (st.getState() == ItemState.DELETED) {
                  if (st.getData().isNode()) {
                    QPath qPath = nextItem.getData().getQPath();

                    NodeData node = (NodeData) st.getData();
                    TransientNodeData item = new TransientNodeData(qPath,
                                                                   node.getIdentifier(),
                                                                   node.getPersistedVersion(),
                                                                   node.getPrimaryTypeName(),
                                                                   node.getMixinTypeNames(),
                                                                   node.getOrderNumber(),
                                                                   node.getParentIdentifier(),
                                                                   node.getACL());
                    incomeState = new ItemState(item,
                                                st.getState(),
                                                st.isEventFire(),
                                                qPath,
                                                st.isInternallyCreated(),
                                                st.isPersisted());

                    // delete lock properties if present
                    for (ItemState inSt : generateDeleleLockProperties((NodeData) incomeState.getData()))
                      resultState.add(inSt);

                    resultState.add(incomeState);

                  } else {
                    QPath qPath = QPath.makeChildPath(nextItem.getData().getQPath(),
                                                      st.getData().getQPath().getEntries()[st.getData()
                                                                                             .getQPath()
                                                                                             .getEntries().length - 1]);

                    PropertyData prop = (PropertyData) st.getData();
                    TransientPropertyData item = new TransientPropertyData(qPath,
                                                                           prop.getIdentifier(),
                                                                           prop.getPersistedVersion(),
                                                                           prop.getType(),
                                                                           prop.getParentIdentifier(),
                                                                           prop.isMultiValued());

                    item.setValues(prop.getValues());
                    incomeState = new ItemState(item,
                                                st.getState(),
                                                st.isEventFire(),
                                                qPath,
                                                st.isInternallyCreated(),
                                                st.isPersisted());
                    resultState.add(incomeState);
                  }
                } else {
                  resultState.add(st);
                }
              }
              return resultState;
            }

            // updated node was renamed
            nextItem = local.getNextItemStateByUUIDOnUpdate(localState, incomeData.getIdentifier());

            if (incomeData.isNode() && nextItem != null) {
              // set new name
              QPath qPath = QPath.makeChildPath(nextItem.getData().getQPath().makeAncestorPath(1),
                                                incomeData.getQPath().getEntries()[incomeData.getQPath()
                                                                                             .getEntries().length - 1]);

              // set new data
              NodeData node = (NodeData) incomeData;

              TransientNodeData item = new TransientNodeData(qPath,
                                                             node.getIdentifier(),
                                                             node.getPersistedVersion(),
                                                             node.getPrimaryTypeName(),
                                                             node.getMixinTypeNames(),
                                                             node.getOrderNumber(),
                                                             node.getParentIdentifier(),
                                                             node.getACL());

              incomeState = new ItemState(item, ItemState.DELETED, incomeState.isEventFire(), qPath);

              // delete lock properties if present
              if (incomeState.getData().isNode()) {
                for (ItemState inSt : generateDeleleLockProperties((NodeData) incomeState.getData()))
                  resultState.add(inSt);
              }

              resultState.add(incomeState);
              resultState.add(nextIncomeState);
              return resultState;
            }

            nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                            nextIncomeState.getData()
                                                                           .getParentIdentifier());
            if (nextItem != null) {
              // set new name
              QPath qPath = QPath.makeChildPath(nextItem.getData().getQPath(),
                                                nextIncomeState.getData().getQPath().getEntries()[nextIncomeState.getData()
                                                                                                                 .getQPath()
                                                                                                                 .getEntries().length - 1]);

              // set new data
              NodeData node = (NodeData) nextIncomeState.getData();

              TransientNodeData item = new TransientNodeData(qPath,
                                                             node.getIdentifier(),
                                                             node.getPersistedVersion(),
                                                             node.getPrimaryTypeName(),
                                                             node.getMixinTypeNames(),
                                                             node.getOrderNumber(),
                                                             node.getParentIdentifier(),
                                                             node.getACL());

              nextIncomeState = new ItemState(item,
                                              nextIncomeState.getState(),
                                              nextIncomeState.isEventFire(),
                                              qPath,
                                              nextIncomeState.isInternallyCreated(),
                                              nextIncomeState.isPersisted());

              // delete lock properties if present
              if (incomeState.getData().isNode()) {
                for (ItemState inSt : generateDeleleLockProperties((NodeData) incomeState.getData()))
                  resultState.add(inSt);
              }

              resultState.add(incomeState);
              resultState.add(nextIncomeState);
              itemChangeProcessed = true;
              break;
            }

            break;
          }

          // Rename sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if ((incomeData.isNode() && localData.isNode() && incomeData.getQPath()
                                                                        .equals(localData.getQPath()))
                || (!incomeData.isNode() && localData.isNode() && incomeData.getQPath()
                                                                            .makeParentPath()

                                                                            .equals(localData.getQPath()))
                || (incomeData.isNode() && !localData.isNode() && incomeData.getQPath()
                                                                            .equals(localData.getQPath()
                                                                                             .makeParentPath()))
                || (!incomeData.isNode() && !localData.isNode() && incomeData.getQPath()
                                                                             .makeParentPath()
                                                                             .equals(localData.getQPath()
                                                                                              .makeParentPath()))) {
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
                } else if (item.getState() == ItemState.DELETED) { // generate add state for old
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

            } else if (nextIncomeState.getData().getQPath().isDescendantOf(localData.getQPath())) {
              // restore renamed node
              resultState.addAll(exporter.exportItem(localData.getIdentifier()));

              // delete renamed node
              if (local.findLastState(nextLocalState.getData().getQPath()) != ItemState.DELETED) {

                // delete lock properties if present
                if (nextLocalState.getData().isNode()) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) nextLocalState.getData()))
                    resultState.add(inSt);
                }

                resultState.add(new ItemState(nextLocalState.getData(),
                                              ItemState.DELETED,
                                              nextLocalState.isEventFire(),
                                              nextLocalState.getData().getQPath()));
              }

              if (!itemChangeProcessed) {

                // delete lock properties if present
                if (incomeState.getData().isNode()) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) incomeState.getData()))
                    resultState.add(inSt);
                }

                resultState.add(incomeState);
              }
              itemChangeProcessed = true;
              break;

              // move to same location
            } else if ((incomeData.isNode() && (nextLocalState.getData()
                                                              .getQPath()
                                                              .isDescendantOf(nextIncomeState.getData()
                                                                                             .getQPath()) || nextLocalState.getData()
                                                                                                                           .getQPath()
                                                                                                                           .equals(nextIncomeState.getData()
                                                                                                                                                  .getQPath())))
                || (!incomeData.isNode() && nextLocalState.getData()
                                                          .getQPath()
                                                          .isDescendantOf(nextIncomeState.getData()
                                                                                         .getQPath()
                                                                                         .makeParentPath()))) {
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
          if (incomeData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())) {
              if (!itemChangeProcessed) {
                resultState.add(nextIncomeState);
              }
              itemChangeProcessed = true;
              break;
            } else if (nextIncomeState.getData().getQPath().isDescendantOf(localData.getQPath())) {
              // restore deleted node and all subtree with renamed node
              resultState.addAll(exporter.exportItem(localData.getIdentifier()));

              if (!itemChangeProcessed) {

                // delete lock properties if present
                if (incomeState.getData().isNode()) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) incomeState.getData()))
                    resultState.add(inSt);
                }

                resultState.add(incomeState);
              }

              return resultState;
            }
          } else if (!incomeData.isNode()
              && income.findNextState(incomeState,
                                      incomeState.getData().getParentIdentifier(),
                                      incomeState.getData().getQPath().makeParentPath(),
                                      ItemState.DELETED) != null) {
            if ((localData.isNode() && incomeData.getQPath().equals(localData.getQPath()))
                || (!localData.isNode() && incomeData.getQPath()
                                                     .isDescendantOf(localData.getQPath()
                                                                              .makeParentPath()))) {

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
            }
          }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {

            List<ItemState> rename = income.getRenameSequence(incomeState);
            for (int i = 0; i < rename.size(); i++) {
              System.out.println(rename.get(i).getData().getQPath());
              if (rename.get(i).getData().getQPath().equals(localData.getQPath())) {

                // restore property
                resultState.add(new ItemState(rename.get(i).getData(),
                                              ItemState.UPDATED,
                                              localState.isEventFire(),
                                              localState.getData().getQPath(),
                                              localState.isInternallyCreated(),
                                              localState.isPersisted()));
                break;
              }
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.isNode()) {
            if (localData.getQPath().equals(incomeData.getQPath())) {
              // delete local mixin changes
              List<ItemState> localMixinSeq = local.getMixinSequence(localState);
              for (int i = localMixinSeq.size() - 1; i >= 0; i--) {
                ItemState item = localMixinSeq.get(i);

                // delete lock properties if present
                if (item.getData().isNode() && item.getState() == ItemState.DELETED) {
                  for (ItemState inSt : generateDeleleLockProperties((NodeData) item.getData()))
                    resultState.add(inSt);
                }

                resultState.add(new ItemState(item.getData(),
                                              ItemState.DELETED,
                                              item.isEventFire(),
                                              item.getData().getQPath()));
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

  /**
   * generateDeleleLockProperties.
   * 
   * @param node
   * @return
   * @throws RepositoryException
   */
  private List<ItemState> generateDeleleLockProperties(NodeData node) throws RepositoryException {
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
