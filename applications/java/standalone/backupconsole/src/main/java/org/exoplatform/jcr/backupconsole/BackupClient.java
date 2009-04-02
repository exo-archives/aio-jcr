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
package org.exoplatform.jcr.backupconsole;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: BackupClient.java 111 2008-11-11 11:11:11Z serg $
 */
public interface BackupClient {

  /**
   * Start Backup.
   * 
   * @param repositoryName String, the repository name.
   * @param workspaceName String, the workspace name.
   * @param backupDir path to backup folder on remote server.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  String startBackUp(String repositoryName, String workspaceName, String backupDir) throws IOException, BackupExecuteException;

  /**
   * Start Incremental Backup.
   * 
   * @param repositoryName String, the repository name.
   * @param workspaceName String, the workspace name.
   * @param incr incemental job period.
   * @param backupDir path to backup folder on remote server.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  String startIncrementalBackUp(String repositoryName, String workspaceName, String backupDir, long incr) throws IOException,
                                                                                 BackupExecuteException;

  /**
   * Get Status.
   * 
   * @param backupId the backup identifier.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  String status(String backupId) throws IOException, BackupExecuteException;
  
  /**
   * Get information about backup service.
   * 
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  String info() throws IOException, BackupExecuteException;
  
  /**
   * Get information about current restores.
   * 
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  String restores() throws IOException, BackupExecuteException;

  /**
   * Restore repository from backup file.
   * 
   * @param repositoryName String, the repository name.
   * @param workspaceName String, the workspace name.
   * @param backupId the backup identifier.
   * @param config InputStream contains workspace configuration.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  String restore(String repositoryName, String workspaceName, String backupId, InputStream config) throws IOException,
                                                                                 BackupExecuteException;

  /**
   * Stop backup.
   * 
   * @param backupId the backup identifier.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  String stop(String backupId) throws IOException, BackupExecuteException;

  /**
   * Drop backup.
   * 
   * @param forceClose force sessions close on droped workspace.
   * @param repositoryName String, the repository name.
   * @param workspaceName String, the workspace name.
   * @return String result.
   * @throws IOException transport exception.
   * @throws BackupExecuteException backup client internal exception.
   */
  String drop(boolean forceClose, String repositoryName, String workspaceName) throws IOException,
                                                         BackupExecuteException;
  
  /**
   * Get information about the current backups (in progress).
   *
   * @return String result
   * @throws IOException
   * @throws BackupExecuteException
   */
  String list() throws IOException, BackupExecuteException;
  
  /**
   * Get information about the completed (ready to restore) backups.
   *
   * @return String result
   * @throws IOException
   * @throws BackupExecuteException
   */
  String listCompleted() throws IOException, BackupExecuteException;

}
