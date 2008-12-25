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

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ItemStatesSequence;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Merge manager per Workspace.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class MergeDataManager {

  protected final WorkspaceSynchronizer synchronizer;

  protected final RemoteExporter        exporter;

  protected final AddMerger             addMerger;

  protected final DataManager           dataManager;

  protected final NodeTypeDataManager   ntManager;
  
  /**
   * Current merge worker or null (ready for a new merge).
   */
  protected MergeWorker currentMerge; 

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
      
      
      
      while (membersChanges.hasNext() && !isInterrupted()) {
        ChangesStorage member = membersChanges.next();
        ItemStatesSequence<ItemState> changes = member.getChanges();
        while (changes.hasNext() && !isInterrupted()) {
          ItemState st = changes.next();
          // TODO
        }
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
  
  MergeDataManager(WorkspaceSynchronizer synchronizer,
                   RemoteExporter exporter,
                   AsyncReceiver receiver,
                   DataManager dataManager,
                   NodeTypeDataManager ntManager) {
    this.synchronizer = synchronizer;

    this.exporter = exporter;

    this.dataManager = dataManager;
    
    this.ntManager = ntManager;

    this.addMerger = new AddMerger(synchronizer.isLocalPriority(), exporter, dataManager, ntManager);
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

}
