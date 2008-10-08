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

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;

/**
 * Created by The eXo Platform SAS Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class GroupHandlerImpl extends CommonHandler implements GroupHandler {

  public static final String                 STORAGE_EXO_DESCRIPTION  = "exo:description";

  public static final String                 STORAGE_EXO_GROUP_ID     = "exo:groupId";

  public static final String                 STORAGE_EXO_GROUPS       = "/exo:groups";

  public static final String                 STORAGE_EXO_LABEL        = "exo:label";

  public static final String                 STORAGE_EXO_PARENT_GROUP = "exo:parentGroup";

  protected final List<GroupEventListener>   listeners                = new ArrayList<GroupEventListener>();

  /**
   * Organization service implementation covering the handler.
   */
  protected final JCROrganizationServiceImpl service;

  /**
   * GroupHandlerImpl constructor.
   * 
   * @param service
   *          The initialization data
   */
  GroupHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /**
   * {@inheritDoc}
   */
  public void addChild(Group parent, Group child, boolean broadcast) throws Exception {
    // TODO implement broadcast
    checkMandatoryProperties(child);

    Session session = service.getStorageSession();
    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS);
      Node gNode = storageNode.addNode(child.getGroupName());

      String parentId = (parent == null) ? "" : parent.getId();
      Group group = new GroupImpl(child.getGroupName(),
                                  parentId + "/" + child.getGroupName(),
                                  gNode.getUUID());
      group.setDescription(child.getDescription());
      group.setLabel(child.getLabel());
      writeObjectToNode(group, gNode);
    } catch (ItemExistsException e) {
      throw new OrganizationServiceException("Can not add child group. The group "
          + child.getGroupName() + " is exist", e);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not add child group ", e);
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to register a group event listener.
   * 
   * @param listener
   *          the group event listener instance.
   */
  public void addGroupEventListener(GroupEventListener listener) {
    listeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void createGroup(Group group, boolean broadcast) throws Exception {
    addChild(null, group, broadcast);
  }

  /**
   * {@inheritDoc}
   */
  public Group createGroupInstance() {
    return new GroupImpl();
  }

  /**
   * {@inheritDoc}
   */
  public Group findGroupById(String groupId) throws Exception {
    String groupName = groupId.substring(groupId.lastIndexOf('/') + 1);

    Session session = service.getStorageSession();
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS + "/"
          + groupName);
      Group group = (Group) readObjectFromNode(gNode);
      return group;
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Group " + groupName + " is absent", e);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group " + groupName, e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupByMembership(String userName, String membershipType) throws Exception {
    Session session = service.getStorageSession();
    List<Group> types = new ArrayList<Group>();

    try {
      Node mtNode = (Node) session.getItem(service.getStoragePath()
          + MembershipTypeHandlerImpl.STORAGE_EXO_MEMBERSHIP_TYPES.substring(1) + "/"
          + membershipType);

      // find memberships
      String mStatement = "select * from " + MembershipHandlerImpl.STORAGE_EXO_USER_MEMBERSHIP
          + " where " + MembershipHandlerImpl.STORAGE_EXO_MEMBERSHIP_TYPE + "=" + mtNode.getUUID();
      Query mQuery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(mStatement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();

        // find users
        String uStatement = "select * from " + MembershipHandlerImpl.STORAGE_EXO_USER_MEMBERSHIP
            + " where " + MembershipHandlerImpl.STORAGE_EXO_MEMBERSHIP_TYPE + "="
            + mtNode.getUUID();
        Query uQuery = service.getStorageSession()
                              .getWorkspace()
                              .getQueryManager()
                              .createQuery(uStatement, Query.SQL);
        QueryResult uRes = mQuery.execute();
        for (NodeIterator uNodes = uRes.getNodes(); uNodes.hasNext();) {
          Node uNode = uNodes.nextNode();

          // check username
          if (uNode.getName().equals(userName)) {
            Node gNode = session.getNodeByUUID(mNode.getProperty(MembershipHandlerImpl.STORAGE_EXO_GROUP)
                                                    .getString());
            types.add((Group) readObjectFromNode(gNode));
          }
        }
      }

      return types;

    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find membership type " + membershipType, e);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroups(Group parent) throws Exception {
    Session session = service.getStorageSession();

    String parentId = (parent == null) ? "" : parent.getId();

    try {
      List<Group> types = new ArrayList<Group>();

      String statement = "select * from " + STORAGE_EXO_GROUPS.substring(1) + " where "
          + STORAGE_EXO_GROUP_ID + "=" + parentId + "/jcr:name";
      Query query = service.getStorageSession()
                           .getWorkspace()
                           .getQueryManager()
                           .createQuery(statement, Query.SQL);
      QueryResult gRes = query.execute();
      for (NodeIterator gNodes = gRes.getNodes(); gNodes.hasNext();) {
        Node gNode = gNodes.nextNode();
        types.add((Group) readObjectFromNode(gNode));
      }

      return types;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find groups", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupsOfUser(String user) throws Exception {
    return findGroupByMembership(user, null);
  }

  /**
   * {@inheritDoc}
   */
  public Collection getAllGroups() throws Exception {

    Session session = service.getStorageSession();
    try {
      List<Group> types = new ArrayList<Group>();

      Node storageNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS);
      for (NodeIterator gNodes = storageNode.getNodes(); gNodes.hasNext();) {
        Node gNode = gNodes.nextNode();
        Group group = (Group) readObjectFromNode(gNode);
        types.add(group);
      }

      return types;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not get all groups ", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Group removeGroup(Group group, boolean broadcast) throws Exception {
    // TODO broadcast
    // TODO broadcast remove membership
    Session session = service.getStorageSession();
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS + "/"
          + group.getGroupName());

      // check child node
      String statement = "select * from " + STORAGE_EXO_GROUPS.substring(1) + " where "
          + STORAGE_EXO_PARENT_GROUP + "=" + group.getId();
      Query query = service.getStorageSession()
                           .getWorkspace()
                           .getQueryManager()
                           .createQuery(statement, Query.SQL);
      QueryResult res = query.execute();
      if (res.getNodes().hasNext()) {
        throw new OrganizationServiceException("Can not remove group " + group.getGroupName()
            + ". The group has a child group.");
      }

      // remove membership
      statement = "select * from " + MembershipHandlerImpl.STORAGE_EXO_USER_MEMBERSHIP + " where "
          + MembershipHandlerImpl.STORAGE_EXO_GROUP + "=" + gNode.getUUID();
      query = service.getStorageSession().getWorkspace().getQueryManager().createQuery(statement,
                                                                                       Query.SQL);
      res = query.execute();
      for (NodeIterator mNodes = res.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        mNode.remove();
      }

      // remove group
      Group g = (Group) readObjectFromNode(gNode);
      gNode.remove();

      session.save();
      return g;

    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not remove group. The group "
          + group.getGroupName() + " is absent", e);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove group " + group.getGroupName(), e);
    } finally {
      session.logout();
    }
  }

  /**
   * Remove registered listener.
   * 
   * @param listener
   *          The registered listener for removing
   * 
   */
  public void removeGroupEventListener(GroupEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void saveGroup(Group group, boolean broadcast) throws Exception {
    // TODO implement broadcast
    checkMandatoryProperties(group);

    Session session = service.getStorageSession();
    try {
      GroupImpl gImpl = (GroupImpl) group;
      if (gImpl.getUUId() == null) {
        throw new OrganizationServiceException("Can not find group for save changes because UUId is null.");
      }

      try {
        Node gNode = session.getNodeByUUID(gImpl.getUUId());
        String srcPath = gNode.getPath();
        int pos = srcPath.lastIndexOf('/');
        String prevName = srcPath.substring(pos + 1);
        String destPath = srcPath.substring(0, pos) + "/" + group.getGroupName();

        try {
          if (!prevName.equals(group.getGroupName())) {
            session.move(srcPath, destPath);
          }
          try {
            Node ngNode = (Node) session.getItem(destPath);
            writeObjectToNode(group, ngNode);
            session.save();
          } catch (PathNotFoundException e) {
            throw new OrganizationServiceException("The membership type " + group.getGroupName()
                + " is absent and can not be save", e);
          }
        } catch (PathNotFoundException e) {
          throw new OrganizationServiceException("The membership type " + prevName
              + " is absent and can not be save", e);
        } catch (ItemExistsException e) {
          throw new OrganizationServiceException("Can not save membership type " + prevName
              + " because new membership type " + group.getGroupName() + " is exist", e);
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

  @Override
  void checkMandatoryProperties(Object obj) throws Exception {
    Group group = (Group) obj;
    if (group.getGroupName() == null || group.getGroupName().length() == 0) {
      throw new OrganizationServiceException("The name of group can not be null or empty.");
    } else if (group.getLabel() == null || group.getLabel().length() == 0) {
      throw new OrganizationServiceException("The label of group can not be null or empty.");
    }
  }

  @Override
  Date readDateProperty(Node node, String prop) throws Exception {
    try {
      return node.getProperty(prop).getDate().getTime();
    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not read property " + prop, e);
    }
  }

  @Override
  Object readObjectFromNode(Node node) throws Exception {
    try {
      String groupId = readStringProperty(node, STORAGE_EXO_GROUP_ID);
      Group group = new GroupImpl(node.getName(), groupId, node.getUUID());
      group.setDescription(readStringProperty(node, STORAGE_EXO_DESCRIPTION));
      group.setLabel(readStringProperty(node, STORAGE_EXO_LABEL));
      return group;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not read node properties", e);
    }
  }

  @Override
  String readStringProperty(Node node, String prop) throws Exception {
    try {
      return node.getProperty(prop).getString();
    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not read property " + prop, e);
    }
  }

  @Override
  void writeObjectToNode(Object obj, Node node) throws Exception {
    Group group = (Group) obj;
    try {
      String parentGroup = (group.getParentId() == null ? null : node.getParent()
                                                                     .getNode(group.getParentId()
                                                                                   .substring(1))
                                                                     .getUUID());

      node.setProperty(STORAGE_EXO_LABEL, group.getLabel());
      node.setProperty(STORAGE_EXO_DESCRIPTION, group.getDescription());
      node.setProperty(STORAGE_EXO_GROUP_ID, group.getId());
      node.setProperty(STORAGE_EXO_PARENT_GROUP, parentGroup);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not write node properties", e);
    }
  }
}
