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

import org.exoplatform.services.jcr.RepositoryService;
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

  private MembershipTypeHandler  mtHandler;

  private JCROrganizationServiceImpl organizationService;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    organizationService = new JCROrganizationServiceImpl(service, "ws", "/exo:organization");
    organizationService.start();

    mtHandler = new MembershipTypeHandlerImpl(organizationService);

    // Create membership types
    MembershipType mt = mtHandler.createMembershipTypeInstance();
    mt.setName("type1");
    mt.setDescription("desc1");
    mtHandler.createMembershipType(mt, true);

    mt = mtHandler.createMembershipTypeInstance();
    mt.setName("type2");
    mt.setDescription("desc2");
    mtHandler.createMembershipType(mt, true);
  }

  public void testMembershipTypeFind() throws Exception {
    MembershipType mt = mtHandler.findMembershipType("type1");
    assertTrue("Membership type name not equal 'type1' but equal '" + mt.getName() + "'",
               mt.getName().equals("type1"));
    assertTrue("Membership type description not equal 'desc1' but equal '" + mt.getDescription()
        + "'", mt.getDescription().equals("desc1"));
  }

  public void testMembershipTypeFindAll() throws Exception {
    Collection mts = mtHandler.findMembershipTypes();
    assertTrue("Membership type count must be equal 2 but equal " + mts.size(), mts.size() == 2);
  }

  public void testMembershipTypeRemove() throws Exception {
    MembershipType mt = mtHandler.createMembershipTypeInstance();
    mt.setName("type4");
    mt.setDescription("desc4");
    mtHandler.createMembershipType(mt, true);

    try {
      mtHandler.removeMembershipType("type4", true);
    } catch (Exception e) {
      fail("Can not remove membership type4");
    }

    try {
      mtHandler.findMembershipType("type4");
      fail("Membership type 'type5' is present, but must be removed");
    } catch (Exception e) {
    }

  }

  public void testMembershipTypeSave() throws Exception {
    MembershipType mt = mtHandler.findMembershipType("type2");

    // change name, description and save
    mt.setName("type3");
    mt.setDescription("desc3");
    mtHandler.saveMembershipType(mt, true);

    mt = mtHandler.findMembershipType("type3");
    assertTrue("Membership type name not equal 'type3' but equal '" + mt.getName() + "'",
               mt.getName().equals("type3"));
    assertTrue("Membership type description not equal 'desc3' but equal '" + mt.getDescription()
        + "'", mt.getDescription().equals("desc3"));

    // check that previous membership type is absent
    try {
      mtHandler.findMembershipType("type2");
      fail("Membership type 'type3' is present, but must be removed");
    } catch (Exception e) {
    }

    // change description only and save
    mt.setDescription("desc2");
    mtHandler.saveMembershipType(mt, true);

    mt = (MembershipTypeImpl) mtHandler.findMembershipType("type3");
    assertTrue("Membership type name not equal 'type3' but equal '" + mt.getName() + "'",
               mt.getName().equals("type3"));
    assertTrue("Membership type description not equal 'desc2' but equal '" + mt.getDescription()
        + "'", mt.getDescription().equals("desc2"));
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

}
