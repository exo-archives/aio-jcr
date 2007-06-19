/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.access;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.BaseStandaloneTest;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Prerequisite: enable access control i.e.
 * <access-control>optional</access-control>
 * @author Gennady Azarenkov
 * @version $Id: TestAccess.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestAccess extends BaseStandaloneTest {

  private ExtendedNode accessTestRoot;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(!session.getRootNode().hasNode("accessTestRoot")) {
      accessTestRoot = (ExtendedNode)session.getRootNode().addNode("accessTestRoot");
      session.save();
    } else
      accessTestRoot = (ExtendedNode)session.getRootNode().getNode("accessTestRoot");
  }

  @Override
  protected void tearDown() throws Exception {
//    accessTestRoot.remove();
//    session.save();
    super.tearDown();
  }

  public String getRepositoryName() {
    return "db1";
  }
	
  /**
   * tests default permission (if node is not exo:accessControllable)
   * @throws Exception
   */
  public void testNoAccessControllable() throws Exception {
    

    AccessControlList acl = ((ExtendedNode) root).getACL();
    assertEquals(SystemIdentity.SYSTEM, acl.getOwner());
    assertEquals(PermissionType.ALL.length, acl.getPermissionEntries().size());
    assertEquals(PermissionType.ALL[0], acl.getPermissionEntries().get(0).getPermission());
  }
  
  /**
   * tests default permission for exo:owneable node
   * @throws Exception
   */
  public void testOwneable() throws Exception {
    
    ExtendedNode node = (ExtendedNode)session.getRootNode().addNode("testACNode");
    node.addMixin("exo:owneable");
    AccessControlList acl = node.getACL();
    assertEquals(session.getUserID(), acl.getOwner());
    assertEquals(PermissionType.ALL.length, acl.getPermissionEntries().size());
    assertEquals(PermissionType.ALL[0], acl.getPermissionEntries().get(0).getPermission());
    
  }

  
  /**
   * tests default permission for exo:privilegeable node
   * @throws Exception
   */
  public void testPrivilegeable() throws Exception {
    
    ExtendedNode node = (ExtendedNode)session.getRootNode().addNode("testACNode");
    node.addMixin("exo:privilegeable");
    AccessControlList acl = node.getACL();
    assertEquals(SystemIdentity.SYSTEM, acl.getOwner());
    assertEquals(PermissionType.ALL.length, acl.getPermissionEntries().size());
    assertEquals(PermissionType.ALL[0], acl.getPermissionEntries().get(0).getPermission());
  }

  
  /**
   * test permission for default exo:accessControllable node - i.e. if just
   * node.addMixin("exo:accessControllable");
   * @throws Exception
   */
  public void testDefaultAccessControllable() throws Exception {
    
    ExtendedNode node = (ExtendedNode)session.getRootNode().addNode("testACNode");
    node.addMixin("exo:accessControllable");
    
    AccessControlList acl = node.getACL();
    assertEquals(session.getUserID(), acl.getOwner());
    
    assertEquals(PermissionType.ALL.length, acl.getPermissionEntries().size());
    assertEquals(PermissionType.ALL[0], acl.getPermissionEntries().get(0).getPermission());
    
    // the same after save() and re-retrieve
    session.save();
    node = (ExtendedNode)session.getRootNode().getNode("testACNode");
    
    Session session1 = repository.login(new CredentialsImpl("exo2", "exo2".toCharArray()));
    session1.getRootNode().getNode("testACNode");
    
    
    acl = node.getACL();
    assertEquals(session.getUserID(), acl.getOwner());

    assertEquals(PermissionType.ALL.length, acl.getPermissionEntries().size());
    assertEquals(PermissionType.ALL[0], acl.getPermissionEntries().get(0).getPermission());

  }
  
  /**
   * tests if persmission are saved permanently
   * @throws Exception
   */
  public void testIfPermissionSaved() throws Exception {
    NodeImpl node = (NodeImpl)accessTestRoot.addNode("testIfPermissionSaved");
    node.addMixin("exo:accessControllable");
    session.save();
    System.out.println("NODE PERM 1 >>> "+node.getACL().dump());
    // change permission
    HashMap<String, String[]> perm = new HashMap<String, String[]>();
    perm.put("exo1", new String[] {PermissionType.ADD_NODE, PermissionType.READ});
    node.setPermissions(perm);
    
//    showPermissions("accessTestRoot/testIfPermissionSaved");
    
    System.out.println("NODE PERM 1 >>> "+node.getACL().dump());
    
    session.save();
    
    System.out.println("NODE PERM 2 >>> "+node.getACL().dump());
    
    //session.getItem(node.getPath());
//    System.out.println("TEST PERM >>> "+
//        ((NodeImpl)session.getItem(node.getPath())).getACL().dump()+" "+
//        ((PropertyImpl)session.getItem(node.getPath()+"/exo:permissions")).getValueArray()[0].getString()
//        +" "+node+" "+session.getItem(node.getPath()));
//
//    System.out.println("NODE PERM 3 >>> "+node.getACL().dump());

    // get node in new session
    NodeImpl testNode = (NodeImpl)repository.getSystemSession().getRootNode().getNode("accessTestRoot/testIfPermissionSaved");
    
    System.out.println("NODE PERM 4 >>> "+node.getACL().dump());

    System.out.println("TEST PERM >>> "+testNode.getACL().dump());

    showPermissions("accessTestRoot/testIfPermissionSaved");

    AccessControlList acl = testNode.getACL();

    // ACL should be:
    // Owner = exo
    // ADD_NODE and READ permissions for exo1
    assertEquals(session.getUserID(), acl.getOwner());
    
    assertEquals(2, acl.getPermissionEntries().size());
    List <AccessControlEntry> entries = acl.getPermissionEntries();
    assertEquals("exo1", entries.get(0).getIdentity());
    assertEquals(PermissionType.ADD_NODE, entries.get(0).getPermission());
    assertEquals(PermissionType.READ, entries.get(1).getPermission());

  }
  
  /**
   * tests child-parent permission inheritance
   * @throws Exception
   */
  public void testPermissionInheritance() throws Exception {
    NodeImpl node = (NodeImpl)accessTestRoot.addNode("testPermissionInheritance");
    node.addMixin("exo:accessControllable");

    // change permission
    HashMap<String, String[]> perm = new HashMap<String, String[]>();
    perm.put("exo1", new String[] {PermissionType.ADD_NODE, PermissionType.READ});
    node.setPermissions(perm);
    //AccessControlList acl = node.getACL();

    // add child node and test if acl is equal to parent
    NodeImpl node1 = (NodeImpl)node.addNode("node1");
    //AccessControlList acl = node1.getACL();
    assertEquals(node.getACL(), node1.getACL());
    
    // add grandchild node and test if acl is equal to grandparent
    NodeImpl node2 = (NodeImpl)node1.addNode("node1");
    assertEquals(node.getACL(), node2.getACL());
  }
  
  
  /**
   * tests session.checkPermission() method
   * @throws Exception
   */
  public void testSessionCheckPermission() throws Exception {
    NodeImpl node = (NodeImpl)accessTestRoot.addNode("testSessionCheckPermission");
    node.addMixin("exo:accessControllable");

    // change permission
    HashMap<String, String[]> perm = new HashMap<String, String[]>();
    perm.put("exo1", new String[] {PermissionType.ADD_NODE, PermissionType.READ});
    node.setPermissions(perm);
    session.save();

//    showPermissions("accessTestRoot/testSessionCheckPermission");

    // ACL is:
    // Owner = exo
    // ADD_NODE and READ permissions for exo1
    // check permission for exo1 - ADD_NODE and READ allowed 
    Session session1 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()));
    session1.checkPermission("/accessTestRoot/testSessionCheckPermission", PermissionType.READ);
    try {
      session1.checkPermission("/accessTestRoot/testSessionCheckPermission", PermissionType.SET_PROPERTY);
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
  
  /**
   * tests READ permission
   * @throws Exception
   */
  public void testRead() throws Exception {
    NodeImpl node = (NodeImpl)accessTestRoot.addNode("testRead");
    node.addMixin("exo:accessControllable");

    // change permission
    HashMap<String, String[]> perm = new HashMap<String, String[]>();
    perm.put("exo1", new String[] {PermissionType.READ});
    node.setPermissions(perm);
    node.addNode("node1");
    session.save();
    
    // ACL is:
    // Owner = exo
    // READ permissions for exo1
    
    // check permission for exo1 - READ allowed 
    Session session1 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()));
    session1.getItem("/accessTestRoot/testRead");
    session1.getItem("/accessTestRoot/testRead/jcr:primaryType");
    session1.getItem("/accessTestRoot/testRead/node1");
    // primartType, mixinTypes, permissions, owner
    assertEquals(4, ((Node)session1.getItem("/accessTestRoot/testRead")).getProperties().getSize());
    
    Node n1 = (Node)session1.getItem("/accessTestRoot");
    assertEquals(1, n1.getNodes().getSize());

    // check permission for exo2 - nothing allowed
    Session session2 = repository.login(new CredentialsImpl("exo2", "exo2".toCharArray()));
    try {
      session2.getItem("/accessTestRoot/testRead");
      fail("AccessDeniedException should have been thrown ");
    } catch (AccessDeniedException e) {
    }
    Node n2 = (Node)session2.getItem("/accessTestRoot");
    assertEquals(0, n2.getNodes().getSize());


    // ... test inheritanse
    try {
      session2.getItem("/accessTestRoot/testRead/node1");
      fail("AccessDeniedException should have been thrown ");
    } catch (AccessDeniedException e) {
    } 

  }  
  
  public void testAddNode() throws Exception {
    //ExtendedNode node = (ExtendedNode)session.getRootNode().addNode("testAddNode");
    ExtendedNode node = (ExtendedNode)accessTestRoot.addNode("testAddNode");
    node.addMixin("exo:accessControllable");

    HashMap<String, String[]> perm = new HashMap<String, String[]>();
    //perm.put("exo1", PermissionType.ALL);
    perm.put("exo1", new String[] {PermissionType.ADD_NODE, PermissionType.READ});
    perm.put("exo2", new String[] {PermissionType.READ});
    node.setPermissions(perm);
    session.save();
    
    // ACL is:
    // Owner = exo
    // READ, ADD_NODE permissions for exo1
    // READ permissions for exo2

    // [PN] 19.06.07 owner it's by whom session was open
    //assertEquals("exo",((ExtendedNode)accessTestRoot.getNode("testAddNode")).getACL().getOwner());
    assertEquals(credentials.getUserID(),((ExtendedNode)accessTestRoot.getNode("testAddNode")).getACL().getOwner());
    
    accessTestRoot.getNode("testAddNode").addNode("ownersNode");
    session.save();
    
    Session session1 = repository.login(new CredentialsImpl("exo2", "exo2".toCharArray()));

    session1.getRootNode().getNode("accessTestRoot/testAddNode").addNode("illegal");

    try {
      session1.save();
      fail("AccessDeniedException should have been thrown ");
    } catch (AccessDeniedException e) {
      session1.refresh(false);
    }
    
    session1 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()));
    session1.getRootNode().getNode("accessTestRoot/testAddNode").addNode("legal");
    session1.save();
    
    NodeImpl addNode = (NodeImpl)session1.getRootNode().getNode("accessTestRoot/testAddNode");
    addNode.setProperty("illegal", "test");
    try {
      session1.save();
      fail("AccessDeniedException should have been thrown ");
    } catch (AccessDeniedException e) {
      session1.refresh(false);
    }
    
  }

  
  public void testModifyAndReadItem() throws Exception {
    ExtendedNode node = (ExtendedNode)accessTestRoot.addNode("testModifyAndReadNode");
    node.addMixin("exo:accessControllable");

    HashMap<String, String[]> perm = new HashMap<String, String[]>();
    perm.put("exo1", PermissionType.ALL);
    perm.put("exo2", new String[] {PermissionType.READ});
    node.setPermissions(perm);
    session.save();
    
    // ACL is:
    // Owner = exo
    // ALL permissions for exo1
    // READ permissions for exo2

    
    assertEquals(credentials.getUserID(),((ExtendedNode)session.getRootNode().getNode("accessTestRoot/testModifyAndReadNode")).getACL().getOwner());
    session.getRootNode().getNode("accessTestRoot/testModifyAndReadNode").addNode("ownersNode");
    session.save();
    
    Session session1 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()));
    
    session1 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()));
    session1.getRootNode().getNode("accessTestRoot/testModifyAndReadNode").setProperty("legal", "test");
    session1.getRootNode().getNode("accessTestRoot/testModifyAndReadNode").setProperty("illegal", "test");
    session1.save();
    
    //session.getItem("/accessTestRoot/testModifyAndReadNode/legal");

    session1.getRootNode().getProperty("accessTestRoot/testModifyAndReadNode/legal").remove();
    session1.save();
    
    // exo1
    session1.getRootNode().getNode("accessTestRoot/testModifyAndReadNode").getProperty("illegal");
    assertEquals(1, session1.getRootNode().getNode("accessTestRoot/testModifyAndReadNode").getProperties("illegal").getSize());

    Session session2 = repository.login(new CredentialsImpl("exo2", "exo2".toCharArray()));
    session2.getRootNode().getNode("accessTestRoot/testModifyAndReadNode").getProperty("illegal").remove();
    
    try {
      // exo2
      session2.save();
      fail("PathNotFoundException or AccessDenied should have been thrown ");
    } catch (AccessDeniedException e) {
      session2.refresh(false);
    }
    
    session2.getRootNode().getNode("accessTestRoot/testModifyAndReadNode").setProperty("illegal2", "test");
    try {
      session2.save();
      fail("PathNotFoundException or AccessDenied should have been thrown ");
    } catch (AccessDeniedException e) {
      session2.refresh(false);
    }
  }
  

  public void testCheckAndCleanPermissions() throws Exception {
    ExtendedNode node = (ExtendedNode)accessTestRoot.addNode("testCheckAndCleanPermissions");
    node.addMixin("exo:accessControllable");

    HashMap<String, String[]> perm = new HashMap<String, String[]>();
    perm.put("exo1", PermissionType.ALL);
    perm.put("exo2", new String[]{PermissionType.READ});
    node.setPermissions(perm);
    session.save();
    
    // ACL is:
    // Owner = exo
    // ALL permissions for exo1
    // READ permissions for exo2
    
    Session session1 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()));
    Session session2 = repository.login(new CredentialsImpl("exo2", "exo2".toCharArray()));

    session1.checkPermission("/accessTestRoot/testCheckAndCleanPermissions", PermissionType.ADD_NODE);
    
    try {
      session2.checkPermission("/accessTestRoot/testCheckAndCleanPermissions", PermissionType.ADD_NODE);
      fail("AccessControlException should have been thrown ");
    } catch (AccessControlException e) {
    } 
    session2.checkPermission("/accessTestRoot/testCheckAndCleanPermissions", PermissionType.READ);

    // try to re set permissions
    ExtendedNode node2 = (ExtendedNode)session2.getRootNode()
    .getNode("accessTestRoot/testCheckAndCleanPermissions");

    try {
      // no set_property permission
      node2.setPermissions(perm);
      session2.save();
      fail("AccessControlException should have been thrown ");
    } catch (AccessControlException e) {
      session2.refresh(false);
    }
    
    // get current permissions
    AccessControlList acl = node2.getACL();
    assertEquals(credentials.getUserID(), acl.getOwner());
    assertEquals(5, acl.getPermissionEntries().size());
    
    try {
      // clean acl
      node2.clearACL();
      session2.save();
      fail("AccessControlException should have been thrown ");
    } catch (AccessControlException e) {
      session2.refresh(false);
    }
    
    ExtendedNode node1 = (ExtendedNode)session1.getRootNode()
    .getNode("accessTestRoot/testCheckAndCleanPermissions");
    node1.clearACL();
    session1.save();
    // default 
    acl = node1.getACL();
    assertEquals(credentials.getUserID(), acl.getOwner());
    assertEquals(PermissionType.ALL.length, acl.getPermissionEntries().size());
    assertEquals(PermissionType.ALL[0], acl.getPermissionEntries().get(0).getPermission());

  }
  public void testPrivilegeableAddNode()throws Exception{
    Node node = session.getRootNode().addNode("testACNode");
    node.addMixin("exo:privilegeable");
    try {
      node.addNode("privilegeable");
      session.save();
    } catch (AccessControlException e) {
      fail("AccessControlException should not have been thrown ");
    }
    try {
      session.getRootNode().getNode("testACNode/privilegeable");

    } catch (RepositoryException e) {
      fail("PathNotFoundException or AccessDenied  should not have been thrown ");
    }

  }
  public void testAddSaveAndRead() throws Exception {
    ExtendedNode node = (ExtendedNode) accessTestRoot.addNode("testSetAndRemovePermission");
    node.addMixin("exo:privilegeable");
    node.setPermission("exo1", PermissionType.ALL);
    String owner = node.getACL().getOwner();
    assertEquals(8, node.getACL().getPermissionEntries().size());
    accessTestRoot.save();
    Session session1 = repository.login(new CredentialsImpl("exo1", "exo1".toCharArray()));
    NodeImpl node1 = (NodeImpl) session1.getRootNode().getNode("accessTestRoot/testSetAndRemovePermission");
    assertEquals(8, node1.getACL().getPermissionEntries().size());
    assertEquals(node1.getACL().getOwner(),owner);
    
  }
  public void testSetAndRemovePermission() throws Exception {
    ExtendedNode node = (ExtendedNode) accessTestRoot.addNode("testSetAndRemovePermission");
    node.addMixin("exo:accessControllable");

    node.setPermission("exo1", PermissionType.ALL);
    assertEquals(PermissionType.ALL.length*2, node.getACL().getPermissionEntries().size());

    System.out.println("Access contr " + node.isNodeType("exo:accessControllable"));
    node.setPermission("exo2", new String[] { PermissionType.READ });
    assertEquals(PermissionType.ALL.length*2 + 1, node.getACL().getPermissionEntries().size());

    node.removePermission("exo1");
    assertEquals(PermissionType.ALL.length+1, node.getACL().getPermissionEntries().size());
    
  }
  
