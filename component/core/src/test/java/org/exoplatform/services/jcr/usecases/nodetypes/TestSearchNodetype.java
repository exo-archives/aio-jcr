/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.usecases.nodetypes;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SASL .
 * 
 * @author <a href="volodymyr.krasnikov@exoplatform.com.ua">Volodymyr Krasnikov</a>
 * @version $Id: TestSearchNodetype.java 17:40:24
 */

public class TestSearchNodetype extends BaseUsecasesTest {

  public void testCreateNodetype() throws Exception {

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
    content1.setProperty("jcr:mimeType", "text/xml");
    content1.setProperty("jcr:data", getClass().getResourceAsStream("nodetypes-usecase-test.xml"));
    node1.addMixin("rma:record");
    node1.setProperty("rma:recordIdentifier", "testIdentificator");
    node1.setProperty("rma:originatingOrganization", "testProperty2");

    Node node2 = someNode.addNode("Test2", "nt:file");
    Node content2 = node2.addNode("jcr:content", "nt:resource");
    content2.setProperty("jcr:lastModified", Calendar.getInstance());
    content2.setProperty("jcr:mimeType", "text/xml");
    content2.setProperty("jcr:data", getClass().getResourceAsStream("nodetypes-usecase-test.xml"));
    node2.addMixin("rma:record");
    node2.setProperty("rma:recordIdentifier", "testIdentificator");
    node2.setProperty("rma:originatingOrganization", "testProperty2");

    session.save();

    String sqlQuery = "SELECT * FROM rma:record WHERE jcr:path LIKE '/queryNode/pathToParent/%' ";
    QueryManager manager = session.getWorkspace().getQueryManager();
    Query query = manager.createQuery(sqlQuery, Query.SQL);

    QueryResult queryResult = query.execute();
    NodeIterator iter = queryResult.getNodes();

    assertTrue(iter.getSize() == 2); // check target nodes for existanse

    while (iter.hasNext()) {
      assertNotNull(iter.nextNode());
    }

  }

  public void testCreateNodetypeWithLogout() throws Exception {

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
    content1.setProperty("jcr:mimeType", "text/xml");
    content1.setProperty("jcr:data", getClass().getResourceAsStream("nodetypes-usecase-test.xml"));
    node1.addMixin("rma:record");
    node1.setProperty("rma:recordIdentifier", "testIdentificator");
    node1.setProperty("rma:originatingOrganization", "testProperty2");

    Node node2 = someNode.addNode("Test2", "nt:file");
    Node content2 = node2.addNode("jcr:content", "nt:resource");
    content2.setProperty("jcr:lastModified", Calendar.getInstance());
    content2.setProperty("jcr:mimeType", "text/xml");
    content2.setProperty("jcr:data", getClass().getResourceAsStream("nodetypes-usecase-test.xml"));
    node2.addMixin("rma:record");
    node2.setProperty("rma:recordIdentifier", "testIdentificator");
    node2.setProperty("rma:originatingOrganization", "testProperty2");

    session.save();

    String sqlQuery = "SELECT * FROM rma:record WHERE jcr:path LIKE '/queryNode/pathToParent/%' ";
    QueryManager manager = session.getWorkspace().getQueryManager();
    Query query = manager.createQuery(sqlQuery, Query.SQL);

    QueryResult queryResult = query.execute();
    NodeIterator iter = queryResult.getNodes();

    assertTrue(iter.getSize() == 2); // check target nodes for existanse

    while (iter.hasNext()) {
      assertNotNull(iter.nextNode());
    }

    session.logout();

    // new login
    session = (SessionImpl) repository.login(credentials, "ws");

    Query query2 = manager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult2 = query2.execute();
    NodeIterator iter2 = queryResult2.getNodes();

    assertTrue(iter2.getSize() == 2); // check target nodes for existanse
    while (iter2.hasNext()) {
      assertNotNull(iter2.nextNode());
    }

  }

  public void testCreateNodetypeWithPreQueryManader() throws Exception {
    String sqlQuery = "SELECT * FROM rma:record WHERE jcr:path LIKE '/queryNode/pathToParent/%' ";
    QueryManager manager = session.getWorkspace().getQueryManager();
    Query query = manager.createQuery(sqlQuery, Query.SQL);

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
    content1.setProperty("jcr:mimeType", "text/xml");
    content1.setProperty("jcr:data", getClass().getResourceAsStream("nodetypes-usecase-test.xml"));
    node1.addMixin("rma:record");
    node1.setProperty("rma:recordIdentifier", "testIdentificator");
    node1.setProperty("rma:originatingOrganization", "testProperty2");

    Node node2 = someNode.addNode("Test2", "nt:file");
    Node content2 = node2.addNode("jcr:content", "nt:resource");
    content2.setProperty("jcr:lastModified", Calendar.getInstance());
    content2.setProperty("jcr:mimeType", "text/xml");
    content2.setProperty("jcr:data", getClass().getResourceAsStream("nodetypes-usecase-test.xml"));
    node2.addMixin("rma:record");
    node2.setProperty("rma:recordIdentifier", "testIdentificator");
    node2.setProperty("rma:originatingOrganization", "testProperty2");

    session.save();

    QueryResult queryResult = query.execute();
    NodeIterator iter = queryResult.getNodes();

    assertTrue(iter.getSize() == 2); // check target nodes for existanse
    while (iter.hasNext()) {
      assertNotNull(iter.nextNode());
    }
    session.logout();

    // new login
    session = (SessionImpl) repository.login(credentials, "ws");

    Query query2 = manager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult2 = query2.execute();
    NodeIterator iter2 = queryResult2.getNodes();

    assertTrue(iter2.getSize() == 2); // check target nodes for existanse

    while (iter2.hasNext()) {
      assertNotNull(iter2.nextNode());
    }
  }
}
