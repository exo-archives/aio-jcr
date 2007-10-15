/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.observation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.SessionLifecycleListener;
import org.exoplatform.services.jcr.impl.util.EntityCollection;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: ObservationManagerImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class ObservationManagerImpl implements ObservationManager, SessionLifecycleListener {

  protected String sessionId;
  
  private List<EventListener> sessionListeners = new ArrayList<EventListener>();
  
  private ObservationManagerRegistry registry;
  
  /**
   * Protected constructor for subclasses
   * @param session
   */
  ObservationManagerImpl(ObservationManagerRegistry registry, String sessionId) {
    this.sessionId = sessionId;
    this.registry = registry;
  }


  /**
   * @see javax.jcr.observation.ObservationManager#addEventListener
   */
  public void addEventListener(EventListener listener, int eventTypes,
      String absPath, boolean isDeep, String[] uuid, String[] nodeTypeName,
      boolean noLocal) throws RepositoryException {
    
    registry.addEventListener(listener, new ListenerCriteria(eventTypes, absPath,
        isDeep, uuid, nodeTypeName, noLocal, sessionId));
    
    sessionListeners.add(listener);
  }

  /**
   * @see javax.jcr.observation.ObservationManager#removeEventListener
   */
  public void removeEventListener(EventListener listener)
      throws RepositoryException {
    registry.removeEventListener(listener);
    sessionListeners.remove(listener);
  }

  /**
   * @see javax.jcr.observation.ObservationManager#getRegisteredEventListeners
   */
  public EventListenerIterator getRegisteredEventListeners()
      throws RepositoryException {
    // return a personal copy of registered listeners, no concurrent modification exc will found 
    return new EntityCollection(new ArrayList<EventListener>(sessionListeners));
  }
  
  // ************** SessionLifecycleListener ****************
  
  /* 
   * @see org.exoplatform.services.jcr.impl.core.SessionLifecycleListener#onCloseSession(org.exoplatform.services.jcr.impl.core.SessionImpl)
   */
  public void onCloseSession(SessionImpl targetSession) {
    // do nothing, as we need to listen events after the session was logout 
  }
  
}