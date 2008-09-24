package org.exoplatform.services.jcr.api.nodetypes;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestNodeTypeManager.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class TestNodeTypeManager extends JcrAPIBaseTest {

  public void testGetNodeType() throws Exception {

    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    NodeType type = ntManager.getNodeType("nt:unstructured");
    assertEquals("nt:unstructured", type.getName());

    try {
      ntManager.getNodeType("nt:not-found");
      fail("exception should have been thrown");
    } catch (NoSuchNodeTypeException e) {
    }

  }

  public void testGetNodeTypes() throws Exception {
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    assertTrue(ntManager.getAllNodeTypes().getSize() > 0);
    assertTrue(ntManager.getPrimaryNodeTypes().getSize() > 0);
    assertTrue(ntManager.getMixinNodeTypes().getSize() > 0);
    // assertEquals("nt",ntManager.getPrimaryNodeTypes().nextNodeType().getName().substring(0,2));
    // assertEquals("mix",ntManager.getMixinNodeTypes().nextNodeType().getName().substring(0,3));
  }

  public void testNodeTypesOrder() throws Exception {
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    NodeTypeIterator nts = ntManager.getPrimaryNodeTypes();
    assertTrue(nts.getSize() > 0);
    assertEquals("nt:base", nts.nextNodeType().getName());
    // Prerequisites : the second entry in nodetypes.xml should be "nt:unstructured" !!!!!
    assertEquals("nt:unstructured", nts.nextNodeType().getName());
  }

}
