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

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.log.ExoLogger;

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
    BeforeExportAction beforeExportAction = new BeforeExportAction() {

      private Node testRoot;

      public void execute() throws RepositoryException {
        testRoot = root.addNode("testImportVersionable");
        root.save();
        testRoot.addMixin("mix:versionable");
        root.save();

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
    BeforeImportAction beforeImportAction = new BeforeImportAction() {

      public void execute() throws RepositoryException {
        Node testRoot2 = root.getNode("testImportVersionable");
        testRoot2.remove();
        root.save();
      }

      public Node getImportRoot() {
        return root;
      }

    };

    // check correct work of imported node
    AfterImportAction afterImportAction = new AfterImportAction() {

      private Node testRoot2;

      public void execute() throws RepositoryException {
        root.save();
        testRoot2 = root.getNode("testImportVersionable");
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

    executeDocumentViewImportTests(beforeExportAction, beforeImportAction, afterImportAction);
    executeSystemViewImportTests(beforeExportAction, beforeImportAction, afterImportAction);
  }
  /**
   * Test re import of versionable file node. With removing source node
   * 
   * @throws Exception
   */
  public void testImportVersionableFile() throws Exception {
    BeforeExportAction beforeExportAction = new BeforeExportAction() {

      @Override
      public Node getExportRoot() throws RepositoryException {
        Node testPdf = root.addNode("testPdf", "nt:file");
        Node contentTestPdfNode = testPdf.addNode("jcr:content", "nt:resource");

        byte[] buff = new byte[1024];
        random.nextBytes(buff);

        contentTestPdfNode.setProperty("jcr:data", new ByteArrayInputStream(buff));
        contentTestPdfNode.setProperty("jcr:mimeType", "application/octet-stream");
        contentTestPdfNode.setProperty("jcr:lastModified",
                                       session.getValueFactory()
                                              .createValue(Calendar.getInstance()));
        session.save();
        testPdf.addMixin("mix:versionable");
        session.save();
        testPdf.checkin();
        testPdf.checkout();
        testPdf.checkin();
        return testPdf;
      }
    };
    BeforeImportAction beforeImportAction = new BeforeImportAction() {

      @Override
      public Node getImportRoot() throws RepositoryException {
        Node importRoot = root.addNode("ImportRoot");
        importRoot.addMixin("mix:versionable");
        root.save();
        return importRoot;
      }
    };

    // check correct work of imported node
    AfterImportAction afterImportAction = new AfterImportAction() {

      private Node testRoot2;

      public void execute() throws RepositoryException {
        root.save();

        if (root.getNode("ImportRoot").hasNode("testPdf"))
          testRoot2 = root.getNode("ImportRoot").getNode("testPdf");
        else
          testRoot2 = root.getNode("testPdf");

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
    executeDocumentViewImportTests(beforeExportAction, beforeImportAction, afterImportAction);
    executeSystemViewImportTests(beforeExportAction, beforeImportAction, afterImportAction);
  }

  /**
   * Test re import of versionable node. Without removing source node
   * 
   * @throws Exception
   */
  public void testImportVersionableNewNode() throws Exception {

    // Create versionable node make some checkin and checkouts
    BeforeExportAction beforeExportAction = new BeforeExportAction() {

      private Node testRoot;

      public void execute() throws RepositoryException {
        testRoot = root.addNode("testImportVersionable");
        root.save();
        testRoot.addMixin("mix:versionable");
        root.save();

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
    BeforeImportAction beforeImportAction = new BeforeImportAction() {

      public Node getImportRoot() throws RepositoryException {
        Node importRoot = root.addNode("ImportRoot");
        importRoot.addMixin("mix:versionable");
        root.save();
        return importRoot;
      }

    };

    // check correct work of imported node
    AfterImportAction afterImportAction = new AfterImportAction() {

      private Node testRoot2;

      public void execute() throws RepositoryException {
        root.save();

        if (root.getNode("ImportRoot").hasNode("testImportVersionable"))
          testRoot2 = root.getNode("ImportRoot").getNode("testImportVersionable");
        else
          testRoot2 = root.getNode("testImportVersionable");

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

    executeDocumentViewImportTests(beforeExportAction, beforeImportAction, afterImportAction);
    executeSystemViewImportTests(beforeExportAction, beforeImportAction, afterImportAction);
  }

}
