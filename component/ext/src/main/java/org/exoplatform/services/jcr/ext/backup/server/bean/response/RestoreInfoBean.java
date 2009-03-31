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

import org.exoplatform.services.jcr.ext.backup.server.bean.BaseBean;
import org.exoplatform.services.jcr.ext.backup.server.bean.RestoreBean;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 27.03.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: RestoresInfoBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class RestoreInfoBean  extends BaseBean {
 
  /**
   *  The backup identifier.
   */
  private String backupId;
  
  /**
   * The path to backup log.
   */
  private String backupLog;
  
  /**
   * The state of restore.
   */
  private Integer restoreState;

  /**
   * RestoreInfoBeen  constructor.
   *
   */
  public RestoreInfoBean() {
    super();
  }
  
  /**
   * RestoreInfoBeen  constructor.
   *
   * @param backupId
   *          String, the backup identifier
   * @param repositoryName
   *          String, the repository name
   * @param workspaceName
   *          String, the workspace name
   * @param backupLog
   *          String, the path to backup log
   * @param restoreState
   *           Integer, the state of restore
   */
  public RestoreInfoBean(String backupId,
                         String repositoryName,
                         String workspaceName,
                         String backupLog,
                         Integer restoreState) {
    super(repositoryName, workspaceName);
    this.backupId = backupId;
    this.backupLog = backupLog;
    this.restoreState = restoreState;
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
   *          String, the backup id
   */
  public void setBackupId(String backupId) {
    this.backupId = backupId;
  }

  /**
   * getBackupLog.
   *
   * @return String
   *           return the path to backup log
   */
  public String getBacskupLog() {
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
   * getRestoreState.
   *
   * @return Integer
   *           return the state of restore
   */
  public Integer getRestoreState() {
    return restoreState;
  }

  /**
   * setRestoreState.
   *
   * @param restoreState
   *          Integer, the state of restore
   */
  public void setRestoreState(Integer restoreState) {
    this.restoreState = restoreState;
  }
  
}
