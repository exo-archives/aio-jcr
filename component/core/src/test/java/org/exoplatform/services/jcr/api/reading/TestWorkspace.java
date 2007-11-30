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
package org.exoplatform.services.jcr.api.reading;


import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SAS.
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
