/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.nodetypes;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL Author : Hoa Pham hoa.pham@exoplatform.com
 * phamvuxuanhoa@yahoo.com Jul 3, 2006
 */
public class TestNodeTypeRegister extends BaseUsecasesTest {

  public void testRegisterNodeType() throws Exception {
    Session session = repository.getSystemSession(repository.getSystemWorkspaceName());
    NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
    NodeTypeValue nodeTypeValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    nodeTypeValue.setName("exo:testNodeType");
    nodeTypeValue.setPrimaryItemName("");
    ExtendedNodeTypeManager extNodeTypeManager = (ExtendedNodeTypeManager) nodeTypeManager;
    try {
      nodeTypeManager.getNodeType("exo:testNodeType");
      fail("Node Type is registed");
    } catch (Exception e) {
    }

    try {
      extNodeTypeManager.registerNodeType(nodeTypeValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);
    } catch (NullPointerException e) {
      fail("something wrong and registerNodeType() throws NullPointException");
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      NodeType nodeType = nodeTypeManager.getNodeType("exo:testNodeType");
      assertNotNull(nodeType);
    } catch (Exception e) {
    }

  }
}
