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

import java.util.Calendar;
import java.util.Collection;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 14 Жов 2008
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestUserProfileHandlerImpl.java 111 2008-11-11 11:11:11Z $
 */
public class TestUserProfileHandlerImpl extends BaseStandaloneTest {
  private Calendar                   calendar;

  private JCROrganizationServiceImpl organizationService;

  private UserHandler                uHandler;

  private UserProfileHandler         upHandler;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    organizationService = new JCROrganizationServiceImpl(service, "ws", "/exo:organization");
    organizationService.start();

    upHandler = new UserProfileHandlerImpl(organizationService);
    uHandler = new UserHandlerImpl(organizationService);

    createRecords("userp");
  }

  public void testFindUserProfileByName() throws Exception {
    UserProfile up = upHandler.findUserProfileByName("userp");
    assertTrue("Can not find user profile by name 'userp'", up != null);
    assertTrue("User name is not equal 'userp' but equal '" + up.getUserName() + "'",
               up.getUserName().equals("userp"));
    assertTrue("Attribute 'key1' is not equal 'value1' but equal '" + up.getAttribute("key1") + "'",
               up.getAttribute("key1").equals("value1"));
    assertTrue("Attribute 'key2' is not equal 'value2' but equal '" + up.getAttribute("key2") + "'",
               up.getAttribute("key2").equals("value2"));

    up = upHandler.findUserProfileByName("userp_");
    assertTrue("User profile is found but must be absent", up == null);
  }

  public void testFindUserProfiles() throws Exception {
    Collection list = upHandler.findUserProfiles();
    assertTrue("Found " + list.size() + " user profiles.", list.size() == 1);
  }

  public void testRemoveUserProfile() throws Exception {
    createRecords("userp2");

    UserProfile up = upHandler.removeUserProfile("userp2", true);
    assertTrue("Attribute 'key1' is not equal 'value1' but equal '" + up.getAttribute("key1") + "'",
               up.getAttribute("key1").equals("value1"));
    assertTrue("Attribute 'key2' is not equal 'value2' but equal '" + up.getAttribute("key2") + "'",
               up.getAttribute("key2").equals("value2"));

    up = upHandler.findUserProfileByName("userp_");
    assertTrue("User profile 'userp2' still present but was removed", up == null);
  }

  public void testSaveUserProfile() throws Exception {
    createRecords("userp3");
    UserProfile up = upHandler.findUserProfileByName("userp3");
    up.setAttribute("key1", "value11");
    up.setAttribute("key2", null);
    upHandler.saveUserProfile(up, true);

    up = upHandler.findUserProfileByName("userp3");
    assertTrue("Attribute 'key1' is not equal 'value11' but equal '" + up.getAttribute("key1")
        + "'", up.getAttribute("key1").equals("value11"));
    assertTrue("Attribute 'key2' is not equal 'null' but equal '" + up.getAttribute("key2") + "'",
               up.getAttribute("key2") == null);
  }

  private void createRecords(String userName) throws Exception {
    calendar = Calendar.getInstance();
    calendar.set(2008, 1, 1);

    // create users
    User u = uHandler.createUserInstance();
    u.setEmail("email");
    u.setFirstName("first");
    u.setLastLoginTime(calendar.getTime());
    u.setCreatedDate(calendar.getTime());
    u.setLastName("last");
    u.setPassword("pwd");
    u.setUserName(userName);
    uHandler.createUser(u, true);

    // create profile
    UserProfile up = upHandler.createUserProfileInstance(userName);
    up.setAttribute("key1", "value1");
    up.setAttribute("key2", "value2");
    upHandler.saveUserProfile(up, true);
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

}
