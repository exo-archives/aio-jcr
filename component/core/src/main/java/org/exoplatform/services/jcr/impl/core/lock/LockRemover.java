/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.core.lock;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.impl.proccess.WorkerThread;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id$
 */
public class LockRemover extends WorkerThread {

  private final Log             log                    = ExoLogger.getLogger("jcr.lock.LockRemover");

  public static final long      DEFAULT_THREAD_TIMEOUT = 30000;                                      // 30

  // sec

  private final LockManagerImpl lockManagerImpl;

  public LockRemover(LockManagerImpl lockManagerImpl) {
    this(lockManagerImpl, DEFAULT_THREAD_TIMEOUT);
  }

  private LockRemover(LockManagerImpl lockManagerImpl, long timeout) {
    super(timeout);
    this.lockManagerImpl = lockManagerImpl;
    setName("LockRemover " + getId());
    setPriority(Thread.MIN_PRIORITY);
    setDaemon(true);
    start();
    if (log.isDebugEnabled())
      log.debug("LockRemover instantiated name= " + getName() + " timeout= " + timeout);
  }

  @Override
  protected void callPeriodically() throws Exception {
    lockManagerImpl.removeExpired();
  }
}
