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
package org.exoplatform.services.jcr.ext.backup.server.bean.response;

import org.exoplatform.services.jcr.ext.backup.BackupChain;
import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.BackupManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 27.03.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: BackupChainBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class BackupChainBean {
  
  /**
   * The backup identifier.
   */
  private String backupId;
  
  /**
   * The backup type.
   */
  private Integer backupType;

  /**
   * The repository name.
   */
  private String repositoryName;

  /**
   * The workspace name.
   */
  private String workspaceName;
  
  /**
   * The full backup state.
   */
  private Integer  fullBackupState;

  /**
   * The incremental backup state.
   */
  private Integer  incrementalBackupState;
  
  /**
   * BackupChainBeen  constructor.
   *
   */
  public BackupChainBean() {
  }

  /**
   * BackupChainBeen  constructor.
   *
   * @param backupChain
   *          the BackupChain
   */
  public BackupChainBean(BackupChain backupChain) {
    this.backupId = backupChain.getBackupId();
    this.backupType = backupChain.getBackupConfig().getBackupType();
    this.repositoryName = backupChain.getBackupConfig().getRepository();
    this.workspaceName = backupChain.getBackupConfig().getWorkspace();
    
    this.fullBackupState = backupChain.getFullBackupState();

    if (backupChain.getBackupConfig().getBackupType() == BackupManager.FULL_BACKUP_ONLY)
      this.incrementalBackupState = 0;
    else
      for (BackupJob job : backupChain.getBackupJobs())
        if (job.getType() == BackupJob.INCREMENTAL)
          this.incrementalBackupState = job.getState();
  }

  /**
   * getBackupId.
   *
   * @return String
   *           return the backup identifier
   */
  public String getBackupId() {
    return backupId;
  }

  /**
   * setBackupId.
   *
   * @param backupId
   *          String, the backup identifier
   */
  public void setBackupId(String backupId) {
    this.backupId = backupId;
  }

  /**
   * getBackupType.
   *
   * @return Integer
   *           the backup type
   */
  public Integer getBackupType() {
    return backupType;
  }

  /**
   * setBackupType.
   *
   * @param backupType
   *          Integer, the backup type
   */
  public void setBackupType(Integer backupType) {
    this.backupType = backupType;
  }

  /**
   * getRepositoryName.
   *
   * @return String
   *           return the repository name
   */
  public String getRepositoryName() {
    return repositoryName;
  }

  /**
   * setRepositoryName.
   *
   * @param repositoryName
   *          String, the repository name
   */
  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  /**
   * getWorkspaceName.
   *
   * @return String
   *           return the workspace name
   */
  public String getWorkspaceName() {
    return workspaceName;
  }

  /**
   * setWorkspaceName.
   *
   * @param workspaceName
   *          String, the workspace name
   */
  public void setWorkspaceName(String workspaceName) {
    this.workspaceName = workspaceName;
  }
  
  /**
   * getFullBackupState.
   *
   * @return Integer
   *           return the state of full backup
   */
  public Integer getFullBackupState() {
    return fullBackupState;
  }

  /**
   * setFullBackupState.
   *
   * @param fullBackupState
   *          Integer, the state of full backup
   */
  public void setFullBackupState(Integer fullBackupState) {
    this.fullBackupState = fullBackupState;
  }

  /**
   * getIncrementalBackupState.
   *
   * @return Integer
   *           return the state of incremental backup
   */
  public Integer getIncrementalBackupState() {
    return incrementalBackupState;
  }

  /**
   * setIncrementalBackupState.
   *
   * @param incrementalBackupState
   *          Integer, teh state of incremental backup
   */
  public void setIncrementalBackupState(Integer incrementalBackupState) {
    this.incrementalBackupState = incrementalBackupState;
  }

}
