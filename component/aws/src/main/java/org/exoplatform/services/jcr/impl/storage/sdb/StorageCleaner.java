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
package org.exoplatform.services.jcr.impl.storage.sdb;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: StorageCleaner.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class StorageCleaner extends Thread {

  /**
   * Container logger.
   */
  protected static final Log                 LOG                     = ExoLogger.getLogger("jcr.StorageCleaner");
  
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
   * @param timeout
   *          timeout to wait
   */
  StorageCleaner(SDBWorkspaceStorageConnection sdbConn, int timeout) {
    setDaemon(true);

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
        sdbConn.runCleanup();
      } catch (Throwable e) {
        LOG.error("Storage cleaner error " + e + ". Continue execution.", e);
      }

      try {
        wait(timeout);
      } catch (InterruptedException e) {
        LOG.error("Storage cleaner wait is interrupted " + e, e);
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
