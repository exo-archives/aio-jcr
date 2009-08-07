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
 * @version $Id$
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

  /**
   * Find membership by specific group and check it count.
   */
  public void testFindMembershipsByGroup() {
    try {
      Group g = gHandler.findGroupById("/platform/users");
      Collection list = mHandler.findMembershipsByGroup(g);
      assertTrue("Found " + list.size() + " memberships but 5 is present", list.size() == 5);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Find membership and check it properties.
   */
  public void testFindMembershipByUserGroupAndType() {
    try {
      Membership m = mHandler.findMembershipByUserGroupAndType("marry", "/platform/users", "member");
      assertTrue("Can not find membership", m != null);
      assertTrue("Group id is not equal '/platform/users' but equal '" + m.getGroupId() + "'",
                 m.getGroupId().equals("/platform/users"));
      assertTrue("Membership type is not equal 'member' but equal '" + m.getMembershipType() + "'",
                 m.getMembershipType().equals("member"));
      assertTrue("User name is not equal 'marry' but equal '" + m.getUserName() + "'",
                 m.getUserName().equals("marry"));

      mHandler.findMembership(m.getId());
      assertTrue("Can not find membership", m != null);

      m = mHandler.findMembershipByUserGroupAndType("marry_", "/platform/users", "member");
      assertTrue("Membership is found but must be absent", m == null);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Find all membership by specific user and check it count.
   */
  public void testFindMembershipsByUser() {
    try {
      Collection list = mHandler.findMembershipsByUser("john");
      assertTrue("Found " + list.size() + " memberships but 5 is present", list.size() == 5);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Find all membership by specific user and group and check it count.
   */
  public void testFindMembershipsByUserAndGroup() {
    try {
      Collection list = mHandler.findMembershipsByUserAndGroup("john", "/platform/users");
      assertTrue("Found " + list.size() + " memberships but 1 is present", list.size() == 1);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Create new membeship and try to remove it.
   */
  public void testRemoveMembership() {
    try {
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

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }

  }

  /**
   * Create membership and than try to remove it by specific user.
   */
  public void testRemoveMembershipByUser() {
    try {
      createMembership("user", "group", "type");

      Collection list = mHandler.removeMembershipByUser("user", true);
      assertTrue("Removed " + list.size() + " memberships but 1 was presented", list.size() == 1);

      Membership m = mHandler.findMembershipByUserGroupAndType("user", "/group", "type");
      assertTrue("Membership is found but must be absent", m == null);

      gHandler.removeGroup(gHandler.findGroupById("/group"), true);
      uHandler.removeUser("user", true);
      mtHandler.removeMembershipType("type", true);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Find groups by membership and check it count.
   */
  public void testFindGroupByMembership() {
    try {
      Collection list = gHandler.findGroupByMembership("john", "manager");
      assertTrue("Found " + list.size() + " memberships but 2 is present", list.size() == 2);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Find groups and check it count.
   */
  public void testFindGroupsOfUser() throws Exception {
    try {
      Collection list = gHandler.findGroupByMembership("james", null);
      assertTrue("Found " + list.size() + " memberships but 2 is present", list.size() == 2);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Find users by group and check it count.
   */
  public void testFindUsersByGroup() throws Exception {
    try {
      PageList pList = uHandler.findUsersByGroup("/platform/users");
      Object list[] = pList.getAll().toArray();

      assertTrue("Found " + pList.getAll().size() + " users but 5 is present", pList.getAll()
                                                                                    .size() == 5);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }

  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private void createMembership(String userName, String groupName, String type) {
    try {
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

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

}
