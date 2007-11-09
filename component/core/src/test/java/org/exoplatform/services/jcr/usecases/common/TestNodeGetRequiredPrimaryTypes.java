/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 09.07.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestNodeGetRequiredPrimaryTypes.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestNodeGetRequiredPrimaryTypes extends BaseUsecasesTest {

  public void testRootNode() throws Exception {
    
    Node rootNode = session.getRootNode();
    NodeDefinition rnDefinition = rootNode.getDefinition();
    try {
      NodeType[] requiredPrimaryTypes = rnDefinition.getRequiredPrimaryTypes();
      assertNotNull("Root node: NodeDefinition.getRequiredPrimaryTypes() must not be null", requiredPrimaryTypes);
    } catch(Exception e) {
      fail("Root node: Error of NodeDefinition.getRequiredPrimaryTypes() call: " + e.getMessage());
    }
  }
  
  public void testUnstructuredNode() throws Exception {
    
    Node testUnstructured = session.getRootNode().addNode("testUnstructured", "nt:unstructured");
    session.save();
    NodeDefinition rnDefinition = testUnstructured.getDefinition();
    try {
      NodeType[] requiredPrimaryTypes = rnDefinition.getRequiredPrimaryTypes();
      assertNotNull("NodeDefinition.getRequiredPrimaryTypes() must not be null", requiredPrimaryTypes);
    } catch(Exception e) {
      fail("Error of NodeDefinition.getRequiredPrimaryTypes() call: " + e.getMessage());
    }
  }
}
