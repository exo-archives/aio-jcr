/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.lock;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.core.ExtendedNode;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestLockImpl extends JcrImplBaseTest {
  private ExtendedNode      lockedNode                = null;

  private LockManagerImpl   service;

  private static final long LOCK_TIMEOUT              = 5;                             // sec

  private static final long LOCK_REMOVER_WAIT = LockManagerImpl.LockRemover.DEFAULT_THREAD_TIMEOUT
                                                          + (LOCK_TIMEOUT + 1) * 1000; // 15sec

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
      log.info("Stoping thread. Wait for removing lock by LockRemover");
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
      log.info("Stoping thread. Wait for removing lock by LockRemover");
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
