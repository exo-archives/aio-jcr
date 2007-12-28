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
package org.exoplatform.services.jcr.api.exporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestExportImport extends ExportBase {
  private final int SNS_NODES_COUNT = 10;

  public TestExportImport() throws ParserConfigurationException {
    super();
  }

  // TODO Fix. Move to non system workspace.
  public void _testWorkspaceExportImportValuesSysView() throws Exception {
    Node testNode = root.addNode("testExportImport");
    for (int i = 0; i < valList.size(); i++) {
      testNode.setProperty("prop" + i + "_string", valList.get(i), PropertyType.STRING);
      testNode.setProperty("prop" + i + "_binary", valList.get(i), PropertyType.BINARY);
    }
    session.save();
    File destFile = File.createTempFile("testWorkspaceExportImportValuesSysView", ".xml");
    destFile.deleteOnExit();
    OutputStream outStream = new FileOutputStream(destFile);
    session.exportWorkspaceSystemView(outStream, false, false);
    outStream.close();

    testNode.remove();
    session.save();

    session.importXML(root.getPath(),
                      new FileInputStream(destFile),
                      ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    session.save();

    Node newNode = root.getNode("testExportImport");

    for (int i = 0; i < valList.size(); i++) {
      if (valList.get(i).length > 1) {
        Value[] stringValues = newNode.getProperty("prop" + i + "_string").getValues();
        for (int j = 0; j < stringValues.length; j++) {
          assertEquals(stringValues[j].getString(), valList.get(i)[j]);
        }
        Value[] binaryValues = newNode.getProperty("prop" + i + "_binary").getValues();
        for (int j = 0; j < stringValues.length; j++) {
          assertEquals(binaryValues[j].getString(), valList.get(i)[j]);
        }
      } else {
        assertEquals(valList.get(i)[0], newNode.getProperty("prop" + i + "_string")
                                               .getValue()
                                               .getString());
        assertEquals(valList.get(i)[0], newNode.getProperty("prop" + i + "_binary")
                                               .getValue()
                                               .getString());

      }
    }
    destFile.delete();
  }

  public void testExportImportCustomNodeType() throws Exception {
    Node folder = root.addNode("childNode", "nt:folder");
    Node file = folder.addNode("childNode2", "exo:salestool");

    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", session.getValueFactory()
                                               .createValue("this is the content",
                                                            PropertyType.BINARY));
    contentNode.setProperty("jcr:mimeType", "application/octet-stream");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory()
                                                       .createValue(Calendar.getInstance()));

    session.save();

    File destFile = File.createTempFile("testExportImportValuesSysView", ".xml");
    destFile.deleteOnExit();
    OutputStream outStream = new FileOutputStream(destFile);
    session.exportSystemView(file.getPath(), outStream, false, false);
    outStream.close();

    folder.remove();
    session.save();

    session.importXML(root.getPath(),
                      new FileInputStream(destFile),
                      ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    session.save();

  }

  public void testExportImportValuesSysView() throws Exception {
    Node testNode = root.addNode("testExportImport");
    for (int i = 0; i < valList.size(); i++) {
      testNode.setProperty("prop" + i + "_string", valList.get(i), PropertyType.STRING);
      testNode.setProperty("prop" + i + "_binary", valList.get(i), PropertyType.BINARY);
    }
    session.save();
    File destFile = File.createTempFile("testExportImportValuesSysView", ".xml");
    destFile.deleteOnExit();
    OutputStream outStream = new FileOutputStream(destFile);
    session.exportSystemView(testNode.getPath(), outStream, false, false);
    outStream.close();

    testNode.remove();
    session.save();

    session.importXML(root.getPath(),
                      new FileInputStream(destFile),
                      ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    session.save();

    Node newNode = root.getNode("testExportImport");

    for (int i = 0; i < valList.size(); i++) {
      if (valList.get(i).length > 1) {
        Value[] stringValues = newNode.getProperty("prop" + i + "_string").getValues();
        for (int j = 0; j < stringValues.length; j++) {
          assertEquals(stringValues[j].getString(), valList.get(i)[j]);
        }
        Value[] binaryValues = newNode.getProperty("prop" + i + "_binary").getValues();
        for (int j = 0; j < stringValues.length; j++) {
          assertEquals(binaryValues[j].getString(), valList.get(i)[j]);
        }
      } else {
        assertEquals(valList.get(i)[0], newNode.getProperty("prop" + i + "_string")
                                               .getValue()
                                               .getString());
        assertEquals(valList.get(i)[0], newNode.getProperty("prop" + i + "_binary")
                                               .getValue()
                                               .getString());

      }
    }
    destFile.delete();
  }

  public void testMixinExportImportDocumentViewContentHandler() throws Exception {

    Node testNode = root.addNode("childNode");
    testNode.setProperty("a", 1);
    testNode.addMixin("mix:versionable");
    testNode.addMixin("mix:lockable");

    session.save();
    doVersionTests(testNode);

    session.save();

    doExportImport(root, "childNode", false, true, null);

    session.save();

    Node childNode = root.getNode("childNode");
    doVersionTests(childNode);
  }

  public void testMixinExportImportDocumentViewStream() throws Exception {

    Node testNode = root.addNode("childNode");
    testNode.setProperty("a", 1);
    testNode.addMixin("mix:versionable");
    testNode.addMixin("mix:lockable");

    session.save();
    doVersionTests(testNode);

    session.save();

    doExportImport(root, "childNode", false, false, null);

    session.save();

    Node childNode = root.getNode("childNode");
    doVersionTests(childNode);
  }

  public void testMixinExportImportSystemViewContentHandler() throws Exception {

    Node testNode = root.addNode("childNode");
    testNode.setProperty("a", 1);
    testNode.addMixin("mix:versionable");
    testNode.addMixin("mix:lockable");

    session.save();
    doVersionTests(testNode);

    doExportImport(root, "childNode", true, true, null);

    Node childNode = root.getNode("childNode");
    doVersionTests(childNode);
  }

  public void testMixinExportImportSystemViewStream() throws Exception {

    Node testNode = root.addNode("childNode");
    testNode.setProperty("a", 1);
    testNode.addMixin("mix:versionable");
    testNode.addMixin("mix:lockable");

    session.save();
    doVersionTests(testNode);

    doExportImport(root, "childNode", true, false, null);

    Node childNode = root.getNode("childNode");
    doVersionTests(childNode);
  }

  public void testSNSDocumentViewCh() throws Exception {
    Node testSNS = root.addNode("testSNS");
    testSNS.addMixin("mix:versionable");

    for (int i = 0; i < SNS_NODES_COUNT; i++) {
      testSNS.addNode("nodeSNS");
    }

    Node testDest = root.addNode("testDest");
    session.save();

    doExportImport(root, "testSNS", false, true, testDest);
    assertTrue(testDest.hasNode("testSNS"));
    Node testSNSNew = testDest.getNode("testSNS");

    for (int i = 0; i < SNS_NODES_COUNT; i++) {
      testSNS.addNode("nodeSNS");
      assertTrue(testSNSNew.hasNode("nodeSNS[" + (i + 1) + "]"));
    }

  }

  public void testSNSDocumentViewStream() throws Exception {
    Node testSNS = root.addNode("testSNS");
    testSNS.addMixin("mix:versionable");

    for (int i = 0; i < SNS_NODES_COUNT; i++) {
      testSNS.addNode("nodeSNS");
    }

    Node testDest = root.addNode("testDest");
    session.save();

    doExportImport(root, "testSNS", false, false, testDest);
    assertTrue(testDest.hasNode("testSNS"));
    Node testSNSNew = testDest.getNode("testSNS");

    for (int i = 0; i < SNS_NODES_COUNT; i++) {
      testSNS.addNode("nodeSNS");
      assertTrue(testSNSNew.hasNode("nodeSNS[" + (i + 1) + "]"));
    }

  }

  public void testSNSSystemViewCh() throws Exception {
    Node testSNS = root.addNode("testSNS");
    testSNS.addMixin("mix:versionable");
    for (int i = 0; i < SNS_NODES_COUNT; i++) {
      testSNS.addNode("nodeSNS");
    }
    Node testDest = root.addNode("testDest");
    session.save();

    doExportImport(root, "testSNS", true, true, testDest);
    assertTrue(testDest.hasNode("testSNS"));
    Node testSNSNew = testDest.getNode("testSNS");

    for (int i = 0; i < SNS_NODES_COUNT; i++) {
      testSNS.addNode("nodeSNS");
      assertTrue(testSNSNew.hasNode("nodeSNS[" + (i + 1) + "]"));
    }
  }

  public void testSNSSystemViewStream() throws Exception {
    Node testSNS = root.addNode("testSNS");
    testSNS.addMixin("mix:versionable");
    for (int i = 0; i < SNS_NODES_COUNT; i++) {
      testSNS.addNode("nodeSNS");
    }
    Node testDest = root.addNode("testDest");
    session.save();

    doExportImport(root, "testSNS", true, false, testDest);
    assertTrue(testDest.hasNode("testSNS"));
    Node testSNSNew = testDest.getNode("testSNS");

    for (int i = 0; i < SNS_NODES_COUNT; i++) {
      testSNS.addNode("nodeSNS");
      assertTrue(testSNSNew.hasNode("nodeSNS[" + (i + 1) + "]"));
    }

  }

  private void doExportImport(Node parentNode,
                              String nodeName,
                              boolean isSystemView,
                              boolean isContentHandler,
                              Node destParentNode) throws RepositoryException,
                                                  IOException,
                                                  TransformerConfigurationException,
                                                  SAXException {
    Node exportNode = parentNode.getNode(nodeName);
    File destFile = File.createTempFile("testExportImport", ".xml");
    destFile.deleteOnExit();
    OutputStream outStream = new FileOutputStream(destFile);

    if (isSystemView) {
      if (isContentHandler) {
        SAXTransformerFactory saxFact = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler handler = saxFact.newTransformerHandler();
        handler.setResult(new StreamResult(outStream));
        session.exportSystemView(exportNode.getPath(), handler, false, false);
      } else {
        session.exportSystemView(exportNode.getPath(), outStream, false, false);
      }
    } else {
      if (isContentHandler) {
        SAXTransformerFactory saxFact = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler handler = saxFact.newTransformerHandler();
        handler.setResult(new StreamResult(outStream));
        session.exportDocumentView(exportNode.getPath(), handler, false, false);
      } else {
        session.exportDocumentView(exportNode.getPath(), outStream, false, false);
      }
    }

    outStream.close();
    if (destParentNode == null) {
      exportNode.remove();
      session.save();
    }

    session.importXML(destParentNode != null ? destParentNode.getPath() : root.getPath(),
                      new FileInputStream(destFile),
                      ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

    session.save();
    assertTrue(parentNode.hasNode(nodeName));

  }

  private void doVersionTests(Node testNode) throws RepositoryException {
    assertTrue(testNode.isNodeType("mix:lockable"));
    assertTrue(testNode.isNodeType("mix:referenceable"));
    assertTrue(testNode.hasProperty("jcr:uuid"));
    assertTrue(testNode.isCheckedOut());
    testNode.setProperty("a", 1);

    session.save();
    Version version1 = testNode.checkin();
    testNode.checkout();
    testNode.setProperty("a", 2);

    session.save();
    assertEquals(2, testNode.getProperty("a").getLong());
    Version version2 = testNode.checkin();
    testNode.checkout();
    Property prop2 = testNode.getProperty("jcr:mixinTypes");
    assertEquals(PropertyType.NAME, prop2.getType());

    assertTrue(testNode.isCheckedOut());
    testNode.restore(version1, true);
    testNode.checkout();
    assertTrue(testNode.isCheckedOut());
    assertEquals(1, testNode.getProperty("a").getLong());

    Property prop = testNode.getProperty("jcr:mixinTypes");
    assertEquals(PropertyType.NAME, prop.getType());

    assertTrue(testNode.isNodeType("mix:lockable"));
    assertTrue(testNode.isNodeType("mix:referenceable"));
    assertTrue(testNode.hasProperty("jcr:uuid"));
    assertTrue(testNode.isCheckedOut());

  }
}
