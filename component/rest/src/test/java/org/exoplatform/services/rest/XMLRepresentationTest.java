/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

//import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.rest.data.XMLRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: CommandServiceTest.java 9296 2006-10-04 13:13:29Z geaz $
 */
public class XMLRepresentationTest extends TestCase {
  
//  private StandaloneContainer container;
  
//  public void setUp() throws Exception {
//    StandaloneContainer.setConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
//    container = StandaloneContainer.getInstance();
//  }
  
  public void testCreateXMLRepresentation() throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//    dbf.setNamespaceAware(true);
//    dbf.setValidating(true);
    
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    
    Document doc = db.newDocument();
    Element e1 = doc.createElementNS("http://test", "tt:test");
    doc.appendChild(e1).
    appendChild(doc.createElementNS("http://test", "tt:test1")).
    appendChild(doc.createElement("test1")).
    setTextContent("content");
    
    XMLRepresentation repr = new XMLRepresentation(doc);
    System.out.println(">>>>>> "+repr.getString());
    //Request
  }

}
