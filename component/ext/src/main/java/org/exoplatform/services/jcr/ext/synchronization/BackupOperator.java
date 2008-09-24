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
package org.exoplatform.services.jcr.ext.synchronization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.backup.BackupChain;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupConfigurationException;
import org.exoplatform.services.jcr.ext.backup.BackupJob;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.BackupOperationException;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.<br/>
 * 
 * Backup service API helper.
 * 
 * <br/>Date: 18.08.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: BackupOperator.java 111 2008-11-11 11:11:11Z peterit $
 */
public class BackupOperator {

  private Log                   log = ExoLogger.getLogger("ext.BackupOperator");

  protected final BackupManager backupManager;

  class Task {
    final String repository;

    final String workspace;

    final File   tmpdir;

    Task(String repository, String workspace, File tmpdir) {
      this.repository = repository;
      this.workspace = workspace;
      this.tmpdir = tmpdir;
    }

    InputStream getContent() throws FileNotFoundException {
      return new FileInputStream(new File(this.tmpdir, "")); // TODO
    }

    void close() {
      for (File f : this.tmpdir.listFiles()) {
        if (!f.delete())
          f.deleteOnExit(); // TODO make it more clean
      }
    }
  }

  BackupOperator(BackupManager backupManager) {
    this.backupManager = backupManager;
  }

  /**
   * Perform full backup and return backup task.
   * 
   * @return
   * @throws RepositoryConfigurationException
   * @throws BackupConfigurationException
   * @throws BackupOperationException
   * @throws Exception
   */
  public Task fullBackup(String repository, String workspace) throws RepositoryException,
                                                             BackupOperationException,
                                                             BackupConfigurationException,
                                                             RepositoryConfigurationException {
    // backup
    String tmpdir = System.getProperty("java.io.tmpdir");
    File backDir = new File(tmpdir, "syncservice-backup-tmpdir");
    backDir.mkdirs();

    BackupConfig config = new BackupConfig();
    config.setRepository(repository);
    config.setWorkspace(workspace);
    config.setBuckupType(BackupManager.FULL_BACKUP_ONLY);

    config.setBackupDir(backDir);

    backupManager.startBackup(config);

    BackupChain bch = backupManager.findBackup(repository, workspace);

    if (bch != null) {
      // wait till full backup will be stopped
      while (bch.getFullBackupState() != BackupJob.FINISHED) {
        Thread.yield();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // TODO
        }
      }

      // stop fullBackup
      backupManager.stopBackup(bch);

      return new Task(repository, workspace, backDir);
    } else {
      // log.info("Can't get fullBackup chain");
      throw new RepositoryException("Can't get fullBackup chain");
    }
  }

}
