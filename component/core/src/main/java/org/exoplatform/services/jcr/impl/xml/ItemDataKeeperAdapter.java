/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.xml;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.dataflow.ItemDataKeeper;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class ItemDataKeeperAdapter implements ItemDataKeeper {
  /**
   * Internal data manager.
   */
  private final SessionDataManager sessionDataManager;

  /**
   * Class logger.
   */
  private final Log                log = ExoLogger.getLogger("jcr.ItemDataKeeperAdapter");

  /**
   * Adapter from SessionDataManager to ItemDataKeeper.
   * 
   * @param sessionDataManager - Data manager.
   */
  public ItemDataKeeperAdapter(SessionDataManager sessionDataManager) {
    super();
    this.sessionDataManager = sessionDataManager;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataKeeper#save(org.exoplatform.services.jcr.dataflow.ItemStateChangesLog)
   */
  public void save(ItemStateChangesLog changes) throws InvalidItemStateException,
                                               UnsupportedOperationException,
                                               RepositoryException {
    for (ItemState itemState : changes.getAllStates()) {
      if (itemState.isAdded())
        sessionDataManager.update(itemState, false);
      else if (itemState.isDeleted())
        sessionDataManager.delete(itemState.getData(), itemState.getAncestorToSave());
    }
  }
}
