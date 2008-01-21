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
package org.exoplatform.services.jcr.impl.access;

import java.security.AccessControlException;

import javax.jcr.Session;

import org.exoplatform.services.jcr.BaseStandaloneTest;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SAS.<br/>
 * Prerequisite: enable access control i.e.
 * <access-control>optional</access-control>
 * @author Gennady Azarenkov
 * @version $Id:TestAccessExoPrivilegeable.java 12535 2007-02-02 15:39:26Z peterit $
 */

public class TestAccessExoPrivilegeable extends BaseStandaloneTest {

  private ExtendedNode accessTestRoot;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    accessTestRoot = (ExtendedNode)session.getRootNode().addNode("accessTestRoot");
    session.save();
  }
  public String getRepositoryName() {
    return "db1";
  }
  
  /**
   * tests session.checkPermission() method
   * @throws Exception
   */
  public void testSessionCheckPermission() throws Exception {
    NodeImpl node = null;
    node = (NodeImpl) accessTestRoot.addNode("testSessionCheckPermission");
    //node.addMixin("exo:accessControllable");
    
    node.addMixin("exo:owneable");
    node.addMixin("exo:privilegeable");
    
    //good style of set permission 
    //1. set for me
    //2. set for others
    //3. remove for any
    node.setPermission("exo", PermissionType.ALL);
    node.setPermission("exo1", new String[] { PermissionType.READ });
    node.removePermission("any");
    session.save();

    Session session1 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()));
    session1.checkPermission("/accessTestRoot/testSessionCheckPermission", PermissionType.READ);
    try {
      session1.checkPermission("/accessTestRoot/testSessionCheckPermission",
          PermissionType.SET_PROPERTY);
      fail("AccessControlException should have been thrown ");
    } catch (AccessControlException e) {
    }

    // check permission for exo2 - nothing allowed
    Session session2 = repository.login(new CredentialsImpl("exo2", "exo2".toCharArray()));
    try {
      session2.checkPermission("/accessTestRoot/testSessionCheckPermission", PermissionType.READ);
      fail("AccessControlException should have been thrown ");
    } catch (AccessControlException e) {
    }
  }
}
