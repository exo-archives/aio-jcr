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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;

import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileHandler;

/**
 * Created by The eXo Platform SAS Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: UserProfileHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class UserProfileHandlerImpl implements UserProfileHandler {

  public static final String                     STORAGE_EXO_ATTRIBUTES = "exo:attributes";

  protected final JCROrganizationServiceImpl     service;

  protected final List<UserProfileEventListener> listeners              = new ArrayList<UserProfileEventListener>();

  UserProfileHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /**
   * {@inheritDoc}
   */
  public void addUserProfileEventListener(UserProfileEventListener listener) {
    listeners.add(listener);
  }

  /**
   * @param listener
   */
  public void removeUserProfileEventListener(UserProfileEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile createUserProfileInstance() {
    return new UserProfileImpl();
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile createUserProfileInstance(String userName) {
    return new UserProfileImpl(userName);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile findUserProfileByName(String userName) throws Exception {
    Session session = service.getStorageSession();
    try {
      UserProfile userProfile = null;

      String absPath = service.getStoragePath() + UserHandlerImpl.STORAGE_EXO_USERS + "/"
          + userName;
      if (!session.itemExists(absPath)) {
        return userProfile;
      }

      Node storagePath = (Node) session.getItem(absPath);
      for (NodeIterator nodes = storagePath.getNodes(); nodes.hasNext();) {
        if (userProfile != null) {
          throw new OrganizationServiceException("More than one user " + userName + " is found.");
        }
        Node uNode = nodes.nextNode();
        Node profileNode = uNode.getNode(STORAGE_EXO_ATTRIBUTES);

        userProfile = new UserProfileImpl(userName);
        for (PropertyIterator props = profileNode.getProperties(); props.hasNext();) {
          Property prop = props.nextProperty();
          userProfile.setAttribute(prop.getName(), prop.getString());
        }
      }
      return userProfile;
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findUserProfiles() throws Exception {
    Session session = service.getStorageSession();
    try {
      Collection<UserProfile> types = new ArrayList<UserProfile>();

      Node storagePath = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS);
      for (NodeIterator nodes = storagePath.getNodes(); nodes.hasNext();) {
        Node uNode = nodes.nextNode();
        types.add(findUserProfileByName(uNode.getName()));
      }
      return types;
    } finally {
      session.logout();
    }

  }

  /**
   * {@inheritDoc}
   */
  public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception {
    // TODO Implement broadcast
    Session session = service.getStorageSession();
    try {
      UserProfile userProfile = findUserProfileByName(userName);
      if (userProfile != null) {
        Node profileNode = (Node) session.getItem(service.getStoragePath()
            + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName + "/"
            + UserHandlerImpl.STORAGE_EXO_PROFILE);
        profileNode.remove();
        session.save();
      }
      return userProfile;
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception {
    // TODO Implement broadcast
    Session session = service.getStorageSession();
    try {
      String absPath = service.getStoragePath() + UserHandlerImpl.STORAGE_EXO_USERS + "/"
          + profile.getUserName();
      if (!session.itemExists(absPath)) {
        throw new OrganizationServiceException("User " + profile.getUserName() + " not found.");
      }

      Node uNode = (Node) session.getItem(absPath);
      if (!session.itemExists(absPath + "/" + UserHandlerImpl.STORAGE_EXO_PROFILE)) {
        Node profileNode = uNode.addNode(UserHandlerImpl.STORAGE_EXO_PROFILE);

        String keys[] = (String[]) profile.getUserInfoMap().keySet().toArray();
        for (int i = 0; i <= keys.length; i++) {
          profileNode.setProperty(keys[i], profile.getAttribute(keys[i]));
        }
        session.save();
      }
    } finally {
      session.logout();
    }
  }

}
