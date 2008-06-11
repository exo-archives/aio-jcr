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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.core.ExtendedWorkspace;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: AbstractImportTest.java 14244 2008-05-14 11:44:54Z ksm $
 */
public abstract class AbstractImportTest extends JcrAPIBaseTest {
  /**
   * Initialization flag.
   */
  private static boolean isInitialized = false;

  /**
   * Logger.
   */
  private Log            log           = ExoLogger.getLogger("jcr.BaseImportTest");

  @Override
  public void initRepository() throws RepositoryException {
    super.initRepository();
    if (!isInitialized) {
      NodeTypeManagerImpl ntManager = session.getWorkspace().getNodeTypeManager();
      InputStream is = TestDocumentViewImport.class.getResourceAsStream("/nodetypes/ext-registry-nodetypes.xml");
      ntManager.registerNodeTypes(is, 0);
      ntManager.registerNodeTypes(TestDocumentViewImport.class.getResourceAsStream("/org/exoplatform/services/jcr/api/nodetypes/ecm/nodetypes-config.xml"),
                                  0);
      ntManager.registerNodeTypes(TestDocumentViewImport.class.getResourceAsStream("/org/exoplatform/services/jcr/api/nodetypes/ecm/nodetypes-config-extended.xml"),
                                  0);
      isInitialized = true;
    }
  }

  /**
   * Deserialize xml content to specific node from InputStream.
   * 
   * @param importRoot
   * @param saveType
   * @param isImportedByStream
   * @param uuidBehavior
   * @param is
   * @throws RepositoryException
   * @throws SAXException
   * @throws IOException
   */
  protected void deserialize(Node importRoot,
                             XmlSaveType saveType,
                             boolean isImportedByStream,
                             int uuidBehavior,
                             InputStream is) throws RepositoryException, SAXException, IOException {

    ExtendedSession extendedSession = (ExtendedSession) importRoot.getSession();
    ExtendedWorkspace extendedWorkspace = (ExtendedWorkspace) extendedSession.getWorkspace();
    if (isImportedByStream) {
      if (saveType == XmlSaveType.SESSION) {
        extendedSession.importXML(importRoot.getPath(), is, uuidBehavior);
      } else if (saveType == XmlSaveType.WORKSPACE) {
        extendedWorkspace.importXML(importRoot.getPath(), is, uuidBehavior);
      }

    } else {
      XMLReader reader = XMLReaderFactory.createXMLReader();
      if (saveType == XmlSaveType.SESSION) {
        reader.setContentHandler(extendedSession.getImportContentHandler(importRoot.getPath(),
                                                                         uuidBehavior));

      } else if (saveType == XmlSaveType.WORKSPACE) {
        reader.setContentHandler(extendedWorkspace.getImportContentHandler(importRoot.getPath(),
                                                                           uuidBehavior));
      }
      InputSource inputSource = new InputSource(is);

      reader.parse(inputSource);

    }
  }

  protected void executeDocumentViewImportTests(BeforeExportAction firstAction,
                                                BeforeImportAction secondAction,
                                                AfterImportAction thirdAction) throws TransformerConfigurationException,
                                                                              IOException,
                                                                              RepositoryException,
                                                                              SAXException {
    XmlTestExecutor testExecutor = new XmlTestExecutor(firstAction, secondAction, thirdAction);
    executeImportTests(testExecutor, false);
  }

  protected void executeSystemViewImportTests(BeforeExportAction firstAction,
                                              BeforeImportAction secondAction,
                                              AfterImportAction thirdAction) throws TransformerConfigurationException,
                                                                            IOException,
                                                                            RepositoryException,
                                                                            SAXException {
    XmlTestExecutor testExecutor = new XmlTestExecutor(firstAction, secondAction, thirdAction);
    executeImportTests(testExecutor, true);
  }

  /**
   * Serialize content of MIX_REFERENCEABLE_NODE_NAME to byte array.
   * 
   * @param rootNode
   * @param isSystemView
   * @param isStream
   * @return
   * @throws IOException
   * @throws RepositoryException
   * @throws SAXException
   * @throws TransformerConfigurationException
   */
  protected byte[] serialize(Node exportRootNode, boolean isSystemView, boolean isStream) throws IOException,
                                                                                         RepositoryException,
                                                                                         SAXException,
                                                                                         TransformerConfigurationException {

    ExtendedSession extendedSession = (ExtendedSession) exportRootNode.getSession();

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    if (isSystemView) {

      if (isStream) {
        extendedSession.exportSystemView(exportRootNode.getPath(), outStream, false, false);
      } else {
        SAXTransformerFactory saxFact = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler handler = saxFact.newTransformerHandler();
        handler.setResult(new StreamResult(outStream));
        extendedSession.exportSystemView(exportRootNode.getPath(), handler, false, false);
      }
    } else {
      if (isStream) {
        extendedSession.exportDocumentView(exportRootNode.getPath(), outStream, false, false);
      } else {
        SAXTransformerFactory saxFact = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler handler = saxFact.newTransformerHandler();
        handler.setResult(new StreamResult(outStream));
        extendedSession.exportDocumentView(exportRootNode.getPath(), handler, false, false);
      }
    }
    outStream.close();
    return outStream.toByteArray();
  }

