/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.backup;

import java.io.File;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class BackupConfig {
  
  private String fullBackupType;
  
  private String incrementalBackupType;

  private String repository;

  private String workspace;

  private long incrementalJobPeriod;

  private int incrementalJobNumber;

  //private String chainLogFile;
  
  private File backupDir;

  public long getIncrementalJobPeriod() {
    return incrementalJobPeriod;
  }

  public void setIncrementalJobPeriod(long incrementalJobPeriod) {
    this.incrementalJobPeriod = incrementalJobPeriod;
  }

  public int getIncrementalJobNumber() {
    return incrementalJobNumber;
  }

  public void setIncrementalJobNumber(int incrementalJobNumber) {
    this.incrementalJobNumber = incrementalJobNumber;
  }

  public String getFullBackupType() {
    return fullBackupType;
  }

  public void setFullBackupType(String fullBackupType) {
    this.fullBackupType = fullBackupType;
  }

  public String getIncrementalBackupType() {
    return incrementalBackupType;
  }

  public void setIncrementalBackupType(String incrementalBackupType) {
    this.incrementalBackupType = incrementalBackupType;
  }

  public String getRepository() {
    return repository;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public String getWorkspace() {
    return workspace;
  }

  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }

//  public String getChainLogFile() {
//    return chainLogFile;
//  }
//
//  public void setChainLogFile(String chainFile) {
//    this.chainLogFile = chainFile;
//  }
  
  public File getBackupDir() {
    return backupDir;
  }

  public void setBackupDir(File backupDir) {
    this.backupDir = backupDir;
  }

}
