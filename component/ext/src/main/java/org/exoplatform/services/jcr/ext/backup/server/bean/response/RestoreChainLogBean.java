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
import org.exoplatform.services.jcr.ext.backup.server.bean.BackupConfigBean;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 30.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: RestoreChainLogBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class RestoreChainLogBean extends ChainLogBean {

  /**
   * The state of restore.
   */
  private Integer restoreState;

  /**
   * The start time of restore.
   */
  private String  restoreStart;

  /**
   * The end time of restore.
   */
  private String  restoreEnd;

  /**
   * The destination repository for restore.
   */
  private String  repositoryName;

  /**
   * The destination workspace for restore.
   */
  private String  workspaceName;

  /**
   * The failure message when restore was failed.
   */
  private String failMessage;

  /**
   * RestoreChainLogBeen constructor.
   * 
   */
  public RestoreChainLogBean() {
    super();
  }

  /**
   * RestoreChainLogBean  constructor.
   *
   * @param chainLog
   *          BackupChainLog, the backup chain log
   * @param backupConfigBean
   *          BackupConfigBean,  the bean to backup configuration
   * @param restoreState
   *          Integer, the state of restore
   * @param restoreStart
   *          String, the start time of restore
   * @param restoreEnd
   *          String, the end time of restore
   * @param repositoryName
   *          String, repository name
   * @param workspaceName
   *          String, workspace name
   * @param failMessage
   *          String, the failure message 
   */
  public RestoreChainLogBean(BackupChainLog chainLog,
                             BackupConfigBean backupConfigBean,
                             Integer restoreState,
                             String restoreStart,
                             String restoreEnd,
                             String repositoryName,
                             String workspaceName,
                             String failMessage) {
    super(chainLog, backupConfigBean);
    this.restoreState = restoreState;
    this.restoreStart = restoreStart;
    this.restoreEnd = restoreEnd;
    this.repositoryName = repositoryName;
    this.workspaceName = workspaceName;
    this.failMessage = failMessage;
  }
  
  /**
   * RestoreChainLogBean  constructor.
   *
   * @param chainLog
   *          BackupChainLog, the backup chain log
   * @param backupConfigBean
   *          BackupConfigBean,  the bean to backup configuration
   * @param restoreState
   *          Integer, the state of restore
   * @param restoreStart
   *          String, the start time of restore
   * @param restoreEnd
   *          String, the end time of restore
   * @param repositoryName
   *          String, repository name
   * @param workspaceName
   *          String, workspace name
   */
  public RestoreChainLogBean(BackupChainLog chainLog,
                             BackupConfigBean backupConfigBean,
                             Integer restoreState,
                             String restoreStart,
                             String restoreEnd,
                             String repositoryName,
                             String workspaceName) {
    this(chainLog, 
         backupConfigBean, 
         restoreState, 
         restoreStart, 
         restoreEnd, 
         repositoryName, 
         workspaceName, 
         null);
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
   *          Integer,  the state of restore
   */
  public void setRestoreState(Integer restoreState) {
    this.restoreState = restoreState;
  }

  /**
   * getRestoreStart.
   *
   * @return String
   *           return the start time of restore
   */
  public String getRestoreStart() {
    return restoreStart;
  }

  /**
   * setRestoreStart.
   *
   * @param restoreStart
   *          String, the start time of restore
   */
  public void setRestoreStart(String restoreStart) {
    this.restoreStart = restoreStart;
  }

  /**
   * getRestoreEnd.
   *
   * @return String
   *           return the end time of restore 
   */
  public String getRestoreEnd() {
    return restoreEnd;
  }

  /**
   * setRestoreEnd.
   *
   * @param restoreEnd
   *          String, the end time of restore
   */
  public void setRestoreEnd(String restoreEnd) {
    this.restoreEnd = restoreEnd;
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
   * getFailMessage.
   *
   * @return String
   *           return the failure message
   */
  public String getFailMessage() {
    return failMessage;
  }

  /**
   * setFailMessage.
   *
   * @param failMessage
   *          String, the failure message
   */
  public void setFailMessage(String failMessage) {
    this.failMessage = failMessage;
  }
}
