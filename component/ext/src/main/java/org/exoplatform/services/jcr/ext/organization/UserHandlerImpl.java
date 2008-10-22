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
 * @version $Id$
 */
public class UserHandlerImpl extends CommonHandler implements UserHandler {

  public static final String                 EXO_CREATED_DATE    = "exo:createdDate";

  public static final String                 EXO_EMAIL           = "exo:email";

  public static final String                 EXO_FIRST_NAME      = "exo:firstName";

  public static final String                 EXO_LAST_LOGIN_TIME = "exo:lastLoginTime";

  public static final String                 EXO_LAST_NAME       = "exo:lastName";

  public static final String                 EXO_MEMBERSHIP      = "exo:membership";

  public static final String                 EXO_PASSWORD        = "exo:password";

  public static final String                 EXO_PROFILE         = "exo:profile";

  public static final String                 STORAGE_EXO_USERS   = "exo:users";

  protected final List<UserEventListener>    listeners           = new ArrayList<UserEventListener>();

  /**
   * Organization service implementation covering the handler.
   */
  protected final JCROrganizationServiceImpl service;

  /**
   * UserHandlerImpl constructor.
   * 
   * @param service
   *          The initialization data.
   */
  UserHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /**
   * {@inheritDoc}
   */
  public void addUserEventListener(UserEventListener listener) {
    listeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  public boolean authenticate(String username, String password) throws Exception {
    User user = findUserByName(username);
    boolean authenticated = (user != null ? user.getPassword().equals(password) : false);
    if (authenticated) {
      user.setLastLoginTime(Calendar.getInstance().getTime());
      saveUser(user, false);
    }
    return authenticated;
  }

  /**
   * {@inheritDoc}
   */
  public void createUser(User user, boolean broadcast) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_USERS);
      Node uNode = storageNode.addNode(user.getUserName());

      // set default value for createdDate
      if (user.getCreatedDate() == null) {
        Calendar calendar = Calendar.getInstance();
        user.setCreatedDate(calendar.getTime());
      }

      if (broadcast) {
        preSave(user, true);
      }

      writeObjectToNode(user, uNode);
      session.save();

