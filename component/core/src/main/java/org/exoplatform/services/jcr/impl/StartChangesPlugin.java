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
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.persistent.StartChangesListener;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: StoreChangesPlugin.java 111 2008-11-11 11:11:11Z $
 */
public class StartChangesPlugin extends BaseComponentPlugin {

  private List<StartChangesListener>                 startChangesListeners;

  private Map<StorageKey, List<ItemStateChangesLog>> changes;

  private String                                     repositoryName;

  private String                                     workspaces;

  /**
   * StoreChangesPlugin constructor.
   * 
   * @param params
   */
  public StartChangesPlugin(InitParams params) {
    changes = new HashMap<StorageKey, List<ItemStateChangesLog>>();
    startChangesListeners = new ArrayList<StartChangesListener>();

    // TODO
    if (params != null) {
      ValueParam valueParam = params.getValueParam("repository-name");
      repositoryName = valueParam == null ? null : valueParam.getValue();

      if (repositoryName == null) {
        throw new RuntimeException("Repository name not specified.");
      }

      valueParam = params.getValueParam("workspaces");
      workspaces = valueParam == null ? null : valueParam.getValue();

      if (repositoryName == null) {
        throw new RuntimeException("Workspaces names not specified.");
      }
    }
  }

  /**
   * addListeners.
   * 
   * @param repository
   */
  public void addListeners(RepositoryContainer repositoryContainer) {
    String curRepositoryName = repositoryContainer.getName();

    if (repositoryName.equals(curRepositoryName)) {
      StringTokenizer listTokenizer = new StringTokenizer(workspaces, ",");

      while (listTokenizer.hasMoreTokens()) {
        String wsName = listTokenizer.nextToken();

        StorageKey sk = new StorageKey(curRepositoryName, wsName);
        changes.put(sk, new ArrayList<ItemStateChangesLog>());

        StartChangesListener changesListener = new StartChangesListener(curRepositoryName,
                                                                        wsName,
                                                                        changes.get(sk));
        startChangesListeners.add(changesListener);

        WorkspaceContainer wc = repositoryContainer.getWorkspaceContainer(wsName);
        wc.registerComponentInstance(changesListener);

        PersistentDataManager dm = (PersistentDataManager) wc.getComponentInstanceOfType(PersistentDataManager.class);
        dm.addItemPersistenceListener(changesListener);
      }
    }
  }

  /**
   * removeListener.
   * 
   * @param repository
   */
  public void removeListeners(RepositoryContainer repositoryContainer) {
    for (int i = 0; i < startChangesListeners.size(); i++) {
      StartChangesListener changesListener = startChangesListeners.get(i);
      if (changesListener.getRepositoryname().equals(repositoryContainer.getName())) {
        WorkspaceContainer wc = repositoryContainer.getWorkspaceContainer(changesListener.getWorkspaceName());
        PersistentDataManager dm = (PersistentDataManager) wc.getComponentInstanceOfType(PersistentDataManager.class);
        dm.removeItemPersistenceListener(changesListener);
      }
    }
  }

  /**
   * Will be used as key for mapLocalStorages.
   * 
   */
  protected class StorageKey {
    private final String repositoryName;

    private final String workspaceName;

    /**
     * StorageKey constructor.
     * 
     * @param repositoryName
     *          The repository name
     * @param workspaceName
     *          The workspace name
     */
    public StorageKey(String repositoryName, String workspaceName) {
      this.repositoryName = repositoryName;
      this.workspaceName = workspaceName;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
      StorageKey k = (StorageKey) o;

      return repositoryName.equals(k.repositoryName) && workspaceName.equals(k.workspaceName);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
      return repositoryName.hashCode() ^ workspaceName.hashCode();
    }
  }
}
