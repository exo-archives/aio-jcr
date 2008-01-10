/*
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

package org.exoplatform.services.jcr.usecases.access;

import java.security.AccessControlException;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Jan 10, 2008  
 */
public class TestAccess extends BaseUsecasesTest {

  public void testAccess() throws Exception{
    //Set only add_node permission on root for exo    
    ((ExtendedNode)root).setPermission("exo",new String[]{PermissionType.ADD_NODE});
    root.save();            
    assertEquals(((ExtendedNode)root).getACL().getPermissions("exo").size(),1);
    assertEquals(((ExtendedNode)root).getACL().getPermissions("exo").get(0),PermissionType.ADD_NODE);
    SimpleCredentials exoCredentical = new SimpleCredentials("exo","exo".toCharArray());
    Session userSession = repositoryService.getDefaultRepository().login(exoCredentical,WORKSPACE);    
    ExtendedNode rootNode = (ExtendedNode)userSession.getRootNode();
    Node testNode = userSession.getRootNode().addNode("testNode");
    testNode.addMixin("exo:owneable");
    userSession.save();        
    //exo still only have add_node permission on root    
    assertEquals(rootNode.getACL().getPermissions("exo").size(),1);
    assertEquals(rootNode.getACL().getPermissions("exo").get(0),PermissionType.ADD_NODE);
    try{
      rootNode.checkPermission(PermissionType.ADD_NODE);
      //why exo has other permissions here???
      rootNode.checkPermission(PermissionType.REMOVE);
      fail();
      rootNode.checkPermission(PermissionType.CHANGE_PERMISSION);
      fail();
      rootNode.checkPermission(PermissionType.SET_PROPERTY);
      fail();
    }catch (AccessControlException e) {
    }
    //exo is owner of testNode so he will have full permissions on it
    AccessControlList acl = ((ExtendedNode)testNode).getACL();
    String owner = testNode.getProperty("exo:owner").getString();    
    assertEquals("exo",owner);    
    assertEquals(acl.getOwner(),owner);
    try {
      ((ExtendedNode)testNode).checkPermission(PermissionType.ADD_NODE);
      ((ExtendedNode)testNode).checkPermission(PermissionType.CHANGE_PERMISSION);      
      ((ExtendedNode)testNode).checkPermission(PermissionType.SET_PROPERTY);
      ((ExtendedNode)testNode).checkPermission(PermissionType.REMOVE);
    } catch (AccessControlException e) {      
      fail();
    }    
  }
}
