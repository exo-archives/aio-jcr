/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.impl.core.observation;

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
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.impl.util.EntityCollection;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: ActionLauncher.java 13421 2007-03-15 10:46:47Z geaz $
 */

public class ActionLauncher implements ItemsPersistenceListener {

  public final int SKIP_EVENT = Integer.MIN_VALUE; 
  
  private static Log log = ExoLogger.getLogger("jcr.ActionLauncher");

  private ObservationManagerRegistry observationRegistry;

  private WorkspacePersistentDataManager workspaceDataManager;

  public ActionLauncher(ObservationManagerRegistry registry,
      WorkspacePersistentDataManager workspaceDataManager) {
    this.observationRegistry = registry;
    this.workspaceDataManager = workspaceDataManager;
    workspaceDataManager.addItemPersistenceListener(this);
  }

  public void onSaveItems(ItemStateChangesLog changesLog) {
    EventListenerIterator eventListeners = observationRegistry.getEventListeners();

    while (eventListeners.hasNext()) { // changesLog.dump()

      EventListener listener = eventListeners.nextEventListener();
      ListenerCriteria criteria = observationRegistry.getListenerFilter(listener);

      EntityCollection events = new EntityCollection();
      
      ChangesLogIterator logIterator = ((CompositeChangesLog)changesLog).getLogIterator();
      while (logIterator.hasNextLog()) {
        
        PlainChangesLog subLog = logIterator.nextLog();
        String sessionId = subLog.getSessionId();

        for (ItemState itemState : subLog.getAllStates()) {
          if (itemState.isEventFire()) {

            ItemData item = itemState.getData();
            try {
              int eventType = eventType(itemState);
              if (eventType != SKIP_EVENT && isTypeMatch(criteria, eventType)
                  && isPathMatch(criteria, item) && isIdentifierMatch(criteria, item)
                  && isNodeTypeMatch(criteria, item)
                  && isSessionMatch(criteria, sessionId)) {

                String path = criteria.getSession().getLocationFactory()
                    .createJCRPath(item.getQPath()).getAsString(false);

                events.add(new EventImpl(eventType, path, criteria.getSession()
                    .getUserID()));
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
    if (criteria.getNoLocal() && criteria.getSession().getId().equals(sessionId))
      return false;
    return true;
  }

  private boolean isPathMatch(ListenerCriteria criteria, ItemData item) {
    if (criteria.getAbsPath() == null)
      return true;
    try {
      QPath cLoc = criteria.getSession().getLocationFactory()
          .parseAbsPath(criteria.getAbsPath()).getInternalPath();
      // 8.3.3 Only events whose associated parent node is at absPath (or
      // within its subtree, if isDeep is true) will be received.
      QPath itemPath = item.getQPath();
      if (item.isNode()) {
        boolean isAtAbsPathNode = cLoc.equals(itemPath);
        if (isAtAbsPathNode) {
          // This is a associated parent node
          return true; //state == Event.NODE_ADDED          
        } else if (criteria.isDeep()) {
          // For descendant
          return itemPath.isDescendantOf(cLoc, false);
        } else {
          // no path match
          return false;
        }
      } else {
        // property is descendant always
        return itemPath.isDescendantOf(cLoc, !criteria.isDeep());
      }
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
      else if (!item.isNode()
          && criteria.getIdentifier()[i].equals(item.getParentIdentifier()))
        return true;
    }
    return false;

  }

  private boolean isNodeTypeMatch(ListenerCriteria criteria, ItemData item)
      throws RepositoryException {
    if (criteria.getNodeTypeName() == null)
      return true;

    NodeData node = (NodeData) workspaceDataManager.getItemData(item.getParentIdentifier());
    if (node == null) {
      return false;
    }
    
    NodeTypeManagerImpl ntManager = criteria.getSession().getWorkspace().getNodeTypeManager();

    for (int i = 0; i < criteria.getNodeTypeName().length; i++) {
//      if (isType(node, criteria.getNodeTypeName()[i], criteria.getSession()))
      ExtendedNodeType criteriaNT = (ExtendedNodeType) ntManager.getNodeType(criteria.getNodeTypeName()[i]);
      InternalQName[] testQNames;
      if(criteriaNT.isMixin()) {
        testQNames = node.getMixinTypeNames();
      } else {
        testQNames = new InternalQName[1];
        testQNames[0] = node.getPrimaryTypeName();
      }
      if(ntManager.isNodeType(criteriaNT.getQName(), testQNames))
        return true;
    }
    return false;
  }

  private int eventType(ItemState state) throws RepositoryException {
    
    if(state.getData().isNode()) {
      if (state.isAdded() || state.isOrderAdded()) 
        return Event.NODE_ADDED;
      else if (state.isDeleted()|| state.isOrderDeleted())
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
    throw new RepositoryException("Unexpected ItemState for Node " + ItemState.nameFromValue(state.getState())
         + " " + state.getData().getQPath().getAsString());
  }

}
