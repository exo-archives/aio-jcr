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
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;

/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.com.ua
 * 01.02.2008  
 */
public class ProxyWorkspaceDataReceiver extends AbstractWorkspaceDataReceiver {

  public ProxyWorkspaceDataReceiver( CacheableWorkspaceDataManager dataManager, LockManagerImpl lockManager)
  throws RepositoryConfigurationException {
    this(dataManager, null, lockManager);
  }
  
  public ProxyWorkspaceDataReceiver( CacheableWorkspaceDataManager dataManager, SearchManager searchManager)
  throws RepositoryConfigurationException {
    this(dataManager, searchManager, null);
  }
  
  public ProxyWorkspaceDataReceiver( CacheableWorkspaceDataManager dataManager)
  throws RepositoryConfigurationException {
    this(dataManager, null, null);
  }
  
  public ProxyWorkspaceDataReceiver( CacheableWorkspaceDataManager dataManager, SearchManager searchManager, LockManagerImpl lockManager)
      throws RepositoryConfigurationException {
    dataKeeper = new WorkspaceDataManagerProxy(dataManager, searchManager, lockManager);
  }
}
