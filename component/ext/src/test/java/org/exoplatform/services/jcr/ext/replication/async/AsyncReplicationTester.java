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

import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.ext.replication.async.AsyncReplication.AsyncWorker;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorageImpl;
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

  public AsyncReplicationTester(RepositoryService repoService,
                                List<String> repositoryNames,
                                int priority,
                                String bindIPAddress,
                                String channelConfig,
                                String channelName,
                                int waitAllMembersTimeout,
                                String storagePath,
                                List<Integer> otherParticipantsPriority) throws RepositoryException,
      RepositoryConfigurationException {
    super(repoService,
          repositoryNames,
          priority,
          bindIPAddress,
          channelConfig,
          channelName,
          waitAllMembersTimeout,
          storagePath,
          otherParticipantsPriority);
  }

  protected void synchronize(String repoName, String workspaceName, String channelNameSuffix) throws RepositoryException,
                                                                                             RepositoryConfigurationException {

    ManageableRepository repository = repoService.getRepository(repoName);

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(workspaceName);

    NodeTypeDataManager ntm = (NodeTypeDataManager) wsc.getComponent(NodeTypeDataManager.class);
    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
    WorkspaceDataContainer dc = (WorkspaceDataContainer) wsc.getComponent(WorkspaceDataContainer.class);

    StorageKey skey = new StorageKey(repoName, workspaceName);
    LocalStorage localStorage = localStorages.get(skey);
    IncomeStorageImpl incomeStorage = new IncomeStorageImpl(incomeStoragePaths.get(skey));
    
    WorkspaceEntry wconf = (WorkspaceEntry) wsc.getComponent(WorkspaceEntry.class);
    WorkspaceFileCleanerHolder wfcleaner = (WorkspaceFileCleanerHolder) wsc.getComponent(WorkspaceFileCleanerHolder.class);


    AsyncWorker synchWorker = new AsyncWorker(dm, 
                                              ntm, 
                                              dc,
                                              (LocalStorageImpl)localStorage, 
                                              (IncomeStorageImpl)incomeStorage,
                                              repoName,
                                              workspaceName,
                                              channelNameSuffix,
                                              wconf,
                                              wfcleaner);
    
    synchWorker.run();

    currentWorkers.add(synchWorker);
  }
  
  protected void removeAllStorageListener() throws RepositoryException, RepositoryConfigurationException {
    for (String repositoryName : repositoryNames) {
      ManageableRepository repository = repoService.getRepository(repositoryName);

      for (String wsName : repository.getWorkspaceNames()) {
        StorageKey skey = new StorageKey(repositoryName, wsName);
        
        WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);
        PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
        
        LocalStorageImpl sls = localStorages.get(skey);
        System.out.println("Remove ItemPersistenceListener : " + sls);
        dm.removeItemPersistenceListener(localStorages.get(skey));
      }
  }
    }
}
