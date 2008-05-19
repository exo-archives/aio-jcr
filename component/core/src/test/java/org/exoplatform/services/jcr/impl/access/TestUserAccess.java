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

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SAS 
 * 
 * Date: 19.05.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public class TestUserAccess extends JcrImplBaseTest {


  private NodeImpl testRoot;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    testRoot = (NodeImpl) root.addNode("testUserAccess");  
    root.save();
  }

  @Override
  protected void tearDown() throws Exception {
    root.refresh(false);
    testRoot.remove();
    root.save();
    super.tearDown();
  }
  
  /**
   * Check if dedicated user has rights to a node with this user rights only. 
   * 
   * @throws Exception
   */
  public void testUser() throws Exception {
    // Mary only node, Mary membership is '*:/exo', seems it's user
    NodeImpl maryNode = (NodeImpl) testRoot.addNode("mary");
    maryNode.addMixin("exo:privilegeable");
    if (!session.getUserID().equals("mary")) {
      maryNode.setPermission("mary", PermissionType.ALL);
      maryNode.removePermission(session.getUserID());
    }
    maryNode.removePermission(SystemIdentity.ANY);
    testRoot.save();
    
    try {
      Session marySession = repository.login(new CredentialsImpl("mary", "exo".toCharArray()), session.getWorkspace().getName());
      NodeImpl myNode = (NodeImpl) marySession.getItem(maryNode.getPath());
      Node test = myNode.addNode("test");
      test.setProperty("property", "any data");
      myNode.save();
      test.remove();
      myNode.save();
    } catch(AccessControlException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Check if admin user has rights to a node with this user rights only. 
   * 
   * @throws Exception
   */
  public void testRoot() throws Exception {
    // root's only node, root membership is '*:/admin'
    NodeImpl rootNode = (NodeImpl) testRoot.addNode("root");
    rootNode.addMixin("exo:privilegeable");
    if (!session.getUserID().equals("root")) {
      rootNode.setPermission("root", PermissionType.ALL);
      rootNode.removePermission(session.getUserID());
    }
    rootNode.removePermission(SystemIdentity.ANY);
    testRoot.save();
   
    try {
      Session rootSession = repository.login(new CredentialsImpl("root", "exo".toCharArray()), session.getWorkspace().getName());
      NodeImpl myNode = (NodeImpl) rootSession.getItem(rootNode.getPath());
      Node test = myNode.addNode("test");
      test.setProperty("property", "any data");
      myNode.save();
      test.remove();
      myNode.save();
    } catch(AccessControlException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
  
  /**
   * Check if root user has rights to a node with this user rights and rights for any to a read. 
   * 
   * @throws Exception
   */
  public void testRootAndAnyRead() throws Exception {
    // root has all rights, any to read only
    NodeImpl rootNode = (NodeImpl) testRoot.addNode("root");
    rootNode.addMixin("exo:privilegeable");
    if (!session.getUserID().equals("root"))
      rootNode.setPermission("root", PermissionType.ALL);
    
    // set any to read only
    rootNode.setPermission(session.getUserID(), PermissionType.ALL); // temp all for current user
    rootNode.removePermission(SystemIdentity.ANY);
    rootNode.setPermission(SystemIdentity.ANY, new String[] {PermissionType.READ});
    rootNode.removePermission(session.getUserID()); // clean temp rights
    
    testRoot.save();
   
    try {
      Session rootSession = repository.login(new CredentialsImpl("root", "exo".toCharArray()), session.getWorkspace().getName());
      NodeImpl myNode = (NodeImpl) rootSession.getItem(rootNode.getPath());
      Node test = myNode.addNode("test");
      test.setProperty("property", "any data");
      myNode.save();
      test.remove();
      myNode.save();
    } catch(AccessControlException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
