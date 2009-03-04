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
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.resolve.ConflictResolver;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AddAnalyzer.java 111 2008-11-11 11:11:11Z $
 */
public class AddAnalyzer extends AbstractAnalyzer {

  /**
   * AddAnalyzer constructor.
   * 
   */
  public AddAnalyzer(boolean localPriority, DataManager dataManager, NodeTypeDataManager ntManager) {
    super(localPriority, dataManager, ntManager);
  }

  public void analyze(ItemState incomeChange,
                      ChangesStorage<ItemState> local,
                      ChangesStorage<ItemState> income,
                      ConflictResolver confilictResolver) throws IOException,
                                                         ClassCastException,
                                                         ClassNotFoundException,
                                                         RepositoryException {
    for (Iterator<ItemState> liter = local.getChanges(); liter.hasNext();) {
      ItemState localState = liter.next();

      ItemData incomeData = incomeChange.getData();
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
          if (localData.isNode()) {
            if (incomeData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
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
                  confilictResolver.add(incomeData.getQPath());
                  confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
                }
              }
            }
          } else {
            if (!incomeData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
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
                  confilictResolver.add(incomeData.getQPath());
                  confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
                }
              }
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState item : updateSeq) {
              if (incomeData.getQPath().isDescendantOf(item.getData().getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
                break;
              }
            }
          }

          // RENAME sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocNodePath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            if (incomeData.getQPath().isDescendantOf(locNodePath)
                || incomeData.getQPath().equals(locNodePath)
                || incomeData.getQPath().isDescendantOf(nextLocNodePath)
                || incomeData.getQPath().equals(nextLocNodePath)
                || nextLocNodePath.isDescendantOf(incomeData.getQPath())
                || nextLocNodePath.equals(incomeData.getQPath())) {
              confilictResolver.add(incomeData.getQPath());
              confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              break;
            }
          }

          // DELETE
          if (localData.isNode()) {
            if (incomeData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
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
          if (localData.isNode()) {
            if (incomeData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.add(localData.getQPath());
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
                  confilictResolver.add(localData.getQPath());
                }
              }
            }
          } else {
            if (!incomeData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.add(localData.getQPath());
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
                  confilictResolver.add(localData.getQPath());
                }
              }
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState st : updateSeq) {
              if (incomeData.getQPath().isDescendantOf(st.getData().getQPath())) {
                confilictResolver.add(st.getData().getQPath());
              }
            }
            break;
          }

          // RENAMED sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocNodePath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            if (incomeData.getQPath().isDescendantOf(locNodePath)
                || incomeData.getQPath().equals(locNodePath)
                || incomeData.getQPath().isDescendantOf(nextLocNodePath)
                || incomeData.getQPath().equals(nextLocNodePath)
                || nextLocNodePath.isDescendantOf(incomeData.getQPath())
                || nextLocNodePath.equals(incomeData.getQPath())) {
              confilictResolver.addAll(local.getUniquePathesByUUID(localData.isNode()
                  ? localData.getIdentifier()
                  : localData.getParentIdentifier()));
            }
            break;
          }

          // Simple DELETE
          if (localData.isNode()) {
            if (incomeData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.add(localData.getQPath());
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                confilictResolver.add(localData.getQPath());
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
  }

}
