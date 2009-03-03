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
import org.exoplatform.services.jcr.ext.replication.async.resolve.ConflictResolver;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: MixinAnayler.java 111 2008-11-11 11:11:11Z $
 */
public class MixinAnalyzer extends AbstractAnalyzer {

  /**
   * MixinAnayler constructor.
   * 
   * @param localPriority
   */
  public MixinAnalyzer(boolean localPriority) {
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

      ItemData localData = localState.getData();
      ItemData incomeData = incomeChange.getData();

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
          break;

        case ItemState.DELETED:
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE node
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState item : updateSeq) {
              if (incomeData.getQPath().equals(item.getData().getQPath())
                  || incomeData.getQPath().isDescendantOf(item.getData().getQPath())) {
                confilictResolver.add(incomeData.getQPath());
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
                break;
              }
            }
            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (localData.isNode()) {
              if (incomeData.getQPath().equals(localData.getQPath())) {
                confilictResolver.addAll(income.getUniquePathesByUUID(incomeData.getIdentifier()));
                confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
              }
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())) {
              confilictResolver.add(incomeData.getQPath());
              confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
            }
          }
          break;

        case ItemState.UPDATED:
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.getQPath().equals(localData.getQPath())) {
            confilictResolver.add(incomeData.getQPath());
            confilictResolver.addSkippedVSChanges(incomeData.getIdentifier());
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
          ItemState nextLocalState = local.findNextState(localState, localData.getIdentifier());

          // UPDATE node
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            List<ItemState> updateSeq = local.getUpdateSequence(localState);
            for (ItemState st : updateSeq) {
              if (incomeData.getQPath().isDescendantOf(st.getData().getQPath())
                  || incomeData.getQPath().equals(st.getData().getQPath())) {
                confilictResolver.add(localData.getQPath());
              }
            }

            break;
          }

          // RENAME
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (incomeData.isNode()) {
              if (localData.getQPath().equals(incomeData.getQPath())) {
                confilictResolver.addAll(local.getUniquePathesByUUID(localData.getIdentifier()));
              }
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())
                || incomeData.getQPath().isDescendantOf(localData.getQPath())) {
              confilictResolver.add(localData.getQPath());
            }
          }
          break;

        case ItemState.RENAMED:
          break;

        case ItemState.MIXIN_CHANGED:
          if (incomeData.getQPath().equals(localData.getQPath())) {
            confilictResolver.add(localData.getQPath());
          }
          break;
        }
      }
    }
  }

}
