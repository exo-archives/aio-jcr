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
import org.exoplatform.services.jcr.ext.replication.async.storage.StorageRuntimeException;
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
public class UpdateMerger extends AbstractMerger {

  public UpdateMerger(boolean localPriority,
                      RemoteExporter exporter,
                      DataManager dataManager,
                      NodeTypeDataManager ntManager) {
    super(localPriority, exporter, dataManager, ntManager);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws RepositoryException
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

    // incomeState is DELETE state and nextIncomeState is UPDATE state
    ItemState incomeState = itemChange;
    ItemState nextIncomeState = null;

    EditableChangesStorage<ItemState> resultState = new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                                             null);
    // income update sequence
    List<ItemState> incUpdateSeq = income.getUpdateSequence(incomeState);

    // find next update state after delete state
    if (incomeState.getData().isNode()) {
      for (ItemState st : incUpdateSeq)
        if (st.getState() == ItemState.UPDATED
            && st.getData().getIdentifier().equals(incomeState.getData().getIdentifier())) {
          nextIncomeState = st;
          break;
        }
    }

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
          for (ItemState st : incUpdateSeq) {
            if (localData.getQPath().isDescendantOf(st.getData().getQPath())) {
              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocNodePath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (item.getData().getQPath().isDescendantOf(locNodePath)
                    || item.getData().getQPath().equals(locNodePath)
                    || locNodePath.isDescendantOf(item.getData().getQPath())
                    || nextLocNodePath.isDescendantOf(item.getData().getQPath())) {

                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(locNodePath)) {
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }

            break;
          }

          // UPDATE
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> locUpdateSeq = local.getUpdateSequence(localState);
            for (ItemState locSt : locUpdateSeq)
              for (ItemState incSt : incUpdateSeq) {
                if (locSt.getData().getQPath().isDescendantOf(incSt.getData().getQPath())
                    || locSt.getData().getQPath().equals(incSt.getData().getQPath())
                    || incSt.getData().getQPath().isDescendantOf(locSt.getData().getQPath())) {

                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }
            break;
          }

          // DELETE
          ItemState locParentNodeState = local.findNextState(localState,
                                                             localData.getParentIdentifier(),
                                                             localData.getQPath().makeParentPath(),
                                                             ItemState.DELETED);

          if (localData.isNode() || locParentNodeState != null) {
            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : locParentNodeState.getData().getQPath();

            for (ItemState item : incUpdateSeq) {
              if (item.getData().getQPath().isDescendantOf(locNodePath)
                  || item.getData().getQPath().equals(locNodePath)
                  || locNodePath.isDescendantOf(item.getData().getQPath())) {

                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }
          } else {
            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (localData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }
            } else {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }
          }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (!incomeData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {

                skipVSChanges(incomeState, skippedList);
                skippedList.add(incomeData.getQPath());

                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            } else {
              for (ItemState item : incUpdateSeq) {
                if (localData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          for (ItemState st : incUpdateSeq) {
            if (localData.getQPath().isDescendantOf(st.getData().getQPath())
                || localData.getQPath().equals(st.getData().getQPath())) {
              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
            }
          }
          break;
        }

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          for (ItemState st : incUpdateSeq) {
            if (st.getState() == ItemState.DELETED)
              continue;

            if (localData.getQPath().isDescendantOf(st.getData().getQPath())) {

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

              // apply income changes
              for (ItemState inSt : incUpdateSeq)
                resultState.add(inSt);

              return resultState;
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {

            List<ItemState> locUpdateSeq = local.getUpdateSequence(localState);
            outer: for (ItemState locSt : locUpdateSeq)
              for (ItemState incSt : incUpdateSeq) {
                if (locSt.getData().getQPath().isDescendantOf(incSt.getData().getQPath())
                    || locSt.getData().getQPath().equals(incSt.getData().getQPath())
                    || incSt.getData().getQPath().isDescendantOf(locSt.getData().getQPath())) {

                  if (!isOrderRestored(restoredOrder, localData.getQPath().makeParentPath())) {
                    restoredOrder.add(localData.getQPath().makeParentPath());

                    // restore original order
                    for (int i = 1; i <= locUpdateSeq.size() - 1; i++) {
                      ItemState item = locUpdateSeq.get(i);
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
                      if (i == locUpdateSeq.size() - 1) {
                        item = locUpdateSeq.get(1);
                        node = (NodeData) item.getData();

                        QPath name = QPath.makeChildPath(node.getQPath().makeParentPath(),
                                                         node.getQPath().getName(),
                                                         locUpdateSeq.size() - 1);

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

                  break outer;
                }
              }
            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocNodePath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            for (ItemState st : incUpdateSeq) {
              if (st.getData().getQPath().isDescendantOf(locNodePath)
                  || st.getData().getQPath().equals(locNodePath)
                  || locNodePath.isDescendantOf(st.getData().getQPath())
                  || nextLocNodePath.isDescendantOf(st.getData().getQPath())) {

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
                      propData.setValues(((PropertyData) rename.get(rename.size() - i - 1)
                                                               .getData()).getValues());
                      resultState.add(new ItemState(propData,
                                                    ItemState.ADDED,
                                                    item.isEventFire(),
                                                    prop.getQPath()));
                    }
                  }
                }

                for (ItemState inSt : incUpdateSeq)
                  resultState.add(inSt);

                return resultState;
              }
            }
            break;
          }

          // DELETE
          if (local.findNextState(localState,
                                  localData.getParentIdentifier(),
                                  localData.getQPath().makeParentPath(),
                                  ItemState.DELETED) != null) {
            break;
          }

          if (localData.isNode()) {
            for (ItemState item : incUpdateSeq) {
              if (item.getData().getQPath().equals(localData.getQPath())
                  || localData.getQPath().isDescendantOf(item.getData().getQPath())) {

                resultState.addAll(exporter.exportItem(localData.getIdentifier()));

                for (ItemState st : incUpdateSeq)
                  resultState.add(st);

                return resultState;

              } else if (item.getData().getQPath().isDescendantOf(localData.getQPath())) {
                resultState.addAll(exporter.exportItem(localData.getIdentifier()));
                return resultState;
              }
            }
          } else {
            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (localData.getQPath().isDescendantOf(item.getData().getQPath())) {

                  // restore property
                  Iterator<ItemState> exportedList = exporter.exportItem(localData.getParentIdentifier())
                                                             .getChanges();
                  while (exportedList.hasNext()) {
                    ItemState st = exportedList.next();
                    if (!st.getData().isNode()
                        && st.getData().getQPath().equals(localData.getQPath())) {
                      resultState.add(new ItemState(st.getData(),
                                                    ItemState.ADDED,
                                                    st.isEventFire(),
                                                    st.getData().getQPath()));
                      break;
                    }
                  }
                }
              }
            } else {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                resultState.add(new ItemState(incomeData,
                                              ItemState.ADDED,
                                              incomeState.isEventFire(),
                                              incomeData.getQPath()));
                itemChangeProcessed = true;
              }
            }
          }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (incomeData.isNode()) {
              outer: for (ItemState item : incUpdateSeq) {
                if (localData.getQPath().isDescendantOf(item.getData().getQPath())) {

                  // restore property
                  Iterator<ItemState> exportedList = exporter.exportItem(localData.getParentIdentifier())
                                                             .getChanges();
                  while (exportedList.hasNext()) {
                    ItemState st = exportedList.next();
                    if (!st.getData().isNode()
                        && st.getData().getQPath().equals(localData.getQPath())) {
                      resultState.add(new ItemState(st.getData(),
                                                    ItemState.UPDATED,
                                                    st.isEventFire(),
                                                    st.getData().getQPath()));
                      break outer;
                    }
                  }
                }
              }
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          for (ItemState st : incUpdateSeq) {
            if (localData.getQPath().isDescendantOf(st.getData().getQPath())
                || localData.getQPath().equals(st.getData().getQPath())) {

              // restore local changes
              resultState.add(localState);

              List<ItemState> localMixinSeq = local.getMixinSequence(localState);
              for (int i = localMixinSeq.size() - 1; i > 0; i--) {
                ItemState item = localMixinSeq.get(i);

                if (item.getState() == ItemState.ADDED) {

                  // delete lock properties if present
                  if (item.getData().isNode() && item.getState() == ItemState.DELETED) {
                    for (ItemState inSt : generateDeleleLockProperties((NodeData) item.getData()))
                      resultState.add(inSt);
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
      if (nextIncomeState != null) {
        if (incomeState.getData().isNode()) {
          for (ItemState st : incUpdateSeq)
            resultState.add(st);
        }
      } else {
        resultState.add(incomeState);
      }
    }

    return resultState;
  }
}
