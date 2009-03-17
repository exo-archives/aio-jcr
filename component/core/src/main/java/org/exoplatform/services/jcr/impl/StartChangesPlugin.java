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
package org.exoplatform.services.jcr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.persistent.StartChangesListener;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: StoreChangesPlugin.java 111 2008-11-11 11:11:11Z $
 */
public class StartChangesPlugin extends BaseComponentPlugin {

  private List<String>                           workspaces;

  private List<StartChangesListener>             startChangesListeners;

  private Map<String, List<ItemStateChangesLog>> changes;

  /**
   * StoreChangesPlugin constructor.
   * 
   * @param params
   */
  public StartChangesPlugin(InitParams params) {
    changes = new HashMap<String, List<ItemStateChangesLog>>();
    workspaces = new ArrayList<String>();
    startChangesListeners = new ArrayList<StartChangesListener>();

    ValueParam param = params.getValueParam("workspaces");
    if (param != null) {
      StringTokenizer listTokenizer = new StringTokenizer(param.getValue(), ",");

      while (listTokenizer.hasMoreTokens()) {
        String wsName = listTokenizer.nextToken();
        workspaces.add(wsName);
        changes.put(wsName, new ArrayList<ItemStateChangesLog>());
      }
    }
  }

  /**
   * getChanges.
   * 
   * @return
   */
  public Map<String, List<ItemStateChangesLog>> getChanges() {
    return changes;
  }

  /**
   * addListeners.
   * 
   * @param repository
   */
  public void addListeners(ManageableRepository repository) {
    for (int i = 0; i < workspaces.size(); i++) {
      String wsName = workspaces.get(i);

      StartChangesListener changesListener = new StartChangesListener(wsName, changes.get(wsName));
      startChangesListeners.add(changesListener);

      WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);
      wsc.addComponent(changesListener);

      CacheableWorkspaceDataManager dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);
      dm.addItemPersistenceListener(changesListener);
    }
  }

  /**
   * removeListener.
   * 
   * @param repository
   */
  public void removeListeners(ManageableRepository repository) {
    for (int i = 0; i < startChangesListeners.size(); i++) {
      StartChangesListener changesListener = startChangesListeners.get(i);

      WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(changesListener.getWorkspaceName());
      PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
      dm.removeItemPersistenceListener(changesListener);
    }
  }

}
