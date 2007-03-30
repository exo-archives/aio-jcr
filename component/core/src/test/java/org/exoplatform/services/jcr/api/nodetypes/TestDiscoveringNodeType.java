package org.exoplatform.services.jcr.api.nodetypes;


import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestDiscoveringNodeType.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestDiscoveringNodeType extends JcrAPIBaseTest {

  public void testGetPrimaryNodeType() throws Exception {
  
    Node node = root.addNode("node1", "nt:unstructured");

    NodeType type = node.getPrimaryNodeType();
    assertEquals("nt:unstructured", type.getName());

  }

  public void testGetMixinNodeTypes() throws Exception {
  
    Node node = root.addNode("node1", "nt:unstructured");
    assertEquals(0, node.getMixinNodeTypes().length);

    node.addMixin("mix:referenceable");
    assertEquals(1, node.getMixinNodeTypes().length);

    NodeType type = node.getMixinNodeTypes()[0];
    assertEquals("mix:referenceable", type.getName());

  }

  public void testIsNodeType() throws Exception {
  
    Node node = root.addNode("node1", "nt:unstructured");
    assertFalse(node.isNodeType("mix:referenceable"));
    node.addMixin("mix:referenceable");

    assertTrue(node.isNodeType("nt:unstructured"));
    assertTrue("Not nt:base", node.isNodeType("nt:base"));
    assertTrue(node.isNodeType("mix:referenceable"));

    assertFalse(node.isNodeType("nt:file"));
    assertFalse(node.isNodeType("mix:notfound"));

  }

}
