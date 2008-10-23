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

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestUserProfileHandlerImpl.java 111 2008-11-11 11:11:11Z $
 */
public class TestUserProfileHandlerImpl extends BaseStandaloneTest {
  private JCROrganizationServiceImpl organizationService;

  private UserHandler                uHandler;

  private UserProfileHandler         upHandler;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    organizationService = (JCROrganizationServiceImpl) container.getComponentInstanceOfType(JCROrganizationServiceImpl.class);

    upHandler = new UserProfileHandlerImpl(organizationService);
    uHandler = new UserHandlerImpl(organizationService);
  }

  /**
   * Find user profile by user name and check attributes.
   */
  public void testFindUserProfileByName() {
    UserProfile up;
    try {
      createUserProfile("userP1", true);
      up = upHandler.findUserProfileByName("userP1");
      assertTrue("Can not find user profile by name 'userP1'", up != null);
      assertTrue("User name is not equal 'userP1' but equal '" + up.getUserName() + "'",
                 up.getUserName().equals("userP1"));
      assertTrue("Attribute 'key1' is not equal 'value1' but equal '" + up.getAttribute("key1")
          + "'", up.getAttribute("key1").equals("value1"));
      assertTrue("Attribute 'key2' is not equal 'value2' but equal '" + up.getAttribute("key2")
          + "'", up.getAttribute("key2").equals("value2"));
      uHandler.removeUser("userP1", true);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }

    try {
      createUserProfile("userP2", false);
      up = upHandler.findUserProfileByName("userP2");
      assertTrue("User profile 'userP2' is found", up == null);
      uHandler.removeUser("userP2", true);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Find all profiles and check it count.
   */
  public void testFindUserProfiles() {
    try {
      createUserProfile("userP3", true);
      createUserProfile("userP4", true);

      Collection list = upHandler.findUserProfiles();
      assertTrue("Found " + list.size() + " user profiles.", list.size() == 2);

      uHandler.removeUser("userP3", true);
      uHandler.removeUser("userP4", true);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Create user profile and than try to remove it.
   */
  public void testRemoveUserProfile() {
    UserProfile up;
    try {
      createUserProfile("userP5", true);

      up = upHandler.removeUserProfile("userP5", true);
      assertTrue("Attribute 'key1' is not equal 'value1' but equal '" + up.getAttribute("key1")
          + "'", up.getAttribute("key1").equals("value1"));
      assertTrue("Attribute 'key2' is not equal 'value2' but equal '" + up.getAttribute("key2")
          + "'", up.getAttribute("key2").equals("value2"));

      up = upHandler.findUserProfileByName("userP5");
      uHandler.removeUser("userP5", true);
      assertTrue("User profile still present but was removed", up == null);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }

    try {
      up = upHandler.removeUserProfile("userP6", true);
      assertTrue("User profile 'userP6' is removed", up == null);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Create user profile, make changes, save and than try to check it.
   */
  public void testSaveUserProfile() {
    try {
      createUserProfile("userP7", true);

      UserProfile up = upHandler.findUserProfileByName("userP7");
      up.setAttribute("key1", "value11");
      up.setAttribute("key2", null);
      upHandler.saveUserProfile(up, true);

      up = upHandler.findUserProfileByName("userP7");
      assertTrue("Attribute 'key1' is not equal 'value11' but equal '" + up.getAttribute("key1")
          + "'", up.getAttribute("key1").equals("value11"));
      assertTrue("Attribute 'key2' is not equal 'null' but equal '" + up.getAttribute("key2") + "'",
                 up.getAttribute("key2") == null);

      uHandler.removeUser("userP7", true);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not be thrown");
    }
  }

  /**
   * Create user with profile.
   */
  private void createUserProfile(String userName, boolean createProfile) {
    // create users
    try {
      User u = uHandler.createUserInstance();
      u.setEmail("email");
      u.setFirstName("first");
      u.setLastLoginTime(Calendar.getInstance().getTime());
      u.setCreatedDate(Calendar.getInstance().getTime());
      u.setLastName("last");
      u.setPassword("pwd");
      u.setUserName(userName);
      uHandler.createUser(u, true);

      // create profile
      if (createProfile) {
        UserProfile up = upHandler.createUserProfileInstance(userName);
        up.setAttribute("key1", "value1");
        up.setAttribute("key2", "value2");
        upHandler.saveUserProfile(up, true);
      }
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
