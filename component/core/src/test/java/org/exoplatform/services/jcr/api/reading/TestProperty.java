/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.reading;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;


/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestProperty.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestProperty extends JcrAPIBaseTest{

  private Node node;

  public void initRepository() throws RepositoryException {
    Node root = session.getRootNode();
    node = root.addNode("childNode", "nt:unstructured");

    Value[] values = new Value[3];
    values[0] = valueFactory.createValue("stringValue");
    values[1] = valueFactory.createValue("true");
    values[2] = valueFactory.createValue("121");
    node.setProperty("multi", values, PropertyType.STRING);
    node.setProperty("multi-boolean", new Value[]{ 
        session.getValueFactory().createValue(true),
        session.getValueFactory().createValue(true)});

    node.setProperty("single", session.getValueFactory().createValue("this is the content"));

    ByteArrayInputStream is = new ByteArrayInputStream("streamValue".getBytes());
    node.setProperty("stream", valueFactory.createValue(is));

  }

  public void tearDown() throws Exception {
    node.remove();
    
    super.tearDown();
  }

  public void testGetValue() throws RepositoryException {

    Property property = node.getProperty("single");
    assertTrue(property.getValue() instanceof Value);

    try {
      node.getProperty("multi").getValue();
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }

    try {
      node.getProperty("multi-boolean").getBoolean();
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }

  }

  public void testGetValues() throws RepositoryException {
    Value[] values = node.getProperty("multi").getValues();
    for (int i = 0; i < values.length; i++) {
      Value value = values[i];
      if(!("stringValue".equals(value.getString()) || "true".equals(value.getString()) ||
          "121".equals(value.getString()) )){
        fail("returned non expected value");
      }
    }
    try {
      node.getProperty("single").getValues();
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }
  }

  public void testGetString() throws RepositoryException {
    node.setProperty("string", session.getValueFactory().createValue("stringValue"));

    String stringValue = node.getProperty("string").getString();
    assertEquals("stringValue", stringValue);

    try {
      node.getProperty("multi").getString();
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }
  }

  public void testGetBinaryAsStream() throws RepositoryException, IOException {

    node.setProperty("stream", new BinaryValue("inputStream"));
    Value value = node.getProperty("stream").getValue();
    InputStream iS = value.getStream();
    byte[] bytes = new byte[iS.available()];
    iS.read(bytes);
    assertEquals("inputStream", new String(bytes));
    try {
      value.getString();
      fail("exception should have been thrown");
    } catch (IllegalStateException e) {
    }
    iS.reset();
    iS = node.getProperty("stream").getValue().getStream();
    bytes = new byte[iS.available()];
    iS.read(bytes);
    assertEquals("inputStream", new String(bytes));

  }


  public void testGetLong() throws RepositoryException {
    node.setProperty("long", valueFactory.createValue(15l));
    assertEquals(15, node.getProperty("long").getLong());
    assertEquals(15, node.getProperty("long").getValue().getLong());
  }

  public void testGetDouble() throws RepositoryException {
    node.setProperty("double", session.getValueFactory().createValue(15));
    assertEquals(15, (int)node.getProperty("double").getDouble());
    assertEquals(15, (int)node.getProperty("double").getValue().getDouble());
    try {
      node.getProperty("multi").getDouble();
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }

  }

  public void testGetDate() throws RepositoryException {

    Calendar calendar = new GregorianCalendar();
    node.setProperty("date", session.getValueFactory().createValue(calendar));
    assertEquals(calendar.getTimeInMillis(), node.getProperty("date").getDate().getTimeInMillis());
    assertEquals(calendar.getTimeInMillis(), node.getProperty("date").getValue().getDate().getTimeInMillis());
  }

  public void testGetBoolean() throws RepositoryException {
    node.setProperty("boolean", session.getValueFactory().createValue(true));
    assertEquals(true, node.getProperty("boolean").getBoolean());
    assertEquals(true, node.getProperty("boolean").getValue().getBoolean());
  }



  public void testGetLength() throws RepositoryException, IOException {
    Property property = node.getProperty("single");
    assertTrue(property.getLength()>0);
    property = node.getProperty("stream");
    //node.setProperty("stream", new BinaryValue(new ByteArrayInputStream("inputStream".getBytes())));
    Value b = valueFactory.createValue(new ByteArrayInputStream("inputStream".getBytes()));
    property.setValue(b);
    
    assertTrue(property.getLength()>0);

    try {
      node.getProperty("multi").getLength();
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }
  }

  public void testGetLengths() throws RepositoryException, IOException {
    Property property = node.getProperty("multi");
    assertTrue(property.getLengths()[0]>0);

    try {
      node.getProperty("single").getLengths();
      fail("exception should have been thrown");
    } catch (ValueFormatException e) {
    }
  }

  public void testGetDefinition() throws RepositoryException {
    Property property = node.getProperty("single");
    assertEquals("*", property.getDefinition().getName());
  }

  public void testGetType() throws RepositoryException {
    assertEquals(PropertyType.STRING, node.getProperty("single").getType());
    assertEquals(PropertyType.STRING, node.getProperty("multi").getType());
  }

  public void testGetBinaryAsString() throws RepositoryException, IOException {

    //System.out.println("STREAM>>>>>>");
  	
    node.setProperty("stream", new BinaryValue("inputStream")); 
    //System.out.println("STREAM>>>>>>");

        
    //log.debug("STREAM>>>>>>");
    Value value = node.getProperty("stream").getValue();
    assertEquals("inputStream", value.getString());
    try {
      value.getStream();
      fail("exception should have been thrown");
    } catch (IllegalStateException e) {
    }

  }
 
  public void testGetNode() throws RepositoryException {
    Node root = session.getRootNode();
    Node node1 = root.addNode("childNode1", "nt:unstructured");

  	Node refNode = node1.addNode("refNode", "nt:resource");
    refNode.setProperty("jcr:data", session.getValueFactory().createValue("this is the content", PropertyType.BINARY));
    refNode.setProperty("jcr:mimeType", session.getValueFactory().createValue("text/html"));
    refNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));

    Value refVal = valueFactory.createValue(refNode);
    Property p = node1.setProperty("reference", refVal);
    //log.debug("RefVal >>>"+p.getString());
    
    root.save();
    
    assertEquals(refNode.getUUID(), node1.getProperty("reference").getString());
    assertEquals(refNode.getPath(), node1.getProperty("reference").getNode().getPath());
    
    refNode.remove();
    node1.remove();
    
  }

  
}
