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
import org.exoplatform.services.jcr.RepositoryService;
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

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    organizationService = new JCROrganizationServiceImpl(service, "ws", "/exo:organization");
    organizationService.start();

    uHandler = new UserHandlerImpl(organizationService);

    calendar = Calendar.getInstance();
    calendar.set(2008, 1, 1);

    // create users
    createRecords("user");
  }

  public void testAuthenticate() throws Exception {
    assertTrue("Can not authenticate 'user' with password 'pwd'", uHandler.authenticate("user",
                                                                                        "pwd"));
    assertFalse("'user' with password 'pwd_' was authenticated", uHandler.authenticate("user",
                                                                                       "pwd_"));
    assertFalse("'user_' with password 'pwd' was authenticated", uHandler.authenticate("user_",
                                                                                       "pwd"));
  }

  public void testFindUserByName() throws Exception {
    User u = uHandler.findUserByName("user");
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
    assertTrue("User name is not equal 'user' but equal '" + u.getUserName() + "'",
               u.getUserName().equals("user"));
  }

  public void testFindUsers() throws Exception {
    org.exoplatform.services.organization.Query query = new org.exoplatform.services.organization.Query();

    query.setEmail("email");
    ObjectPageList pList = (ObjectPageList) uHandler.findUsers(query);
    assertTrue("Found " + pList.getAll().size() + " users with email equal 'email'",
               pList.getAll().size() == 1);
    query.setEmail(null);

    query.setUserName("user");
    pList = (ObjectPageList) uHandler.findUsers(query);
    assertTrue("Found " + pList.getAll().size() + " users with name equal 'user'",
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
  }

  public void testGetUserPageList() throws Exception {
    PageList pList = (ObjectPageList) uHandler.getUserPageList(10);
    assertTrue("Found " + pList.getAll().size() + " users but present only one", pList.getAll()
                                                                                      .size() == 1);
  }

  public void testRemoveUser() throws Exception {
    createRecords("user3");
    try {
      uHandler.removeUser("user3", true);
    } catch (Exception e) {
      fail("Can not remove user 'user3'");
    }

    User u = uHandler.findUserByName("user3");
    assertTrue("User 'user3' still present but was removed", u == null);
  }

  public void testSaveUser() throws Exception {
    createRecords("user3");

    // change name
    User u = uHandler.findUserByName("user3");
    u.setUserName("user4");
    uHandler.saveUser(u, true);
    u = uHandler.findUserByName("user4");
    assertFalse("Can not find user 'user4'", u == null);

    // change email
    u.setEmail("email_");
    uHandler.saveUser(u, true);
    u = uHandler.findUserByName("user4");
    assertTrue("User email is not equal 'email_' but equal '" + u.getEmail() + "'",
               u.getEmail().equals("email_"));

    uHandler.removeUser("user4", true);
  }

  private void createRecords(String userName) throws Exception {
    User u = uHandler.createUserInstance();
    u.setEmail("email");
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
