/**
 * 
 */
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
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: RenameAnalyzer.java 111 2008-11-11 11:11:11Z $
 */
public class RenameAnalyzer extends AbstractAnalyzer {

  /**
   * RenameAnalyzer constructor.
   * 
   * @param localPriority
   */
  public RenameAnalyzer(boolean localPriority,
                        DataManager dataManager,
                        NodeTypeDataManager ntManager) {
    super(localPriority, dataManager, ntManager);
  }

  /**
   * analyze.
   * 
   * @param incomeChange
   * @param nextIncomeChange
   * @param local
   * @param income
   * @param confilictResolver
   * @throws IOException
   * @throws ClassCastException
   * @throws ClassNotFoundException
   * @throws RemoteExportException
   * @throws RepositoryException
   */
  public void analyze(ItemState incomeChange,
                      ItemState nextIncomeChange,
                      ChangesStorage<ItemState> local,
                      ChangesStorage<ItemState> income,
                      ConflictResolver confilictResolver) throws IOException,
                                                         ClassCastException,
                                                         ClassNotFoundException,
                                                         RemoteExportException,
                                                         RepositoryException {

    ItemState incomeState = incomeChange;

    QPath incNodePath = incomeState.getData().isNode()
        ? incomeState.getData().getQPath()
        : incomeState.getData().getQPath().makeParentPath();

    QPath nextIncNodePath = nextIncomeChange.getData().isNode()
        ? nextIncomeChange.getData().getQPath()
        : nextIncomeChange.getData().getQPath().makeParentPath();

    for (MarkableIterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
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

      // skip root node
      if (localData.getIdentifier().equals(Constants.ROOT_UUID)) {
        continue;
      }

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (localData.isNode()) {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)
                || localData.getQPath().equals(nextIncNodePath)
                || nextIncNodePath.equals(localData.getQPath())
                || nextIncNodePath.isDescendantOf(localData.getQPath())) {
              confilictResolver.addAll(income.getUniquePathesByUUID(incomeData.isNode()
                  ? incomeData.getIdentifier()
                  : incomeData.getParentIdentifier()));
              confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
            }
          } else {
            if (localData.getQPath().isDescendantOf(incNodePath)) {
              confilictResolver.addAll(income.getUniquePathesByUUID(incomeData.isNode()
                  ? incomeData.getIdentifier()
                  : incomeData.getParentIdentifier()));
              confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
            }
          }
          break;

