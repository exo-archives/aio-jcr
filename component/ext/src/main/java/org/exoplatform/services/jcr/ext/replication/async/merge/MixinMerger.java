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
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogReadException;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableItemStatesStorage;

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

    boolean itemChangeProcessed = false;
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
          if (localData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())
                || incomeData.getQPath().isDescendantOf(localData.getQPath())) {
              skippedList.add(incomeData.getQPath());
              return resultEmptyState;
            }
          }
          break;
        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextItemState(localState, localData.getIdentifier());

          // UPDATE node
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            // TODO
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (localData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())
                  || incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(nextLocalState.getData().getQPath())
                  || incomeData.getQPath().isDescendantOf(nextLocalState.getData().getQPath())) {
                skippedList.add(incomeData.getQPath());
                return resultEmptyState;
              }
            }
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
            for (int i = 1; i < mixinSequence.size(); i++) { // skip first state (MIXIN_CHANGED)
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
          break;
        case ItemState.DELETED:
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
      for (ItemState st : income.getMixinSequence(incomeState))
        resultState.add(st);
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
    // TODO case of remote changes merge, local managers can be not actual
    PropertyDefinitionDatas pdef = ntManager.findPropertyDefinitions(propertyName,
                                                                     parent.getPrimaryTypeName(),
                                                                     parent.getMixinTypeNames());
    return pdef != null;
  }
}
