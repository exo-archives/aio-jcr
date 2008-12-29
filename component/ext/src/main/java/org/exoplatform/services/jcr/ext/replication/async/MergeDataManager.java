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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.DeleteMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.RenameMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.UpdateMerger;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
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

  protected final WorkspaceSynchronizer workspace;

  protected final RemoteExporter        exporter;

  protected final DataManager           dataManager;

  protected final NodeTypeDataManager   ntManager;

  protected final int                   localPriority;

  /**
   * Flag allowing run of merge.
   */
  private volatile boolean              run = true;

  /**
   * Log.
   */
  protected static Log                  log = ExoLogger.getLogger("jcr.MergerDataManager");

  MergeDataManager(WorkspaceSynchronizer workspace,
                   RemoteExporter exporter,
                   DataManager dataManager,
                   NodeTypeDataManager ntManager,
                   int localPriority) {

    this.workspace = workspace;

    this.exporter = exporter;

    this.dataManager = dataManager;

    this.ntManager = ntManager;

    this.localPriority = localPriority;
  }

  /**
   * Start merge process.
   * 
   * @param incomeChanges
   *          TransactionChangesLog
   * @throws RemoteExportException
   * @throws RepositoryException
   * @throws IOException 
   */
  public void merge(List<ChangesStorage> membersChanges) throws RepositoryException,
                                                        RemoteExportException, IOException {

    try {
      // add local changes to list
      if (membersChanges.get(membersChanges.size() - 1).getMember().getPriority() < localPriority) {
        membersChanges.add(workspace.getLocalChanges());
      } else {
        for (int i = 0; i < membersChanges.size(); i++) {
          if (membersChanges.get(i).getMember().getPriority() > localPriority) {
            membersChanges.add(i, workspace.getLocalChanges());
            break;
          }
        }
      }

      doMerge(membersChanges.iterator());
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

  /**
   * Runs merge till not interrupted or finished.
   * 
   * @throws RemoteExportException
   * @throws RepositoryException
   * @throws IOException 
   */
  private void doMerge(Iterator<ChangesStorage> membersChanges) throws RepositoryException,
                                                               RemoteExportException, IOException {

    EditableChangesStorage synchronizedChanges = null;
    
    try {
    ChangesStorage currentChanges = membersChanges.next();
    ChangesStorage localChanges;
    ChangesStorage incomeChanges;
    while (membersChanges.hasNext() && run) {
      ChangesStorage nextChanges = membersChanges.next();

      boolean isLocalPriority = localPriority >= nextChanges.getMember().getPriority();
      if (isLocalPriority) {
        incomeChanges = currentChanges;
        localChanges = nextChanges;
      } else {
        incomeChanges = nextChanges;
        localChanges = currentChanges;
      }

      exporter.setMember(nextChanges.getMember());
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

      Iterator<ItemState> changes = incomeChanges.getChanges();
      while (changes.hasNext() && run) {
        ItemState incomeChange = changes.next();

        switch (incomeChange.getState()) {
        case ItemState.ADDED:
          addMerger.merge(incomeChange, incomeChanges, localChanges);
          break;
        case ItemState.DELETED:
          // DELETE
          if (incomeChange.isPersisted()) {
            deleteMerger.merge(incomeChange, incomeChanges, localChanges);
          } else {
            ItemState nextIncomeChange = incomeChanges.getNextItemState(incomeChange);

            // RENAME
            if (nextIncomeChange != null && nextIncomeChange.getState() == ItemState.RENAMED) {
              renameMerger.merge(incomeChange, incomeChanges, localChanges);
              // UPDATE
            } else if (nextIncomeChange != null && nextIncomeChange.getState() == ItemState.UPDATED) {
              udpateMerger.merge(incomeChange, incomeChanges, localChanges);
            } else {
              log.error("Unknown DELETE sequence");
            }
          }
          break;
        case ItemState.UPDATED:
          if (!incomeChange.getData().isNode()) {
            udpateMerger.merge(incomeChange, incomeChanges, localChanges);
          }
        case ItemState.MIXIN_CHANGED:
          break;
        }
      }

      currentChanges = synchronizedChanges;
    }

    // if success
    workspace.save(synchronizedChanges);
    
    } finally {
      // clean synchronizedChanges anyway
      synchronizedChanges.delete();
    }
  }
}
