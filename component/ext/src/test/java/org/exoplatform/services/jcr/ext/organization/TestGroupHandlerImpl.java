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

import java.util.List;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10 Жов 2008
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestOrganizationService.java 111 2008-11-11 11:11:11Z $
 */
public class TestGroupHandlerImpl extends BaseStandaloneTest {

  private GroupHandlerImpl           gHandler;

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

    // Create groups
    GroupImpl g1 = (GroupImpl) gHandler.createGroupInstance();
    g1.setGroupName("group1");
    g1.setLabel("label1");
    g1.setDescription("desc1");
    gHandler.createGroup(g1, true);

    g1 = (GroupImpl) gHandler.findGroupById("/group1");
    GroupImpl g2 = (GroupImpl) gHandler.createGroupInstance();
    g2.setGroupName("group2");
    g2.setLabel("label2");
    g2.setDescription("desc2");
    gHandler.addChild(g1, g2, true);
  }

  public void testGroupAddChild() throws Exception {
    GroupImpl g2 = (GroupImpl) gHandler.findGroupById("/group1/group2");
    assertTrue("Group '/group1/group2' is not found", g2 != null);
    assertTrue("Group description is not equal 'desc2' but equal '" + g2.getDescription() + "'",
               g2.getDescription().equals("desc2"));
    assertTrue("Group name is not equal 'group2' but equal '" + g2.getGroupName() + "'",
               g2.getGroupName().equals("group2"));
    assertTrue("Group groupId is not equal '/group1/group2' but equal '" + g2.getId() + "'",
               g2.getId().equals("/group1/group2"));
    assertTrue("Group label is not equal 'label2' but equal '" + g2.getLabel() + "'",
               g2.getLabel().equals("label2"));
    assertTrue("Group parentId is not equal '/group1' but equal '" + g2.getParentId() + "'",
               g2.getParentId().equals("/group1"));
  }

  public void testGroupFindById() throws Exception {
    GroupImpl g1 = (GroupImpl) gHandler.findGroupById("/group1");
    assertTrue("Group 'group1' is not found", g1 != null);
    assertTrue("Group description is not equal 'desc1' but equal '" + g1.getDescription() + "'",
               g1.getDescription().equals("desc1"));
    assertTrue("Group name is not equal 'group1' but equal '" + g1.getGroupName() + "'",
               g1.getGroupName().equals("group1"));
    assertTrue("Group groupId is not equal '/group1' but equal '" + g1.getId() + "'",
               g1.getId().equals("/group1"));
    assertTrue("Group label is not equal 'label1' but equal '" + g1.getLabel() + "'",
               g1.getLabel().equals("label1"));
    assertTrue("Group parentId is not equal 'null' but equal '" + g1.getParentId() + "'",
               g1.getParentId() == null);
  }

  public void testGroupFindGroups() throws Exception {
    GroupImpl g1 = (GroupImpl) gHandler.findGroupById("/group1");

    List<GroupImpl> gs = (List<GroupImpl>) gHandler.findGroups(null);
    assertTrue("Group count must be equal 2 but equal " + gs.size(), gs.size() == 2);

    gs = (List<GroupImpl>) gHandler.findGroups(g1);
    assertTrue("Group count must be equal 1 but equal " + gs.size(), gs.size() == 1);
  }

  public void testGroupRemove() throws Exception {
    GroupImpl g1 = (GroupImpl) gHandler.findGroupById("/group1");

    GroupImpl g3 = (GroupImpl) gHandler.createGroupInstance();
    g3.setGroupName("group3");
    g3.setLabel("label3");
    g3.setDescription("desc3");
    gHandler.addChild(g1, g3, true);

    g3 = (GroupImpl) gHandler.findGroupById("/group1/group3");

    try {
      gHandler.removeGroup(g1, true);
      fail("Can not remove group with child groups");
    } catch (Exception e) {
    }

    try {
      gHandler.removeGroup(g3, true);
    } catch (Exception e) {
      fail("Can not remove group '/group1/group3'");
    }

    try {
      g3 = (GroupImpl) gHandler.findGroupById("/group1/group3");
      assertTrue("Group '/group1/group3' is removed but still present", g3 == null);
    } catch (Exception e) {
    }
  }

  public void testGroupSave() throws Exception {
    GroupImpl g1 = (GroupImpl) gHandler.findGroupById("/group1");

    GroupImpl g3 = (GroupImpl) gHandler.createGroupInstance();
    g3.setGroupName("group3");
    g3.setLabel("label3");
    g3.setDescription("desc3");
    gHandler.addChild(g1, g3, true);

    g3 = (GroupImpl) gHandler.findGroupById("/group1/group3");

    // change name and save
    g3.setGroupName("group4");
    gHandler.saveGroup(g3, true);

    // check
    GroupImpl g4 = (GroupImpl) gHandler.findGroupById("/group1/group4");
    assertTrue("Group '/group3/group4' is not found", g4 != null);
    assertTrue("Group description is not equal 'desc3' but equal '" + g4.getDescription() + "'",
               g4.getDescription().equals("desc3"));
    assertTrue("Group name is not equal 'group4' but equal '" + g4.getGroupName() + "'",
               g4.getGroupName().equals("group4"));
    assertTrue("Group groupId is not equal '/group1/group4' but equal '" + g4.getId() + "'",
               g4.getId().equals("/group1/group4"));
    assertTrue("Group label is not equal 'label3' but equal '" + g4.getLabel() + "'",
               g4.getLabel().equals("label3"));
    assertTrue("Group parentId is not equal '/group1' but equal '" + g4.getParentId() + "'",
               g4.getParentId().equals("/group1"));

    // save description only and save
    g4.setDescription("desc4");
    gHandler.saveGroup(g4, true);

    // check
    g4 = (GroupImpl) gHandler.findGroupById("/group1/group4");
    assertTrue("Group description is not equal 'desc4' but equal '" + g4.getDescription() + "'",
               g4.getDescription().equals("desc4"));

    // remove group
    gHandler.removeGroup(g4, true);

  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }
}
