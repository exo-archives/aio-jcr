/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.api.writing;

import java.io.ByteArrayInputStream;
import java.util.GregorianCalendar;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.version.OnParentVersionAction;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.value.NameValue;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestSetProperty.java 14508 2008-05-20 10:07:45Z ksm $
 */
public class TestSetProperty extends JcrAPIBaseTest {

  static protected String TEST_MULTIVALUED = "testMultivalued";

  protected Node          testMultivalued  = null;

  public void initRepository() throws RepositoryException {
    Node root = session.getRootNode();
    Node propDef = root.addNode("propertyDefNode", "nt:propertyDefinition");
    propDef.setProperty("jcr:name", valueFactory.createValue("test", PropertyType.NAME));

    propDef.setProperty("jcr:autoCreated", false);
    propDef.setProperty("jcr:mandatory", false);
    propDef.setProperty("jcr:onParentVersion", OnParentVersionAction.ACTIONNAME_COPY);
    propDef.setProperty("jcr:protected", false);
    propDef.setProperty("jcr:requiredType", PropertyType.TYPENAME_STRING.toUpperCase());
    propDef.setProperty("jcr:multiple", false);
    // Unknown Property Type. Should set something!
    Value[] defVals = { session.getValueFactory().createValue("testString") };
    propDef.setProperty("jcr:defaultValues", defVals);

    Node childNodeDefNode = root.addNode("childNodeDefNode", "nt:childNodeDefinition");
    childNodeDefNode.setProperty("jcr:name", valueFactory.createValue("test"), PropertyType.NAME);
    childNodeDefNode.setProperty("jcr:autoCreated", false);
    childNodeDefNode.setProperty("jcr:mandatory", false);
    childNodeDefNode.setProperty("jcr:onParentVersion", OnParentVersionAction.ACTIONNAME_COPY);
    childNodeDefNode.setProperty("jcr:protected", false);
    childNodeDefNode.setProperty("jcr:requiredPrimaryTypes",
                                 new NameValue[] { (NameValue) valueFactory.createValue("nt:base",
                                                                                        PropertyType.NAME) });
    childNodeDefNode.setProperty("jcr:sameNameSiblings", false);

    root.addNode("unstructured", "nt:unstructured");

    testMultivalued = root.addNode(TEST_MULTIVALUED);

    session.save();
  }

  public void tearDown() throws Exception {

    try {
      // testMultivalued.getSession().refresh(false);
      testMultivalued.remove();
      testMultivalued.getSession().save();
    } catch (RepositoryException e) {
      log.error("Error delete '" + TEST_MULTIVALUED + "' node", e);
    }

    session = (SessionImpl) repository.login(credentials, WORKSPACE);
    Node root = session.getRootNode();
    root.getNode("unstructured").remove();

    // session.getItem("/propertyDefNode").remove();
    root.getNode("propertyDefNode").remove();
    root.getNode("childNodeDefNode").remove();
    session.save();

    super.tearDown();
  }

  public void testSetPropertyNameValue() throws RepositoryException {
    Node root = session.getRootNode();
    Node node = root.getNode("propertyDefNode");
    // Node node = (Node)session.getItem("/propertyDefNode");

    try {
      node.setProperty("jcr:multiple", valueFactory.createValue(20l));
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }
    session.refresh(false);
  }

  public void testSetPropertyNameValueType() throws RepositoryException {
    Node root = session.getRootNode();
    Node node = root.getNode("propertyDefNode");

    session.refresh(false);
    node.setProperty("jcr:defaultValues", new Value[] { valueFactory.createValue(10l) }); // ,
    // PropertyType
    // .LONG
    assertEquals(PropertyType.LONG, node.getProperty("jcr:defaultValues").getValues()[0].getType());
    assertEquals(10, node.getProperty("jcr:defaultValues").getValues()[0].getLong());
    node.save();
    session = (SessionImpl) repository.login(credentials, WORKSPACE);
    node = session.getRootNode().getNode("propertyDefNode");
    assertEquals(10, node.getProperty("jcr:defaultValues").getValues()[0].getLong());
  }

