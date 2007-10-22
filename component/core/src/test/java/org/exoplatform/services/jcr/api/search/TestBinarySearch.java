/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.search;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 22.10.2007  
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestBinarySearch.java 111 2007-11-11 11:11:11Z peterit $
 */
public class TestBinarySearch extends JcrAPIBaseTest {
  
  public void testSearchBinaryContent() throws Exception {
    Node rootNode = session.getRootNode();
    Node queryNode = rootNode.addNode("queryNode", "nt:unstructured");
    Node someNode = queryNode.addNode("pathToParent", "nt:unstructured");

    if (!someNode.canAddMixin("rma:record"))
      throw new RepositoryException("Cannot add mixin node");
    else {
      someNode.addMixin("rma:record");
      someNode.setProperty("rma:recordIdentifier", "testIdentificator");
      someNode.setProperty("rma:originatingOrganization", "testProperty2");
    }

    Node node1 = someNode.addNode("Test1", "nt:file");
    Node content1 = node1.addNode("jcr:content", "nt:resource");
    content1.setProperty("jcr:lastModified", Calendar.getInstance());
    content1.setProperty("jcr:mimeType", "text/plain");
    content1.setProperty("jcr:data", new ByteArrayInputStream("ABBA AAAA".getBytes()));
    node1.addMixin("rma:record");
    node1.setProperty("rma:recordIdentifier", "testIdentificator");
    node1.setProperty("rma:originatingOrganization", "testProperty2");

    Node node2 = someNode.addNode("Test2", "nt:file");
    Node content2 = node2.addNode("jcr:content", "nt:resource");
    content2.setProperty("jcr:lastModified", Calendar.getInstance());
    content2.setProperty("jcr:mimeType", "text/plain");
    content2.setProperty("jcr:data", new ByteArrayInputStream("ACDC EEEE".getBytes()));
    node2.addMixin("rma:record");
    node2.setProperty("rma:recordIdentifier", "testIdentificator");
    node2.setProperty("rma:originatingOrganization", "testProperty2");

    session.save();

    SessionImpl querySession = (SessionImpl) repository.login(credentials, "ws");
    String sqlQuery = "SELECT * FROM rma:record WHERE jcr:path LIKE '/queryNode/pathToParent/%' ";
    QueryManager manager = querySession.getWorkspace().getQueryManager();
    Query query = manager.createQuery(sqlQuery, Query.SQL);    
    
    QueryResult queryResult = query.execute();
    NodeIterator iter = queryResult.getNodes();

    assertTrue(iter.getSize() == 2); // check target nodes for existanse
    while (iter.hasNext()) {
      assertNotNull(iter.nextNode());
    }
    
    sqlQuery = "//*[jcr:contains(., 'ABBA')]";
    query = manager.createQuery(sqlQuery, Query.XPATH);
    
    queryResult = query.execute();
    iter = queryResult.getNodes();

    assertEquals("Result nodes count is wrong", 1, iter.getSize());
    while (iter.hasNext()) {
      assertEquals("Content must be equals", "ABBA AAAA", iter.nextNode().getProperty("jcr:data").getString());
    }
  }
  
  public void testSearchBinaryContentAnotherSessionQueryManader() throws Exception {
    SessionImpl querySession = (SessionImpl) repository.login(credentials, "ws");
    
    Node rootNode = session.getRootNode();
    Node queryNode = rootNode.addNode("queryNode", "nt:unstructured");
    Node someNode = queryNode.addNode("pathToParent", "nt:unstructured");

    if (!someNode.canAddMixin("rma:record"))
      throw new RepositoryException("Cannot add mixin node");
    else {
      someNode.addMixin("rma:record");
      someNode.setProperty("rma:recordIdentifier", "testIdentificator");
      someNode.setProperty("rma:originatingOrganization", "testProperty2");
    }

    Node node1 = someNode.addNode("Test1", "nt:file");
    Node content1 = node1.addNode("jcr:content", "nt:resource");
    content1.setProperty("jcr:lastModified", Calendar.getInstance());
    content1.setProperty("jcr:mimeType", "text/plain");
    content1.setProperty("jcr:data", new ByteArrayInputStream("ABBA AAAA".getBytes()));
    node1.addMixin("rma:record");
    node1.setProperty("rma:recordIdentifier", "testIdentificator");
    node1.setProperty("rma:originatingOrganization", "testProperty2");

    Node node2 = someNode.addNode("Test2", "nt:file");
    Node content2 = node2.addNode("jcr:content", "nt:resource");
    content2.setProperty("jcr:lastModified", Calendar.getInstance());
    content2.setProperty("jcr:mimeType", "text/plain");
    content2.setProperty("jcr:data", new ByteArrayInputStream("ACDC EEEE".getBytes()));
    node2.addMixin("rma:record");
    node2.setProperty("rma:recordIdentifier", "testIdentificator");
    node2.setProperty("rma:originatingOrganization", "testProperty2");

    session.save();

    String sqlQuery = "SELECT * FROM rma:record WHERE jcr:path LIKE '/queryNode/pathToParent/%' ";
    QueryManager manager = querySession.getWorkspace().getQueryManager();
    Query query = manager.createQuery(sqlQuery, Query.SQL);    
    
    QueryResult queryResult = query.execute();
    NodeIterator iter = queryResult.getNodes();

    assertTrue(iter.getSize() == 2); // check target nodes for existanse
    while (iter.hasNext()) {
      assertNotNull(iter.nextNode());
    }
    
    sqlQuery = "//*[jcr:contains(., 'ABBA')]";
    query = manager.createQuery(sqlQuery, Query.XPATH);
    
    queryResult = query.execute();
    iter = queryResult.getNodes();

    assertEquals("Result nodes count is wrong", 1, iter.getSize());
    while (iter.hasNext()) {
      assertEquals("Content must be equals", "ABBA AAAA", iter.nextNode().getProperty("jcr:data").getString());
    }
  }
  
}
 