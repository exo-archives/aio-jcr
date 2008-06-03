package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

public class TestMoveNodeWithNewName extends BaseUsecasesTest {  
  
  public void testMove() throws Exception {  
    String workspaceName = repository.getSystemWorkspaceName() ;
    Session session2 = repository.getSystemSession(workspaceName) ;            
    Node rootNode = session2.getRootNode();
    Node cmsNode = rootNode.addNode("cms","nt:unstructured") ;
    Node nodeA = cmsNode.addNode("node_A","nt:unstructured") ;
    rootNode.save() ;
    session2.save() ;
    
    nodeA.addNode("node_1","nt:unstructured") ;
    nodeA.addNode("node_2","nt:unstructured") ;
    nodeA.addNode("node_3","nt:unstructured") ;
    
    nodeA.save() ;
    session2.save() ;
    
    String srcPath = cmsNode.getPath() + "/node_A" ;
    String destPath = cmsNode.getPath() + "/node_B" ;
    
    session2.move(srcPath, destPath) ;
    session2.save() ;
    
    Node nodeB = (Node) session2.getItem(destPath) ;
    assertNotNull(nodeB) ;
    
    Node node1 = nodeB.getNode("node_1") ;
    assertNotNull(node1) ;
    assertEquals("/cms/node_B/node_1", node1.getPath()) ;
    //System.out.println("node 1 path ========= " + node1.getPath()) ;
    
    Node node2 = nodeB.getNode("node_2") ;
    assertNotNull(node2) ;
    assertEquals("/cms/node_B/node_2", node2.getPath()) ;
    //System.out.println("node 2 path ========= " + node2.getPath()) ;
    
    Node node3 = nodeB.getNode("node_3") ;
    assertNotNull(node3) ;
    assertEquals("/cms/node_B/node_3", node3.getPath()) ;
    //System.out.println("node 3 path ========= " + node3.getPath()) ;
  }
}
