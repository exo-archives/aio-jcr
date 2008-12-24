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
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
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
  private static final Log    LOG                     = ExoLogger.getLogger(TestNodeTypeRegistration.class);

  private NodeTypeValue       testNodeTypeValue       = null;

  private NodeTypeValue       testNodeTypeValue2      = null;

  private NodeTypeValue       testNtFileNodeTypeValue = null;

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

    testNodeTypeValue2 = new NodeTypeValue();
    List<String> superType2 = new ArrayList<String>();
    superType2.add("nt:base");
    superType2.add(testNodeTypeValue.getName());
    testNodeTypeValue2.setName("exo:testRegistrationNodeType2");
    testNodeTypeValue2.setPrimaryItemName("");
    testNodeTypeValue2.setDeclaredSupertypeNames(superType2);

    testNtFileNodeTypeValue = new NodeTypeValue();
    List<String> superType3 = new ArrayList<String>();
    superType3.add("nt:base");
    testNtFileNodeTypeValue.setName("nt:file");
    testNtFileNodeTypeValue.setPrimaryItemName("");
    testNtFileNodeTypeValue.setDeclaredSupertypeNames(superType3);

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

  /**
   * Test remove of build in node type
   */
  public void testRemoveBuildInNodeType() {
    try {
      nodeTypeManager.unregisterNodeType("nt:base");
      fail();
    } catch (RepositoryException e) {
      // ok
    }
  }

  /**
   * @throws RepositoryException
   */
  public void testRemoveSuperNodeType() throws RepositoryException {
    nodeTypeManager.registerNodeType(testNodeTypeValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);
    nodeTypeManager.registerNodeType(testNodeTypeValue2, ExtendedNodeTypeManager.FAIL_IF_EXISTS);
    try {
      nodeTypeManager.unregisterNodeType(testNodeTypeValue.getName());
      fail();
    } catch (RepositoryException e) {
      // ok
    }
    nodeTypeManager.unregisterNodeType(testNodeTypeValue2.getName());
    nodeTypeManager.unregisterNodeType(testNodeTypeValue.getName());
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

  }

  /**
   * @throws Exception
   */
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
    testNode.remove();
    session.save();
    nodeTypeManager.unregisterNodeType(testNodeTypeValue.getName());
  }

  public void testReregisterBuildInNodeType() throws Exception {
    try {
      nodeTypeManager.registerNodeType(testNtFileNodeTypeValue,
                                       ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (RepositoryException e) {
      // ok
    }
  }

  /**
   * Remove residual property definition. Cover
   * PropertyDefinitionComparator.validateRemoved method.
   * 
   * @throws Exception
   */
  public void testReregisterResidual() throws Exception {
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testRemoveResidual");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);
    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();
    props.add(new PropertyDefinitionValue("*",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          0,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);
    assertTrue(nodeTypeManager.getNodeType(testNValue.getName()).getDeclaredPropertyDefinitions().length == 1);

    Node tNode = root.addNode("test", "exo:testRemoveResidual");
    Property prop = tNode.setProperty("tt", "tt");
    session.save();

    testNValue.setDeclaredPropertyDefinitionValues(new ArrayList<PropertyDefinitionValue>());

    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (RepositoryException e) {
      // ok
    }

    prop.remove();
    session.save();
    assertTrue(nodeTypeManager.getNodeType(testNValue.getName()).getDeclaredPropertyDefinitions().length == 1);
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
    assertTrue(nodeTypeManager.getNodeType(testNValue.getName()).getDeclaredPropertyDefinitions().length == 0);
    tNode.remove();
    session.save();
    nodeTypeManager.unregisterNodeType(testNValue.getName());
  }

  /**
   * Cover part of the PropertyDefinitionComparator.doChanged method.
   * 
   * @throws Exception
   */
  public void testReregisterProtected() throws Exception {
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testChangeProtected");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);
    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();

    List<String> def = new ArrayList<String>();
    def.add("tt");
    props.add(new PropertyDefinitionValue("tt",
                                          true,
                                          false,
                                          1,
                                          false,
                                          def,
                                          false,
                                          PropertyType.STRING,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node tNode = root.addNode("test", "exo:testChangeProtected");
    session.save();
    Property property = tNode.getProperty("tt");
    assertEquals("tt", property.getString());

    property.remove();
    session.save();

    tNode.addMixin("mix:versionable");

    // chenge mandatory
    List<PropertyDefinitionValue> props2 = new ArrayList<PropertyDefinitionValue>();
    props2.add(new PropertyDefinitionValue("tt",
                                           true,
                                           true,
                                           1,
                                           true,
                                           def,
                                           false,
                                           PropertyType.STRING,
                                           new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props2);
    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (RepositoryException e) {
      // ok;
    }
  }

  /**
   * Cover PropertyDefinitionComparator.validateAdded method.
   * 
   * @throws Exception
   */
  public void testReregisterAddNewProperty() throws Exception {
    NodeTypeValue testNTValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNTValue.setName("exo:testReregisterAddNewProperty");
    testNTValue.setPrimaryItemName("");
    testNTValue.setDeclaredSupertypeNames(superType);

    nodeTypeManager.registerNodeType(testNTValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node testNode = root.addNode("testNode", testNTValue.getName());
    session.save();

    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();
    props.add(new PropertyDefinitionValue("tt",
                                          true,
                                          true,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.STRING,
                                          new ArrayList<String>()));
    testNTValue.setDeclaredPropertyDefinitionValues(props);

    try {
      nodeTypeManager.registerNodeType(testNTValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (RepositoryException e) {
      // ok
    }

    List<String> def = new ArrayList<String>();
    def.add("tt");
    props = new ArrayList<PropertyDefinitionValue>();
    props.add(new PropertyDefinitionValue("tt",
                                          true,
                                          true,
                                          1,
                                          false,
                                          def,
                                          false,
                                          PropertyType.STRING,
                                          new ArrayList<String>()));
    testNTValue.setDeclaredPropertyDefinitionValues(props);
    nodeTypeManager.registerNodeType(testNTValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);

    assertEquals("tt", testNode.getProperty("tt").getString());
    Node test2 = root.addNode("test2", testNTValue.getName());
    assertEquals("tt", test2.getProperty("tt").getString());
  }

  /**
   * Cover part of the PropertyDefinitionComparator.doChanged method.
   * 
   * @throws Exception
   */
  public void testReregisterMandatory() throws Exception {
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testReregisterMandatory");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);
    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();

    props.add(new PropertyDefinitionValue("tt",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.STRING,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node tNode = root.addNode("test", "exo:testReregisterMandatory");
    session.save();

    // chenge mandatory
    List<PropertyDefinitionValue> props2 = new ArrayList<PropertyDefinitionValue>();
    props2.add(new PropertyDefinitionValue("tt",
                                           false,
                                           true,
                                           1,
                                           false,
                                           new ArrayList<String>(),
                                           false,
                                           PropertyType.STRING,
                                           new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props2);
    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (RepositoryException e) {
      // ok;
    }
    tNode.setProperty("tt", "tt");
    session.save();

    Property property = tNode.getProperty("tt");
    assertEquals("tt", property.getString());

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
  }

  public void testReregisterRequiredNodeTypeChangeResidualProperty() throws Exception {
    // part1 any to string
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testReregisterRequiredNodeTypeChangeResidualProperty");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);
    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();

    props.add(new PropertyDefinitionValue("*",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.UNDEFINED,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node tNode = root.addNode("test", "exo:testReregisterRequiredNodeTypeChangeResidualProperty");
    tNode.setProperty("tt", "tt");
    tNode.setProperty("t2", 1);
    tNode.setProperty("t3", Calendar.getInstance());
    session.save();

    // chenge mandatory
    List<PropertyDefinitionValue> props2 = new ArrayList<PropertyDefinitionValue>();
    props2.add(new PropertyDefinitionValue("*",
                                           false,
                                           false,
                                           1,
                                           false,
                                           new ArrayList<String>(),
                                           false,
                                           PropertyType.STRING,
                                           new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props2);

    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (RepositoryException e) {
      // ok;
    }
    tNode.remove();
    session.save();

    tNode = root.addNode("test", "exo:testReregisterRequiredNodeTypeChangeResidualProperty");
    tNode.setProperty("tt", "tt");
    session.save();
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
    Property prop = tNode.setProperty("t2", 1);
    assertEquals(PropertyType.STRING, prop.getType());
  }

  public void testReregisterRequiredNodeTypeChangeProperty() throws Exception {
    // part1 any to string
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testReregisterRequiredNodeTypeChangeProperty");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);
    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();

    props.add(new PropertyDefinitionValue("tt",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.UNDEFINED,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node tNode = root.addNode("test", "exo:testReregisterRequiredNodeTypeChangeProperty");
    tNode.setProperty("tt", 1);

    session.save();

    // chenge mandatory
    List<PropertyDefinitionValue> props2 = new ArrayList<PropertyDefinitionValue>();
    props2.add(new PropertyDefinitionValue("tt",
                                           false,
                                           false,
                                           1,
                                           false,
                                           new ArrayList<String>(),
                                           false,
                                           PropertyType.STRING,
                                           new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props2);

    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (RepositoryException e) {
      // ok;
    }
    tNode.remove();
    session.save();

    tNode = root.addNode("test", "exo:testReregisterRequiredNodeTypeChangeProperty");
    tNode.setProperty("tt", "tt");
    session.save();
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
    Property prop = tNode.setProperty("tt", "22");
    assertEquals(PropertyType.STRING, prop.getType());
  }

  public void testReregisterValueConstraintChangeResidualProperty() throws Exception {

    // part1 any to string
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testReregisterValueConstraintChangeResidualProperty");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);
    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();

    props.add(new PropertyDefinitionValue("*",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.LONG,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node tNode = root.addNode("test", "exo:testReregisterValueConstraintChangeResidualProperty");
    tNode.setProperty("tt", 100);
    Property prop = tNode.setProperty("t1", 150);
    tNode.setProperty("t2", 1);
    tNode.setProperty("t3", 200);
    session.save();
    List<String> valueConstraint = new ArrayList<String>();
    valueConstraint.add("(,100]");
    valueConstraint.add("[200,)");
    props = new ArrayList<PropertyDefinitionValue>();
    props.add(new PropertyDefinitionValue("*",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.LONG,
                                          valueConstraint));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (ConstraintViolationException e) {
      // ok;
    }
    prop.remove();
    session.save();

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
  }

  public void testReregisterValueConstraintChangeProperty() throws Exception {

    // part1 any to string
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testReregisterValueConstraintChangeProperty");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);
    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();

    props.add(new PropertyDefinitionValue("t1",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.LONG,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node tNode = root.addNode("test", "exo:testReregisterValueConstraintChangeProperty");

    Property prop = tNode.setProperty("t1", 150);
    session.save();
    List<String> valueConstraint = new ArrayList<String>();
    valueConstraint.add("(,100]");
    valueConstraint.add("[200,)");
    props = new ArrayList<PropertyDefinitionValue>();
    props.add(new PropertyDefinitionValue("t1",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.LONG,
                                          valueConstraint));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (ConstraintViolationException e) {
      // ok;
    }

    tNode.setProperty("t1", 100);
    session.save();

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
  }

  public void testReregisterIsMultipleChangeResidualProperty() throws Exception {

    // part1 any to string
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testReregisterIsMultipleChangeResidualProperty");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);
    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();

    props.add(new PropertyDefinitionValue("*",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          true,
                                          PropertyType.STRING,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node tNode = root.addNode("test", "exo:testReregisterIsMultipleChangeResidualProperty");
    Property prop = tNode.setProperty("t1", new String[] { "100", "150" });

    session.save();
    props = new ArrayList<PropertyDefinitionValue>();
    props.add(new PropertyDefinitionValue("*",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.STRING,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (ConstraintViolationException e) {
      // ok;
    }
    prop.remove();
    session.save();

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);

    try {
      prop = tNode.setProperty("t1", new String[] { "100", "150" });
      session.save();
      fail();
    } catch (ValueFormatException e) {
      // ok
    }
  }

  public void testReregisterIsMultipleChangeProperty() throws Exception {

    // part1 any to string
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testReregisterIsMultipleChangeProperty");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);
    List<PropertyDefinitionValue> props = new ArrayList<PropertyDefinitionValue>();

    props.add(new PropertyDefinitionValue("t1",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          true,
                                          PropertyType.STRING,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node tNode = root.addNode("test", "exo:testReregisterIsMultipleChangeProperty");
    Property prop = tNode.setProperty("t1", new String[] { "100", "150" });

    session.save();
    props = new ArrayList<PropertyDefinitionValue>();
    props.add(new PropertyDefinitionValue("t1",
                                          false,
                                          false,
                                          1,
                                          false,
                                          new ArrayList<String>(),
                                          false,
                                          PropertyType.STRING,
                                          new ArrayList<String>()));
    testNValue.setDeclaredPropertyDefinitionValues(props);
    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (ConstraintViolationException e) {
      // ok;
    }
    prop.remove();
    session.save();

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);

    try {
      prop = tNode.setProperty("t1", new String[] { "100", "150" });
      session.save();
      fail();
    } catch (ValueFormatException e) {
      // ok
    }
  }

  /**
   * @throws Exception
   */
  public void testReregisterRemoveResidualChildNodeDefinition() throws Exception {
    // create new NodeType value
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testReregisterRemoveResidualChildNodeDefinition");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);

    List<NodeDefinitionValue> nodes = new ArrayList<NodeDefinitionValue>();
    nodes.add(new NodeDefinitionValue("*",
                                      false,
                                      false,
                                      1,
                                      false,
                                      "nt:base",
                                      new ArrayList<String>(),
                                      false));
    testNValue.setDeclaredChildNodeDefinitionValues(nodes);

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node testNode = root.addNode("testNode", "exo:testReregisterRemoveResidualChildNodeDefinition");
    Node child = testNode.addNode("child");
    session.save();

    nodes = new ArrayList<NodeDefinitionValue>();

    testNValue.setDeclaredChildNodeDefinitionValues(nodes);

    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (RepositoryException e) {
      // ok;
    }
    child.remove();
    session.save();

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);

    try {
      child = testNode.addNode("child");
      session.save();
    } catch (ConstraintViolationException e) {
      // e.printStackTrace();
    }
  }

  /**
   * @throws Exception
   */
  public void testReregisterRemoveChildNodeDefinition() throws Exception {
    // create new NodeType value
    NodeTypeValue testNValue = new NodeTypeValue();

    List<String> superType = new ArrayList<String>();
    superType.add("nt:base");
    testNValue.setName("exo:testReregisterRemoveChildNodeDefinition");
    testNValue.setPrimaryItemName("");
    testNValue.setDeclaredSupertypeNames(superType);

    List<NodeDefinitionValue> nodes = new ArrayList<NodeDefinitionValue>();
    nodes.add(new NodeDefinitionValue("child",
                                      false,
                                      false,
                                      1,
                                      false,
                                      "nt:base",
                                      new ArrayList<String>(),
                                      false));
    testNValue.setDeclaredChildNodeDefinitionValues(nodes);

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.FAIL_IF_EXISTS);

    Node testNode = root.addNode("testNode", testNValue.getName());
    Node child = testNode.addNode("child");
    session.save();

    nodes = new ArrayList<NodeDefinitionValue>();

    testNValue.setDeclaredChildNodeDefinitionValues(nodes);

    try {
      nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      fail();
    } catch (RepositoryException e) {
      // ok;
    }
    child.remove();
    session.save();

    nodeTypeManager.registerNodeType(testNValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);

    try {
      child = testNode.addNode("child");
      session.save();
      fail();
    } catch (ConstraintViolationException e) {
      // ok
      // e.printStackTrace();
    }
  }
}
