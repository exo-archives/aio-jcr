/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionException;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.command.action.ActionMatcher;
import org.exoplatform.services.command.action.Condition;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration;
import org.exoplatform.services.jcr.impl.ext.action.AddActionsPlugin;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog;
import org.exoplatform.services.jcr.impl.ext.action.SessionEventMatcher;
import org.exoplatform.services.jcr.impl.ext.action.AddActionsPlugin.ActionsConfig;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

public class TestSessionActionCatalog extends BaseUsecasesTest {

  public void testIfServicePresent() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);

    assertNotNull("No SessionActionCatalog configured!", catalog);
    Iterator<ActionMatcher> matchers = catalog.getAllEntries().keySet().iterator();
    System.out.println(">>testIfServicePresent");
    while (matchers.hasNext()) {
      System.out.println(((SessionEventMatcher) matchers.next()).dump());
    }

  }

  public void testPluginConfiguration() throws Exception {
    ActionConfiguration ac = new ActionConfiguration("org.exoplatform.services.jcr.usecases.action.DummyAction",
        "addNode,addProperty",
        "/test,/exo:test1",
        true,
        "nt:base,",
        "nt:base",
        null,null);
    List actionsList = new ArrayList();
    ActionsConfig actions = new ActionsConfig();
    actions.setActions(actionsList);
    actionsList.add(ac);
    InitParams params = new InitParams();
    ObjectParameter op = new ObjectParameter();
    op.setObject(actions);
    op.setName("actions");
    params.addParameter(op);
    AddActionsPlugin aap = new AddActionsPlugin(params);

    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    catalog.addPlugin(aap);
    assertEquals(1, aap.getActions().size());
    assertEquals(1, catalog.getAllActions().size());
    ActionConfiguration ac1 = (ActionConfiguration) aap.getActions().get(0);
    assertEquals("org.exoplatform.services.jcr.usecases.action.DummyAction", ac1
        .getActionClassName());
    assertEquals("/test,/exo:test1", ac1.getPath());

    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.NODE_ADDED);

    assertEquals(1, catalog.getActions(cond).size());

    Iterator<ActionMatcher> matchers = catalog.getAllEntries().keySet().iterator();

    while (matchers.hasNext()) {
      System.out.println(((SessionEventMatcher) matchers.next()).dump());
    }
  }

  public void testMatchEventType() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();
    assertEquals(0, catalog.getAllActions().size());

    // ((NodeTypeImpl)node.getPrimaryNodeType()).getQName()
    // node.getInternalPath()
    // cond.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
    // cond.put(SessionEventMatcher.NODETYPE_KEY,
    // ((NodeTypeImpl)node.getPrimaryNodeType()).getQName());

    // test by event type
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.NODE_ADDED,
        null,
        true,
        null,
        null,
        null,null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.NODE_ADDED);
    assertEquals(1, catalog.getActions(cond).size());
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.ADD_MIXIN);
    assertEquals(0, catalog.getActions(cond).size());

  }

  public void testMatchDeepPath() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();
    NodeImpl node = (NodeImpl) root.addNode("test");

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.NODE_ADDED,
        new QPath[] { node.getInternalPath() },
        true,
        null,
        null,
        null,null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();

    cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.NODE_ADDED);
    cond.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());

    // test for this path
    assertEquals(1, catalog.getActions(cond).size());

    cond.put(SessionEventMatcher.PATH_KEY, Constants.ROOT_PATH);
    assertEquals(0, catalog.getActions(cond).size());

    // test for child path
    QPath child = QPath.makeChildPath(node.getInternalPath(), Constants.EXO_PREFIX);
    cond.put(SessionEventMatcher.PATH_KEY, child);
    assertEquals(1, catalog.getActions(cond).size());

    // test for grandchild path - found as deep = true
    QPath grandchild = QPath.makeChildPath(child, Constants.EXO_PREFIX);
    cond.put(SessionEventMatcher.PATH_KEY, grandchild);
    assertEquals(1, catalog.getActions(cond).size());

  }

  public void testMatchNotDeepPath() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();
    NodeImpl node = (NodeImpl) root.addNode("test");

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.NODE_ADDED,
        new QPath[] { ((NodeImpl) root).getInternalPath() },
        false,
        null,
        null,
        null,null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();

    cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.NODE_ADDED);

    // test for this path
    cond.put(SessionEventMatcher.PATH_KEY, Constants.ROOT_PATH);
    assertEquals(1, catalog.getActions(cond).size());

    // test for child path
    cond.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
    assertEquals(1, catalog.getActions(cond).size());

    // test for grandchild path - not found as deep = false
    QPath child = QPath.makeChildPath(node.getInternalPath(), Constants.EXO_PREFIX);
    cond.put(SessionEventMatcher.PATH_KEY, child);
    assertEquals(0, catalog.getActions(cond).size());

  }

  public void testMatchNodeType() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.NODE_ADDED,
        null,
        true,
        new InternalQName[] { Constants.NT_UNSTRUCTURED },
        null,
        null,null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.NODE_ADDED);

    // test for this nodetype
    cond.put(SessionEventMatcher.NODETYPE_KEY, Constants.NT_UNSTRUCTURED);
    assertEquals(1, catalog.getActions(cond).size());

    cond.put(SessionEventMatcher.NODETYPE_KEY, Constants.NT_NODETYPE);
    assertEquals(0, catalog.getActions(cond).size());
  }
  
  public void testMatchNodeTypes() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.ADD_MIXIN,
        null,
        true,
        null,
        null,
        null,new InternalQName[] { Constants.MIX_LOCKABLE});
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.ADD_MIXIN);

    // test for this nodetype
    cond.put(SessionEventMatcher.NODETYPES_KEY, new InternalQName[]{Constants.NT_UNSTRUCTURED});
    assertEquals(0, catalog.getActions(cond).size());

    cond.put(SessionEventMatcher.NODETYPES_KEY, new InternalQName[]{Constants.MIX_LOCKABLE});
    assertEquals(1, catalog.getActions(cond).size());
  }
  public void testMatchParentNodeType() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.NODE_ADDED,
        null,
        true,
        null,
        new InternalQName[] { Constants.NT_UNSTRUCTURED },
        null,null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.NODE_ADDED);

    // test for this nodetype
    cond.put(SessionEventMatcher.PARENT_NODETYPES_KEY, new NodeType[] { session.getWorkspace()
        .getNodeTypeManager().getNodeType(Constants.NT_UNSTRUCTURED) });
    assertEquals(1, catalog.getActions(cond).size());

    cond.put(SessionEventMatcher.PARENT_NODETYPES_KEY, new NodeType[] { session.getWorkspace()
        .getNodeTypeManager().getNodeType(Constants.NT_NODETYPE) });
    assertEquals(0, catalog.getActions(cond).size());

  }

  public void testMatchWorkspace() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    //
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.NODE_ADDED,
        null,
        true,
        null,
        null,
        new String[] { "production" },null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.NODE_ADDED);

    // test for this ws
    cond.put(SessionEventMatcher.WORKSPACE_KEY, "production");
    assertEquals(1, catalog.getActions(cond).size());

    cond.put(SessionEventMatcher.WORKSPACE_KEY, "draft");
    assertEquals(0, catalog.getActions(cond).size());

  }

  public void testDumpMatcher() throws Exception {
    NodeImpl node = (NodeImpl) root.addNode("test");
    NodeImpl node1 = (NodeImpl) root.addNode("test1");
    QPath[] paths = new QPath[] { node.getInternalPath(), node1.getInternalPath() };

    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.NODE_ADDED,
        paths,
        true,
        new InternalQName[] { Constants.NT_BASE, Constants.NT_QUERY },
        new InternalQName[] { Constants.NT_UNSTRUCTURED, Constants.NT_QUERY },
        null,null);
    System.out.println(matcher.dump());
  }

  public void testRemoveMixinAction() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.REMOVE_MIXIN,
        null,
        true,
        null,
        new InternalQName[] { Constants.NT_UNSTRUCTURED },
        null,null);
    DummyAction dAction = new DummyAction();
    catalog.addAction(matcher, dAction);

    assertEquals(0, dAction.getActionExecuterCount());
    Node tnode = root.addNode("testnode");
    tnode.addMixin("exo:owneable");
    assertEquals(0, dAction.getActionExecuterCount());
    tnode.removeMixin("exo:owneable");
    assertEquals(1, dAction.getActionExecuterCount());
  }

  public void testLockActions() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.LOCK,
        null,
        true,
        null,
        new InternalQName[] { Constants.NT_UNSTRUCTURED },
        null,null);
    DummyAction dAction = new DummyAction();
    catalog.addAction(matcher, dAction);

    assertEquals(0, dAction.getActionExecuterCount());

    Node lockedNode = root.addNode("locked node");
    if (lockedNode.canAddMixin("mix:lockable"))
      lockedNode.addMixin("mix:lockable");
    root.save();

    assertEquals(0, dAction.getActionExecuterCount());
    lockedNode.lock(true, true);
    assertEquals(1, dAction.getActionExecuterCount());
  }

  public void testReadAction() throws ItemExistsException,
      PathNotFoundException,
      VersionException,
      ConstraintViolationException,
      LockException,
      RepositoryException {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    
    Node testNode = root.addNode("testNode");
    PropertyImpl prop = (PropertyImpl) testNode.setProperty("test","test");
    root.save();
    
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.READ,
        new QPath[]{prop.getData().getQPath()} ,
        true,
        null,
        new InternalQName[] { Constants.NT_UNSTRUCTURED },
        null,null);
    DummyAction dAction = new DummyAction();
    
    catalog.addAction(matcher, dAction);
    
    assertEquals(0, dAction.getActionExecuterCount());
    String val = testNode.getProperty("test").getValue().getString();
    assertEquals(1, dAction.getActionExecuterCount());

  }

}
