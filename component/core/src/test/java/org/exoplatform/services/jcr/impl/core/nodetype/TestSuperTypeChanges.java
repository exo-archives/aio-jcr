/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.core.nodetype;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestSuperTypeChanges extends JcrImplBaseTest {
  /**
   * Class logger.
   */
  private static final Log    LOG = ExoLogger.getLogger(TestSuperTypeChanges.class);

  private NodeTypeManagerImpl nodeTypeManager;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    nodeTypeManager = (NodeTypeManagerImpl) session.getWorkspace().getNodeTypeManager();
  }

  public void testAddVersionableSuper() throws Exception {
    // create new NodeType value
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testAddVersionableSuper");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);
    Node testNode = root.addNode("testNode", testNValue.getName());
    session.save();
    assertFalse(testNode.isNodeType("mix:versionable"));

    superType.add("mix:versionable");
    testNValue.setDeclaredSupertypeNames(superType);

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);

    assertTrue(testNode.isNodeType("mix:versionable"));
    testNode.checkin();
    testNode.checkout();
    testNode.remove();

    testNode = root.addNode("testNode", testNValue.getName());
    session.save();
    assertTrue(testNode.isNodeType("mix:versionable"));
  }

  public void testRemoveVersionableSuper() throws Exception {
    // create new NodeType value
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    superType.add("mix:versionable");
    testNValue.setName("exo:testRemoveVersionableSuper");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);
    Node testNode = root.addNode("testNode", testNValue.getName());
    session.save();
    assertTrue(testNode.isNodeType("mix:versionable"));

    superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setDeclaredSupertypeNames(superType);

    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (ConstraintViolationException e) {
      // ok
    }

    testNode.remove();
    session.save();

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);

    testNode = root.addNode("testNode", testNValue.getName());
    session.save();
    assertFalse(testNode.isNodeType("mix:versionable"));
  }
}
