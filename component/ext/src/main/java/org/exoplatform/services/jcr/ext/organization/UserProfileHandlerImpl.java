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
import javax.jcr.PathNotFoundException;
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
   * When a method save, remove are called, the will broadcast an event. You can use this method to
   * register a listener to catch those events
   * 
   * @param listener
   *          The listener instance
   * @see UserProfileEventListener
   */
  public void addUserProfileEventListener(UserProfileEventListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove registered listener
   * 
   * @param listener
   *          The registered listener for removing
   */
  public void removeUserProfileEventListener(UserProfileEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * @return return a new UserProfile implementation instance. This instance is not persisted yet
   */
  public UserProfile createUserProfileInstance() {
    return new UserProfileImpl();
  }

  /**
   * @return return a new UserProfile implementation instance. This instance is not persisted yet
   * @param userName
   *          The user profile record with the username
   */
  public UserProfile createUserProfileInstance(String userName) {
    return new UserProfileImpl(userName);
  }

  /**
   * This method should search for and return UserProfile record according to the username
   * 
   * @param userName
   * @return return null if no record match the userName. return an UserProfile instance if a record
   *         match the username.
   * @throws Exception
   *           Throw Exception if the method fail to access the database or find more than one
   *           record that match the username.
   * @see UserProfile
   */
  public UserProfile findUserProfileByName(String userName) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName);

      try {
        Node profileNode = uNode.getNode(STORAGE_EXO_ATTRIBUTES);
        UserProfile userProfile = new UserProfileImpl(userName);
        for (PropertyIterator props = profileNode.getProperties(); props.hasNext();) {
          Property prop = props.nextProperty();
          userProfile.setAttribute(prop.getName(), prop.getString());
        }
        return userProfile;
      } finally {
      }
    } catch (PathNotFoundException e) {
      return null;
    } finally {
      session.logout();
    }
  }

  /**
   * Find and return all the UserProfile record in the database
   * 
   * @return
   * @throws Exception
   *           Throw exception if the method fail to access the database
   */
  public Collection findUserProfiles() throws Exception {
    Session session = service.getStorageSession();
    try {
      List<UserProfile> types = new ArrayList<UserProfile>();

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
   * This method should remove the user profile record in the database. If any listener fail to
   * handle event. The record should not be removed from the database.
   * 
   * @param userName
   *          The user profile record with the username should be removed from the database
   * @param broadcast
   *          Broadcast the event the listeners if broadcast is true.
   * @return The UserProfile instance that has been removed.
   * @throws Exception
   *           Throw exception if the method fail to remove the record or any listener fail to
   *           handle the event TODO Should we provide this method or the user profile should be
   *           removed only when the user is removed
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
   * This method should persist the profile instance to the database. If the profile is not existed
   * yet, the method should create a new user profile record. If there is an existed record. The
   * method should merge the data with the existed record
   * 
   * @param profile
   *          the profile instance to persist.
   * @param broadcast
   *          broadcast the event to the listener if broadcast is true
   * @throws Exception
   *           throw exception if the method fail to access the database or any listener fail to
   *           handle the event.
   */
  public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      String userPath = service.getStoragePath() + UserHandlerImpl.STORAGE_EXO_USERS + "/"
          + profile.getUserName();

      Node uNode = (Node) session.getItem(userPath);
      if (!session.itemExists(userPath + "/" + UserHandlerImpl.STORAGE_EXO_PROFILE)) {
        uNode.addNode(UserHandlerImpl.STORAGE_EXO_PROFILE);
      }

      Node profileNode = uNode.getNode(UserHandlerImpl.STORAGE_EXO_PROFILE);
      String keys[] = (String[]) profile.getUserInfoMap().keySet().toArray();
      for (int i = 0; i < keys.length; i++) {
        profileNode.setProperty(keys[i], profile.getAttribute(keys[i]));
      }
      session.save();

    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user " + profile.getUserName()
          + " for save profile.");
    } finally {
      session.logout();
    }
  }
}
