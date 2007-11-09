/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved. 
 * Please look at license.txt in info directory for more license detail.  
 */

package org.exoplatform.services.jcr.impl.replication;

import javax.jcr.Node;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 02.03.2007
 * 17:38:10
 * 
 * @version $Id: TestReplicationLock.java 02.03.2007 17:38:10 rainfox
 */

public class TestReplicationLock extends BaseReplicationTest {

  public void testLock() throws Exception {
    Node nodeLocked = root.addNode("Node Locked");
    nodeLocked.setProperty("jcr:data", "node data");
    nodeLocked.addMixin("mix:lockable");
    session.save();

    Thread.sleep(4 * 1000);

    Node destNodeLocked = root2.getNode("Node Locked");
    assertEquals("node data", destNodeLocked.getProperty("jcr:data").getString());
    assertEquals("mix:lockable", destNodeLocked.getMixinNodeTypes()[0].getName());
    assertEquals(false, destNodeLocked.isLocked());

    Lock lock = nodeLocked.lock(false, false);
    session.save();

    Thread.sleep(4 * 1000);

    assertEquals(true, destNodeLocked.isLocked());

    try {
      destNodeLocked.setProperty("jcr:data", "dd");
      session2.save();
      fail("Errore: Node is not locked");
    } catch (LockException e) {
      // ok
    }

    nodeLocked.unlock();

    Thread.sleep(4 * 1000);

    assertEquals(false, destNodeLocked.isLocked());
  }
}
