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
import org.exoplatform.services.jcr.datamodel.InternalQName;
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
 * @version $Id$
 */
public class AddMerger extends AbstractMerger {

  public AddMerger(boolean localPriority,
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
                                         List<QPath> skippedList) throws RepositoryException,
                                                                 RemoteExportException,
                                                                 IOException,
                                                                 ClassCastException,
                                                                 ClassNotFoundException,
                                                                 ChangesLogReadException {

    // TODO Invalid order number when added two not conflicted nodes

    boolean itemChangeProcessed = false;

    ItemState incomeState = itemChange;
    ItemState parentNodeState;

    EditableChangesStorage<ItemState> resultState = new EditableItemStatesStorage<ItemState>(new File(mergeTempDir),
                                                                                             null);

    List<QPath> locSkippedList = new ArrayList<QPath>();

    outer: for (Iterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();

      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      // skip local itemstates
      for (QPath skipPath : locSkippedList) {
        if (localData.getQPath().equals(skipPath))
          continue outer;
      }

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
            if (incomeData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            } else {

              // try to add property and node with same name
              if ((incomeData.getQPath().equals(localData.getQPath()))) {
                InternalQName propertyName = !incomeData.isNode()
                    ? incomeData.getQPath().getName()
                    : localData.getQPath().getName();
                String parentIdentifier = !incomeData.isNode()
                    ? incomeData.getParentIdentifier()
                    : localData.getParentIdentifier();

                if (!isPropertyAllowed(propertyName,
                                       (NodeData) dataManager.getItemData(parentIdentifier))) {
                  skippedList.add(incomeData.getQPath());
                  return new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }
            }
          } else {
            if (incomeData.isNode()) {

              // try to add property and node with same name
              if ((incomeData.getQPath().equals(localData.getQPath()))) {
                InternalQName propertyName = !incomeData.isNode()
                    ? incomeData.getQPath().getName()
                    : localData.getQPath().getName();
                String parentIdentifier = !incomeData.isNode()
                    ? incomeData.getParentIdentifier()
                    : localData.getParentIdentifier();

                if (!isPropertyAllowed(propertyName,
                                       (NodeData) dataManager.getItemData(parentIdentifier))) {
                  skippedList.add(incomeData.getQPath());
                  return new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }

            } else {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {

            // TODO

            // if item added to updated item
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {

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

          // RENAME sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (localData.isNode()) {
              if (incomeData.isNode()) {
                if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                    || incomeData.getQPath().equals(nextLocalState.getData().getQPath())) {
                  skippedList.add(incomeData.getQPath());
                  return new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              } else {
                if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                  skippedList.add(incomeData.getQPath());
                  return new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), null);
                }
              }
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (incomeData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), null);
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return new EditableItemStatesStorage<ItemState>(new File(mergeTempDir), null);
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

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (local.findNextState(localState,
                                  localData.getIdentifier(),
                                  localData.getQPath(),
                                  ItemState.DELETED) != null) {
            break;
          }

          if (localData.isNode()) {
            if (incomeData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {

                // try to add property and node with same name
                if (incomeData.isNode() != localData.isNode()) {
                  InternalQName propertyName = !incomeData.isNode()
                      ? incomeData.getQPath().getName()
                      : localData.getQPath().getName();
                  String parentIdentifier = !incomeData.isNode()
                      ? incomeData.getParentIdentifier()
                      : localData.getParentIdentifier();

                  if (isPropertyAllowed(propertyName,
                                        (NodeData) dataManager.getItemData(parentIdentifier))) {
                    break;
                  }
                }

                // add DELETE state
                List<ItemState> items = local.getChanges(localState, localData.getQPath(), true);
                for (int i = items.size() - 1; i >= 0; i--) {
                  if (local.findLastState(items.get(i).getData().getQPath()) != ItemState.DELETED) {

                    // delete lock properties if present
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
                for (ItemState st : income.getChanges(incomeState, incomeData.getQPath())) {

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
          } else {
            if (!incomeData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                resultState.add(new ItemState(localData,
                                              ItemState.DELETED,
                                              localState.isEventFire(),
                                              localData.getQPath()));
              }
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {

            // TODO

            // if item added to updated item
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {

              int relativeDegree = incomeState.getData().getQPath().getEntries().length
                  - localData.getQPath().getEntries().length;

              ItemState parent = local.getNextItemStateByIndexOnUpdate(localState,
                                                                       incomeState.getData()
                                                                                  .getQPath()
                                                                                  .makeAncestorPath(relativeDegree)
                                                                                  .getIndex());

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
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            QPath incNodePath = incomeData.isNode()
                ? incomeData.getQPath()
                : incomeData.getQPath().makeParentPath();

            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocNodePath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            if (incomeData.getQPath().isDescendantOf(locNodePath)
                || incomeData.getQPath().equals(locNodePath)
                || incomeData.getQPath().isDescendantOf(nextLocNodePath)
                || incomeData.getQPath().equals(nextLocNodePath)) {

              // add DELETE state
              List<ItemState> items = local.getRenameSequence(localState);
              for (int i = items.size() - 1; i >= 0; i--) {
                ItemState item = items.get(i);
                locSkippedList.add(item.getData().getQPath());

                if (item.getState() == ItemState.RENAMED) {

                  // delete lock properties if present
                  if (item.getData().isNode()) {
                    for (ItemState st : generateDeleleLockProperties((NodeData) item.getData()))
                      resultState.add(st);
                  }

                  resultState.add(new ItemState(item.getData(),
                                                ItemState.DELETED,
                                                item.isEventFire(),
                                                item.getData().getQPath()));

                } else if (item.getState() == ItemState.DELETED) {
                  if (!incNodePath.equals(locNodePath)) {
                    if (item.getData().isNode()) {
                      NodeData node = (NodeData) item.getData();
                      TransientNodeData newNode = new TransientNodeData(node.getQPath(),
                                                                        node.getIdentifier(),
                                                                        node.getPersistedVersion(),
                                                                        node.getPrimaryTypeName(),
                                                                        node.getMixinTypeNames(),
                                                                        node.getOrderNumber(),
                                                                        node.getParentIdentifier(),
                                                                        node.getACL());

                      ItemState newItem = new ItemState(newNode,
                                                        ItemState.ADDED,
                                                        item.isEventFire(),
                                                        node.getQPath());
                      resultState.add(newItem);

                    } else {
                      PropertyData prop = (PropertyData) item.getData();
                      TransientPropertyData newProp = new TransientPropertyData(prop.getQPath(),
                                                                                prop.getIdentifier(),
                                                                                prop.getPersistedVersion(),
                                                                                prop.getType(),
                                                                                prop.getParentIdentifier(),
                                                                                prop.isMultiValued());

                      newProp.setValues(((PropertyData) items.get(items.size() - i - 1).getData()).getValues());
                      ItemState newItem = new ItemState(newProp,
                                                        ItemState.ADDED,
                                                        item.isEventFire(),
                                                        prop.getQPath());
                      resultState.add(newItem);
                    }
                  }
                }
              }

              for (ItemState st : income.getChanges(incomeState, incomeData.isNode()
                  ? incomeData.getQPath()
                  : incomeData.getQPath().makeParentPath()))
                resultState.add(st);

              return resultState;
            }

            break;
          }

          // Simple DELETE
          if (income.findNextState(incomeState,
                                   incomeData.getIdentifier(),
                                   incomeData.getQPath(),
                                   ItemState.ADDED) != null) {
            break;
          }

          if (local.findNextState(localState,
                                  localData.getParentIdentifier(),
                                  localData.getQPath().makeParentPath(),
                                  ItemState.DELETED) != null) {
            break;
          }

          if (localData.isNode()) {
            if (incomeData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
              /*|| incomeData.getQPath().equals(localData.getQPath())*/) {

                skippedList.add(localData.getQPath());
                resultState.addAll(exporter.exportItem(localData.getParentIdentifier()));

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
      resultState.add(incomeState);
    }

    return resultState;
  }
}
