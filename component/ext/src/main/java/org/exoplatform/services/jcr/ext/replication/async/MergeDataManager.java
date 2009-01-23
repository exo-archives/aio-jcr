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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.DeleteMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.MixinMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.RenameMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.UpdateMerger;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogReadException;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableItemStatesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/> Merge manager per Workspace.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class MergeDataManager {

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  protected final int                 localPriority;

  /**
   * Flag allowing run of merge.
   */
  private volatile boolean            run = true;

  private final String                storageDir;

  /**
   * Log.
   */
  protected static Log                log = ExoLogger.getLogger("jcr.MergerDataManager");

  MergeDataManager(RemoteExporter exporter,
                   DataManager dataManager,
                   NodeTypeDataManager ntManager,
                   int localPriority,
                   String storageDir) {

    this.exporter = exporter;

    this.dataManager = dataManager;

    this.ntManager = ntManager;

    this.localPriority = localPriority;

    this.storageDir = storageDir;
  }

  private File makePath(Member first, Member second) {
    File dir = new File(storageDir, first.getPriority() + "-" + second.getPriority());
    dir.mkdirs();
    return dir;
  }

  /**
   * Start merge process.
   * 
   * @param membersChanges
   *          List of members changes
   * @throws RepositoryException
   * @throws RemoteExportException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws ClassCastException
   * @throws MergeDataManagerException
   */
  public ChangesStorage<ItemState> merge(Iterator<ChangesStorage<ItemState>> membersChanges) throws RepositoryException,
                                                                                            RemoteExportException,
                                                                                            IOException,
                                                                                            ClassCastException,
                                                                                            ClassNotFoundException,
                                                                                            MergeDataManagerException,
                                                                                            ChangesLogReadException {

    try {

      EditableChangesStorage<ItemState> synchronizedChanges = null;

      ChangesStorage<ItemState> local;
      ChangesStorage<ItemState> income;

      ChangesStorage<ItemState> first = membersChanges.next();

      while (membersChanges.hasNext() && run) {
        ChangesStorage<ItemState> second = membersChanges.next();

        synchronizedChanges = new EditableItemStatesStorage<ItemState>(makePath(first.getMember(),
                                                                                second.getMember()));

        List<QPath> skippedList = new ArrayList<QPath>();

        boolean isLocalPriority = localPriority >= second.getMember().getPriority();
        if (isLocalPriority) {
          income = first;
          local = second;
        } else {
          income = second;
          local = first;
        }

        exporter.setMember(second.getMember());
        // TODO NT reregistration

        AddMerger addMerger = new AddMerger(isLocalPriority, exporter, dataManager, ntManager);
        DeleteMerger deleteMerger = new DeleteMerger(isLocalPriority,
                                                     exporter,
                                                     dataManager,
                                                     ntManager);
        UpdateMerger udpateMerger = new UpdateMerger(isLocalPriority,
                                                     exporter,
                                                     dataManager,
                                                     ntManager);
        RenameMerger renameMerger = new RenameMerger(isLocalPriority,
                                                     exporter,
                                                     dataManager,
                                                     ntManager);
        MixinMerger mixinMerger = new MixinMerger(isLocalPriority, exporter, dataManager, ntManager);

        outer: for (Iterator<ItemState> changes = income.getChanges(); changes.hasNext() && run;) {
          ItemState incomeChange = changes.next();

          // skip already processed itemstate
          if (synchronizedChanges.hasState(incomeChange)) {
            continue;
          }

          // skip subtree changes
          for (int i = 0; i < skippedList.size(); i++) {
            if (incomeChange.getData().getQPath().equals(skippedList.get(i))
                || incomeChange.getData().getQPath().isDescendantOf(skippedList.get(i))) {
              continue outer;
            }
          }

          // skip lock properties
          if (!incomeChange.getData().isNode()) {
            if (incomeChange.getData().getQPath().getName().equals(Constants.JCR_LOCKISDEEP)
                || incomeChange.getData().getQPath().getName().equals(Constants.JCR_LOCKOWNER)) {
              continue;
            }
          }

          // TODO for move locked node need to move LOCKISDEEP and LOCKOWNER properties
          switch (incomeChange.getState()) {
          case ItemState.ADDED:
            synchronizedChanges.addAll(addMerger.merge(incomeChange,
                                                       income,
                                                       local,
                                                       storageDir,
                                                       skippedList));
            break;
          case ItemState.DELETED:
            // DELETE
            if (incomeChange.isPersisted()) {
              synchronizedChanges.addAll(deleteMerger.merge(incomeChange,
                                                            income,
                                                            local,
                                                            storageDir,
                                                            skippedList));
            } else {
              ItemState nextIncomeChange = income.findNextState(incomeChange,
                                                                incomeChange.getData()
                                                                            .getIdentifier());

              // RENAME
              if (nextIncomeChange != null && nextIncomeChange.getState() == ItemState.RENAMED) {

                // skip processed itemstates
                if (synchronizedChanges.hasState(nextIncomeChange.getData().getIdentifier(),
                                                 nextIncomeChange.getData().getQPath(),
                                                 ItemState.ADDED)) {
                  continue;
                }

                synchronizedChanges.addAll(renameMerger.merge(incomeChange,
                                                              income,
                                                              local,
                                                              storageDir,
                                                              skippedList));

                // UPDATE node
              } else if (nextIncomeChange != null
                  && nextIncomeChange.getState() == ItemState.UPDATED) {
                synchronizedChanges.addAll(udpateMerger.merge(incomeChange,
                                                              income,
                                                              local,
                                                              storageDir,
                                                              skippedList));
              } else {
                log.info("Income changes log: " + income.dump());
                log.info("Local changes log: " + local.dump());

                throw new MergeDataManagerException("Can not resolve merge. Unknown DELETE sequence."
                    + "[path="
                    + incomeChange.getData().getQPath().getAsString()
                    + "][identifier="
                    + incomeChange.getData().getIdentifier()
                    + "][parentIdentifier="
                    + incomeChange.getData().getParentIdentifier() + "]");
              }
            }
            break;
          case ItemState.UPDATED:
            // UPDATE property
            if (!incomeChange.getData().isNode()) {
              synchronizedChanges.addAll(udpateMerger.merge(incomeChange,
                                                            income,
                                                            local,
                                                            storageDir,
                                                            skippedList));
            }
            break;
          case ItemState.MIXIN_CHANGED:
            synchronizedChanges.addAll(mixinMerger.merge(incomeChange,
                                                         income,
                                                         local,
                                                         storageDir,
                                                         skippedList));
            break;
          }
        }

        first = synchronizedChanges;
      }

      // if success
      return synchronizedChanges;

    } finally {
      run = true;
    }
  }

  /**
   * Cancel current merge process.
   * 
   * @throws RepositoryException
   * @throws RemoteExportException
   */
  public void cancel() throws RepositoryException, RemoteExportException {
    run = false;
  }
}
