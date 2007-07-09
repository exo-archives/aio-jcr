package org.exoplatform.services.jcr.api.nodetypes;


import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestDiscoveringNodeTypeDefinition.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestDiscoveringNodeTypeDefinition extends JcrAPIBaseTest {

  public void testPrimaryNodeTypeDefinitionProperties() throws Exception {
    Node node = root.addNode("node1", "nt:resource");
    NodeType type = node.getPrimaryNodeType();
    assertEquals("nt:resource", type.getName());
    assertEquals(false, type.isMixin());
    assertEquals(false, type.hasOrderableChildNodes());
    assertEquals("jcr:data", type.getPrimaryItemName());
    assertEquals(2, type.getSupertypes().length);
    assertEquals(2, type.getDeclaredSupertypes().length);
    assertEquals(true, type.isNodeType("nt:base"));
    assertEquals(false, type.isNodeType("nt:file"));
    assertEquals(7, type.getPropertyDefinitions().length);
    assertEquals(4, type.getDeclaredPropertyDefinitions().length);
    assertEquals(0, type.getChildNodeDefinitions().length);
    assertEquals(0, type.getDeclaredChildNodeDefinitions().length);
  }

  public void testMixinNodeTypeDefinitionProperties() throws Exception {
    Node node = root.addNode("node1", "nt:base");
    node.addMixin("mix:referenceable");
    assertEquals(1, node.getMixinNodeTypes().length);
    NodeType type = node.getMixinNodeTypes()[0];
    assertEquals("mix:referenceable", type.getName());
    assertEquals(true, type.isMixin());
    assertEquals(false, type.hasOrderableChildNodes());
    assertNull(type.getPrimaryItemName());
    assertEquals(0, type.getSupertypes().length);
    assertEquals(0, type.getDeclaredSupertypes().length);
    assertEquals(false, type.isNodeType("nt:base"));
    assertEquals(1, type.getPropertyDefinitions().length);
    assertEquals(1, type.getDeclaredPropertyDefinitions().length);
    assertEquals(0, type.getChildNodeDefinitions().length);
    assertEquals(0, type.getDeclaredChildNodeDefinitions().length);
  }

  public void testCanModify() throws Exception {
    Node node = root.addNode("node1", "nt:resource");
    NodeType type = node.getPrimaryNodeType();
    assertFalse(type.canAddChildNode("jcr:anyNode"));
    assertFalse(type.canAddChildNode("jcr:anyNode", "nt:base"));
    //assertTrue(type.canSetProperty("jcr:data", new BinaryValue("test")));
    assertFalse(type.canSetProperty("jcr:data", new BinaryValue[] {new BinaryValue("test")}));
    assertFalse(type.canSetProperty("jcr:notFound", new BinaryValue("test")));
    // [PN] 06.03.06 Row below commented
    //assertFalse(type.canSetProperty("jcr:data", new StringValue("test")));
    assertFalse(type.canRemoveItem("jcr:data"));
    assertFalse(type.canRemoveItem("jcr:notFound"));

    node = root.addNode("node2", "nt:file");
    type = node.getPrimaryNodeType();
    //    does not work - TODO
    //assertTrue(type.canAddChildNode("jcr:content"));
    assertTrue(type.canAddChildNode("jcr:content", "nt:unstructured"));
    assertFalse(type.canAddChildNode("jcr:othernode"));
    assertTrue(type.canAddChildNode("jcr:content", "nt:unstructured"));
    assertFalse(type.canAddChildNode("jcr:content", "mix:referenceable"));

//    root.getNode("node2").remove();
    node = root.addNode("node3", "nt:folder");
    type = node.getPrimaryNodeType();
    // Residual, 
    // 6.7.22.8 nt:folder, ChildNodeDefinition, Name * RequiredPrimaryType[nt:hierarchyNode] 
    assertTrue(type.canAddChildNode("jcr:content", "nt:hierarchyNode"));
    assertFalse(type.canAddChildNode("jcr:othernode"));

    //    does not work - TODO
    //assertTrue(type.canAddChildNode("jcr:content", "nt:unstructured"));
    //assertTrue(type.canAddChildNode("jcr:content", "mix:referenceable"));

  }
}
