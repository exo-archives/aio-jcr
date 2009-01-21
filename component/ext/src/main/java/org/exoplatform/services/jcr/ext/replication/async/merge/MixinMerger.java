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
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: AddMerger.java 26315 2009-01-06 12:51:39Z tolusha $
 */
public class MixinMerger implements ChangesMerger {

  protected final boolean             localPriority;

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  public MixinMerger(boolean localPriority,
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
   * @throws ClassNotFoundException
   * @throws ClassCastException
   */
  public ChangesStorage<ItemState> merge(ItemState itemChange,
                                         ChangesStorage<ItemState> income,
                                         ChangesStorage<ItemState> local,
                                         String mergeTempDir,
                                         List<QPath> skippedList) throws RepositoryException,
                                                                 RemoteExportException,
                                                                 IOException,
                                                                 ClassCastException,
                                                                 ClassNotFoundException,
                                                                 ChangesLogReadException {

    boolean itemChangeProcessed = false; // TODO really need?

    ItemState incomeState = itemChange;
    EditableChangesStorage<ItemState> resultEmptyState = new EditableItemStatesStorage<ItemState>(new File(mergeTempDir));
    EditableChangesStorage<ItemState> resultState = new EditableItemStatesStorage<ItemState>(new File(mergeTempDir));

    for (Iterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();

      ItemData localData = localState.getData();
      ItemData incomeData = incomeState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          break;
        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextItemState(localState, localData.getIdentifier());

          // UPDATE node
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            // TODO
            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (localData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())
                  || incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())
                || incomeData.getQPath().isDescendantOf(localData.getQPath())) {
              skippedList.add(incomeData.getQPath());
              return resultEmptyState;
            }
          }
          break;
        case ItemState.UPDATED:
          // UPDATE property
          break;
        case ItemState.RENAMED:
          break;
        case ItemState.MIXIN_CHANGED:
          if (incomeData.getQPath().equals(localData.getQPath())) {
            List<ItemState> mixinSequence = income.getMixinSequence(incomeState);
            for (int i = 1; i < mixinSequence.size(); i++) { // first state is MIXIN_CHANGED
              skippedList.add(mixinSequence.get(i).getData().getQPath());
            }
            return resultEmptyState;
          }
          break;
        }
      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          break;
        case ItemState.UPDATED:
          // UPDATE property
          break;
        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextItemState(localState, localData.getIdentifier());

          // UPDATE node
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            // TODO
            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

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
                        propData.setValues(((PropertyData) renameSequence.get(renameSequence.size()
                            - j - 1).getData()).getValues());
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

            // delete local mixin changes
            List<ItemState> localMixinSeq = local.getMixinSequence(localState);
            for (int i = localMixinSeq.size() - 1; i >= 0; i--) {
              ItemState item = localMixinSeq.get(i);

              // delete lock properties if present
              if (item.getData().isNode() && item.getState() == ItemState.DELETED) {
                for (ItemState st : generateDeleleLockProperties((NodeData) item.getData()))
                  resultState.add(st);
              }

              resultState.add(new ItemState(item.getData(),
                                            ItemState.DELETED,
                                            item.isEventFire(),
                                            item.getData().getQPath()));
            }

            // apply income rename
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
