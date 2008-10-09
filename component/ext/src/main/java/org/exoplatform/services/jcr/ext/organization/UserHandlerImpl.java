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

  public static final String                 EXO_PROFILE = "exo:profile";

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
    return (user != null ? user.getPassword().equals(password) : false);
  }

  /**
   * {@inheritDoc}
   */
  public void createUser(User user, boolean broadcast) throws Exception {
    // TODO Implement broadcast

    checkMandatoryProperties(user);

    Session session = service.getStorageSession();
    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_USERS);
      Node uNode = storageNode.addNode(user.getUserName());
      writeObjectToNode(user, uNode);
      session.save();
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
      return (User) readObjectFromNode(uNode);
    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find user " + userName, e);
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
      where.concat((where.length() == 0 ? "" : " AND ") + "jcr:name LIKE " + query.getUserName());
    }
    if (query.getEmail() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + EXO_EMAIL + " LIKE " + query.getEmail());
    }
    if (query.getFirstName() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + EXO_FIRST_NAME + " LIKE "
          + query.getFirstName());
    }
    if (query.getLastName() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + EXO_LAST_NAME + " LIKE "
          + query.getLastName());
    }
    if (query.getFromLoginDate() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + EXO_LAST_LOGIN_TIME + ">="
          + query.getFromLoginDate());
    }
    if (query.getToLoginDate() != null) {
      where.concat((where.length() == 0 ? "" : " AND ") + EXO_LAST_LOGIN_TIME + "<="
          + query.getToLoginDate());
    }

    List<User> types = new ArrayList<User>();

    String statement = "select * from exo:user " + (where.length() == 0 ? "" : " where " + where);
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
   * {@inheritDoc}
   */
  public PageList findUsersByGroup(String groupId) throws Exception {
    List<User> users = new ArrayList<User>();
    String groupName = groupId.substring(groupId.lastIndexOf('/') + 1);
    Session session = service.getStorageSession();
    try {
      // get UUId of the group
      Node gNode = (Node) session.getItem(service.getStoragePath() + "/"
          + GroupHandlerImpl.STORAGE_EXO_GROUPS + "/" + groupName);

      // find group
      String statement = "select * from " + MembershipHandlerImpl.EXO_GROUP + " where jcr:uuid='"
          + gNode.getUUID() + "'";
      Query gquery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(statement, Query.SQL);
      QueryResult gres = gquery.execute();
      if (gres.getNodes().hasNext()) {
        // has group
        Node groupNode = gres.getNodes().nextNode();

        // find memberships
        statement = "select * from " + MembershipHandlerImpl.STORAGE_EXO_USER_MEMBERSHIP
            + " where " + MembershipHandlerImpl.EXO_GROUP + "='" + groupNode.getUUID() + "'";
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

    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find group " + groupName, e);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group ", e);
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
      Node storageNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_USERS);
      for (NodeIterator uNodes = storageNode.getNodes(); uNodes.hasNext();) {
        Node uNode = uNodes.nextNode();
        User user = findUserByName(uNode.getName());
        if (user != null) {
          types.add(user);
        }
      }
      return new ObjectPageList(types, 10);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group ", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public User removeUser(String userName, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_USERS + "/"
          + userName);
      User user = findUserByName(userName);
      uNode.remove();
      session.save();
      return user;

    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user " + userName + " for delete");
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group ", e);
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
    // TODO implement broadcast
    checkMandatoryProperties(user);

    Session session = service.getStorageSession();
    try {
      UserImpl uImpl = (UserImpl) user;
      if (uImpl.getUUId() == null) {
        throw new OrganizationServiceException("Can not find user for save changes because UUId is null.");
      }

      try {
        Node uNode = session.getNodeByUUID(uImpl.getUUId());
        String srcPath = uNode.getPath();
        int pos = srcPath.lastIndexOf('/');
        String prevName = srcPath.substring(pos + 1);
        String destPath = srcPath.substring(0, pos) + "/" + user.getUserName();

        try {
          if (!prevName.equals(user.getUserName())) {
            session.move(srcPath, destPath);
          }
          try {
            Node nmtNode = (Node) session.getItem(destPath);
            writeObjectToNode(user, nmtNode);
            session.save();
          } catch (PathNotFoundException e) {
            throw new OrganizationServiceException("The membership type " + user.getUserName()
                + " is absent and can not be save", e);
          }
        } catch (PathNotFoundException e) {
          throw new OrganizationServiceException("The membership type " + prevName
              + " is absent and can not be save", e);
        } catch (ItemExistsException e) {
          throw new OrganizationServiceException("Can not save membership type " + prevName
              + " because new membership type " + user.getUserName() + " is exist", e);
        }

      } catch (ItemNotFoundException e) {
        throw new OrganizationServiceException("Can not find membership type for save changes by UUId",
                                               e);
      }
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not save membership type", e);
    } finally {
      session.logout();
    }

  }

  private void checkMandatoryProperties(Object obj) throws Exception {
    User user = (User) obj;
    if (user.getUserName() == null || user.getUserName().length() == 0) {
      throw new OrganizationServiceException("Can not create user without name.");
    } else if (user.getFirstName() == null || user.getFirstName().length() == 0) {
      throw new OrganizationServiceException("Can not create user without first name.");
    } else if (user.getLastName() == null || user.getLastName().length() == 0) {
      throw new OrganizationServiceException("Can not create user without last name.");
    } else if (user.getPassword() == null || user.getPassword().length() == 0) {
      throw new OrganizationServiceException("Can not create user without password.");
    } else if (user.getCreatedDate() == null) {
      throw new OrganizationServiceException("Can not create user without created date.");
    }

  }

  private Object readObjectFromNode(Node node) throws Exception {
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

  private void writeObjectToNode(Object obj, Node node) throws Exception {
    User user = (User) obj;

    try {
      Calendar calendar = Calendar.getInstance();
      node.setProperty(EXO_EMAIL, user.getEmail());
      node.setProperty(EXO_FIRST_NAME, user.getFirstName());
      node.setProperty(EXO_LAST_NAME, user.getLastName());
      node.setProperty(EXO_PASSWORD, user.getPassword());

      // TODO is it correct?
      calendar.setTime(user.getCreatedDate());
      node.setProperty(EXO_CREATED_DATE, calendar);
      calendar.setTime(user.getLastLoginTime());
      node.setProperty(EXO_LAST_LOGIN_TIME, calendar);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not write user properties", e);
    }
  }
}
