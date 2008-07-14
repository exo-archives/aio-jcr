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

  private int    buckupType;

  private String repository;

  private String workspace;

  private long   incrementalJobPeriod;

  private int    incrementalJobNumber;

  private File   backupDir;

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

  public File getBackupDir() {
    return backupDir;
  }

  public void setBackupDir(File backupDir) {
    this.backupDir = backupDir;
  }

  public int getBuckupType() {
    return buckupType;
  }

  public void setBuckupType(int buckupType) {
    this.buckupType = buckupType;
  }
}
