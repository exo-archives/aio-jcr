/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
import javax.jcr.RepositoryException;

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
public class TestNodeTypeRegistration extends JcrImplBaseTest {
  /**
   * Class logger.
   */
  private static final Log    LOG               = ExoLogger.getLogger(TestNodeTypeRegistration.class);

  private NodeTypeValue       testNodeTypeValue = null;

  private NodeTypeManagerImpl nodeTypeManager;

  /**
   * 
   */
  public TestNodeTypeRegistration() {
    super();
    testNodeTypeValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNodeTypeValue.setName("exo:testRegistrationNodeType");
    testNodeTypeValue.setPrimaryItemName("");
    testNodeTypeValue.setDeclaredSupertypeNames(superType);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    nodeTypeManager = (NodeTypeManagerImpl) session.getWorkspace().getNodeTypeManager();
  }

  public void testRemoveNodeTypeUnexisted() {
    try {
      nodeTypeManager.unregisterNodeType("blah-blah");
      fail();
    } catch (RepositoryException e) {
      // ok
    }
  }

  public void testRemoveBuildInNodeType() {
    try {
      nodeTypeManager.unregisterNodeType("nt:base");
      fail();
    } catch (RepositoryException e) {
      // ok
    }
  }

  public void testRemoveNodeTypeExistedNode() throws Exception {
    nodeTypeManager.registerNodeType(testNodeTypeValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);
    Node testNode = root.addNode("test", testNodeTypeValue.getName());
    assertTrue(testNode.isNodeType(testNodeTypeValue.getName()));
    session.save();
    try {
      nodeTypeManager.unregisterNodeType(testNodeTypeValue.getName());
      fail("");
    } catch (RepositoryException e) {
      // ok
    }

  }
}
