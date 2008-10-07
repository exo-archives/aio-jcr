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

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS.
 * 
 * NOTE: Check if nodetypes and/or existing interfaces of API don't relate one to other. Date:
 * 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class MembershipHandlerImpl implements MembershipHandler {

  public static final String                    STORAGE_EXO_USER_MEMBERSHIP = "exo:userMembership";

  public static final String                    STORAGE_EXO_MEMBERSHIP_TYPE = "exo:membershipType";

  public static final String                    STORAGE_EXO_GROUP           = "exo:group";

  protected final JCROrganizationServiceImpl    service;

  protected final List<MembershipEventListener> listeners                   = new ArrayList<MembershipEventListener>();

  MembershipHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /**
   * Use this method to register a membership event listener.
   * 
   * @param listener
   *          the listener instance.
   */
  public void addMembershipEventListener(MembershipEventListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove registered listener
   * 
   * @param listener
   *          The registered listener
   */
  public void removeMembershipEventListener(MembershipEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * @deprecated This method should no be called, use the linkMembership(..) instead
   */
  public void createMembership(Membership m, boolean broadcast) throws Exception {
  }

  /**
   * @deprecated This method should not be called, use the linkMembership instead.
   */
  public Membership createMembershipInstance() {
    return null;
  }

  /**
   * Use this method to search for an membership record with the given id
   * 
   * @param id
   *          The id of the membership
   * @return Return The membership object that matched the id
   * @throws Exception
   *           An exception is throwed if the method fail to access the database or no membership is
   *           found.
   */
  public Membership findMembership(String id) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node mNode = session.getNodeByUUID(id);
      try {
        Node uNode = mNode.getParent();
        String groupId = mNode.getProperty(STORAGE_EXO_GROUP).toString();
        String membershipType = mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE).toString();
        Membership membership = new MembershipImpl(uNode.getName(), groupId, membershipType);
        return membership;
      } finally {
        // TODO ???
      }
    } catch (ItemNotFoundException e) {
      // TODO put cause exception into OrganizationServiceException
      throw new OrganizationServiceException("Can not find membership by UUId"); 
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to search for a specific membership type of an user in a group.
   * 
   * @param userName
   *          The username of the user.
   * @param groupId
   *          The group identifier
   * @param type
   *          The membership type
   * @return Null if no such memberhsip record or a membership object.
   * @throws Exception
   *           Usually an exception is thrown if the method cannot access the database
   */
  public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName);

      try {
        // TODO Query will be better?
        for (NodeIterator mNodes = uNode.getNodes(STORAGE_EXO_USER_MEMBERSHIP); mNodes.hasNext();) {
          Node mNode = mNodes.nextNode();
          String group = mNode.getProperty(STORAGE_EXO_GROUP).toString();
          String membershipType = mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE).toString();
          if (group.equals(groupId)) {
            Node mtNode = session.getNodeByUUID(membershipType);
            if (mtNode.getName().equals(type)) {
              return new MembershipImpl(userName, groupId, membershipType);
            }
          }
        }
        return null;
      } catch (ItemNotFoundException e) {
        throw new OrganizationServiceException("Can not find membership type");
      }
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user " + userName);
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to find all the membership in a group. Note that an user can have more than one
   * membership in a group. For example , user admin can have memberhsip 'member' and 'admin' in the
   * group '/users'
   * 
   * @param group
   * @return A collection of the memberships. The collection cannot be none and empty if no
   *         membership is found.
   * @throws Exception
   */
  public Collection findMembershipsByGroup(Group group) throws Exception {

    Session session = service.getStorageSession();

    try {
      List<Membership> types = new ArrayList<Membership>();

      
      String statement = "select * from " + STORAGE_EXO_USER_MEMBERSHIP; // TODO ...where exo:group='group_id'
      Query mquery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(statement, Query.SQL);
      QueryResult mres = mquery.execute();
      for (NodeIterator mNodes = mres.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        String groupId = mNode.getProperty(STORAGE_EXO_GROUP).toString();
        Node gNode = session.getNodeByUUID(groupId);
        if (gNode.getName().equals(group.getGroupName())) {
          Node uNode = mNode.getParent();
          Membership membership = new MembershipImpl(uNode.getName(),
                                                     groupId,
                                                     mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE)
                                                          .toString());
          types.add(membership);
        }
      }

      return types;
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to find all the memberships of an user in any group.
   * 
   * @param userName
   * @return A collection of the memebership. The collection cannot be null and if no membership is
   *         found , the collection should be empty
   * @throws Exception
   *           Usually an exception is throwed if the method cannot access the database.
   */
  public Collection findMembershipsByUser(String userName) throws Exception {
    Session session = service.getStorageSession();
    try {
      List<Membership> types = new ArrayList<Membership>();

      // TODO userNode not a storageNode
      Node storageNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName);

      try {
        for (NodeIterator nodes = storageNode.getNodes(STORAGE_EXO_USER_MEMBERSHIP); nodes.hasNext();) {
          Node mNode = nodes.nextNode();
          String groupId = mNode.getProperty(STORAGE_EXO_GROUP).toString();
          String membershipType = mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE).toString();
          Membership membership = new MembershipImpl(userName, groupId, membershipType);
          types.add(membership);
        }
      } finally {
      }
      return types;
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user " + userName);
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to find all the memberships of an user in a group
   * 
   * @param userName
   * @param groupId
   * @return A collection of the membership of an user in a group. The collection cannot be null and
   *         the collection should be empty is no membership is found
   * @throws Exception
   *           Usually an exception is thrown if the method cannot access the database.
   */
  public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName);

      try {
        // TODO use query
        
        List<Membership> types = new ArrayList<Membership>();

        for (NodeIterator nodes = storageNode.getNodes(STORAGE_EXO_USER_MEMBERSHIP); nodes.hasNext();) {
          Node mNode = nodes.nextNode();
          if (mNode.getProperty(STORAGE_EXO_GROUP).toString().equals(groupId)) {
            String membershipType = mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE).toString();
            Membership membership = new MembershipImpl(userName, groupId, membershipType);
            types.add(membership);
          }
        }
        return types;
      } finally {
        // TODO ???
      }
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user " + userName);
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to create a membership record, a relation of the user , group and membership
   * type
   * 
   * @param user
   *          The user of the membership
   * @param group
   *          The group of the membership
   * @param m
   *          The MembershipType of the membership
   * @param broadcast
   *          Broadcast the event if the value of the broadcast is 'true'
   * @throws Exception
   *           An exception is throwed if the method is fail to access the database, a membership
   *           record with the same user , group and membership type existed or any listener fail to
   *           handle the event.
   */
  public void linkMembership(User user, Group group, MembershipType m, boolean broadcast) throws Exception {
    // TODO Implement broadcast
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + user.getUserName());

      try {
        Node gNode = (Node) session.getItem(service.getStoragePath()
            + GroupHandlerImpl.STORAGE_EXO_GROUPS + group.getParentId() + "/"
            + group.getGroupName());

        try {
          Node mtNode = (Node) session.getItem(service.getStoragePath()
              + MembershipTypeHandlerImpl.STORAGE_EXO_MEMBERSHIP_TYPES + "/" + m.getName());
          try {
            Node mNode = uNode.addNode(STORAGE_EXO_USER_MEMBERSHIP);
            mNode.setProperty(STORAGE_EXO_GROUP, gNode.getUUID());
            mNode.setProperty(STORAGE_EXO_MEMBERSHIP_TYPE, mtNode.getUUID());
          } finally {
            // TODO ???
          }
        } catch (PathNotFoundException e) {
          throw new OrganizationServiceException("Can not membership type " + m.getName()); // TODO ,e
        }
      } catch (PathNotFoundException e) {
        throw new OrganizationServiceException("Can not find group " + group.getParentId() + "/"
            + group.getGroupName());
      }
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user " + user.getUserName());
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to remove a membership. Usually you need to call the method
   * findMembershipByUserGroupAndType(..) to find the membership and remove.
   * 
   * @param id
   *          The id of the membership
   * @param broadcast
   *          Broadcast the event to the registered listeners if the broadcast event is 'true'
   * @return The membership object which has been removed from the database
   * @throws Exception
   *           An exception is throwed if the method cannot access the database or any listener fail
   *           to handle the event.
   */
  public Membership removeMembership(String id, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      Node mNode = session.getNodeByUUID(id);

      try {
        // get user
        Node uNode = mNode.getParent();
        String groupId = mNode.getProperty(STORAGE_EXO_GROUP).toString();
        String membershipType = mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE).toString();
        Membership membership = new MembershipImpl(uNode.getName(), groupId, membershipType);
        mNode.remove();
        session.save();
        return membership;
      } finally {
        // TODO
      }
    } catch (ItemNotFoundException e) {
      throw new OrganizationServiceException("Can not find membership by UUId.");
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to remove all user's membership.
   * 
   * @param username
   *          The username which user object need remove memberships
   * @param broadcast
   *          Broadcast the event to the registered listeners if the broadcast event is 'true'
   * @return The membership object which has been removed from the database
   * @throws Exception
   *           An exception is throwed if the method cannot access the database or any listener fail
   *           to handle the event.
   */
  public Collection removeMembershipByUser(String userName, boolean broadcast) throws Exception {
    // TODO broadcast
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName);
      Node mNode = uNode.getNode(STORAGE_EXO_USER_MEMBERSHIP); // TODO uNode.getNodes(...

      List<Membership> types = new ArrayList<Membership>();
      types = (List) findMembershipsByUser(userName);
      mNode.remove();
      session.save();
      return types;
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user " + userName
          + " for remove membership.");

    } finally {
      session.logout();
    }
  }
}
