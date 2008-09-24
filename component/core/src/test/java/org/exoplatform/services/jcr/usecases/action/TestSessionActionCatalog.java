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
package org.exoplatform.services.jcr.usecases.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.observation.Event;
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

  public void testDumpMatcher() throws Exception {
    NodeImpl node = (NodeImpl) root.addNode("test");
    NodeImpl node1 = (NodeImpl) root.addNode("test1");
    QPath[] paths = new QPath[] { node.getInternalPath(), node1.getInternalPath() };

    SessionEventMatcher matcher = new SessionEventMatcher(Event.NODE_ADDED,
                                                          paths,
                                                          true,
                                                          null,
                                                          new InternalQName[] { Constants.NT_BASE,
                                                              Constants.NT_UNSTRUCTURED,
                                                              Constants.NT_QUERY });
    System.out.println(matcher.dump());
  }

  public void testIfServicePresent() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);

    assertNotNull("No SessionActionCatalog configured!", catalog);
    Iterator<ActionMatcher> matchers = catalog.getAllEntries().keySet().iterator();
    System.out.println(">>testIfServicePresent");
    while (matchers.hasNext()) {
      System.out.println(((SessionEventMatcher) matchers.next()).dump());
    }

  }

  public void testLockActions() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.LOCK,
                                                          null,
                                                          true,
                                                          null,
                                                          new InternalQName[] { Constants.NT_UNSTRUCTURED });
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

  public void testMatchDeepPath() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();
    NodeImpl node = (NodeImpl) root.addNode("test");

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(Event.NODE_ADDED,
                                                          new QPath[] { node.getInternalPath() },
                                                          true,
                                                          null,
                                                          null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();

    cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, Event.NODE_ADDED);
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

  public void testMatchEventType() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();
    assertEquals(0, catalog.getAllActions().size());

    // ((NodeTypeImpl)node.getPrimaryNodeType()).getQName()
    // node.getInternalPath()
    // cond.put(SessionEventMatcher.PATH_KEY, node.getInternalPath());
    // cond.put(SessionEventMatcher.NODETYPE_KEY,
    // ((NodeTypeImpl)node.getPrimaryNodeType()).getQName());

    // test by event type
    SessionEventMatcher matcher = new SessionEventMatcher(Event.NODE_ADDED, null, true, null, null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, Event.NODE_ADDED);
    assertEquals(1, catalog.getActions(cond).size());
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.ADD_MIXIN);
    assertEquals(0, catalog.getActions(cond).size());

  }

  public void testMatchNodeTypes() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.ADD_MIXIN,
                                                          null,
                                                          true,
                                                          null,
                                                          new InternalQName[] { Constants.MIX_LOCKABLE });
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, ExtendedEvent.ADD_MIXIN);

    // test for this nodetype
    cond.put(SessionEventMatcher.NODETYPES_KEY, new InternalQName[] { Constants.NT_UNSTRUCTURED });
    assertEquals(0, catalog.getActions(cond).size());

    cond.put(SessionEventMatcher.NODETYPES_KEY, new InternalQName[] { Constants.MIX_LOCKABLE });
    assertEquals(1, catalog.getActions(cond).size());
  }

  public void testMatchNotDeepPath() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();
    NodeImpl node = (NodeImpl) root.addNode("test");

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(Event.NODE_ADDED,
                                                          new QPath[] { ((NodeImpl) root).getInternalPath() },
                                                          false,
                                                          null,
                                                          null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();

    cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, Event.NODE_ADDED);

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

  public void testMatchWorkspace() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    //
    SessionEventMatcher matcher = new SessionEventMatcher(Event.NODE_ADDED,
                                                          null,
                                                          true,
                                                          new String[] { "production" },
                                                          null);
    catalog.addAction(matcher, new DummyAction());
    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, Event.NODE_ADDED);

    // test for this ws
    cond.put(SessionEventMatcher.WORKSPACE_KEY, "production");
    assertEquals(1, catalog.getActions(cond).size());

    cond.put(SessionEventMatcher.WORKSPACE_KEY, "draft");
    assertEquals(0, catalog.getActions(cond).size());

  }

  public void testPluginConfiguration() throws Exception {
    ActionConfiguration ac = new ActionConfiguration("org.exoplatform.services.jcr.usecases.action.DummyAction",
                                                     "addNode,addProperty",
                                                     "/test,/exo:test1",
                                                     true,
                                                     null,
                                                     "nt:base");
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

    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    catalog.addPlugin(aap);
    assertEquals(1, aap.getActions().size());
    assertEquals(1, catalog.getAllActions().size());
    ActionConfiguration ac1 = aap.getActions().get(0);
    assertEquals("org.exoplatform.services.jcr.usecases.action.DummyAction",
                 ac1.getActionClassName());
    assertEquals("/test,/exo:test1", ac1.getPath());

    Condition cond = new Condition();
    cond.put(SessionEventMatcher.EVENTTYPE_KEY, Event.NODE_ADDED);

    assertEquals(1, catalog.getActions(cond).size());

    Iterator<ActionMatcher> matchers = catalog.getAllEntries().keySet().iterator();

    while (matchers.hasNext()) {
      System.out.println(((SessionEventMatcher) matchers.next()).dump());
    }
  }

  public void testReadAction() throws ItemExistsException,
                              PathNotFoundException,
                              VersionException,
                              ConstraintViolationException,
                              LockException,
                              RepositoryException {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path

    Node testNode = root.addNode("testNode");
    PropertyImpl prop = (PropertyImpl) testNode.setProperty("test", "test");
    root.save();

    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.READ,
                                                          new QPath[] { prop.getData().getQPath() },
                                                          true,
                                                          null,
                                                          new InternalQName[] { Constants.NT_UNSTRUCTURED });
    DummyAction dAction = new DummyAction();

    catalog.addAction(matcher, dAction);

    assertEquals(0, dAction.getActionExecuterCount());
    String val = testNode.getProperty("test").getValue().getString();
    assertEquals(1, dAction.getActionExecuterCount());

  }

  public void testAddMixinAction() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.ADD_MIXIN,
                                                          null,
                                                          true,
                                                          null,
                                                          new InternalQName[] {
                                                              Constants.MIX_REFERENCEABLE,
                                                              Constants.EXO_OWNEABLE });
    DummyAction dAction = new DummyAction();
    catalog.addAction(matcher, dAction);

    assertEquals(0, dAction.getActionExecuterCount());
    Node tnode = root.addNode("testnode");
    assertEquals(0, dAction.getActionExecuterCount());
    tnode.addMixin("exo:owneable");
    assertEquals(1, dAction.getActionExecuterCount());
    tnode.addMixin("mix:referenceable");
    assertEquals(2, dAction.getActionExecuterCount());
  }

  public void testRemoveMixinAction() throws Exception {
    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(ExtendedEvent.REMOVE_MIXIN,
                                                          null,
                                                          true,
                                                          null,
                                                          new InternalQName[] { Constants.EXO_OWNEABLE });
    DummyAction dAction = new DummyAction();
    catalog.addAction(matcher, dAction);

    assertEquals(0, dAction.getActionExecuterCount());
    Node tnode = root.addNode("testnode");
    tnode.addMixin("exo:owneable");
    assertEquals(0, dAction.getActionExecuterCount());
    tnode.removeMixin("exo:owneable");
    assertEquals(1, dAction.getActionExecuterCount());
  }

}
