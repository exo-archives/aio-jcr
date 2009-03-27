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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.analyze.AddAnalyzer;
import org.exoplatform.services.jcr.ext.replication.async.analyze.DeleteAnalyzer;
import org.exoplatform.services.jcr.ext.replication.async.analyze.MixinAnalyzer;
import org.exoplatform.services.jcr.ext.replication.async.analyze.RenameAnalyzer;
import org.exoplatform.services.jcr.ext.replication.async.analyze.UpdateAnalyzer;
import org.exoplatform.services.jcr.ext.replication.async.resolve.ConflictResolver;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.CompositeItemStatesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.MemberChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.StorageRuntimeException;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id$
 */
public class MergeDataManager extends AbstractMergeManager {

  protected final FileCleaner fileCleaner;
  protected final int maxBufferSize;
  
  MergeDataManager(RemoteExporter exporter,
                   DataManager dataManager,
                   NodeTypeDataManager ntManager,
                   String storageDir, FileCleaner fileCleaner, int maxBufferSize) {
    super(exporter, dataManager, ntManager, storageDir);
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
  }

  public ChangesStorage<ItemState> merge(Iterator<MemberChangesStorage<ItemState>> membersChanges) throws RepositoryException,
                                                                                                  RemoteExportException,
                                                                                                  IOException,
                                                                                                  ClassCastException,
                                                                                                  ClassNotFoundException,
                                                                                                  MergeDataManagerException,
                                                                                                  StorageRuntimeException {
    try {
      MemberChangesStorage<ItemState> first = membersChanges.next();

      EditableChangesStorage<ItemState> accumulated = new CompositeItemStatesStorage<ItemState>(makePath("accumulated-"
                                                                                                    + first.getMember()
                                                                                                           .getPriority()),
                                                                                                first.getMember(),
                                                                                                resHolder, fileCleaner, maxBufferSize);

      EditableChangesStorage<ItemState> result = new CompositeItemStatesStorage<ItemState>(makePath("result"),
                                                                                           localMember,
                                                                                           resHolder, fileCleaner, maxBufferSize);

      MemberChangesStorage<ItemState> local;
      MemberChangesStorage<ItemState> income;

      // prepare
      if (localMember.getPriority() == first.getMember().getPriority())
        accumulated.addAll(first);

      while (membersChanges.hasNext() && run) {
        MemberChangesStorage<ItemState> second = membersChanges.next();

        boolean isLocalPriority = localMember.getPriority() >= second.getMember().getPriority();
        if (isLocalPriority) {
          income = first;
          local = second;
        } else {
          income = second;
          local = first;
        }

        LOG.info("Merge changes (local=" + isLocalPriority + ") from "
            + first.getMember().getPriority() + " (" + first.getMember().getAddress() + ") and "
            + second.getMember().getPriority() + " (" + second.getMember().getAddress()
            + ") members");

        EditableChangesStorage<ItemState> iteration = new CompositeItemStatesStorage<ItemState>(makePath(first.getMember(),
                                                                                                         second.getMember()),
                                                                                                second.getMember(),
                                                                                                resHolder, fileCleaner, maxBufferSize);

        exporter.setRemoteMember(second.getMember().getAddress());

        ConflictResolver conflictResolver = new ConflictResolver(isLocalPriority,
                                                                 local,
                                                                 income,
                                                                 exporter,
                                                                 dataManager,
                                                                 ntManager);

        AddAnalyzer addAnalyzer = new AddAnalyzer(isLocalPriority, dataManager, ntManager);
        RenameAnalyzer renameAnalyzer = new RenameAnalyzer(isLocalPriority, dataManager, ntManager);
        UpdateAnalyzer updateAnalyzer = new UpdateAnalyzer(isLocalPriority, dataManager, ntManager);
        MixinAnalyzer mixinAnalyzer = new MixinAnalyzer(isLocalPriority, dataManager, ntManager);
        DeleteAnalyzer deleteAnalyzer = new DeleteAnalyzer(isLocalPriority, dataManager, ntManager);

        if (run) {
          Iterator<ItemState> changes = income.getChanges();
          if (changes.hasNext()) {
            while (changes.hasNext() && run) {
              ItemState incomeChange = changes.next();

              if (LOG.isDebugEnabled())
                LOG.debug("\t\tAnalyzing income item "
                    + ItemState.nameFromValue(incomeChange.getState()) + " "
                    + incomeChange.getData().getQPath().getAsString());

              // skip lock properties
              if (!incomeChange.getData().isNode()) {
                if (incomeChange.getData().getQPath().getName().equals(Constants.JCR_LOCKISDEEP)
                    || incomeChange.getData().getQPath().getName().equals(Constants.JCR_LOCKOWNER)) {
                  continue;
                }
              }

              switch (incomeChange.getState()) {
              case ItemState.ADDED:
                addAnalyzer.analyze(incomeChange, local, income, conflictResolver);
                break;
              case ItemState.DELETED:
                if (incomeChange.isPersisted()) { // DELETE
                  deleteAnalyzer.analyze(incomeChange, local, income, conflictResolver);
                } else {
                  ItemState nextIncomeChange = income.findNextState(incomeChange,
                                                                    incomeChange.getData()
                                                                                .getIdentifier());

                  if (nextIncomeChange != null && nextIncomeChange.getState() == ItemState.RENAMED) { // RENAME
                    renameAnalyzer.analyze(incomeChange, local, income, conflictResolver);

                  } else if (nextIncomeChange != null
                      && nextIncomeChange.getState() == ItemState.UPDATED) { // UPDATE node
                    updateAnalyzer.analyze(incomeChange, local, income, conflictResolver);

                  } else {
                    if (LOG.isDebugEnabled())
                      LOG.debug("Income changes log: " + income.dump());
                    if (LOG.isDebugEnabled())
                      LOG.debug("Local changes log: " + local.dump());

                    throw new MergeDataManagerException("Can not resolve merge. Unknown DELETE sequence."
                        + "[path="
                        + incomeChange.getData().getQPath().getAsString()
                        + "][identifier="
                        + incomeChange.getData().getIdentifier()
                        + "][parentIdentifier="
                        + incomeChange.getData().getParentIdentifier()
                        + "]");
                  }
                }
                break;
              case ItemState.UPDATED:
                if (!incomeChange.getData().isNode()) { // UPDATE property
                  updateAnalyzer.analyze(incomeChange, local, income, conflictResolver);
                }
                break;
              case ItemState.MIXIN_CHANGED:
                mixinAnalyzer.analyze(incomeChange, local, income, conflictResolver);
                break;
              }
            }

            conflictResolver.restore(iteration);
            conflictResolver.applyIncomeChanges(iteration);
          }

          // add changes to resulted changes and prepare changes for next merge iteration
          if (isLocalPriority) {
            accumulated.delete();
            accumulated = new CompositeItemStatesStorage<ItemState>(makePath("accumulated-"
                + second.getMember().getPriority()), second.getMember(), resHolder, fileCleaner, maxBufferSize);

            accumulated.addAll(second);
            accumulated.addAll(iteration);
            if (localMember.getPriority() == second.getMember().getPriority())
              result.addAll(iteration);

          } else {
            accumulated.addAll(iteration);
            result.addAll(iteration);
          }

          first = accumulated;
        }
      }

      // if success
      return result;

    } finally {
      run = true;
    }
  }
}
