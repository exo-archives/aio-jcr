/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.impl.proccess.WorkerThread;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public final class SessionRegistry implements Startable {
  private final Map<String, SessionImpl> sessionsMap;

  // 60 min
  private int                            DEFAULT_TIMEOUT = 60 * 60 * 1000;

  protected static Log                   log             = ExoLogger
                                                             .getLogger("jcr.SessionRegistry");

  private SessionCleaner                 sessionCleaner;

  protected long                         timeOut;

  public SessionRegistry(InitParams params) {
    sessionsMap = new WeakHashMap<String, SessionImpl>();
    if (params != null && params.getValueParam("session-max-age") != null) {
      int t = Integer.parseInt(params.getValueParam("session-max-age").getValue());
      log.debug("session-max-age="+t);
      this.timeOut = t > 0 ? t : DEFAULT_TIMEOUT;

    }

  }

  protected void setTimeOut(long timeOut) {
    this.timeOut = timeOut;
    sessionCleaner.setTimeOut(timeOut);
  }

  public void registerSession(SessionImpl session) {
    synchronized (sessionsMap) {
      sessionsMap.put(session.getId(), session);
    }
  }

  public void unregisterSession(String sessionId) {
    synchronized (sessionsMap) {
      sessionsMap.remove(sessionId);
    }
  }
  public SessionImpl  getSession(String sessionId) {
      return sessionsMap.get(sessionId);
  }
  public void start() {
    sessionCleaner = new SessionCleaner(timeOut);

  }

  public void stop() {
    sessionCleaner.halt();
  }

  private class SessionCleaner extends WorkerThread {
    private void setTimeOut(long timeOut) {
      this.timeout = timeOut;
    }

    public SessionCleaner(long timeout) {
      super(timeout);
      setName("SessionCleaner " + getId());
      setPriority(Thread.MIN_PRIORITY);
      start();
      log.info("SessionCleaner instantiated name= " + getName() + " timeout= " + timeout);
    }

    @Override
    protected void callPeriodically() throws Exception {
      SessionImpl[] sessions;
      synchronized (sessionsMap) {
        sessionsMap.values().toArray(sessions = new SessionImpl[sessionsMap.size()]);
      }
      synchronized (sessions) {
        for (int i = 0; i < sessions.length; i++) {
          if (sessions[i].getLastAccessTime() + timeout < System.currentTimeMillis()) {
            sessions[i].logout();
          }
        }
      }
    }
  }
}
