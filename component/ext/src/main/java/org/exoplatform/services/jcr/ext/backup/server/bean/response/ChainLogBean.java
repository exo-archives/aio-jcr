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

import org.exoplatform.services.jcr.ext.backup.BackupChainLog;
import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.JobEntryInfo;
import org.exoplatform.services.jcr.ext.backup.server.bean.BackupBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.BackupConfigBean;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 27.03.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: ChainLogBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ChainLogBean extends BackupBean {

  /**
   * The BackupConfigBeen.
   */
  private BackupConfigBean backupConfigBean;
  
  /**
   * The path to backup log.
   */
  private String backupLog;
  
  /**
   * The date of full backup.
   */
  private String fullBackupDate;
  
  /**
   * ChainLogBeen  constructor.
   * Empty constructor.
   */
  public ChainLogBean() {
    super();
  }
  
  /**
   * ChainLogBeen  constructor.
   *
   * @param chainLog
   *          BackupChainLog, the backup chain log
   * @param backupConfigBean
   *          BackupConfigBeen, the been for BackupConfig
   */
  public ChainLogBean(BackupChainLog chainLog, BackupConfigBean backupConfigBean) {
    super(chainLog.getBackupId());
    this.backupConfigBean = backupConfigBean;
    this.backupLog = chainLog.getLogFilePath();
    
    for (JobEntryInfo info : chainLog.getJobEntryInfos()) 
       if (info.getType() == BackupJob.FULL) 
         fullBackupDate =  JCRDateFormat.format(info.getDate());
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
   * getFullBackupDate.
   *
   * @return String
   *           return the date of full backup
   */
  public String getFullBackupDate() {
    return fullBackupDate;
  }

  /**
   * setFullBackupDate.
   *
   * @param fullBackupDate
   *          String, the date of full backup
   */
  public void setFullBackupDate(String fullBackupDate) {
    this.fullBackupDate = fullBackupDate;
  }

}
