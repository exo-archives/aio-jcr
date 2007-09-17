/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 */

package org.exoplatform.services.jcr.api.reading;


import java.util.Calendar;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestNode.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestNode extends JcrAPIBaseTest{
  
  private Node testRoot;

  public void initRepository() throws RepositoryException {
    Node root = session.getRootNode();
    Node file = root.addNode("childNode", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode = file.getNode("jcr:content");
    contentNode.setProperty("jcr:data", session.getValueFactory().createValue("this is the content", PropertyType.BINARY));
    contentNode.setProperty("jcr:mimeType", session.getValueFactory().createValue("text/html"));
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));

    session.save();
  }

  public void tearDown() throws Exception {
    Node root = session.getRootNode();
    Node node = root.getNode("childNode");
    node.remove();
    session.save();

    super.tearDown();
  }


  public void testGetNode() throws Exception {

    Node root = session.getRootNode();

    try {
      root.getNode("/childNode/childNode2");
      fail("exception should have been thrown - not rel path");
    } catch (RepositoryException e) {
    }

    Node node = root.getNode("childNode/childNode2");
    assertNotNull(node);

    assertEquals("nt:file", node.getPrimaryNodeType().getName());
    Property property = node.getNode("jcr:content").getProperty("jcr:data");
    property.setValue(new StringValue("this is the NEW content"));

    node = root.getNode("childNode");
    node.addNode("childNode3", "nt:file");

    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    root = session.getRootNode();
    try {
      Node n = root.getNode("childNode/childNode3");
      fail("exception should have been thrown "+n);
    } catch (RepositoryException e) {
    }

    property = root.getNode("childNode/childNode2/jcr:content").
        getProperty("jcr:data");

    assertEquals("this is the content", property.getString());
    Value val = new BinaryValue("this is the NEW content");
    node = root.getNode("childNode/childNode2/jcr:content");
    node.setProperty("jcr:data", val);
//    property.setValue(val);

    node = root.getNode("childNode");
    session.save();
    root = repository.login(credentials, WORKSPACE).getRootNode();
 //     System.out.println("------------------");
    property = root.getNode("childNode/childNode2/jcr:content").
        getProperty("jcr:data");

    assertEquals("this is the NEW content", property.getString());

    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    root = session.getRootNode();
    node = root.getNode("childNode");
    assertEquals(node.toString(), root.getNode("childNode").toString());

// not allowed!
//    root.getNode("childNode/childNode2/jcr:content").setProperty("myapp:temp", new StringValue("Temp"));

    Session session2 = repository.login(credentials, WORKSPACE);
    Node root2 = session2.getRootNode();
    Node node2 = root2.getNode("childNode/childNode2/jcr:content");
    node2.setProperty("jcr:data", new BinaryValue("Temp"));
    session2.save();

    session.refresh(false);

    root = session.getRootNode();
    node = root.getNode("childNode/childNode2/jcr:content");
    assertNotNull(node);
    assertNotNull(node.getProperty("jcr:data"));
    assertEquals("Temp", node.getProperty("jcr:data").getString());
    try {
      node.getProperty("myapp:temp");
      fail("exception should have been thrown");
    } catch (RepositoryException e) {
    }

  }

  public void testGetSomeSiblingNode() throws RepositoryException {
    root = session.getRootNode();
    Node subRoot = root.addNode("subRoot", "nt:unstructured");
    Node child1 = subRoot.addNode("child", "nt:unstructured");
    child1.setProperty("prop1", "prop1");
    Node child2 = subRoot.addNode("child", "nt:unstructured");
    child2.setProperty("prop2", "prop2");
    Node child3 = subRoot.addNode("child", "nt:unstructured");
    assertEquals(1, child1.getIndex());
    assertTrue(child1.hasProperty("prop1"));
    assertEquals(2, child2.getIndex());
    assertTrue(child2.hasProperty("prop2"));
    assertEquals(3, child3.getIndex());

    root.save();
//    System.out.println(">>"+session.getContainer());
    subRoot = root.getNode("subRoot");
    child1 = subRoot.getNode("child");
    assertEquals(1, child1.getIndex());
    assertTrue(child1.hasProperty("prop1"));
    NodeIterator children = subRoot.getNodes();
    assertEquals(3, (int)children.getSize());
    child1 = (Node)children.next();
    assertEquals(1, child1.getIndex());
    assertTrue(child1.hasProperty("prop1"));
    child2 = (Node)children.next();
    assertEquals(2, child2.getIndex());
    assertTrue(child2.hasProperty("prop2"));

    //read first same name sibling
    child1 = (Node)session.getItem("/subRoot/child");
    assertEquals("Not returned first item",1, child1.getIndex());


    subRoot.remove();
    root.save();
    //subRoot.save(); ipossible to call save() on removed node
  }

  public void testGetNodes() throws RepositoryException {
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    Node root = session.getRootNode();
    Node node = root.getNode("childNode");
    log.debug("ChildNode before refresh "+node);

    node.addNode("childNode4", "nt:folder");

    NodeIterator nodeIterator = node.getNodes();
    while(nodeIterator.hasNext()){
      node = (Node) nodeIterator.next();
      assertNotNull(node.getSession());
      if(!("childNode4".equals(node.getName()) || "childNode2".equals(node.getName())))
        fail("returned non expected nodes"+node.getName()+" "+node);
    }

    Session session2 = repository.login(credentials, WORKSPACE);
    Node root2 = session2.getRootNode();
    Node node2 = root2.getNode("childNode");
    Node node5 = node2.addNode("childNode5", "nt:folder");
    session2.save();

    session.refresh(false);

    node = root.getNode("childNode");
 //   log.debug("ChildNode after refresh "+node+" "+((NodeImpl)node).isChildNodesInitialized());

    nodeIterator = node.getNodes();
    
    while(nodeIterator.hasNext()){
      node = (Node) nodeIterator.next();
      if(!("childNode5".equals(node.getName()) || "childNode2".equals(node.getName())))
        fail("returned non expected nodes "+node.getName()+"  "+node);
    }

    node5.remove();
    session2.save();
  }
 

  public void testGetNodesWithNamePattern() throws RepositoryException{
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    Node root = session.getRootNode();
    Node node = root.getNode("childNode");
    node.addNode("childNode4", "nt:folder");
    node.addNode("otherNode", "nt:folder");
    node.addNode("lastNode", "nt:folder");

    Node result = (Node) node.getNodes("lastNode").next();
    assertEquals("lastNode", result.getName());

    NodeIterator iterator = node.getNodes("otherNode | lastNode");
    if(!iterator.hasNext())
      fail("nodes should have been found");
    while(iterator.hasNext()){
      Node nodeTmp = iterator.nextNode();
      if(!("otherNode".equals(nodeTmp.getName()) || "lastNode".equals(nodeTmp.getName())))
        fail("returned non expected nodes");
    }

    iterator = node.getNodes("childNode*");
    if(!iterator.hasNext())
      fail("nodes should have been found");
    while(iterator.hasNext()){
      Node nodeTmp = iterator.nextNode();
      if(!("childNode2".equals(nodeTmp.getName()) || "childNode4".equals(nodeTmp.getName())))
        fail("returned non expected nodes");
    }

    Session session2 = repository.login(credentials, WORKSPACE);
    Node root2 = session2.getRootNode();
    Node node2 = root2.getNode("childNode");
    node2.addNode("childNode5", "nt:folder");
    session2.save();

    session.refresh(false);
    node = root.getNode("childNode");
    iterator = node.getNodes("childNode*");
    if(!iterator.hasNext())
      fail("nodes should have been found");
    while(iterator.hasNext()){
      Node nodeTmp = iterator.nextNode();
      if(!("childNode2".equals(nodeTmp.getName()) || "childNode5".equals(nodeTmp.getName())))
        fail("returned non expected nodes");
    }
  }


  public void testGetProperty() throws RepositoryException {
    
    final String valueNew = "this is the NEW value";
    
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    Node root = session.getRootNode();
    Node node = root.getNode("childNode/childNode2/jcr:content");
    Property property = node.getProperty("jcr:data");
    assertEquals("this is the content", property.getString());

    Session session2 = repository.login(credentials, WORKSPACE);
    Node root2 = session2.getRootNode();
    Node node2 = root2.getNode("childNode/childNode2/jcr:content");
//    log.debug("Set prop");
    node2.getProperty("jcr:data").setValue(valueFactory.createValue(valueNew.toString(), PropertyType.BINARY));
    //node2.setProperty("jcr:data", valueFactory.createValue("this is the NEW value", PropertyType.BINARY));
    session2.save();
//    log.debug("Set prop end");

    assertEquals(valueNew.toString(), ((Property)session2.
    		getItem("/childNode/childNode2/jcr:content/jcr:data")).getString());
    
    assertEquals("this is the NEW value", root2.getNode("childNode/childNode2/jcr:content").
    		getProperty("jcr:data").getString());

    Session session3 = repository.login(credentials, WORKSPACE);
    Node root3 = session3.getRootNode();
    Node node3 = root3.getNode("childNode/childNode2/jcr:content");
    assertEquals(valueNew.toString(), ((Property)session3.
    		getItem("/childNode/childNode2/jcr:content/jcr:data")).getString());
    assertEquals(valueNew.toString(), node3.getProperty("jcr:data").getString());

    node.refresh(false);
//    session = repository.login(credentials, WORKSPACE);

    property = root.getNode("childNode/childNode2/jcr:content").getProperty("jcr:data");
    assertEquals("/childNode/childNode2/jcr:content/jcr:data", property.getPath());
    assertEquals(valueNew.toString(), property.getString());
  }


  public void testGetProperties() throws RepositoryException {
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    Node root = session.getRootNode();
    Node node = root.getNode("childNode");

    PropertyIterator iterator = node.getProperties();
    while(iterator.hasNext()){
      Property property = iterator.nextProperty();
      if(!("jcr:primaryType".equals(property.getName()) || "jcr:created".equals(property.getName()) ||
          "jcr:lastModified".equals(property.getName())))
        fail("returned non expected nodes");
    }

    Session session2 = repository.login(credentials, WORKSPACE);
    Node root2 = session2.getRootNode();
    Node node2 = root2.getNode("childNode/childNode2/jcr:content");
    node2.setProperty("jcr:data", session.getValueFactory().createValue("hehe", PropertyType.BINARY));
    session2.save();

    session.refresh(false);
    node = root.getNode("childNode/childNode2/jcr:content");
    iterator = node.getProperties();

    while(iterator.hasNext()){
      Property property = iterator.nextProperty();
      log.debug("PROP---"+property);
    }
  }


  public void testGetPropertiesWithNamePattern() throws RepositoryException {
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    Node root = session.getRootNode();
//    Node node = root.getNode("/childNode/childNode2/jcr:content");

    Node node = root.addNode("testNode", "nt:unstructured");

    node.setProperty("property1", "prop1Value");
    node.setProperty("property2", "prop2Value");

    PropertyIterator iterator = node.getProperties("property1 | property2");


    while(iterator.hasNext()){
      Property property = iterator.nextProperty();
      if(!("property1".equals(property.getName()) || "property2".equals(property.getName())))
        fail("returned non expected properties");
    }

    iterator = node.getProperties("property1 | jcr:*");

    while(iterator.hasNext()){
      Property property = iterator.nextProperty();
      if(!("property1".equals(property.getName()) || "jcr:primaryType".equals(property.getName())))
        fail("returned non expected properties");
    }


  }


  public void testGetPrimaryItem() throws RepositoryException {
    Node root = session.getRootNode();
    try {
      root.getPrimaryItem();
      fail("exception should have been thrown");
    } catch (RepositoryException e) {
      assertTrue(e instanceof ItemNotFoundException);
    }

    Node node = root.getNode("childNode/childNode2");
    Item item = node.getPrimaryItem();
    assertNotNull(item);
    assertEquals("jcr:content", item.getName());
  }


  public void testGetUUID() throws RepositoryException {
    Node root = session.getRootNode();
    try {
      root.getUUID();
      fail("exception should have been thrown");
    } catch (UnsupportedRepositoryOperationException e) {
    }
    Node node = root.getNode("childNode/childNode2/jcr:content");
    assertTrue(session.itemExists("/childNode/childNode2/jcr:content/jcr:uuid"));
    assertNotNull(node.getUUID());
  }

  public void testGetDefinition() throws RepositoryException {
    Node root = session.getRootNode();
    assertNotNull(root.getDefinition());
    assertEquals("*", root.getNode("childNode").getDefinition().getName());
    assertEquals("jcr:content", root.getNode("childNode").getNode("childNode2").getNode("jcr:content").getDefinition().getName());
  }

  public void testHasNode() throws RepositoryException {
    Node root = session.getRootNode();
    assertFalse(root.hasNode("dummyNode"));
    assertTrue(root.hasNode("childNode"));
 //   root.getNode("childNode").remove();
    //assertFalse(root.hasNode(""));
    
  }

  public void testHasNodes() throws RepositoryException {
    Node root = session.getRootNode();
    //System.out.println("Node>>>"+session.getItem("/childNode"));
    //System.out.println("Node>>>"+root.getNode("childNode"));
//    System.out.println("Node>>>"+root.getNodes().next());
    assertTrue(root.hasNodes());
//    Node node = root.getNode("/childNode/childNode2/jcr:content");
    Node node = root.addNode("tempNode", "nt:unstructured");
    node = node.addNode("tempNode1", "nt:unstructured");

    assertFalse(node.hasNodes());
  }


  public void testHasProperty() throws RepositoryException {
    Node root = session.getRootNode();
    assertFalse(root.hasProperty("dummyProperty"));
    assertTrue(root.getNode("childNode").hasProperty("jcr:created"));
  }

  public void testHasProperties() throws RepositoryException {
    Node root = session.getRootNode();
    Node node = root.getNode("childNode");
    assertTrue(node.hasProperties());
  }


  public void testGetNodesWithNamePatternAndSameNameSibs() throws RepositoryException{

//  The standard method for retrieving a set of such nodes is
//  Node.getNodes(String namePattern) which returns an iterator
//  over all the child nodes of the calling node that have the specified
//  pattern (by making namePattern just a name, without wildcards,
//  we can get all the nodes with that exact name, see section

      //fail("testGetNodesWithNamePatternAndSameNameSibs() TODO!");
    Node root = session.getRootNode();
    Node node = root.addNode("snTestNode");
    node.addNode("sn");
    node.addNode("sn");
    node.addNode("sn");
    
    NodeIterator i = node.getNodes("sn");
    assertEquals(3l, i.getSize()); 
   }

  public void testRemoveSameNameSiblings() throws Exception {
    
    Node testRoot = root.addNode("snsRemoveTest");
    session.save();
    
    try {
    
      Node node1 = testRoot.addNode("_node");
      node1.setProperty("prop", "_data1");
      Node node2 = testRoot.addNode("_node");
      node2.setProperty("prop", "_data2");
      
      testRoot.save();
      
      try {
        
        node1.remove(); // /snsRemoveTest/_node[2] -> /snsRemoveTest/_node[1]
        
        // check  
        String n2p = node2.getProperty("prop").getString();
        assertEquals("A property must be same ", "_data2", n2p);
      } catch(RepositoryException e) {
        e.printStackTrace();
        fail("A property must exists on the node /snsRemoveTest/_node[1] " + e);
      }
    } finally {
      testRoot.remove();
      session.save();
    }
  }

}
