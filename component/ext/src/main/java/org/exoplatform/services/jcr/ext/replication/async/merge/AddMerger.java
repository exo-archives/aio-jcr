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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedNodeData;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedPropertyData;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;

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
   */
  public List<ItemState> merge(ItemState itemChange,
                               TransactionChangesLog income,
                               TransactionChangesLog local) throws IOException, RepositoryException {

    boolean itemChangeProcessed = false;

    ItemState incomeState = itemChange;
    List<ItemState> resultEmptyState = new ArrayList<ItemState>();
    List<ItemState> resultState = new ArrayList<ItemState>();

    for (ItemState localState : local.getAllStates()) {
      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.getQPath().isDescendantOf(localData.getQPath())
              || incomeData.getQPath().equals(localData.getQPath())) {
            return resultEmptyState;
          }
          break;
        case ItemState.DELETED:
          ItemState nextState = local.getNextItemState(localState);

          // UPDATE sequences
          if (nextState != null && nextState.getState() == ItemState.UPDATED) {

            // if item added to updated item
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {

              ItemState parentState = income.getPreviousItemStateByQPath(incomeState,
                                                                         incomeState.getData()
                                                                                    .getQPath()
                                                                                    .makeAncestorPath(incomeState.getData()
                                                                                                                 .getQPath()
                                                                                                                 .getEntries().length
                                                                                        - localData.getQPath()
                                                                                                   .getEntries().length
                                                                                        - 1));

              QPath parentPath = local.getNextItemStateByUUIDOnUpdate(localState,
                                                                      parentState != null
                                                                          ? parentState.getData()
                                                                                       .getParentIdentifier()
                                                                          : incomeData.getParentIdentifier());

              // set new QPath
              QPathEntry names[] = new QPathEntry[incomeData.getQPath().getEntries().length];
              System.arraycopy(parentPath.getEntries(), 0, names, 0, parentPath.getEntries().length);
              System.arraycopy(incomeData.getQPath().getEntries(),
                               localData.getQPath().getEntries().length,
                               names,
                               localData.getQPath().getEntries().length,
                               incomeData.getQPath().getEntries().length
                                   - localData.getQPath().getEntries().length);

              // set new ItemData
              if (incomeData.isNode()) {
                NodeData node = (NodeData) incomeData;
                PersistedNodeData item = new PersistedNodeData(node.getIdentifier(),
                                                               new QPath(names),
                                                               node.getParentIdentifier(),
                                                               node.getPersistedVersion(),
                                                               node.getOrderNumber(),
                                                               node.getPrimaryTypeName(),
                                                               node.getMixinTypeNames(),
                                                               node.getACL());
                incomeState = new ItemState(item, ItemState.ADDED, false, new QPath(names));
                resultState.add(incomeState);
              } else {
                PropertyData prop = (PropertyData) incomeData;
                PersistedPropertyData item = new PersistedPropertyData(prop.getIdentifier(),
                                                                       new QPath(names),
                                                                       prop.getParentIdentifier(),
                                                                       prop.getPersistedVersion(),
                                                                       prop.getType(),
                                                                       prop.isMultiValued());
                item.setValues(prop.getValues());

                incomeState = new ItemState(item, ItemState.ADDED, false, new QPath(names));
                resultState.add(incomeState);
              }
              itemChangeProcessed = true;
            }
            break;
          }

          // RENAME sequences
          if (nextState != null && nextState.getState() == ItemState.RENAMED) {
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || incomeData.getQPath().equals(localData.getQPath())
                || incomeData.getQPath().isDescendantOf(nextState.getData().getQPath())
                || incomeData.getQPath().equals(nextState.getData().getQPath())) {
              return resultEmptyState;
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if ((incomeData.getQPath().isDescendantOf(localData.getQPath()) || incomeData.getQPath()
                                                                                         .equals(localData.getQPath()))) {
              return resultEmptyState;
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
          if (incomeData.getQPath().equals(localData.getQPath())) {

            // add DELETE state
            Collection<ItemState> itemsCollection = local.getDescendantsChanges(localData.getQPath(),
                                                                                true,
                                                                                true);
            ItemState itemsArray[];
            itemsCollection.toArray(itemsArray = new ItemState[itemsCollection.size()]);
            for (int i = itemsArray.length - 1; i >= 0; i--) {
              if (local.getLastState(itemsArray[i].getData().getQPath()) != ItemState.DELETED) {
                resultState.add(new ItemState(itemsArray[i].getData(),
                                              ItemState.DELETED,
                                              false,
                                              itemsArray[i].getData().getQPath()));
              }
            }
            if (local.getLastState(localData.getQPath()) != ItemState.DELETED) {
              resultState.add(new ItemState(localData,
                                            ItemState.DELETED,
                                            false,
                                            localData.getQPath()));
            }

            // add all state from income changes
            if (!itemChangeProcessed) {
              resultState.add(incomeState);
            }
            resultState.addAll(income.getDescendantsChanges(incomeData.getQPath(), false, false));

            itemChangeProcessed = true;
          }
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          ItemState nextState = local.getNextItemState(localState);

          // UPDATE sequences
          if (nextState != null && nextState.getState() == ItemState.UPDATED) {
            // if item added to updated item
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {

              ItemState parentState = income.getPreviousItemStateByQPath(incomeState,
                                                                         incomeState.getData()
                                                                                    .getQPath()
                                                                                    .makeAncestorPath(incomeState.getData()
                                                                                                                 .getQPath()
                                                                                                                 .getEntries().length
                                                                                        - localData.getQPath()
                                                                                                   .getEntries().length
                                                                                        - 1));

              QPath parentPath = local.getNextItemStateByUUIDOnUpdate(localState,
                                                                      parentState != null
                                                                          ? parentState.getData()
                                                                                       .getParentIdentifier()
                                                                          : incomeData.getParentIdentifier());

              QPathEntry names[] = new QPathEntry[incomeData.getQPath().getEntries().length];
              System.arraycopy(parentPath.getEntries(), 0, names, 0, parentPath.getEntries().length);
              System.arraycopy(incomeData.getQPath().getEntries(),
                               localData.getQPath().getEntries().length,
                               names,
                               localData.getQPath().getEntries().length,
                               incomeData.getQPath().getEntries().length
                                   - localData.getQPath().getEntries().length);

              // set new ItemData
              if (incomeData.isNode()) {
                NodeData node = (NodeData) incomeData;
                PersistedNodeData item = new PersistedNodeData(node.getIdentifier(),
                                                               new QPath(names),
                                                               node.getParentIdentifier(),
                                                               node.getPersistedVersion(),
                                                               node.getOrderNumber(),
                                                               node.getPrimaryTypeName(),
                                                               node.getMixinTypeNames(),
                                                               node.getACL());
                incomeState = new ItemState(item, ItemState.ADDED, false, new QPath(names));
                resultState.add(incomeState);
              } else {
                PropertyData prop = (PropertyData) incomeData;
                PersistedPropertyData item = new PersistedPropertyData(prop.getIdentifier(),
                                                                       new QPath(names),
                                                                       prop.getParentIdentifier(),
                                                                       prop.getPersistedVersion(),
                                                                       prop.getType(),
                                                                       prop.isMultiValued());
                item.setValues(prop.getValues());

                incomeState = new ItemState(item, ItemState.ADDED, false, new QPath(names));
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
              Collection<ItemState> itemsCollection = local.getDescendantsChanges(nextState.getData()
                                                                                           .getQPath(),
                                                                                  true,
                                                                                  true);
              ItemState itemsArray[];
              itemsCollection.toArray(itemsArray = new ItemState[itemsCollection.size()]);
              for (int i = itemsArray.length - 1; i >= 0; i--) {
                if (local.getLastState(itemsArray[i].getData().getQPath()) != ItemState.DELETED) {
                  resultState.add(new ItemState(itemsArray[i].getData(),
                                                ItemState.DELETED,
                                                false,
                                                itemsArray[i].getData().getQPath()));
                }
              }
              if (local.getLastState(nextState.getData().getQPath()) != ItemState.DELETED) {
                resultState.add(new ItemState(nextState.getData(),
                                              ItemState.DELETED,
                                              false,
                                              localData.getQPath()));
              }

              resultState.add(incomeState);
              resultState.addAll(income.getDescendantsChanges(incomeData.getQPath(), false, false));
              itemChangeProcessed = true;
            }
            break;
          }

          // Simple DELETE
          if (localData.isNode()
              && (incomeData.getQPath().isDescendantOf(localData.getQPath()) || incomeData.getQPath()
                                                                                          .equals(localData.getQPath()))) {
            resultState.addAll(exporter.exportItem(localData.getParentIdentifier()).getAllStates());
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

  protected boolean isPropertyAllowed(InternalQName propertyName, NodeData parent) {

    PropertyDefinitionDatas pdef = ntManager.findPropertyDefinitions(propertyName,
                                                                     parent.getPrimaryTypeName(),
                                                                     parent.getMixinTypeNames());

    return pdef != null;
  }

}
