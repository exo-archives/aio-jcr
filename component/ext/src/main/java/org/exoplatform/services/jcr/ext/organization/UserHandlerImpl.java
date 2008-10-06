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
import java.util.Calendar;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: UserHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class UserHandlerImpl implements UserHandler {

  public static final String                 STORAGE_EXO_CREATED_DATE    = "exo:createdDate";

  public static final String                 STORAGE_EXO_EMAIL           = "exo:email";

  public static final String                 STORAGE_EXO_FIRST_NAME      = "exo:firstName";

  public static final String                 STORAGE_EXO_LAST_LOGIN_TIME = "exo:lastLoginTime";

  public static final String                 STORAGE_EXO_LAST_NAME       = "exo:lastName";

  public static final String                 STORAGE_EXO_MEMBERSHIP      = "exo:membership";

  public static final String                 STORAGE_EXO_PASSWORD        = "exo:password";

  public static final String                 STORAGE_EXO_PROFILE         = "exo:profile";

  public static final String                 STORAGE_EXO_USERS           = "/exo:users";

  protected final List<UserEventListener>    listeners                   = new ArrayList<UserEventListener>();

  protected final JCROrganizationServiceImpl service;

  UserHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /**
   * This method is used to register an user event listener
   * 
   * @param listener
   */
  public void addUserEventListener(UserEventListener listener) {
    listeners.add(listener);
  }

  /**
   * Check if the username and the password of an user is valid.
   * 
   * @param username
   * @param password
   * @return return true if the username and the password is match with an user record in the
   *         database, else return false.
   * @throws Exception
   *           throw an exception if cannot access the database
   */
  public boolean authenticate(String username, String password) throws Exception {
    User user = findUserByName(username);
    return (user != null ? user.getPassword().equals(password) : false);
  }

  /**
   * This method is used to persist a new user object.
   * 
   * @param user
   *          The user object to save
   * @param broadcast
   *          If the broadcast value is true , then the UserHandler should broadcast the event to
   *          all the listener that register with the organization service. For example, the portal
   *          service register an user event listener with the organization service. when a new
   *          account is created, a portal configuration should be created for the new user account
   *          at the same time. In this case the portal user event listener will be called in the
   *          createUser method.
   * @throws Exception
   *           The exception can be throwed if the the UserHandler cannot persist the user object or
   *           any listeners fail to handle the user event.
   */
  public void createUser(User user, boolean broadcast) throws Exception {
    // TODO Implement broadcast

    if (user.getUserName() == null) {
      throw new OrganizationServiceException("Can not create user without name.");
    } else if (user.getFirstName() == null) {
      throw new OrganizationServiceException("Can not create user without first name.");
    } else if (user.getLastName() == null) {
      throw new OrganizationServiceException("Can not create user without last name.");
    } else if (user.getPassword() == null) {
      throw new OrganizationServiceException("Can not create user without password.");
    } else if (user.getCreatedDate() == null) {
      throw new OrganizationServiceException("Can not create user without created date.");
    }

    Session session = service.getStorageSession();
    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_USERS);
      Node uNode = storageNode.addNode(user.getUserName());

      Calendar calendar = Calendar.getInstance();
      uNode.setProperty(STORAGE_EXO_EMAIL, user.getEmail());
      uNode.setProperty(STORAGE_EXO_FIRST_NAME, user.getFirstName());
      uNode.setProperty(STORAGE_EXO_LAST_NAME, user.getLastName());
      uNode.setProperty(STORAGE_EXO_PASSWORD, user.getPassword());

      // TODO is it correct?
      calendar.setTime(user.getCreatedDate());
      uNode.setProperty(STORAGE_EXO_CREATED_DATE, calendar);
      calendar.setTime(user.getLastLoginTime());
      uNode.setProperty(STORAGE_EXO_LAST_LOGIN_TIME, calendar);

      session.save();
    } finally {
      session.logout();
    }
  }

  /**
   * @deprecated This method create an User instance that implement the User interface. The user
   *             instance is not persisted yet
   * @return new user instance
   */
  public User createUserInstance() {
    return new UserImpl();
  }

  /**
   * This method create an User instance that implement the User interface. The user instance is not
   * persisted yet
   * 
   * @param username
   *          Username for new user instance.
   * @return new user instance
   */
  public User createUserInstance(String username) {
    return new UserImpl(username);
  }

  /**
   * @param userName
   *          the user that the user handler should search for
   * @return The method return null if there no user matches the given username. The method return
   *         an User object if an user that match the username.
   * @throws Exception
   *           The exception is throwed if the method fail to access the user database or more than
   *           one user object with the same username is found
   */
  public User findUserByName(String userName) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_USERS + "/"
          + userName);
      try {
        User user = new UserImpl(userName, uNode.getUUID());
        user.setCreatedDate(uNode.getProperty(STORAGE_EXO_CREATED_DATE).getDate().getTime());
        user.setLastLoginTime(uNode.getProperty(STORAGE_EXO_LAST_LOGIN_TIME).getDate().getTime());
        user.setEmail(uNode.getProperty(STORAGE_EXO_EMAIL).getString());
        user.setPassword(uNode.getProperty(STORAGE_EXO_PASSWORD).getString());
        user.setFirstName(uNode.getProperty(STORAGE_EXO_FIRST_NAME).getString());
        user.setLastName(uNode.getProperty(STORAGE_EXO_LAST_NAME).getString());
        return user;
      } finally {
      }
    } catch (PathNotFoundException e) {
      return null;
    } finally {
      session.logout();
    }
  }

  /**
   * This method search for the users according to a search criteria, the query
   * 
   * @param query
   *          The query object contains the search criteria.
   * @return return the found users in a page list according to the query.
   * @throws Exception
   *           throw exception if the service cannot access the database
   */
  public PageList findUsers(org.exoplatform.services.organization.Query query) throws Exception {
    String where = "";
    if (query.getUserName() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + "jcr:path LIKE "
          + service.getStoragePath() + STORAGE_EXO_USERS + "/" + query.getUserName());
    }
    if (query.getEmail() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + STORAGE_EXO_EMAIL + " LIKE "
          + query.getEmail());
    }
    if (query.getFirstName() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + STORAGE_EXO_FIRST_NAME + " LIKE "
          + query.getFirstName());
    }
    if (query.getLastName() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + STORAGE_EXO_LAST_NAME + " LIKE "
          + query.getLastName());
    }
    if (query.getFromLoginDate() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + STORAGE_EXO_LAST_LOGIN_TIME + ">="
          + query.getFromLoginDate());
    }
    if (query.getToLoginDate() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + STORAGE_EXO_LAST_LOGIN_TIME + "<="
          + query.getToLoginDate());
    }

    List<User> types = new ArrayList<User>();

    String statement = "select * from " + STORAGE_EXO_USERS.substring(1)
        + (where.length() == 0 ? "" : " where " + where);
    Query uQuery = service.getStorageSession()
                          .getWorkspace()
                          .getQueryManager()
                          .createQuery(statement, Query.SQL);
    QueryResult uRes = uQuery.execute();
    for (NodeIterator uNodes = uRes.getNodes(); uNodes.hasNext();) {
      Node uNode = uNodes.nextNode();
      User user = findUserByName(uNode.getName());
      if (user != null) {
        types.add(user);
      }
    }
    return new ObjectPageList(types, 10);
  }

  /**
   * This method should search and return the list of the users in a given group.
   * 
   * @param groupId
   *          id of the group. The return users list should be in this group
   * @return return a page list iterator of a group of the user in the database
   * @throws Exception
   */
  public PageList findUsersByGroup(String groupId) throws Exception {
    List<User> users = new ArrayList<User>();

    // find group
    String statement = "select * from " + MembershipHandlerImpl.STORAGE_EXO_GROUP
        + " where jcr:uuid='" + groupId + "'";
    Query gquery = service.getStorageSession()
                          .getWorkspace()
                          .getQueryManager()
                          .createQuery(statement, Query.SQL);
    QueryResult gres = gquery.execute();
    if (gres.getNodes().hasNext()) {
      // has group
      Node groupNode = gres.getNodes().nextNode();

      // find memberships
      statement = "select * from " + MembershipHandlerImpl.STORAGE_EXO_USER_MEMBERSHIP + " where "
          + MembershipHandlerImpl.STORAGE_EXO_GROUP + "='" + groupNode.getUUID() + "'";
      Query mquery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(statement, Query.SQL);
      QueryResult mres = mquery.execute();
      for (NodeIterator membs = mres.getNodes(); membs.hasNext();) {
        Node membership = membs.nextNode();
        Node userNode = membership.getParent();
        User user = findUserByName(userNode.getName());
        if (user != null) {
          users.add(user);
        }
      }
    }

    return new ObjectPageList(users, 10);
  }

  /**
   * This method is used to get all the users in the database
   * 
   * @param pageSize
   *          The number of user in each page
   * @return return a page list iterator. The page list should allow the developer get all the users
   *         or get a page of users if the return number of users is too large.
   * @throws Exception
   */
  public PageList getUserPageList(int pageSize) throws Exception {
    Session session = service.getStorageSession();
    try {
      List<User> types = new ArrayList<User>();
      Node storageNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_USERS);
      for (NodeIterator uNodes = storageNode.getNodes(); uNodes.hasNext();) {
        Node uNode = uNodes.nextNode();
        User user = findUserByName(uNode.getName());
        if (user != null) {
          types.add(user);
        }
      }
      return new ObjectPageList(types, 10);
    } finally {
      session.logout();
    }
  }

  /**
   * Remove an user and broadcast the event to all the registered listener. When the user is removed
   * , the user profile and all the membership of the user should be removed as well.
   * 
   * @param userName
   *          The user should be removed from the user database
   * @param broadcast
   *          If broadcast is true, the the delete user event should be broadcasted to all
   *          registered listener
   * @return return the User object after that user has been removed from database
   * @throws Exception
   * @TODO Should we broadcast the membership remove event when a user is removed ??
   */
  public User removeUser(String userName, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_USERS + "/"
          + userName);
      try {
        User user = findUserByName(userName);
        uNode.remove();
        session.save();
        return user;
      } finally {
      }
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user " + userName + " for delete");
    } finally {
      session.logout();
    }
  }

  /**
   * Remove registered listener
   * 
   * @param listener
   *          The registered listener for remove
   */
  public void removeUserEventListener(UserEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * This method is used to update an existing User object
   * 
   * @param user
   *          The user object to update
   * @param broadcast
   *          If the broadcast is true , then all the user event listener that register with the
   *          organization service will be called
   * @throws Exception
   *           The exception can be throwed if the the UserHandler cannot save the user object or
   *           any listeners fail to handle the user event.
   */
  public void saveUser(User user, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      UserImpl userImpl = (UserImpl) user;
      if (userImpl.getUUId() == null) {
        throw new OrganizationServiceException("Can not find user for save changes because UUId is null.");
      }

      try {
        Node uNode = session.getNodeByUUID(userImpl.getUUId());
        String srcPath = uNode.getPath();
        int pos = srcPath.lastIndexOf('/');
        String prevName = srcPath.substring(pos + 1);
        String destPath = srcPath.substring(0, pos) + "/" + user.getUserName();

        try {
          session.move(srcPath, destPath);
          try {
            Calendar calendar = Calendar.getInstance();
            uNode.setProperty(STORAGE_EXO_EMAIL, user.getEmail());
            uNode.setProperty(STORAGE_EXO_FIRST_NAME, user.getFirstName());
            uNode.setProperty(STORAGE_EXO_LAST_NAME, user.getLastName());
            uNode.setProperty(STORAGE_EXO_PASSWORD, user.getPassword());

            // TODO is it correct?
            calendar.setTime(user.getCreatedDate());
            uNode.setProperty(STORAGE_EXO_CREATED_DATE, calendar);
            calendar.setTime(user.getLastLoginTime());
            uNode.setProperty(STORAGE_EXO_LAST_LOGIN_TIME, calendar);
            session.save();

          } finally {
          }
        } catch (PathNotFoundException e) {
          throw new OrganizationServiceException("The user " + prevName
              + " is absent and can not be save.");
        } catch (ItemExistsException e) {
          throw new OrganizationServiceException("Can not save user " + prevName
              + " because new user " + user.getUserName() + " is exist.");
        }

      } catch (ItemNotFoundException e) {
        throw new OrganizationServiceException("Can not user for save changes by UUId.");
      }
    } finally {
      session.logout();
    }
  }
}
