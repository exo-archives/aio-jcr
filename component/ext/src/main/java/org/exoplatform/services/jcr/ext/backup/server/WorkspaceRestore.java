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
import java.util.ArrayList;
import java.util.Iterator;

import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.QueryHandlerEntry;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.ext.backup.BackupChainLog;
import org.exoplatform.services.jcr.ext.backup.BackupConfigurationException;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.BackupOperationException;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

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
   * The user name.
   */
  private final String            userName;

  /**
   * The password.
   */
  private final String            password;

  /**
   * The path to backup log.
   */
  private final String            path;

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
   * @param path
   *          path to backup log
   */
  public WorkspaceRestore(RepositoryService repositoryService,
                          BackupManager backupManager,
                          String repositoryName,
                          String workspaceName,
                          String userName,
                          String password,
                          String path) {
    this.repositoryService = repositoryService;
    this.backupManager = backupManager;
    this.repositoryName = repositoryName;
    this.workspaceName = workspaceName;
    this.userName = userName;
    this.password = password;
    this.path = path;
  }

  public void restore() throws WorkspaceRestoreExeption {
    try {
      RepositoryImpl repository = (RepositoryImpl) repositoryService.getRepository(repositoryName);
      SessionImpl session = (SessionImpl) repository.login(new CredentialsImpl(userName,
                                                                               password.toCharArray()),
                                                           workspaceName);

      RepositoryEntry reEntry = (RepositoryEntry) session.getContainer()
                                                         .getComponentInstanceOfType(RepositoryEntry.class);
      WorkspaceEntry wsEntry = (WorkspaceEntry) session.getContainer()
                                                       .getComponentInstanceOfType(WorkspaceEntry.class);


      if (!repository.canRemoveWorkspace(workspaceName))
        throw new WorkspaceRestoreExeption("The workspase '" + workspaceName+ "' can not be removed");
      else {
        // remove workspase
        repository.removeWorkspace(workspaceName);

        repository.configWorkspace(wsEntry);

        File backLog = new File(path);
        if (backLog.exists()) {
          try {
            BackupChainLog bchLog = new BackupChainLog(backLog);
            backupManager.restore(bchLog, reEntry, wsEntry);
          } catch (BackupOperationException e) {
            throw new WorkspaceRestoreExeption("Can not be restored the workspace '"
                + workspaceName + "' :" + e, e);
          } catch (BackupConfigurationException e) {
            throw new WorkspaceRestoreExeption("Can not be restored the workspace '"
                + workspaceName + "' :" + e, e);
          } catch (RepositoryException e) {
            throw new WorkspaceRestoreExeption("Can not be restored the workspace '"
                + workspaceName + "' :" + e, e);
          } catch (RepositoryConfigurationException e) {
            throw new WorkspaceRestoreExeption("Can not be restored the workspace '"
                + workspaceName + "' :" + e, e);
          }
        } else
          throw new WorkspaceRestoreExeption("Can not find the backup log file : " + path);
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
}
