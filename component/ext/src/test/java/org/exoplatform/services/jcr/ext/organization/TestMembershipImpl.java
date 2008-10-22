/**
 * 
 */
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
package org.exoplatform.services.jcr.ext.organization;

import java.util.Collection;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestMembershipImpl.java 111 2008-11-11 11:11:11Z $
 */
public class TestMembershipImpl extends BaseStandaloneTest {
  private GroupHandler               gHandler;

  private MembershipHandler          mHandler;

  private UserHandler                uHandler;

  private MembershipTypeHandler      mtHandler;

  private JCROrganizationServiceImpl organizationService;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    organizationService = (JCROrganizationServiceImpl) container.getComponentInstanceOfType(JCROrganizationServiceImpl.class);

    gHandler = new GroupHandlerImpl(organizationService);
    uHandler = new UserHandlerImpl(organizationService);
    mHandler = new MembershipHandlerImpl(organizationService);
    mtHandler = new MembershipTypeHandlerImpl(organizationService);
  }

  public void testFindMembershipsByGroup() throws Exception {
    Group g = gHandler.findGroupById("/platform/users");
    Collection list = mHandler.findMembershipsByGroup(g);
    assertTrue("Found " + list.size() + " memberships but 5 is present", list.size() == 5);
  }

  public void testFindMembershipByUserGroupAndType() throws Exception {
    Membership m = mHandler.findMembershipByUserGroupAndType("marry", "/platform/users", "member");
    assertTrue("Can not find membership", m != null);
    assertTrue("Group id is not equal '/platform/users' but equal '" + m.getGroupId() + "'",
               m.getGroupId().equals("/platform/users"));
    assertTrue("Membership type is not equal 'member' but equal '" + m.getMembershipType() + "'",
               m.getMembershipType().equals("member"));
    assertTrue("User name is not equal 'marry' but equal '" + m.getUserName() + "'",
               m.getUserName().equals("marry"));

    m = mHandler.findMembershipByUserGroupAndType("marry_", "/platform/users", "member");
    assertTrue("Membership is found but must be absent", m == null);
  }

  public void testFindMembershipsByUser() throws Exception {
    Collection list = mHandler.findMembershipsByUser("john");
    assertTrue("Found " + list.size() + " memberships but 3 is present", list.size() == 3);
  }

  public void testFindMembershipsByUserAndGroup() throws Exception {
    Collection list = mHandler.findMembershipsByUserAndGroup("john", "/platform/users");
    assertTrue("Found " + list.size() + " memberships but 1 is present", list.size() == 1);
  }

  public void testRemoveMembership() throws Exception {
    createMembership("user", "group", "type");

    Membership m = mHandler.findMembershipByUserGroupAndType("user", "/group", "type");
    assertTrue("Can not find membership", m != null);

    m = mHandler.removeMembership(m.getId(), true);
    assertTrue("Group id is not equal '/group' but equal '" + m.getGroupId() + "'",
               m.getGroupId().equals("/group"));
    assertTrue("Membership type is not equal 'type' but equal '" + m.getMembershipType() + "'",
               m.getMembershipType().equals("type"));
    assertTrue("User name is not equal 'user' but equal '" + m.getUserName() + "'",
               m.getUserName().equals("user"));

    m = mHandler.findMembershipByUserGroupAndType("user", "/group", "type");
    assertTrue("Membership is found but must be absent", m == null);

    gHandler.removeGroup(gHandler.findGroupById("/group"), true);
    uHandler.removeUser("user", true);
    mtHandler.removeMembershipType("type", true);
  }

  public void testRemoveMembershipByUser() throws Exception {
    createMembership("user", "group", "type");

    Collection list = mHandler.removeMembershipByUser("user", true);
    assertTrue("Removed " + list.size() + " memberships but 1 was presented", list.size() == 1);

    Membership m = mHandler.findMembershipByUserGroupAndType("user", "/group", "type");
    assertTrue("Membership is found but must be absent", m == null);

    gHandler.removeGroup(gHandler.findGroupById("/group"), true);
    uHandler.removeUser("user", true);
    mtHandler.removeMembershipType("type", true);
  }

  public void testFindGroupByMembership() throws Exception {
    Collection list = gHandler.findGroupByMembership("john", "manager");
    assertTrue("Found " + list.size() + " memberships but 1 is present", list.size() == 1);

    Object[] groups = list.toArray();
    Group g = (Group) groups[0];
    assertTrue("Group is not found", g != null);
    assertTrue("Group description is not equal 'the /platform/users group' but equal '"
        + g.getDescription() + "'", g.getDescription().equals("the /platform/users group"));
    assertTrue("Group name is not equal 'users' but equal '" + g.getGroupName() + "'",
               g.getGroupName().equals("users"));
    assertTrue("Group groupId is not equal '/platform/users' but equal '" + g.getId() + "'",
               g.getId().equals("/platform/users"));
    assertTrue("Group label is not equal 'Users' but equal '" + g.getLabel() + "'",
               g.getLabel().equals("Users"));
    assertTrue("Group parentId is not equal '/platform' but equal '" + g.getParentId() + "'",
               g.getParentId().equals("/platform"));
  }

  public void testFindGroupsOfUser() throws Exception {
    Collection list = gHandler.findGroupByMembership("james", null);
    assertTrue("Found " + list.size() + " memberships but 2 is present", list.size() == 2);
  }

  public void testFindUsersByGroup() throws Exception {
    PageList pList = uHandler.findUsersByGroup("/platform/users");
    Object list[] = pList.getAll().toArray();

    assertTrue("Found " + pList.getAll().size() + " users but 5 is present",
               pList.getAll().size() == 5);
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private void createMembership(String userName, String groupName, String type) throws Exception {
    // create users
    User u = uHandler.createUserInstance(userName);
    u.setEmail("email");
    u.setFirstName("first");
    u.setLastName("last");
    u.setPassword("pwd");
    uHandler.createUser(u, true);

    // create groups
    Group g = gHandler.createGroupInstance();
    g.setGroupName(groupName);
    g.setLabel("label");
    g.setDescription("desc");
    gHandler.createGroup(g, true);

    // Create membership types
    MembershipType mt = mtHandler.createMembershipTypeInstance();
    mt.setName(type);
    mt.setDescription("desc");
    mtHandler.createMembershipType(mt, true);

    // Create membership
    Membership m = new MembershipImpl(null, userName, "/" + groupName, type);
    mHandler.createMembership(m, true);
  }

}
