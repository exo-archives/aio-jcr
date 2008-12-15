package org.exoplatform.services.jcr.api.nodetypes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.core.nodetype.ExtendedItemDefinition;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.util.EntityCollection;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestPredefinedPrimaryNodeTypes.java 11907 2008-03-13 15:36:21Z
 *          ksm $
 */

public class TestPredefinedPrimaryNodeTypes extends JcrAPIBaseTest {

  public void testUnstructured() throws Exception {
    Node node = root.addNode("node1", "nt:unstructured");
    NodeDefinition def = node.getDefinition();
    NodeType type = node.getPrimaryNodeType();

    assertTrue("have child ", ((EntityCollection) node.getNodes()).size() == 0);
    assertTrue("prop num !=1 ", ((EntityCollection) node.getProperties()).size() == 1);
    assertEquals("Prop not default ", "nt:unstructured", node.getProperty("jcr:primaryType")
                                                             .getString());

    assertEquals("Type!= nt:unstructured", type.getName(), "nt:unstructured");

    assertTrue("typeNodeDefs != 1", type.getChildNodeDefinitions().length == 1);
    assertTrue("typePropDefs != 4", type.getPropertyDefinitions().length == 4);

    // assertEquals("prop!=jcr:primaryType", "jcr:primaryType",
    // type.getPropertyDefinitions()[1].getName());
    assertEquals(Constants.JCR_ANY_NAME.getName(), type.getPropertyDefinitions()[0].getName());
    assertEquals(Constants.JCR_ANY_NAME.getName(), type.getChildNodeDefinitions()[0].getName());

  }

  public void testHierarchyNode() throws Exception {

    Node node = root.addNode("node-hi", "nt:hierarchyNode");
    NodeDefinition def = node.getDefinition();
    NodeType type = node.getPrimaryNodeType();

    assertTrue("have child ", ((EntityCollection) node.getNodes()).size() == 0);
    assertTrue("prop num !=2 ==" + ((EntityCollection) node.getProperties()).size(),
               ((EntityCollection) node.getProperties()).size() == 2);

    assertTrue("typePropDefs != 3", type.getPropertyDefinitions().length == 3);
    // NodeDefs = null
    assertTrue("nodeDefs != 0", type.getChildNodeDefinitions().length == 0);

    // Property names: [0]=jcr:created, [1]=jcr:lastModified,
    // [2]=jcr:primaryType
    assertEquals("prop2 name !=jcr:primaryType",
                 "jcr:primaryType",
                 type.getPropertyDefinitions()[1].getName());
    assertEquals("prop0 name != jcr:created",
                 "jcr:created",
                 type.getPropertyDefinitions()[0].getName());

    node = root.getNode("node-hi");
    assertNotNull("Prop null ", node.getProperty("jcr:created").toString());
    // assertNull("Prop modified SAVED not null ",
    // node.getProperty("jcr:lastModified").getValue());
  }

  public void testFile() throws Exception {

    Node node = root.addNode("node-f", "nt:file");
    NodeType type = node.getPrimaryNodeType();

    assertEquals("Type!= nt:file", "nt:file", type.getName());
    assertTrue("typePropDefs != 3", type.getPropertyDefinitions().length == 3);
    assertTrue("typeNodeDefs != 1", type.getChildNodeDefinitions().length == 1);

    // Property names: [0]=jcr:created, [2]=jcr:primaryType
    assertEquals("node0 name != jcr:content",
                 "jcr:content",
                 type.getChildNodeDefinitions()[0].getName());

    try {
      node.addNode("not-allowed");
      fail("AddNode ConstraintViolationException should be thrown!");
    } catch (ConstraintViolationException e) {
    }

    try {
      node.setProperty("not-allowed", "val");
      node.save();
      fail("SetProp ConstraintViolationException should be thrown!");
    } catch (RepositoryException e) {
    }

  }

  public void testFolder() throws Exception {

    Node node = root.addNode("node-fl", "nt:folder");
    NodeType type = node.getPrimaryNodeType();

    assertEquals("Type!= nt:folder", "nt:folder", type.getName());
    assertTrue("typePropDefs != 3", type.getPropertyDefinitions().length == 3);
    assertTrue("typeNodeDefs != 1", type.getChildNodeDefinitions().length == 1);

    assertEquals(ExtendedItemDefinition.RESIDUAL_SET, type.getChildNodeDefinitions()[0].getName());

    try {
      node.setProperty("not-allowed", "val");
      node.save();
      fail("SetProp ConstraintViolationException should be thrown!");
    } catch (RepositoryException e) {
    }

  }

  public void testMimeResource() throws Exception {

    Node node = root.addNode("node-mr", "nt:resource");
    NodeType type = node.getPrimaryNodeType();

    assertEquals("Type!=nt:resource", "nt:resource", type.getName());
    PropertyDefinition[] propDefs = type.getPropertyDefinitions();

    // 4 + primaryType, mixinType, uuid
    assertTrue("typePropDefs = " + type.getPropertyDefinitions().length,
               type.getPropertyDefinitions().length == 7);
    assertTrue("typeNodeDefs != 0", type.getChildNodeDefinitions().length == 0);

  }

  public void testLinkedFile() throws Exception {

    Node node = root.addNode("node-lf", "nt:linkedFile");
    NodeType type = node.getPrimaryNodeType();

    assertEquals("nt:linkedFile", type.getName());
    assertTrue("typePropDefs != 4", type.getPropertyDefinitions().length == 4);
    assertTrue("typeNodeDefs != 0", type.getChildNodeDefinitions().length == 0);

    assertEquals("node0 name != jcr:content",
                 "jcr:content",
                 type.getPropertyDefinitions()[0].getName());

  }

  public void testNodeType() throws Exception {

    Node node = root.addNode("node-nt", "nt:nodeType");
    NodeType type = node.getPrimaryNodeType();

    assertEquals("nt:nodeType", type.getName());

    assertTrue(type.getPropertyDefinitions().length == 7);
    assertTrue(type.getChildNodeDefinitions().length == 2);

  }

  public void testPropertyDef() throws Exception {

    Node node = root.addNode("node-pd", "nt:propertyDefinition");
    NodeType type = node.getPrimaryNodeType();

    assertEquals("nt:propertyDefinition", type.getName());

    assertTrue(type.getPropertyDefinitions().length == 11);
    assertTrue(type.getChildNodeDefinitions().length == 0);

  }

  public void testChildNodeDef() throws Exception {

    Node node = root.addNode("node-cnd", "nt:childNodeDefinition");
    NodeType type = node.getPrimaryNodeType();

    assertEquals("nt:childNodeDefinition", type.getName());

    assertTrue(type.getPropertyDefinitions().length == 10);
    assertTrue(type.getChildNodeDefinitions().length == 0);

  }
}
