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

import java.util.Calendar;

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
public class TestUserHandlerImpl extends BaseStandaloneTest {

  private UserHandlerImpl            uHandler;

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

    uHandler = new UserHandlerImpl(organizationService);

    calendar = Calendar.getInstance();

    // create user
    /*
    UserImpl u = (UserImpl) uHandler.createUserInstance();
    u.setEmail("email1");
    u.setFirstName("first1");
    u.setLastLoginTime(calendar.getTime());
    u.setCreatedDate(calendar.getTime());
    u.setLastName("last1");
    u.setPassword("pwd1");
    u.setUserName("user1");
    uHandler.createUser(u, true);
    */
  }

  public void testUserFindByName() throws Exception {
    /*
    UserImpl u = (UserImpl) uHandler.findUserByName("user");
    assertTrue("User created date is not equal " + calendar.getTime() + " but equal "
        + u.getCreatedDate(), u.getCreatedDate() != calendar.getTime());
    assertTrue("", u.getEmail().equals("email"));
    assertTrue("", u.getFirstName().equals("first"));
    assertTrue("User last login time is not equal " + calendar.getTime() + " but equal "
        + u.getLastLoginTime(), u.getLastLoginTime() != calendar.getTime());
    assertTrue("", u.getLastName().equals("last"));
    assertTrue("", u.getPassword().equals("pwd"));
    assertTrue("", u.getUserName().equals("user")); 
    */
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }
}
