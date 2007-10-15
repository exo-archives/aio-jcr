/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.reading;


import java.util.Calendar;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestItem.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestItem extends JcrAPIBaseTest{

  public void initRepository() throws RepositoryException {

    Node root = session.getRootNode();
    Node file = root.addNode("childNode", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
//    System.out.println(" >>>>>>>>>");
    contentNode.setProperty("jcr:data", session.getValueFactory().createValue("this is the content", PropertyType.BINARY));
    contentNode.setProperty("jcr:mimeType", session.getValueFactory().createValue("text/html"));
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));
    
//    System.out.println(" >>>>>>>>>"+((SessionImpl)session).getNodesManager());
//    System.out.println(" >>>>>>>>>"+((SessionImpl)session).getContainer());
    session.save();
  }

  public void tearDown() throws Exception {
    Node root = session.getRootNode();
    root.getNode("childNode").remove();
    session.save();
    
    super.tearDown();
  }
  
  public void testGetPath() throws RepositoryException {
    Node root = session.getRootNode();
    Property property = root.getProperty("childNode/childNode2/jcr:content/jcr:data");
    Item item = property.getAncestor(3);
    assertEquals("/childNode/childNode2/jcr:content", item.getPath());
  }

  public void testGetName() throws RepositoryException {
    Node root = session.getRootNode();
    Property property = root.getProperty("childNode/childNode2/jcr:content/jcr:data");

    Item item = property.getAncestor(0);
    assertEquals("", item.getName());

    item = property.getAncestor(1);
    assertEquals("childNode", item.getName());
  }

  public void testGetAncestor() throws RepositoryException {
    Node root = session.getRootNode();
    Property property = root.getProperty("childNode/childNode2/jcr:content/jcr:data");

    Item item = property.getAncestor(0);
    assertEquals(root.getPath(), item.getPath());

    item = property.getAncestor(1);
    assertEquals("/childNode", item.getPath());
    assertEquals("childNode", item.getName());
    assertTrue(item instanceof Node);

    item = property.getAncestor(2);
    assertEquals("/childNode/childNode2", item.getPath());
    assertEquals("childNode2", item.getName());
    assertTrue(item instanceof Node);

    item = property.getAncestor(4);
    assertEquals(property, item);
    assertTrue(item instanceof Property);

    try {
      item = property.getAncestor(5);
      fail("exception should have been thrown");
    } catch (ItemNotFoundException e) {
    }
  }


  public void testGetParent() throws RepositoryException {
    Node root = session.getRootNode();
    Property property = root.getProperty("childNode/childNode2/jcr:content/jcr:data");
    Item item = property.getAncestor(4);
    assertEquals("jcr:content", item.getParent().getName());

    try {
      root.getParent();
      fail("exception should have been thrown");
    } catch (ItemNotFoundException e) {
    }

  }

  public void testGetDepth() throws RepositoryException {
    Node root = session.getRootNode();
    assertEquals(0, root.getDepth());
    Property property = root.getProperty("childNode/childNode2/jcr:content/jcr:data");
    assertEquals(4, property.getDepth());
  }

  public void testGetSession() throws RepositoryException {
    Node root = session.getRootNode();
    Property property = root.getProperty("childNode/childNode2/jcr:content/jcr:data");
    assertEquals(session, property.getSession());
  }

  public void testIsNode() throws RepositoryException {
    Node root = session.getRootNode();
    assertTrue(root.isNode());
    Property property = root.getProperty("childNode/childNode2/jcr:content/jcr:data");
    assertFalse(property.isNode());
  }

  public void testIsSame() throws RepositoryException {

    Node root = session.getRootNode();
    Node contentNode = root.getNode("childNode/childNode2/jcr:content");

    Session session2 = repository.login(credentials, WORKSPACE);
    root = session2.getRootNode();
    Item contentNode2 = root.getNode("childNode/childNode2/jcr:content");

    assertTrue(contentNode.isSame(contentNode));
    assertFalse(contentNode2.isSame(root));
    assertFalse(contentNode2.isSame(contentNode.getProperty("jcr:data")));
  }





}
