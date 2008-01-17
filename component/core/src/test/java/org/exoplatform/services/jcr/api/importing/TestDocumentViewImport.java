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
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.util.StringConverter;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.jcr.impl.xml.importing.ContentImporter;
import org.exoplatform.services.log.ExoLogger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestDocumentViewImport extends BaseImportTest {
  private static Log   log                         = ExoLogger.getLogger("jcr.TestDocumentViewImport");

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

  private final String docView                     = "<exo:test xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" "
                                                       + "xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" "
                                                       + "xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" "
                                                       + "xmlns:exo=\"http://www.exoplatform.com/jcr/exo/1.0\" "
                                                       + "xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" "
                                                       + "jcr:primaryType=\"nt:unstructured\">"
                                                       + "<childNode jcr:created=\"2004-08-18T20:07:42.626+01:00\" jcr:primaryType=\"nt:folder\">"
                                                       + "<childNode3 jcr:created=\"2004-08-18T20:07:42.636+01:00\" jcr:primaryType=\"nt:file\">"
                                                       + "<jcr:content jcr:data=\"dGhpcyBpcyB0aGUgYmluYXJ5IGNvbnRlbnQ=\" jcr:primaryType=\"nt:resource\" jcr:lastModified=\"2004-08-18T20:07:42.626+01:00\" jcr:mimeType=\"text/html\" jcr:uuid=\"1092852462407_\">"
                                                       + "</jcr:content>"
                                                       + "</childNode3>"
                                                       + "<childNode2 jcr:created=\"2004-08-18T20:07:42.636+01:00\" jcr:primaryType=\"nt:file\">"
                                                       + "<jcr:content jcr:data=\"VGhyZWUgYnl0ZXMgYXJlIGNvbmNhdGVuYXRlZCwgdGhlbiBzcGxpdCB0byBmb3JtIDQgZ3JvdXBz"
                                                       + "IG9mIDYtYml0cyBlYWNoOw==\" jcr:primaryType=\"nt:resource\" jcr:mimeType=\"text/html\" jcr:lastModified=\"2004-08-18T20:07:42.626+01:00\" jcr:uuid=\"1092852462406_\">"
                                                       + "</jcr:content>"
                                                       + "</childNode2>"
                                                       + "</childNode>"
                                                       + "<testNodeWithText1 jcr:mixinTypes='mix:referenceable' jcr:uuid='id_uuidNode3' testProperty='test property value'>Thisi is a text content of node &lt;testNodeWithText1/&gt; </testNodeWithText1>"
                                                       + "<testNodeWithText2><![CDATA[This is a text content of node <testNodeWithText2>]]></testNodeWithText2>"
                                                       + "<uuidNode1 jcr:mixinTypes='mix:referenceable' jcr:uuid='id_uuidNode1' source='docView'/>"
                                                       + "</exo:test>";

  private final String docViewECM                  = "<test-article xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:kfx=\"http://www.exoplatform.com/jcr/kfx/1.1/\" xmlns:Fwd=\"http://www.exoplatform.com/jcr/Fwd/1.1/\" xmlns:Re=\"http://www.exoplatform.com/jcr/Re/1.1/\" xmlns:rma=\"http://www.rma.com/jcr/\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" xmlns:fn=\"http://www.w3.org/2004/10/xpath-functions\" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:exo=\"http://www.exoplatform.com/jcr/exo/1.0\" exo:summary=\"\" exo:voteTotal=\"0\" exo:votingRate=\"0.0\" jcr:primaryType=\"exo:article\" jcr:mixinTypes=\"mix:votable mix:i18n\" jcr:uuid=\"6da3fcebc0a800070043d28761e00078\" exo:language=\"en\" exo:title=\"title\" exo:text=\"\"></test-article>";

  private final String docView2                    = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                                       + "<childNode2 "
                                                       + "xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" "
                                                       + "xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" "
                                                       + "jcr:primaryType=\"nt:file\" "
                                                       + "jcr:created=\"2004-08-18T17:17:00.856+03:00\">"
                                                       + "<jcr:content "
                                                       + "jcr:primaryType=\"nt:resource\" "
                                                       + "jcr:uuid=\"6a3859dac0a8004b006e6e0bf444ebaa\" "
                                                       + "jcr:data=\"dGhpcyBpcyB0aGUgYmluYXJ5IGNvbnRlbnQ=\" "
                                                       + "jcr:lastModified=\"2004-08-18T17:17:00.856+03:00\" "
                                                       + "jcr:lastModified2=\"2004-08-18T17:17:00.856+03:00\" "
                                                       + "jcr:mimeType=\"text/text\"/>"
                                                       + "</childNode2>";

  // @Override
  // public void initRepository() throws RepositoryException {
  // super.initRepository();
  // InputStream is =
  // TestDocumentViewImport.class.getResourceAsStream("/nodetypes/ext-registry-nodetypes.xml");
  // session.getWorkspace().getNodeTypeManager().registerNodeTypes(is, 0);
  //    
  // session.getRootNode().addNode("test", "nt:unstructured");
  // session.getRootNode().addNode("test2", "nt:unstructured");
  // session.getRootNode().addNode("testECM", "nt:unstructured");
  // NodeTypeManagerImpl ntManager =
  // session.getWorkspace().getNodeTypeManager();
  //
  // ntManager.registerNodeTypes(TestDocumentViewImport.class.getResourceAsStream("/org/exoplatform/services/jcr/api/nodetypes/ecm/nodetypes-config.xml"),
  // 0);
  // ntManager.registerNodeTypes(TestDocumentViewImport.class.getResourceAsStream("/org/exoplatform/services/jcr/api/nodetypes/ecm/nodetypes-config-extended.xml"),
  // 0);
  //    
  // }

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

  /**
   * (boolean isSystemView, boolean isExportedByStream, boolean
   * isImportedByStream, XmlSaveType saveType, int testedBehavior)
   */
  public void testUuidCollision_IContentHandler_EContentHandler_Session_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Session_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Session_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Session_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Workspace_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Workspace_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Workspace_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EContentHandler_Workspace_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EStream_Session_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IContentHandler_EStream_Session_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IContentHandler_EStream_Session_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EStream_Session_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EStream_Workspace_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IContentHandler_EStream_Workspace_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IContentHandler_EStream_Workspace_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            false,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IContentHandler_EStream_Workspace_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IStream_EContentHandler_Session_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IStream_EContentHandler_Session_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IStream_EContentHandler_Session_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IStream_EContentHandler_Session_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IStream_EContentHandler_Workspace_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IStream_EContentHandler_Workspace_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IStream_EContentHandler_Workspace_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IStream_EContentHandler_Workspace_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            false,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IStream_EStream_Session_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IStream_EStream_Session_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IStream_EStream_Session_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IStream_EStream_Session_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.SESSION,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testUuidCollision_IStream_EStream_Workspace_COLLISION_THROW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  public void testUuidCollision_IStream_EStream_Workspace_CREATE_NEW() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
  }

  public void testUuidCollision_IStream_EStream_Workspace_REMOVE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
  }

  public void testUuidCollision_IStream_EStream_Workspace_REPLACE_EXISTING() throws Exception {
    importUuidCollisionTest(false,
                            true,
                            true,
                            XmlSaveType.WORKSPACE,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
  }

  public void testDocImportUnExistingPropertyDefinition() throws Exception {
    InvocationContext context = new InvocationContext();
    try {
      
      context.put(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS, true);
      ((ExtendedSession) session).importXML(root.getPath(),
                                            new ByteArrayInputStream(docView2.getBytes()),
                                            0,
                                            context);
      session.save();
      fail();
    } catch (RepositoryException e) {
      // ok
    }
    try {
      context.put(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS, false);
      ((ExtendedSession) session).importXML(root.getPath(),
                                            new ByteArrayInputStream(docView2.getBytes()),
                                            0,
                                            context);
      session.save();
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testDocViewImportContentHandler() throws Exception {

    XMLReader reader = XMLReaderFactory.createXMLReader();
    root.addNode("test");
    reader.setContentHandler(session.getImportContentHandler("/test", 0));
    InputSource inputSource = new InputSource(new ByteArrayInputStream(docView.getBytes()));

    reader.parse(inputSource);
    // fail ("STOP");
    session.save();

    Node root = session.getRootNode();
    NodeIterator iterator = root.getNode("test/exo:test").getNodes();
    assertEquals(4, iterator.getSize());

    iterator = root.getNode("test/exo:test/childNode").getNodes();
    assertEquals(2, iterator.getSize());

    Property property = root.getProperty("test/exo:test/childNode/childNode2/jcr:content/jcr:data");
    assertEquals("Three bytes are concatenated, then split to form 4 groups of 6-bits each;",
                 property.getString());

    // property =
    // root.getProperty("childNode/childNode3/jcr:content/exo:content");
    // assertEquals("this is the binary content", property.getString());
  }

  public void testImportDocView() throws RepositoryException,
                                 InvalidSerializedDataException,
                                 ConstraintViolationException,
                                 IOException,
                                 ItemExistsException {
    root.addNode("test2");
    session.importXML("/test2", new ByteArrayInputStream(docView.getBytes()), 0);
    session.save();

    Node root = session.getRootNode().getNode("test2");
    NodeIterator iterator = root.getNodes();

    assertEquals(1, iterator.getSize());
    // log.debug(">>"+session.getWorkspaceDataContainer()); iterator =
    iterator = root.getNode("exo:test/childNode").getNodes();
    assertEquals(2, iterator.getSize());
    Property property = root.getProperty("exo:test/childNode/childNode3/jcr:content/jcr:data");
    assertEquals("this is the binary content", property.getString());
    property = root.getProperty("exo:test/childNode/childNode2/jcr:content/jcr:data");
    assertEquals("Three bytes are concatenated, then split to form 4 groups of 6-bits each;",
                 property.getString());

  }

  public void testImportDocViewECM() throws RepositoryException,
                                    InvalidSerializedDataException,
                                    ConstraintViolationException,
                                    IOException,
                                    ItemExistsException {
    root.addNode("testECM");
    session.importXML("/testECM", new ByteArrayInputStream(docViewECM.getBytes()), 0);
    session.save();

    Node testEcmNode = session.getRootNode().getNode("testECM");

    NodeIterator iterator = testEcmNode.getNodes();
    assertEquals(1, iterator.getSize());

    Node nodeArticle = root.getNode("testECM/test-article");
    assertEquals("title", nodeArticle.getProperty("exo:title").getString());
  }

  public void testImportDocumentViewStreamInvalidChildNodeType() throws Exception {

    Node testRoot = root.addNode("testRoot", "nt:folder");

    Node exportRoot = root.addNode("exportRoot", "exo:article");

    exportRoot.setProperty("exo:title", "title");
    exportRoot.setProperty("exo:text", "text");

    session.save();

    byte[] content = serialize(exportRoot, false, true);

    try {
      deserialize(testRoot,
                  XmlSaveType.SESSION,
                  true,
                  ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
                  new ByteArrayInputStream(content));
      fail();
    } catch (RepositoryException e) {
    }
  }

  public void testImportDocumentViewContentHandlerInvalidChildNodeType() throws Exception {

    Node testRoot = root.addNode("testRoot", "nt:folder");

    Node exportRoot = root.addNode("exportRoot", "exo:article");

    exportRoot.setProperty("exo:title", "title");
    exportRoot.setProperty("exo:text", "text");

    session.save();

    byte[] content = serialize(exportRoot, false, true);

    try {
      deserialize(testRoot,
                  XmlSaveType.SESSION,
                  false,
                  ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
                  new ByteArrayInputStream(content));
      fail();
    } catch (SAXException e) {
    }
  }

}
