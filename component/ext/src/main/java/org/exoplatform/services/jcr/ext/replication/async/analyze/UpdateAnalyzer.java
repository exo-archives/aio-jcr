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

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.resolve.ConflictResolver;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: UpdateAnalyzer.java 111 2008-11-11 11:11:11Z $
 */
public class UpdateAnalyzer extends AbstractAnalyzer {

  /**
   * UpdateAnalyzer constructor.
   * 
   * @param localPriority
   */
  public UpdateAnalyzer(boolean localPriority,
                        DataManager dataManager,
                        NodeTypeDataManager ntManager) {
    super(localPriority, dataManager, ntManager);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyze(ItemState incomeChange,
                      ChangesStorage<ItemState> local,
                      ChangesStorage<ItemState> income,
                      ConflictResolver confilictResolver) throws IOException,
                                                         ClassCastException,
                                                         ClassNotFoundException {
    List<ItemState> incUpdateSeq = income.getUpdateSequence(incomeChange);

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

      // skip root node
      if (localData.getIdentifier().equals(Constants.ROOT_UUID)) {
        continue;
      }

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.isNode()) {
            for (ItemState st : incUpdateSeq) {
              if (localData.getQPath().isDescendantOf(st.getData().getQPath())) {
                confilictResolver.add(st.getData().getQPath());
                confilictResolver.addSkippedVSChanges(st.getData().getIdentifier());
              }
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocNodePath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (item.getData().getQPath().isDescendantOf(locNodePath)
                    || item.getData().getQPath().equals(locNodePath)
                    || locNodePath.isDescendantOf(item.getData().getQPath())
                    || nextLocNodePath.isDescendantOf(item.getData().getQPath())) {
                  confilictResolver.add(item.getData().getQPath());
                  confilictResolver.addSkippedVSChanges(item.getData().getIdentifier());
                }
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(locNodePath)) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            }

            break;
          }

          // UPDATE
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> locUpdateSeq = local.getUpdateSequence(localState);
            if (incomeData.isNode()) {
              for (ItemState locSt : locUpdateSeq) {
                for (ItemState incSt : incUpdateSeq) {
                  if (locSt.getData().getQPath().isDescendantOf(incSt.getData().getQPath())
                      || locSt.getData().getQPath().equals(incSt.getData().getQPath())
                      || incSt.getData().getQPath().isDescendantOf(locSt.getData().getQPath())) {
                    confilictResolver.add(incSt.getData().getQPath());
                    confilictResolver.addSkippedVSChanges(incSt.getData().getIdentifier());
                  }
                }
              }
            } else {
              for (ItemState locSt : locUpdateSeq) {
                if (incomeData.getQPath().isDescendantOf(locSt.getData().getQPath())) {
                  confilictResolver.add(incomeData.getQPath());
                  confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
                }
              }
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (item.getData().getQPath().isDescendantOf(localData.getQPath())
                    || item.getData().getQPath().equals(localData.getQPath())
                    || localData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  confilictResolver.add(item.getData().getQPath());
                  confilictResolver.addSkippedVSChanges(item.getData().getIdentifier());
                }
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            }
          } else {
            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (localData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  confilictResolver.add(item.getData().getQPath());
                  confilictResolver.addSkippedVSChanges(item.getData().getIdentifier());
                }
              }
            } else {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            }
          }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (localData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  confilictResolver.add(item.getData().getQPath());
                  confilictResolver.addSkippedVSChanges(item.getData().getIdentifier());
                }
              }
            } else {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.isNode()) {
            for (ItemState st : incUpdateSeq) {
              if (localData.getQPath().isDescendantOf(st.getData().getQPath())
                  || localData.getQPath().equals(st.getData().getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            }
          }
          break;
        }

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.isNode()) {
            for (ItemState st : incUpdateSeq) {
              if (localData.getQPath().isDescendantOf(st.getData().getQPath())) {
                confilictResolver.add(localData.getQPath());
                break;
              }
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> locUpdateSeq = local.getUpdateSequence(localState);
            if (incomeData.isNode()) {
              for (ItemState locSt : locUpdateSeq) {
                for (ItemState incSt : incUpdateSeq) {
                  if (locSt.getData().getQPath().isDescendantOf(incSt.getData().getQPath())
                      || locSt.getData().getQPath().equals(incSt.getData().getQPath())
                      || incSt.getData().getQPath().isDescendantOf(locSt.getData().getQPath())) {
                    confilictResolver.add(localData.getQPath());
                  }
                }
              }
            } else {
              for (ItemState locSt : locUpdateSeq) {
                if (incomeData.getQPath().isDescendantOf(locSt.getData().getQPath()))
                  confilictResolver.add(locSt.getData().getQPath());
              }
            }
            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            QPath locNodePath = localData.isNode()
                ? localData.getQPath()
                : localData.getQPath().makeParentPath();

            QPath nextLocNodePath = localData.isNode()
                ? nextLocalState.getData().getQPath()
                : nextLocalState.getData().getQPath().makeParentPath();

            if (incomeData.isNode()) {
              for (ItemState st : incUpdateSeq) {
                if (st.getData().getQPath().isDescendantOf(locNodePath)
                    || st.getData().getQPath().equals(locNodePath)
                    || locNodePath.isDescendantOf(st.getData().getQPath())
                    || nextLocNodePath.isDescendantOf(st.getData().getQPath())) {
                  confilictResolver.addAll(local.getUniquePathesByUUID(localData.isNode()
                      ? localData.getIdentifier()
                      : localData.getParentIdentifier()));
                }
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(locNodePath)) {
                confilictResolver.addAll(local.getUniquePathesByUUID(localData.isNode()
                    ? localData.getIdentifier()
                    : localData.getParentIdentifier()));
              }
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (item.getData().getQPath().equals(localData.getQPath())
                    || localData.getQPath().isDescendantOf(item.getData().getQPath())
                    || item.getData().getQPath().isDescendantOf(localData.getQPath())) {
                  confilictResolver.add(localData.getQPath());
                }
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath()))
                confilictResolver.add(localData.getQPath());
            }
          } else {
            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (localData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  confilictResolver.add(localData.getQPath());
                }
              }
            } else {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                confilictResolver.add(localData.getQPath());
              }
            }
          }
          break;

        case ItemState.UPDATED:
          if (!localData.isNode()) {
            if (incomeData.isNode()) {
              for (ItemState item : incUpdateSeq) {
                if (localData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  confilictResolver.add(localData.getQPath());
                }
              }
            }
          } else {
            if (incomeData.getQPath().equals(localData.getQPath())) {
              confilictResolver.add(localData.getQPath());
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.isNode()) {
            for (ItemState st : incUpdateSeq) {
              if (localData.getQPath().isDescendantOf(st.getData().getQPath())
                  || localData.getQPath().equals(st.getData().getQPath())) {
                confilictResolver.add(localData.getQPath());
              }
            }
          }
          break;
        }
      }
    }
  }
}
