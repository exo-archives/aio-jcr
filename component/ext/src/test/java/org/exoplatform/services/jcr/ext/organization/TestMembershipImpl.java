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

import java.util.Calendar;
import java.util.Collection;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.jcr.RepositoryService;
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

  private Calendar                   calendar;

  private JCROrganizationServiceImpl organizationService;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    organizationService = new JCROrganizationServiceImpl(service, "ws", "/exo:organization");
    organizationService.start();

    gHandler = new GroupHandlerImpl(organizationService);
    uHandler = new UserHandlerImpl(organizationService);
    mHandler = new MembershipHandlerImpl(organizationService);
    mtHandler = new MembershipTypeHandlerImpl(organizationService);

    calendar = Calendar.getInstance();
    calendar.set(2008, 1, 1);

    createRecords("user1", "group1", "type1");
  }

  public void testFindMembership() throws Exception {
    Membership m = mHandler.findMembershipByUserGroupAndType("user1", "/group1", "type1");

    m = mHandler.findMembership(m.getId());
    assertTrue("Can not find membership", m != null);
  }

  public void testFindMembershipsByGroup() throws Exception {
    Group g = gHandler.findGroupById("/group1");
    Collection list = mHandler.findMembershipsByGroup(g);
    assertTrue("Found " + list.size() + " memberships but 1 is present", list.size() == 1);
  }

  public void testFindMembershipByUserGroupAndType() throws Exception {
    Membership m = mHandler.findMembershipByUserGroupAndType("user1", "/group1", "type1");
    assertTrue("Can not find membership", m != null);
    assertTrue("Group id is not equal '/group1' but equal '" + m.getGroupId() + "'",
               m.getGroupId().equals("/group1"));
    assertTrue("Membership type is not equal 'type1' but equal '" + m.getMembershipType() + "'",
               m.getMembershipType().equals("type1"));
    assertTrue("User name is not equal 'user1' but equal '" + m.getUserName() + "'",
               m.getUserName().equals("user1"));
  }

  public void testFindMembershipsByUser() throws Exception {
    Collection list = mHandler.findMembershipsByUser("user1");
    assertTrue("Found " + list.size() + " memberships but 1 is present", list.size() == 1);
  }

  public void testFindMembershipsByUserAndGroup() throws Exception {
    Collection list = mHandler.findMembershipsByUserAndGroup("user1", "/group1");
    assertTrue("Found " + list.size() + " memberships but 1 is present", list.size() == 1);
  }

  public void testRemoveMembership() throws Exception {
    createRecords("user2", "group2", "type2");

    Membership m = mHandler.findMembershipByUserGroupAndType("user2", "/group2", "type2");
    assertTrue("Can not find membership", m != null);

    m = mHandler.removeMembership(m.getId(), true);
    assertTrue("Group id is not equal '/group2' but equal '" + m.getGroupId() + "'",
               m.getGroupId().equals("/group2"));
    assertTrue("Membership type is not equal 'type2' but equal '" + m.getMembershipType() + "'",
               m.getMembershipType().equals("type2"));
    assertTrue("User name is not equal 'user2' but equal '" + m.getUserName() + "'",
               m.getUserName().equals("user2"));

    m = mHandler.findMembershipByUserGroupAndType("user2", "/group2", "type2");
    assertTrue("Membership is found but must be absent", m == null);
  }

  public void testRemoveMembershipByUser() throws Exception {
    createRecords("user3", "group3", "type3");

    Collection list = mHandler.removeMembershipByUser("user3", true);
    assertTrue("Removed " + list.size() + " memberships but 1 was presented", list.size() == 1);

    Membership m = mHandler.findMembershipByUserGroupAndType("user3", "/group3", "type3");
    assertTrue("Membership is found but must be absent", m == null);
  }

  public void testFindGroupByMembership() throws Exception {
    Collection list = gHandler.findGroupByMembership("user1", "type1");
    assertTrue("Found " + list.size() + " memberships but 1 is present", list.size() == 1);

    Object[] groups = list.toArray();
    Group g = (Group) groups[0];
    assertTrue("Group 'group1' is not found", g != null);
    assertTrue("Group description is not equal 'desc' but equal '" + g.getDescription() + "'",
               g.getDescription().equals("desc"));
    assertTrue("Group name is not equal 'group1' but equal '" + g.getGroupName() + "'",
               g.getGroupName().equals("group1"));
    assertTrue("Group groupId is not equal '/group1' but equal '" + g.getId() + "'",
               g.getId().equals("/group1"));
    assertTrue("Group label is not equal 'label' but equal '" + g.getLabel() + "'",
               g.getLabel().equals("label"));
    assertTrue("Group parentId is not equal 'null' but equal '" + g.getParentId() + "'",
               g.getParentId() == null);
  }

  public void testFindGroupsOfUser() throws Exception {
    Collection list = gHandler.findGroupByMembership("user1", null);
    assertTrue("Found " + list.size() + " memberships but 1 is present", list.size() == 1);
  }

  public void testFindUsersByGroup() throws Exception {
    PageList pList = uHandler.findUsersByGroup("/group1");
    Object list[] = pList.getAll().toArray();

    assertTrue("Found " + pList.getAll().size() + " users but 1 is present",
               pList.getAll().size() == 1);

    User u = (User) list[0];
    assertTrue("User 'user' it not found", u != null);
    assertTrue("User created date is not equal " + calendar.getTime() + " but equal "
        + u.getCreatedDate(), u.getCreatedDate() != calendar.getTime());
    assertTrue("User email is not equal 'email' but equal '" + u.getEmail() + "'",
               u.getEmail().equals("email"));
    assertTrue("User first name is not equal 'first' but equal '" + u.getFirstName() + "'",
               u.getFirstName().equals("first"));
    assertTrue("User last login time is not equal " + calendar.getTime() + " but equal "
        + u.getLastLoginTime(), u.getLastLoginTime() != calendar.getTime());
    assertTrue("User last name is not equal 'last' but equal '" + u.getLastName() + "'",
               u.getLastName().equals("last"));
    assertTrue("User password is not equal 'pwd' but equal '" + u.getPassword() + "'",
               u.getPassword().equals("pwd"));
    assertTrue("User name is not equal 'user1' but equal '" + u.getUserName() + "'",
               u.getUserName().equals("user1"));
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private void createRecords(String userName, String groupName, String type) throws Exception {
    // create users
    User u = uHandler.createUserInstance(userName);
    u.setEmail("email");
    u.setFirstName("first");
    u.setLastLoginTime(calendar.getTime());
    u.setCreatedDate(calendar.getTime());
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
