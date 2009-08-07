package org.exoplatform.services.jcr.api.nodetypes;

import javax.jcr.Node;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id$
 */

public class TestPredefinedMixinNodeTypes extends JcrAPIBaseTest {

  public void testReferenceable() throws Exception {

    Node node;
    try {
      node = root.addNode("node-h", "mix:referenceable");
      fail("AddNode ConstraintViolationException should be thrown as type is not primary!");
    } catch (ConstraintViolationException e) {
    }

    node = root.addNode("node-h", "nt:unstructured");

    node.addMixin("mix:referenceable");
    assertEquals(1, node.getMixinNodeTypes().length);
    assertEquals("mix:referenceable", node.getMixinNodeTypes()[0].getName());
    assertEquals("nt:unstructured", node.getPrimaryNodeType().getName());

    assertNotNull(node.getProperty("jcr:uuid").toString());
    assertEquals("jcr:uuid", node.getProperty("jcr:uuid").getDefinition().getName());
    assertTrue(node.getProperty("jcr:mixinTypes").getDefinition().isProtected());
    assertTrue(node.getProperty("jcr:mixinTypes").getDefinition().isMultiple());

    assertTrue(node.getProperty("jcr:uuid").getDefinition().isProtected());
    assertFalse(node.getProperty("jcr:uuid").getDefinition().isMultiple());

    root.save();
    node = root.getNode("node-h");
    assertNotNull("Prop not null ", node.getProperty("jcr:uuid").toString());

    // UUID Read Only
    try {
      node.setProperty("jcr:uuid", "1234");
      node.save();
      fail("SetProp UUID ConstraintViolationException should be thrown!");
    } catch (ConstraintViolationException e) {
    }
  }

}
