/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.analyze;

import java.io.IOException;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.resolve.ConflictResolver;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.MarkableIterator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: UpdateProperyAnalyzer.java 111 2008-11-11 11:11:11Z $
 */
public class UpdatePropertyAnalyzer extends AbstractAnalyzer {

  /**
   * UpdateAnalyzer constructor.
   * 
   * @param localPriority
   */
  public UpdatePropertyAnalyzer(boolean localPriority,
                                DataManager dataManager,
                                NodeTypeDataManager ntManager) {
    super(localPriority, dataManager, ntManager);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws RepositoryException
   * @throws RemoteExportException
   */
  public void analyze(ItemState incomeChange,
                      ChangesStorage<ItemState> local,
                      ChangesStorage<ItemState> income,
                      ConflictResolver confilictResolver) throws IOException,
                                                         ClassCastException,
                                                         ClassNotFoundException,
                                                         RemoteExportException,
                                                         RepositoryException {
    for (MarkableIterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();

      ItemData incomeData = incomeChange.getData();
      ItemData localData = localState.getData();

      if (asyncHelper.isLockProperty(localData.getQPath().getName())) {
        continue;
      }

      if (asyncHelper.isFixedIdentifier(localData.getIdentifier())) {
        continue;
      }

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          break;

        case ItemState.DELETED:
          if (localState.isPersisted()) {
            // DELETE
            if (localData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            } else {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            }
            break;
          }

          ItemState nextLocalState = local.findNextState(liter, localData.getIdentifier());

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            if (incomeData.getQPath().isDescendantOf(locNodePath)) {
              confilictResolver.add(incomeData.getQPath());
              confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
            }

            break;
          }

          // UPDATE
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> locUpdateSeq = local.getUpdateSequence(liter, localState);
            for (ItemState locSt : locUpdateSeq) {
              if (incomeData.getQPath().isDescendantOf(locSt.getData().getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            }
            break;
          }

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())) {
              confilictResolver.add(incomeData.getQPath());
              confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
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

        case ItemState.DELETED:
          if (localState.isPersisted()) {
            // DELETE
            if (localData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath()))
                confilictResolver.add(localData.getQPath());
            } else {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                confilictResolver.add(localData.getQPath());
              }
            }
            break;
          }

          ItemState nextLocalState = local.findNextState(liter, localData.getIdentifier());

          // UPDATE
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> locUpdateSeq = local.getUpdateSequence(liter, localState);
            for (ItemState locSt : locUpdateSeq) {
              if (incomeData.getQPath().isDescendantOf(locSt.getData().getQPath()))
                confilictResolver.add(locSt.getData().getQPath());
            }
            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            if (incomeData.getQPath().isDescendantOf(locNodePath)) {
              confilictResolver.addAll(local.getUniquePathesByUUID(localData.isNode()
                  ? localData.getIdentifier()
                  : localData.getParentIdentifier()));
            }
            break;
          }

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())) {
              confilictResolver.add(localData.getQPath());
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          break;
        }
      }
    }
  }
}
