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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.organization.UserProfile;

/**
 * Created by The eXo Platform SAS Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: UserProfileImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class UserProfileImpl implements UserProfile {

  private Map<String, String> attributes;

  private String              userName;

  public UserProfileImpl() {
    attributes = new HashMap<String, String>();
  }

  public UserProfileImpl(String name) {
    attributes = new HashMap<String, String>();
    userName = name;
  }

  /**
   * @param attName
   *          The key name of an attribute in the user info map.
   * @return null if no key is matched in the user info map or a String value.
   */
  public String getAttribute(String attName) {
    return attributes.get(attName);
  }

  /**
   * @return The map that contains the user information. The map should only accept the
   *         java.lang.String for the key and the value.
   */
  public Map<String, String> getUserInfoMap() {
    return attributes;
  }

  /**
   * @return the username, the identifier of an user profile instance
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Use this method to change or add a new attribute to the user info map.
   * 
   * @param key
   *          The attribute name of the info
   * @param value
   *          An info of the user.
   */
  public void setAttribute(String key, String value) {
    attributes.put(key, value);
  }

  /**
   * @param map
   *          The map that contains the extra user information. The map should contains only the
   *          java.lang.String as the key and the value.
   */
  public void setUserInfoMap(Map<String, String> map) {
    attributes = map;
  }

  /**
   *@deprecated The third party developer should not used this method. We should pass the username
   *             to the @see UserProfileHandler.createUserInstance() and set the username for the
   *             instance once only.
   */
  public void setUserName(String username) {
    userName = username;
  }
}
