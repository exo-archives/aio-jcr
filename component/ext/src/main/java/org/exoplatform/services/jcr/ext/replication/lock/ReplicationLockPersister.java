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
package org.exoplatform.services.jcr.ext.replication.lock;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.ReplicationService;
import org.exoplatform.services.jcr.impl.core.lock.FileSystemLockPersister;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ReplicationLockPersister.java 111 2008-11-11 11:11:11Z rainf0x $
 */
/**
 * @author rainf0x
 */
public class ReplicationLockPersister extends FileSystemLockPersister {
  private final ReplicationService replicationService;

  private final Log                log = ExoLogger.getLogger("ext.ReplicationLockPersister");

  private Thread                   delayStarterThread;

  public ReplicationLockPersister(WorkspacePersistentDataManager dataManager,
                                  WorkspaceEntry config,
                                  ReplicationService service) throws RepositoryConfigurationException,
      RepositoryException {
    super(dataManager, config);
    log.info("init");

    replicationService = service;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.lock.FileSystemLockPersister#start()
   */
  public void start() {
    log.info("start");

    delayStarterThread = new Thread(new DelaySatrter());
    delayStarterThread.start();
  }

  private void superStart() {
    super.start();
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.lock.FileSystemLockPersister#stop()
   */
  public void stop() {
    log.info("stop");
  }

  class DelaySatrter implements Runnable {
    public void run() {
      try {
        while (!replicationService.isStarted())
          Thread.sleep(250);

        superStart();
      } catch (InterruptedException ie) {
        log.error("Can not start", ie);
      }
    }
  }
}