      if (broadcast) {
        postSave(user, true);
      }

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not create user '" + user.getUserName() + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public User createUserInstance() {
    return new UserImpl();
  }

  /**
   * {@inheritDoc}
   */
  public User createUserInstance(String username) {
    return new UserImpl(username);
  }

  /**
   * {@inheritDoc}
   */
  public User findUserByName(String userName) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_USERS + "/"
          + userName);
      return readObjectFromNode(uNode);

    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find user '" + userName + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public PageList findUsers(org.exoplatform.services.organization.Query query) throws Exception {
    String where = "";
    where = "jcr:path LIKE '" + "%" + "'";
    if (query.getEmail() != null) {
      where = where + (where.length() == 0 ? "" : " AND ")
          + ("exo:email LIKE '" + replaceAsterix(query.getEmail()) + "'");
    }
    if (query.getFirstName() != null) {
      where = where + (where.length() == 0 ? "" : " AND ")
          + ("exo:firstName LIKE '" + replaceAsterix(query.getFirstName()) + "'");
    }
    if (query.getLastName() != null) {
      where = where + (where.length() == 0 ? "" : " AND ")
          + ("exo:lastName LIKE '" + replaceAsterix(query.getLastName()) + "'");
    }

    List<User> types = new ArrayList<User>();

    String statement = "select * from exo:user " + (where.length() == 0 ? "" : "where " + where);
    Query uQuery = service.getStorageSession()
                          .getWorkspace()
                          .getQueryManager()
                          .createQuery(statement, Query.SQL);
    QueryResult uRes = uQuery.execute();
    for (NodeIterator uNodes = uRes.getNodes(); uNodes.hasNext();) {
      Node uNode = uNodes.nextNode();
      String userName = uNode.getName();
      if (query.getUserName() == null || userName.indexOf(removeAsterix(query.getUserName())) != -1) {
        User user = findUserByName(uNode.getName());
        if ((user != null)
            && (query.getFromLoginDate() == null || (user.getLastLoginTime() != null && query.getFromLoginDate()
                                                                                             .getTime() <= user.getLastLoginTime()
                                                                                                               .getTime()))
            && (query.getToLoginDate() == null || (user.getLastLoginTime() != null && query.getToLoginDate()
                                                                                           .getTime() >= user.getLastLoginTime()
                                                                                                             .getTime()))) {
          types.add(user);
        }
      }
    }
    return new ObjectPageList(types, 10);
  }

  /**
   * {@inheritDoc}
   */
  public PageList findUsersByGroup(String groupId) throws Exception {
    Session session = service.getStorageSession();
    try {
      List<User> users = new ArrayList<User>();

      // get UUId of the group
      Node gNode = (Node) session.getItem(service.getStoragePath() + "/"
          + GroupHandlerImpl.STORAGE_EXO_GROUPS + groupId);

      // find group
      String statement = "select * from exo:group where jcr:uuid='" + gNode.getUUID() + "'";
      Query gquery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(statement, Query.SQL);
      QueryResult gres = gquery.execute();
      if (gres.getNodes().hasNext()) {
        // has group
        Node groupNode = gres.getNodes().nextNode();

        // find memberships
        statement = "select * from exo:userMembership where exo:group='" + groupNode.getUUID()
            + "'";
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

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not users by group '" + groupId + "'", e);
    } finally {
      session.logout();
    }

  }

  /**
   * {@inheritDoc}
   */
  public PageList getUserPageList(int pageSize) throws Exception {
    Session session = service.getStorageSession();
    try {
      List<User> types = new ArrayList<User>();
      Node storageNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_USERS);
      for (NodeIterator uNodes = storageNode.getNodes(); uNodes.hasNext();) {
        Node uNode = uNodes.nextNode();
        User user = findUserByName(uNode.getName());
        if (user != null) {
          types.add(user);
        }
      }
      return new ObjectPageList(types, 10);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find users", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public User removeUser(String userName, boolean broadcast) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_USERS + "/"
          + userName);
      User user = findUserByName(userName);

      if (broadcast) {
        preDelete(user);
      }

      uNode.remove();
      session.save();

      if (broadcast) {
        postDelete(user);
      }

      return user;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove user '" + userName + "'", e);
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
   * {@inheritDoc}
   */
  public void saveUser(User user, boolean broadcast) throws Exception {
    Session session = service.getStorageSession();
    try {
      UserImpl userImpl = (UserImpl) user;
      String userUUID = userImpl.getUUId() != null
          ? userImpl.getUUId()
          : ((UserImpl) findUserByName(user.getUserName())).getUUId();
      Node uNode = session.getNodeByUUID(userUUID);

      String srcPath = uNode.getPath();
      int pos = srcPath.lastIndexOf('/');
      String prevName = srcPath.substring(pos + 1);
      String destPath = srcPath.substring(0, pos) + "/" + user.getUserName();

      if (!prevName.equals(user.getUserName())) {
        session.move(srcPath, destPath);
      }

      Node nmtNode = (Node) session.getItem(destPath);

      if (broadcast) {
        preSave(user, false);
      }

      writeObjectToNode(user, nmtNode);
      session.save();

      if (broadcast) {
        postSave(user, false);
      }

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not save user '" + user.getUserName() + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * Read user properties from the node in the storage.
   * 
   * @param node
   *          The node to read from
   * @return The user
   * @throws Exception
   *           An exception is thrown if method can not get access to the database
   */
  private User readObjectFromNode(Node node) throws Exception {
    try {
      User user = new UserImpl(node.getName(), node.getUUID());
      user.setCreatedDate(readDateProperty(node, EXO_CREATED_DATE));
      user.setLastLoginTime(readDateProperty(node, EXO_LAST_LOGIN_TIME));
      user.setEmail(readStringProperty(node, EXO_EMAIL));
      user.setPassword(readStringProperty(node, EXO_PASSWORD));
      user.setFirstName(readStringProperty(node, EXO_FIRST_NAME));
      user.setLastName(readStringProperty(node, EXO_LAST_NAME));
      return user;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not read user properties", e);
    }
  }

  /**
   * Write user properties to the node
   * 
   * @param usr
   *          The user
   * @param node
   *          The node in the storage
   * @throws Exception
   *           An exception is thrown if method can not get access to the database
   */
  private void writeObjectToNode(User user, Node node) throws Exception {
    try {
      Calendar calendar = null;
      node.setProperty(EXO_EMAIL, user.getEmail());
      node.setProperty(EXO_FIRST_NAME, user.getFirstName());
      node.setProperty(EXO_LAST_NAME, user.getLastName());
      node.setProperty(EXO_PASSWORD, user.getPassword());

      if (user.getLastLoginTime() == null) {
        node.setProperty(EXO_LAST_LOGIN_TIME, calendar);
      } else {
        calendar = Calendar.getInstance();
        calendar.setTime(user.getLastLoginTime());
        node.setProperty(EXO_LAST_LOGIN_TIME, calendar);
      }

      calendar = Calendar.getInstance();
      calendar.setTime(user.getCreatedDate());
      node.setProperty(EXO_CREATED_DATE, calendar);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not write user properties", e);
    }
  }

  /**
   * PreSave Event.
   * 
   * @param user
   *          The user to save
   * @param isNew
   *          It is new user or not
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void preSave(User user, boolean isNew) throws Exception {
    for (UserEventListener listener : listeners)
      listener.preSave(user, isNew);
  }

  /**
   * PostSave Event.
   * 
   * @param user
   *          The user to save
   * @param isNew
   *          It is new user or not
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void postSave(User user, boolean isNew) throws Exception {
    for (UserEventListener listener : listeners)
      listener.postSave(user, isNew);
  }

  /**
   * PreDelete Event.
   * 
   * @param user
   *          The user to delete
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void preDelete(User user) throws Exception {
    for (UserEventListener listener : listeners)
      listener.preDelete(user);
  }

  /**
   * PostDelete Event.
   * 
   * @param user
   *          The user to delete
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void postDelete(User user) throws Exception {
    for (UserEventListener listener : listeners)
      listener.postDelete(user);
  }

  /**
   * ReplaceAsterix replace char '*' to '%' from start and end if string starts and ends with '*'.
   * 
   * @param str
   *          String to replace char
   * @return String with replaced chars or the same string
   */
  private String replaceAsterix(String str) {
    if (str.startsWith("*")) {
      str = "%" + str.substring(1);
    }
    if (str.endsWith("*")) {
      str = str.substring(0, str.length() - 1) + "%";
    }
    return str;
  }

  /**
   * RemoveAsterix remove char '*' from start and end if string starts and ends with '*'.
   * 
   * @param str
   *          String to remove char
   * @return String with removed chars or the same string
   */
  private String removeAsterix(String str) {
    if (str.startsWith("*")) {
      str = str.substring(1);
    }
    if (str.endsWith("*")) {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }
}
