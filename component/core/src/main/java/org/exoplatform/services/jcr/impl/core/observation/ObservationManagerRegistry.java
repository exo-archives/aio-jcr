/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.observation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.impl.util.EntityCollection;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ObservationManagerRegistry.java 4025 2006-02-06 09:12:12Z
 *          peterit $
 */

public class ObservationManagerRegistry {
  
  protected static Log log = ExoLogger.getLogger("jcr.RepositoryService");

  protected Map<EventListener, ListenerCriteria> listenersMap;

  protected ActionLauncher                       launcher;

  public ObservationManagerRegistry(WorkspacePersistentDataManager workspaceDataManager) {

    this.listenersMap = new HashMap<EventListener, ListenerCriteria>();
    this.launcher = new ActionLauncher(this, workspaceDataManager);
  }

  public ObservationManagerImpl createObservationManager(SessionImpl session) {
    return new ObservationManagerImpl(this, session);
  }

  public void addEventListener(EventListener listener, ListenerCriteria filter) {
    listenersMap.put(listener, filter);
  }

  public void removeEventListener(EventListener listener) {
    listenersMap.remove(listener);
  }

  public EventListenerIterator getEventListeners() {
    return new EntityCollection(listenersMap.keySet());
  }

  public ListenerCriteria getListenerFilter(EventListener listener) {
    return listenersMap.get(listener);
  }

  public void removeSessionEventListeners(SessionImpl session) {
    // Iterating without ConcurrentModificationException
    List<EventListener> eventsForRemove = new ArrayList<EventListener>();

    for (EventListener listener : listenersMap.keySet()) {
      ListenerCriteria criteria = listenersMap.get(listener);
      if (criteria.getSession() == session) {
        eventsForRemove.add(listener);
      }
    }
    for (EventListener listener : eventsForRemove) {
      listenersMap.remove(listener);
    }
    
  }

}
