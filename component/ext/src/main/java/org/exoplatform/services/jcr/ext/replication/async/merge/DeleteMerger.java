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
          if (incomeData.isNode()
              && (localData.getQPath().isDescendantOf(incomeData.getQPath()) || localData.getQPath()
                                                                                         .equals(incomeData.getQPath()))) {
            return resultEmptyState;
          }
          break;
        case ItemState.DELETED:
          ItemState nextState = local.getNextItemState(localState);

          // UPDATE sequences
          if (nextState != null && nextState.getState() == ItemState.UPDATED) {

            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || incomeData.getQPath().equals(localData.getQPath())) {

              int relativeDegree = incomeState.getData().getQPath().getEntries().length
                  - localData.getQPath().getEntries().length - 1;

              // find state with parent node that is child of UPDATED node
              ItemState parentState = relativeDegree > 0
                  ? income.getPreviousItemStateByQPath(incomeState,
                                                       incomeState.getData()
                                                                  .getQPath()
                                                                  .makeAncestorPath(relativeDegree))
                  : null;

              // find new path of UPDATED node
              QPath parentPath = local.getNextItemStateByUUIDOnUpdate(localState,
                                                                      parentState != null
                                                                          ? parentState.getData()
                                                                                       .getParentIdentifier()
                                                                          : incomeData.getParentIdentifier())
                                      .getData()
                                      .getQPath();

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
                incomeState = new ItemState(item, ItemState.DELETED, false, new QPath(names));
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

                incomeState = new ItemState(item, ItemState.DELETED, false, new QPath(names));
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
              return resultEmptyState;
            }
            break;
          }

          // DELETE
          if (incomeData.isNode() && !localData.isNode()) {
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
            Collection<ItemState> itemsCollection = local.getDescendantsChanges(incomeData.getQPath(),
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

            // apply income changes for all subtree
            resultState.add(itemChange);
            resultState.addAll(income.getDescendantsChanges(incomeData.getQPath(), false, false));
            return resultState;
          }
          break;
        case ItemState.DELETED:
          ItemState nextState = local.getNextItemState(localState);

          // UPDATE sequences
          if (nextState != null && nextState.getState() == ItemState.UPDATED) {
            ItemState nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                                      localData.getIdentifier());

            // if item was deleted of updated item of its child
            if (incomeData.getParentIdentifier().equals(nextItem.getData().getIdentifier())) {
              QPath name = QPath.makeChildPath(nextItem.getData().getQPath(),
                                               incomeData.getQPath().getEntries()[incomeData.getQPath()
                                                                                            .getEntries().length - 1]);
              PropertyData prop = (PropertyData) incomeData;
              PersistedPropertyData item = new PersistedPropertyData(prop.getIdentifier(),
                                                                     name,
                                                                     prop.getParentIdentifier(),
                                                                     prop.getPersistedVersion(),
                                                                     prop.getType(),
                                                                     prop.isMultiValued());
              item.setValues(prop.getValues());

              incomeState = new ItemState(item, ItemState.ADDED, false, name);
              resultState.add(incomeState);
              itemChangeProcessed = true;
              break;
            }
            break;
          }

          // RENAMED sequences
          if (nextState != null && nextState.getState() == ItemState.RENAMED) {
            if (incomeData.isNode() && incomeData.getQPath().equals(localData.getQPath())) {
              resultState.add(new ItemState(nextState.getData(),
                                            ItemState.DELETED,
                                            false,
                                            localData.getQPath()));
              itemChangeProcessed = true;
              break;
            } else if (!incomeData.isNode()
                && localData.getIdentifier().equals(incomeData.getParentIdentifier())) {
              resultState.add(new ItemState(nextState.getData(),
                                            ItemState.DELETED,
                                            false,
                                            nextState.getData().getQPath()));
              resultState.addAll(exporter.exportItem(incomeData.getParentIdentifier())
                                         .getAllStates());
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
}
