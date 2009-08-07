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

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id$
 */
public class TestGroupHandlerImpl extends BaseStandaloneTest {

  private GroupHandler        gHandler;

  private OrganizationService organizationService;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();
    organizationService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    gHandler = organizationService.getGroupHandler();
  }

  /**
   * Find group by id and check it properties.
   */
  public void testFindGroupById() throws Exception {
    try {
      Group g = gHandler.findGroupById("/platform/administrators");
      assertTrue("GroupId '/platform/administrators' is not found", g != null);
      assertTrue("Group description is not equal 'the /platform/administrators group' but equal '"
          + g.getDescription() + "'", g.getDescription()
                                       .equals("the /platform/administrators group"));
      assertTrue("Group name is not equal 'administrators' but equal '" + g.getGroupName() + "'",
                 g.getGroupName().equals("administrators"));
      assertTrue("Group groupId is not equal '/platform/administrators' but equal '" + g.getId()
          + "'", g.getId().equals("/platform/administrators"));
      assertTrue("Group label is not equal 'Administrators' but equal '" + g.getLabel() + "'",
                 g.getLabel().equals("Administrators"));
      assertTrue("Group parentId is not equal '/platform' but equal '" + g.getParentId() + "'",
                 g.getParentId().equals("/platform"));

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown.");
    }
  }

  /**
   * Find group by id and check it properties.
   */
  public void testFindGroupsByUser() throws Exception {
    try {
      // Hibernate org service returns
      // [Group[/organization/management/executive-board|executive-board],
      // Group[/platform/administrators|administrators], Group[/platform/users|users]]
      // JCR returns
      // [[groupId=/platform/administrators][groupName=administrators][parentId=/platform],
      // [groupId=/platform/users][groupName=users][parentId=/platform],
      // [groupId=/organization/management/executive-board][groupName=executive-board][parentId=/organization/management],
      // [groupId=/organization/management/executive-board][groupName=executive-board][parentId=/organization/management],
      // [groupId=/organization/management/executive-board][groupName=executive-board][parentId=/organization/management]]
      Collection g = gHandler.findGroupsOfUser("john");
      assertTrue("user John are in 3 groups", g.size() == 3);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown.");
    }
  }

  /**
   * Find groups by specific parent and check it count.
   */
  public void testFindGroups() throws Exception {
    try {
      Collection list = gHandler.findGroups(null);
      assertTrue("Found " + list.size() + " groups but 4 is present", list.size() == 4);

      list = gHandler.findGroups(gHandler.findGroupById("/organization/operations"));
      assertTrue("Found " + list.size() + " groups but 2 is present", list.size() == 2);

      list = gHandler.findGroups(gHandler.findGroupById("/organization/management/executive-board"));
      assertTrue("Found " + list.size() + " groups but 0 is present", list.size() == 0);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown.");
    }
  }

  /**
   * Get all groups and check it count.
   */
  public void testGetAllGroups() throws Exception {
    try {
      Collection list = gHandler.getAllGroups();
      assertTrue("Found " + list.size() + " groups but 16 is present", list.size() == 16);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown.");
    }

  }

  /**
   * Create new group and try to remove it.
   */
  public void testRemoveGroup() {
    Group g;
    try {
      createGroup("/organization/management/executive-board", "group1", "label", "desc");
      createGroup("/organization/management/executive-board/group1", "group2", "label", "desc");

      g = gHandler.findGroupById("/organization/management/executive-board/group1");
      gHandler.removeGroup(g, true);

      g = gHandler.findGroupById("/organization/management/executive-board/group1");
      assertTrue("Group '/organization/management/executive-board/group1' is removed but still present",
                 g == null);

      g = gHandler.findGroupById("/organization/management/executive-board/group1/group2");
      assertTrue("Group '/organization/management/executive-board/group1/group2' is removed but still present",
                 g == null);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown.");
    }

    try {
      createGroup("/organization/management/executive-board", "group1", "label", "desc");
      g = gHandler.findGroupById("/organization/management/executive-board/group1");
      gHandler.removeGroup(g, true);
      gHandler.removeGroup(g, true);
      fail("Exception should be thrown");
    } catch (Exception e) {
    }
  }

  /**
   * Create new group, change properties and save. Than try to check it.
   */
  public void testSaveGroup() {
    try {
      createGroup("/organization/management/executive-board", "group1", "label", "desc");

      Group g = gHandler.findGroupById("/organization/management/executive-board/group1");

      // change description and save
      g.setDescription("newDesc");
      gHandler.saveGroup(g, true);

      // check
      g = gHandler.findGroupById("/organization/management/executive-board/group1");
      assertTrue("Group description is not equal 'newDesc' but equal '" + g.getDescription() + "'",
                 g.getDescription().equals("newDesc"));

      // remove group
      gHandler.removeGroup(g, true);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown.");
    }
  }

  /**
   * Create new group.
   */
  private void createGroup(String parentId, String name, String label, String desc) {
    try {
      Group pg = gHandler.findGroupById(parentId);

      Group g = gHandler.createGroupInstance();
      g.setGroupName(name);
      g.setLabel(label);
      g.setDescription(desc);
      gHandler.addChild(pg, g, true);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown.");
    }
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }
}
