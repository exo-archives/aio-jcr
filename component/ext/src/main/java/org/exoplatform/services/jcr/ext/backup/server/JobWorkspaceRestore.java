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
import java.util.Calendar;

import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;

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
public class JobWorkspaceRestore extends Thread {

  /**
   * RESTORE_STARTED. The state of start restore.
   */
  public static final int         RESTORE_STARTED     = 1;

  /**
   * RESTORE_SUCCESSFUL. The state of restore successful.
   */
  public static final int         RESTORE_SUCCESSFUL  = 2;

  /**
   * RESTORE_FAIL. The state of restore fail.
   */
  public static final int         RESTORE_FAIL        = 3;

  /**
   * RESTORE_STARTED. The state of initialized restore.
   */
  public static final int         RESTORE_INITIALIZED = 4;

  /**
   * The state of restore.
   */
  private int                     stateRestore;

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
   * The exception on restore.
   */
  private Exception               restoreException    = null;

  /**
   * The start time of restore.
   */
  private Calendar                startTime;

  /**
   * The end time of restore.
   */
  private Calendar                endTime;

  /**
   * The BackupChainLog for restore.
   */
  private BackupChainLog          backupChainLog;

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
  public JobWorkspaceRestore(RepositoryService repositoryService,
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
    this.stateRestore = RESTORE_INITIALIZED;
  }

  /**
   * {@inheritDoc}
   */
  public void run() {
    try {
      stateRestore = RESTORE_STARTED;
      startTime = Calendar.getInstance();

      restore();

      stateRestore = RESTORE_SUCCESSFUL;
      endTime = Calendar.getInstance();
    } catch (WorkspaceRestoreExeption e) {
      stateRestore = RESTORE_FAIL;
      restoreException = e;
    }
  }

  /**
   * Will be restored the workspace.
   * 
   * @throws WorkspaceRestoreExeption
   *           will be generated the WorkspaceRestoreExeption
   */
  private void restore() throws WorkspaceRestoreExeption {
    try {
      RepositoryImpl repository = (RepositoryImpl) repositoryService.getRepository(repositoryName);

      RepositoryEntry reEntry = repository.getConfiguration();

      WorkspaceEntry wsEntry = getWorkspaceEntry(wEntry, workspaceName);

      repository.configWorkspace(wsEntry);

      try {
        File backLog = new File(path);
        backupChainLog = new BackupChainLog(backLog);
        backupManager.restore(backupChainLog, reEntry, wsEntry);
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

  /**
   * getRestoreException.
   * 
   * @return Exception return the exception of restore.
   */
  public Exception getRestoreException() {
    return restoreException;
  }

  /**
   * getStateRestore.
   * 
   * @return int return state of restore.
   */
  public int getStateRestore() {
    return stateRestore;
  }

  /**
   * getBeginTime.
   * 
   * @return Calendar return the start time of restore
   */
  public Calendar getStartTime() {
    return startTime;
  }

  /**
   * getEndTime.
   * 
   * @return Calendar return the end time of restore
   */
  public Calendar getEndTime() {
    return endTime;
  }

  /**
   * getBackupChainLog.
   *
   * @return BackupChainLog
   *           return the backup chain log for this restore.
   */
  public BackupChainLog getBackupChainLog() {
    return backupChainLog;
  }

  /**
   * getRepositoryName.
   *
   * @return String
   *           the name of destination repository
   */
  public String getRepositoryName() {
    return repositoryName;
  }

  /**
   * getWorkspaceName.
   *
   * @return
   */
  /**
   * getWorkspaceName.
   *
   * @return String
   *           the name of destination workspace
   */
  public String getWorkspaceName() {
    return workspaceName;
  }

}
