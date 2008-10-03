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
   * @see org.exoplatform.services.organization.UserHandler#addUserEventListener(org.exoplatform.services.organization.UserEventListener)
   */
  public void addUserEventListener(UserEventListener listener) {
    listeners.add(listener);
  }

  /**
   * @see org.exoplatform.services.organization.UserHandler#authenticate(java.lang .String,
   *      java.lang.String)
   */
  public boolean authenticate(String username, String password) throws Exception {
    User user = findUserByName(username);
    return user.getPassword().equals(password);
  }

  /**
   * @see org.exoplatform.services.organization.UserHandler#createUser(org.exoplatform
   *      .services.organization.User, boolean)
   */
  public void createUser(User user, boolean broadcast) throws Exception {
    // TODO Implement broadcast
    Session session = service.getStorageSession();
    try {
      Node uNode = ((Node) session.getItem(service.getStoragePath() + STORAGE_EXO_USERS)).addNode(user.getUserName());

      Calendar calendar = Calendar.getInstance();
      uNode.setProperty(STORAGE_EXO_EMAIL, user.getEmail());
      uNode.setProperty(STORAGE_EXO_FIRST_NAME, user.getFirstName());
      uNode.setProperty(STORAGE_EXO_LAST_NAME, user.getLastName());
      uNode.setProperty(STORAGE_EXO_PASSWORD, user.getPassword());
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
   * @see org.exoplatform.services.organization.UserHandler#createUserInstance()
   */
  public User createUserInstance() {
    return new UserImpl();
  }

  /**
   * @see org.exoplatform.services.organization.UserHandler#createUserInstance(java .lang.String)
   */
  public User createUserInstance(String username) {
    return new UserImpl(username);
  }

  /**
   * @see org.exoplatform.services.organization.UserHandler#findUserByName(java.lang .String)
   */
  public User findUserByName(String userName) throws Exception {
    Session session = service.getStorageSession();
    try {
      User user = null;
      String userPath = service.getStoragePath() + STORAGE_EXO_USERS + "/" + userName;
      if (!session.itemExists(userPath)) {
        return user;
      }

      Node storagePath = (Node) session.getItem(userPath);
      for (NodeIterator nodes = storagePath.getNodes(); nodes.hasNext();) {
        if (user != null) {
          throw new OrganizationServiceException("More than one user " + userName + " is found");
        }
        Node uNode = nodes.nextNode();
        user = createUserInstance(userName);
        user.setCreatedDate(uNode.getProperty(STORAGE_EXO_CREATED_DATE).getDate().getTime());
        user.setLastLoginTime(uNode.getProperty(STORAGE_EXO_LAST_LOGIN_TIME).getDate().getTime());
        user.setEmail(uNode.getProperty(STORAGE_EXO_EMAIL).getString());
        user.setPassword(uNode.getProperty(STORAGE_EXO_PASSWORD).getString());
        user.setFirstName(uNode.getProperty(STORAGE_EXO_FIRST_NAME).getString());
        user.setLastName(uNode.getProperty(STORAGE_EXO_LAST_NAME).getString());
      }
      return user;
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
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
   * @see org.exoplatform.services.organization.UserHandler#findUsersByGroup(java .lang.String)
   */
  public PageList findUsersByGroup(String groupId) throws Exception {
    List<User> users = new ArrayList<User>();

    // find group
    String statement = "select * from exo:group where jcr:uuid='" + groupId + "'";
    Query gquery = service.getStorageSession()
                          .getWorkspace()
                          .getQueryManager()
                          .createQuery(statement, Query.SQL);
    QueryResult gres = gquery.execute();
    if (gres.getNodes().hasNext()) {
      // has group
      Node groupNode = gres.getNodes().nextNode();

      // find memberships
      statement = "select * from exo:userMembership where exo:group='" + groupNode.getUUID() + "'";
      Query mquery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(statement, Query.SQL);
      QueryResult mres = mquery.execute();
      for (NodeIterator membs = mres.getNodes(); membs.hasNext();) {
        Node membership = membs.nextNode();
        Node userNode = membership.getParent();
        User user = findUserByName(userNode.getName());
        users.add(user);
      }
    }

    return new ObjectPageList(users, 10);
  }

  /**
   * @see org.exoplatform.services.organization.UserHandler#getUserPageList(int)
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
   * @see org.exoplatform.services.organization.UserHandler#removeUser(java.lang. String, boolean)
   */
  public User removeUser(String userName, boolean broadcast) throws Exception {
    // TODO implement broadcast
    // TODO get user
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_USERS + "/"
          + userName);
      User user = new UserImpl();
      uNode.remove();
      session.save();
      return user;
    } finally {
      session.logout();
    }
  }

  public void removeUserEventListener(UserEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUser(User user, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_USERS + "/"
          + user.getUserName());

      Calendar calendar = Calendar.getInstance();
      uNode.setProperty(STORAGE_EXO_EMAIL, user.getEmail());
      uNode.setProperty(STORAGE_EXO_FIRST_NAME, user.getFirstName());
      uNode.setProperty(STORAGE_EXO_LAST_NAME, user.getLastName());
      uNode.setProperty(STORAGE_EXO_PASSWORD, user.getPassword());
      calendar.setTime(user.getCreatedDate());
      uNode.setProperty(STORAGE_EXO_CREATED_DATE, calendar);
      calendar.setTime(user.getLastLoginTime());
      uNode.setProperty(STORAGE_EXO_LAST_LOGIN_TIME, calendar);

      session.save();
    } finally {
      session.logout();
    }
  }

}
