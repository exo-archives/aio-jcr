/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.backup;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.ext.backup.impl.BackupMessage;
import org.exoplatform.services.jcr.ext.backup.impl.BackupScheduler;
import org.exoplatform.services.jcr.ext.backup.impl.JobWorkspaceRestore;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface BackupManager {

  /**
   * The full backup only the type of backup.
   */
  static final int FULL_BACKUP_ONLY     = 0;

  /**
   * The full and incremental backup the type of backup.
   */
  static final int FULL_AND_INCREMENTAL = 1;

  /**
   * Getting current backups.
   *
   * @return Set
   *           return the set of current backups 
   */
  Set<BackupChain> getCurrentBackups();

  /**
   * Getting list of restores.
   *
   * @return List
   *           return the list of backups
   */
  List<JobWorkspaceRestore> getRestores();

  /**
   * Getting last restore by repository nam workspace.
   *
   * @param repositoryName
   *          String,  the repository name
   * @param workspaceName
   *          String, the workspace name
   * @return JobWorkspaceRestore
   *           return the job to restore
   */
  JobWorkspaceRestore getLastRestore(String repositoryName, String workspaceName);

  /**
   * Getting all backup logs .
   *
   * @return BackupChainLog[]
   *           return the all backup logs
   */
  BackupChainLog[] getBackupsLogs();

  /**
   * Starting backup.
   *
   * @param config
   *          BackupConfig, the backup configuration
   * @return BackupChain
   *           return the backup chain
   * @throws BackupOperationException BackupOperationException
   *           will be generate the exception BackupOperationException
   * @throws BackupConfigurationException
   *           will be generate the exception BackupConfigurationException
   * @throws RepositoryException
   *           will be generate the exception RepositoryException
   * @throws RepositoryConfigurationException
   *           will be generate the exception RepositoryConfigurationException
   */
  BackupChain startBackup(BackupConfig config) throws BackupOperationException,
                                              BackupConfigurationException,
                                              RepositoryException,
                                              RepositoryConfigurationException;

  /**
   * Stop backup.
   *
   * @param backup
   *          BackupChain, the backup chain 
   */
  void stopBackup(BackupChain backup);

  /**
   * Finding current backup by repository and workspace.
   *
   * @param reposytore
   *          String, the repository name
   * @param workspace
   *          String, the workspace name
   * @return BackupChain
   *           return the current backup 
   */
  BackupChain findBackup(String reposytore, String workspace);

  /**
   * Finding current backup by identifier.
   *
   * @param backupId
   *          String the backup identifier
   * @return BackupChain
   *           return the current backup
   */
  BackupChain findBackup(String backupId);

  /**
   * Restore from backup.
   *
   * @param log
   *          BackupChainLog, the backup log
   * @param repositoryName
   *          String, repository name
   * @param workspaceEntry
   *          WorkspaceEntry, the workspace entry
   * @throws BackupOperationException
   *           will be generate the exception BackupOperationException 
   * @throws BackupConfigurationException
   *           will be generate the exception BackupConfigurationException 
   * @throws RepositoryException
   *           will be generate the exception RepositoryException 
   * @throws RepositoryConfigurationException
   *           will be generate the exception RepositoryConfigurationException 
   */
  @Deprecated
  void restore(BackupChainLog log, String repositoryName, WorkspaceEntry workspaceEntry) throws BackupOperationException,
                                                                                        BackupConfigurationException,
                                                                                        RepositoryException,
                                                                                        RepositoryConfigurationException;
  
  /**
   * Restore from backup.
   *
   * @param log
   *          BackupChainLog, the backup log
   * @param repositoryName
   *          String, repository name
   * @param workspaceEntry
   *          WorkspaceEntry, the workspace entry
   * @param asynchronous
   *          boolean, in 'true' then asynchronous restore.   
   * @throws BackupOperationException
   *           will be generate the exception BackupOperationException 
   * @throws BackupConfigurationException
   *           will be generate the exception BackupConfigurationException 
   * @throws RepositoryException
   *           will be generate the exception RepositoryException 
   * @throws RepositoryConfigurationException
   *           will be generate the exception RepositoryConfigurationException 
   */
  void restore(BackupChainLog log,
               String repositoryName,
               WorkspaceEntry workspaceEntry,
               boolean asynchronous) throws BackupOperationException,
                                    BackupConfigurationException,
                                    RepositoryException,
                                    RepositoryConfigurationException;

  /**
   * Getting the scheduler.
   *
   * @return BackupScheduler
   *           return the BackupScheduler 
   */
  BackupScheduler getScheduler();

  /**
   * Getting the backup messages.
   *
   * @return BackupMessage[]
   *           return the backup messages
   */
  BackupMessage[] getMessages();

  /**
   * Getting backup directory.
   *
   * @return File
   *           return the backup directory
   */
  File getBackupDirectory();

  /**
   * Getting full backup type.
   *
   * @return Sting
   *           return FQN to full backup type 
   */
  String getFullBackupType();

  /**
   * Getting incremental backup type.
   *
   * @return String
   *         return FQN to full backup type
   */
  String getIncrementalBackupType();

  /**
   * Getting default incremental job period.
   *
   * @return long
   *           return the default incremental job period 
   */
  long getDefaultIncrementalJobPeriod();
}
