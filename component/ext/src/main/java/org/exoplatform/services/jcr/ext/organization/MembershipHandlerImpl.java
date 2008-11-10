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

import org.apache.commons.logging.Log;

import org.exoplatform.services.log.ExoLogger;
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

  /**
   * The membership type property that contain reference to linked group.
   */
  public static final String                    EXO_GROUP           = "exo:group";

  /**
   * The membership type property that contain reference to linked membership type.
   */
  public static final String                    EXO_MEMBERSHIP_TYPE = "exo:membershipType";

  /**
   * The list of listeners to broadcast the events.
   */
  protected final List<MembershipEventListener> listeners           = new ArrayList<MembershipEventListener>();

  /**
   * Organization service implementation covering the handler.
   */
  protected final JCROrganizationServiceImpl    service;

  /**
   * Log.
   */
  protected static Log                          log                 = ExoLogger.getLogger("jcr.MembershipHandlerImpl");

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
   * {@inheritDoc}
   */
  public void createMembership(Membership m, boolean broadcast) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("createMembership");
    }

    Session session = service.getStorageSession();
    try {
      Group g = ((GroupHandlerImpl) service.getGroupHandler()).findGroupById(session,
                                                                             m.getGroupId());
      User u = service.getUserHandler().findUserByName(m.getUserName());

      MembershipType mt = service.getMembershipTypeHandler().createMembershipTypeInstance();
      mt.setName(m.getMembershipType());

      linkMembership(u, g, mt, broadcast);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not create membership", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Membership createMembershipInstance() {
    if (log.isDebugEnabled()) {
      log.debug("createMembershipInstance");
    }

    return new MembershipImpl();
  }

  /**
   * {@inheritDoc}
   */
  public Membership findMembership(String id) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("findMembership");
    }

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
    if (log.isDebugEnabled()) {
      log.debug("findMembershipByUserGroupAndType");
    }

    Session session = service.getStorageSession();
    try {
      Membership membership = null;

      String groupUUId = getGroupUUID(groupId);
      if (groupUUId != null) {

        String membershipTypeUUId = getMembershipTypeUUID(type);
        if (membershipTypeUUId != null) {

          String mStatement = "select * from exo:userMembership where exo:group='" + groupUUId
              + "' and exo:membershipType='" + membershipTypeUUId + "'";
          Query mQuery = service.getStorageSession()
                                .getWorkspace()
                                .getQueryManager()
                                .createQuery(mStatement, Query.SQL);
          QueryResult mRes = mQuery.execute();
          for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
            Node mNode = mNodes.nextNode();
            Node uNode = mNode.getParent();

            if (uNode.getName().equals(userName)) {
              if (membership != null) {
                throw new OrganizationServiceException("More than one membership is found");
              }
              membership = new MembershipImpl(mNode.getUUID(), userName, groupId, type);
            }
          }
        }
      }

      return membership;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership type for user '" + userName
          + "' groupId '" + groupId + "' type '" + type + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findMembershipsByGroup(Group group) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("findMembershipByGroup");
    }

    Session session = service.getStorageSession();

    try {
      List<Membership> types = new ArrayList<Membership>();

      String groupUUID = getGroupUUID(group.getId());
      if (groupUUID != null) {

        String statement = "select * from exo:userMembership where exo:group='" + groupUUID + "'";
        Query mQuery = service.getStorageSession()
                              .getWorkspace()
                              .getQueryManager()
                              .createQuery(statement, Query.SQL);
        QueryResult mRes = mQuery.execute();
        for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
          Node mNode = mNodes.nextNode();
          types.add(readObjectFromNode(mNode));
        }
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
    if (log.isDebugEnabled()) {
      log.debug("findMembeshipByUser");
    }

    Session session = service.getStorageSession();
    try {
      List<Membership> types = new ArrayList<Membership>();

      String userPath = service.getStoragePath() + "/" + UserHandlerImpl.STORAGE_EXO_USERS + "/"
          + userName;

      if (!session.itemExists(userPath)) {
        return types;
      }

      // find membership
      String mStatement = "select * from exo:userMembership";
      Query mQuery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(mStatement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        Node uNode = mNode.getParent();

        // check username
        if (uNode.getName().equals(userName)) {
          types.add(readObjectFromNode(mNode));
        }
      }
      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership by user '" + userName + "'",
                                             e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("findMembershipByUserAndGroup");
    }

    Session session = service.getStorageSession();
    try {
      List<Membership> types = new ArrayList<Membership>();

      String groupUUId = getGroupUUID(groupId);
      if (groupUUId != null) {
        // find membership
        String mStatement = "select * from exo:userMembership where exo:group='" + groupUUId + "'";
        Query mQuery = service.getStorageSession()
                              .getWorkspace()
                              .getQueryManager()
                              .createQuery(mStatement, Query.SQL);
        QueryResult mRes = mQuery.execute();

        for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
          Node mNode = mNodes.nextNode();
          Node uNode = mNode.getParent();

          // check username
          if (uNode.getName().equals(userName)) {
            types.add(readObjectFromNode(mNode));
          }
        }
      }
      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership by user '" + userName
          + "' and group '" + groupId + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void linkMembership(User user, Group group, MembershipType m, boolean broadcast) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("linkMembership");
    }

    Session session = service.getStorageSession();
    try {
      // create membership type '*'
      if (m.getName().equals("*")
          && !session.itemExists(service.getStoragePath() + "/"
              + MembershipTypeHandlerImpl.STORAGE_EXO_MEMBERSHIP_TYPES + "/" + m.getName())) {

        MembershipType mt = service.getMembershipTypeHandler().createMembershipTypeInstance();
        mt.setName("*");
        mt.setDescription("any membership type");
        ((MembershipTypeHandlerImpl) service.getMembershipTypeHandler()).createMembershipType(session,
                                                                                              mt,
                                                                                              broadcast);
      }

      Node uNode = (Node) session.getItem(service.getStoragePath() + "/"
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + user.getUserName());

      Membership membership = new MembershipImpl(null,
                                                 user.getUserName(),
                                                 group.getId(),
                                                 m.getName());
      Node mNode = uNode.addNode(UserHandlerImpl.EXO_MEMBERSHIP);

      if (broadcast) {
        preSave(membership, true);
      }

      writeObjectToNode(membership, mNode);
      session.save();

      if (broadcast) {
        postSave(membership, true);
      }

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not link membership for user '"
          + user.getUserName() + "' group '" + group.getGroupName() + "' and membership type '"
          + m.getName() + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Membership removeMembership(String id, boolean broadcast) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("removeMembership");
    }

    Session session = service.getStorageSession();
    try {
      Node mNode = session.getNodeByUUID(id);
      Membership membership = readObjectFromNode(mNode);

      if (broadcast) {
        preDelete(membership);
      }

      mNode.remove();
      session.save();

      if (broadcast) {
        postDelete(membership);
      }

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
    if (log.isDebugEnabled()) {
      log.debug("removeMembershipByUser");
    }

    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath() + "/"
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName);

      List<Membership> types = new ArrayList<Membership>();
      types = (List) findMembershipsByUser(userName);

      for (NodeIterator mNodes = uNode.getNodes(UserHandlerImpl.EXO_MEMBERSHIP); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        Membership membership = readObjectFromNode(mNode);

        if (broadcast) {
          preDelete(membership);
        }

        mNode.remove();
      }

      session.save();
      for (int i = 0; i < types.size(); i++) {
        if (broadcast) {
          postDelete(types.get(i));
        }
      }

      return types;

    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find user '" + userName
          + "' for remove membership.");
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove membership by user '" + userName + "'",
                                             e);
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
   * Get membership type UUID by the name.
   * 
   * @param type
   *          The membership type
   * @return The membership type UUId in the storage
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  private String getMembershipTypeUUID(String type) throws Exception {
    Session session = service.getStorageSession();
    try {
      String mtPath = service.getStoragePath() + "/"
          + MembershipTypeHandlerImpl.STORAGE_EXO_MEMBERSHIP_TYPES;
      return (type != null && type.length() != 0 && session.itemExists(mtPath + "/" + type)
          ? ((Node) session.getItem(mtPath + "/" + type)).getUUID()
          : null);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership type '" + type + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * Get group UUID from the name of the group.
   * 
   * @param groupId
   *          The name of the group
   * @return The group UUId in the storage
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  private String getGroupUUID(String groupId) throws Exception {
    Session session = service.getStorageSession();
    try {
      String gPath = service.getStoragePath() + "/" + GroupHandlerImpl.STORAGE_EXO_GROUPS;
      return (groupId != null && groupId.length() != 0 && session.itemExists(gPath + groupId)
          ? ((Node) session.getItem(gPath + groupId)).getUUID()
          : null);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group '" + groupId + "'", e);
    } finally {
      session.logout();
    }

  }

  /**
   * Get membership type name from the UUID.
   * 
   * @param UUID
   *          The UUID of the group in the storage
   * @return The membership type name
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  private String getMembershipType(String UUID) throws Exception {
    Session session = service.getStorageSession();
    try {
      return session.getNodeByUUID(UUID).getName();

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find membership type by uuid " + UUID, e);
    } finally {
      session.logout();
    }
  }

  /**
   * Get groupId from the UUId.
   * 
   * @param UUID
   *          The UUID of the group in the storage
   * @return The groupId of the group
   * @throws Exception
   *           An exception is thrown if the method cannot access the database
   */
  private String getGroupId(String UUID) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node gNode = session.getNodeByUUID(UUID);
      return readStringProperty(gNode, GroupHandlerImpl.EXO_GROUP_ID);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group by uuid " + UUID, e);
    } finally {
      session.logout();
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
      String groupUUID = readStringProperty(node, EXO_GROUP);
      String membershipTypeUUID = readStringProperty(node, EXO_MEMBERSHIP_TYPE);

      String groupId = getGroupId(groupUUID);
      String membershipType = getMembershipType(membershipTypeUUID);
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
      String groupUUId = getGroupUUID(m.getGroupId());
      String membershipTypeUUId = getMembershipTypeUUID(m.getMembershipType());

      node.setProperty(EXO_GROUP, groupUUId);
      node.setProperty(EXO_MEMBERSHIP_TYPE, membershipTypeUUId);

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not write membership properties", e);
    }
  }

  /**
   * PreSave event.
   * 
   * @param membership
   *          The membership to save
   * @param isNew
   *          Is it new membership or not
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void preSave(Membership membership, boolean isNew) throws Exception {
    for (int i = 0; i < listeners.size(); i++) {
      MembershipEventListener listener = (MembershipEventListener) listeners.get(i);
      listener.preSave(membership, isNew);
    }
  }

  /**
   * PostSave event.
   * 
   * @param membership
   *          The membership to save
   * @param isNew
   *          Is it new membership or not
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void postSave(Membership membership, boolean isNew) throws Exception {
    for (int i = 0; i < listeners.size(); i++) {
      MembershipEventListener listener = (MembershipEventListener) listeners.get(i);
      listener.postSave(membership, isNew);
    }
  }

  /**
   * PreDelete event.
   * 
   * @param membership
   *          The membership to delete
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void preDelete(Membership membership) throws Exception {
    for (int i = 0; i < listeners.size(); i++) {
      MembershipEventListener listener = (MembershipEventListener) listeners.get(i);
      listener.preDelete(membership);
    }
  }

  /**
   * PostDelete event.
   * 
   * @param membership
   *          The membership to delete
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void postDelete(Membership membership) throws Exception {
    for (int i = 0; i < listeners.size(); i++) {
      MembershipEventListener listener = (MembershipEventListener) listeners.get(i);
      listener.postDelete(membership);
    }
  }
}
