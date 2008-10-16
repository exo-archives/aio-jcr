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
package org.exoplatform.services.jcr.ext.organization;

import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfileHandler;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com
 * Oct 27, 2005
 */

public class TestOrganizationServiceJDBC extends BaseStandaloneTest {

  static String                      Group1  = "Group1";

  static String                      Group2  = "Group2";

  static String                      Benj    = "Benj";

  static String                      Tuan    = "Tuan";

  private JCROrganizationServiceImpl organizationService;

  UserHandler                        userHandler_;

  UserProfileHandler                 profileHandler_;

  GroupHandler                       groupHandler_;

  MembershipTypeHandler              mtHandler_;

  MembershipHandler                  membershipHandler_;

  boolean                            runtest = true;

  public void setUp() throws Exception {
    if (!runtest)
      return;

    super.setUp();

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    organizationService = new JCROrganizationServiceImpl(service, "ws", "/exo:organization");
    organizationService.start();

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
    return "\n\n***************** Test organization service ***********************\n\n";
  }

  public void testUser() throws Exception {
    try {
      if (!runtest)
        return;
      long startGet = System.currentTimeMillis();
      /* Create an user with UserName: test */
      String USER = "test";
      // System.out.println("\n\n\n-------------- 000001 ---------------");

      createUser(USER);

      User u = userHandler_.findUserByName(USER);
      assertTrue("Found user instance", u != null);
      assertEquals("Expect user name is: ", USER, u.getUserName());
      // System.out.println("\n\n\n-------------- 000002 ---------------");
      // UserProfile up = profileHandler_.findUserProfileByName(USER);
      // assertTrue("Expect user profile is found: ", up != null);
      // System.out.println("\n\n\n-------------- 000003 ---------------");
      Query query = new Query();
      try {
        PageList users = userHandler_.findUsers(query);
        assertTrue("Expect 1 user found ", users.getAvailable() >= 1);
        System.out.println("AVAILABLE USERS: " + users.getAvailable());
        // System.out.println("\n\n\n-------------- 000004 ---------------");
      } catch (Exception e) {
        e.printStackTrace();
      }
      /* Update user's information */
      u.setFirstName("Exo(Update)");
      userHandler_.saveUser(u, false);

      // System.out.println("\n\n\n-------------- 000005 ---------------");
      assertEquals("expect first name is", "Exo(Update)", u.getFirstName());

      // System.out.println("\n\n\n-------------- 000006 ---------------");
      try {
        PageList piterator = userHandler_.getUserPageList(10);
        List list = piterator.getPage(1);
        // assertTrue(piterator.currentPage().size() == 2) ;
        assertEquals(1, piterator.currentPage().size()); // [PN] was 2, but from
        // where?
        /*
         * Remove a user: Expect result: user and it's profile will be removed
         */
      } catch (Exception e) {
        e.printStackTrace();
      }
      // System.out.println("\n\n\n-------------- 000007 ---------------");

      userHandler_.removeUser(USER, true);
      assertEquals("User: USER is removed: ", null, userHandler_.findUserByName(USER));
      assertTrue(" user's profile of USER was removed:",
                 profileHandler_.findUserProfileByName(USER) == null);
      // System.out.println("\n\n\n-------------- 000008 ---------------");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void testGroup() throws Exception {
    if (!runtest)
      return;
    try {
      /* Create a parent group with name is: GroupParent */
      String parentName = "GroupParent";
      Group groupParent = groupHandler_.createGroupInstance();
      groupParent.setGroupName(parentName);
      groupParent.setDescription("This is description");
      groupHandler_.createGroup(groupParent, true);
      assertTrue(groupParent.getId() != null);
      groupParent = groupHandler_.findGroupById(groupParent.getId());
      assertEquals(groupParent.getGroupName(), "GroupParent");
      /* Create a child group with name: Group1 */
      Group groupChild = groupHandler_.createGroupInstance();
      groupChild.setGroupName(Group1);
      groupHandler_.addChild(groupParent, groupChild, true);
      groupChild = groupHandler_.findGroupById(groupChild.getId());
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
      groupChild = groupHandler_.findGroupById(groupChild.getId());
      assertEquals(groupChild.getParentId(), groupParent.getId());
      assertEquals("Expect group child's name is: ", Group2, groupChild.getGroupName());
      /*
       * find all child group in groupParent Expect result: 2 child group:
       * group1, group2
       */
      Collection groups = groupHandler_.findGroups(groupParent);
      assertEquals("Expect number of child group in parent group is: ", 2, groups.size());
      Object arraygroups[] = groups.toArray();
      assertEquals("Expect child group's name is: ",
                   Group1,
                   ((Group) arraygroups[0]).getGroupName());
      assertEquals("Expect child group's name is: ",
                   Group2,
                   ((Group) arraygroups[1]).getGroupName());
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
    } catch (Exception e) {
      e.printStackTrace();
    }
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
