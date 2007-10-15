/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.writing;


import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;


/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestSession.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestSession extends JcrAPIBaseTest{

  public void testSave() throws RepositoryException {
    Node root = session.getRootNode();
    try {
      root.addNode("childNode", "nt:folder").addNode("childNode2", "nt:propertyDefinition");
      session.save();
      fail("exception should have been thrown");
    } catch (ConstraintViolationException e) {
    }

    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    root = session.getRootNode();
    try {
      root.getNode("childNode/childNode2");
      fail("exception should have been thrown");
    } catch (PathNotFoundException e) {
    }

    Node node = root.addNode("nodeType", "nt:base");
  	session.save();
    root.getNode("nodeType").remove();
    session.save();
  }

  public void testRefresh() throws RepositoryException {
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    Node root = session.getRootNode();
    Node node = root.addNode("nodeType", "exo:mockNodeType");
    node.addNode("jcr:childNodeDefinition", "nt:childNodeDefinition");
    session.refresh(false);
    try {
      root.getNode("nodeType");
      fail("exception should have been thrown");
    } catch (PathNotFoundException e) {
    }
    session.save();

    // Test refresh(true)
    /////////////////////
  }

  public void testHasPendingChanges() throws RepositoryException {
    assertFalse(session.hasPendingChanges());
    session.getRootNode().addNode("test", "nt:unstructured");
    assertTrue(session.hasPendingChanges());
    session.save();
    assertFalse(session.hasPendingChanges());
  }

  public void testSaveWithUUID() throws RepositoryException {
  }


  public void testPropertiesManipThenSave() throws RepositoryException {
    Node root = session.getRootNode();
    Node node = root.addNode("testPropertiesManipThenSave", "nt:unstructured");
    node.addNode("node2BRem", "nt:unstructured");
    node.setProperty("existingProp", "existingValue");
    node.setProperty("existingProp2", "existingValue2");
    session.save();
    node.setProperty("prop", "propValue");
    node.setProperty("existingProp", "existingValueBis");
    node.getProperty("existingProp2").remove();
    node.getNode("node2BRem").remove();
    node.addNode("addedNode", "nt:unstructured");
    session.save();

    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    node = session.getRootNode().getNode("testPropertiesManipThenSave");
    root = session.getRootNode();
    try {
      node.getProperty("prop");
    } catch (PathNotFoundException e) {
      e.printStackTrace();
      fail("exception should not be thrown");
    }
    assertEquals("existingValueBis", node.getProperty("existingProp").getString());
    try {
      node.getProperty("existingProp2");
      fail("exception should have been thrown");
    } catch (PathNotFoundException e) {
    }
    try {
      node.getNode("node2BRem");
      fail("exception should have been thrown");
    } catch (PathNotFoundException e) {
    }
    node.getNode("addedNode");

//    System.out.println("REMOVE childNode");
    root.getNode("testPropertiesManipThenSave").remove();
//    System.out.println("SAVE childNode");
    session.save();
//    System.out.println("REMOVED");
  }

}
