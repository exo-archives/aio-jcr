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
 * @version $Id: BackupBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class BackupBean {

  /**
   * The backup identifier.
   */
  String backupId;
  
  /**
   * BackupBeen  constructor.
   * Empty constructor.
   */
  public BackupBean() {
  }
  
  /**
   * BackupBeen  constructor.
   *
   * @param backupId
   *          the backup identifier
   */
  public BackupBean(String backupId) {
    this.backupId = backupId;
  }

  /**
   * getBackupId.
   *
   * @return String
   *           the backup identifier
   */
  public String getBackupId() {
    return backupId;
  }

  /**
   * setBackupId.
   *
   * @param backupId
   *          the backup identifier
   */
  public void setBackupId(String backupId) {
    this.backupId = backupId;
  }
  
}
