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
package org.exoplatform.services.jcr.api.importing;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.util.StringConverter;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestImportDocView extends BaseImportTest {
  private final String xmlSpeacialChars            = "<html><body>a&lt;b>b&lt;/b>c</body></html>";

  private final String xmlSameNameSablings4Xmltext = "<html><body>a<b>b</b>c</body></html>";

  private final String NAV_XML                     = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                                       + "<node-navigation>"
                                                       + "<owner-type>portal</owner-type>"
                                                       + "<owner-id>portalone</owner-id>"
                                                       + "<access-permissions>*:/guest</access-permissions>"
                                                       + "<page-nodes>"
                                                       + "<node>"
                                                       + "<uri>portalone::home</uri>"
                                                       + "<name>home</name>"
                                                       + "<label>Home</label>"
                                                       + "<page-reference>portal::portalone::content</page-reference>"
                                                       + "</node>"
                                                       + "<node>"
                                                       + "<uri>portalone::register</uri>"
                                                       + "<name>register</name>"
                                                       + "<label>Register</label>"
                                                       + "<page-reference>portal::portalone::register</page-reference>"
                                                       + "</node>" + "</page-nodes>"
                                                       + "</node-navigation>";

  private final String NAV_XML2                    = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                                       + "<node-navigation  xmlns:jcr='http://www.jcp.org/jcr/1.0' jcr:primaryType='nt:unstructured' >"
                                                       + "<owner-type>portal</owner-type>"
                                                       + "<owner-id>portalone</owner-id>"
                                                       + "<access-permissions>*:/guest</access-permissions>"
                                                       + "<page-nodes>"
                                                       + "<node>"
                                                       + "<uri>portalone::home</uri>"
                                                       + "<name>home</name>"
                                                       + "<label>Home</label>"
                                                       + "<page-reference>portal::portalone::content</page-reference>"
                                                       + "</node>"
                                                       + "<node>"
                                                       + "<uri>portalone::register</uri>"
                                                       + "<name>register</name>"
                                                       + "<label>Register</label>"
                                                       + "<page-reference>portal::portalone::register</page-reference>"
                                                       + "</node>" + "</page-nodes>"
                                                       + "</node-navigation>";

  @Override
  public void initRepository() throws RepositoryException {
    super.initRepository();
    InputStream is = TestImportDocView.class.getResourceAsStream("/nodetypes/ext-registry-nodetypes.xml");
    session.getWorkspace().getNodeTypeManager().registerNodeTypes(is, 0);
  }

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
    assertEquals(2, xmlTextNodes.getSize());
    Node nodeA = xmlTextNodes.nextNode();
    Node nodeC = xmlTextNodes.nextNode();
    assertEquals("a", nodeA.getProperty("jcr:xmlcharacters").getString());
    assertEquals("c", nodeC.getProperty("jcr:xmlcharacters").getString());
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
    assertEquals(2, xmlTextNodes.getSize());
    Node nodeA = xmlTextNodes.nextNode();
    Node nodeC = xmlTextNodes.nextNode();
    // assertEquals("a",nodeA.getProperty("jcr:xmlcharacters").getString());
    // assertEquals("c",nodeC.getProperty("jcr:xmlcharacters").getString());
    //    
    // File tmpFile1 = File.createTempFile("testDocSNS","xml");
    // File tmpFile2 = File.createTempFile("testSysSNS","xml");
    //
    // session.exportDocumentView(htmlNode.getPath(),new
    // FileOutputStream(tmpFile1),false,false);
    // session.exportSystemView(htmlNode.getPath(),new
    // FileOutputStream(tmpFile2),false,false);
    //    

  }

  public void testImportRawXml() throws Exception {

    Node testRoot = root.addNode("testRoot", "exo:registryGroup");
    session.save();

    deserialize(testRoot,
                XmlSaveType.SESSION,
                true,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
                new ByteArrayInputStream(NAV_XML.getBytes()));

    Node node_navigation = testRoot.getNode("node-navigation");

    assertTrue(node_navigation.isNodeType("exo:registryEntry"));
    Node owner_type = node_navigation.getNode("owner-type");
    assertTrue(owner_type.isNodeType("nt:unstructured"));
  }

  public void testImportRawXmlFail() throws Exception {

    Node testRoot = root.addNode("testRoot", "exo:registryGroup");
    session.save();

    try {
      deserialize(testRoot,
                  XmlSaveType.SESSION,
                  true,
                  ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
                  new ByteArrayInputStream(NAV_XML2.getBytes()));
      fail();
    } catch (ConstraintViolationException e) {
      // ok
    }

  }
}
