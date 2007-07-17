/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.lock;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL Author : Peter Nedonosko
 * peter.nedonosko@exoplatform.com.ua 21.09.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestLock.java 13621 2007-03-21 13:43:55Z ksm $
 */
public class TestLock extends JcrAPIBaseTest {

  private Node lockedNode = null;

  public void setUp() throws Exception {

    super.setUp();

    if (lockedNode == null)
    try {
      lockedNode = root.addNode("locked node");
      if (lockedNode.canAddMixin("mix:lockable"))
        lockedNode.addMixin("mix:lockable");
      root.save();
    } catch (RepositoryException e) {
      fail("Child node must be accessible and readable. But error occurs: " + e);
    }
  }

  public void testLockByOwner() throws RepositoryException {

    try {
      lockedNode.lock(true, true);
      Node foo = lockedNode.addNode("foo");
      foo.addNode("bar"); // throws LockException "Node /node/foo is locked"
      lockedNode.save();
    } catch (RepositoryException e) {
      fail("Child node must be accessible and readable. But error occurs: " + e);
    }

    try {
      lockedNode.unlock();
      lockedNode.addNode("foo");
      session.save();
      lockedNode.lock(true, true);
      lockedNode.getNode("foo").addNode("bar"); // throws LockException "Node
      // /node/foo is locked"
      lockedNode.save();
    } catch (RepositoryException e) {
      fail("Child node must be accessible and readable. But error occurs: " + e);
    }

    try {
      lockedNode.unlock();
      lockedNode.addNode("foo");
      session.save();
      lockedNode.lock(true, true);
      lockedNode.getNode("foo").setProperty("bar", "bar"); // throws LockException "Node /node/foo is locked"
      lockedNode.save();
      lockedNode.unlock();
    } catch (RepositoryException e) {
      fail("Child node must be accessible and readable. But error occurs: " + e);
    }
  }

  public void testLockByOwnerAnotherSession() throws RepositoryException {
    Session session1 = repository.login(new CredentialsImpl("admin", "admin".toCharArray()), "ws");
    Node nodeToLockSession1 = session1.getRootNode().addNode("nodeToLockSession1");
    if (nodeToLockSession1.canAddMixin("mix:lockable"))
      nodeToLockSession1.addMixin("mix:lockable");
    session1.save();
    Lock lock = nodeToLockSession1.lock(true, false);// boolean isSessionScoped
    // in ECM we are using lock(true, true) without saving lockToken
    assertTrue(nodeToLockSession1.isLocked());
    String lockToken = lock.getLockToken();
    session1.logout();
    //
    Session session2 = repository.login(new CredentialsImpl("admin", "admin".toCharArray()), "ws");
    Node nodeToLockSession2 = session2.getRootNode().getNode("nodeToLockSession1");
    assertEquals(true, nodeToLockSession2.isLocked());
    session2.addLockToken(lockToken);
    //make sure you made this operation, otherwise you can't do unlock
    try {
      nodeToLockSession2.unlock();
      assertFalse(nodeToLockSession2.isLocked());
    } catch (Exception e) {
      fail("unlock() method should pass ok, as admin is lockOwner, but error occurs: " + e);
    }
  }

  public void testCreateAfterLockWithFile() throws RepositoryException {
    String lockToken = "";
    String nodeName = "nodeToLockAndDelete" + System.currentTimeMillis();
    //
    try {
      {
        Session localSession = repository.login(new CredentialsImpl("admin", "admin".toCharArray()), "ws");
  
        Node folder1 = localSession.getRootNode().addNode(nodeName, "nt:folder");      
        localSession.save();

        Node file1 = folder1.addNode(nodeName, "nt:file");
        
        Node resourceNode = file1.addNode("jcr:content", "nt:resource");
        resourceNode.setProperty("jcr:mimeType", "text/xml");
        resourceNode.setProperty("jcr:lastModified", Calendar.getInstance());
        resourceNode.setProperty("jcr:data", new ByteArrayInputStream("VETAL_OK".getBytes()));        
        
        localSession.save();
        
        file1.addMixin("mix:lockable");
        localSession.save();
        
        Lock lock = file1.lock(true, false);      
        assertTrue(file1.isLocked());
  
        lockToken = lock.getLockToken();
        localSession.logout();      
      }
      
      {
        Session localSession = repository.login(new CredentialsImpl("admin", "admin".toCharArray()), "ws");
        Node folder1 = localSession.getRootNode().getNode(nodeName);
        Node file1 = folder1.getNode(nodeName);
        assertTrue(file1.isLocked());
        file1.remove();
        localSession.save();
        localSession.logout();
      }
  
      {
        Session localSession = repository.login(new CredentialsImpl("admin", "admin".toCharArray()), "ws");
        
        Node folder1 = localSession.getRootNode().getNode(nodeName);      

        Node file1 = folder1.addNode(nodeName, "nt:file");
        
        Node resourceNode = file1.addNode("jcr:content", "nt:resource");
        resourceNode.setProperty("jcr:mimeType", "text/xml");
        resourceNode.setProperty("jcr:lastModified", Calendar.getInstance());
        resourceNode.setProperty("jcr:data", new ByteArrayInputStream("VETAL_OK".getBytes()));        
        
        localSession.save();        
        localSession.logout();      
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("error while adding same name node: " + e);
    }
  }  
  public void testCopyLockedNode() throws Exception {
    Session session1 = repository.login(new CredentialsImpl("admin", "admin".toCharArray()), "ws");
    Node nodeToCopyLock = session1.getRootNode().addNode("node2testCopyLockedNode");
    if (nodeToCopyLock.canAddMixin("mix:lockable"))
      nodeToCopyLock.addMixin("mix:lockable");
    session1.save();
    Lock lock = nodeToCopyLock.lock(true, false);// boolean isSessionScoped
    // in ECM we are using lock(true, true) without saving lockToken
    assertTrue(nodeToCopyLock.isLocked());
    
    Session session2 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()), "ws");
    
    Node lockedNode = session2.getRootNode().getNode("node2testCopyLockedNode"); 
    
    assertTrue(nodeToCopyLock.isLocked());
    
    Node destParent =  session2.getRootNode().addNode("destParent");
    session2.save();
    session2.getWorkspace().copy(lockedNode.getPath(),destParent.getPath()+"/"+lockedNode.getName());
    Node destCopyNode  = destParent.getNode("node2testCopyLockedNode");
    
    assertFalse(destCopyNode.isLocked());
    try {
      destCopyNode.lock(true,true);
    } catch (RepositoryException e) {
      fail("to lock node");
    }
    assertTrue(destCopyNode.isLocked());
    
    destCopyNode.unlock();
    nodeToCopyLock.unlock();
  }
}
