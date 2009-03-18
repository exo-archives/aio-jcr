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
package org.exoplatform.services.jcr.dataflow.persistent;

import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: ChangesListenerData.java 111 2008-11-11 11:11:11Z $
 */
public class StartChangesListener implements ItemsPersistenceListener {

  private final String                    workspaceName;

  private final String                    repositoryName;

  private final List<ItemStateChangesLog> changes;

  /**
   * ChangesListener constructor.
   * 
   * @param workspaceName
   */
  public StartChangesListener(String repositoryName,
                              String workspaceName,
                              List<ItemStateChangesLog> changes) {
    this.workspaceName = workspaceName;
    this.repositoryName = repositoryName;
    this.changes = changes;
  }

  /**
   * getWorkspaceName.
   * 
   * @return
   */
  public String getWorkspaceName() {
    return workspaceName;
  }

  public String getRepositoryname() {
    return repositoryName;
  }

  /**
   * getChanges.
   * 
   * @return
   */
  public List<ItemStateChangesLog> getChanges() {
    return changes;
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {
    changes.add(itemStates);
  }
}
