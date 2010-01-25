/**
 * 
 */
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
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id$
 */
public class TestMembershipTypeHandlerImpl extends BaseStandaloneTest {

  private MembershipTypeHandler      mtHandler;

  private JCROrganizationServiceImpl organizationService;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();
    organizationService = (JCROrganizationServiceImpl) container.getComponentInstanceOfType(JCROrganizationServiceImpl.class);
    mtHandler = new MembershipTypeHandlerImpl(organizationService);
  }

  /**
   * Find membership type with specific name.
   */
  public void testFindMembershipType() {
    MembershipType mt;
    try {
      mt = mtHandler.findMembershipType("manager");
      assertTrue("Membership type 'member' is absent but must be present", mt != null);
      assertTrue("Membership type name not equal 'manager' but equal '" + mt.getName() + "'",
                 mt.getName().equals("manager"));
      assertTrue("Membership type description not equal 'manager membership type' but equal '"
          + mt.getDescription() + "'", mt.getDescription().equals("manager membership type"));
    } catch (Exception e1) {
      e1.printStackTrace();
      fail("Exception should not be thrown");
    }

    try {
      mt = mtHandler.findMembershipType("manager_");
      assertTrue("Membership type 'member_' is present but must be absent", mt == null);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Find all membership types in the storage and check count.
   */
  public void testFindMembershipTypes() {
    try {
      Collection mts = mtHandler.findMembershipTypes();
      assertTrue("Membership type count must be equal 3 but equal " + mts.size(), mts.size() == 3);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Create new membership type and try to remove it.
   */
  public void testRemoveMembershipType() {
    try {
      createMembershipType("type", "desc");
      mtHandler.removeMembershipType("type", true);
      assertTrue("Membership type 'type' is present but must be removed",
                 mtHandler.findMembershipType("type") == null);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Create new membership type and try to save with new name and than with new description.
   */
  public void testSaveMembershipType() {
    createMembershipType("type", "desc");
    MembershipType mt;
    try {
      mt = mtHandler.findMembershipType("type");

      // change name, description and save
      mt.setName("newType");
      mtHandler.saveMembershipType(mt, true);

      mt = mtHandler.findMembershipType("newType");
      assertTrue("Membership type name not equal 'newType' but equal '" + mt.getName() + "'",
                 mt.getName().equals("newType"));
      assertTrue("Membership type description not equal 'desc' but equal '" + mt.getDescription()
          + "'", mt.getDescription().equals("desc"));

      // check that previous membership type is absent
      assertTrue("Membership type 'type' is present but must be renamed",
                 mtHandler.findMembershipType("type") == null);

      // change description only and save
      mt.setDescription("newDesc");
      mtHandler.saveMembershipType(mt, true);

      mt = mtHandler.findMembershipType("newType");
      assertTrue("Membership type description not equal 'newDesc' but equal '"
          + mt.getDescription() + "'", mt.getDescription().equals("newDesc"));

      mtHandler.removeMembershipType(mt.getName(), true);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Create new membership type.
   * 
   * @param type
   *          The name of new type
   * @param desc
   *          The description of membership type
   * @throws Exception
   */
  private void createMembershipType(String type, String desc) {
    MembershipType mt = mtHandler.createMembershipTypeInstance();
    mt.setName(type);
    mt.setDescription(desc);
    try {
      mtHandler.createMembershipType(mt, true);
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

}