//  
// public void testAccessJcrSystem() throws Exception {
//    showPermissions("jcr:system");
//    
////    showPermissions("jcr:system/jcr:nodetypes");
//    Session session1 = repository.login(new CredentialsImpl("exo", "exo".toCharArray()));
////    try {
////      session1.checkPermission("/jcr:system/jcr:nodetypes", PermissionType.READ);
////      fail("AccessControlException should have been thrown");
////    } catch (AccessControlException e) {
////    }
////    try {
////      ((ExtendedNode)session1.getRootNode().getNode("jcr:system")).checkPermission(PermissionType.READ);
////      fail("Exception");
////    } catch (AccessControlException e) {
////    }
////
////    try {
////      session1.getRootNode().getNode("jcr:system").getNodes();
////      fail("Exception");
////    } catch (AccessDeniedException e) {
////    }
//
////    showPermissions("jcr:system");
//    session1 = repository.login(new CredentialsImpl("admin", "admin".toCharArray()));
//    session1.checkPermission("/jcr:system/jcr:nodetypes", PermissionType.READ);
//    ((ExtendedNode)session1.getRootNode().getNode("jcr:system")).checkPermission(PermissionType.READ);
//    session1.getRootNode().getNode("jcr:system").getNodes();
//  }
//  
//  
//  
  private void showPermissions(String path) throws RepositoryException {
    NodeImpl node = (NodeImpl)this.repository.getSystemSession().getRootNode().getNode(path);
    AccessControlList acl = node.getACL();
    //System.out.println("OWNER: "+acl.getOwner());
    System.out.println("DUMP: "+ acl.dump());
//    Map perms = acl.getPermissionsMap();
//    Iterator ids = perms.keySet().iterator();
//    while(ids.hasNext()) {
//      String id = (String)ids.next();
//      String[] perm = (String[])perms.get(id);
//      String permStr = "";
//      for(int i=0; i<perm.length; i++)
//        permStr+=perm[i]+",";
//      System.out.println(" "+id +" --->" +permStr);
//    }
    
  }
  
}
