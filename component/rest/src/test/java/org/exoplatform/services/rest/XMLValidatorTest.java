/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import junit.framework.TestCase;

import org.exoplatform.services.rest.data.XMLRepresentation;
import org.exoplatform.services.rest.data.XMLValidator;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.container.StandaloneContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class XMLValidatorTest extends TestCase {

  private StandaloneContainer container;
  private XMLValidator validator;
  
  public void setUp() throws Exception {
    StandaloneContainer.setConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
    container = StandaloneContainer.getInstance();
    validator = (XMLValidator)container.getComponentInstanceOfType(XMLValidator.class);
  }
  
  public void testStreamXMLValidator() throws Exception {
  	InputStream in = new FileInputStream(new File("src/test/java/conf/standalone/test.xml"));
  	assertEquals("schema1", validator.validate(in));
  }
  
  public void testCreateXMLRepresentation() throws Exception {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document xmldoc = dbuilder.newDocument();
    Element elem = xmldoc.createElement("test");
    xmldoc.appendChild(elem);
    elem.appendChild(xmldoc.createElement("testChild1"));
    elem.appendChild(xmldoc.createElement("testChild2"));
    elem.appendChild(xmldoc.createElement("testChild3"));
    XMLRepresentation xmlRepr = new XMLRepresentation(xmldoc);
    System.out.println(">>>>> " + xmlRepr.getString());
    assertEquals("schema1", validator.validate(xmldoc));
  }  
  
}
