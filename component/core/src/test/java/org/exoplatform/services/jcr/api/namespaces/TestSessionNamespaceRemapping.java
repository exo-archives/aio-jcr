/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.namespaces;


import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: TestSessionNamespaceRemapping.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestSessionNamespaceRemapping extends JcrAPIBaseTest {

  private NamespaceRegistry namespaceRegistry;

  public void init() throws Exception {
    workspace = session.getWorkspace();
    namespaceRegistry = workspace.getNamespaceRegistry();
  }

  public void testSetNamespacePrefix() throws Exception {
    try {
      session.setNamespacePrefix("exo2", "http://dummy.com");
      fail("exception should have been thrown as http://dummy.com is not mapped in reg");
    } catch (NamespaceException e) {
    }

    try {
      session.setNamespacePrefix("exo", "http://www.jcp.org/jcr/1.0");
      fail("exception should have been thrown");
    } catch (NamespaceException e) {
    }

    session.setNamespacePrefix("exo2", "http://www.exoplatform.com/jcr/exo/1.0");
    assertEquals("http://www.exoplatform.com/jcr/exo/1.0", session.getNamespaceURI("exo2"));
    //assertNull(session.getNamespaceURI("exo"));

    assertEquals("http://www.jcp.org/jcr/1.0", session.getNamespaceURI("jcr"));
  }

  public void testGetNamespacePrefixes() throws Exception {
    String[] protectedNamespaces = {"jcr", "nt", "mix","", "sv", "exo2"};
    session.setNamespacePrefix("exo2", "http://www.exoplatform.com/jcr/exo/1.0");
    String[] prefixes = session.getNamespacePrefixes();
    assertTrue(protectedNamespaces.length<=prefixes.length);
  }

  public void testGetNamespaceURI() throws Exception {
    session.setNamespacePrefix("exo2", "http://www.exoplatform.com/jcr/exo/1.0");
    assertEquals("http://www.exoplatform.com/jcr/exo/1.0", session.getNamespaceURI("exo2"));
    //assertNull(session.getNamespaceURI("exo"));
    assertEquals("http://www.jcp.org/jcr/1.0", session.getNamespaceURI("jcr"));
  }

  public void testGetNamespacePrefix() throws Exception {
    assertEquals("exo", session.getNamespacePrefix("http://www.exoplatform.com/jcr/exo/1.0"));
    session.setNamespacePrefix("exo2", "http://www.exoplatform.com/jcr/exo/1.0");
    assertEquals("exo2", session.getNamespacePrefix("http://www.exoplatform.com/jcr/exo/1.0"));
  }

}