  public void testSetPropertyNameValuesType() throws RepositoryException {
    Node root = session.getRootNode();
    Node node = root.getNode("childNodeDefNode");
    Value[] values = { session.getValueFactory().createValue("not"),
        session.getValueFactory().createValue("in") };

    // it converts to required !
    // node.setProperty("jcr:requiredPrimaryTypes", values, PropertyType.LONG);
    node.setProperty("jcr:requiredPrimaryTypes", values, PropertyType.NAME);

    try {
      Property prop = node.setProperty("jcr:onParentVersion", values, PropertyType.STRING);
      fail("exception should have been thrown " + prop.getString());
    } catch (ValueFormatException e) {
    }

    Value[] nameValues = { valueFactory.createValue("jcr:unstructured", PropertyType.NAME),
        valueFactory.createValue("jcr:base", PropertyType.NAME) };
    node.setProperty("jcr:requiredPrimaryTypes", nameValues, PropertyType.NAME);
    node.save();

    session = (SessionImpl) repository.login(credentials, WORKSPACE);
    node = session.getRootNode().getNode("childNodeDefNode");
    assertEquals(2, node.getProperty("jcr:requiredPrimaryTypes").getValues().length);
  }

  public void testSetPropertyNameStringValueType() throws RepositoryException {
    Node root = session.getRootNode();
    Node node = root.getNode("propertyDefNode");

    session.refresh(false);

    node.setProperty("jcr:defaultValues", new Value[] { valueFactory.createValue((long) 10) });
    assertEquals(PropertyType.LONG, node.getProperty("jcr:defaultValues").getValues()[0].getType());
    assertEquals(10, node.getProperty("jcr:defaultValues").getValues()[0].getLong());
    node.save();
    session = (SessionImpl) repository.login(credentials, WORKSPACE);
    node = session.getRootNode().getNode("propertyDefNode");
    assertEquals(10, node.getProperty("jcr:defaultValues").getValues()[0].getLong());
  }

  public void testSetPropertyNameStringValuesType() throws RepositoryException {
    Node root = session.getRootNode();
    Node node = root.getNode("childNodeDefNode");
    String[] values = { "not", "in" };
    try {
      // it converts to required !
      node.setProperty("jcr:requiredPrimaryTypes", values, PropertyType.LONG);
    } catch (ValueFormatException e) {
    }
    try {
      node.setProperty("jcr:onParentVersion", values, PropertyType.STRING);
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }

    Value[] nameValues = { valueFactory.createValue("jcr:unstructured", PropertyType.NAME),
        valueFactory.createValue("jcr:base", PropertyType.NAME) };
    node.setProperty("jcr:requiredPrimaryTypes", nameValues, PropertyType.NAME);
    node.save();

    session = (SessionImpl) repository.login(credentials, WORKSPACE);
    node = session.getRootNode().getNode("childNodeDefNode");
    assertEquals(2, node.getProperty("jcr:requiredPrimaryTypes").getValues().length);
  }

  public void testSetPropertyMultivaluedString() throws RepositoryException {
    String[] values = { "binary string 1", "binary string 2" };
    Property mvp1 = null;
    try {
      mvp1 = testMultivalued.setProperty("Multivalued Property", values, PropertyType.BINARY);
      testMultivalued.save();
    } catch (ValueFormatException e) {
      fail("Can't add 'Multivalued Property'. Error: " + e.getMessage());
    }
    try {
      assertTrue("'Multivalued Property' must have size 2", mvp1.getLengths().length == 2);
    } catch (RepositoryException e) {
      fail("Error of 'Multivalued Property' length reading. Error: " + e.getMessage());
    }

    SessionImpl newSession = (SessionImpl) repository.login(credentials, WORKSPACE);
    Node test = (Node) newSession.getItem(testMultivalued.getPath());
    assertEquals("Node '" + TEST_MULTIVALUED + "' must have values length 2",
                 2,
                 test.getProperty("Multivalued Property").getValues().length);
    test = newSession.getRootNode().getNode(TEST_MULTIVALUED);
    assertEquals("Node '" + TEST_MULTIVALUED + "' must have values length 2",
                 2,
                 test.getProperty("Multivalued Property").getValues().length);
  }

