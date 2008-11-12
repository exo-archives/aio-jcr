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
package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 12, 2008 2:26:36 PM
 */
public class TestLock extends BaseUsecasesTest {
  
  public void testLock() throws Exception {
    System.out.println("\n\n----------Test Lock");
    Session sysSession = repository.getSystemSession(WORKSPACE);
    Node rootNode = sysSession.getRootNode();
    rootNode.addNode("bbb");
    rootNode.save();
    Node newNode = (Node)sysSession.getItem("/bbb");
    if (newNode.canAddMixin("exo:privilegeable")) {
      newNode.addMixin("exo:privilegeable");
    }
    ((ExtendedNode)newNode).setPermission("admin", PermissionType.ALL);
    newNode.save();
    sysSession.logout();
    Session session1 = repository.login(credentials, WORKSPACE);
    Node newNode1 = ((Node)session1.getItem("/bbb")).addNode("ccc");
    if (newNode1.canAddMixin("mix:lockable")) {
      newNode1.addMixin("mix:lockable");
    }
    session1.save();
    //Lock with isDeep = false and isSessionScoped = false
    newNode1.lock(false, false);
    session1.save();
    session1.logout();
    
    Session session2 = repository.login(credentials, WORKSPACE);
    Node newNode2 = (Node)session2.getItem("/bbb/ccc");
    //Lockower of newNode2 is admin
    if(newNode2.isLocked()) {
      assertEquals("admin", newNode2.getLock().getLockOwner());
      newNode2.unlock();
    }
    session2.save();
    session2.logout();
  }

}
