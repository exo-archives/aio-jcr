/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrImplBaseTest;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestSessionCleaner extends JcrImplBaseTest {
  private final static int AGENT_COUNT = 10;

  private SessionRegistry  sessionRegistry;
  
  private long oldTimeOut;
  
  private final static long TEST_SESSION_TIMEOUT  = 20000;
  @Override
  public void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();
    sessionRegistry = (SessionRegistry) session.getContainer().getComponentInstanceOfType(SessionRegistry.class);
    oldTimeOut = sessionRegistry.timeOut;
    sessionRegistry.timeOut = TEST_SESSION_TIMEOUT;
    sessionRegistry.stop();
    Thread.yield();
    sessionRegistry.start();
  }
  

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    sessionRegistry.stop();    
    sessionRegistry.timeOut = oldTimeOut;
    Thread.yield();
    sessionRegistry.start();
  }


  public void testSessionRemove() throws LoginException,
      NoSuchWorkspaceException,
      RepositoryException,
      InterruptedException {
    SessionImpl session2 = repository.login(credentials, "ws");
    assertTrue(session2.isLive());

    
    assertNotNull(sessionRegistry);
   
    Thread.sleep(SessionRegistry.DEFAULT_CLEANER_TIMEOUT+20);

    assertFalse(session2.isLive());
  }

  public void testSessionRemoveMultiThread() throws InterruptedException {
    assertNotNull(sessionRegistry);
    final Random random = new Random();
    class Agent extends Thread {
      boolean result = false;

      boolean active = false;

      public Agent() {
        active = random.nextBoolean();
      }

      @Override
      public void run() {
        try {
          SessionImpl session2 = repository.login(credentials, "ws");
          Node rootNode = session2.getRootNode();
          rootNode.addNode("test");
          assertTrue(session2.isLive());
          


          if (active) {
            log.info("start active session");
            long startTime = System.currentTimeMillis();
            while (startTime + sessionRegistry.timeOut * 2 < System.currentTimeMillis()) {
              Node root2  = session2.getRootNode();
              Node testNode = root2.getNode("test");
              testNode.setProperty("prop1","value");
              Thread.sleep(sessionRegistry.timeOut /2);
            }
            result = session2.isLive();
          }else{
            log.info("start pasive session");
            Thread.sleep(SessionRegistry.DEFAULT_CLEANER_TIMEOUT+20);
            result = !session2.isLive();
          }


          
          
        } catch (InterruptedException e) {
        } catch (LoginException e) {
        } catch (NoSuchWorkspaceException e) {
        } catch (RepositoryException e) {
        }
      }

    }
    List<Agent> agents = new ArrayList<Agent>();
    for (int i = 0; i < AGENT_COUNT; i++) {
      agents.add(new Agent());
    }
    for (Agent agent : agents) {
      agent.start();
    }
    boolean isNeedWait = true;
    while (isNeedWait) {
      isNeedWait = false;
      for (int i = 0; i < AGENT_COUNT; i++) {
        Agent curClient = agents.get(i);
        if (curClient.isAlive()) {
          isNeedWait = true;
          break;
        }
      }
      Thread.sleep(100);
    }
    for (Agent agent2 : agents) {
      assertTrue(agent2.result);
    }
  }
}
