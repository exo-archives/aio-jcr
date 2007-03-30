/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.xa;


import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.core.XASession;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.transaction.TransactionService;

/**
 * Created by The eXo Platform SARL        . <br>
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestUserTransaction.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestUserTransaction extends JcrAPIBaseTest{

  private TransactionService txService;

  public void setUp() throws Exception {
    
//    StandaloneContainer.setConfigurationPath("src/java/conf/standalone/test-configuration.xml");
//    
//    container = StandaloneContainer.getInstance();
    
    super.setUp();
    
    txService = (TransactionService) container.getComponentInstanceOfType(TransactionService.class);    
  }
  
  private List<Session> openSomeSessions() throws Exception {
    
    List<Session> someSessions = new ArrayList<Session>(); 
    
    Session s1 = repository.login(new SimpleCredentials("admin","admin".toCharArray()), session.getWorkspace().getName());
    Node rootS1 = s1.getRootNode();
    rootS1.addNode("someNode1");
    rootS1.save();
    someSessions.add(s1);
    log.info("s1: " + s1);
    Session s2 = repository.login(new SimpleCredentials("exo","exo".toCharArray()), session.getWorkspace().getName());
    Node rootS2 = s2.getRootNode();
    rootS2.addNode("someNode2");
    rootS2.save();
    someSessions.add(s2);
    log.info("s2: " + s2);
    Session s3 = repository.login(new SimpleCredentials("exo","exo".toCharArray()), session.getWorkspace().getName());
    Node rootS3 = s3.getRootNode();
    rootS3.addNode("someNode3");
    rootS3.getNode("someNode2").remove();
    rootS3.save();
    someSessions.add(s3);
    log.info("s3: " + s3);
    Session s4 = repository.login(new SimpleCredentials("admin","admin".toCharArray()), session.getWorkspace().getName());
    Node rootS4 = s4.getRootNode();
    Node n = rootS4.getNode("someNode3");
    n.addNode("someNode4");
    rootS4.getNode("someNode1").remove();
    rootS4.save();
    someSessions.add(s4);
    log.info("s4: " + s4);
    
    // some logouts
    session.logout();
    someSessions.add(session);
    log.info("session: " + session);
    s1.logout();
    
    // ...from setUp()
    session = (SessionImpl) repository.login(credentials, "ws");
    log.info("session (new): " + session);
    someSessions.add(session);
    
    workspace = session.getWorkspace();
    root = session.getRootNode();
    valueFactory = session.getValueFactory();
    
    return someSessions;
  }

  public void testCommit() throws Exception {
    
    List<Session> someSessions = openSomeSessions();
    
    UserTransaction ut = txService.getUserTransaction();
    ut.begin();
    session.getRootNode().addNode("txcommit");
    session.save();
    assertNotNull(session.getItem("/txcommit"));
    Session s1 = repository.login(new SimpleCredentials("admin","admin".toCharArray()), 
        session.getWorkspace().getName());
    try {
      assertNotNull(s1.getItem("/txcommit"));
      fail("PathNotFoundException should have be thrown");
    } catch (PathNotFoundException e) {
      log.info("Ok: " + e.getMessage());
    } 
    ut.commit();
    assertNotNull(s1.getItem("/txcommit"));
    
    someSessions.clear();
  }

  public void testRollback() throws Exception {
    
    UserTransaction ut = txService.getUserTransaction();
    
    ut.begin();
    session.getRootNode().addNode("txrollback");
    session.save();
    assertNotNull(session.getItem("/txrollback"));
    
//    Session s1 = repository.login(new SimpleCredentials("admin","admin".toCharArray()), 
//        session.getWorkspace().getName());
    ut.rollback();
    try {
      assertNotNull(session.getItem("/txrollback"));
      fail("PathNotFoundException should have be thrown");
    } catch (PathNotFoundException e) {
    } 
  }
  public void testUserTransactionFromJndi() throws Exception {

    InitialContext ctx = new InitialContext();
    Object obj = ctx.lookup("UserTransaction");
    UserTransaction ut = (UserTransaction) obj;

    ut.begin();
    Session s1 = repository.login(new SimpleCredentials("admin","admin".toCharArray()), 
     session.getWorkspace().getName());
    s1.getRootNode().addNode("txcommit1");
    s1.save();
    ut.commit();
    assertNotNull(session.getItem("/txcommit1"));

  }
  
  public void testReuseUT() throws Exception {

    InitialContext ctx = new InitialContext();
    Object obj = ctx.lookup("UserTransaction");
    UserTransaction ut = (UserTransaction) obj;
    
    Session s1 = repository.login(new SimpleCredentials("admin","admin".toCharArray()), 
        session.getWorkspace().getName());

    
    ut.begin();
    Node tx2 = s1.getRootNode().addNode("txcommit2");
    ut.commit();
    
    // In a case of reusing Have to enlist the resource once again!
    ((XASession)s1).enlistResource();
    
    ut.begin();
    tx2.addNode("txcommit21");
    s1.save();
    ut.commit();
    assertNotNull(session.getItem("/txcommit2/txcommit21"));
    
  }

}
