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
public class MembershipHandlerImpl extends CommonHandler implements MembershipHandler {

  public static final String                    EXO_GROUP                   = "exo:group";

  public static final String                    EXO_MEMBERSHIP_TYPE         = "exo:membershipType";

  public static final String                    STORAGE_EXO_USER_MEMBERSHIP = "exo:userMembership";

  protected final List<MembershipEventListener> listeners                   = new ArrayList<MembershipEventListener>();

  /**
   * Organization service implementation covering the handler.
   */
  protected final JCROrganizationServiceImpl    service;

  /**
   * MembershipHandlerImpl constructor.
   * 
   * @param service
   *          The initialization data
   */
  MembershipHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /**
   * {@inheritDoc}
   */
  public void addMembershipEventListener(MembershipEventListener listener) {
    listeners.add(listener);
  }

  /**
   * Create membership.
   * 
   * @deprecated This method should not be uses.
   * @param m
   *          The membership to create
   * @param broadcast
   *          Broadcast the event if the value of the broadcast is 'true'
   */
  public void createMembership(Membership m, boolean broadcast) throws Exception {
  }

  /**
   * {@inheritDoc}
   */
  public Membership createMembershipInstance() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Membership findMembership(String id) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node mNode = session.getNodeByUUID(id);
      return readObjectFromNode(mNode);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership by UUId", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception {
    Session session = service.getStorageSession();
    try {
      Membership membership = null;

      String groupName = groupId.substring(groupId.lastIndexOf('/') + 1);
      String groupUUId = getGroupReferenceByName(groupName);
      String membershipTypeUUId = getMembershipTypeReferenceByName(type);

      String mStatement = "select * from exo:userMembership where exo:group='" + groupUUId
          + "' and exo:membershipType='" + membershipTypeUUId + "'";
      Query mQuery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(mStatement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        String uStatement = "select * from exo:user where exo:membeship='" + mNode.getUUID() + "'";
        Query uQuery = service.getStorageSession()
                              .getWorkspace()
                              .getQueryManager()
                              .createQuery(uStatement, Query.SQL);
        QueryResult uRes = uQuery.execute();
        for (NodeIterator uNodes = uRes.getNodes(); uNodes.hasNext();) {
          Node uNode = uNodes.nextNode();

          if (uNode.getName().equals(userName)) {
            if (membership != null) {
              throw new OrganizationServiceException("More than one membership is found");
            }
            membership = new MembershipImpl(mNode.getUUID(), userName, groupId, type);
          }
        }
      }

      return membership;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership type", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findMembershipsByGroup(Group group) throws Exception {
    Session session = service.getStorageSession();

    try {
      List<Membership> types = new ArrayList<Membership>();
      String groupUUId = getGroupReferenceByName(group.getGroupName());

      String statement = "select * from exo:userMembership where exo:group='" + groupUUId + "'";
      Query mQuery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(statement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        types.add(readObjectFromNode(mNode));
      }
      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership by group", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findMembershipsByUser(String userName) throws Exception {
    Session session = service.getStorageSession();
    try {
      List<Membership> types = new ArrayList<Membership>();

      // find membership
      String mStatement = "select * from exo:userMembership";
      Query mQuery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(mStatement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        String uStatement = "select * from exo:user where exo:membership='" + mNode.getUUID() + "'";
        Query uQuery = service.getStorageSession()
                              .getWorkspace()
                              .getQueryManager()
                              .createQuery(uStatement, Query.SQL);
        QueryResult uRes = uQuery.execute();
        for (NodeIterator uNodes = uRes.getNodes(); uNodes.hasNext();) {
          Node uNode = uNodes.nextNode();

          // check username
          if (uNode.getName().equals(userName)) {
            types.add(readObjectFromNode(mNode));
          }
        }
      }
      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership by user " + userName, e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception {
    Session session = service.getStorageSession();
    try {
      String groupName = groupId.substring(groupId.lastIndexOf('/') + 1);
      String groupUUId = getGroupReferenceByName(groupName);

      List<Membership> types = new ArrayList<Membership>();

      // find membership
      String mStatement = "select * from exo:membership where exo:group='" + groupUUId + "'";
      Query mQuery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(mStatement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        String uStatement = "select * from exo:user where exo:membership='" + mNode.getUUID() + "'";
        Query uQuery = service.getStorageSession()
                              .getWorkspace()
                              .getQueryManager()
                              .createQuery(uStatement, Query.SQL);
        QueryResult uRes = uQuery.execute();
        for (NodeIterator uNodes = uRes.getNodes(); uNodes.hasNext();) {
          Node uNode = uNodes.nextNode();

          // check username
          if (uNode.getName().equals(userName)) {
            types.add(readObjectFromNode(mNode));
          }
        }
      }
      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership by user " + userName
          + " and group " + groupId, e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void linkMembership(User user, Group group, MembershipType m, boolean broadcast) throws Exception {
    // TODO Implement broadcast
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + "/"
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + user.getUserName());

      Membership membership = new MembershipImpl(null,
                                                 user.getUserName(),
                                                 group.getId(),
                                                 m.getName());
      Node mNode = uNode.addNode(STORAGE_EXO_USER_MEMBERSHIP);
      writeObjectToNode(membership, mNode);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not link membership with user " + user.getEmail()
          + " group " + group.getGroupName() + " and membership type " + m.getName(), e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Membership removeMembership(String id, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      Node mNode = session.getNodeByUUID(id);
      Membership membership = readObjectFromNode(mNode);
      mNode.remove();
      session.save();
      return membership;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership by UUId", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection removeMembershipByUser(String userName, boolean broadcast) throws Exception {
    // TODO broadcast
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + "/"
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName);

      List<Membership> types = new ArrayList<Membership>();
      types = (List) findMembershipsByUser(userName);

      for (NodeIterator mNodes = uNode.getNodes(STORAGE_EXO_USER_MEMBERSHIP); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        mNode.remove();
      }

      session.save();
      return types;

    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user " + userName
          + " for remove membership.");
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove membership by user " + userName, e);
    } finally {
      session.logout();
    }
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
   * Get membership type UUID from the name.
   * 
   * @param type
   *          The membership type
   * @return The membership type UUId in the storage
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  private String getMembershipTypeReferenceByName(String type) throws Exception {
    try {
      String Statement = "select * from exo:membershipType where jcr:name=" + type;
      Query query = service.getStorageSession()
                           .getWorkspace()
                           .getQueryManager()
                           .createQuery(Statement, Query.SQL);
      QueryResult res = query.execute();
      if (!res.getNodes().hasNext()) {
        throw new OrganizationServiceException("Can not find membership type " + type);
      }
      return res.getNodes().nextNode().getUUID();

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership type " + type, e);
    }
  }

  /**
   * Get group UUID from the name of the group.
   * 
   * @param groupName
   *          The name of the group
   * @return The group UUId in the storage
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  private String getGroupReferenceByName(String groupName) throws Exception {
    try {
      String Statement = "select * from exo:group where jcr:name=" + groupName;
      Query query = service.getStorageSession()
                           .getWorkspace()
                           .getQueryManager()
                           .createQuery(Statement, Query.SQL);
      QueryResult res = query.execute();
      if (!res.getNodes().hasNext()) {
        throw new OrganizationServiceException("Can not find group " + groupName);
      }
      return res.getNodes().nextNode().getUUID();

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group " + groupName, e);
    }
  }

  /**
   * Get membership type name from the UUId.
   * 
   * @param UUId
   *          The UUId of the group in the storage
   * @return The membership type name
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  private String getMembershipTypeNameByReference(String UUId) throws Exception {
    try {
      String Statement = "select * from exo:membershipType where jcr:uuid='" + UUId + "'";
      Query query = service.getStorageSession()
                           .getWorkspace()
                           .getQueryManager()
                           .createQuery(Statement, Query.SQL);
      QueryResult res = query.execute();
      if (!res.getNodes().hasNext()) {
        throw new OrganizationServiceException("Can not find membership type by uuid " + UUId);
      }
      return res.getNodes().nextNode().getName();

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership type by uuid " + UUId, e);
    }
  }

  /**
   * Get group id from the UUId.
   * 
   * @param UUId
   *          The UUId of the group in the storage
   * @return The group id of the group
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  private String getGroupIdByReference(String UUId) throws Exception {
    try {
      String Statement = "select * from exo:group where jcr:uuid='" + UUId + "'";
      Query query = service.getStorageSession()
                           .getWorkspace()
                           .getQueryManager()
                           .createQuery(Statement, Query.SQL);
      QueryResult res = query.execute();
      if (!res.getNodes().hasNext()) {
        throw new OrganizationServiceException("Can not find group by uuid " + UUId);
      }
      Node gNode = res.getNodes().nextNode();
      return readStringProperty(gNode, GroupHandlerImpl.EXO_GROUP_ID);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group by uuid " + UUId, e);
    }
  }

  /**
   * Read membership properties from the node in the storage.
   * 
   * @param node
   *          The node to read from
   * @return The membership
   * @throws Exception
   *           An Exception is thrown if method can not get access to the database
   */
  private Membership readObjectFromNode(Node node) throws Exception {
    try {
      String groupUUId = readStringProperty(node, EXO_GROUP);
      String membershipTypeUUId = readStringProperty(node, EXO_MEMBERSHIP_TYPE);

      String groupId = getGroupIdByReference(groupUUId);
      String membershipType = getMembershipTypeNameByReference(membershipTypeUUId);
      String userName = node.getParent().getName();

      return new MembershipImpl(node.getUUID(), userName, groupId, membershipType);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not read membership properties", e);
    }
  }

  /**
   * Write membership properties to the node in the storage.
   * 
   * @param m
   *          The membership
   * @param node
   *          The node to write in
   * @throws Exception
   *           An Exception is thrown if method can not get access to the database
   */
  private void writeObjectToNode(Membership m, Node node) throws Exception {
    try {
      String groupName = m.getGroupId().substring(m.getGroupId().lastIndexOf('/') + 1);
      String groupUUId = getGroupReferenceByName(groupName);
      String membershipTypeUUId = getMembershipTypeReferenceByName(m.getMembershipType());

      node.setProperty(EXO_GROUP, groupUUId);
      node.setProperty(EXO_MEMBERSHIP_TYPE, membershipTypeUUId);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not write membership properties", e);
    }
  }
}
