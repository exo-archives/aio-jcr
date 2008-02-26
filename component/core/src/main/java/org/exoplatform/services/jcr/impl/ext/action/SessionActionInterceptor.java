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
package org.exoplatform.services.jcr.impl.ext.action;

import java.util.Iterator;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.command.action.ActionCatalog;
import org.exoplatform.services.command.action.Condition;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: SessionActionInterceptor.java 13421 2007-03-15 10:46:47Z geaz $
 */

public class SessionActionInterceptor {

  private final ActionCatalog catalog;

  private final ExoContainer  container;

  private static Log          log        = ExoLogger.getLogger("jcr.SessionActionInterceptor");

  /**
   * SessionActionInterceptor is per session, and only one action per
   * session/time can be active.
   */
  private ItemImpl            activeItem = null;

  public SessionActionInterceptor(ActionCatalog catalog, ExoContainer container) {
    this.catalog = catalog;
    this.container = container;
  }

  /**
   * Gather information about add mixin action
   * 
   * @param node
   * @param mixinType
   * @throws RepositoryException
   */
  public void postAddMixin(NodeImpl node, InternalQName mixinType) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = node;
    else
      return;

    try {
      Condition conditions = new Condition();
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.ADD_MIXIN);
      conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, new InternalQName[] { mixinType });

      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, node);
      ctx.put(InvocationContext.EVENT, ExtendedEvent.ADD_MIXIN);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  public void postAddNode(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = node;
    else
      return;

    try {
      Condition conditions = new Condition();
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.NODE_ADDED);
      conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames((NodeData) node.getData()));
      
      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, node);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      ctx.put(InvocationContext.EVENT, ExtendedEvent.NODE_ADDED);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  public void postCheckin(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = node;
    else
      return;

    try {
      Condition conditions = new Condition();
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.CHECKIN);
      conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames((NodeData) node.getData()));

      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, node);
      ctx.put(InvocationContext.EVENT, ExtendedEvent.CHECKIN);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  public void postCheckout(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = node;
    else
      return;

    try {
      Condition conditions = new Condition();
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.CHECKOUT);
      conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames((NodeData) node.getData()));

      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, node);
      ctx.put(InvocationContext.EVENT, ExtendedEvent.CHECKOUT);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  public void postLock(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = node;
    else
      return;

    try {
      Condition conditions = new Condition();
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.LOCK);
      conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames((NodeData) node.getData()));

      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, node);
      ctx.put(InvocationContext.EVENT, ExtendedEvent.LOCK);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  public void postRead(ItemImpl item) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = item;
    else
      return;

    try {
      Condition conditions = new Condition();
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.READ);
      conditions.put(SessionEventMatcher.PATH_KEY, item.getInternalPath());
      
      if (item.isNode()) {
        conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames((NodeData) item.getData()));
      } else {
        conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames(item.parentData()));
      }

      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, item);
      ctx.put(InvocationContext.EVENT, ExtendedEvent.READ);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  public void postSetProperty(PropertyImpl property, int state) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = property;
    else
      return;

    try {
      int event = -1;
      switch (state) {
      case ItemState.ADDED:
        event = ExtendedEvent.PROPERTY_ADDED;
        break;
      case ItemState.UPDATED:
        event = ExtendedEvent.PROPERTY_CHANGED;
        break;
      case ItemState.DELETED:
        event = ExtendedEvent.PROPERTY_REMOVED;
        break;
      default:
        return;
      }

      Condition conditions = new Condition();
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, event);
      conditions.put(SessionEventMatcher.PATH_KEY, property.getInternalPath());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames(property.parentData()));

      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, property);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      ctx.put(InvocationContext.EVENT, event);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  public void postUnlock(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = node;
    else
      return;

    try {
      Condition conditions = new Condition();
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.UNLOCK);
      conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames((NodeData) node.getData()));

      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, node);
      ctx.put(InvocationContext.EVENT, ExtendedEvent.UNLOCK);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  public void preRemoveItem(ItemImpl item) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = item;
    else
      return;

    try {
      Condition conditions = new Condition();
      int event = item.isNode() ? ExtendedEvent.NODE_REMOVED : ExtendedEvent.PROPERTY_REMOVED;
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, event);
      conditions.put(SessionEventMatcher.PATH_KEY, item.getInternalPath());
      if (item.isNode()) {
        conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames((NodeData) item.getData()));
      } else {
        conditions.put(SessionEventMatcher.NODETYPES_KEY, readNodeTypeNames(item.parentData()));
      }

      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, item);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      ctx.put(InvocationContext.EVENT, event);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  public void preRemoveMixin(NodeImpl node, InternalQName mixinType) throws RepositoryException {
    if (catalog == null)
      return;

    if (activeItem == null)
      activeItem = node;
    else
      return;

    try {
      Condition conditions = new Condition();
      conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.REMOVE_MIXIN);
      conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, new InternalQName[] { mixinType });

      InvocationContext ctx = new InvocationContext();
      ctx.put(InvocationContext.CURRENT_ITEM, node);
      ctx.put(InvocationContext.EVENT, ExtendedEvent.REMOVE_MIXIN);
      ctx.put(InvocationContext.EXO_CONTAINER, container);
      launch(conditions, ctx);
    } finally {
      activeItem = null;
    }
  }

  private InternalQName[] readNodeTypeNames(NodeData node) {
    InternalQName primaryTypeName = node.getPrimaryTypeName();
    InternalQName[] mixinNames = node.getMixinTypeNames();
    InternalQName[] nodeTypeNames = new InternalQName[mixinNames.length + 1];

    nodeTypeNames[0] = primaryTypeName;
    for (int i = 1; i <= mixinNames.length; i++) {
      nodeTypeNames[i] = mixinNames[i - 1];
    }
    
    return nodeTypeNames; 
  }

  protected final void launch(Condition conditions, InvocationContext context) {
    Set<Action> cond = catalog.getActions(conditions);
    if (cond != null) {
      Iterator<Action> i = cond.iterator();
      while (i.hasNext()) {
        try {
          i.next().execute(context);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
