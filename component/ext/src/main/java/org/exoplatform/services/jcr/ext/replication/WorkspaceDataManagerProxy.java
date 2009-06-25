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
package org.exoplatform.services.jcr.ext.replication;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemDataKeeper;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.impl.core.lock.LockManagerImpl;
import org.exoplatform.services.jcr.impl.core.query.SearchManager;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.<br/>
 * Proxy of WorkspaceDataManager for "proxy" mode of replication to let replicator not to make
 * persistent changes but replicate cache, indexes etc instead. This is the case if persistent
 * replication is done with some external way (by repliucation enabled RDB or AS etc)
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class WorkspaceDataManagerProxy implements ItemDataKeeper {

  /**
   * The apache logger.
   */
  private static Log                     log = ExoLogger.getLogger("jcr.WorkspaceDataManagerProxy");

  /**
   * The ItemsPersistenceListeners.
   */
  private List<ItemsPersistenceListener> listeners;

  /**
   * WorkspaceDataManagerProxy constructor.
   * 
   * @param dataManager
   *          the CacheableWorkspaceDataManager
   * @param searchIndex
   *          the SearchManager
   * @param lockManager
   *          the LockManagerImpl
   */
  public WorkspaceDataManagerProxy(CacheableWorkspaceDataManager dataManager,
                                   SearchManager searchIndex,
                                   LockManagerImpl lockManager) {
    this.listeners = new ArrayList<ItemsPersistenceListener>();
    listeners.add(dataManager.getCache());
    if (searchIndex != null)
      listeners.add(searchIndex);
    if (lockManager != null)
      listeners.add(lockManager);
    log.info("WorkspaceDataManagerProxy is instantiated");
  }

  /**
   * calls onSaveItems on all registered listeners.
   * 
   * @param changesLog
   *          the ChangesLog with data
   *
   * @throws InvalidItemStateException
   *           will be generate the exception InvalidItemStateException           
   * @throws UnsupportedOperationException
   *           will be generate the exception UnsupportedOperationException
   * @throws RepositoryException
   *           will be generate the exception RepositoryException
   */
  public void save(ItemStateChangesLog changesLog) throws InvalidItemStateException,
                                                  UnsupportedOperationException,
                                                  RepositoryException {
    for (ItemsPersistenceListener listener : listeners) {
      listener.onSaveItems(changesLog);
    }
    if (log.isDebugEnabled())
      log.debug("ChangesLog sent to " + listeners);
  }
}