  protected void serialize(Node rootNode, boolean isSystemView, boolean isStream, File content) throws IOException,
                                                                                               RepositoryException,
                                                                                               SAXException,
                                                                                               TransformerConfigurationException {
    ExtendedSession extendedSession = (ExtendedSession) rootNode.getSession();

    OutputStream outStream = new FileOutputStream(content);
    if (isSystemView) {

      if (isStream) {
        extendedSession.exportSystemView(rootNode.getPath(), outStream, false, false);
      } else {
        SAXTransformerFactory saxFact = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler handler = saxFact.newTransformerHandler();
        handler.setResult(new StreamResult(outStream));
        extendedSession.exportSystemView(rootNode.getPath(), handler, false, false);
      }
    } else {
      if (isStream) {
        extendedSession.exportDocumentView(rootNode.getPath(), outStream, false, false);
      } else {
        SAXTransformerFactory saxFact = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler handler = saxFact.newTransformerHandler();
        handler.setResult(new StreamResult(outStream));
        extendedSession.exportDocumentView(rootNode.getPath(), handler, false, false);
      }
    }
    outStream.close();

  }

  private void executeImportTests(XmlTestExecutor testExecutor, boolean isSystemView) throws TransformerConfigurationException,
                                                                                     IOException,
                                                                                     RepositoryException,
                                                                                     SAXException {
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.SESSION,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.SESSION,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.SESSION,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.SESSION,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.SESSION,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.SESSION,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.SESSION,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.SESSION,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.WORKSPACE,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.WORKSPACE,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.WORKSPACE,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.WORKSPACE,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.WORKSPACE,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.WORKSPACE,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.WORKSPACE,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.WORKSPACE,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.SESSION,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.SESSION,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.SESSION,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.SESSION,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.SESSION,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.SESSION,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.SESSION,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.SESSION,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.WORKSPACE,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.WORKSPACE,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             true,
                             XmlSaveType.WORKSPACE,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.WORKSPACE,
                             true,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.WORKSPACE,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.WORKSPACE,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.WORKSPACE,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    testExecutor.executeTest(isSystemView,
                             false,
                             XmlSaveType.WORKSPACE,
                             false,
                             ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
  }

  protected abstract class AfterImportAction extends ImportExportAction {

    @Override
    public void cleanUp() throws RepositoryException {
      session.refresh(false);
      Node rootNode = session.getRootNode();
      if (rootNode.hasNodes()) {
        // clean test root
        for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
          Node node = children.nextNode();
          if (!node.getPath().startsWith("/jcr:system")) {
            // log.info("DELETing ------------- "+node.getPath());
            node.remove();
          }
        }
        session.save();

      }
    }
  }

  protected abstract class BeforeExportAction extends ImportExportAction {
    public abstract Node getExportRoot() throws RepositoryException;
  }

  protected abstract class BeforeImportAction extends ImportExportAction {
    public abstract Node getImportRoot() throws RepositoryException;
  }

  protected abstract class ImportExportAction {

    public void cleanUp() throws RepositoryException {

    };

    public void execute() throws RepositoryException {
    };
  }

  private class XmlTestExecutor {
    private final BeforeExportAction firstAction;

    /**
     * Logger.
     */
    private Log                      log = ExoLogger.getLogger("jcr.XmlTestExecutor");

    private final BeforeImportAction secondAction;

    private final AfterImportAction  thirdAction;

    public XmlTestExecutor(BeforeExportAction firstAction,
                           BeforeImportAction secondAction,
                           AfterImportAction thirdAction) {
      super();
      this.firstAction = firstAction;
      this.secondAction = secondAction;
      this.thirdAction = thirdAction;
    }

    public void executeTest(boolean isSystemViewExport,
                            boolean isExportedByStream,
                            XmlSaveType importSaveType,
                            boolean isImportedByStream,
                            int importUUIDBehavior) throws TransformerConfigurationException,
                                                   IOException,
                                                   RepositoryException,
                                                   SAXException {
      if (log.isDebugEnabled())
        log.debug("isSys=" + isSystemViewExport + "\t" + "isES=" + isExportedByStream + "\t"
            + "importST=" + importSaveType.toString() + "\t" + "isIS=" + isImportedByStream + "\t"
            + "importBehavior=" + importUUIDBehavior + "\t");
      firstAction.execute();
      firstAction.cleanUp();

      byte[] buf = serialize(firstAction.getExportRoot(), isSystemViewExport, isExportedByStream);

      secondAction.execute();
      secondAction.cleanUp();

      Node importRoot = secondAction.getImportRoot();
      Exception resultException = null;
      try {
        deserialize(importRoot,
                    importSaveType,
                    isImportedByStream,
                    importUUIDBehavior,
                    new ByteArrayInputStream(buf));
        if (importSaveType.equals(XmlSaveType.SESSION))
          importRoot.getSession().save();
      } catch (RepositoryException e) {
        resultException = e;
      } catch (SAXException e) {
        resultException = e;
      }
      if (resultException != null
          && importUUIDBehavior != ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW) {

        if (resultException instanceof RepositoryException)
          throw (RepositoryException) resultException;
        else if (resultException instanceof SAXException)
          throw (SAXException) resultException;

      }

      thirdAction.execute();
      thirdAction.cleanUp();

    }
  }
}