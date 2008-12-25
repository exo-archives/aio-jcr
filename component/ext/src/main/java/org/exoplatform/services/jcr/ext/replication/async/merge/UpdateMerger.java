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
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedPropertyData;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: AddMerger.java 24880 2008-12-11 11:49:03Z tolusha $
 */
public class UpdateMerger implements ChangesMerger {

  protected final boolean             localPriority;

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  public UpdateMerger(boolean localPriority,
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

    // incomeState is DELETE state and nextIncomeState is UPDATE state
    ItemState incomeState = itemChange;
    ItemState nextIncomeState = null;
    if (incomeState.getData().isNode()) {
      nextIncomeState = income.getNextItemState(incomeState);
    }

    List<ItemState> resultEmptyState = new ArrayList<ItemState>();
    List<ItemState> resultState = new ArrayList<ItemState>();

    for (ItemState localState : local.getAllStates()) {
      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          break;
        case ItemState.UPDATED:
          if (!incomeData.isNode() && incomeData.getIdentifier().equals(localData.getIdentifier())) {
            return resultEmptyState;
          }
          break;
        case ItemState.DELETED:
          ItemState nextLocalState = local.getNextItemState(localState);

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (incomeData.isNode()
                && (income.getNextItemStateByUUIDOnUpdate(incomeState, localData.getIdentifier()) != null || income.getNextItemStateByUUIDOnUpdate(incomeState,
                                                                                                                                                   nextLocalState.getData()
                                                                                                                                                                 .getParentIdentifier()) != null)) {
              return resultEmptyState;
            } else if (!incomeData.isNode()
                && incomeData.getParentIdentifier().equals(localData.getIdentifier())) {
              return resultEmptyState;
            }
            break;
          }

          // UPDATE
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            ItemState nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                                      incomeState.getData()
                                                                                 .getIdentifier());
            if (incomeData.isNode() && nextItem != null) {
              return resultEmptyState;
            }

            nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                            incomeState.getData()
                                                                       .getParentIdentifier());
            if (!incomeData.isNode() && nextItem != null) {
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

              incomeState = new ItemState(item, ItemState.UPDATED, false, name);
              resultState.add(incomeState);
              itemChangeProcessed = true;
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (income.getNextItemStateByUUIDOnUpdate(incomeState, localState.getData()
                                                                             .getIdentifier()) != null) {
              return resultEmptyState;
            }
          } else {
            if (incomeData.getIdentifier().equals(localData.getIdentifier())) {
              return resultEmptyState;
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
          break;
        case ItemState.UPDATED:
          // TODO
          break;
        case ItemState.DELETED:
          // DELETE
          if (localData.isNode()) {
            if (localData.getIdentifier().equals(incomeData.getParentIdentifier())) {
              for (Iterator<ItemState> exp = exporter.exportItem(localData.getIdentifier()); exp.hasNext();) {
                resultState.add(exp.next());
              }
              return resultState;
            } else if (income.getNextItemStateByUUIDOnUpdate(incomeState, localData.getIdentifier()) != null) {
              resultState.add(new ItemState(localData, ItemState.ADDED, false, localData.getQPath()));
              break;
            }
          } else {
            if (localData.getIdentifier().equals(incomeData.getIdentifier())) {
              resultState.add(new ItemState(localData, ItemState.ADDED, false, localData.getQPath()));
              itemChangeProcessed = true;
            }
          }
          break;
        case ItemState.RENAMED:
          // TODO
          break;
        case ItemState.MIXIN_CHANGED:
          break;
        }
      }
    }

    // apply income changes if not processed
    if (!itemChangeProcessed) {
      resultState.add(incomeState);
      if (nextIncomeState != null) {
        resultState.addAll(income.getUpdateSequence(incomeState));
      }
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
