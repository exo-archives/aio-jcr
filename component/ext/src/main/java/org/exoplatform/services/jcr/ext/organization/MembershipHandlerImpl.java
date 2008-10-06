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
 * @version $Id: MembershipHandler.java 111 2008-11-11 11:11:11Z peterit $
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
   * 
   * @see org.exoplatform.services.organization.MembershipHandler# addMembershipEventListener
   *      (org.exoplatform .services.organization .MembershipEventListener)
   */
  public void addMembershipEventListener(MembershipEventListener listener) {
    listeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void removeMembershipEventListener(MembershipEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * @see org.exoplatform.services.organization.MembershipHandler#createMembership
   *      (org.exoplatform.services.organization.Membership, boolean)
   */
  public void createMembership(Membership m, boolean broadcast) throws Exception {
    // TODO Auto-generated method stub
  }

  /**
   * @see org.exoplatform.services.organization.MembershipHandler# createMembershipInstance()
   */
  public Membership createMembershipInstance() {
    return new MembershipImpl();
  }

  /**
   * @see org.exoplatform.services.organization.MembershipHandler#findMembership(java.lang.String)
   */
  public Membership findMembership(String id) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node mNode = session.getNodeByUUID(id);
      String groupId = mNode.getProperty(STORAGE_EXO_GROUP).toString();
      String membershipType = mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE).toString();
      Membership membership = new MembershipImpl(groupId, membershipType);
      return membership;
    } finally {
      session.logout();
    }
  }

  /**
   * @see org.exoplatform.services.organization.MembershipHandler#
   *      findMembershipByUserGroupAndType(java.lang.String, java.lang.String, java.lang.String)
   */
  public Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception {
    // TODO
    Membership membership = null;

    String statement = "select * from " + STORAGE_EXO_USER_MEMBERSHIP + " where "
        + STORAGE_EXO_GROUP + "=" + groupId + " AND " + STORAGE_EXO_MEMBERSHIP_TYPE + "=" + type;
    Query mquery = service.getStorageSession()
                          .getWorkspace()
                          .getQueryManager()
                          .createQuery(statement, Query.SQL);
    QueryResult mres = mquery.execute();
    for (NodeIterator mNodes = mres.getNodes(); mNodes.hasNext();) {
      Node uNode = mNodes.nextNode().getParent();
      if (uNode.getName().equals(userName)) {
        membership = new MembershipImpl(groupId, type);
        break;
      }
    }

    return membership;
  }

  /**
   * @see org.exoplatform.services.organization.MembershipHandler#findMembershipsByGroup
   *      (org.exoplatform.services.organization.Group)
   */
  public Collection findMembershipsByGroup(Group group) throws Exception {

    Session session = service.getStorageSession();

    try {
      List<Membership> types = new ArrayList<Membership>();

      String statement = "select * from " + STORAGE_EXO_USER_MEMBERSHIP;
      Query mquery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(statement, Query.SQL);
      QueryResult mres = mquery.execute();
      for (NodeIterator mNodes = mres.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        String groupId = mNode.getProperty(STORAGE_EXO_GROUP).toString();
        Node gNode = session.getNodeByUUID(groupId);
        if (gNode.getName().equals(group.getLabel())) {
          Membership membership = new MembershipImpl(groupId,
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
   * @see org.exoplatform.services.organization.MembershipHandler#findMembershipsByUser
   *      (java.lang.String)
   */
  public Collection findMembershipsByUser(String userName) throws Exception {
    Session session = service.getStorageSession();
    try {
      List<Membership> types = new ArrayList<Membership>();

      Node storageNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName + "/" + STORAGE_EXO_USER_MEMBERSHIP);

      for (NodeIterator nodes = storageNode.getNodes(); nodes.hasNext();) {
        Node mNode = nodes.nextNode();
        String groupId = mNode.getProperty(STORAGE_EXO_GROUP).toString();
        String membershipType = mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE).toString();
        Membership membership = new MembershipImpl(groupId, membershipType);
        types.add(membership);
      }

      return types;
    } finally {
      session.logout();
    }
  }

  /**
   * @see org.exoplatform.services.organization.MembershipHandler#
   *      findMembershipsByUserAndGroup(java. lang.String, java.lang.String)
   */
  public Collection findMembershipsByUserAndGroup(String userName, String groupId) throws Exception {
    Session session = service.getStorageSession();
    try {
      List<Membership> types = new ArrayList<Membership>();

      Node storageNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName + "/" + STORAGE_EXO_USER_MEMBERSHIP);

      for (NodeIterator nodes = storageNode.getNodes(); nodes.hasNext();) {
        Node mNode = nodes.nextNode();
        if (mNode.getProperty(STORAGE_EXO_GROUP).toString().equals(groupId)) {
          String membershipType = mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE).toString();
          Membership membership = new MembershipImpl(groupId, membershipType);
          types.add(membership);
        }
      }

      return types;
    } finally {
      session.logout();
    }
  }

  /**
   * @see org.exoplatform.services.organization.MembershipHandler#linkMembership(org.exoplatform.services.organization.User,
   *      org.exoplatform.services.organization.Group,
   *      org.exoplatform.services.organization.MembershipType, boolean)
   */
  public void linkMembership(User user, Group group, MembershipType m, boolean broadcast) throws Exception {
    // TODO Implement broadcast
    // TODO
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + user.getUserName() + "/"
          + STORAGE_EXO_USER_MEMBERSHIP);

    } finally {
      session.logout();
    }
  }

  /**
   * @see org.exoplatform.services.organization.MembershipHandler#removeMembership
   *      (java.lang.String, boolean)
   */
  public Membership removeMembership(String id, boolean broadcast) throws Exception {
    // TODO implement broadcast
    Session session = service.getStorageSession();
    try {
      Node mNode = session.getNodeByUUID(id);
      String groupId = mNode.getProperty(STORAGE_EXO_GROUP).toString();
      String membershipType = mNode.getProperty(STORAGE_EXO_MEMBERSHIP_TYPE).toString();
      Membership membership = new MembershipImpl(groupId, membershipType);
      mNode.remove();
      session.save();
      return membership;
    } finally {
      session.logout();
    }
  }

  /**
   * @see org.exoplatform.services.organization.MembershipHandler#removeMembershipByUser
   *      (java.lang.String, boolean)
   */
  public Collection removeMembershipByUser(String userName, boolean broadcast) throws Exception {
    // TODO broadcast
    Session session = service.getStorageSession();
    try {
      Node uNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName);
      Node mNode = uNode.getNode(STORAGE_EXO_USER_MEMBERSHIP);

      List<Membership> types = new ArrayList<Membership>();
      types = (List) findMembershipsByUser(userName);
      mNode.remove();
      uNode.addNode(STORAGE_EXO_USER_MEMBERSHIP);
      session.save();
      return types;
    } finally {
      session.logout();
    }
  }
}
