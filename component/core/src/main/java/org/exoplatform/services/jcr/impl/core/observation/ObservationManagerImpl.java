/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.observation;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.SessionLifecycleListener;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: ObservationManagerImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class ObservationManagerImpl implements ObservationManager, SessionLifecycleListener {

  protected SessionImpl session;
  
  private ObservationManagerRegistry registry;
  
  /**
   * Protected constructor for subclasses
   * @param session
   */
  ObservationManagerImpl(ObservationManagerRegistry registry, SessionImpl session) {
    this.session = session;
    this.registry = registry;
  }


  /**
   * @see javax.jcr.observation.ObservationManager#addEventListener
   */
  public void addEventListener(EventListener listener, int eventTypes,
      String absPath, boolean isDeep, String[] uuid, String[] nodeTypeName,
      boolean noLocal) throws RepositoryException {
    registry.addEventListener(listener, new ListenerCriteria(eventTypes, absPath,
        isDeep, uuid, nodeTypeName, noLocal, session));
  }

  /**
   * @see javax.jcr.observation.ObservationManager#removeEventListener
   */
  public void removeEventListener(EventListener listener)
      throws RepositoryException {
    registry.removeEventListener(listener);
  }

  /**
   * @see javax.jcr.observation.ObservationManager#getRegisteredEventListeners
   */
  public EventListenerIterator getRegisteredEventListeners()
      throws RepositoryException {
    return registry.getEventListeners();
  }
  
  /**
   * @return Returns the session.
   */
  public SessionImpl getSession() {
    return session;
  }
  
//  /**
//   * @param session The session to set.
//   */
//  public void setSession(SessionImpl session) {
//    this.session = session;
//  }

  // ************** SessionLifecycleListener ****************
  
  /* 
   * @see org.exoplatform.services.jcr.impl.core.SessionLifecycleListener#onCloseSession(org.exoplatform.services.jcr.impl.core.SessionImpl)
   */
  public void onCloseSession(SessionImpl targetSession) {
    // clear all event listeners on session created this manager
    if (this.getSession().getId() == targetSession.getId()) {
      registry.removeSessionEventListeners(targetSession);
    }
    session = null;
  }
  
}