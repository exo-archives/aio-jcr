package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

public class RemoveSameNameSiblingTest extends BaseUsecasesTest {

  public void testRemoveSameNameSibling() throws RepositoryException {
    System.out.println("########BEGIN TEST REMOVE SAME NAME SIBLING###########");
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
    System.out.println("########  END  ###########");
  }

  public void testRemoveSameNameSiblingReindex() throws RepositoryException {
    System.out.println("########BEGIN TEST REMOVE SAME NAME SIBLING WITH REINDEX###########");
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
    System.out.println("########  END  ###########");
  }

  public void testRemoveSameNameSiblingReindexGetChilds() throws RepositoryException {
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
    //root.save(); 

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
}
