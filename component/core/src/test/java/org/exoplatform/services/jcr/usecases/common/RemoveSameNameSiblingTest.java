package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

public class RemoveSameNameSiblingTest extends BaseUsecasesTest {

  public void _testRemoveSameNameSibling() throws RepositoryException {
    Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()), WORKSPACE);
    Node root = session.getRootNode();

    Node subRoot = root.addNode("u");
    Node n1 = subRoot.addNode("child", "nt:unstructured");
    Node n2 = subRoot.addNode("child", "nt:unstructured");
    Node n3 = subRoot.addNode("child", "nt:unstructured");
    root.save();
    // session.save() ;
    // session.refresh(false) ;

    root.getNode("u/child[3]");
    n2 = subRoot.getNode("child[2]");
    log.debug(">>>> SAME NAME start " + n2.getPath() + " " + n2.getIndex());
    n2.remove();
    root.save();
    // session.save() ;

    log.debug("SIZE >>>" + root.getNode("u").getNodes().getSize()); // /child[2]");
    log.debug("SIZE >>>" + session.getRootNode().getNode("u").getNodes().getSize()); // /child[2]");

    assertEquals(2, subRoot.getNodes().getSize());
    try {
      root.getNode("u/child[3]");
      fail("exception should have been thrown");
    } catch (PathNotFoundException e) {
    }
  }

  public void _testRemoveSameNameSiblingReindex() throws RepositoryException {
    Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()), WORKSPACE);
    Node root = session.getRootNode();

    Node subRoot = root.addNode("u1");
    Node n1 = subRoot.addNode("child", "nt:unstructured");
    Node n2 = subRoot.addNode("child", "nt:unstructured");
    Node n3 = subRoot.addNode("child", "nt:unstructured");
    root.save();

    root.getNode("u1/child[3]");
    n2 = subRoot.getNode("child[2]");
    log.debug(">>>> SAME NAME start " + n2.getPath() + " " + n2.getIndex());
    n2.remove();
    root.save(); // reindex child[3] --> child[2]

    log.debug("SIZE >>>" + root.getNode("u1").getNodes().getSize());
    log.debug("SIZE >>>" + session.getRootNode().getNode("u1").getNodes().getSize());

    assertEquals(2, subRoot.getNodes().getSize());
    try {
      root.getNode("u1/child[2]"); // 
    } catch (PathNotFoundException e) {
      fail("A node u/child[2] must exists");
    }
  }

  public void _testRemoveSameNameSiblingReindexGetChilds() throws RepositoryException {
    Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()), WORKSPACE);
    Node root = session.getRootNode();

    Node subRoot = root.addNode("u1");
    Node n1 = subRoot.addNode("child", "nt:unstructured");
    Node n2 = subRoot.addNode("child", "nt:unstructured");
    Node n3 = subRoot.addNode("child", "nt:unstructured");

    Node n3_n1n2 = n3.addNode("n1").addNode("n2");
    n3_n1n2.addNode("n2-1"); // /u1/child[3]/n1/n2/n2-1
    n3_n1n2.addNode("n2-2"); // /u1/child[3]/n1/n2/n2-2
    n3_n1n2.addNode("n2-3"); // /u1/child[3]/n1/n2/n2-3

    root.save();

    root.getNode("u1/child[3]");
    n2 = subRoot.getNode("child[2]");
    log.debug(">>>> SAME NAME start " + n2.getPath() + " " + n2.getIndex());
    n2.remove(); // reindex child[3] --> child[2]
    // root.save();

    assertEquals("Same-name siblings path must be reindexed", "/u1/child[2]/n1/n2", n3_n1n2.getPath());

    try {
      NodeIterator chns = n3_n1n2.getNodes();
      while (chns.hasNext()) {
        Node chn = chns.nextNode();
      }
    } catch (PathNotFoundException e) {
      fail("Nodes must exists but " + e);
    }
  }

  public void testSearchByJcrPathSQL() throws RepositoryException {
    Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()), WORKSPACE);
    Node root = session.getRootNode();

    Node subRoot = root.addNode("u1");
    Node n1 = subRoot.addNode("child", "nt:unstructured");
    Node n2 = subRoot.addNode("child", "nt:unstructured");
    Node n3 = subRoot.addNode("child", "nt:unstructured");

    Node n3_n1n2 = n3.addNode("n1").addNode("n2");
    n3_n1n2.addNode("n2-1"); // /u1/child[3]/n1/n2/n2-1
    n3_n1n2.addNode("n2-2"); // /u1/child[3]/n1/n2/n2-2
    n3_n1n2.addNode("n2-3"); // /u1/child[3]/n1/n2/n2-3

    root.save();

    root.getNode("u1/child[3]");
    n2 = subRoot.getNode("child[2]");
    log.debug(">>>> SAME NAME start " + n2.getPath() + " " + n2.getIndex());
    n2.remove(); // reindex child[3] --> child[2]
    root.save();

    try {
      Query query = session.getWorkspace().getQueryManager().createQuery(
          "select * from nt:base where jcr:path like '/u1/child[3]/%'", Query.SQL);
      QueryResult queryResult = query.execute();
      NodeIterator iterator = queryResult.getNodes();
      while (iterator.hasNext()) {
        fail("No nodes should exists");
      }

      query = session.getWorkspace().getQueryManager().createQuery(
          "select * from nt:base where jcr:path like '/u1/child[2]/%'", Query.SQL);
      queryResult = query.execute();
      iterator = queryResult.getNodes();
      while (iterator.hasNext()) {
        Node n = iterator.nextNode();
        assertTrue("Node path must be reindexed ", n.getPath().startsWith("/u1/child[2]"));
      }
    } catch (RepositoryException e) {
      fail(e.getMessage());
    }
  }

  public void _testSearchByJcrPathXPath() throws RepositoryException {
    Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()), WORKSPACE);
    Node root = session.getRootNode();

    Node subRoot = root.addNode("u1");
    Node n1 = subRoot.addNode("child", "nt:unstructured");
    Node n2 = subRoot.addNode("child", "nt:unstructured");
    Node n3 = subRoot.addNode("child", "nt:unstructured");

    Node n3_n1n2 = n3.addNode("n1").addNode("n2");
    n3_n1n2.addNode("n2-1"); // /u1/child[3]/n1/n2/n2-1
    n3_n1n2.addNode("n2-2"); // /u1/child[3]/n1/n2/n2-2
    n3_n1n2.addNode("n2-3"); // /u1/child[3]/n1/n2/n2-3

    root.save();

    root.getNode("u1/child[3]");
    n2 = subRoot.getNode("child[2]");
    log.debug(">>>> SAME NAME start " + n2.getPath() + " " + n2.getIndex());
    n2.remove(); // reindex child[3] --> child[2]
    root.save();

    try {
      Query query = session.getWorkspace().getQueryManager().createQuery("/jcr:root/u1/child[3]//element(*, nt:base)",
          Query.XPATH);
      QueryResult queryResult = query.execute();
      NodeIterator iterator = queryResult.getNodes();
      while (iterator.hasNext()) {
        fail("No nodes should exists");
      }

      query = session.getWorkspace().getQueryManager().createQuery("/jcr:root/u1/child[2]//element(*, nt:base)",
          Query.XPATH);
      queryResult = query.execute();
      iterator = queryResult.getNodes();
      while (iterator.hasNext()) {
        Node n = iterator.nextNode();
        assertTrue("Node path must be reindexed ", n.getPath().startsWith("/u1/child[2]"));
      }
    } catch (RepositoryException e) {
      fail(e.getMessage());
    }
  }
}
