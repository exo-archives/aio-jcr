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

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger;

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

  MergeDataManager(WorkspaceSynchronizer synchronizer,
                   AsyncTransmitter transmitter,
                   DataManager dataManager,
                   NodeTypeDataManager ntManager) {
    this.synchronizer = synchronizer;

    this.exporter = new RemoteExporterImpl(transmitter, /*TODO*/ 100);

    this.dataManager = dataManager;
    
    this.ntManager = ntManager;

    this.addMerger = new AddMerger(synchronizer.getLocalPriority(), exporter, dataManager, ntManager);
  }

  /**
   * Start merge process.
   * 
   * @param incomeChanges
   *          TransactionChangesLog
   */
  public void merge(TransactionChangesLog incomeChanges) {

    for (ItemState change : incomeChanges.getAllStates()) {

    }
  }

}
