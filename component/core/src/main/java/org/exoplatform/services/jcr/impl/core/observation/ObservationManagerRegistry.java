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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.impl.util.EntityCollection;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id$
 */

public class ObservationManagerRegistry {

  protected static Log                           log = ExoLogger.getLogger("jcr.RepositoryService");

  protected Map<EventListener, ListenerCriteria> listenersMap;

  protected ActionLauncher                       launcher;

  public ObservationManagerRegistry(WorkspacePersistentDataManager workspaceDataManager,
                                    SessionRegistry sessionRegistry) {

    this.listenersMap = new HashMap<EventListener, ListenerCriteria>();
    this.launcher = new ActionLauncher(this, workspaceDataManager, sessionRegistry);
  }

  public ObservationManagerImpl createObservationManager(SessionImpl session) {
    return new ObservationManagerImpl(this, session.getId());
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
      if (criteria.getSessionId().equals(session.getId())) {
        eventsForRemove.add(listener);
      }
    }
    for (EventListener listener : eventsForRemove) {
      listenersMap.remove(listener);
    }
  }

}
