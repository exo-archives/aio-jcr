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

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;

/**
 * Created by The eXo Platform SAS. <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class WorkspaceSynchronizerImpl implements WorkspaceSynchronizer {

  protected final LocalStorage storage;
  
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
  public ChangesStorage<ItemState> getLocalChanges() {
    return storage.getLocalChanges();
  }

  /**
   * {@inheritDoc} 
   */
  public void save(ChangesStorage<ItemState> synchronizedChanges) {
    // TODO save to Workspace data manager
    ItemStateChangesLog changes = new PlainChangesLogImpl();
    try {
      workspace.save(changes);
    } catch (InvalidItemStateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnsupportedOperationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
