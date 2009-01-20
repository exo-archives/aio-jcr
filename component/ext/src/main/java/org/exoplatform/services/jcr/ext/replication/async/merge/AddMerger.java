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
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
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
public class AddMerger implements ChangesMerger {

  protected final boolean             localPriority;

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  public AddMerger(boolean localPriority,
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

    // TODO Invalid order number when added two not conflicted nodes

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
          if (localData.isNode()) {
            if (incomeData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(localData.getQPath())
                  || localData.getQPath().isDescendantOf(incomeData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }

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
                  return resultEmptyState;
                }
              }
            }
          } else {
            if (incomeData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath().makeParentPath())
                  || incomeData.getQPath().equals(localData.getQPath().makeParentPath())
                  || localData.getQPath().makeParentPath().isDescendantOf(incomeData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }

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
                  return resultEmptyState;
                }
              }

            } else {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }
            }
          }
          break;
        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextItemState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {

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
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || incomeData.getQPath().equals(localData.getQPath())
                || incomeData.getQPath().isDescendantOf(nextLocalState.getData().getQPath())
                || incomeData.getQPath().equals(nextLocalState.getData().getQPath())) {
              skippedList.add(incomeData.getQPath());
              return resultEmptyState;
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if ((incomeData.getQPath().isDescendantOf(localData.getQPath()) || incomeData.getQPath()
                                                                                         .equals(localData.getQPath()))) {
              skippedList.add(incomeData.getQPath());
              return resultEmptyState;
            }
          } else {

          }
          break;
        case ItemState.UPDATED:
          if (!localData.isNode()) { // update locally single property
            if (incomeData.isNode()) {
              if (localData.getQPath().isDescendantOf(incomeData.getQPath())) {
                skippedList.add(incomeData.getQPath());
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
          break;
        }
      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
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
            List<ItemState> items = local.getDescendantsChanges(localState,
                                                                localData.getQPath(),
                                                                true);
            for (int i = items.size() - 1; i >= 0; i--) {
              if (local.findLastState(items.get(i).getData().getQPath()) != ItemState.DELETED) {
                resultState.add(new ItemState(items.get(i).getData(),
                                              ItemState.DELETED,
                                              items.get(i).isEventFire(),
                                              items.get(i).getData().getQPath()));
              }
            }
            if (local.findLastState(localData.getQPath()) != ItemState.DELETED) {
              resultState.add(new ItemState(localData,
                                            ItemState.DELETED,
                                            localState.isEventFire(),
                                            localData.getQPath()));
            }

            // add all state from income changes
            for (ItemState st : income.getChanges(incomeState, incomeData.getQPath()))
              resultState.add(st);

            return resultState;
          }
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          ItemState nextState = local.findNextItemState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextState != null && nextState.getState() == ItemState.UPDATED) {
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
          if (nextState != null && nextState.getState() == ItemState.RENAMED) {
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || incomeData.getQPath().equals(localData.getQPath())
                || incomeData.getQPath().isDescendantOf(nextState.getData().getQPath())
                || incomeData.getQPath().equals(nextState.getData().getQPath())) {

              // add DELETE state
              List<ItemState> items = local.getDescendantsChanges(nextState,
                                                                  nextState.getData().getQPath(),
                                                                  true);
              for (int i = items.size() - 1; i >= 0; i--) {
                if (local.findLastState(items.get(i).getData().getQPath()) != ItemState.DELETED) {
                  resultState.add(new ItemState(items.get(i).getData(),
                                                ItemState.DELETED,
                                                items.get(i).isEventFire(),
                                                items.get(i).getData().getQPath()));
                }
              }
              if (local.findLastState(nextState.getData().getQPath()) != ItemState.DELETED) {
                resultState.add(new ItemState(nextState.getData(),
                                              ItemState.DELETED,
                                              nextState.isEventFire(),
                                              nextState.getData().getQPath()));
              }

              resultState.addAll(exporter.exportItem(localData.getIdentifier()));

              itemChangeProcessed = true;
            }
            break;
          }

          // Simple DELETE
          if (localData.isNode()
              && (incomeData.getQPath().isDescendantOf(localData.getQPath()) || incomeData.getQPath()
                                                                                          .equals(localData.getQPath()))) {

            // local.hasParentDelete(;)
            resultState.addAll(exporter.exportItem(localData.getParentIdentifier()));

            itemChangeProcessed = true;
            break;
          }
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

  /**
   * isPropertyAllowed.
   * 
   * @param propertyName
   * @param parent
   * @return
   */
  protected boolean isPropertyAllowed(InternalQName propertyName, NodeData parent) {
    PropertyDefinitionDatas pdef = ntManager.findPropertyDefinitions(propertyName,
                                                                     parent.getPrimaryTypeName(),
                                                                     parent.getMixinTypeNames());
    return pdef != null;
  }

  /**
   * isPathConflicted.
   * 
   * @param src
   * @param dst
   * @return
   */
  private boolean isPathConflicted(QPath src, QPath dst) {
    return src.isDescendantOf(dst) || src.equals(dst) || dst.isDescendantOf(src);
  }

}
