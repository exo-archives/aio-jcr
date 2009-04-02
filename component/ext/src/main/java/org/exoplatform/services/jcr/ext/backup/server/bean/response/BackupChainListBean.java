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

import java.util.Collection;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 27.03.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: BackupChainListBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class BackupChainListBean {
  
  /**
   * The list of BackupChainBeen.
   */
  private Collection<BackupChainBean> backupChains;
  
  /**
   * BackupChainListBeen  constructor.
   *
   */
  public BackupChainListBean() {
    
  }
  
  /**
   * BackupChainListBeen  constructor.
   *
   * @param backupChains
   *          the list of BackupChainBeen.
   */
  public BackupChainListBean(Collection<BackupChainBean> backupChains) {
    this.backupChains = backupChains;
  }

  /**
   * getBackupChains.
   *
   * @return Collection
   *           return the list of BackupChainBeen.
   */
  public Collection<BackupChainBean> getBackupChains() {
    return backupChains;
  }

  /**
   * setBackupChains.
   *
   * @param backupChains
   *          Collection, the list of BackupChainBeen.
   */
  public void setBackupChains(Collection<BackupChainBean> backupChains) {
    this.backupChains = backupChains;
  }
  
}
