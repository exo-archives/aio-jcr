/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: SessionActionInterceptor.java 13421 2007-03-15 10:46:47Z geaz $
 */

public class SessionActionInterceptor {

  private final ActionCatalog catalog;

  private final ExoContainer  container;

  private static Log          log = ExoLogger.getLogger(SessionActionInterceptor.class);

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

    Condition conditions = new Condition();
    conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.ADD_MIXIN);
    conditions.put(SessionEventMatcher.NODETYPE_KEY, mixinType);

    conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(node.getAllNodeTypes()));
    conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, node.getAllNodeTypes());

    conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());

    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", node);
    ctx.put("event", ExtendedEvent.ADD_MIXIN);
    ctx.put("exocontainer", container);
    launch(conditions, ctx);
  }

  public void postAddNode(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;
    Condition conditions = new Condition();
    conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.NODE_ADDED);
    conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());

    conditions.put(SessionEventMatcher.NODETYPE_KEY, ((ExtendedNodeType) node.getPrimaryNodeType())
        .getQName());

    conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(node.getAllNodeTypes()));
    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", node);
    ctx.put("exocontainer", container);
    ctx.put("event", ExtendedEvent.NODE_ADDED);
    launch(conditions, ctx);
  }

  public void postCheckin(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;

    Condition conditions = new Condition();
    conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.CHECKIN);
    conditions.put(SessionEventMatcher.NODETYPE_KEY, ((ExtendedNodeType) node.getPrimaryNodeType())
        .getQName());
    conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, ((NodeImpl) node.getParent())
        .getAllNodeTypes());
    conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(node.getAllNodeTypes()));
    conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());

    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", node);
    ctx.put("event", ExtendedEvent.CHECKIN);
    ctx.put("exocontainer", container);
    launch(conditions, ctx);
  }

  public void postCheckout(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;

    Condition conditions = new Condition();
    conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.CHECKOUT);
    conditions.put(SessionEventMatcher.NODETYPE_KEY, ((ExtendedNodeType) node.getPrimaryNodeType())
        .getQName());
    conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, ((NodeImpl) node.getParent())
        .getAllNodeTypes());
    conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(node.getAllNodeTypes()));
    conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());

    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", node);
    ctx.put("event", ExtendedEvent.CHECKOUT);
    ctx.put("exocontainer", container);
    launch(conditions, ctx);
  }

  public void postLock(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;

    Condition conditions = new Condition();
    conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.LOCK);
    conditions.put(SessionEventMatcher.NODETYPE_KEY, ((ExtendedNodeType) node.getPrimaryNodeType())
        .getQName());
    conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, ((NodeImpl) node.getParent())
        .getAllNodeTypes());
    conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(node.getAllNodeTypes()));
    conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());

    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", node);
    ctx.put("event", ExtendedEvent.LOCK);
    ctx.put("exocontainer", container);
    launch(conditions, ctx);
  }

  public void postRead(ItemImpl item) throws RepositoryException {
    if (catalog == null)
      return;



    Condition conditions = new Condition();
    conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.READ);
    
    
    
    if (item.isNode()) {
      conditions.put(SessionEventMatcher.NODETYPE_KEY, ((ExtendedNodeType) ((NodeImpl) item)
          .getPrimaryNodeType()).getQName());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(((NodeImpl) item)
          .getAllNodeTypes()));
      if(!item.isRoot())
        conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, ((NodeImpl) item.getParent())
            .getAllNodeTypes());
    } else {
      conditions.put(SessionEventMatcher.NODETYPE_KEY, ((ExtendedNodeType) ((NodeImpl) item
          .getParent()).getPrimaryNodeType()).getQName());
      conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(((NodeImpl) item
          .getParent()).getAllNodeTypes()));
      conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, ((NodeImpl) item.getParent())
          .getAllNodeTypes());

    }

    conditions.put(SessionEventMatcher.PATH_KEY, item.getInternalPath());

    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", item);
    ctx.put("event", ExtendedEvent.READ);
    ctx.put("exocontainer", container);
    launch(conditions, ctx);

  }

  public void postSetProperty(NodeImpl parentNode, PropertyImpl property, int state) throws RepositoryException {
    if (catalog == null || property == null)
      return;
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

    conditions.put(SessionEventMatcher.NODETYPE_KEY, ((ExtendedNodeType) parentNode
        .getPrimaryNodeType()).getQName());
    conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, parentNode.getAllNodeTypes());
    conditions.put(SessionEventMatcher.NODETYPES_KEY,
        getInternalNames(parentNode.getAllNodeTypes()));
    conditions.put(SessionEventMatcher.PATH_KEY, property.getInternalPath());

    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", property);
    ctx.put("exocontainer", container);
    ctx.put("event", event);
    launch(conditions, ctx);
  }

  public void postUnlock(NodeImpl node) throws RepositoryException {
    if (catalog == null)
      return;

    Condition conditions = new Condition();
    conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.UNLOCK);
    conditions.put(SessionEventMatcher.NODETYPE_KEY, ((ExtendedNodeType) node.getPrimaryNodeType())
        .getQName());
    conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, ((NodeImpl) node.getParent())
        .getAllNodeTypes());
    conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(node.getAllNodeTypes()));
    conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());

    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", node);
    ctx.put("event", ExtendedEvent.UNLOCK);
    ctx.put("exocontainer", container);
    launch(conditions, ctx);
  }

  public void preRemoveItem(NodeImpl parent, ItemImpl item) throws RepositoryException {
    Condition conditions = new Condition();
    int event = item.isNode() ? ExtendedEvent.NODE_REMOVED : ExtendedEvent.PROPERTY_REMOVED;
    conditions.put(SessionEventMatcher.EVENTTYPE_KEY, event);

    conditions.put(SessionEventMatcher.NODETYPE_KEY, ((ExtendedNodeType) parent
        .getPrimaryNodeType()).getQName());
    conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, parent.getAllNodeTypes());

    if (item.isNode()) {
      conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(((NodeImpl) item)
          .getAllNodeTypes()));
    } else {
      conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(parent.getAllNodeTypes()));
    }
    conditions.put(SessionEventMatcher.PATH_KEY, item.getInternalPath());

    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", item);
    ctx.put("exocontainer", container);
    ctx.put("event", event);
    launch(conditions, ctx);

  }

  public void preRemoveMixin(NodeImpl node, InternalQName mixinType) throws RepositoryException {
    if (catalog == null)
      return;

    Condition conditions = new Condition();
    conditions.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.REMOVE_MIXIN);
    conditions.put(SessionEventMatcher.NODETYPE_KEY, mixinType);
    conditions.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
    conditions.put(SessionEventMatcher.PARENT_NODETYPES_KEY, node.getAllNodeTypes());

    conditions.put(SessionEventMatcher.NODETYPES_KEY, getInternalNames(node.getAllNodeTypes()));

    InvocationContext ctx = new InvocationContext();
    ctx.put("currentItem", node);
    ctx.put("event", ExtendedEvent.REMOVE_MIXIN);
    ctx.put("exocontainer", container);
    launch(conditions, ctx);
  }

  private InternalQName[] getInternalNames(NodeType[] nodeTypes) {
    InternalQName[] names = new InternalQName[nodeTypes.length];
    for (int i = 0; i < nodeTypes.length; i++) {
      names[i] = ((ExtendedNodeType) nodeTypes[i]).getQName();
    }
    return names;
  }

//  private NodeType[] getAllNodeTypes(SessionImpl session, NodeData data) throws RepositoryException {
//
//    ExtendedNodeType primaryType = session.getWorkspace().getNodeTypeManager().getNodeType(data
//        .getPrimaryTypeName());
//
//    InternalQName[] mixinNames = data.getMixinTypeNames();
//    NodeType[] nodeTypes = new NodeType[mixinNames.length + 1];
//    nodeTypes[0] = primaryType;
//    for (int i = 1; i <= mixinNames.length; i++) {
//      nodeTypes[i] = session.getWorkspace().getNodeTypeManager().getNodeType(mixinNames[i - 1]);
//    }
//
//    return nodeTypes;
//  }

  protected final void launch(Condition conditions, InvocationContext context) {
    if (conditions != null && catalog != null) {
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
}
