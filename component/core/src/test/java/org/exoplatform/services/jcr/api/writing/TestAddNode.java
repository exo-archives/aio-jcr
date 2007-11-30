/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.api.writing;


import java.util.Calendar;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestAddNode.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestAddNode extends JcrAPIBaseTest{

  public void initRepository() throws RepositoryException {
    Node root = session.getRootNode();
    Node file = root.addNode("TestNodesTree", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode = file.getNode("jcr:content");
    contentNode.setProperty("jcr:data", session.getValueFactory().createValue("this is the content", PropertyType.BINARY));
    contentNode.setProperty("jcr:mimeType", session.getValueFactory().createValue("text/html"));
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));

    session.save();
  }

  public void tearDown() throws Exception {
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    Node root = session.getRootNode();
    Node node = root.getNode("TestNodesTree");
    node.remove();
    root.save();
    //node.save();ipossible to call save() on removed node
    
    super.tearDown();
  }

  public void testAddNode() throws RepositoryException {
    Node root = session.getRootNode();

    // If relPath implies intermediary nodes that do not exist,
    // then a PathNotFoundException is thrown.
    try {
      root.addNode("notExisting/dummy");
      fail("exception should have been thrown");
    } catch (PathNotFoundException e) {
    }

    //If an item already exists at relPath, and same-name
    //siblings are not permitted, then an ItemExistsException
    //is thrown immediately (not on save).
    try {
      root.getNode("TestNodesTree").addNode("childNode2", "nt:file");
      fail("exception should have been thrown");
    } catch (ItemExistsException e) {
    }

    try {
      root.addNode("TestNodesTree/childNode2/jcr:primaryType/test");
      fail("exception should have been thrown");
    } catch (ConstraintViolationException e) {
    }

    // undefined nodetype
    Node node = root.getNode("TestNodesTree/childNode2");
    try {
      node.addNode("notJcrContentNode");
      fail("exception should have been thrown");
    } catch (ConstraintViolationException e) {
    }
  }

  public void testAddAndSaveNodeWithNodeType() throws RepositoryException {

    Node root = session.getRootNode();
    Node node = root.getNode("TestNodesTree/childNode2");
    try {
      node.addNode("dummy", "nt:base");
      session.save();
      fail("exception should have been thrown");
    } catch (ConstraintViolationException e) {
    }
    
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    root = session.getRootNode();
    node = root.getNode("TestNodesTree");
    try {
      node.addNode("dummy1", "nt:dymyNT");
      fail("exception should have been thrown");
    } catch (NoSuchNodeTypeException e) {
    }

  }

  public void testIfAddedNodeAppearsInGetNodesImmediatelly() throws RepositoryException {
    Node root = session.getRootNode();
    Node root1 = root.addNode("root1", "nt:unstructured");
    assertEquals(0, root1.getNodes().getSize());
    root1.addNode("node1", "nt:unstructured");
    assertEquals(1, root1.getNodes().getSize());
    assertTrue(root1.hasNode("node1"));
    assertNotNull(root1.getNode("node1"));
  }

  public void testAddingSameNameSibs() throws RepositoryException {

    // the same node already exists but same name sibs
    Node subRoot = root.addNode("subRoot", "nt:unstructured");
    subRoot = root.addNode("subRoot", "nt:unstructured");
    Node node1=subRoot.addNode("TestNodesTree", "nt:unstructured");
    Node node2=subRoot.addNode("TestNodesTree", "nt:unstructured");
    assertNotNull(node1);
    NodeIterator nodes = subRoot.getNodes();
    assertEquals(2, (int)nodes.getSize());
    try {
      subRoot.addNode("TestNodesTree[3]", "nt:unstructured");
      fail("exception should have been thrown");
    } catch (RepositoryException e) {
    }

  }

  public void testInvalidItemStateException() throws RepositoryException {
    Node n = session.getRootNode().addNode("sameNode", "nt:base");
    
    Session session2 = repository.login(credentials, "ws");
    Node n2 = session2.getRootNode().addNode("sameNode", "nt:base");
    session.save();
    
    try {
      session2.save();
      fail("InvalidItemStateException should have been thrown");
    } catch (ItemExistsException e) {
    }
  }

  public void testAddingNodeToDiffWorkspaces() throws RepositoryException {
  	
  	String[] wsNames = repository.getWorkspaceNames();
  	if(wsNames.length < 2)
  		fail("Too few number of ws for test should be > 1");
  	log.debug(">>>>>>>> "+wsNames.length);
  	for(int i=0;i<wsNames.length;i++)
  		log.debug(">>>>>>>> "+wsNames[i]);
  			
//  	Session s1 = repository.login(credentials, wsNames[0]);
  	Session s1 = repository.getSystemSession(wsNames[0]);

  	s1.getRootNode().addNode("test1", "nt:unstructured");
  	s1.save();
  	s1 = repository.login(credentials, wsNames[0]);
  	log.debug("S1 >>>>"+wsNames[0]+" : "+s1.getItem("/test1"));
  	log.debug("S1 >>>>"+wsNames[0]+" : "+s1+" "+s1.getWorkspace().getName());
  	
  	//Session s2 = repository.getSystemSession(wsNames[1]);

  	//s2.getRootNode().addNode("test2", "nt:unstructured");
  	//s2.save();
  	//s2 = repository.login(credentials, wsNames[1]);
  	//log.debug("S2 "+wsNames[1]+" : "+s2.getItem("/test2"));
  	//log.debug("S2 >>>>"+wsNames[1]+" : "+s2+" "+s2.getWorkspace().getName());
  }

  public void testDeleteAndAddNode() throws RepositoryException {
    log.debug(">>> start getRoot");
    Node root = session.getRootNode();
    log.debug(">>> start remove");
    root.getNode("TestNodesTree").remove();
    log.debug(">>> end remove");
    root.addNode("TestNodesTree");
    log.debug(">>> end add");
    root.save();
    log.debug(">>> end save");
    log.debug(">>> "+root.getNode("TestNodesTree"));
  }
  
}
