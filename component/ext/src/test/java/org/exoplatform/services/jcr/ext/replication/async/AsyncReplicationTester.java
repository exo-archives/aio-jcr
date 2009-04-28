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

import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.ext.replication.async.config.AsyncWorkspaceConfig;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorageImpl;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 16.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncReplicationTester.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncReplicationTester extends AsyncReplication {

  public AsyncReplicationTester(RepositoryService repoService, InitParams params) throws RepositoryException,
      RepositoryConfigurationException {
    super(repoService, params);
  }

  public AsyncReplicationTester(RepositoryService repoService, List<AsyncWorkspaceConfig> configs) throws RepositoryException,
      RepositoryConfigurationException {
    super(repoService, configs);
  }

  protected void synchronize(String repoName, String workspaceName, String channelNameSuffix) throws RepositoryException,
                                                                                             RepositoryConfigurationException {
    AsyncWorkspaceConfig awConfig = null;

    for (AsyncWorkspaceConfig config : asyncWorkspaceConfigs)
      if (repoName.endsWith(config.getRepositoryName())
          && workspaceName.equals(config.getWorkspaceName()))
        awConfig = config;

    if (awConfig == null)
      throw new RuntimeException("The asynchronus replication was not configured for workspace "
          + repoName + "@" + workspaceName);

    ManageableRepository repository = repoService.getRepository(repoName);

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(workspaceName);

    NodeTypeDataManager ntm = (NodeTypeDataManager) wsc.getComponent(NodeTypeDataManager.class);
    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
    WorkspaceDataContainer dc = (WorkspaceDataContainer) wsc.getComponent(WorkspaceDataContainer.class);

    WorkspaceContainerFacade sysWsc = repository.getWorkspaceContainer(((RepositoryImpl) repository).getSystemWorkspaceName());
    PersistentDataManager sysDm = (PersistentDataManager) sysWsc.getComponent(PersistentDataManager.class);

    StorageKey skey = new StorageKey(repoName, workspaceName);
    LocalStorage localStorage = localStorages.get(skey);

    WorkspaceEntry wconf = (WorkspaceEntry) wsc.getComponent(WorkspaceEntry.class);
    WorkspaceFileCleanerHolder wfcleaner = (WorkspaceFileCleanerHolder) wsc.getComponent(WorkspaceFileCleanerHolder.class);
    int maxBufferSize = wconf.getContainer()
                             .getParameterInteger(WorkspaceDataContainer.MAXBUFFERSIZE,
                                                  WorkspaceDataContainer.DEF_MAXBUFFERSIZE);

    ReaderSpoolFileHolder holder = new ReaderSpoolFileHolder();
    IncomeStorageImpl incomeStorage = new IncomeStorageImpl(incomeStoragePaths.get(skey),
                                                            wfcleaner.getFileCleaner(),
                                                            maxBufferSize,
                                                            holder);

    AsyncWorker synchWorker = new AsyncWorker(dm,
                                              sysDm,
                                              ntm,
                                              dc,
                                              (LocalStorageImpl) localStorage,
                                              (IncomeStorageImpl) incomeStorage,
                                              awConfig,
                                              channelNameSuffix,
                                              wconf,
                                              wfcleaner,
                                              holder);

    synchWorker.run();

    currentWorkers.add(synchWorker);
  }

  protected void removeAllStorageListener() throws RepositoryException,
                                           RepositoryConfigurationException {
    for (AsyncWorkspaceConfig config : asyncWorkspaceConfigs) {
      ManageableRepository repository = repoService.getRepository(config.getRepositoryName());

      StorageKey skey = new StorageKey(config.getRepositoryName(), config.getWorkspaceName());

      WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(config.getWorkspaceName());
      PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);

      LocalStorageImpl sls = localStorages.get(skey);
      System.out.println("Remove ItemPersistenceListener : " + sls);
      dm.removeItemPersistenceListener(localStorages.get(skey));
    }
  }

  protected static InitParams getInitParams(String repositoryName,
                                            String workspaceName,
                                            int priority,
                                            List<Integer> otherParticipantsPriority,
                                            String bindAddress,
                                            String channelConfig,
                                            String channelName,
                                            String storageDir,
                                            int waitAllMemberTimeout) {

    InitParams params = new InitParams();

    PropertiesParam pps = new PropertiesParam();

    pps.setName("async-workspca-config");

    pps.setProperty("repository-name", repositoryName);
    pps.setProperty("workspace-name", workspaceName);
    pps.setProperty("priority", String.valueOf(priority));

    String others = "";
    for (int i = 0; i < otherParticipantsPriority.size(); i++)
      if (i == 0)
        others += String.valueOf(otherParticipantsPriority.get(i));
      else
        others += ("," + String.valueOf(otherParticipantsPriority.get(i)));

    pps.setProperty("other-participants-priority", others);

    pps.setProperty("bind-ip-address", bindAddress);
    pps.setProperty("channel-config", channelConfig);
    pps.setProperty("channel-name", channelName);
    pps.setProperty("storage-dir", storageDir);
    pps.setProperty("wait-all-members", String.valueOf(waitAllMemberTimeout));

    params.addParam(pps);

    return params;
  }

  public boolean hasAddedRootNodeWS3() throws Exception {
    LocalStorageImpl sls = localStorages.get(new StorageKey("db1", "ws3"));

    WorkspaceContainerFacade wsc = repoService.getRepository("db1").getWorkspaceContainer("ws3");
    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
    dm.removeItemPersistenceListener(sls);
    sls.onStart(null);

    Iterator<ItemState> changes = sls.getLocalChanges(false).getChanges();
    ItemState item = changes.next();
    return item.isSame(Constants.ROOT_UUID, Constants.ROOT_PATH, ItemState.ADDED);
  }

}
