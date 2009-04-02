/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.api.importing;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Random;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.util.VersionHistoryImporter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestImport extends AbstractImportTest {
  /**
   * Class logger.
   */
  private final Log    log = ExoLogger.getLogger("jcr.TestImport");

  private final Random random;

  public TestImport() {
    super();
    random = new Random();
  }

  /**
   * Test re import of versionable node. Without removing source node.
   * 
   * @throws Exception
   */
  public void testImportVersionable() throws Exception {
    // Create versionable node make some checkin and checkouts
    BeforeExportAction beforeExportAction = new BeforeExportAction(null, null) {

      private Node testRoot;

      public void execute() throws RepositoryException {
        testRoot = testRootNode.addNode("testImportVersionable");
        testSession.save();
        testRoot.addMixin("mix:versionable");
        testSession.save();

        testRoot.checkin();
        testRoot.checkout();

        testRoot.addNode("node1");
        testRoot.addNode("node2").setProperty("prop1", "a property #1");
        testRoot.save();

        testRoot.checkin();
        testRoot.checkout();

        testRoot.getNode("node1").remove();
        testRoot.save();
      }

      public Node getExportRoot() {
        return testRoot;
      }
    };
    // Before import remove versionable node
    BeforeImportAction beforeImportAction = new BeforeImportAction(null, null) {

      public void execute() throws RepositoryException {
        Node testRoot2 = testRootNode.getNode("testImportVersionable");
        testRoot2.remove();
        testRootNode.save();
      }

      public Node getImportRoot() {
        return testRootNode;
      }

    };

    // check correct work of imported node
    AfterImportAction afterImportAction = new AfterImportAction(null, null) {

      private Node testRoot2;

      public void execute() throws RepositoryException {
        testRootNode.save();
        testRoot2 = testRootNode.getNode("testImportVersionable");
        assertTrue(testRoot2.isNodeType("mix:versionable"));

        testRoot2.checkin();
        testRoot2.checkout();

        testRoot2.addNode("node3");
        testRoot2.addNode("node4").setProperty("prop1", "a property #1");
        testRoot2.save();

        testRoot2.checkin();
        testRoot2.checkout();

        testRoot2.getNode("node3").remove();
        testRoot2.save();
      }

    };

    executeSingeleThreadImportTests(1,
                                    beforeExportAction.getClass(),
                                    beforeImportAction.getClass(),
                                    afterImportAction.getClass());

    executeMultiThreadImportTests(2,
                                  5,
                                  beforeExportAction.getClass(),
                                  beforeImportAction.getClass(),
                                  afterImportAction.getClass());
  }

  /**
   * Test re import of versionable file node. With removing source node
   * 
   * @throws Exception
   */
  public void testImportVersionableFile() throws Exception {
    BeforeExportAction beforeExportAction = new BeforeExportAction(null, null) {

      @Override
      public Node getExportRoot() throws RepositoryException {
        Node testPdf = testRootNode.addNode("testPdf", "nt:file");
        Node contentTestPdfNode = testPdf.addNode("jcr:content", "nt:resource");

        byte[] buff = new byte[1024];
        random.nextBytes(buff);

        contentTestPdfNode.setProperty("jcr:data", new ByteArrayInputStream(buff));
        contentTestPdfNode.setProperty("jcr:mimeType", "application/octet-stream");
        contentTestPdfNode.setProperty("jcr:lastModified",
                                       session.getValueFactory()
                                              .createValue(Calendar.getInstance()));
        testSession.save();
        testPdf.addMixin("mix:versionable");
        testSession.save();
        testPdf.checkin();
        testPdf.checkout();
        testPdf.checkin();
        return testPdf;
      }
    };
    BeforeImportAction beforeImportAction = new BeforeImportAction(null, null) {

      @Override
      public Node getImportRoot() throws RepositoryException {
        Node importRoot = testRootNode.addNode("ImportRoot");
        importRoot.addMixin("mix:versionable");
        testRootNode.save();
        return importRoot;
      }
    };

    // check correct work of imported node
    AfterImportAction afterImportAction = new AfterImportAction(null, null) {

      private Node testRoot2;

      public void execute() throws RepositoryException {
        testRootNode.save();

        if (testRootNode.getNode("ImportRoot").hasNode("testPdf"))
          testRoot2 = testRootNode.getNode("ImportRoot").getNode("testPdf");
        else
          testRoot2 = testRootNode.getNode("testPdf");

        assertTrue(testRoot2.isNodeType("mix:versionable"));

        testRoot2.checkin();
        testRoot2.checkout();

        testRoot2.getNode("jcr:content").setProperty("jcr:lastModified",
                                                     session.getValueFactory()
                                                            .createValue(Calendar.getInstance()));
        testRoot2.save();

        testRoot2.checkin();
        testRoot2.checkout();
        testRoot2.getNode("jcr:content").setProperty("jcr:lastModified",
                                                     session.getValueFactory()
                                                            .createValue(Calendar.getInstance()));

        testRoot2.save();
      }
    };
    executeSingeleThreadImportTests(1,
                                    beforeExportAction.getClass(),
                                    beforeImportAction.getClass(),
                                    afterImportAction.getClass());

    executeMultiThreadImportTests(2,
                                  5,
                                  beforeExportAction.getClass(),
                                  beforeImportAction.getClass(),
                                  afterImportAction.getClass());

  }

  /**
   * Test re import of versionable node. Without removing source node
   * 
   * @throws Exception
   */
  public void testImportVersionableNewNode() throws Exception {

    // Create versionable node make some checkin and checkouts
    BeforeExportAction beforeExportAction = new BeforeExportAction(null, null) {

      private Node testRoot;

      public void execute() throws RepositoryException {
        testRoot = testRootNode.addNode("testImportVersionable");
        testRootNode.save();
        testRoot.addMixin("mix:versionable");
        testRootNode.save();

        testRoot.checkin();
        testRoot.checkout();

        testRoot.addNode("node1");
        testRoot.addNode("node2").setProperty("prop1", "a property #1");
        testRoot.save();

        testRoot.checkin();
        testRoot.checkout();

        testRoot.getNode("node1").remove();
        testRoot.save();
      }

      public Node getExportRoot() {
        return testRoot;
      }
    };
    // Before import remove versionable node
    BeforeImportAction beforeImportAction = new BeforeImportAction(null, null) {

      public Node getImportRoot() throws RepositoryException {
        Node importRoot = testRootNode.addNode("ImportRoot");
        importRoot.addMixin("mix:versionable");
        testRootNode.save();
        return importRoot;
      }

    };

    // check correct work of imported node
    AfterImportAction afterImportAction = new AfterImportAction(null, null) {

      private Node testRoot2;

      public void execute() throws RepositoryException {
        testRootNode.save();

        if (testRootNode.getNode("ImportRoot").hasNode("testImportVersionable"))
          testRoot2 = testRootNode.getNode("ImportRoot").getNode("testImportVersionable");
        else
          testRoot2 = testRootNode.getNode("testImportVersionable");

        assertTrue(testRoot2.isNodeType("mix:versionable"));

        testRoot2.checkin();
        testRoot2.checkout();

        testRoot2.addNode("node3");
        testRoot2.addNode("node4").setProperty("prop1", "a property #1");
        testRoot2.save();

        testRoot2.checkin();
        testRoot2.checkout();

        testRoot2.getNode("node3").remove();
        testRoot2.save();
      }

    };

    executeSingeleThreadImportTests(1,
                                    beforeExportAction.getClass(),
                                    beforeImportAction.getClass(),
                                    afterImportAction.getClass());

    executeMultiThreadImportTests(2,
                                  5,
                                  beforeExportAction.getClass(),
                                  beforeImportAction.getClass(),
                                  afterImportAction.getClass());

  }

  /**
   * Test import of the history of versuions.
   * 
   * @throws Exception
   */
  public void testImportVersionHistory() throws Exception {

    Node testRootNode = root.addNode("testRoot");
    Node testRoot = testRootNode.addNode("testImportVersionable");
    session.save();
    testRoot.addMixin("mix:versionable");
    testRootNode.save();

    testRoot.checkin();
    testRoot.checkout();

    testRoot.addNode("node1");
    testRoot.addNode("node2").setProperty("prop1", "a property #1");
    testRoot.save();

    testRoot.checkin();
    testRoot.checkout();

    testRoot.getNode("node1").remove();
    testRoot.save();

    assertEquals(3, testRoot.getVersionHistory().getAllVersions().getSize());

    String baseVersionUuid = testRoot.getBaseVersion().getUUID();

    Value[] values = testRoot.getProperty("jcr:predecessors").getValues();
    String[] predecessors = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      predecessors[i] = values[i].getString();
    }
    String versionHistory = testRoot.getVersionHistory().getUUID();

    File versionableNodeContent = File.createTempFile("versionableNodeContent", "tmp");
    File vhNodeContent = File.createTempFile("vhNodeContent", "tmp");
    versionableNodeContent.deleteOnExit();
    vhNodeContent.deleteOnExit();
    serialize(testRoot, false, true, versionableNodeContent);
    serialize(testRoot.getVersionHistory(), false, true, vhNodeContent);

    testRoot.remove();
    session.save();

    deserialize(testRootNode,
                XmlSaveType.SESSION,
                true,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING,
                new BufferedInputStream(new FileInputStream(versionableNodeContent)));
    session.save();
    testRoot = testRootNode.getNode("testImportVersionable");
    assertTrue(testRoot.isNodeType("mix:versionable"));

    assertEquals(1, testRoot.getVersionHistory().getAllVersions().getSize());

    VersionHistoryImporter historyImporter = new VersionHistoryImporter((NodeImpl) testRoot,
                                                                        new BufferedInputStream(new FileInputStream(vhNodeContent)),
                                                                        baseVersionUuid,
                                                                        predecessors,
                                                                        versionHistory);
    historyImporter.doImport();
    session.save();

    assertEquals(3, testRoot.getVersionHistory().getAllVersions().getSize());

    testRoot.addNode("node3");
    testRoot.addNode("node4").setProperty("prop1", "a property #1");
    testRoot.save();

    testRoot.checkin();
    testRoot.checkout();
    assertEquals(4, testRoot.getVersionHistory().getAllVersions().getSize());

  }

  public void testAclImpormt() throws Exception {
    AccessManager accessManager = ((SessionImpl) root.getSession()).getAccessManager();

    NodeImpl testRoot = (NodeImpl) root.addNode("TestRoot");

    testRoot.addMixin("exo:owneable");
    testRoot.addMixin("exo:privilegeable");
    session.save();
    assertTrue(accessManager.hasPermission(testRoot.getACL(),
                                           PermissionType.SET_PROPERTY,
                                           new Identity("exo")));

    testRoot.setPermission(testRoot.getSession().getUserID(), PermissionType.ALL);
    testRoot.setPermission("exo", new String[] { PermissionType.SET_PROPERTY });
    testRoot.removePermission(SystemIdentity.ANY);
    session.save();
    assertTrue(accessManager.hasPermission(testRoot.getACL(),
                                           PermissionType.SET_PROPERTY,
                                           new Identity("exo")));
    assertFalse(accessManager.hasPermission(testRoot.getACL(),
                                            PermissionType.READ,
                                            new Identity("exo")));

    File tmp = File.createTempFile("testAclImpormt", "tmp");
    tmp.deleteOnExit();
    serialize(testRoot, false, true, tmp);
    testRoot.remove();
    session.save();

    NodeImpl importRoot = (NodeImpl) root.addNode("ImportRoot");

    deserialize(importRoot,
                XmlSaveType.SESSION,
                true,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING,
                new BufferedInputStream(new FileInputStream(tmp)));
    session.save();
    Node n1 = importRoot.getNode("TestRoot");
    assertTrue(accessManager.hasPermission(((NodeImpl) n1).getACL(),
                                           PermissionType.SET_PROPERTY,
                                           new Identity("exo")));
    assertFalse(accessManager.hasPermission(((NodeImpl) n1).getACL(),
                                            PermissionType.READ,
                                            new Identity("exo")));

  }
}
