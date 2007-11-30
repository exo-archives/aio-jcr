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

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.impl.proccess.WorkerThread;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id$
 */
public final class SessionRegistry implements Startable {
  private final Map<String, SessionImpl> sessionsMap;

  // 1 min
  public final static int             DEFAULT_CLEANER_TIMEOUT = 60 * 1000;

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
    if (workspaceName == null){
      log.info("Session in use "+sessionsMap.size());
      return sessionsMap.size() > 0;
    }
    for (SessionImpl session : sessionsMap.values()) {
      if (session.getWorkspace().getName().equals(workspaceName)){
        log.info("Session for workspace "+workspaceName+" in use." +
        		" Session id:"+session.getId()+ " user: "+session.getUserID());
        return true;
      }
    }
    return false;
  }
  
  public void start() {
    sessionsMap.clear();
    
    if (timeOut > 0)
      sessionCleaner = new SessionCleaner(DEFAULT_CLEANER_TIMEOUT, timeOut);
  }

  public void stop() {
    if (timeOut > 0 && sessionCleaner != null)
      sessionCleaner.halt();
    sessionsMap.clear();
  }

  private class SessionCleaner extends WorkerThread {

    private final long sessionTimeOut;

    public SessionCleaner(long workTime, long sessionTimeOut) {
      super(workTime);
      this.sessionTimeOut = sessionTimeOut;
      setName("SessionCleaner " + getId());
      setPriority(Thread.MIN_PRIORITY);
      setDaemon(true);
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
