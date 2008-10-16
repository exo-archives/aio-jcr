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
 * @version $Id$
 */
public class UserProfileHandlerImpl extends CommonHandler implements UserProfileHandler {

  public static final String                     EXO_ATTRIBUTES = "exo:attributes";

  protected final List<UserProfileEventListener> listeners      = new ArrayList<UserProfileEventListener>();

  /**
   * Organization service implementation covering the handler.
   */
  protected final JCROrganizationServiceImpl     service;

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
      String uPath = service.getStoragePath() + "/" + UserHandlerImpl.STORAGE_EXO_USERS + "/"
          + userName;
      if (!session.itemExists(uPath)) {
        return null;
      }

      Node uNode = (Node) session.getItem(uPath);
      Node attrNode = uNode.getNode(UserHandlerImpl.EXO_PROFILE).getNode(EXO_ATTRIBUTES);

      UserProfile userProfile = new UserProfileImpl(userName);
      for (PropertyIterator props = attrNode.getProperties(); props.hasNext();) {
        Property prop = props.nextProperty();

        // ignore system properties
        if (!(prop.getName()).startsWith("jcr:")) {
          userProfile.setAttribute(prop.getName(), prop.getString());
        }
      }
      return userProfile;

    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find user profile", e);
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
      List<UserProfile> types = new ArrayList<UserProfile>();

      Node storagePath = (Node) session.getItem(service.getStoragePath() + "/"
          + UserHandlerImpl.STORAGE_EXO_USERS);
      for (NodeIterator nodes = storagePath.getNodes(); nodes.hasNext();) {
        Node uNode = nodes.nextNode();
        UserProfile userProfile = findUserProfileByName(uNode.getName());
        if (userProfile != null) {
          types.add(userProfile);
        }
      }
      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find user profile", e);
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
        Node profileNode = (Node) session.getItem(service.getStoragePath() + "/"
            + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName + "/"
            + UserHandlerImpl.EXO_PROFILE);
        profileNode.remove();
        session.save();
        return userProfile;
      }
      throw new OrganizationServiceException("Can not find user '" + userName
          + "' for remove profile.");

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove user profile for user '" + userName
          + "'", e);
    } finally {
      session.logout();
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
    // TODO implement broadcast
    Session session = service.getStorageSession();
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

      Object[] keys = profile.getUserInfoMap().keySet().toArray();
      for (int i = 0; i < keys.length; i++) {
        String key = (String) keys[i];
        attrNode.setProperty((String) keys[i], profile.getAttribute((String) keys[i]));
      }

      session.save();

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not save user profile for user '"
          + profile.getUserName() + "'", e);
    } finally {
      session.logout();
    }
  }
}
