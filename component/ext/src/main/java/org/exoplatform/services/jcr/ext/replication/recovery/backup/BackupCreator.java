/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.recovery.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.replication.recovery.FileNameFactory;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */
public class BackupCreator implements Runnable {
  private static Log         log = ExoLogger.getLogger("ext.BackupCreator");

  private Thread               backupCreatorThread;

  private long                 delayTime;

  private String               workspaceName;

  private File                 backupDir;

  private ManageableRepository manageableRepository;

  private FileNameFactory      fileNameFactory;

  public BackupCreator(long delayTime,
                       String workspaceName,
                       File backupDir,
                       ManageableRepository manageableRepository) {
    this.delayTime = delayTime;
    this.workspaceName = workspaceName;
    this.backupDir = backupDir;
    this.manageableRepository = manageableRepository;

    fileNameFactory = new FileNameFactory();

    backupCreatorThread = new Thread(this, "BackupCreatorThread@"
        + manageableRepository.getConfiguration().getName() + ":" + workspaceName);
    backupCreatorThread.start();
  }

  public void run() {
    try {
      Thread.yield();
      Thread.sleep(delayTime);

      log.info("The backup has been started : " + manageableRepository.getConfiguration().getName()
          + "@" + workspaceName);

      SessionImpl session = (SessionImpl) manageableRepository.getSystemSession(workspaceName);

      Calendar backupTime = Calendar.getInstance();
      String fileName = manageableRepository.getConfiguration().getName() + "_" + workspaceName
          + "_" + fileNameFactory.getStrDate(backupTime) + "_"
          + fileNameFactory.getStrTime(backupTime) + ".xml";

      File backupFile = new File(backupDir.getCanonicalPath() + File.separator + fileName);

      if (backupFile.createNewFile()) {

        session.exportWorkspaceSystemView(new FileOutputStream(backupFile), false, false);

        log.info("The backup has been finished : "
            + manageableRepository.getConfiguration().getName() + "@" + workspaceName);
      } else
        throw new IOException("Can't create file : " + backupFile.getCanonicalPath());

    } catch (InterruptedException ie) {
      log.error("The InterruptedExeption", ie);
    } catch (RepositoryException e) {
      log.error("The RepositoryException", e);
    } catch (IOException e) {
      log.error("The IOException", e);
    }

  }
}
