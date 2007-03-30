/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.version;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL
 *
 * 18.01.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestVersionLocks.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestVersionLocks extends BaseVersionTest {

  protected final String TEST_ROOT = "lockable_version_test";
  
  protected final int MAX = 10;
  
  protected Node testBase = null;
  
  public void setUp() throws Exception {
    super.setUp();
    
    testBase = root.addNode(TEST_ROOT);
    root.save();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      testBase.remove();
      root.save();
    } catch(Throwable e) {
      log.error("TEAR DOWN ERROR. " + getName() + ". " + e.getMessage(), e);
    }
    
    super.tearDown();
  }
  
  
  /**
   * ISSUE: instable behavior in lock/unlock and checkout/checkin weaving
   * http://jira.exoplatform.org/browse/JCR-129
   * 
   * ========================================================
   * 
   * if "node" is "mix:versionable" and "mix:lockable", then the following code fails for MAX > 2 (but works for one or two iterations !):
         for (int i = 0; i < MAX; i++) {
           node.lock(false, true);
           node.checkout();
           // editing & saving here doesn't change the issue !
           node.checkin();
           node.unlock();
         }
     an exception is thrown by "unlock": javax.jcr.InvalidItemStateException: (update) Item []:1[]node:1[http://www.jcp.org/jcr/1.0]lockOwner:1 not found. Probably was deleted by another session
     on the other hand, the following code works for every MAX I tried !
         for (int i = 0; i < MAX; i++) {
           node.lock(false, true);
           node.checkout();
           node.unlock();
           node.checkin();
         }
   */
  public void testCheckinLocked() throws Exception {
    Node lockable = testBase.addNode("lockable");
    testBase.save();
    
    lockable.addMixin("mix:lockable");
    lockable.addMixin("mix:versionable");
    testBase.save();
    
    for (int i = 0; i < MAX; i++) {
      lockable.lock(false, true);
      lockable.checkout();
      
      // editing & saving here doesn't change the issue !
      lockable.addNode("any node " + i).setProperty("any property", 123d);
      if (i >= 2) {
        lockable.getNode("any node " + (i - 2)).remove();
      }
      lockable.save();
      
      lockable.checkin();
      lockable.unlock();
    }
  }
  
  public void testCheckinUnlocked() throws Exception {
    Node lockable = testBase.addNode("lockable");
    testBase.save();
    
    lockable.addMixin("mix:lockable");
    lockable.addMixin("mix:versionable");
    testBase.save();
    
    for (int i = 0; i < MAX; i++) {
      lockable.lock(false, true);
      lockable.checkout();
      
      // editing & saving here doesn't change the issue !
      lockable.addNode("any node " + i).setProperty("any property", 123d);
      if (i >= 2) {
        lockable.getNode("any node " + (i - 2)).remove();
      }
      lockable.save();

      lockable.unlock();
      lockable.checkin();
    }
  }
  
}
