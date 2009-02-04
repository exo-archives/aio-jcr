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
 * @version $Id: AddMerger.java 26315 2009-01-06 12:51:39Z tolusha $
 */
public class MixinMerger extends AbstractMerger {

  public MixinMerger(boolean localPriority,
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

    boolean itemChangeProcessed = false; // TODO really need?

    ItemState incomeState = itemChange;
    EditableChangesStorage<ItemState> resultState = new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                                             null);

    ItemState parentNodeState;
    for (Iterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();

      ItemData localData = localState.getData();
      ItemData incomeData = incomeState.getData();

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
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE node
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState item : updateSeq) {
              if (incomeData.getQPath().equals(item.getData().getQPath())
                  || incomeData.getQPath().isDescendantOf(item.getData().getQPath())) {

                skipVSChanges(incomeState, skippedList);
                skippedList.add(incomeData.getQPath());

                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }
            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (localData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {

                skipVSChanges(incomeState, skippedList);
                skippedList.add(incomeData.getQPath());

                return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())) {

              skipVSChanges(incomeState, skippedList);
              skippedList.add(incomeData.getQPath());

              return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
            }
          }
          break;

        case ItemState.UPDATED:
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.getQPath().equals(localData.getQPath())) {
            List<ItemState> mixinSequence = income.getMixinSequence(incomeState);
            for (int i = 1; i < mixinSequence.size(); i++) { // first state is MIXIN_CHANGED
              skipVSChanges(mixinSequence.get(i), skippedList);
              skippedList.add(mixinSequence.get(i).getData().getQPath());
            }

            return new BufferedItemStatesStorage<ItemState>(new File(mergeTempDir), null);
          }
          break;
        }

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          break;

        case ItemState.UPDATED:
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE node
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState st : updateSeq) {
              if (incomeData.getQPath().isDescendantOf(st.getData().getQPath())
                  || incomeData.getQPath().equals(st.getData().getQPath())) {

                if (!isOrderRestored(restoredOrder, localData.getQPath().makeParentPath())) {
                  restoredOrder.add(localData.getQPath().makeParentPath());

                  // restore original order
                  List<ItemState> locUpdateSeq = local.getUpdateSequence(localState);
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
              }
            }

            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            parentNodeState = income.findNextState(incomeState,
                                                   incomeData.getParentIdentifier(),
                                                   incomeData.getQPath().makeParentPath(),
                                                   ItemState.DELETED);

            if (incomeData.isNode() || parentNodeState != null) {

              QPath incNodePath = incomeData.isNode()
                  ? incomeData.getQPath()
                  : parentNodeState.getData().getQPath();

              if (localData.getQPath().equals(incNodePath)) {

                List<ItemState> renameSequence = local.getRenameSequence(localState);
                for (int i = 0; i < renameSequence.size(); i++) {
                  ItemState item = renameSequence.get(i);
                  if (item.getData().isNode()) {
                    if (incomeData.getQPath().isDescendantOf(item.getData().getQPath())
                        || incomeData.getQPath().equals(item.getData().getQPath())) {

                      for (int j = renameSequence.size() - 1; j >= 0; j--) {
                        item = renameSequence.get(j);
                        if (item.getState() == ItemState.RENAMED) { // generate delete state for new
                          // place

                          // delete lock properties if present
                          if (item.getData().isNode()) {
                            for (ItemState st : generateDeleleLockProperties((NodeData) item.getData()))
                              resultState.add(st);
                          }

                          resultState.add(new ItemState(item.getData(),
                                                        ItemState.DELETED,
                                                        item.isEventFire(),
                                                        item.getData().getQPath()));

                        } else if (item.getState() == ItemState.DELETED) { // generate add state for
                          // old
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
                            propData.setValues(((PropertyData) renameSequence.get(renameSequence.size()
                                                                                 - j - 1)
                                                                             .getData()).getValues());
                            resultState.add(new ItemState(propData,
                                                          ItemState.ADDED,
                                                          item.isEventFire(),
                                                          prop.getQPath()));
                          }
                        }
                      }

                      // apply income mixin changes
                      for (ItemState st : income.getMixinSequence(incomeState)) {

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
                }
              }
            }

            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (local.findNextState(localState,
                                    localData.getParentIdentifier(),
                                    localData.getQPath().makeParentPath(),
                                    ItemState.DELETED) != null) {
              break;
            }

            if (incomeData.getQPath().equals(localData.getQPath())
                || incomeData.getQPath().isDescendantOf(localData.getQPath())) {

              List<ItemState> changes = income.getChanges(incomeState, localData.getQPath());
              for (int i = 0; i < changes.size(); i++)
                skipVSChanges(changes.get(i), skippedList);

              skippedList.add(localData.getQPath());
              resultState.addAll(exporter.exportItem(localData.getIdentifier()));

              return resultState;
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.getQPath().equals(localData.getQPath())) {

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

            // apply income changes
            for (ItemState st : income.getMixinSequence(incomeState)) {

              // delete lock properties if present
              if (st.getData().isNode() && st.getState() == ItemState.DELETED) {
                for (ItemState inSt : generateDeleleLockProperties((NodeData) st.getData()))
                  resultState.add(inSt);
              }

              resultState.add(st);
            }
          }
          break;
        }
      }
    }

    // apply income changes if not processed
    if (!itemChangeProcessed) {
      for (ItemState st : income.getMixinSequence(incomeState)) {

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
