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

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.impl.util.EntityCollection;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id$
 */
public class ActionLauncher implements ItemsPersistenceListener {

  public final int                             SKIP_EVENT = Integer.MIN_VALUE;

  private final Log                            log        = ExoLogger.getLogger("jcr.ActionLauncher");

  private final ObservationManagerRegistry     observationRegistry;

  private final WorkspacePersistentDataManager workspaceDataManager;

  private final SessionRegistry                sessionRegistry;

  public ActionLauncher(ObservationManagerRegistry registry,
                        WorkspacePersistentDataManager workspaceDataManager,
                        SessionRegistry sessionRegistry) {
    this.observationRegistry = registry;
    this.workspaceDataManager = workspaceDataManager;
    this.sessionRegistry = sessionRegistry;
    this.workspaceDataManager.addItemPersistenceListener(this);
  }

  public void onSaveItems(ItemStateChangesLog changesLog) {
    EventListenerIterator eventListeners = observationRegistry.getEventListeners();

    while (eventListeners.hasNext()) {

      EventListener listener = eventListeners.nextEventListener();
      ListenerCriteria criteria = observationRegistry.getListenerFilter(listener);

      EntityCollection events = new EntityCollection();

      ChangesLogIterator logIterator = ((CompositeChangesLog) changesLog).getLogIterator();
      while (logIterator.hasNextLog()) {

        PlainChangesLog subLog = logIterator.nextLog();
        String sessionId = subLog.getSessionId();

        SessionImpl userSession = sessionRegistry.getSession(sessionId);

        if (userSession != null)
          for (ItemState itemState : subLog.getAllStates()) {
            if (itemState.isEventFire()) {

              ItemData item = itemState.getData();
              try {
                int eventType = eventType(itemState);
                if (eventType != SKIP_EVENT && isTypeMatch(criteria, eventType)
                    && isPathMatch(criteria, item, userSession)
                    && isIdentifierMatch(criteria, item)
                    && isNodeTypeMatch(criteria, item, userSession, subLog)
                    && isSessionMatch(criteria, sessionId)) {

                  String path = userSession.getLocationFactory()
                                           .createJCRPath(item.getQPath())
                                           .getAsString(false);

                  events.add(new EventImpl(eventType, path, userSession.getUserID()));
                }
              } catch (RepositoryException e) {
                log.error("Can not fire ActionLauncher.onSaveItems() for "
                    + item.getQPath().getAsString() + " reason: " + e);
              }
            }
          }
      }
      if (events.size() > 0) {
        // TCK says, no events - no onEvent() action
        listener.onEvent(events);
      }
    }
  }

  // ---------------------------------

  private boolean isTypeMatch(ListenerCriteria criteria, int state) {
    return (criteria.getEventTypes() & state) > 0;
  }

  private boolean isSessionMatch(ListenerCriteria criteria, String sessionId) {
    if (criteria.getNoLocal() && criteria.getSessionId().equals(sessionId))
      return false;
    return true;
  }

  private boolean isPathMatch(ListenerCriteria criteria, ItemData item, SessionImpl userSession) {
    if (criteria.getAbsPath() == null)
      return true;
    try {
      QPath cLoc = userSession.getLocationFactory()
                              .parseAbsPath(criteria.getAbsPath())
                              .getInternalPath();

      // 8.3.3 Only events whose associated parent node is at absPath (or
      // within its subtree, if isDeep is true) will be received.

      QPath itemPath = item.getQPath();

      return itemPath.isDescendantOf(cLoc, !criteria.isDeep());// TODO is Child
    } catch (RepositoryException e) {
      return false;
    }
  }

  private boolean isIdentifierMatch(ListenerCriteria criteria, ItemData item) {

    if (criteria.getIdentifier() == null)
      return true;

    // assotiated parent is node itself for node and parent for property ????
    for (int i = 0; i < criteria.getIdentifier().length; i++) {
      if (item.isNode() && criteria.getIdentifier()[i].equals(item.getIdentifier()))
        return true;
      else if (!item.isNode() && criteria.getIdentifier()[i].equals(item.getParentIdentifier()))
        return true;
    }
    return false;

  }

  private boolean isNodeTypeMatch(ListenerCriteria criteria,
                                  ItemData item,
                                  SessionImpl userSession,
                                  PlainChangesLog changesLog) throws RepositoryException {
    if (criteria.getNodeTypeName() == null)
      return true;

    NodeData node = (NodeData) workspaceDataManager.getItemData(item.getParentIdentifier());
    if (node == null) {
      // check if parent exists in changes log
      List<ItemState> states = changesLog.getAllStates();
      for (int i = states.size() - 1; i >= 0; i--) {
        if (states.get(i).getData().getIdentifier().equals(item.getParentIdentifier())) {
          // parent found
          node = (NodeData) states.get(i).getData();
          break;
        }
      }

      if (node == null) {
        log.warn("Item's "
            + item.getQPath().getAsString()
            + " associated parent ("
            + item.getParentIdentifier()
            + ") can't be found nor in workspace nor in current changes. Nodetype filter is rejected.");
        return false;
      }
    }

    NodeTypeManagerImpl ntManager = userSession.getWorkspace().getNodeTypeManager();

    for (int i = 0; i < criteria.getNodeTypeName().length; i++) {
      ExtendedNodeType criteriaNT = (ExtendedNodeType) ntManager.getNodeType(criteria.getNodeTypeName()[i]);
      InternalQName[] testQNames;
      if (criteriaNT.isMixin()) {
        testQNames = node.getMixinTypeNames();
      } else {
        testQNames = new InternalQName[1];
        testQNames[0] = node.getPrimaryTypeName();
      }
      if (ntManager.isNodeType(criteriaNT.getQName(), testQNames))
        return true;
    }
    return false;
  }

  private int eventType(ItemState state) throws RepositoryException {

    if (state.getData().isNode()) {
      if (state.isAdded() || state.isRenamed() || state.isUpdated())
        return Event.NODE_ADDED;
      else if (state.isDeleted())
        return Event.NODE_REMOVED;
      else if (state.isUpdated())
        return SKIP_EVENT;
      else if (state.isUnchanged())
        return SKIP_EVENT;
    } else { // property
      if (state.isAdded())
        return Event.PROPERTY_ADDED;
      else if (state.isDeleted())
        return Event.PROPERTY_REMOVED;
      else if (state.isUpdated())
        return Event.PROPERTY_CHANGED;
      else if (state.isUnchanged())
        return SKIP_EVENT;
    }
    throw new RepositoryException("Unexpected ItemState for Node "
        + ItemState.nameFromValue(state.getState()) + " "
        + state.getData().getQPath().getAsString());
  }
}
