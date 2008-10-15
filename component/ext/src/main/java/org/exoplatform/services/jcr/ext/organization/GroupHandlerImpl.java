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
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;

/**
 * Created by The eXo Platform SAS Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class GroupHandlerImpl extends CommonHandler implements GroupHandler {

  public static final String                 EXO_DESCRIPTION    = "exo:description";

  public static final String                 EXO_GROUP_ID       = "exo:groupId";

  public static final String                 EXO_LABEL          = "exo:label";

  public static final String                 EXO_PARENT_GROUP   = "exo:parentGroup";

  public static final String                 STORAGE_EXO_GROUPS = "exo:groups";

  protected final List<GroupEventListener>   listeners          = new ArrayList<GroupEventListener>();

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
    Session session = service.getStorageSession();

    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_GROUPS);
      Node gNode = storageNode.addNode(child.getGroupName());

      String parentId = (parent == null) ? null : parent.getId();
      Group group = new GroupImpl(child.getGroupName(), parentId, gNode.getUUID());
      group.setDescription(child.getDescription());
      group.setLabel(child.getLabel());

      writeObjectToNode(group, gNode);
      session.save();

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not add child group '" + child.getId() + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
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
    Session session = service.getStorageSession();

    try {
      String groupName = groupId.substring(groupId.lastIndexOf('/') + 1);
      Node gNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_GROUPS + "/"
          + groupName);
      return readObjectFromNode(gNode);

    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group '" + groupId + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupByMembership(String userName, String membershipType) throws Exception {
    Session session = service.getStorageSession();

    try {
      List<Group> types = new ArrayList<Group>();
      String mtUUID = (membershipType == null)
          ? null
          : ((Node) session.getItem(service.getStoragePath() + "/"
              + MembershipTypeHandlerImpl.STORAGE_EXO_MEMBERSHIP_TYPES + "/" + membershipType)).getUUID();
      String whereStatement = (mtUUID == null) ? "" : " where exo:membershipType='" + mtUUID + "'";

      // find memberships
      String mStatement = "select * from exo:userMembership" + whereStatement;
      Query mQuery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(mStatement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        Node uNode = mNode.getParent();

        // check user name
        if (uNode.getName().equals(userName)) {
          Node gNode = session.getNodeByUUID(readStringProperty(mNode,
                                                                MembershipHandlerImpl.EXO_GROUP));
          types.add(readObjectFromNode(gNode));
        }
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
  public Collection findGroups(Group parent) throws Exception {
    Session session = service.getStorageSession();

    try {
      List<Group> types = new ArrayList<Group>();
      String parentGroup = (parent == null)
          ? null
          : ((Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_GROUPS + "/"
              + parent.getGroupName())).getUUID();
      String whereStatement = (parentGroup == null) ? "" : " where exo:parentGroup='" + parentGroup
          + "'";

      String statement = "select * from exo:group" + whereStatement;
      Query query = service.getStorageSession()
                           .getWorkspace()
                           .getQueryManager()
                           .createQuery(statement, Query.SQL);
      QueryResult gRes = query.execute();
      for (NodeIterator gNodes = gRes.getNodes(); gNodes.hasNext();) {
        Node gNode = gNodes.nextNode();
        types.add(readObjectFromNode(gNode));
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

      Node storageNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_GROUPS);
      for (NodeIterator gNodes = storageNode.getNodes(); gNodes.hasNext();) {
        Node gNode = gNodes.nextNode();
        Group group = readObjectFromNode(gNode);
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
    Session session = service.getStorageSession();
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_GROUPS + "/"
          + group.getGroupName());

      // check child node
      String statement = "select * from exo:group where exo:parentGroup='" + gNode.getUUID() + "'";
      Query query = service.getStorageSession()
                           .getWorkspace()
                           .getQueryManager()
                           .createQuery(statement, Query.SQL);
      QueryResult res = query.execute();
      if (res.getNodes().hasNext()) {
        throw new OrganizationServiceException("Can not remove group '" + group.getGroupName()
            + "'. The group has a child group.");
      }

      // remove membership
      String mStatement = "select * from exo:userMembership where exo:group='" + gNode.getUUID()
          + "'";
      Query mQuery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(mStatement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        mNode.remove();
      }

      // remove group
      Group g = readObjectFromNode(gNode);
      gNode.remove();

      session.save();
      return g;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove group '" + group.getGroupName() + "'",
                                             e);
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
    Session session = service.getStorageSession();
    try {
      GroupImpl gImpl = (GroupImpl) group;
      if (gImpl.getUUId() == null) {
        throw new OrganizationServiceException("Can not find group for save changes because UUId is null.");
      }

      Node gNode = session.getNodeByUUID(gImpl.getUUId());
      String srcPath = gNode.getPath();
      int pos = srcPath.lastIndexOf('/');
      String prevName = srcPath.substring(pos + 1);
      String destPath = srcPath.substring(0, pos) + "/" + group.getGroupName();

      if (!prevName.equals(group.getGroupName())) {
        session.move(srcPath, destPath);
      }

      Node ngNode = (Node) session.getItem(destPath);
      writeObjectToNode(group, ngNode);
      session.save();

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not save group '" + group.getGroupName() + "'", e);
    } finally {
      session.logout();
    }
  }

  /**
   * Read group properties from node.
   * 
   * @param node
   *          The node in the storage to read from
   * @return The group
   * @throws Exception
   *           An exception is thrown if the method can not get access to the database
   */
  private Group readObjectFromNode(Node node) throws Exception {
    try {
      String groupId = readStringProperty(node, EXO_GROUP_ID);
      Group group = new GroupImpl(node.getName(),
                                  groupId.substring(0, groupId.lastIndexOf('/')),
                                  node.getUUID());
      group.setDescription(readStringProperty(node, EXO_DESCRIPTION));
      group.setLabel(readStringProperty(node, EXO_LABEL));
      return group;
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not read node properties", e);
    }
  }

  /**
   * Write group properties to the node in the storage.
   * 
   * @param group
   *          The group to write
   * @param node
   *          The node in the storage
   * @throws Exception
   *           An exception is thrown if the method can not get access to the database
   */
  private void writeObjectToNode(Group group, Node node) throws Exception {
    try {
      String parentGroup = (group.getParentId() == null ? null : node.getParent()
                                                                     .getNode(group.getParentId()
                                                                                   .substring(1))
                                                                     .getUUID());
      node.setProperty(EXO_LABEL, group.getLabel());
      node.setProperty(EXO_DESCRIPTION, group.getDescription());
      node.setProperty(EXO_GROUP_ID, group.getId());
      node.setProperty(EXO_PARENT_GROUP, parentGroup);
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not write node properties", e);
    }
  }
}
