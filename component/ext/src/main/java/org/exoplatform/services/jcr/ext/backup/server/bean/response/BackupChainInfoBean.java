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
import org.exoplatform.services.jcr.ext.backup.server.bean.BackupConfigBean;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 27.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: BackupChainInfoBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class BackupChainInfoBean extends BackupChainBean {

  /**
   * The BackupConfigBeen.
   */
  private BackupConfigBean backupConfigBean;

  /**
   * The path to backup log.
   */
  private String           backupLog;

  /**
   * The full backup state.
   */
  private Integer          fullBackupState;

  /**
   * The incremental backup state.
   */
  private Integer          incrementalBackupState;

  /**
   * BackupChainInfoBeen constructor.
   * 
   */
  public BackupChainInfoBean() {
    super();
  }

  /**
   * BackupChainInfoBeen constructor.
   * 
   * @param backupChain
   *          BacnupChain, the backup chain
   * @param configBeen
   *          BackupConfigBeen, the backup configuration been
   */
  public BackupChainInfoBean(BackupChain backupChain, BackupConfigBean configBeen) {
    super(backupChain);
    this.backupConfigBean = configBeen;
    this.backupLog = backupChain.getLogFilePath();
    this.fullBackupState = backupChain.getFullBackupState();

    if (backupChain.getBackupConfig().getBackupType() == BackupManager.FULL_BACKUP_ONLY)
      this.incrementalBackupState = 0;
    else
      for (BackupJob job : backupChain.getBackupJobs())
        if (job.getType() == BackupJob.INCREMENTAL)
          this.incrementalBackupState = job.getState();

  }

  /**
   * getBackupConfigBeen.
   * 
   * @return BackupConfigBeen, return the backup configuration been
   */
  public BackupConfigBean getBackupConfigBeen() {
    return backupConfigBean;
  }

  /**
   * setBackupConfigBeen.
   * 
   * @param backupConfigBean
   *          BackupConfigBeen, the backup configuration been
   */
  public void setBackupConfigBeen(BackupConfigBean backupConfigBean) {
    this.backupConfigBean = backupConfigBean;
  }

  /**
   * getBackupLog.
   *
   * @return String
   *           return the path to backup log
   */
  public String getBackupLog() {
    return backupLog;
  }

  /**
   * setBackupLog.
   *
   * @param backupLog
   *          String, the path to backup log
   */
  public void setBackupLog(String backupLog) {
    this.backupLog = backupLog;
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
