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
package org.exoplatform.services.jcr.ext.backup.impl;

import java.util.Calendar;

import javax.jcr.RepositoryException;

import org.exoplatform.services.log.Log;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.ext.backup.BackupChainLog;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.server.WorkspaceRestoreExeption;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;
import org.exoplatform.services.log.ExoLogger;

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
   * The apache logger.
   */
  private static Log              log = ExoLogger.getLogger("ext.JobWorkspaceRestore");

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
   * The WorkspaceEntry to restored workspace.
   */
  private final WorkspaceEntry    wEntry;

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
  private Throwable               restoreException    = null;

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
  private final BackupChainLog    backupChainLog;

  /**
   * JobWorkspaceRestore constructor.
   * 
   * @param repositoryService
   *          the RepositoryService
   * @param backupManager
   *          the BackupManager
   * @param repositoryName
   *          the destination repository
   * @param log
   *          the backup chain log
   * @param wEntry 
   *          the workspace entry
   */
  public JobWorkspaceRestore(RepositoryService repositoryService,
                             BackupManager backupManager,
                             String repositoryName,
                             BackupChainLog log,
                             WorkspaceEntry wEntry) {
    this.repositoryService = repositoryService;
    this.backupManager = backupManager;
    this.repositoryName = repositoryName;
    this.backupChainLog = log;
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
    } catch (Throwable t) {
      stateRestore = RESTORE_FAIL;
      restoreException = t;
      
      log.error("The restore was fail", t);
    }
  }

  /**
   * Will be restored the workspace.
   * 
   * @throws Throwable
   *           will be generated the Throwable
   */
  private void restore() throws Throwable {
    try {
      RepositoryImpl repository = (RepositoryImpl) repositoryService.getRepository(repositoryName);
      RepositoryEntry reEntry = repository.getConfiguration();

      try {
        backupManager.restore(backupChainLog, reEntry.getName(), wEntry, false);
      } catch (Throwable t) {
        removeWorkspace(repository, wEntry.getName());
        throw new WorkspaceRestoreExeption("Can not be restored the workspace '" + "/" + repositoryName + "/"
          + wEntry.getName() + "' :", t);
      } 

    } catch (Throwable t) {
      throw new WorkspaceRestoreExeption("Can not be restored the workspace  '" + "/" + repositoryName + "/"
          + wEntry.getName() + "' :", t);
    }
  }

  /**
   * removeWorkspace.
   *
   * @param mr
   *          ManageableRepository, the manageable repository
   * @param workspaceName
   *          String, the workspace name
   * @throws RepositoryException
   *           will be generated the RepositoryException
   */
  private void removeWorkspace(ManageableRepository mr, String workspaceName) throws RepositoryException {
    
    boolean isExists = false;
    for(String wsName : mr.getWorkspaceNames()) 
      if (workspaceName.equals(wsName)) {
        isExists = true;
        break;
      }

    if (isExists) {
      if (!mr.canRemoveWorkspace(workspaceName)) {
        WorkspaceContainerFacade wc = mr.getWorkspaceContainer(workspaceName);
        SessionRegistry sessionRegistry = (SessionRegistry) wc.getComponent(SessionRegistry.class);
        sessionRegistry.closeSessions(workspaceName);
      }
  
      mr.removeWorkspace(workspaceName);
    }
  }

  /**
   * getRestoreException.
   * 
   * @return Throwable return the exception of restore.
   */
  public Throwable getRestoreException() {
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
   * @return String
   *           the name of destination workspace
   */
  public String getWorkspaceName() {
    return wEntry.getName();
  }
  
  /**
   * getWorkspaceEntry
   * 
   * @return WorkspaceEntry
   * 
   */
  public WorkspaceEntry getWorkspaceEntry() {
  	return wEntry;
  }

}
