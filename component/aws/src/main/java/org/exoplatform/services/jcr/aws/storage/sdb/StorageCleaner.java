/**
 * 
 */
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
package org.exoplatform.services.jcr.aws.storage.sdb;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * It's a temporary solution... IMO. it will be better to delete after each commit in independent
 * thread. Comming soon...
 * 
 * <br/>
 * The cleaner will runs with random (around container constant) time to prevent prbs in cluster
 * environment.
 * 
 * <br/>
 * Date: 15.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class StorageCleaner extends Thread {

  /**
   * Container logger.
   */
  protected static final Log                  LOG = ExoLogger.getLogger("jcr.StorageCleaner");

  /**
   * Active status.
   */
  private volatile boolean                    run = true;

  /**
   * Wait timeout.
   */
  private final int                           timeout;

  /**
   * SDB Connection.
   */
  private final SDBWorkspaceStorageConnection sdbConn;

  /**
   * StorageCleaner constructor.
   * 
   * @param sdbConn
   *          SDB Connection
   * @param containerName
   *          container name
   * @param timeout
   *          timeout to wait
   */
  StorageCleaner(String containerName, SDBWorkspaceStorageConnection sdbConn, int timeout) {
    setDaemon(true);
    setName("JCRSimpleDBStorageCleaner_" + containerName);

    this.sdbConn = sdbConn;

    this.timeout = timeout;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run() {
    while (run) {
      try {
        // make the cleaner period a bit randomized
        Random rnd = new Random();
        int rndPart = rnd.nextInt(timeout);
        Thread.sleep(timeout + rndPart); // one or two or... smth between
      } catch (Throwable e) {
        LOG.error("Storage cleaner wait is interrupted " + e, e);
      }

      try {
        sdbConn.runCleanup();
      } catch (Throwable e) {
        LOG.error("Storage cleaner error " + e + ". Continue execution.", e);
      }
    }
  }

  /**
   * Signal that cleanup should be canceled.
   */
  public void cancel() {
    this.run = false;
  }

}
