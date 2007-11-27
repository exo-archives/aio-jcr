/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.importing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
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
public class BaseImportTest extends JcrAPIBaseTest {

  private static Log log = ExoLogger.getLogger("jcr.BaseImportTest");

  private void checkResult(Node importRoot,
                           Node testRoot,
                           Exception result,
                           boolean isImportedByStream,
                           int testedBehavior) throws RepositoryException {
    assertNotNull(testRoot);
    assertNotNull(importRoot);

    if (testedBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW) {
      assertNotNull("Exception should have been thrown", result);
      if (isImportedByStream) {
        assertTrue("Exception should be the ItemExistsException type",
                   result instanceof ItemExistsException);
      } else {

        assertTrue("Exception should be the SAXException type", result instanceof SAXException);
      }
    } else if (testedBehavior == ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) {

      assertNull("An exception should not have been thrown", result);
      Node oldNode1 = root.getNode("node1");
      assertNotNull(oldNode1);
      assertTrue(oldNode1.isNodeType("mix:referenceable"));
      String node1Uuid = oldNode1.getProperty("jcr:uuid").getString();

      assertTrue(oldNode1.hasNode("node5"));
      assertTrue(importRoot.hasNode("node1"));
      Node newNode1 = importRoot.getNode("node1");
      assertTrue(newNode1.isNodeType("mix:referenceable"));
      String node2Uuid = newNode1.getProperty("jcr:uuid").getString();

      assertFalse("node1 uuid and testRoot/node1 uuid should be different",
                  node1Uuid.equals(node2Uuid));
      assertTrue(newNode1.hasNode("node2"));
      Node node2 = newNode1.getNode("node2");
      assertTrue(node2.hasNode("node3"));
      assertTrue(node2.hasNode("node4"));

    } else if (testedBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING) {
      assertNull("An exception should not have been thrown", result);
      assertFalse(root.hasNode("node1"));
      assertFalse(root.hasNode("node1/node5"));
      assertTrue(importRoot.hasNode("node1"));
      assertTrue(importRoot.hasNode("node1/node2"));
      assertTrue(importRoot.hasNode("node1/node2/node3"));
      assertTrue(importRoot.hasNode("node1/node2/node4"));
    } else if (testedBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING) {
      assertNull("An exception should not have been thrown", result);
      assertTrue(root.hasNode("node1"));
      assertFalse(root.hasNode("node1/node5"));
      assertFalse(importRoot.hasNode("node1"));
      assertTrue(root.hasNode("node1/node2"));
      assertTrue(root.hasNode("node1/node2/node3"));
      assertTrue(root.hasNode("node1/node2/node4"));
    }

  }

  private void deserialize(Node importRoot,
                           XmlSaveType saveType,
                           boolean isImportedByStream,
                           int testedBehavior,
                           File content) throws PathNotFoundException,
                                        ItemExistsException,
                                        ConstraintViolationException,
                                        InvalidSerializedDataException,
                                        IOException,
                                        RepositoryException,
                                        SAXException {

    InputStream is = new BufferedInputStream(new FileInputStream(content));
    if (isImportedByStream) {
      if (saveType == XmlSaveType.SESSION) {
        session.importXML(importRoot.getPath(), is, testedBehavior);
        session.save();
      } else if (saveType == XmlSaveType.WORKSPACE) {
        workspace.importXML(importRoot.getPath(), is, testedBehavior);
      }

    } else {
      XMLReader reader = XMLReaderFactory.createXMLReader();
      if (saveType == XmlSaveType.SESSION) {
        reader.setContentHandler(session.getImportContentHandler(importRoot.getPath(),
                                                                 testedBehavior));
        session.save();
      } else if (saveType == XmlSaveType.WORKSPACE) {
        reader.setContentHandler(workspace.getImportContentHandler(importRoot.getPath(),
                                                                   testedBehavior));
      }
      InputSource inputSource = new InputSource(is);

      reader.parse(inputSource);

    }
  }

  private Node prepareForExport() throws RepositoryException {
    Node node1 = root.addNode("node1");
    node1.addMixin("mix:referenceable");
    Node node2 = node1.addNode("node2");
    Node node3 = node2.addNode("node3");
    Node node4 = node2.addNode("node4");
    session.save();
    assertTrue(root.getNode("node1").isNodeType("mix:referenceable"));
    return node1;
  }

  private Node prepareForImport(Node rootNode) throws RepositoryException {
    assertTrue(rootNode.hasNode("node2"));
    rootNode.getNode("node2").remove();
    rootNode.addNode("node5");
    Node importRoot = root.addNode("importRoot");
    session.save();
    assertFalse(rootNode.hasNode("node2"));
    return importRoot;
  }

  private File serialize(Node rootNode, boolean isSystemView, boolean isStream) throws IOException,
                                                                               RepositoryException,
                                                                               SAXException,
                                                                               TransformerConfigurationException {

    File content = File.createTempFile("baseImportTest", ".xml");
    content.deleteOnExit();
    OutputStream outStream = new FileOutputStream(content);
    if (isSystemView) {

      if (isStream) {
        session.exportSystemView(rootNode.getPath(), outStream, false, false);
      } else {
        SAXTransformerFactory saxFact = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler handler = saxFact.newTransformerHandler();
        handler.setResult(new StreamResult(outStream));
        session.exportSystemView(rootNode.getPath(), handler, false, false);
      }
    } else {
      if (isStream) {
        session.exportDocumentView(rootNode.getPath(), outStream, false, false);
      } else {
        SAXTransformerFactory saxFact = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler handler = saxFact.newTransformerHandler();
        handler.setResult(new StreamResult(outStream));
        session.exportDocumentView(rootNode.getPath(), handler, false, false);
      }
    }
    outStream.close();
    return content;
  }

  protected void importUuidCollisionTest(boolean isSystemView,
                                         boolean isExportedByStream,
                                         boolean isImportedByStream,
                                         XmlSaveType saveType,
                                         int testedBehavior) throws RepositoryException,
                                                            TransformerConfigurationException,
                                                            IOException,
                                                            SAXException {
    Node testRoot = prepareForExport();
    File content = serialize(testRoot, isSystemView, isExportedByStream);
    Node importRoot = prepareForImport(testRoot);
    Exception result = null;

    try {
      deserialize(importRoot, saveType, isImportedByStream, testedBehavior, content);
    } catch (Exception e) {
      result = e;
    }

    checkResult(importRoot, testRoot, result, isImportedByStream, testedBehavior);

  }
}