package org.exoplatform.services.jcr.rmi;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import junit.framework.TestCase;

/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

/**
 * Created by The eXo Platform SAS
 * <p>
 * The test case connect to remote JCR using JCR-RMI wrapper
 * 
 * @author Sergey Kabashnyuk
 */
public class RmiRepositoryConnectTest extends TestCase {
  private RMITestHelper rmiTestHelper = new RMITestHelper();;
  /**
   * The repository instance
   */
  private Repository repository;

  protected void setUp() throws Exception {

    rmiTestHelper.setUrl("//Sergey:9999/repository");
    rmiTestHelper.setWorkspace("production");
    rmiTestHelper.login("admin","admin");

  }
  
  public void testGetRootNode() throws RepositoryException{
    Node node = rmiTestHelper.getRootNode();
    System.out.println("root node path: " + node.getPath());
  }
  
  protected void tearDown() throws Exception {
    
  }

}
