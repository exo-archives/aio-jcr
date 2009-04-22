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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogsIterator;
import org.exoplatform.services.jcr.ext.replication.async.storage.StartChangesLocalStorageImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: ChangesListenerData.java 111 2008-11-11 11:11:11Z $
 */
public class AsyncStartChangesListener implements ItemsPersistenceListener {

  /**
   * The data manager.
   */
  private final PersistentDataManager        dataManager;

  /**
   * The storage.
   */
  private final StartChangesLocalStorageImpl storage;

  /**
   * The storage directory.
   */
  private final File                         storageDir;

  /**
   * Is listener active or not.
   */
  private boolean                            active;

  private boolean                            clear;

  /**
   * ChangesListener constructor.
   * 
   * @param workspaceName
   * @throws NoSuchAlgorithmException
   * @throws IOException
   */
  public AsyncStartChangesListener(PersistentDataManager dataManager,
                                   AsyncReplication asyncReplication,
                                   WorkspaceFileCleanerHolder wfcleaner,
                                   RepositoryEntry rconf,
                                   WorkspaceEntry wconf) throws NoSuchAlgorithmException,
      IOException {
    this.dataManager = dataManager;
    this.dataManager.addItemPersistenceListener(this);
    this.active = true;

    this.storageDir = new File(System.getProperty("java.io.tmpdir"), "startchanges"
        + File.separator + rconf.getName() + "-" + wconf.getName() + File.separator
        + System.currentTimeMillis());

    if (!storageDir.exists())
      storageDir.mkdirs();

    int maxBufferSize = wconf.getContainer()
                             .getParameterInteger(WorkspaceDataContainer.MAXBUFFERSIZE,
                                                  WorkspaceDataContainer.DEF_MAXBUFFERSIZE);

    this.storage = new StartChangesLocalStorageImpl(storageDir.getAbsolutePath(),
                                                    wfcleaner.getFileCleaner(),
                                                    maxBufferSize,
                                                    new ReaderSpoolFileHolder());
    this.storage.onStart(null);

  }

  /**
   * Return all changes from the start.
   * 
   * @return List of changes if present or null if changes was cleared.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public ChangesLogsIterator<TransactionChangesLog> getChanges() throws IOException,
                                                                ClassNotFoundException {
    return storage.getChangesLogs(false);
  }

  /**
   * Make the listener is not active.
   */
  public void stop() {
    active = false;
  }

  /**
   * Clear the accumulated changes and unregister as listener.
   */
  public void cleanup() {
    dataManager.removeItemPersistenceListener(this);
    storage.onStop();
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {
    if (active) {
      storage.onSaveItems(itemStates);
    }
  }
}
