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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.log.ExoLogger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: TestImport.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestImport extends BaseImportTest {
  private Log          log        = ExoLogger.getLogger(TestImport.class);

  private final String docView    = "<exo:test xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" "
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

  private final String docViewECM = "<test-article xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:kfx=\"http://www.exoplatform.com/jcr/kfx/1.1/\" xmlns:Fwd=\"http://www.exoplatform.com/jcr/Fwd/1.1/\" xmlns:Re=\"http://www.exoplatform.com/jcr/Re/1.1/\" xmlns:rma=\"http://www.rma.com/jcr/\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" xmlns:fn=\"http://www.w3.org/2004/10/xpath-functions\" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:exo=\"http://www.exoplatform.com/jcr/exo/1.0\" exo:summary=\"\" exo:voteTotal=\"0\" exo:votingRate=\"0.0\" jcr:primaryType=\"exo:article\" jcr:mixinTypes=\"mix:votable mix:i18n\" jcr:uuid=\"6da3fcebc0a800070043d28761e00078\" exo:language=\"en\" exo:title=\"title\" exo:text=\"\"></test-article>";

  private final String sysView    = "<sv:node xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" "
                                      + "xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" "
                                      + "xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" "
                                      + "xmlns:exo=\"http://www.exoplatform.com/jcr/exo/1.0\" "
                                      + "xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" sv:name=\"exo:test\">"
                                      + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:unstructured</sv:value></sv:property>"
                                      +

                                      "<sv:node sv:name=\"childNode\">"
                                      + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:folder</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:created\" sv:type=\"Date\"><sv:value>2004-08-18T15:17:00.856+01:00</sv:value></sv:property>"
                                      + "<sv:node sv:name=\"childNode3\">"
                                      + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:file</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:created\" sv:type=\"Date\"><sv:value>2004-08-18T15:17:00.856+01:00</sv:value></sv:property>"
                                      + "<sv:node sv:name=\"jcr:content\">"
                                      + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:resource</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:uuid\" sv:type=\"String\"><sv:value>1092835020617_</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:data\" sv:type=\"Binary\"><sv:value>dGhpcyBpcyB0aGUgYmluYXJ5IGNvbnRlbnQ=</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:mimeType\" sv:type=\"String\"><sv:value>application/unknown</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:lastModified\" sv:type=\"Date\"><sv:value>2004-08-18T15:17:00.856+01:00</sv:value></sv:property>"
                                      + "</sv:node>"
                                      + "</sv:node>"
                                      + "<sv:node sv:name=\"childNode2\">"
                                      + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:file</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:created\" sv:type=\"Date\"><sv:value>2004-08-18T15:17:00.856+01:00</sv:value></sv:property>"
                                      + "<sv:node sv:name=\"jcr:content\">"
                                      + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:resource</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:uuid\" sv:type=\"String\"><sv:value>1092835020616_</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:data\" sv:type=\"Binary\"><sv:value>dGhpcyBpcyB0aGUgYmluYXJ5IGNvbnRlbnQ=</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:mimeType\" sv:type=\"String\"><sv:value>text/text</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:lastModified\" sv:type=\"Date\"><sv:value>2004-08-18T15:17:00.856+01:00</sv:value></sv:property>"
                                      + "</sv:node>"
                                      + "</sv:node>"
                                      + "</sv:node>"
                                      +

                                      "<sv:node sv:name='uuidNode1'>"
                                      + "<sv:property sv:name='jcr:primaryType' sv:type='Name'><sv:value>nt:unstructured</sv:value></sv:property>"
                                      + "<sv:property sv:name='jcr:mixinTypes' sv:type='Name'>"
                                      + "<sv:value>mix:referenceable</sv:value>"
                                      + "<!-- sv:value>exo:accessControllable</sv:value -->"
                                      + "</sv:property>"
                                      + "<sv:property sv:name='jcr:test' sv:type='String'><sv:value>val1</sv:value><sv:value>val1</sv:value></sv:property>"
                                      + "<sv:property sv:name='source' sv:type='String'><sv:value>sysView</sv:value></sv:property>"
                                      + "<sv:property sv:name='jcr:uuid' sv:type='String'><sv:value>id_uuidNode1</sv:value></sv:property>"
                                      + "</sv:node>"
                                      +

                                      "<sv:node sv:name='uuidNode2'>"
                                      + "<sv:property sv:name='jcr:primaryType' sv:type='Name'><sv:value>nt:unstructured</sv:value></sv:property>"
                                      + "<sv:property sv:name='jcr:mixinTypes' sv:type='Name'><sv:value>mix:referenceable</sv:value></sv:property>"
                                      + "<sv:property sv:name='jcr:test' sv:type='String'><sv:value>val2</sv:value><sv:value>val1</sv:value></sv:property>"
                                      + "<sv:property sv:name='jcr:uuid' sv:type='String'><sv:value>uuidNode2</sv:value></sv:property>"
                                      + "<sv:property sv:name='ref_to_1' sv:type='Reference'><sv:value>id_uuidNode1</sv:value></sv:property>"
                                      + "<sv:property sv:name='ref_to_1_and_3' sv:type='Reference'><sv:value>id_uuidNode1</sv:value><sv:value>id_uuidNode3</sv:value></sv:property>"
                                      + "<sv:property sv:name='ref_to_3' sv:type='Reference'><sv:value>id_uuidNode3</sv:value></sv:property>"
                                      + "</sv:node>"
                                      +

                                      "<sv:node sv:name='uuidNode3'>"
                                      + "<sv:property sv:name='jcr:primaryType' sv:type='Name'><sv:value>nt:unstructured</sv:value></sv:property>"
                                      + "<sv:property sv:name='jcr:mixinTypes' sv:type='Name'><sv:value>mix:referenceable</sv:value></sv:property>"
                                      + "<sv:property sv:name='ref_to_1' sv:type='Reference'><sv:value>id_uuidNode1</sv:value></sv:property>"
                                      + "<sv:property sv:name='jcr:test' sv:type='String'><sv:value>val1</sv:value><sv:value>va31</sv:value></sv:property>"
                                      + "<sv:property sv:name='jcr:uuid' sv:type='String'><sv:value>id_uuidNode3</sv:value></sv:property>"
                                      + "</sv:node>"
                                      +

                                      "<sv:node sv:name=\"childNode4\">"
                                      + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:unstructured</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:test\" sv:type=\"String\"><sv:value>val1</sv:value><sv:value>val1</sv:value></sv:property>"
                                      + "</sv:node>" +

                                      "</sv:node>";

  private final String sysView2   = "<sv:node xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" "
                                      + "xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" "
                                      + "xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" "
                                      + "xmlns:exo=\"http://www.exoplatform.com/jcr/exo/1.0\" "
                                      + "xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" sv:name=\"childNode2\">"
                                      + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:file</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:created\" sv:type=\"Date\"><sv:value>2004-08-18T15:17:00.856+01:00</sv:value></sv:property>"
                                      + "<sv:node sv:name=\"jcr:content\">"
                                      + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:resource</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:uuid\" sv:type=\"String\"><sv:value>1092835020616_</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:data\" sv:type=\"Binary\"><sv:value>dGhpcyBpcyB0aGUgYmluYXJ5IGNvbnRlbnQ=</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:mimeType\" sv:type=\"String\"><sv:value>text/text</sv:value></sv:property>"
                                      + "<sv:property sv:name=\"jcr:lastModified\" sv:type=\"Date\"><sv:value>2004-08-18T15:17:00.856+01:00</sv:value></sv:property>"
                                      // Special unexisting property
                                      + "<sv:property sv:name=\"jcr:lastModified2\" sv:type=\"Date\"><sv:value>2004-08-18T15:17:00.856+01:00</sv:value></sv:property>"
                                      + "</sv:node>" + "</sv:node>";

  private final String docView2   = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<childNode2 "
                                      + "xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" "
                                      + "xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" "
                                      + "jcr:primaryType=\"nt:file\" "
                                      + "jcr:created=\"2004-08-18T17:17:00.856+03:00\">"
                                      + "<jcr:content " + "jcr:primaryType=\"nt:resource\" "
                                      + "jcr:uuid=\"6a3859dac0a8004b006e6e0bf444ebaa\" "
                                      + "jcr:data=\"dGhpcyBpcyB0aGUgYmluYXJ5IGNvbnRlbnQ=\" "
                                      + "jcr:lastModified=\"2004-08-18T17:17:00.856+03:00\" "
                                      + "jcr:lastModified2=\"2004-08-18T17:17:00.856+03:00\" "
                                      + "jcr:mimeType=\"text/text\"/>" + "</childNode2>";

  public void initRepository() throws RepositoryException {
    session.getRootNode().addNode("test", "nt:unstructured");
    session.getRootNode().addNode("test2", "nt:unstructured");
    session.getRootNode().addNode("testECM", "nt:unstructured");
    NodeTypeManagerImpl ntManager = session.getWorkspace().getNodeTypeManager();
    byte[] xmlData1 = readXmlContent("/org/exoplatform/services/jcr/api/nodetypes/ecm/nodetypes-config.xml");
    ByteArrayInputStream xmlInput1 = new ByteArrayInputStream(xmlData1);
    ntManager.registerNodeTypes(xmlInput1, 0);
    byte[] xmlData2 = readXmlContent("/org/exoplatform/services/jcr/api/nodetypes/ecm/nodetypes-config-extended.xml");
    ByteArrayInputStream xmlInput2 = new ByteArrayInputStream(xmlData2);
    ntManager.registerNodeTypes(xmlInput2, 0);
  }

  public void test_docView_UuidBehaviourIMPORT_UUID_COLLISION_REPLACE_EXISTING() throws Exception {

    XMLReader reader = XMLReaderFactory.createXMLReader();
    InputSource inputSource;

    // first import
    reader.setContentHandler(session.getImportContentHandler("/test",
                                                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING));

    inputSource = new InputSource(new ByteArrayInputStream(docView.getBytes()));
    reader.parse(inputSource);

    Node nodeDocViewUuidNode1 = session.getRootNode().getNode("test/exo:test/uuidNode1");

    Property propDocViewUuidNode1 = nodeDocViewUuidNode1.getProperty("jcr:uuid");

    assertEquals("Uuid must be same (docView)", "id_uuidNode1", propDocViewUuidNode1.getString());

    assertEquals("check (docView)", "docView", nodeDocViewUuidNode1.getProperty("source")
                                                                   .getString());

    session.save();
    // log.debug(" node location id "+((NodeImpl)nodeDocViewUuidNode1);
    assertNotNull("session.getNodeByUUID doc", session.getNodeByUUID("id_uuidNode1"));
    assertEquals("Property source by uuid", "docView", session.getNodeByUUID("id_uuidNode1")
                                                              .getProperty("source")
                                                              .getString());

    reader.setContentHandler(session.getImportContentHandler("/test2",
                                                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING));

    inputSource = new InputSource(new ByteArrayInputStream(sysView.getBytes()));
    reader.parse(inputSource);

    session.save();

    try {
      Node nodeSysViewUuidNode1 = session.getRootNode().getNode("test2/exo:test/uuidNode1");
      fail("test2/exo:test/uuidNode1 must be replace");
    } catch (PathNotFoundException ex) {

    }

    Node nodeSysViewUuidNode1 = session.getRootNode().getNode("test/exo:test/uuidNode1");
    Property propSysViewUuidNode1 = nodeSysViewUuidNode1.getProperty("jcr:uuid");

    assertEquals("Uuid must be same (sysView)", "id_uuidNode1", propSysViewUuidNode1.getString());

    assertEquals("Sourse  sysView)", "sysView", nodeSysViewUuidNode1.getProperty("source")
                                                                    .getString());
  }

  public void test_docViewUuidBehaviourIMPORT_UUID_COLLISION_REMOVE_EXISTING() throws Exception {

    XMLReader reader = XMLReaderFactory.createXMLReader();
    InputSource inputSource;

    // first import
    reader.setContentHandler(session.getImportContentHandler("/test",
                                                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING));

    inputSource = new InputSource(new ByteArrayInputStream(docView.getBytes()));
    reader.parse(inputSource);

    Node nodeDocViewUuidNode1 = session.getRootNode().getNode("test/exo:test/uuidNode1");

    Property propDocViewUuidNode1 = nodeDocViewUuidNode1.getProperty("jcr:uuid");

    assertEquals("Uuid must be same (docView)", "id_uuidNode1", propDocViewUuidNode1.getString());

    assertEquals("check (docView)", "docView", nodeDocViewUuidNode1.getProperty("source")
                                                                   .getString());

    session.save();
    // log.debug(" node location id "+((NodeImpl)nodeDocViewUuidNode1);
    assertNotNull("session.getNodeByUUID doc", session.getNodeByUUID("id_uuidNode1"));

    reader.setContentHandler(session.getImportContentHandler("/test2",
                                                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING));

    inputSource = new InputSource(new ByteArrayInputStream(sysView.getBytes()));
    reader.parse(inputSource);

    session.save();

    Node nodeSysViewUuidNode1 = session.getRootNode().getNode("test2/exo:test/uuidNode1");

    Property propSysViewUuidNode1 = nodeSysViewUuidNode1.getProperty("jcr:uuid");

    assertEquals("Uuid must be same (sysView)", "id_uuidNode1", propSysViewUuidNode1.getString());

    assertEquals("Sourse  sysView)", "sysView", nodeSysViewUuidNode1.getProperty("source")
                                                                    .getString());

    try {
      session.getRootNode().getNode("test/exo:test/uuidNode1");
      fail("test/exo:test/uuidNode1 must be deleted");
    } catch (PathNotFoundException ex) {

    }

  }

  public void testDocImportUnExistingPropertyDefinition() throws Exception {
    try {
      ((ExtendedSession) session).importXML(root.getPath(),
                                            new ByteArrayInputStream(docView2.getBytes()),
                                            0,
                                            true);
      session.save();
      fail();
    } catch (RepositoryException e) {
      // ok
    }
    try {
      ((ExtendedSession) session).importXML(root.getPath(),
                                            new ByteArrayInputStream(docView2.getBytes()),
                                            0,
                                            false);
      session.save();
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testDocViewImportContentHandler() throws Exception {

    XMLReader reader = XMLReaderFactory.createXMLReader();

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

  public void testEmpty() throws Exception {

  }

  public void testImportDocView() throws RepositoryException,
                                 InvalidSerializedDataException,
                                 ConstraintViolationException,
                                 IOException,
                                 ItemExistsException {

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

    session.importXML("/testECM", new ByteArrayInputStream(docViewECM.getBytes()), 0);
    session.save();

    Node testEcmNode = session.getRootNode().getNode("testECM");

    NodeIterator iterator = testEcmNode.getNodes();
    assertEquals(1, iterator.getSize());

    Node nodeArticle = root.getNode("testECM/test-article");
    assertEquals("title", nodeArticle.getProperty("exo:title").getString());
  }

  public void testImportSysView() throws RepositoryException,
                                 InvalidSerializedDataException,
                                 ConstraintViolationException,
                                 IOException,
                                 ItemExistsException {

    // session.getRootNode().addNode("test", "nt:unstructured");
    session.importXML("/test", new ByteArrayInputStream(sysView.getBytes()), 0);
    session.save();

    Node root = session.getRootNode().getNode("test");
    NodeIterator iterator = root.getNodes();
    assertEquals(1, iterator.getSize());

    // log.debug(">>"+session.getWorkspaceDataContainer());

    iterator = root.getNode("exo:test/childNode").getNodes();
    assertEquals(2, iterator.getSize());

    Property property = root.getProperty("exo:test/childNode/childNode3/jcr:content/jcr:data");
    assertEquals("this is the binary content", property.getString());

    property = root.getProperty("exo:test/childNode/childNode2/jcr:content/jcr:data");
    assertEquals("this is the binary content", property.getString());

    property = root.getProperty("exo:test/childNode4/jcr:test");
    assertEquals(2, property.getValues().length);
    assertEquals("val1", property.getValues()[0].getString());
  }

  public void testImportSysViewContentHandler() throws Exception {

    // session.getRootNode().addNode("test", "nt:unstructured");

    XMLReader reader = XMLReaderFactory.createXMLReader();

    reader.setContentHandler(session.getImportContentHandler("/test", 0));
    InputSource inputSource = new InputSource(new ByteArrayInputStream(sysView.getBytes()));
    reader.parse(inputSource);

    session.save();

    Node root = session.getRootNode().getNode("test");
    NodeIterator iterator = root.getNodes();
    assertEquals(1, iterator.getSize());

    // log.debug(">>"+session.getWorkspaceDataContainer());

    iterator = root.getNode("exo:test/childNode").getNodes();
    assertEquals(2, iterator.getSize());

    Property property = root.getProperty("exo:test/childNode/childNode3/jcr:content/jcr:data");
    assertEquals("this is the binary content", property.getString());

    property = root.getProperty("exo:test/childNode/childNode2/jcr:content/jcr:data");
    assertEquals("this is the binary content", property.getString());

    property = root.getProperty("exo:test/childNode4/jcr:test");
    assertEquals(2, property.getValues().length);
    assertEquals("val1", property.getValues()[0].getString());
  }

  public void testSysImportUnExistingPropertyDefinition() throws Exception {

    try {
      ((ExtendedSession) session).importXML(root.getPath(),
                                            new ByteArrayInputStream(sysView2.getBytes()),
                                            0,
                                            true);
      session.save();
      fail();
    } catch (RepositoryException e) {
      // ok
    }
    try {
      ((ExtendedSession) session).importXML(root.getPath(),
                                            new ByteArrayInputStream(sysView2.getBytes()),
                                            0,
                                            false);
      session.save();

    } catch (RepositoryException e) {
      // e.printStackTrace();
      fail();
    }

  }

  public void testUuidBehaviourIMPORT_UUID_COLLISION_THROW() throws Exception {

    XMLReader reader = XMLReaderFactory.createXMLReader();
    root.addNode("testCollision");
    session.save();
    reader.setContentHandler(session.getImportContentHandler("/testCollision",
                                                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW));

    InputSource inputSource = new InputSource(new ByteArrayInputStream(sysView.getBytes()));
    reader.parse(inputSource);

    session.save();

    Node node = session.getRootNode().getNode("testCollision/exo:test/uuidNode1");

    Value valueUuid = node.getProperty("jcr:uuid").getValue();

    assertEquals("Uuid must exists [" + valueUuid.getString() + "]",
                 "id_uuidNode1",
                 valueUuid.getString());

    try {
      session.getNodeByUUID("id_uuidNode1");
    } catch (ItemNotFoundException ex) {
      fail("not find node with uuid [id_uuidNode1] " + ex.getMessage());
    }

    Node nodeUuidNode3 = session.getRootNode().getNode("testCollision/exo:test/uuidNode3");

    Value valueRef3ToUuidNode1 = nodeUuidNode3.getProperty("ref_to_1").getValue();

    assertEquals("ref_to_1", "id_uuidNode1", valueRef3ToUuidNode1.getString());

    // part 2
    reader.setContentHandler(session.getImportContentHandler("/test2",
                                                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW));

    inputSource = new InputSource(new ByteArrayInputStream(sysView.getBytes()));
    try {
      reader.parse(inputSource);
      fail("Must failed");
    } catch (SAXException ex) {
      log.debug("Sax exc occure", ex);
    }

  }

  public void testUuidBehaviourIMPORT_UUID_CREATE_NEW() throws Exception {

    PlainChangesLog changesLog = new PlainChangesLogImpl();

    TransientNodeData testNodeData = TransientNodeData.createNodeData((NodeData) ((NodeImpl) root).getData(),
                                                                      new InternalQName("",
                                                                                        "nodeWithPredefUuid"),
                                                                      Constants.NT_UNSTRUCTURED,
                                                                      "id_uuidNode1");
    changesLog.add(ItemState.createAddedState(testNodeData));
    TransientPropertyData primaryType = TransientPropertyData.createPropertyData(testNodeData,
                                                                                 Constants.JCR_PRIMARYTYPE,
                                                                                 PropertyType.NAME,
                                                                                 false);
    primaryType.setValue(new TransientValueData(testNodeData.getPrimaryTypeName()));
    changesLog.add(ItemState.createAddedState(primaryType));

    session.getTransientNodesManager().getTransactManager().save(changesLog);
    root.getNode("nodeWithPredefUuid").addMixin("mix:referenceable");

    session.save();
    XMLReader reader = XMLReaderFactory.createXMLReader();

    reader.setContentHandler(session.getImportContentHandler("/test",
                                                             ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW));

    InputSource inputSource = new InputSource(new ByteArrayInputStream(sysView.getBytes()));
    reader.parse(inputSource);

    session.save();

    Node nodeUuidNode1 = session.getRootNode().getNode("test/exo:test/uuidNode1");
    Value valueUuidNode1 = nodeUuidNode1.getProperty("jcr:uuid").getValue();

    assertTrue("Uuid must be new [" + valueUuidNode1.getString() + "]",
               !"id_uuidNode1".equals(valueUuidNode1.getString()));

    assertFalse(session.getNodeByUUID("id_uuidNode1").getName().equals("uuidNode1"));

  }

  public void testImportSystemViewStreamInvalidChildNodeType() throws Exception {

    Node testRoot = root.addNode("testRoot", "nt:folder");

    Node exportRoot = root.addNode("exportRoot", "exo:article");

    exportRoot.setProperty("exo:title", "title");
    exportRoot.setProperty("exo:text", "text");

    session.save();
    // try {
    // testRoot.addNode("test", "exo:article");
    // fail();
    // } catch (RepositoryException e) {
    // }

    byte[] content = serialize(exportRoot, true, true);

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

  public void testImportSystemViewContentHandlerInvalidChildNodeType() throws Exception {

    Node testRoot = root.addNode("testRoot", "nt:folder");

    Node exportRoot = root.addNode("exportRoot", "exo:article");

    exportRoot.setProperty("exo:title", "title");
    exportRoot.setProperty("exo:text", "text");

    session.save();

    byte[] content = serialize(exportRoot, true, true);

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

  private byte[] readXmlContent(String fileName) {
    try {
      InputStream is = TestImport.class.getResourceAsStream(fileName);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      int r = is.available();
      byte[] bs = new byte[r];
      while (r > 0) {
        r = is.read(bs);
        if (r > 0) {
          output.write(bs, 0, r);
        }
        r = is.available();
      }
      is.close();
      return output.toByteArray();
    } catch (Exception e) {
      log.error("Error read file '" + fileName + "' with NodeTypes. Error:" + e);
      return null;
    }
  }
}
