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
import java.util.Date;
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

  public static final String                     STORAGE_EXO_ATTRIBUTES = "exo:attributes";

  protected final List<UserProfileEventListener> listeners              = new ArrayList<UserProfileEventListener>();

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
      } catch (Exception e) {
        throw new OrganizationServiceException("Can not find user profile", e);
      }

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

      Node storagePath = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS);
      for (NodeIterator nodes = storagePath.getNodes(); nodes.hasNext();) {
        Node uNode = nodes.nextNode();
        types.add(findUserProfileByName(uNode.getName()));
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
        Node profileNode = (Node) session.getItem(service.getStoragePath()
            + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName + "/"
            + UserHandlerImpl.STORAGE_EXO_PROFILE);
        profileNode.remove();
        session.save();
        return userProfile;
      } else {
        throw new OrganizationServiceException("Can not find user " + userName
            + " for remove profile.");
      }
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove user profile for user " + userName, e);
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
          + " for save profile.", e);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not save user profile for user "
          + profile.getUserName(), e);
    } finally {
      session.logout();
    }
  }

  @Override
  void checkMandatoryProperties(Object obj) throws Exception {
  }

  @Override
  Date readDateProperty(Node node, String prop) throws Exception {
    return null;
  }

  @Override
  Object readObjectFromNode(Node node) throws Exception {
    return null;
  }

  @Override
  String readStringProperty(Node node, String prop) throws Exception {
    return null;
  }

  @Override
  void writeObjectToNode(Object obj, Node node) throws Exception {
  }
}
