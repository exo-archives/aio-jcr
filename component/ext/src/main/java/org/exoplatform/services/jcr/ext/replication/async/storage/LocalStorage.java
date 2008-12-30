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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Stores changes from local workspace. Used by <code>WorkspaceSynchronizer</code>. 
 * 
 * <br/>Date: 24.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public interface LocalStorage extends ItemsPersistenceListener {

  /**
   * Return Local changes persisted to a <code>ChangesStorage</code>.
   * 
   * @return ChangesStorage
   */
  ChangesStorage<ItemState> getLocalChanges();
  
}
