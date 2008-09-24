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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserHandler;

/**
 * Created by The eXo Platform SAS
 * 
 * Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: UserHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class UserHandlerImpl implements UserHandler {

  public PageList findUsers(org.exoplatform.services.organization.Query query) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  protected final JCROrganizationServiceImpl service;

  protected final List<UserEventListener>    listeners = new ArrayList<UserEventListener>();

  UserHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  public void addUserEventListener(UserEventListener listener) {
    listeners.add(listener);
  }

  public void removeUserEventListener(UserEventListener listener) {
    listeners.remove(listener);
  }

  public boolean authenticate(String username, String password) throws OrganizationServiceException {
    return false; // TODO
  }

  public void createUser(User user, boolean broadcast) throws OrganizationServiceException {
    // TODO Auto-generated method stub

  }

  public User createUserInstance() {
    // TODO Auto-generated method stub
    return null;
  }

  public User createUserInstance(String username) {
    // TODO Auto-generated method stub
    return null;
  }

  public User findUserByName(String userName) throws OrganizationServiceException {
    // TODO Auto-generated method stub
    return null;
  }

  public PageList findUsersByGroup(String groupId) throws OrganizationServiceException {
    // TODO
    try {
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

          UserImpl user = new UserImpl();
          // TODO fill user from userNode

          users.add(user);
        }
      }

      return new ObjectPageList(users, 10);
    } catch (RepositoryException e) {
      throw new OrganizationServiceException("Query error " + e, e);
    }
  }

  public PageList getUserPageList(int pageSize) throws OrganizationServiceException {
    // TODO Auto-generated method stub
    return null;
  }

  public User removeUser(String userName, boolean broadcast) throws OrganizationServiceException {
    // TODO Auto-generated method stub
    return null;
  }

  public void saveUser(User user, boolean broadcast) throws OrganizationServiceException {
    // TODO Auto-generated method stub

  }

}
