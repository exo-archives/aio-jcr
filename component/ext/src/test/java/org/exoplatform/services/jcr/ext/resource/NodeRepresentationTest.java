/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.services.jcr.ext.resource;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Collection;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.resource.representation.NtFileNodeRepresentation;
import org.exoplatform.services.jcr.ext.resource.representation.NtFileNodeRepresentationFactory;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class NodeRepresentationTest extends BaseStandaloneTest {

  private NodeRepresentationService       nodeRepresentationService;

  private NtFileNodeRepresentationFactory ntFileNodeRepresentationFactory;

  private Node                            testRoot;

  public void setUp() throws Exception {
    super.setUp();
    if (nodeRepresentationService == null) {
      nodeRepresentationService = (NodeRepresentationService) container.getComponentInstanceOfType(NodeRepresentationService.class);
      assertNotNull(nodeRepresentationService);
      ntFileNodeRepresentationFactory = (NtFileNodeRepresentationFactory) container.getComponentInstanceOfType(NtFileNodeRepresentationFactory.class);
      assertNotNull(ntFileNodeRepresentationFactory);

      testRoot = root.addNode("NodeRepresentationTest", "nt:unstructured");

    }
  }

  @Override
  protected void tearDown() throws Exception {
    testRoot.remove();
    session.save();
    super.tearDown();
  }

  public void testServiceInitialization() throws Exception {
    Collection<String> nts = nodeRepresentationService.getNodeTypes();
    assertTrue(nts.size() > 0);
    assertTrue(nts.contains("nt:file"));
    assertTrue(nts.contains("nt:resource"));
  }

  public void testNtFileNodeRepresentation() throws Exception {
    String data = "Test JCR";

    Node file = testRoot.addNode("file", "nt:file");
    Node d = file.addNode("jcr:content", "nt:resource");
    d.setProperty("jcr:mimeType", "text/plain");
    d.setProperty("jcr:lastModified", Calendar.getInstance());
    d.setProperty("jcr:data", new ByteArrayInputStream(data.getBytes()));
    session.save();

    NodeRepresentation nodeRepresentation = nodeRepresentationService.getNodeRepresentation(file,
                                                                                            "text/plain");

    assertNotNull(nodeRepresentation);

    assertTrue(nodeRepresentation instanceof NtFileNodeRepresentation);

    // for(String n : nodeRepresentation.getPropertyNames()) {
    // System.out.println(">>>>>>>>>>>>>>>>>>> "+n+" "+nodeRepresentation);
    // }

    assertEquals(3, nodeRepresentation.getPropertyNames().size());

    compareStream(nodeRepresentation.getInputStream(), new ByteArrayInputStream(data.getBytes()));

  }

}
