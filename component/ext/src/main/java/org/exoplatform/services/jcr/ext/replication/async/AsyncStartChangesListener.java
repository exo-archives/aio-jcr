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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: ChangesListenerData.java 111 2008-11-11 11:11:11Z $
 */
public class AsyncStartChangesListener implements ItemsPersistenceListener {

  private final List<ItemStateChangesLog> changes;

  private final PersistentDataManager     dataManager;

  /**
   * ChangesListener constructor.
   * 
   * @param workspaceName
   */
  public AsyncStartChangesListener(PersistentDataManager dataManager,
                                   AsyncReplication asyncReplication) {
    this.changes = new ArrayList<ItemStateChangesLog>();
    this.dataManager = dataManager;
    this.dataManager.addItemPersistenceListener(this);
  }

  /**
   * Return all changes from the start.
   * 
   * @return List of changes.
   */
  public List<ItemStateChangesLog> getChanges() {
    return changes;
  }

  /**
   * Clear the accumulated changes and unregister as listener.
   */
  public void clear() {
    dataManager.removeItemPersistenceListener(this);
    changes.clear();
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {
    changes.add(itemStates);
  }
}
