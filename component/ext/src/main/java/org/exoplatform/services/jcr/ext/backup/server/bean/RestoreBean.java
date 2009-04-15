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
package org.exoplatform.services.jcr.ext.backup.server.bean;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 26.03.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: RestoreBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class RestoreBean {

  /**
   * The backup identifier.
   */
  String backupId;
  
  /**
   * The workspace configuration.
   */
  String workspaceConfig;
  
  /**
   * RestoreBeen  constructor.
   *
   */
  public RestoreBean() {
  }
  
  /**
   * RestoreBeen  constructor.
   *
   * @param backupId
   *          String, the backup identifier
   * @param workspaceConfig
   *           the workspace configuration
   */
  public RestoreBean(String backupId,
                     String workspaceConfig) {
    this.backupId = backupId;
    this.workspaceConfig = workspaceConfig;
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
   * getWorkspaceConfig.
   *
   * @return String
   *           return the workspace configuration
   */
  public String getWorkspaceConfig() {
    return workspaceConfig;
  }

  /**
   * setWorkspaceConfig.
   *
   * @param workspaceConfig
   *          String, the workspace configuration
   */
  public void setWorkspaceConfig(String workspaceConfig) {
    this.workspaceConfig = workspaceConfig;
  }
}
