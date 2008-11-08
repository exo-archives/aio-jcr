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
package org.exoplatform.services.jcr.impl.core;

import javax.jcr.Session;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

/**
 * A framework for detecting JCR session leaks.
 * 
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SessionReference extends WeakReference<Session> {

  //
  private static final int INITIAL_DELAY = 10;
  private static final int DELAY = 15;

  //
  private static ScheduledExecutorService executor;
  private static ConcurrentHashMap<Object, SessionReference> objects;
  private static boolean started = false;
  private static long maxAgeMillis_;
  
  public synchronized static void start(long maxAgeMillis) {
    if (!started) {
      if (maxAgeMillis < 0) {
        throw new IllegalStateException("Wrong max age value " + maxAgeMillis);
      }
      objects = new ConcurrentHashMap<Object, SessionReference>();
      executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleWithFixedDelay(detectorTask, INITIAL_DELAY, DELAY, TimeUnit.SECONDS);
      maxAgeMillis_ = maxAgeMillis;
      started = true;
    }
  }

  public synchronized static boolean isStarted() {
    return started;
  }

  private static final Runnable detectorTask = new Runnable() {
    public void run() {
      System.out.println("Starting detector task");

      //
      ArrayList<SessionReference> list;
      synchronized (SessionReference.class) {
        list = new ArrayList<SessionReference>(objects.values());
      }

      //
      for (SessionReference ref : list) {

        // It is closed we remove it
        if (ref.closed) {
          objects.remove(ref.key);
        } else {
          // We get the maybe null session
          Session session = ref.get();

          //
          String error = null;
          if (session == null) {
            // we can consider it is expired and was not closed
            error = "garbagednotclosed";
          } else if (ref.timestamp < System.currentTimeMillis()) {
            // it was not closed but we consider it is way too old
            error = "expired";
          } 

          //
          if (error != null) {
            objects.remove(ref.key);
            Exception e = new Exception();
            e.setStackTrace(ref.stack);
            System.out.println("<" + error + ">");
            e.printStackTrace();
            System.out.println("</" + error + ">");
          }
        }
      }
      System.out.println("Finished detector task");
    }
  };

  private final StackTraceElement[] stack = new Exception().getStackTrace();
  private final Object key = new Object();
  private final long timestamp = System.currentTimeMillis() + maxAgeMillis_;
  volatile boolean closed = false;

  SessionReference(Session referent) {
    super(referent);
    objects.put(key, this);
  }
}
