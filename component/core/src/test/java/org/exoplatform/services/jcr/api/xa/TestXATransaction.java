/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.xa;


import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.lock.Lock;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.core.XASession;
import org.exoplatform.services.transaction.TransactionService;

/**
 * Created by The eXo Platform SARL        . <br>
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestXATransaction.java 13485 2007-03-16 16:19:54Z ksm $
 */
public class TestXATransaction extends JcrAPIBaseTest{

  private TransactionService ts;

  public void setUp() throws Exception {
    
    super.setUp();
    
    ts = (TransactionService)container.
    getComponentInstanceOfType(TransactionService.class);
    
  }

  public void testSimpleGlobalTransaction() throws Exception {
    Xid id = ts.createXid();
    XAResource xares = ((XASession)session).getXAResource();
    xares.start(id, XAResource.TMNOFLAGS);
    session.getRootNode().addNode("txg1");
    session.save();
    xares.commit(id, true);
    Session s1 = repository.login(new SimpleCredentials("admin","admin".toCharArray()), 
        session.getWorkspace().getName());
    assertNotNull(s1.getItem("/txg1"));

  }
  
  public void test2GlobalTransactions() throws Exception {
    Session s1 = repository.login(new SimpleCredentials("admin","admin".toCharArray()), 
        session.getWorkspace().getName());
    
    Xid id1 = ts.createXid();
    XAResource xares = ((XASession)session).getXAResource();
    xares.start(id1, XAResource.TMNOFLAGS);
    
    session.getRootNode().addNode("txg2");
    session.save();
    //xares.commit(id, true);
    try {
      s1.getItem("/txg2");
      fail("PathNotFoundException");
    } catch (PathNotFoundException e) {
    }
    xares.end(id1, XAResource.TMSUSPEND);
    
    Xid id2 = ts.createXid();
    xares.start(id2, XAResource.TMNOFLAGS);
    session.getRootNode().addNode("txg3");
    session.save();

    try {
      s1.getItem("/txg3");
      fail("PathNotFoundException");
    } catch (PathNotFoundException e) {
    } 

//  End work
    xares.end(id2, XAResource.TMSUCCESS);

//  Resume work with former transaction
    xares.start(id1, XAResource.TMRESUME);
    
//  Commit work recorded when associated with xid2
    xares.commit(id1, true);
//    xares.commit(id2, true);
    assertNotNull(s1.getItem("/txg2"));
    assertNotNull(s1.getItem("/txg3"));

  }
  public void testLockInTransactions() throws LoginException, NoSuchWorkspaceException, RepositoryException, XAException  {
    Session s1 = repository.login(new SimpleCredentials("admin","admin".toCharArray()), 
        session.getWorkspace().getName());
    Session s2 = repository.login(new SimpleCredentials("exo","exo".toCharArray()), 
        session.getWorkspace().getName());

    Node n1 = session.getRootNode().addNode("testLock");
    n1.addMixin("mix:lockable");
    session.getRootNode().save();
    
    Xid id1 = ts.createXid();
    XAResource xares = ((XASession)session).getXAResource();
    xares.start(id1, XAResource.TMNOFLAGS);
    

    // lock node
    Lock lock = n1.lock(false, true);

    // assert: isLive must return true
    assertTrue("Lock must be live", lock.isLive());

    assertFalse(s2.getRootNode().getNode("testLock").isLocked());
    
//  End work
    xares.end(id1, XAResource.TMSUCCESS);

    
//  Commit work recorded when associated with xid2
    xares.commit(id1, true);
    assertTrue(s2.getRootNode().getNode("testLock").isLocked());

    n1.unlock();
  }
}
