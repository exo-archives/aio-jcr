/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.importing;

import java.io.ByteArrayInputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.util.StringConverter;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestImportDocView extends JcrAPIBaseTest {
  private final String xmlSpeacialChars = "<html><body>a&lt;b>b&lt;/b>c</body></html>";
  private final  String xmlSameNameSablings4Xmltext = "<html><body>a<b>b</b>c</body></html>";

  public void testImportXmlStream() throws Exception {
    
    session.importXML(root.getPath(),
        new ByteArrayInputStream(xmlSpeacialChars.getBytes()),
        ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    session.save();
    Node htmlNode = root.getNode("html");
    Node bodyNode = htmlNode.getNode("body");
    Node xmlTextNode = bodyNode.getNode("jcr:xmltext");
    String xmlChars = xmlTextNode.getProperty("jcr:xmlcharacters").getString();
    assertTrue(StringConverter.denormalizeString(xmlSpeacialChars).contains(xmlChars));
  }
  public void testImportXmlCh() throws Exception {
    

    XMLReader reader = XMLReaderFactory.createXMLReader();

    reader.setContentHandler(session.getImportContentHandler(root.getPath(), 0));
    InputSource inputSource = new InputSource(new ByteArrayInputStream(xmlSpeacialChars.getBytes()));

    reader.parse(inputSource);
    
    session.save();
    Node htmlNode = root.getNode("html");
    Node bodyNode = htmlNode.getNode("body");
    Node xmlTextNode = bodyNode.getNode("jcr:xmltext");
    String xmlChars = xmlTextNode.getProperty("jcr:xmlcharacters").getString();
    assertTrue(StringConverter.denormalizeString(xmlSpeacialChars).contains(xmlChars));
  }  
 public void testImportXmlStreamSameNameSablings() throws Exception {
    
    session.importXML(root.getPath(),
        new ByteArrayInputStream(xmlSameNameSablings4Xmltext.getBytes()),
        ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    session.save();
    Node htmlNode = root.getNode("html");
    Node bodyNode = htmlNode.getNode("body");
    NodeIterator xmlTextNodes = bodyNode.getNodes("jcr:xmltext");
    assertEquals(2,xmlTextNodes.getSize());
    Node nodeA = xmlTextNodes.nextNode();
    Node nodeC = xmlTextNodes.nextNode();
    assertEquals("a",nodeA.getProperty("jcr:xmlcharacters").getString());
    assertEquals("c",nodeC.getProperty("jcr:xmlcharacters").getString());
  }
  public void testImportXmlChSameNameSablings() throws Exception {
    

    XMLReader reader = XMLReaderFactory.createXMLReader();

    reader.setContentHandler(session.getImportContentHandler(root.getPath(), 0));
    InputSource inputSource = new InputSource(new ByteArrayInputStream(xmlSameNameSablings4Xmltext.getBytes()));

    reader.parse(inputSource);
    
    session.save();
    Node htmlNode = root.getNode("html");
    Node bodyNode = htmlNode.getNode("body");
    NodeIterator xmlTextNodes = bodyNode.getNodes("jcr:xmltext");
    assertEquals(2,xmlTextNodes.getSize());
    Node nodeA = xmlTextNodes.nextNode();
    Node nodeC = xmlTextNodes.nextNode();
//    assertEquals("a",nodeA.getProperty("jcr:xmlcharacters").getString());
//    assertEquals("c",nodeC.getProperty("jcr:xmlcharacters").getString());
//    
//    File tmpFile1 = File.createTempFile("testDocSNS","xml");
//    File tmpFile2 = File.createTempFile("testSysSNS","xml");
//
//    session.exportDocumentView(htmlNode.getPath(),new FileOutputStream(tmpFile1),false,false);
//    session.exportSystemView(htmlNode.getPath(),new FileOutputStream(tmpFile2),false,false);
//    
    
  }  
}

