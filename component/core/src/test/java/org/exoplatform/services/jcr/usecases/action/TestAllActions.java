/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.action;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog;
import org.exoplatform.services.jcr.impl.ext.action.SessionEventMatcher;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;
import org.exoplatform.services.jcr.usecases.action.info.ActionInfo;
import org.exoplatform.services.jcr.usecases.action.info.CheckinActionInfo;
import org.exoplatform.services.jcr.usecases.action.info.CheckoutActionInfo;
import org.exoplatform.services.jcr.usecases.action.info.LockActionInfo;
import org.exoplatform.services.jcr.usecases.action.info.UnLockActionInfo;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: TestAllActions.java 13421 2007-03-15 10:46:47Z geaz $
 */
public class TestAllActions extends BaseUsecasesTest {

  SessionActionCatalog catalog = null;

  @Override
  public void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();
    catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
  }

  public Node prepareNode(Node rootNode, String name, String nodeType, String parentNodeType)
      throws RepositoryException {
    Node currentRoot = rootNode;
    if (parentNodeType != null) {
      currentRoot = currentRoot.addNode("firs_sub_node", parentNodeType);
      //rootNode.save();
    }

    return nodeType != null ? currentRoot.addNode(name, nodeType) : currentRoot.addNode(name);

  }

  public SessionEventMatcher prepareActionCatalog(Action action,
                                                  int event,
                                                  QPath[] paths,
                                                  boolean isDeep,
                                                  InternalQName[] nodeTypeNames,
                                                  InternalQName[] parentNodeTypeNames,
                                                  String[] workspaces) {

    catalog.clear();

    // test by path
    SessionEventMatcher matcher = new SessionEventMatcher(event, paths, isDeep, nodeTypeNames,
        parentNodeTypeNames, workspaces);

    catalog.addAction(matcher, action);
    return matcher;
  }

  public void matchEventType(ActionInfo actionInfo) {
    try {
      DummyAction daction = new DummyAction();

      prepareActionCatalog(daction, actionInfo.getEventType(), null, true, null, null, null);
      assertEquals(0, daction.getActionExecuterCount());
      Node node = prepareNode(root, "seampletest", null, null);
      assertEquals(0, daction.getActionExecuterCount());

      Context ctx = new ContextBase();
      ctx.put("node", node);
      actionInfo.execute(ctx);

      assertEquals(1, daction.getActionExecuterCount());

      actionInfo.tearDown(ctx);
      node.remove();
      session.save();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      fail("matchByNodeType test fail. " + e.getLocalizedMessage());
    }

  };

  public void matchDeepPath(ActionInfo actionInfo) {

    try {
      Node node = prepareNode(root, "FirsPath", null, null);
      Node node2 = prepareNode(node, "DeepPath", null, null);
      Node node3 = prepareNode(node2, "ThirdPath", null, null);
      Node otherNode = prepareNode(root, "Other", null, null);
      DummyAction daction = new DummyAction();
      // test by path
      prepareActionCatalog(daction,
                           actionInfo.getEventType(),
                           new QPath[] { ((NodeImpl) node).getInternalPath() },
                           true,
                           null,
                           null,
                           null);
      assertEquals(0, daction.getActionExecuterCount());
      Context ctx = new ContextBase();
      ctx.put("node", otherNode);
      actionInfo.execute(ctx);
      assertEquals(0, daction.getActionExecuterCount());
      actionInfo.tearDown(ctx);

      ctx.put("node", node3);
      actionInfo.execute(ctx);
      assertEquals(1, daction.getActionExecuterCount());
      actionInfo.tearDown(ctx);

      // Not deep
      daction = new DummyAction();
      prepareActionCatalog(daction,
                           actionInfo.getEventType(),
                           new QPath[] { ((NodeImpl) node).getInternalPath() },
                           false,
                           null,
                           null,
                           null);

      assertEquals(0, daction.getActionExecuterCount());

      ctx.put("node", otherNode);
      actionInfo.execute(ctx);
      assertEquals(0, daction.getActionExecuterCount());
      actionInfo.tearDown(ctx);

      ctx.put("node", node3);
      actionInfo.execute(ctx);
      assertEquals(0, daction.getActionExecuterCount());
      actionInfo.tearDown(ctx);

      node.remove();
      otherNode.remove();
      session.save();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      fail("matchDeepPath test fail. " + e.getLocalizedMessage());
    }

  }

  public void matchNodeType(ActionInfo actionInfo) {

    try {
      Node node = prepareNode(root, "firsPath", "nt:folder", null);
      Node node2 = prepareNode(root, "firsPath", null, null);
      DummyAction daction = new DummyAction();
      // test by path
      SessionEventMatcher matcher = prepareActionCatalog(daction,
                                                         actionInfo.getEventType(),
                                                         null,
                                                         true,
                                                         new InternalQName[] { session
                                                             .getLocationFactory()
                                                             .parseJCRName("nt:folder")
                                                             .getInternalName() },
                                                         null,
                                                         null);

      assertEquals(0, daction.getActionExecuterCount());
      Context ctx = new ContextBase();
      ctx.put("node", node);
      actionInfo.execute(ctx);
      assertEquals(1, daction.getActionExecuterCount());
      actionInfo.tearDown(ctx);

      daction = new DummyAction();
      matcher = prepareActionCatalog(daction,
                                     actionInfo.getEventType(),
                                     null,
                                     true,
                                     new InternalQName[] { session.getLocationFactory()
                                         .parseJCRName("nt:folder").getInternalName() },
                                     null,
                                     null);

      assertEquals(0, daction.getActionExecuterCount());

      ctx.put("node", node2);
      actionInfo.execute(ctx);
      assertEquals(0, daction.getActionExecuterCount());
      actionInfo.tearDown(ctx);

      node.remove();
      node2.remove();
      session.save();

    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      fail("matchNodeType test fail. " + e.getLocalizedMessage());
    }

  }

  public void matchParentNodeType(ActionInfo actionInfo) {
    try {
      Node n1 = root.addNode("n1");
      Node node = prepareNode(root , "firsPath", "nt:folder","nt:folder" );
     // node.setProperty("content","asdlfija;lsdkjf");
      DummyAction daction = new DummyAction();
      // test by path
      SessionEventMatcher matcher = prepareActionCatalog(daction,
                                                         actionInfo.getEventType(),
                                                         null,
                                                         true,
                                                         null,
                                                         new InternalQName[] { session
                                                             .getLocationFactory()
                                                             .parseJCRName("nt:folder")
                                                             .getInternalName() },

                                                         null);

      assertEquals(0, daction.getActionExecuterCount());
      Context ctx = new ContextBase();
      ctx.put("node", n1);
      actionInfo.execute(ctx);
      assertEquals(0, daction.getActionExecuterCount());
      actionInfo.tearDown(ctx);

      ctx.put("node", node);
      actionInfo.execute(ctx);
      assertEquals(1, daction.getActionExecuterCount());
      actionInfo.tearDown(ctx);

      n1.remove();
      node.remove();
      session.save();

    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      fail("matchParentNodeType test fail. " + e.getLocalizedMessage());
    }
  }

  public void actionTest(ActionInfo action) {

    matchEventType(action);
    matchDeepPath(action);
    matchNodeType(action);
    matchParentNodeType(action);
  }

  public void testActionLock() {
    actionTest(new LockActionInfo());
  }

  public void testActionUnLock() {
    actionTest(new UnLockActionInfo());
  }

  public void testActionCheckin() {
    actionTest(new CheckinActionInfo());
  }

  public void testActionCheckout() {
    actionTest(new CheckoutActionInfo());
  }
  
  public void testActionAddMixin(){
    
    //actionTest(new AddMixinActionInfo());
  }
}
