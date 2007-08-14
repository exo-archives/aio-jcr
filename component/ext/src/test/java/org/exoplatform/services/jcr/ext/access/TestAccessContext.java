/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.access;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration;
import org.exoplatform.services.jcr.impl.ext.action.AddActionsPlugin;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog;
import org.exoplatform.services.jcr.impl.ext.action.AddActionsPlugin.ActionsConfig;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestAccessContext extends BaseStandaloneTest {
  private final static int MULTI_THIARD_OPERATIONS = 100;

  private final static int THREAD_COUNT            = 300;

  @Override
  public void setUp() throws Exception {

    super.setUp();
    setContextAction();
  }

  private void setContextAction() {
    SessionActionCatalog catalog = (SessionActionCatalog) container
        .getComponentInstanceOfType(SessionActionCatalog.class);
    ActionConfiguration ac = new ActionConfiguration(
        "org.exoplatform.services.jcr.ext.access.SetAccessControlContextAction",
        "addProperty,changeProperty,removeProperty,read", null, true, null, null, null,null);
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
    catalog.clear();
    catalog.addPlugin(aap);
  };

  public void testSetAccessContext() throws RepositoryException {
    setContextAction();
    Node testNode = root.addNode("test");
    session.save();
    testNode.setProperty("p1", 9);
    assertEquals(9, testNode.getProperty("p1").getValue().getLong());

    testNode.setProperty("p1", 10);
    session.save();
    testNode.setProperty("p1", (Value) null);
    session.save();
    SessionImpl s2 = repository.login(session.getCredentials(), "ws2");
    AccessManager am = s2.getAccessManager();
//    assertNotNull(am.context());
  }

  public void testWorkspaceAccessMenager() throws RepositoryException {
    SessionImpl s2 = repository.login(session.getCredentials(), "ws2");
    AccessManager am = s2.getAccessManager();
//    assertFalse(am instanceof DummyAccessManager);
  }

  public void testDenyAccessMenager() throws RepositoryException {
    Node tNode = root.addNode("testNode");
    tNode.setProperty("deny", "value");
    session.save();
    try {
      tNode.getProperty("deny");
      fail("AccessDeniedException scheduled to be");
    } catch (AccessDeniedException e) {
      // Ok
    }
    SessionImpl sysSession = repository.getSystemSession();
    try {
      sysSession.getRootNode().getNode("testNode").getProperty("deny");
    } catch (AccessDeniedException e) {
      fail("AccessDeniedException ");
    }

  }

  public void testAccessMenedgerContextMultiThiard() throws RepositoryException,
      InterruptedException {

    Node multiACTNode = root.addNode("testMultiACT");

    Random random = new Random();
    int nextInt = 0;
    for (int i = 0; i < MULTI_THIARD_OPERATIONS; i++) {
      nextInt = random.nextInt(100);
      if (nextInt % 2 == 0) {
        multiACTNode.setProperty("deny" + i, i);
      } else {
        multiACTNode.setProperty("someNode" + i, i);
      }
    }
    session.save();
    // Run each thread
    ArrayList<JCRClient4AccessContextTest> clients = new ArrayList<JCRClient4AccessContextTest>();
    //SessionImpl sysSession = repository.getSystemSession();
    for (int i = 0; i < THREAD_COUNT; i++) {
//      JCRClient4AccessContextTest jcrClient = new JCRClient4AccessContextTest(sysSession,
//          sysSession, session);
      JCRClient4AccessContextTest jcrClient = new JCRClient4AccessContextTest();
      jcrClient.start();
      clients.add(jcrClient);
    }
    // Next code is waiting for shutting down of all the threads
    boolean isNeedWait = true;
    int totalErrors = 0;
    while (isNeedWait) {
      isNeedWait = false;
      for (int i = 0; i < THREAD_COUNT; i++) {
        JCRClient4AccessContextTest curClient = clients.get(i);
        if (curClient.isAlive()) {
          isNeedWait = true;
          break;
        }
      }
      Thread.sleep(100);

    }
    for (JCRClient4AccessContextTest client4AccessContextTest : clients) {

      totalErrors += client4AccessContextTest.errorsCount;
    }
    log.info("Total rezult try=" + MULTI_THIARD_OPERATIONS * THREAD_COUNT + " errors="
        + totalErrors);
  }

  protected class JCRClient4AccessContextTest extends Thread {
    private SessionImpl systemSession;

    private SessionImpl adminSession;

    private SessionImpl userSession;

    private int         errorsCount;

    private Log         log = ExoLogger.getLogger("jcr.JCRClient4AccessContextTest");
    
    public JCRClient4AccessContextTest() {
      try {
//        StandaloneContainer container = StandaloneContainer.getInstance();
//        CredentialsImpl credentials = new CredentialsImpl("exo", "exo".toCharArray());
//
//        RepositoryService repositoryService = (RepositoryService) container
//            .getComponentInstanceOfType(RepositoryService.class);

        ///repository = (RepositoryImpl) repositoryService.getRepository();
        systemSession = repository.getSystemSession();
        adminSession = repository.getSystemSession();
        userSession = repository.login(credentials,"ws");
        log.info("Thread created");
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
//    public JCRClient4AccessContextTest(SessionImpl systemSession, SessionImpl adminSession,
//        
//        SessionImpl userSession) {
//
//      this.systemSession = systemSession;
//      this.adminSession = adminSession;
//      this.userSession = userSession;
//      this.errorsCount = 0;
//      log.info("Thread created");
//    }

    private void SequentialReadProperty() throws RepositoryException {
      Node sysNode = systemSession.getRootNode().getNode("testMultiACT");
      Node adminNode = adminSession.getRootNode().getNode("testMultiACT");
      Node userNode = userSession.getRootNode().getNode("testMultiACT");

      if (sysNode.getProperties().getSize() != MULTI_THIARD_OPERATIONS+1) {
        errorsCount++;
      }
      if (adminNode.getProperties().getSize() != MULTI_THIARD_OPERATIONS+1) {
        errorsCount++;
      }

      for (PropertyIterator i = userNode.getProperties(); i.hasNext();) {
        Property prop = i.nextProperty();
        if (prop.getName().indexOf("deny") > -1) {
          errorsCount++;
        }
      }
    }

    private void SequentialReadNode() throws RepositoryException {
      Node sysNode = systemSession.getRootNode().getNode("testMultiACT");
      Node adminNode = systemSession.getRootNode().getNode("testMultiACT");
      Node userNode = systemSession.getRootNode().getNode("testMultiACT");

      for (NodeIterator i = sysNode.getNodes(); i.hasNext();) {
        Node prop = i.nextNode();

        // try {
        //          
        //          
        // } catch (RepositoryException e) {
        // // TODO Auto-generated catch block
        // errorsCount++;
        // log.error("Exception must not to throw");
        // }
      }
      for (PropertyIterator i = adminNode.getProperties(); i.hasNext();) {
        Property prop = i.nextProperty();
        try {
          log.info(prop.getValue().getString());
        } catch (RepositoryException e) {
          // TODO Auto-generated catch block
          errorsCount++;
          log.error("Exception must not to throw");
        }
      }
      for (PropertyIterator i = userNode.getProperties(); i.hasNext();) {
        Property prop = i.nextProperty();
        try {
          log.info(prop.getValue().getString());
          if (prop.getName().indexOf("deny") > -1) {
            errorsCount++;
            log.error("Exception must throw");
          }
        } catch (RepositoryException e) {
        }
      }
    }

    private void ParalelRead() {
      for (int i = 0; i < MULTI_THIARD_OPERATIONS; i++) {

      }

    }

    @Override
    public void run() {
      try {
        SequentialReadProperty();
        // SequentialReadNode();
        ParalelRead();

      } catch (RepositoryException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        log.error("Error");
      }
      if(errorsCount>0){
        log.info("errorsCount = " + errorsCount);
      }
    }

  }
}
