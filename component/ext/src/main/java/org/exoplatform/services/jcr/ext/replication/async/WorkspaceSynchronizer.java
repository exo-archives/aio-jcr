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

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.SynchronizationException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 24.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: WorkspaceSynchronizer.java 25715 2008-12-24 11:40:47Z pnedonosko $
 */
public interface WorkspaceSynchronizer {

  /**
   * Get Local changes.
   *
   * @return ChangesStorage
   */
  ChangesStorage<ItemState> getLocalChanges();
  
  /**
   * Save synchronized changes to a local workspace.
   * 
   * @throws SynchronizationException 
   * @throws RepositoryException 
   * @throws UnsupportedOperationException 
   * @throws InvalidItemStateException 
   */
  void save(ChangesStorage<ItemState> synchronizedChanges) throws InvalidItemStateException, UnsupportedOperationException, RepositoryException, SynchronizationException;

}
