/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableDataManager;
import org.exoplatform.services.jcr.impl.xml.ExportImportFactory;
import org.exoplatform.services.jcr.impl.xml.importing.ContentImporter;
import org.exoplatform.services.jcr.impl.xml.importing.StreamImporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 * 13.12.2010
 */
public class TestImportReferenceable extends AbstractCollisionTest {

  public void tearDown() throws Exception {

    // cleanup ws1
    Session sysSession = repository.getSystemSession("ws1");
    NodeIterator nit = sysSession.getRootNode().getNodes();
    while (nit.hasNext()) {
      nit.nextNode().remove();
      sysSession.save();
    }
    // cleanup ws2
    sysSession = repository.getSystemSession("ws2");
    nit = sysSession.getRootNode().getNodes();
    while (nit.hasNext()) {
      nit.nextNode().remove();
      sysSession.save();
    }
    super.tearDown();
  }

  /**
   * Usecase: put a referenceable node into workspace export full workspace
   * content import this data into another workspace There must not be
   * NullPointerException.
   * 
   * @throws Exception
   */
  public void testJCR1479ExportImportWorkspaceWithReference() throws Exception {
    // cleanup ws1 content
    Session sysSession = repository.getSystemSession("ws1");
    NodeIterator nit = sysSession.getRootNode().getNodes();
    while (nit.hasNext()) {
      nit.nextNode().remove();
      sysSession.save();
    }

    File tempFile = File.createTempFile("testJCR1479", "suf");
    FileOutputStream fout = null;
    FileInputStream fin = null;
    // take a ws1 session
    SessionImpl sessWS1 = (SessionImpl) repository.login(credentials, "ws1");
    try {

      // make mix:referenceable node
      Node n = sessWS1.getRootNode().addNode("testRefNode");
      n.addMixin("mix:referenceable");
      sessWS1.save();

      fout = new FileOutputStream(tempFile);

      sessWS1.exportWorkspaceSystemView(fout, false, false);
      fout.close();

      fin = new FileInputStream(tempFile);

      Map<String, Object> context = new HashMap<String, Object>();
      context.put(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS, true);

      SessionImpl sessWS2 = (SessionImpl) repository.login(credentials, "ws2");
      NodeData rootData = ((NodeData) ((NodeImpl) sessWS2.getRootNode()).getData());
      TransactionableDataManager dataManager = sessWS2.getTransientNodesManager()
                                                      .getTransactManager();
      ExportImportFactory eiFactory = new ExportImportFactory();

      StreamImporter importer = eiFactory.getWorkspaceImporter(rootData,
                                                               ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
                                                               dataManager,
                                                               dataManager,
                                                               (NodeTypeManagerImpl) repository.getNodeTypeManager(),
                                                               sessWS2.getLocationFactory(),
                                                               sessWS2.getValueFactory(),
                                                               repository.getNamespaceRegistry(),
                                                               sessWS2.getAccessManager(),
                                                               sessWS2.getUserState(),
                                                               context,
                                                               repository,
                                                               sessWS2.getWorkspace().getName());
      importer.importStream(fin);
      sessWS2.save();
      sessWS2.logout();

    } finally {
      if (fout != null) {
        fout.close();
      }
      if (fin != null) {
        fin.close();
      }
      tempFile.delete();
    }
  }

}
