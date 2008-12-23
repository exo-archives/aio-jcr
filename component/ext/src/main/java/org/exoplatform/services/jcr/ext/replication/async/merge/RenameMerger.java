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
import java.util.List;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: AddMerger.java 24880 2008-12-11 11:49:03Z tolusha $
 */
public class RenameMerger implements ChangesMerger {

  protected final boolean             localPriority;

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  public RenameMerger(boolean localPriority,
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
   */
  public List<ItemState> merge(ItemState itemChange,
                               TransactionChangesLog income,
                               TransactionChangesLog local) throws IOException {
    boolean itemChangeProcessed = false;

    // incomeState is DELETE state and nextIncomeState is RENAME state
    ItemState incomeState = itemChange;
    ItemState nextIncomeState = income.getNextItemState(incomeState);

    List<ItemState> resultEmptyState = new ArrayList<ItemState>();
    List<ItemState> resultState = new ArrayList<ItemState>();

    for (ItemState localState : local.getAllStates()) {
      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (localData.getQPath().isDescendantOf(incomeData.getQPath())
              || localData.getQPath().equals(incomeData.getQPath())
              || localData.getQPath().isDescendantOf(nextIncomeState.getData().getQPath())
              || localData.getQPath().equals(nextIncomeState.getData().getQPath())) {
            return resultEmptyState;
          }
          break;
        case ItemState.UPDATED:
          // TODO
          break;
        case ItemState.DELETED:
          // simple DELETE
          if (incomeData.getQPath().equals(localData.getQPath())
              || nextIncomeState.getData().getQPath().isDescendantOf(localData.getQPath())
              || nextIncomeState.getData().getQPath().equals(localData.getQPath())) {
            return resultEmptyState;
          }
          break;
        case ItemState.RENAMED:
          // TODO
          break;
        case ItemState.MIXIN_CHANGED:
          // TODO
          break;
        }
      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          // TODO
          break;
        case ItemState.UPDATED:
          // TODO
          break;
        case ItemState.DELETED:
          // TODO
          break;
        case ItemState.RENAMED:
          // TODO
          break;
        case ItemState.MIXIN_CHANGED:
          // TODO
          break;
        }
      }
    }

    // apply income changes if not processed
    if (!itemChangeProcessed) {
      resultState.add(incomeState);
      resultState.add(nextIncomeState);
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
