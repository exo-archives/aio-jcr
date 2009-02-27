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
 * @version $Id: DeleteAnaylez.java 111 2008-11-11 11:11:11Z $
 */
public class DeleteAnalyzer extends AbstractAnalyzer {

  public DeleteAnalyzer(boolean localPriority) {
    super(localPriority);
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
          if (incomeData.isNode()) {
            if (localData.getQPath().isDescendantOf(incomeData.getQPath())) {
              confilictResolver.add(localData.getQPath());
            }
          } else {
            if (!localData.isNode()) {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                confilictResolver.add(localData.getQPath());
              }
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            if (incomeData.isNode()) {

              List<ItemState> updateSeq = local.getUpdateSequence(localState);
              for (ItemState item : updateSeq) {
                if (item.getData().getQPath().isDescendantOf(incomeData.getQPath())
                    || incomeData.getQPath().equals(item.getData().getQPath())
                    || incomeData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  confilictResolver.add(item.getData().getQPath());
                }
              }
            } else {
              List<ItemState> updateSeq = local.getUpdateSequence(localState);
              for (ItemState item : updateSeq) {
                if (incomeData.getQPath().isDescendantOf(item.getData().getQPath())) {
                  confilictResolver.add(item.getData().getQPath());
                }
              }
            }
            break;
          }

          // RENAMED sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {

            if (incomeData.isNode()) {
              QPath locNodePath = localData.isNode()
                  ? localData.getQPath()
                  : local.findNextState(localState,
                                        localData.getParentIdentifier(),
                                        localData.getQPath().makeParentPath(),
                                        ItemState.DELETED).getData().getQPath();

              QPath nextLocNodePath = localData.isNode()
                  ? nextLocalState.getData().getQPath()
                  : local.findNextState(localState,
                                        nextLocalState.getData().getParentIdentifier(),
                                        nextLocalState.getData().getQPath().makeParentPath(),
                                        ItemState.RENAMED).getData().getQPath();

              if (incomeData.getQPath().isDescendantOf(locNodePath)
                  || incomeData.getQPath().equals(locNodePath)
                  || nextLocNodePath.isDescendantOf(incomeData.getQPath())) {
                confilictResolver.addAll(local.getUniquePathesByUUID(localData.getIdentifier()));
              }
            } else {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                confilictResolver.addAll(local.getUniquePathesByUUID(localData.getIdentifier()));
              }
            }
            break;
          }

          // DELETE
          if (incomeData.isNode()) {
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || incomeData.getQPath().equals(localData.getQPath())) {
              confilictResolver.add(localData.getQPath());
            }
          } else {
            if (localData.isNode()) {
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
          if (!localData.isNode()) {
            if (incomeData.isNode()) {
              if (localData.getQPath().isDescendantOf(incomeData.getQPath())) {
                confilictResolver.add(localData.getQPath());
              }
            } else {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                confilictResolver.add(localData.getQPath());
              }
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.isNode()) {
            if (localData.getQPath().equals(incomeData.getQPath())
                || localData.getQPath().isDescendantOf(incomeData.getQPath())) {
              confilictResolver.add(localData.getQPath());
            }
          } else {
            List<ItemState> mixinSeq = local.getMixinSequence(localState);

            for (int i = 0; i < mixinSeq.size(); i++) {
              ItemState item = mixinSeq.get(i);
              if (!item.getData().isNode()) {
                if (item.getData().getQPath().equals(incomeData.getQPath())) {
                  confilictResolver.add(localData.getQPath());
                  break;
                }
              }
            }
          }
          break;
        }

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.isNode()) {
            if (localData.getQPath().isDescendantOf(incomeData.getQPath())
                || localData.getQPath().equals(incomeData.getQPath())) {
              confilictResolver.add(localData.getQPath());
            }
          }
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState st : updateSeq) {
              if (incomeData.getQPath().isDescendantOf(st.getData().getQPath())
                  || incomeData.getQPath().equals(st.getData().getQPath())) {
                confilictResolver.add(st.getData().getQPath());
              }
            }
            break;
          }

          // RENAMED sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (incomeData.isNode()) {
              QPath locNodePath = localData.isNode()
                  ? localData.getQPath()
                  : local.findNextState(localState,
                                        localData.getParentIdentifier(),
                                        localData.getQPath().makeParentPath(),
                                        ItemState.DELETED).getData().getQPath();

              QPath nextLocNodePath = localData.isNode()
                  ? nextLocalState.getData().getQPath()
                  : local.findNextState(localState,
                                        nextLocalState.getData().getParentIdentifier(),
                                        nextLocalState.getData().getQPath().makeParentPath(),
                                        ItemState.RENAMED).getData().getQPath();

              if (incomeData.getQPath().isDescendantOf(locNodePath)
                  || incomeData.getQPath().equals(locNodePath)
                  || nextLocNodePath.isDescendantOf(incomeData.getQPath())) {
                confilictResolver.addAll(local.getUniquePathesByUUID(localData.getIdentifier()));
              }
            } else {
              if (localData.isNode()) {
                if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                  confilictResolver.add(localData.getQPath());
                }
              } else {
                if (incomeData.getQPath().equals(localData.getQPath())) {
                  confilictResolver.add(localData.getQPath());
                }
              }
            }
            break;
          }

          // DELETE
          if (incomeData.isNode()) {
            if (localData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                  || incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.remove(localData.getQPath());
              }
            }
          } else {
            if (localData.isNode()) {
              if (incomeData.getQPath().isDescendantOf(localData.getQPath())) {
                confilictResolver.remove(localData.getQPath());
              }
            } else {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.remove(localData.getQPath());
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
