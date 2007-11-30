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
package org.exoplatform.services.jcr.api.namespaces;


import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: TestNamespaceRegistry.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestNamespaceRegistry extends JcrAPIBaseTest{

  protected NamespaceRegistry namespaceRegistry;

  public void initRepository() throws RepositoryException {
    workspace = session.getWorkspace();
    namespaceRegistry = workspace.getNamespaceRegistry();
    try {
      namespaceRegistry.getURI("newMapping");
    } catch (NamespaceException e) {
      // not found
      namespaceRegistry.registerNamespace("newMapping", "http://dumb.uri/jcr");
    }
  }

  public void testGetPrefixes() throws RepositoryException {
    //namespaceRegistry.registerNamespace("newMapping", "http://dumb.uri/jcr");
    String[] namespaces = {"jcr", "nt", "mix","", "sv", "exo", "newMapping"};

    String[] prefixes = namespaceRegistry.getPrefixes();

    for (int i = 0; i < namespaces.length; i++) {
    	
      String namespace = namespaces[i];
      assertTrue("not found "+namespace, ArrayUtils.contains(prefixes, namespace));
    }
    assertTrue(prefixes.length>=7);

  }

  public void testGetURIs() throws RepositoryException {
    //namespaceRegistry.registerNamespace("newMapping", "http://dumb.uri/jcr");
    String[] namespacesURIs = {"http://www.jcp.org/jcr/1.0", "http://www.jcp.org/jcr/nt/1.0",
                               "http://www.jcp.org/jcr/mix/1.0", "",
                               "http://www.jcp.org/jcr/sv/1.0", "http://www.exoplatform.com/jcr/exo/1.0",
                               "http://dumb.uri/jcr"};

    String[] uris = namespaceRegistry.getURIs();
    for (int i = 0; i < namespacesURIs.length; i++) {
      String namespacesURI = namespacesURIs[i];
      assertTrue("not found "+namespacesURI, ArrayUtils.contains(uris, namespacesURI));
    }
  }

  public void testGetURI() throws RepositoryException {
    //namespaceRegistry.registerNamespace("newMapping", "http://dumb.uri/jcr");

    assertNotNull(namespaceRegistry.getURI("mix"));
    assertNotNull(namespaceRegistry.getURI("newMapping"));
  }

  public void testGetPrefix() throws RepositoryException {
    //namespaceRegistry.registerNamespace("newMapping", "http://dumb.uri/jcr");

    assertNotNull(namespaceRegistry.getPrefix("http://www.jcp.org/jcr/mix/1.0"));
    assertEquals("mix", namespaceRegistry.getPrefix("http://www.jcp.org/jcr/mix/1.0"));
    assertNotNull(namespaceRegistry.getPrefix("http://dumb.uri/jcr"));

    try {
      namespaceRegistry.getPrefix("http://dumb.uri/jcr2");
      fail("exception should have been thrown");
    } catch (RepositoryException e) {
    }

  }


  /////////////////// LEVEL 2

  public void testBuiltInNamespace() throws RepositoryException {
    try {
      namespaceRegistry.registerNamespace("jcr", null);
      fail("exception should have been thrown");
    } catch (NamespaceException e) {
    }
    try {
      namespaceRegistry.registerNamespace("nt", null);
      fail("exception should have been thrown");
    } catch (NamespaceException e) {
    }
    try {
      namespaceRegistry.registerNamespace("mix", null);
      fail("exception should have been thrown");
    } catch (NamespaceException e) {
    }

    try {
      namespaceRegistry.registerNamespace("sv", null);
      fail("exception should have been thrown");
    } catch (NamespaceException e) {
    }

    try {
      namespaceRegistry.registerNamespace("jcr", "http://dumb.uri/jcr");
      fail("exception should have been thrown");
    } catch (NamespaceException e) {
    }

    try {
      namespaceRegistry.registerNamespace("xml-started", "http://dumb.uri/jcr");
      fail("exception should have been thrown");
    } catch (NamespaceException e) {
    }

    try {
      namespaceRegistry.unregisterNamespace("jcr");
      fail("exception should have been thrown");
    } catch (NamespaceException e) {
    }
  }
  public void testRegisterNamespace() throws RepositoryException {
    //namespaceRegistry.registerNamespace("newMapping", "http://dumb.uri/jcr");
    assertNotNull(namespaceRegistry.getURI("newMapping"));
    assertEquals("http://dumb.uri/jcr", namespaceRegistry.getURI("newMapping"));

    NodeImpl n = (NodeImpl)root.addNode("newMapping:test", "nt:unstructured");
    System.out.println("Node before save"+n);
    root.save();
    System.out.println("Node after save"+n);
    n = (NodeImpl)root.getNode("newMapping:test");
    n.remove();
    System.out.println("Node after remove"+n);
    root.save();

    // [PN] Unregisteration of node types its not supported in eXo JCR.
    // (see http://jira.exoplatform.org/browse/JCR-43)
//    namespaceRegistry.unregisterNamespace("newMapping");
//    try {
//        root.addNode("newMapping:test1", "nt:unstructured");
//        root.save();
//        fail("exception should have been thrown");
//    } catch (RepositoryException e) {
//    }
//    
//    
//    try {
//      assertNull(namespaceRegistry.getURI("newMapping"));
//      fail("exception should have been thrown");
//    } catch (NamespaceException e) {
//    }
    
  }

  public void testReRegiterNamespace() throws RepositoryException {

    // (see http://jira.exoplatform.org/browse/JCR-43)
    
    //namespaceRegistry.registerNamespace("newMapping", "http://dumb.uri/jcr");
//    namespaceRegistry.registerNamespace("newMapping2", "http://dumb.uri/jcr");
//    try {
//      assertNull(namespaceRegistry.getURI("newMapping"));
//      fail("exception should have been thrown");
//    } catch (NamespaceException e) {
//    }
//    assertNotNull(namespaceRegistry.getURI("newMapping2"));
//    assertEquals("http://dumb.uri/jcr", namespaceRegistry.getURI("newMapping2"));
  }

}
