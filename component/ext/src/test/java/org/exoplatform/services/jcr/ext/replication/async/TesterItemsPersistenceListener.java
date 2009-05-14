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

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 10.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TesterItemsPersistenceListener implements ItemsPersistenceListener {

  private final List<TransactionChangesLog> logsList = new ArrayList<TransactionChangesLog>();

  private final PersistentDataManager       dataManager;

  public TesterItemsPersistenceListener(SessionImpl session) {
    this.dataManager = (PersistentDataManager) ((ManageableRepository) session.getRepository()).getWorkspaceContainer(session.getWorkspace()
                                                                                                                             .getName())
                                                                                               .getComponent(PersistentDataManager.class);
    this.dataManager.addItemPersistenceListener(this);
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {
    logsList.add((TransactionChangesLog) itemStates);
  }

  /**
   * Unregister the listener and return collected changes.
   * 
   * @return List of TransactionChangesLog
   */
  public List<TransactionChangesLog> pushChanges() {
    dataManager.removeItemPersistenceListener(this);
    return logsList;
  }

  public List<TransactionChangesLog> getCurrentLogList() {
    return logsList;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void finalize() throws Throwable {
    logsList.clear();
  }
}
