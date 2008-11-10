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

import org.apache.commons.logging.Log;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileHandler;

/**
 * Created by The eXo Platform SAS Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class UserProfileHandlerImpl extends CommonHandler implements UserProfileHandler {

  /**
   * The child not to storage users profile properties.
   */
  public static final String                     EXO_ATTRIBUTES = "exo:attributes";

  /**
   * The list of listeners to broadcast events.
   */
  protected final List<UserProfileEventListener> listeners      = new ArrayList<UserProfileEventListener>();

  /**
   * Organization service implementation covering the handler.
   */
  protected final JCROrganizationServiceImpl     service;

  /**
   * Log.
   */
  protected static Log                           log            = ExoLogger.getLogger("jcr.UserProfileHandlerImpl");

  /**
   * UserProfileHandlerImpl constructor.
   * 
   * @param service
   *          The initialization data
   */
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
   * {@inheritDoc}
   */
  public UserProfile createUserProfileInstance() {
    if (log.isDebugEnabled()) {
      log.debug("UserProfile.createUserProfileInstance() method is started");
    }

    return new UserProfileImpl();
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile createUserProfileInstance(String userName) {
    if (log.isDebugEnabled()) {
      log.debug("UserProfile.createUserProfileInstance(String) method is started");
    }

    return new UserProfileImpl(userName);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile findUserProfileByName(String userName) throws Exception {
    Session session = service.getStorageSession();
    try {
      return findUserProfileByName(session, userName);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile findUserProfileByName(Session session, String userName) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("UserProfile.findUserProfileByName method is started");
    }

    try {
      String attrPath = service.getStoragePath() + "/" + UserHandlerImpl.STORAGE_EXO_USERS + "/"
          + userName + "/" + UserHandlerImpl.EXO_PROFILE + "/" + EXO_ATTRIBUTES;

      // if attributes is absent return null
      if (!session.itemExists(attrPath)) {
        return null;
      }

      Node attrNode = (Node) session.getItem(attrPath);

      UserProfile userProfile = new UserProfileImpl(userName);
      for (PropertyIterator props = attrNode.getProperties(); props.hasNext();) {
        Property prop = props.nextProperty();

        // ignore system properties
        if (!(prop.getName()).startsWith("jcr:") && !(prop.getName()).startsWith("exo:")) {
          userProfile.setAttribute(prop.getName(), prop.getString());
        }
      }
      return userProfile;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find user profile", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findUserProfiles() throws Exception {
    Session session = service.getStorageSession();
    try {
      return findUserProfiles(session);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findUserProfiles(Session session) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("UserProfile.findUserProfiles method is started");
    }

    try {
      List<UserProfile> types = new ArrayList<UserProfile>();

      Node storagePath = (Node) session.getItem(service.getStoragePath() + "/"
          + UserHandlerImpl.STORAGE_EXO_USERS);
      for (NodeIterator nodes = storagePath.getNodes(); nodes.hasNext();) {
        Node uNode = nodes.nextNode();
        UserProfile userProfile = findUserProfileByName(session, uNode.getName());
        if (userProfile != null) {
          types.add(userProfile);
        }
      }
      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find user profiles", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile removeUserProfile(String userName, boolean broadcast) throws Exception {
    Session session = service.getStorageSession();
    try {
      return removeUserProfile(session, userName, broadcast);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile removeUserProfile(Session session, String userName, boolean broadcast) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("UserProfile.removeUserProfile method is started");
    }

    try {
      UserProfile userProfile = findUserProfileByName(session, userName);
      if (userProfile == null) {
        return null;
      }

      Node profileNode = (Node) session.getItem(service.getStoragePath() + "/"
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName + "/" + UserHandlerImpl.EXO_PROFILE);

      if (broadcast) {
        preDelete(userProfile);
      }

      profileNode.remove();
      session.save();

      if (broadcast) {
        postDelete(userProfile);
      }

      return userProfile;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove '" + userName + "' profile", e);
    }
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
   * {@inheritDoc}
   */
  public void saveUserProfile(UserProfile profile, boolean broadcast) throws Exception {
    Session session = service.getStorageSession();
    try {
      saveUserProfile(session, profile, broadcast);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserProfile(Session session, UserProfile profile, boolean broadcast) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("UserProfile.saveUserProfile method is started");
    }

    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + "/"
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + profile.getUserName());

      try {
        uNode.getNode(UserHandlerImpl.EXO_PROFILE);
      } catch (PathNotFoundException e) {
        uNode.addNode(UserHandlerImpl.EXO_PROFILE);
      }
      Node profileNode = uNode.getNode(UserHandlerImpl.EXO_PROFILE);

      try {
        profileNode.getNode(EXO_ATTRIBUTES);
      } catch (PathNotFoundException e) {
        profileNode.addNode(EXO_ATTRIBUTES);
      }
      Node attrNode = profileNode.getNode(EXO_ATTRIBUTES);

      if (broadcast) {
        preSave(profile, false);
      }

      Object[] keys = profile.getUserInfoMap().keySet().toArray();
      for (int i = 0; i < keys.length; i++) {
        String key = (String) keys[i];
        attrNode.setProperty(key, profile.getAttribute(key));
      }

      session.save();

      if (broadcast) {
        postSave(profile, false);
      }

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not save '" + profile.getUserName() + "' profile",
                                             e);
    }
  }

  /**
   * PreSave event.
   * 
   * @param userProfile
   *          The userProfile to save
   * @param isNew
   *          Is it new profile or not
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void preSave(UserProfile userProfile, boolean isNew) throws Exception {
    for (UserProfileEventListener listener : listeners)
      listener.preSave(userProfile, isNew);
  }

  /**
   * PostSave event.
   * 
   * @param userProfile
   *          The user profile to save
   * @param isNew
   *          Is it new profile or not
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void postSave(UserProfile userProfile, boolean isNew) throws Exception {
    for (UserProfileEventListener listener : listeners)
      listener.postSave(userProfile, isNew);
  }

  /**
   * PreDelete event.
   * 
   * @param userProfile
   *          The user profile to delete
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void preDelete(UserProfile userProfile) throws Exception {
    for (UserProfileEventListener listener : listeners)
      listener.preDelete(userProfile);
  }

  /**
   * PostDelete event.
   * 
   * @param userProfile
   *          The user profile to delete
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void postDelete(UserProfile userProfile) throws Exception {
    for (UserProfileEventListener listener : listeners)
      listener.postDelete(userProfile);
  }
}
