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
 * @version $Id: AddMerger.java 25356 2008-12-18 09:54:16Z tolusha $
 */
public class DeleteMerger implements ChangesMerger {

  protected final boolean             localPriority;

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  public DeleteMerger(boolean localPriority,
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
   * @throws IOException
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

    boolean itemChangeProcessed = false;

    ItemState incomeState = itemChange;
    EditableChangesStorage<ItemState> resultEmptyState = new EditableItemStatesStorage<ItemState>(new File(mergeTempDir));
    EditableChangesStorage<ItemState> resultState = new EditableItemStatesStorage<ItemState>(new File(mergeTempDir));

    for (Iterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();
      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.isNode()) {
            if (localData.getQPath().isDescendantOf(incomeData.getQPath())) {
              skippedList.add(incomeData.getQPath());
              return resultEmptyState;
            }
          } else {
            if (localData.isNode()) {
              if ((income.findNextState(incomeState,
                                        incomeState.getData().getParentIdentifier(),
                                        incomeState.getData().getQPath().makeParentPath(),
                                        ItemState.DELETED) != null)
                  && (localData.getQPath().isDescendantOf(incomeData.getQPath().makeParentPath()))) {
                skippedList.add(incomeData.getQPath().makeParentPath());
                return resultEmptyState;
              }
            }
          }
          break;
        case ItemState.DELETED:
          ItemState nextState = local.findNextItemState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextState != null && nextState.getState() == ItemState.UPDATED) {

            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || local.getNextItemStateByUUIDOnUpdate(localState, incomeData.getIdentifier()) != null) {

              int relativeDegree = incomeState.getData().getQPath().getEntries().length
                  - localData.getQPath().getEntries().length;

              ItemState parent = local.getNextItemStateByIndexOnUpdate(localState,
                                                                       incomeState.getData()
                                                                                  .getQPath()
                                                                                  .makeAncestorPath(relativeDegree)
                                                                                  .getIndex());

              // set new QPath
              QPathEntry names[] = new QPathEntry[incomeData.getQPath().getEntries().length];
              System.arraycopy(parent.getData().getQPath().getEntries(),
                               0,
                               names,
                               0,
                               parent.getData().getQPath().getEntries().length);
              System.arraycopy(incomeData.getQPath().getEntries(),
                               localData.getQPath().getEntries().length,
                               names,
                               localData.getQPath().getEntries().length,
                               incomeData.getQPath().getEntries().length
                                   - localData.getQPath().getEntries().length);

              // set new ItemData
              if (incomeData.isNode()) {
                NodeData node = (NodeData) incomeData;
                TransientNodeData item = new TransientNodeData(new QPath(names),
                                                               node.getIdentifier(),
                                                               node.getPersistedVersion(),
                                                               node.getPrimaryTypeName(),
                                                               node.getMixinTypeNames(),
                                                               node.getOrderNumber(),
                                                               node.getParentIdentifier(),
                                                               node.getACL());
                incomeState = new ItemState(item,
                                            incomeState.getState(),
                                            incomeState.isEventFire(),
                                            new QPath(names),
                                            incomeState.isInternallyCreated(),
                                            incomeState.isPersisted());
                resultState.add(incomeState);
              } else {
                PropertyData prop = (PropertyData) incomeData;
                TransientPropertyData item = new TransientPropertyData(new QPath(names),
                                                                       prop.getIdentifier(),
                                                                       prop.getPersistedVersion(),
                                                                       prop.getType(),
                                                                       prop.getParentIdentifier(),
                                                                       prop.isMultiValued());
                item.setValues(prop.getValues());

                incomeState = new ItemState(item,
                                            incomeState.getState(),
                                            incomeState.isEventFire(),
                                            new QPath(names),
                                            incomeState.isInternallyCreated(),
                                            incomeState.isPersisted());
                resultState.add(incomeState);
              }
              itemChangeProcessed = true;
            }
            break;
          }

          // RENAMED sequences
          if (nextState != null && nextState.getState() == ItemState.RENAMED) {
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || incomeData.getQPath().equals(localData.getQPath())
                || incomeData.getQPath().isDescendantOf(nextState.getData().getQPath())
                || incomeData.getQPath().equals(nextState.getData().getQPath())) {
              skippedList.add(incomeData.getQPath());
              return resultEmptyState;
            }
            break;
          }

          // DELETE
          if (incomeData.isNode() && !localData.isNode()) {
            break;
          } else if (incomeData.getQPath().isDescendantOf(localData.getQPath())
              || incomeData.getQPath().equals(localData.getQPath())) {
            skippedList.add(incomeData.getQPath());
            return resultEmptyState;
          }
          break;
        case ItemState.UPDATED:
          if ((!localData.isNode())
              && ((incomeData.isNode() && localData.getQPath()
                                                   .isDescendantOf(incomeData.getQPath())) || (!incomeData.isNode() && localData.getQPath()
                                                                                                                                .equals(incomeData.getQPath())))) {
            skippedList.add(incomeData.getQPath());
            return resultEmptyState;
          }
          break;
        case ItemState.RENAMED:
          break;
        case ItemState.MIXIN_CHANGED:
          if (incomeData.isNode()) {
            if (localData.getQPath().equals(incomeData.getQPath())
                || localData.getQPath().isDescendantOf(incomeData.getQPath())) {
              skippedList.add(incomeData.getQPath());
              return resultEmptyState;
            }
          } else {
            ItemState parent = income.findNextState(incomeState,
                                                    incomeData.getParentIdentifier(),
                                                    incomeData.getQPath().makeParentPath(),
                                                    ItemState.DELETED);
            if (parent != null) {
              if (localData.getQPath().equals(parent.getData().getQPath())
                  || localData.getQPath().isDescendantOf(parent.getData().getQPath())) {
                skippedList.add(parent.getData().getQPath());
                return resultEmptyState;
              }
            }
          }
          break;
        }
      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.isNode()
              && localData.isNode()
              && (localData.getQPath().isDescendantOf(incomeData.getQPath()) || localData.getQPath()
                                                                                         .equals(incomeData.getQPath()))) {

            // add Delete state
            List<ItemState> items = local.getDescendantsChanges(localState,
                                                                incomeData.getQPath(),
                                                                true);
            for (int i = items.size() - 1; i >= 0; i--) {
              if (local.findLastState(items.get(i).getData().getQPath()) != ItemState.DELETED) {
                resultState.add(new ItemState(items.get(i).getData(),
                                              ItemState.DELETED,
                                              items.get(i).isEventFire(),
                                              items.get(i).getData().getQPath()));
              }
            }

            // apply income changes for all subtree
            for (ItemState st : income.getChanges(incomeState, incomeData.getQPath()))
              resultState.add(st);

            return resultState;
          } else if (!incomeData.isNode()
              && income.findNextState(incomeState,
                                      incomeState.getData().getParentIdentifier(),
                                      incomeState.getData().getQPath().makeParentPath(),
                                      ItemState.DELETED) != null
              && (localData.getQPath().isDescendantOf(incomeData.getQPath().makeParentPath()) || localData.getQPath()
                                                                                                          .equals(incomeData.getQPath()
                                                                                                                            .makeParentPath()))) {
            // add Delete state
            List<ItemState> items = local.getDescendantsChanges(localState,
                                                                incomeData.getQPath(),
                                                                true);
            for (int i = items.size() - 1; i >= 0; i--) {
              if (local.findLastState(items.get(i).getData().getQPath()) != ItemState.DELETED) {
                resultState.add(new ItemState(items.get(i).getData(),
                                              ItemState.DELETED,
                                              items.get(i).isEventFire(),
                                              items.get(i).getData().getQPath()));
              }
            }

            // apply income changes for all subtree
            for (ItemState st : income.getChanges(incomeState, incomeData.getQPath()))
              resultState.add(st);

            return resultState;
          }
          break;
        case ItemState.DELETED:
          ItemState nextState = local.findNextItemState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextState != null && nextState.getState() == ItemState.UPDATED) {

            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || local.getNextItemStateByUUIDOnUpdate(localState, incomeData.getIdentifier()) != null) {

              int relativeDegree = incomeState.getData().getQPath().getEntries().length
                  - localData.getQPath().getEntries().length;

              ItemState parent = local.getNextItemStateByIndexOnUpdate(localState,
                                                                       incomeState.getData()
                                                                                  .getQPath()
                                                                                  .makeAncestorPath(relativeDegree)
                                                                                  .getIndex());

              // set new QPath
              QPathEntry names[] = new QPathEntry[incomeData.getQPath().getEntries().length];
              System.arraycopy(parent.getData().getQPath().getEntries(),
                               0,
                               names,
                               0,
                               parent.getData().getQPath().getEntries().length);
              System.arraycopy(incomeData.getQPath().getEntries(),
                               localData.getQPath().getEntries().length,
                               names,
                               localData.getQPath().getEntries().length,
                               incomeData.getQPath().getEntries().length
                                   - localData.getQPath().getEntries().length);

              // set new ItemData
              if (incomeData.isNode()) {
                NodeData node = (NodeData) incomeData;
                TransientNodeData item = new TransientNodeData(new QPath(names),
                                                               node.getIdentifier(),
                                                               node.getPersistedVersion(),
                                                               node.getPrimaryTypeName(),
                                                               node.getMixinTypeNames(),
                                                               node.getOrderNumber(),
                                                               node.getParentIdentifier(),
                                                               node.getACL());
                incomeState = new ItemState(item,
                                            incomeState.getState(),
                                            incomeState.isEventFire(),
                                            new QPath(names),
                                            incomeState.isInternallyCreated(),
                                            incomeState.isPersisted());
                resultState.add(incomeState);
              } else {
                PropertyData prop = (PropertyData) incomeData;
                TransientPropertyData item = new TransientPropertyData(new QPath(names),
                                                                       prop.getIdentifier(),
                                                                       prop.getPersistedVersion(),
                                                                       prop.getType(),
                                                                       prop.getParentIdentifier(),
                                                                       prop.isMultiValued());
                item.setValues(prop.getValues());

                incomeState = new ItemState(item,
                                            incomeState.getState(),
                                            incomeState.isEventFire(),
                                            new QPath(names),
                                            incomeState.isInternallyCreated(),
                                            incomeState.isPersisted());
                resultState.add(incomeState);
              }
              itemChangeProcessed = true;
            }
            break;
          }

          // RENAMED sequences
          if (nextState != null && nextState.getState() == ItemState.RENAMED) {
            if (incomeData.getQPath().equals(localData.getQPath())) {
              resultState.add(new ItemState(nextState.getData(),
                                            ItemState.DELETED,
                                            nextState.isEventFire(),
                                            nextState.getData().getQPath()));
              itemChangeProcessed = true;
              break;
            } else if (!incomeData.isNode()
                && localData.getIdentifier().equals(incomeData.getParentIdentifier())
                && income.findNextState(incomeState,
                                        incomeState.getData().getParentIdentifier(),
                                        incomeState.getData().getQPath().makeParentPath(),
                                        ItemState.DELETED) == null) {
              resultState.add(new ItemState(nextState.getData(),
                                            ItemState.DELETED,
                                            nextState.isEventFire(),
                                            nextState.getData().getQPath()));

              resultState.addAll(exporter.exportItem(incomeData.getParentIdentifier()));

              itemChangeProcessed = true;
              break;
            }

            break;
          }

          // Simple DELETE
          if (incomeData.isNode() == localData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())) {
              return resultEmptyState;
            }
            break;
          } else if (incomeData.isNode() && !localData.isNode()) {
            break;
          } else if (incomeData.getQPath().isDescendantOf(localData.getQPath())
              || incomeData.getQPath().equals(localData.getQPath())) {
            return resultEmptyState;
          }
          break;
        case ItemState.UPDATED:
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
      if (incomeState.getData().isNode()) {
        for (ItemState st : generateDeleleLockProperties((NodeData) incomeState.getData()))
          resultState.add(st);
      }

      resultState.add(incomeState);
    }

    return resultState;
  }

  private List<ItemState> generateDeleleLockProperties(NodeData node) throws RepositoryException {
    List<ItemState> result = new ArrayList<ItemState>();

    if (ntManager.isNodeType(Constants.MIX_LOCKABLE,
                             node.getPrimaryTypeName(),
                             node.getMixinTypeNames())) {

      ItemData item = dataManager.getItemData(node, new QPathEntry(Constants.JCR_LOCKISDEEP, 1));
      result.add(new ItemState(item, ItemState.DELETED, true, node.getQPath()));

      item = dataManager.getItemData(node, new QPathEntry(Constants.JCR_LOCKOWNER, 1));
      result.add(new ItemState(item, ItemState.DELETED, true, node.getQPath()));
    }

    return result;
  }
}