  public void testSetPropertyMultivaluedBinary() throws RepositoryException {
    Value[] values = {
        valueFactory.createValue(new ByteArrayInputStream("binary string 1".getBytes())),
        valueFactory.createValue(new ByteArrayInputStream("binary string 2".getBytes())) };
    Property mvp1 = null;
    try {
      mvp1 = testMultivalued.setProperty("Multivalued Property", values, PropertyType.BINARY);
      testMultivalued.save();
    } catch (ValueFormatException e) {
      fail("Can't add 'Multivalued Property'. Error: " + e.getMessage());
    }
    try {
      assertTrue("'Multivalued Property' must have size 2", mvp1.getValues().length == 2);
    } catch (RepositoryException e) {
      fail("Error of 'Multivalued Property' length reading. Error: " + e.getMessage());
    }

    SessionImpl newSession = (SessionImpl) repository.login(credentials, WORKSPACE);
    Node test = (Node) newSession.getItem(testMultivalued.getPath());
    assertEquals("Node '" + TEST_MULTIVALUED + "' must have values length 2",
                 2,
                 test.getProperty("Multivalued Property").getValues().length);
    test = newSession.getRootNode().getNode(TEST_MULTIVALUED);
    assertEquals("Node '" + TEST_MULTIVALUED + "' must have values length 2",
                 2,
                 test.getProperty("Multivalued Property").getValues().length);
  }

  public void testSetPropertyNameTypedValue() throws RepositoryException {
    Node root = session.getRootNode();
    Node node = root.getNode("propertyDefNode");

    node.setProperty("jcr:defaultValues", new Value[] { valueFactory.createValue("default") });
    node.setProperty("jcr:defaultValues",
                     new Value[] { valueFactory.createValue(new ByteArrayInputStream(new String("default").getBytes())) });
    node.setProperty("jcr:defaultValues", new Value[] { valueFactory.createValue(true) });
    node.setProperty("jcr:defaultValues",
                     new Value[] { valueFactory.createValue(new GregorianCalendar()) });
    node.setProperty("jcr:defaultValues", new Value[] { valueFactory.createValue(20D) });
    node.setProperty("jcr:defaultValues", new Value[] { valueFactory.createValue(20L) });

    try {
      node.setProperty("jcr:multiple", 20D);
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }
  }

  public void testSetPathProperty() throws RepositoryException {
    Node root = session.getRootNode();
    Node node1 = root.addNode("node1", "nt:unstructured");
    node1.setProperty("pathValue", valueFactory.createValue("/root-node/node_1", PropertyType.PATH));
    assertNotNull(session.getItem("/node1/pathValue"));
    assertEquals("/root-node/node_1", ((Property) session.getItem("/node1/pathValue")).getString());
    root.save();
    assertNotNull(session.getItem("/node1/pathValue"));
    assertEquals("/root-node/node_1", ((Property) session.getItem("/node1/pathValue")).getString());
    node1.remove();
    root.save();
    // node1.save();//impossible
  }

  public void testInvalidItemStateException() throws RepositoryException {
    Property p = session.getRootNode().setProperty("sameProperty", "test");

    Session session2 = repository.login(credentials, "ws");
    Property p2 = session2.getRootNode().setProperty("sameProperty", "test");
    session.save();

    try {
      session2.save();
      fail("InvalidItemStateException should have been thrown");
    } catch (ItemExistsException e) {
    }

  }

}
