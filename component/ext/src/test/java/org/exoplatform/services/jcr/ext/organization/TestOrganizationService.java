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
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfileHandler;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com
 * Oct 27, 2005
 */

public class TestOrganizationService extends BaseStandaloneTest {

  static String               Group1  = "Group1";

  static String               Group2  = "Group2";

  static String               Benj    = "Benj";

  static String               Tuan    = "Tuan";

  private OrganizationService organizationService;

  UserHandler                 userHandler_;

  UserProfileHandler          profileHandler_;

  GroupHandler                groupHandler_;

  MembershipTypeHandler       mtHandler_;

  MembershipHandler           membershipHandler_;

  boolean                     runtest = true;

  public void setUp() throws Exception {
    super.setUp();

    if (!runtest)
      return;
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);

    userHandler_ = organizationService.getUserHandler();
    profileHandler_ = organizationService.getUserProfileHandler();
    groupHandler_ = organizationService.getGroupHandler();
    mtHandler_ = organizationService.getMembershipTypeHandler();
    membershipHandler_ = organizationService.getMembershipHandler();
  }

  public void tearDown() throws Exception {
    if (!runtest)
      return;
    System.err.println("##############################################################");

    super.tearDown();
  }

  protected String getDescription() {
    if (!runtest)
      return "";
    return "test hibernate organization service";
  }

  public void testUserPageSize() throws Exception {
    if (!runtest)
      return;
    /* Create an user with UserName: test */
    String USER = "test";
    int s = 15;

    for (int i = 0; i < s; i++)
      createUser(USER + "_" + String.valueOf(i));
    Query query = new Query();
    PageList users = userHandler_.findUsers(query);
    System.out.println("\n\n\n\n\n\n size: " + users.getAvailablePage());

    List list = users.getPage(1);
    for (Object ele : list) {
      User u = (User) ele;
      System.out.println(u.getUserName() + " and " + u.getEmail());
    }
    System.out.println("\n\n\n\n page 2:");
    list = users.getPage(2);
    System.out.println("size : " + list.size());
    for (Object ele : list) {
      User u = (User) ele;
      System.out.println(u.getUserName() + " and " + u.getEmail());
    }
    System.out.println("\n\n\n\n");

    for (int i = 0; i < s; i++) {
      userHandler_.removeUser(USER + "_" + String.valueOf(i), true);
    }
  }

  public void testUser() throws Exception {
    if (!runtest)
      return;
    /* Create an user with UserName: test */
    String USER = "testUser";

    createUser(USER);

    User u = userHandler_.findUserByName(USER);
    assertTrue("Found user instance", u != null);
    assertEquals("Expect user name is: ", USER, u.getUserName());

    // UserProfile up = profileHandler_.findUserProfileByName(USER);
    // assertTrue("Expect user profile is found: ", up != null);

    Query query = new Query();
    PageList users = userHandler_.findUsers(query);
    assertTrue("Expect 6 user found ", users.getAvailable() == 6);

    /* Update user's information */
    u.setFirstName("Exo(Update)");
    userHandler_.saveUser(u, false);
    // up.getUserInfoMap().put("user.gender", "male");
    // profileHandler_.saveUserProfile(up, true);
    // up = profileHandler_.findUserProfileByName(USER);
    assertEquals("expect first name is", "Exo(Update)", u.getFirstName());
    // assertEquals("Expect profile is updated: user.gender is ", "male", up.getUserInfoMap()
    // .get("user.gender"));

    PageList piterator = userHandler_.getUserPageList(10);
    // assertTrue (piterator.currentPage().size() == 2) ;
    assertEquals(6, piterator.currentPage().size()); // [PN] was 2, but from
    // where?

    /*
     * Remove a user: Expect result: user and it's profile will be removed
     */
    userHandler_.removeUser(USER, true);
    assertEquals("User: USER is removed: ", null, userHandler_.findUserByName(USER));
    assertTrue(" user's profile of USER was removed:",
               profileHandler_.findUserProfileByName(USER) == null);
  }

  public void testGroup() throws Exception {
    if (!runtest)
      return;
    /* Create a parent group with name is: GroupParent */
    String parentName = "GroupParent";
    Group groupParent = groupHandler_.createGroupInstance();
    groupParent.setGroupName(parentName);
    groupParent.setDescription("This is description");
    groupHandler_.createGroup(groupParent, true);
    assertTrue(((Group) groupParent).getId() != null); // [PN] was GroupImpl of
    // jdbc, caused a class
    // cast exc.
    groupParent = groupHandler_.findGroupById(groupParent.getId());
    assertEquals(groupParent.getGroupName(), "GroupParent");

    /* Create a child group with name: Group1 */
    Group groupChild = groupHandler_.createGroupInstance();
    groupChild.setGroupName(Group1);
    groupHandler_.addChild(groupParent, groupChild, true);
    groupChild = groupHandler_.findGroupById(groupParent.getId() + "/" + groupChild.getGroupName());
    assertEquals(groupChild.getParentId(), groupParent.getId());
    assertEquals("Expect group child's name is: ", Group1, groupChild.getGroupName());

    /* Update groupChild's information */
    groupChild.setLabel("GroupRenamed");
    groupChild.setDescription("new description ");
    groupHandler_.saveGroup(groupChild, true);
    assertEquals(groupHandler_.findGroupById(groupChild.getId()).getLabel(), "GroupRenamed");

    /* Create a group child with name is: Group2 */
    groupChild = groupHandler_.createGroupInstance();
    groupChild.setGroupName(Group2);
    groupHandler_.addChild(groupParent, groupChild, true);
    groupChild = groupHandler_.findGroupById(groupParent.getId() + "/" + groupChild.getGroupName());
    assertEquals(groupChild.getParentId(), groupParent.getId());
    assertEquals("Expect group child's name is: ", Group2, groupChild.getGroupName());

    /*
     * find all child group in groupParent Expect result: 2 child group: group1,
     * group2
     */
    Collection groups = groupHandler_.findGroups(groupParent);
    assertEquals("Expect number of child group in parent group is: ", 2, groups.size());
    Object arraygroups[] = groups.toArray();
    assertEquals("Expect child group's name is: ", Group1, ((Group) arraygroups[0]).getGroupName());
    assertEquals("Expect child group's name is: ", Group2, ((Group) arraygroups[1]).getGroupName());

    /* Remove a groupchild */
    groupHandler_.removeGroup(groupHandler_.findGroupById("/" + parentName + "/" + Group1), true);
    assertEquals("Expect child group has been removed: ", null, groupHandler_.findGroupById("/"
        + Group1));
    assertEquals("Expect only 1 child group in parent group",
                 1,
                 groupHandler_.findGroups(groupParent).size());

    /* Remove Parent group, all it's group child will be removed */
    groupHandler_.removeGroup(groupParent, true);
    assertEquals("Expect ParentGroup is removed:",
                 null,
                 groupHandler_.findGroupById(groupParent.getId()));
    assertEquals("Expect all child group is removed: ", 0, groupHandler_.findGroups(groupParent)
                                                                        .size());
  }

  public void testMembershipType() throws Exception {
    if (!runtest)
      return;
    /* Create a membershipType */
    String testType = "testType";
    MembershipType mt = mtHandler_.createMembershipTypeInstance();
    mt.setName(testType);
    mt.setDescription("This is a test");
    mt.setOwner("exo");
    mtHandler_.createMembershipType(mt, true);
    assertEquals("Expect mebershiptype is:", testType, mtHandler_.findMembershipType(testType)
                                                                 .getName());

    /* Update MembershipType's information */
    String desc = "This is a test (update)";
    mt.setDescription(desc);
    mtHandler_.saveMembershipType(mt, true);
    assertEquals("Expect membershiptype's description",
                 desc,
                 mtHandler_.findMembershipType(testType).getDescription());

    /* create another membershipType */
    mt = mtHandler_.createMembershipTypeInstance();
    mt.setName("anothertype");
    mt.setOwner("exo");
    mtHandler_.createMembershipType(mt, true);

    /*
     * find all membership type Expect result: 3 membershipType:
     * "testmembership", "anothertype" and "member"(default membership type)
     */
    Collection ms = mtHandler_.findMembershipTypes();
    assertEquals("Expect 6 membership in collection: ", 6, ms.size());

    /* remove "testmembership" */
    mtHandler_.removeMembershipType(testType, true);
    assertEquals("Membership type has been removed:", null, mtHandler_.findMembershipType(testType));
    assertEquals("Expect 5 membership in collection: ", 5, mtHandler_.findMembershipTypes().size());

    /* remove "anothertype" */
    mtHandler_.removeMembershipType("anothertype", true);
    assertEquals("Membership type has been removed:",
                 null,
                 mtHandler_.findMembershipType("anothertype"));
    assertEquals("Expect 4 membership in collection: ", 4, mtHandler_.findMembershipTypes().size());
    /* All membershipType was removed(except default membership) */
  }

  public void testMembership() throws Exception {
    if (!runtest)
      return;
    /* Create 2 user: benj and tuan */
    User user = createUser(Benj);
    User user2 = createUser(Tuan);

    /* Create "Group1" */
    Group group = groupHandler_.createGroupInstance();
    group.setGroupName(Group1);
    groupHandler_.createGroup(group, true);
    /* Create "Group2" */
    group = groupHandler_.createGroupInstance();
    group.setGroupName(Group2);
    groupHandler_.createGroup(group, true);

    /* Create membership1 and assign Benj to "Group1" with this membership */
    String testType = "testmembership";
    MembershipType mt = mtHandler_.createMembershipTypeInstance();
    mt.setName(testType);
    mtHandler_.createMembershipType(mt, true);

    membershipHandler_.linkMembership(user, groupHandler_.findGroupById("/" + Group1), mt, true);
    membershipHandler_.linkMembership(user, groupHandler_.findGroupById("/" + Group2), mt, true);
    membershipHandler_.linkMembership(user2, groupHandler_.findGroupById("/" + Group2), mt, true);

    mt = mtHandler_.createMembershipTypeInstance();
    mt.setName("membershipType2");
    mtHandler_.createMembershipType(mt, true);
    membershipHandler_.linkMembership(user, groupHandler_.findGroupById("/" + Group2), mt, true);

    mt = mtHandler_.createMembershipTypeInstance();
    mt.setName("membershipType3");
    mtHandler_.createMembershipType(mt, true);
    membershipHandler_.linkMembership(user, groupHandler_.findGroupById("/" + Group2), mt, true);

    /*
     * find all memberships in group2 Expect result: 4 membership: 3 for
     * Benj(testmebership, membershipType2, membershipType3) : 1 for
     * Tuan(testmembership)
     */
    System.out.println(" --------- find memberships by group -------------");
    Collection<Membership> mems = membershipHandler_.findMembershipsByGroup(groupHandler_.findGroupById("/"
        + Group2));
    assertEquals("Expect number of membership in group 4 is: ", 4, mems.size());

    /*
     * find all memberships in "Group2" relate with Benj Expect result: 3
     * membership
     */
    System.out.println(" --------- find memberships by user and group--------------");
    mems = membershipHandler_.findMembershipsByUserAndGroup(Benj, "/" + Group2);
    assertEquals("Expect number of membership in " + Group2 + " relate with benj is: ",
                 3,
                 mems.size());

    /*
     * find all memberships of Benj in all group Expect result: 5 membership: 3
     * memberships in "Group2", 1 membership in "Users" (default) : 1 membership
     * in "group1"
     */
    System.out.println(" --------- find memberships by user-------------");
    mems = membershipHandler_.findMembershipsByUser(Benj);
    assertEquals("expect membership is: ", 4, mems.size());

    /*
     * find memberships of Benj in "Group2" with membership type: testType
     * Expect result: 1 membership with membershipType is "testType"
     * (testmembership)
     */
    System.out.println("---------- find membership by User, Group and Type-----------");
    Membership membership = membershipHandler_.findMembershipByUserGroupAndType(Benj,
                                                                                "/" + Group2,
                                                                                testType);
    assertTrue("Expect membership is found:", membership != null);
    assertEquals("Expect membership type is: ", testType, membership.getMembershipType());
    assertEquals("Expect groupId of this membership is: ", "/" + Group2, membership.getGroupId());
    assertEquals("Expect user of this membership is: ", Benj, membership.getUserName());

    /*
     * find all groups of Benj Expect result: 3 group: "Group1", "Group2" and
     * "user" ("user" is default group)
     */
    System.out.println(" --------- find groups by user -------------");
    Collection<Group> groups = groupHandler_.findGroupsOfUser(Benj);
    assertEquals("expect group is: ", 2, groups.size()); // PN 28.11.2008, fix to 2 was 4

    /*
     * find all groups has membership type "TYPE" relate with Benj expect
     * result: 2 group: "Group1" and "Group2"
     */
    System.out.println("---------- find group of a user by membership-----------");
    groups = groupHandler_.findGroupByMembership(Benj, testType);
    assertEquals("expect group is: ", 2, groups.size());

    /* remove a membership */
    System.out.println("----------------- removed a membership ---------------------");
    String memId = membershipHandler_.findMembershipByUserGroupAndType(Benj,
                                                                       "/" + Group2,
                                                                       "membershipType3").getId();
    membershipHandler_.removeMembership(memId, true);
    assertTrue("Membership was removed: ",
               membershipHandler_.findMembershipByUserGroupAndType(Benj,
                                                                   "/" + Group2,
                                                                   "membershipType3") == null);

    /*
     * remove a user Expect result: all membership related with user will be
     * remove
     */
    System.out.println("----------------- removed a user----------------------");
    userHandler_.removeUser(Tuan, true);
    assertTrue("This user was removed", userHandler_.findUserByName(Tuan) == null);
    mems = membershipHandler_.findMembershipsByUser(Tuan);
    assertTrue("All membership related with this user was removed:", mems.isEmpty());

    /*
     * Remove a group Expect result: all membership associate with this group
     * will be removed
     */
    System.out.println("----------------- removed a group------------");
    groupHandler_.removeGroup(groupHandler_.findGroupById("/" + Group1), true);
    assertTrue("This group was removed", groupHandler_.findGroupById("/" + Group1) == null);

    /*
     * Remove a MembershipType Expect result: All membership have this type will
     * be removed
     */

    System.out.println("----------------- removed a membershipType------------");
    mtHandler_.removeMembershipType(testType, true);
    assertTrue("This membershipType was removed: ", mtHandler_.findMembershipType(testType) == null);
    // Check all memberships associate with all groups
    // * to guarantee that no membership associate with removed membershipType
    groups = groupHandler_.getAllGroups();
    for (Group g : groups) {
      mems = membershipHandler_.findMembershipsByGroup(g);
      for (Membership m : mems) {
        assertFalse("MembershipType of this membership is not: " + testType,
                    m.getMembershipType().equalsIgnoreCase(testType));
      }
    }

    System.out.println("----------------- removed a othes entities------------");
    mtHandler_.removeMembershipType("membershipType3", true);
    mtHandler_.removeMembershipType("membershipType2", true);
    groupHandler_.removeGroup(groupHandler_.findGroupById("/" + Group2), true);
    userHandler_.removeUser(Benj, true);
  }

  public User createUser(String userName) throws Exception {
    User user = userHandler_.createUserInstance();
    user.setUserName(userName);
    user.setPassword("default");
    user.setFirstName("default");
    user.setLastName("default");
    user.setEmail("exo@exoportal.org");
    userHandler_.createUser(user, true);
    return user;
  }
}