        case ItemState.DELETED:
          if (localState.isPersisted()) {
            // simple DELETE
            if (localData.isNode()) {
              if (incNodePath.isDescendantOf(localData.getQPath())
                  || incNodePath.equals(localData.getQPath())
                  || localData.getQPath().equals(incNodePath)
                  || localData.getQPath().isDescendantOf(incNodePath)
                  || nextIncNodePath.isDescendantOf(localData.getQPath())) {
                confilictResolver.addAll(income.getUniquePathesByUUID(incomeData.isNode()
                    ? incomeData.getIdentifier()
                    : incomeData.getParentIdentifier()));
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            } else {
              if (incNodePath.isDescendantOf(localData.getQPath().makeParentPath())
                  || incNodePath.equals(localData.getQPath().makeParentPath())
                  || localData.getQPath().makeParentPath().equals(incNodePath)
                  || localData.getQPath().makeParentPath().isDescendantOf(incNodePath)
                  || nextIncNodePath.isDescendantOf(localData.getQPath().makeParentPath())) {
                confilictResolver.addAll(income.getUniquePathesByUUID(incomeData.isNode()
                    ? incomeData.getIdentifier()
                    : incomeData.getParentIdentifier()));
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            }
            break;
          }

          ItemState nextLocalState = local.findNextState(liter, localData.getIdentifier());

          // Update sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(liter, localState);
            for (ItemState item : updateSeq) {
              if (item.getData().getQPath().isDescendantOf(incNodePath)
                  || incNodePath.equals(item.getData().getQPath())
                  || incNodePath.isDescendantOf(item.getData().getQPath())
                  || nextIncNodePath.isDescendantOf(item.getData().getQPath())) {
                confilictResolver.addAll(income.getUniquePathesByUUID(incomeData.isNode()
                    ? incomeData.getIdentifier()
                    : incomeData.getParentIdentifier()));
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
                break;
              }
            }
            break;
          }

          // Rename sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            QPath localPath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocalPath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            if (localPath.isDescendantOf(incNodePath) || localPath.equals(incNodePath)
                || nextIncNodePath.isDescendantOf(localPath)
                || nextIncNodePath.equals(nextLocalPath)
                || nextLocalPath.isDescendantOf(incNodePath)) {
              confilictResolver.addAll(income.getUniquePathesByUUID(incomeData.isNode()
                  ? incomeData.getIdentifier()
                  : incomeData.getParentIdentifier()));
              confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
            }
            break;
          }

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)) {
              confilictResolver.addAll(income.getUniquePathesByUUID(incomeData.isNode()
                  ? incomeData.getIdentifier()
                  : incomeData.getParentIdentifier()));
              confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (localData.getQPath().equals(incNodePath)
              || localData.getQPath().isDescendantOf(incNodePath)) {
            confilictResolver.addAll(income.getUniquePathesByUUID(incomeData.isNode()
                ? incomeData.getIdentifier()
                : incomeData.getParentIdentifier()));
            confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
          }
          break;
        }

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.isNode()) {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)
                || localData.getQPath().isDescendantOf(nextIncNodePath)
                || localData.getQPath().equals(nextIncNodePath)) {
              confilictResolver.add(localData.getQPath());
            } else {
              if (localData.getQPath().isDescendantOf(incNodePath)
                  || localData.getQPath().equals(incNodePath)
                  || localData.getQPath().isDescendantOf(nextIncNodePath)
                  || localData.getQPath().equals(nextIncNodePath)) {
                confilictResolver.add(localData.getQPath());
              }
            }
          }
          break;

        case ItemState.DELETED:
          if (localState.isPersisted()) {
            // DELETE
            if (localData.isNode()) {
              if (incNodePath.isDescendantOf(localData.getQPath())
                  || incNodePath.equals(localData.getQPath())
                  || localData.getQPath().equals(incNodePath)
                  || localData.getQPath().isDescendantOf(incNodePath)
                  || nextIncNodePath.isDescendantOf(localData.getQPath())) {
                confilictResolver.add(localData.getQPath());
              }
            } else {
              if (incNodePath.isDescendantOf(localData.getQPath().makeParentPath())
                  || incNodePath.equals(localData.getQPath().makeParentPath())
                  || localData.getQPath().makeParentPath().equals(incNodePath)
                  || localData.getQPath().makeParentPath().isDescendantOf(incNodePath)
                  || nextIncNodePath.isDescendantOf(localData.getQPath().makeParentPath())) {
                confilictResolver.add(localData.getQPath());
              }
            }
            break;
          }

          ItemState nextLocalState = local.findNextState(liter, localData.getIdentifier());

          // Update sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(liter, localState);
            for (ItemState st : updateSeq) {
              if (st.getData().getQPath().isDescendantOf(incNodePath)
                  || incNodePath.equals(st.getData().getQPath())
                  || incNodePath.isDescendantOf(st.getData().getQPath())
                  || nextIncNodePath.isDescendantOf(st.getData().getQPath())) {
                confilictResolver.add(st.getData().getQPath());
              }
            }

            break;
          }

          // Rename sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            QPath localPath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocalPath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            if (localPath.isDescendantOf(incNodePath) || localPath.equals(incNodePath)
                || nextIncNodePath.isDescendantOf(localPath)
                || nextIncNodePath.equals(nextLocalPath)
                || nextLocalPath.isDescendantOf(incNodePath)) {
              confilictResolver.addAll(local.getUniquePathesByUUID(localData.isNode()
                  ? localData.getIdentifier()
                  : localData.getParentIdentifier()));
            }
            break;
          }

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (localData.getQPath().isDescendantOf(incNodePath)
                || localData.getQPath().equals(incNodePath)) {
              confilictResolver.add(localData.getQPath());
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.isNode()) {
            if (localData.getQPath().equals(incomeData.getQPath())
                || localData.getQPath().isDescendantOf(incNodePath)) {
              confilictResolver.add(localData.getQPath());
            }
          }
          break;
        }
      }
    }
  }

}
