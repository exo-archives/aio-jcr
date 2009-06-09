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
package org.exoplatform.services.jcr.impl.core.lock;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.impl.core.NodeImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: TestLockImpl.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class TestLockImpl extends JcrImplBaseTest {
  private ExtendedNode      lockedNode        = null;

  private LockManagerImpl   service;

  private static final long LOCK_TIMEOUT      = 5;                            // sec

  private static final long LOCK_REMOVER_WAIT = LockRemover.DEFAULT_THREAD_TIMEOUT
                                                  + (LOCK_TIMEOUT + 1) * 1000; // 15

  // sec

  public void setUp() throws Exception {

    super.setUp();

    service = (LockManagerImpl) container.getComponentInstanceOfType(LockManagerImpl.class);

    if (lockedNode == null)
      try {
        lockedNode = (ExtendedNode) root.addNode("locked node");
        if (lockedNode.canAddMixin("mix:lockable"))
          lockedNode.addMixin("mix:lockable");
        root.save();
      } catch (RepositoryException e) {
        fail("Child node must be accessible and readable. But error occurs: " + e);
      }
  }

  public void testNonSessionScopedLockRemoveOnTimeOut() {
    try {
      LockImpl lock = (LockImpl) lockedNode.lock(true, false);

      assertTrue(lockedNode.isLocked());
      lock.setTimeOut(LOCK_TIMEOUT);// 5 sec
      if (log.isDebugEnabled())
        log.debug("Stoping thread. Wait for removing lock for node "
            + ((NodeImpl) lockedNode).getData().getIdentifier() + "by LockRemover");
      Thread.sleep(LOCK_REMOVER_WAIT);
      assertFalse(lockedNode.isLocked());

    } catch (RepositoryException e) {
      fail(e.getLocalizedMessage());
    } catch (InterruptedException e) {
      fail(e.getLocalizedMessage());
    }
  }

  public void testSessionScopedLockRemoveOnTimeOut() {
    try {
      LockImpl lock = (LockImpl) lockedNode.lock(true, true);
      assertTrue(lockedNode.isLocked());
      lock.setTimeOut(LOCK_TIMEOUT); // sec
      if (log.isDebugEnabled())
        log.debug("Stoping thread. Wait for removing lock by LockRemover");
      Thread.sleep(LOCK_REMOVER_WAIT);
      assertTrue(lockedNode.isLocked());
      lockedNode.unlock();
    } catch (RepositoryException e) {
      fail(e.getLocalizedMessage());
    } catch (InterruptedException e) {
      fail(e.getLocalizedMessage());
    }
  }

}
