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
