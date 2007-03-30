/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.reading;


import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestWorkspace.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestWorkspace extends JcrAPIBaseTest {

  public void testGetSession() {
    assertEquals(session, workspace.getSession());
  }

  public void testGetName() {
    assertEquals("ws", workspace.getName());
  }

  public void testGetQueryManager() throws Exception {
    assertNotNull(workspace.getQueryManager());
  }

  public void testGetNamespaceRegistry() throws Exception {
    assertNotNull(workspace.getNamespaceRegistry());
  }

  public void testGetNodeTypeManager() throws Exception {
    assertNotNull(workspace.getNodeTypeManager());
  }

  public void testGetAccessibleWorkspaceNames() throws Exception {
    log.debug(workspace.getAccessibleWorkspaceNames()[0]);
    assertNotNull(workspace.getAccessibleWorkspaceNames());
  }


}
