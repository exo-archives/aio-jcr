/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.importing;

import java.io.ByteArrayInputStream;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.util.StringConverter;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestDocView extends JcrAPIBaseTest {
  String simpleXml = "<html><body>a&lt;b>b&lt;/b>c</body></html>";

  public void testImportXmlStream() throws Exception {
    
    session.importXML(root.getPath(),
        new ByteArrayInputStream(simpleXml.getBytes()),
        ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    session.save();
    Node htmlNode = root.getNode("html");
    Node bodyNode = htmlNode.getNode("body");
    Node xmlTextNode = bodyNode.getNode("jcr:xmltext");
    String xmlChars = xmlTextNode.getProperty("jcr:xmlcharacters").getString();
    assertTrue(StringConverter.denormalizeString(simpleXml).contains(xmlChars));
  }
  public void testImportXmlCh() throws Exception {
    

    XMLReader reader = XMLReaderFactory.createXMLReader();

    reader.setContentHandler(session.getImportContentHandler(root.getPath(), 0));
    InputSource inputSource = new InputSource(new ByteArrayInputStream(simpleXml.getBytes()));

    reader.parse(inputSource);
    
    session.save();
    Node htmlNode = root.getNode("html");
    Node bodyNode = htmlNode.getNode("body");
    Node xmlTextNode = bodyNode.getNode("jcr:xmltext");
    String xmlChars = xmlTextNode.getProperty("jcr:xmlcharacters").getString();
    assertTrue(StringConverter.denormalizeString(simpleXml).contains(xmlChars));
  }  
  
}
