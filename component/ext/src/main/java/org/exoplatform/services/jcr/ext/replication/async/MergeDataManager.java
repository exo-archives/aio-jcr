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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.DeleteMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.RenameMerger;
import org.exoplatform.services.jcr.ext.replication.async.merge.UpdateMerger;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
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

  protected final WorkspaceSynchronizer        workspace;

  protected final RemoteExporter               exporter;

  protected final DataManager                  dataManager;

  protected final NodeTypeDataManager          ntManager;

  private final int                            localPriority;

  /**
   * Log.
   */
  protected static Log                         log       = ExoLogger.getLogger("jcr.MergerDataManager");

  /**
   * Listeners in order of addition.
   */
  protected final Set<SynchronizationListener> listeners = new LinkedHashSet<SynchronizationListener>();

  /**
   * Current merge worker or null (ready for a new merge).
   */
  protected MergeWorker                        currentMerge;

  class MergeWorker extends Thread {

    private final Iterator<ChangesStorage> membersChanges;

    MergeWorker(Iterator<ChangesStorage> membersChanges) {
      this.membersChanges = membersChanges;
    }

    /**
     * Runs merge till not interrupted or finished.
     * 
     */
    private void doMerge() {

      ChangesStorage synchronizedChanges = null;
      ChangesStorage currentChanges = membersChanges.next();
      ChangesStorage localChanges;
      ChangesStorage incomeChanges;
      while (membersChanges.hasNext() && !isInterrupted()) {
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
        while (changes.hasNext() && !isInterrupted()) {
          ItemState incomeChange = changes.next();

          switch (incomeChange.getState()) {
          case ItemState.ADDED:
            try {
              addMerger.merge(incomeChange, incomeChanges, localChanges);
            } catch (Exception e) {
              e.printStackTrace();
            }
            break;
          case ItemState.DELETED:
            if (incomeChange.isPersisted()) {
              try {
                deleteMerger.merge(incomeChange, incomeChanges, localChanges);
              } catch (Exception e) {
                e.printStackTrace();
              }
            } else {
              ItemState nextIncomeChange = incomeChanges.getNextItemState(incomeChange);
              if (nextIncomeChange != null && nextIncomeChange.getState() == ItemState.RENAMED) {
                try {
                  renameMerger.merge(incomeChange, incomeChanges, localChanges);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              } else if (nextIncomeChange != null
                  && nextIncomeChange.getState() == ItemState.UPDATED) {
                try {
                  // incomeChange.getData().getParentIdentifier();
                  udpateMerger.merge(incomeChange, incomeChanges, localChanges);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              } else {
                log.error("Unknown DELETE sequence");
              }
            }
            break;
          case ItemState.UPDATED:
            if (!incomeChange.getData().isNode()) {
              try {
                udpateMerger.merge(incomeChange, incomeChanges, localChanges);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          case ItemState.MIXIN_CHANGED:
            break;
          }
        }

        currentChanges = synchronizedChanges;
      }

      if (!isInterrupted()) { // if success
        workspace.save(synchronizedChanges);
        for (SynchronizationListener syncl : listeners)
          // inform all interested
          syncl.onDone(null); // TODO local done - null
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      doMerge();
    }

  }

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
   */
  public void merge(Iterator<ChangesStorage> membersChanges) {
    currentMerge = new MergeWorker(membersChanges);
    currentMerge.start();
  }

  public void addSynchronizationListener(SynchronizationListener listener) {
    listeners.add(listener);
  }

  public void removeSynchronizationListener(SynchronizationListener listener) {
    listeners.remove(listener);
  }

}
