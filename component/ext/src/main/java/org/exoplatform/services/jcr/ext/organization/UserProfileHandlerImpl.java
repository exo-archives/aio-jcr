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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileHandler;

/**
 * Created by The eXo Platform SAS 
 * 
 * Date: 24.07.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: UserProfileHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class UserProfileHandlerImpl implements UserProfileHandler {

  protected final JCROrganizationServiceImpl service;
  
  protected final List<UserProfileEventListener> listeners  = new ArrayList<UserProfileEventListener>();
  
  UserProfileHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }
  
  public void addUserProfileEventListener(UserProfileEventListener listener) {
    listeners.add(listener);
  }
  
  public void removeUserProfileEventListener(UserProfileEventListener listener) {
    listeners.remove(listener);
  }

  public UserProfile createUserProfileInstance() {
    // TODO Auto-generated method stub
    return null;
  }

  public UserProfile createUserProfileInstance(String userName) {
    // TODO Auto-generated method stub
    return null;
  }

  public UserProfile findUserProfileByName(String userName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection findUserProfiles() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub

  }

}
