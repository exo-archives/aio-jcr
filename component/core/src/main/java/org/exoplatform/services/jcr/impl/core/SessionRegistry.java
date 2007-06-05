/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.impl.proccess.WorkerThread;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public final class SessionRegistry implements Startable {
  private final Map<String, SessionImpl> sessionsMap;

  // 1 min
  private int                            DEFAULT_CLEANER_TIMEOUT = 60 * 1000;

  protected static Log                   log                     = ExoLogger
                                                                     .getLogger("jcr.SessionRegistry");

  private SessionCleaner                 sessionCleaner;

  protected long                         timeOut;

  public SessionRegistry(RepositoryEntry entry) {
    sessionsMap = new WeakHashMap<String, SessionImpl>();
    if (entry != null) {
      this.timeOut = entry.getSessionTimeOut() > 0 ? entry.getSessionTimeOut() : 0;
    }
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

  public SessionImpl getSession(String sessionId) {
    return sessionsMap.get(sessionId);
  }
  
  public boolean isInUse(String workspaceName){
    if (workspaceName == null)
      return sessionsMap.size() > 0;
    for (SessionImpl session : sessionsMap.values()) {
      if (session.getWorkspace().getName().equals(workspaceName))
        return true;
    }
    return false;
  }
  public void start() {
    if (timeOut > 0)
      sessionCleaner = new SessionCleaner(DEFAULT_CLEANER_TIMEOUT, timeOut);
  }

  public void stop() {
    if (timeOut > 0)
      sessionCleaner.halt();
  }

  private class SessionCleaner extends WorkerThread {

    private long sessionTimeOut;

    public SessionCleaner(long workTime, long sessionTimeOut) {
      super(workTime);
      this.sessionTimeOut = sessionTimeOut;
      setName("SessionCleaner " + getId());
      setPriority(Thread.MIN_PRIORITY);
      start();
      log.info("SessionCleaner instantiated name= " + getName() + " workTime= " + workTime
          + " sessionTimeOut=" + sessionTimeOut);
    }

    @Override
    protected void callPeriodically() throws Exception {
      SessionImpl[] sessions;
      synchronized (sessionsMap) {
        sessionsMap.values().toArray(sessions = new SessionImpl[sessionsMap.size()]);
      }
      synchronized (sessions) {
        for (int i = 0; i < sessions.length; i++) {
          if (sessions[i].getLastAccessTime() + sessionTimeOut < System.currentTimeMillis()) {
            sessions[i].logout();
          }
        }
      }
    }
  }
}
