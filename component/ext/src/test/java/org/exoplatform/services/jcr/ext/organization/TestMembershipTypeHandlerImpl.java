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
 * @version $Id: TestMembershipTypeHandlerImpl.java 111 2008-11-11 11:11:11Z $
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

  public void testFindMembershipType() throws Exception {
    MembershipType mt = mtHandler.findMembershipType("manager");
    assertTrue("Membership type name not equal 'manager' but equal '" + mt.getName() + "'",
               mt.getName().equals("manager"));
    assertTrue("Membership type description not equal 'manager membership type' but equal '"
        + mt.getDescription() + "'", mt.getDescription().equals("manager membership type"));
  }

  public void testFindMembershipTypes() throws Exception {
    Collection mts = mtHandler.findMembershipTypes();
    assertTrue("Membership type count must be equal 4 but equal " + mts.size(), mts.size() == 4);
  }

  public void testRemoveMembershipType() throws Exception {
    createMembershipType("type", "desc");
    mtHandler.removeMembershipType("type", true);
    assertTrue("Membership type 'type' is present but must be removed",
               mtHandler.findMembershipType("type") == null);

  }

  public void testSaveMembershipType() throws Exception {
    createMembershipType("type", "desc");
    MembershipType mt = mtHandler.findMembershipType("type");

    // change name, description and save
    mt.setName("newType");
    mt.setDescription("desc");
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
    assertTrue("Membership type description not equal 'newDesc' but equal '" + mt.getDescription()
        + "'", mt.getDescription().equals("newDesc"));

    mtHandler.removeMembershipType(mt.getName(), true);
  }

  private void createMembershipType(String type, String desc) throws Exception {
    MembershipType mt = mtHandler.createMembershipTypeInstance();
    mt.setName(type);
    mt.setDescription(desc);
    mtHandler.createMembershipType(mt, true);
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

}
