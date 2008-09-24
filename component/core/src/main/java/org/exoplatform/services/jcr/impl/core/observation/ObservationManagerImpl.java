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
package org.exoplatform.services.jcr.impl.core.observation;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.core.SessionLifecycleListener;
import org.exoplatform.services.jcr.impl.util.EntityCollection;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: ObservationManagerImpl.java 12096 2008-03-19 11:42:40Z gazarenkov $
 */

public class ObservationManagerImpl implements ObservationManager, SessionLifecycleListener {

  protected String                   sessionId;

  private List<EventListener>        sessionListeners = new ArrayList<EventListener>();

  private ObservationManagerRegistry registry;

  /**
   * Protected constructor for subclasses
   * 
   * @param session
   */
  ObservationManagerImpl(ObservationManagerRegistry registry, String sessionId) {
    this.sessionId = sessionId;
    this.registry = registry;
  }

  /**
   * @see javax.jcr.observation.ObservationManager#addEventListener
   */
  public void addEventListener(EventListener listener,
                               int eventTypes,
                               String absPath,
                               boolean isDeep,
                               String[] uuid,
                               String[] nodeTypeName,
                               boolean noLocal) throws RepositoryException {

    registry.addEventListener(listener, new ListenerCriteria(eventTypes,
                                                             absPath,
                                                             isDeep,
                                                             uuid,
                                                             nodeTypeName,
                                                             noLocal,
                                                             sessionId));

    sessionListeners.add(listener);
  }

  /**
   * @see javax.jcr.observation.ObservationManager#removeEventListener
   */
  public void removeEventListener(EventListener listener) throws RepositoryException {
    registry.removeEventListener(listener);
    sessionListeners.remove(listener);
  }

  /**
   * @see javax.jcr.observation.ObservationManager#getRegisteredEventListeners
   */
  public EventListenerIterator getRegisteredEventListeners() throws RepositoryException {
    // return a personal copy of registered listeners, no concurrent modification exc will found
    return new EntityCollection(new ArrayList<EventListener>(sessionListeners));
  }

  // ************** SessionLifecycleListener ****************

  /*
   * @see
   * org.exoplatform.services.jcr.impl.core.SessionLifecycleListener#onCloseSession(org.exoplatform
   * .services.jcr.impl.core.SessionImpl)
   */
  public void onCloseSession(ExtendedSession targetSession) {
    // do nothing, as we need to listen events after the session was logout
  }

}
