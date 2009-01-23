/**
 * 
 */
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

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogReadException;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.SynchronizationException;
import org.exoplatform.services.jcr.ext.replication.async.storage.SynchronizerChangesLog;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class WorkspaceSynchronizerImpl implements WorkspaceSynchronizer {

  private static final Log              LOG = ExoLogger.getLogger("ext.WorkspaceSynchronizerImpl");

  protected final LocalStorage          storage;

  protected final PersistentDataManager workspace;

  public WorkspaceSynchronizerImpl(PersistentDataManager workspace, LocalStorage storage) {
    this.storage = storage;
    this.workspace = workspace;
  }

  /**
   * Return local changes.<br/> 1. to a merger<br/> 2. to a receiver
   * 
   * @return ChangesStorage
   */
  public ChangesStorage<ItemState> getLocalChanges() throws IOException {
    return storage.getLocalChanges(null);
  }

  /**
   * {@inheritDoc}
   */
  public void save(ChangesStorage<ItemState> synchronizedChanges) throws SynchronizationException,
                                                                 InvalidItemStateException,
                                                                 UnsupportedOperationException,
                                                                 RepositoryException {
    //dump
    try {
      LOG.info("save \r\n" + synchronizedChanges.dump());
    } catch (ClassCastException e1) {
      LOG.error("Changes dump error " + e1);
    } catch (IOException e1) {
      LOG.error("Changes dump error " + e1);
    } catch (ClassNotFoundException e1) {
      LOG.error("Changes dump error " + e1);
    } catch (ChangesLogReadException e1){
      LOG.error("Changes dump error " + e1);
    }

    OnSynchronizationWorkspaceListenersFilter apiFilter = new OnSynchronizationWorkspaceListenersFilter();
    workspace.addItemPersistenceListenerFilter(apiFilter);

    try {
      workspace.save(new SynchronizerChangesLog(synchronizedChanges));
    } catch (ChangesLogReadException e) {
      throw new SynchronizationException("Error of merge result read on save " + e, e);
    } finally {

      workspace.removeItemPersistenceListenerFilter(apiFilter);
    }
  }

}
