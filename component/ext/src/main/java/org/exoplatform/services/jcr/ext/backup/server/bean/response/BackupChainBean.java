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
  }

  public String getBackupId() {
    return backupId;
  }

  public void setBackupId(String backupId) {
    this.backupId = backupId;
  }

  public Integer getBackupType() {
    return backupType;
  }

  public void setBackupType(Integer backupType) {
    this.backupType = backupType;
  }

  public String getRepositoryName() {
    return repositoryName;
  }

  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  public String getWorkspaceName() {
    return workspaceName;
  }

  public void setWorkspaceName(String workspaceName) {
    this.workspaceName = workspaceName;
  }

}
