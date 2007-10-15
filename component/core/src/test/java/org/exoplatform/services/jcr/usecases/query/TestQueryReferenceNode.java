/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.usecases.query;


import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.JcrAPIBaseTest;


/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestQueryReferenceNode.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestQueryReferenceNode extends JcrAPIBaseTest {
  
  public void testGetReferences() throws Exception {
    
    String sqlQuery = "select * from nt:unstructured where jcr:path like '/queryNode/%' " ;
    //Session session = repository.getSystemSession(repository.getSystemWorkspaceName()) ;    
    Node rootNode = session.getRootNode() ;
    Node queryNode = rootNode.addNode("queryNode","nt:unstructured");
    rootNode.save();
    // make sure database is clean
    QueryManager manager = session.getWorkspace().getQueryManager() ;
    Query query = manager.createQuery(sqlQuery,Query.SQL) ;
    QueryResult queryResult = query.execute() ;
    NodeIterator iter = queryResult.getNodes() ;
    assertTrue(iter.getSize() == 0) ;
    
    Node testNode = queryNode.addNode("testGetReferences", "nt:unstructured");        ;    
    Node n1 = queryNode.addNode("n1", "nt:unstructured");
    Node n2 = queryNode.addNode("n2", "nt:unstructured");        
    queryNode.save();
    
    //before make reference     
    queryResult = query.execute() ;
    iter = queryResult.getNodes() ;
    System.out.println("SIZE: "+iter.getSize());

    assertTrue(iter.getSize() == 3) ;
    
    //After make reference
    testNode.addMixin("mix:referenceable");    
    n1.setProperty("p1", testNode);    
    queryNode.save();
    
    ///////////
    Session session1 = repository.login(new SimpleCredentials("admin", "admin".toCharArray()), 
        repository.getSystemWorkspaceName());
    manager = session1.getWorkspace().getQueryManager() ;
    query = manager.createQuery(sqlQuery,Query.SQL) ;
    ///////////  
    
    queryResult = query.execute() ;
    iter = queryResult.getNodes() ;
    
    System.out.println(" "+iter.nextNode().getPath());
        
    assertEquals(3,iter.getSize()) ;  
    // clean database
    queryNode.remove() ;
    session.save() ;
     
  }  
}
