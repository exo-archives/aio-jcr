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

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestOrganizationService.java 111 2008-11-11 11:11:11Z $
 */
public class TestUserHandlerImpl extends BaseStandaloneTest {

  private Calendar                   calendar;

  private JCROrganizationServiceImpl organizationService;

  private UserHandler                uHandler;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    organizationService = (JCROrganizationServiceImpl) container.getComponentInstanceOfType(JCROrganizationServiceImpl.class);

    uHandler = new UserHandlerImpl(organizationService);

    calendar = Calendar.getInstance();
    calendar.set(2008, 1, 1);
  }

  public void testAuthenticate() throws Exception {
    assertTrue("Can not authenticate 'demo' with password 'exo'", uHandler.authenticate("demo",
                                                                                        "exo"));
    assertFalse("'demo' with password 'exo_' was authenticated", uHandler.authenticate("demo",
                                                                                       "exo_"));
  }

  public void testFindUserByName() throws Exception {
    User u = uHandler.findUserByName("demo");
    assertTrue("User 'demo' it not found", u != null);
    assertTrue("User email is not equal 'demo@localhost' but equal '" + u.getEmail() + "'",
               u.getEmail().equals("demo@localhost"));
    assertTrue("User first name is not equal 'Demo' but equal '" + u.getFirstName() + "'",
               u.getFirstName().equals("Demo"));
    assertTrue("User last name is not equal 'exo' but equal '" + u.getLastName() + "'",
               u.getLastName().equals("exo"));
    assertTrue("User password is not equal 'exo' but equal '" + u.getPassword() + "'",
               u.getPassword().equals("exo"));
    assertTrue("User name is not equal 'demo' but equal '" + u.getUserName() + "'",
               u.getUserName().equals("demo"));
  }

  public void testFindUsers() throws Exception {
    createUser("user");
    org.exoplatform.services.organization.Query query = new org.exoplatform.services.organization.Query();

    query.setEmail("email@test");
    ObjectPageList pList = (ObjectPageList) uHandler.findUsers(query);
    assertTrue("Found " + pList.getAll().size() + " users with email equal 'email@test'",
               pList.getAll().size() == 1);
    query.setEmail(null);

    query.setUserName("*user*");
    pList = (ObjectPageList) uHandler.findUsers(query);
    assertTrue("Found " + pList.getAll().size() + " users with name equal '*user*'",
               pList.getAll().size() == 1);
    query.setUserName(null);

    query.setFirstName("first");
    query.setLastName("last");
    pList = (ObjectPageList) uHandler.findUsers(query);
    assertTrue("Found " + pList.getAll().size()
        + " users with name frist name equal 'first' and last name equal 'last'", pList.getAll()
                                                                                       .size() == 1);
    query.setFirstName(null);
    query.setLastName(null);

    Calendar calc = (Calendar) calendar.clone();
    calc.set(2007, 1, 1);
    query.setUserName("user");
    query.setFromLoginDate(calc.getTime());
    pList = (ObjectPageList) uHandler.findUsers(query);
    assertTrue("Found " + pList.getAll().size() + " users with fromLoginDate equal "
        + calc.getTime(), pList.getAll().size() == 1);

    calc.set(2009, 1, 1);
    query.setFromLoginDate(calc.getTime());
    pList = (ObjectPageList) uHandler.findUsers(query);
    assertTrue("Found " + pList.getAll().size() + " users with fromLoginDate equal "
        + calc.getTime(), pList.getAll().size() == 0);
    query.setFromLoginDate(null);
    query.setUserName(null);

    uHandler.removeUser("user", true);
  }

  public void testGetUserPageList() throws Exception {
    PageList pList = (ObjectPageList) uHandler.getUserPageList(10);
    assertTrue("Found " + pList.getAll().size() + " users but present only 5", pList.getAll()
                                                                                    .size() == 5);
  }

  public void testRemoveUser() throws Exception {
    createUser("user1");
    uHandler.removeUser("user1", true);
    User u = uHandler.findUserByName("user1");
    assertTrue("User 'user' still present but was removed", u == null);
  }

  public void testSaveUser() throws Exception {
    createUser("user2");

    // change name
    User u = uHandler.findUserByName("user2");
    u.setUserName("user_");
    uHandler.saveUser(u, true);
    u = uHandler.findUserByName("user_");
    assertFalse("Can not find user 'user_'", u == null);

    // change email
    u.setEmail("email_");
    uHandler.saveUser(u, true);
    u = uHandler.findUserByName("user_");
    assertTrue("User email is not equal 'email_' but equal '" + u.getEmail() + "'",
               u.getEmail().equals("email_"));

    uHandler.removeUser("user_", true);
  }

  private void createUser(String userName) throws Exception {
    User u = uHandler.createUserInstance();
    u.setEmail("email@test");
    u.setFirstName("first");
    u.setLastLoginTime(calendar.getTime());
    u.setCreatedDate(calendar.getTime());
    u.setLastName("last");
    u.setPassword("pwd");
    u.setUserName(userName);
    uHandler.createUser(u, true);

  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }
}
