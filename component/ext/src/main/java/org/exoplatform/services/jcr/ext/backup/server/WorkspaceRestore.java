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
package org.exoplatform.services.jcr.ext.backup.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.ext.backup.BackupChainLog;
import org.exoplatform.services.jcr.ext.backup.BackupConfigurationException;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.BackupOperationException;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 24.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: WorkspaceRestore.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class WorkspaceRestore {

  /**
   * The destination repository.
   */
  private final String            repositoryName;

  /**
   * The destination workspace.
   */
  private final String            workspaceName;

  /**
   * The path to backup log.
   */
  private final String            path;

  /**
   * The input stream with WorkspaceEntry.
   */
  private final InputStream       wEntry;

  /**
   * The repository service.
   */
  private final RepositoryService repositoryService;

  /**
   * The backup manager.
   */
  private final BackupManager     backupManager;

  /**
   * WorkspaceRestore constructor.
   * 
   * @param repositoryService
   *          the RepositoryService
   * @param backupManager
   *          the BackupManager
   * @param repositoryName
   *          the destination repository
   * @param workspaceName
   *          the destination workspace
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param logPath
   *          path to backup log
   */
  public WorkspaceRestore(RepositoryService repositoryService,
                          BackupManager backupManager,
                          String repositoryName,
                          String workspaceName,
                          String logPath,
                          InputStream wEntry) {
    this.repositoryService = repositoryService;
    this.backupManager = backupManager;
    this.repositoryName = repositoryName;
    this.workspaceName = workspaceName;
    this.path = logPath;
    this.wEntry = wEntry;
  }

  public void restore() throws WorkspaceRestoreExeption {
    try {
      RepositoryImpl repository = (RepositoryImpl) repositoryService.getRepository(repositoryName);

      RepositoryEntry reEntry = repository.getConfiguration();

      WorkspaceEntry wsEntry = getWorkspaceEntry(wEntry, workspaceName);

      repository.configWorkspace(wsEntry);

      try {
        File backLog = new File(path);
        BackupChainLog bchLog = new BackupChainLog(backLog);
        backupManager.restore(bchLog, reEntry, wsEntry);
      } catch (BackupOperationException e) {
        removeWorkspace(repository, workspaceName);
        throw new WorkspaceRestoreExeption("Can not be restored the workspace '" + workspaceName
            + "' :" + e, e);
      } catch (BackupConfigurationException e) {
        removeWorkspace(repository, workspaceName);
        throw new WorkspaceRestoreExeption("Can not be restored the workspace '" + workspaceName
            + "' :" + e, e);
      } catch (RepositoryException e) {
        removeWorkspace(repository, workspaceName);
        throw new WorkspaceRestoreExeption("Can not be restored the workspace '" + workspaceName
            + "' :" + e, e);
      } catch (RepositoryConfigurationException e) {
        removeWorkspace(repository, workspaceName);
        throw new WorkspaceRestoreExeption("Can not be restored the workspace '" + workspaceName
            + "' :" + e, e);
      }

    } catch (NoSuchWorkspaceException e) {
      throw new WorkspaceRestoreExeption("Can not be restored the workspace '" + workspaceName
          + "' :" + e, e);
    } catch (RepositoryException e) {
      throw new WorkspaceRestoreExeption("Can not be restored the workspace '" + workspaceName
          + "' :" + e, e);
    } catch (RepositoryConfigurationException e) {
      throw new WorkspaceRestoreExeption("Can not be restored the workspace '" + workspaceName
          + "' :" + e, e);
    } catch (Throwable t) {
      throw new WorkspaceRestoreExeption("Can not be restored the workspace '" + workspaceName
          + "' :" + t, t);
    }
  }

  private WorkspaceEntry getWorkspaceEntry(InputStream wEntryStream, String workspaceName) throws FileNotFoundException,
                                                                                          JiBXException,
                                                                                          RepositoryConfigurationException {
    WorkspaceEntry wsEntry = null;

    IBindingFactory factory = BindingDirectory.getFactory(RepositoryServiceConfiguration.class);
    IUnmarshallingContext uctx = factory.createUnmarshallingContext();
    RepositoryServiceConfiguration conf = (RepositoryServiceConfiguration) uctx.unmarshalDocument(wEntryStream,
                                                                                                  null);

    RepositoryEntry rEntry = conf.getRepositoryConfiguration(repositoryName);

    for (WorkspaceEntry wEntry : rEntry.getWorkspaceEntries())
      if (wEntry.getName().equals(workspaceName))
        wsEntry = wEntry;

    if (wsEntry == null)
      throw new RuntimeException("Can not find the workspace '" + workspaceName
          + "' in configuration.");

    return wsEntry;
  }

  private void removeWorkspace(ManageableRepository mr, String workspaceName) throws RepositoryException {

    if (!mr.canRemoveWorkspace(workspaceName)) {
      WorkspaceContainerFacade wc = mr.getWorkspaceContainer(workspaceName);
      SessionRegistry sessionRegistry = (SessionRegistry) wc.getComponent(SessionRegistry.class);
      sessionRegistry.closeSessions(workspaceName);
    } 
    
    mr.removeWorkspace(workspaceName);
  }

}
