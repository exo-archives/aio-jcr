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

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;

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
  
  protected final RemoteExporter               exporter;

  protected final AddMerger                    addMerger;

  protected final DataManager                  dataManager;

  protected final NodeTypeDataManager          ntManager;

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
      
      while (membersChanges.hasNext() && !isInterrupted()) {
        ChangesStorage member = membersChanges.next();
        Iterator<ItemState> changes = member.getChanges();
        while (changes.hasNext() && !isInterrupted()) {
          ItemState incomeChange = changes.next();
          // TODO merge to synchronizedChanges
        }
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
                   boolean localPriority) {
    
    this.workspace = workspace;
    
    this.exporter = exporter;

    this.dataManager = dataManager;

    this.ntManager = ntManager;

    this.addMerger = new AddMerger(localPriority, exporter, dataManager, ntManager);
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
