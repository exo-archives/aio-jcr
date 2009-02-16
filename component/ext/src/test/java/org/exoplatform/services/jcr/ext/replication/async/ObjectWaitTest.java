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
package org.exoplatform.services.jcr.ext.replication.async;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ObjectWaitTest.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class ObjectWaitTest extends TestCase {

  class Waiter extends Thread {

    final Object lock = new Object();

    Exception    error;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      int i = 0;
      while (true) {
        synchronized (lock) {
          try {
            lock.wait();

            System.out.println("Unlocked for the moment " + (i++));
          } catch (InterruptedException e) {
            e.printStackTrace();
            error = e;
          }
        }
      }
    }

    void check() {
      synchronized (lock) {
        lock.notify();
      }
    }
  }

  public void testWait() throws InterruptedException {
    Waiter w = new Waiter();
    w.start();

    try {
      for (int i = 0; i < 3; i++) {
        Thread.sleep(200);
        w.check();
      }
    } catch (Throwable e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    assertNull("error " + w.error, w.error);
  }

}
