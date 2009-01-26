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
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
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
public class DeleteMerger extends AbstractMerger {

  public DeleteMerger(Member localMember,
                      boolean localPriority,
                      RemoteExporter exporter,
                      DataManager dataManager,
                      NodeTypeDataManager ntManager) {
    super(localMember, localPriority, exporter, dataManager, ntManager);
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
    EditableChangesStorage<ItemState> resultEmptyState = new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), localMember);
    EditableChangesStorage<ItemState> resultState = new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), localMember);

    ItemState parentNodeState;

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
          parentNodeState = income.findNextState(incomeState,
                                                 incomeState.getData().getParentIdentifier(),
                                                 incomeState.getData().getQPath().makeParentPath(),
                                                 ItemState.DELETED);

          if (incomeState.getData().isNode() || parentNodeState != null) {
            QPath incNodePath = incomeData.isNode()
                ? incomeData.getQPath()
                : parentNodeState.getData().getQPath();

            if (localData.getQPath().isDescendantOf(incNodePath)) {
              skippedList.add(incNodePath);
              return resultEmptyState;
            }
          } else {
            if (!localData.isNode()) {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            // TODO

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

                // delete lock properties if present
                for (ItemState st : generateDeleleLockProperties((NodeData) incomeState.getData()))
                  resultState.add(st);

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
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            parentNodeState = income.findNextState(incomeState,
                                                   incomeState.getData().getParentIdentifier(),
                                                   incomeState.getData()
                                                              .getQPath()
                                                              .makeParentPath(),
                                                   ItemState.DELETED);

            if (incomeState.getData().isNode() || parentNodeState != null) {
              QPath incNodePath = incomeData.isNode()
                  ? incomeData.getQPath()
                  : parentNodeState.getData().getQPath();

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
                return resultEmptyState;
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }
            }
            break;
          }

          // DELETE
          parentNodeState = income.findNextState(incomeState,
                                                 incomeState.getData().getParentIdentifier(),
                                                 incomeState.getData().getQPath().makeParentPath(),
                                                 ItemState.DELETED);

          if (incomeData.isNode() || parentNodeState != null) {
            QPath incNodePath = incomeData.isNode()
                ? incomeData.getQPath()
                : parentNodeState.getData().getQPath();

            if (incNodePath.isDescendantOf(localData.getQPath())
                || incNodePath.equals(localData.getQPath())) {
              skippedList.add(incNodePath);
              return resultEmptyState;
            }
          } else {
            if (localData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }
            }
          }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            parentNodeState = income.findNextState(incomeState,
                                                   incomeState.getData().getParentIdentifier(),
                                                   incomeState.getData()
                                                              .getQPath()
                                                              .makeParentPath(),
                                                   ItemState.DELETED);

            if (incomeData.isNode() || parentNodeState != null) {
              QPath incNodePath = incomeData.isNode()
                  ? incomeData.getQPath()
                  : parentNodeState.getData().getQPath();

              if (localData.getQPath().isDescendantOf(incNodePath)) {
                skippedList.add(incNodePath);
                return resultEmptyState;
              }
            } else {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }
            }
          }

          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          parentNodeState = income.findNextState(incomeState,
                                                 incomeState.getData().getParentIdentifier(),
                                                 incomeState.getData().getQPath().makeParentPath(),
                                                 ItemState.DELETED);

          if (incomeData.isNode() || parentNodeState != null) {
            QPath incNodePath = incomeData.isNode()
                ? incomeData.getQPath()
                : parentNodeState.getData().getQPath();

            if (localData.getQPath().equals(incNodePath)
                || localData.getQPath().isDescendantOf(incNodePath)) {
              skippedList.add(incNodePath);
              return resultEmptyState;
            }
          } else {
            List<ItemState> mixinSeq = local.getMixinSequence(localState);

            for (int i = 0; i < mixinSeq.size(); i++) {
              ItemState item = mixinSeq.get(i);
              if (!item.getData().isNode()) {
                if (item.getData().getQPath().equals(incomeData.getQPath())) {
                  skippedList.add(incomeData.getQPath());
                  return resultEmptyState;
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

          parentNodeState = income.findNextState(incomeState,
                                                 incomeState.getData().getParentIdentifier(),
                                                 incomeState.getData().getQPath().makeParentPath(),
                                                 ItemState.DELETED);

          if ((incomeData.isNode() || parentNodeState != null)) {

            QPath incNodePath = incomeData.isNode()
                ? incomeData.getQPath()
                : parentNodeState.getData().getQPath();

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
            // TODO

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

                // delete lock properties if present
                for (ItemState st : generateDeleleLockProperties((NodeData) incomeState.getData()))
                  resultState.add(st);

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
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            if (local.findNextState(nextLocalState,
                                    nextLocalState.getData().getIdentifier(),
                                    nextLocalState.getData().getQPath(),
                                    ItemState.DELETED) != null) {
              break;
            }

            parentNodeState = income.findNextState(incomeState,
                                                   incomeState.getData().getParentIdentifier(),
                                                   incomeState.getData()
                                                              .getQPath()
                                                              .makeParentPath(),
                                                   ItemState.DELETED);

            if ((incomeData.isNode() || parentNodeState != null)) {

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
