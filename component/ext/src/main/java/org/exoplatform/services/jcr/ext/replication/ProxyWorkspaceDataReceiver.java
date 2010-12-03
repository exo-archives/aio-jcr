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

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.impl.core.lock.LockManagerImpl;
import org.exoplatform.services.jcr.impl.core.query.SearchManager;
import org.exoplatform.services.jcr.impl.core.query.SystemSearchManager;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class ProxyWorkspaceDataReceiver extends AbstractWorkspaceDataReceiver {

  /**
   * ProxyWorkspaceDataReceiver constructor.
   * 
   * @param dataManager
   *          the CacheableWorkspaceDataManager
   * @param lockManager
   *          the LockManagerImpl
   * @throws RepositoryConfigurationException
   *           will be generated RepositoryConfigurationException
   */
  public ProxyWorkspaceDataReceiver(CacheableWorkspaceDataManager dataManager,
                                    LockManagerImpl lockManager) throws RepositoryConfigurationException {
      this(dataManager, null, lockManager, null);
  }

  /**
   * ProxyWorkspaceDataReceiver constructor.
   * 
   * @param dataManager
   *          the CacheableWorkspaceDataManager
   * @param searchManager
   *          the SearchManager
   * @throws RepositoryConfigurationException
   *           will be generated RepositoryConfigurationException
   */
  public ProxyWorkspaceDataReceiver(CacheableWorkspaceDataManager dataManager,
                                    SearchManager searchManager) throws RepositoryConfigurationException {
      this(dataManager, searchManager, null, null);
  }

  /**
   * ProxyWorkspaceDataReceiver constructor.
   * 
   * @param dataManager
   *          the CacheableWorkspaceDataManager
   * @throws RepositoryConfigurationException
   *           will be generated RepositoryConfigurationException
   */
  public ProxyWorkspaceDataReceiver(CacheableWorkspaceDataManager dataManager) throws RepositoryConfigurationException {
      this(dataManager, null, null, null);
  }

  /**
   * ProxyWorkspaceDataReceiver constructor.
   * 
   * @param dataManager
   *          the CacheableWorkspaceDataManager
   * @param searchManager
   *          the SearchManager
   * @param lockManager
   *          the LockManagerImpl
   * @throws RepositoryConfigurationException
   *           will be generated the RepositoryConfigurationException
   */
  public ProxyWorkspaceDataReceiver(CacheableWorkspaceDataManager dataManager,
                                    SearchManager searchManager,
            LockManagerImpl lockManager, SystemSearchManager systemSearchIndexer)
            throws RepositoryConfigurationException
   {
      dataKeeper = new WorkspaceDataManagerProxy(dataManager, searchManager, lockManager, systemSearchIndexer);
  }
}
