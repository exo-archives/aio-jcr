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
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id: GroupHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class GroupHandlerImpl implements GroupHandler {

  public static final String                 STORAGE_EXO_GROUPS      = "/exo:groups";

  public static final String                 STORAGE_EXO_DESCRIPTION = "exo:description";

  protected final List<GroupEventListener>   listeners               = new ArrayList<GroupEventListener>();

  protected final JCROrganizationServiceImpl service;

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
      String parentId = "";
      String label = "";
      if (parent != null) {
        parentId = parent.getParentId();
        label = parent.getLabel();
      }
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS + parentId
          + "/" + label);

      Node newNode = gNode.addNode(child.getLabel());
      newNode.setProperty(STORAGE_EXO_DESCRIPTION, child.getDescription());
      session.save();

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
    // TODO check this method
    Group group = null;
    Session session = service.getStorageSession();
    try {
      String parentId = "";
      String label = "";
      int pos = groupId.lastIndexOf('/');
      if (pos != -1) {
        parentId = groupId.substring(0, pos);
        label = groupId.substring(pos + 1);

        Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS + groupId);
        group = new GroupImpl(label, parentId);
        group.setDescription(gNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());
      }
      return group;
    } catch (PathNotFoundException e) {
      return group;
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupByMembership(String userName, String membershipType) throws Exception {
    // TODO Auto-generated method stub
    Session session = service.getStorageSession();
    try {
      List<Group> types = new ArrayList<Group>();
      Node uNode = (Node) session.getItem(service.getStoragePath()
          + UserHandlerImpl.STORAGE_EXO_USERS + userName);

      // Node mNode = uNode.getNode(UserHandlerImpl.STORAGE_EXO_MEMBERSHIP);

      return types;
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
      String label = "";
      String parentId = "";
      if (parent != null) {
        label = parent.getLabel();
        parentId = parent.getParentId();
      }

      List<Group> types = new ArrayList<Group>();
      Node storageNode = null;

      try {
        storageNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS
            + parentId);
      } catch (PathNotFoundException e) {
        return types;
      }

      for (NodeIterator nodes = storageNode.getNodes(label); nodes.hasNext();) {
        Node gNode = nodes.nextNode();
        Group group = new GroupImpl(gNode.getName(), parentId + '/' + label);
        group.setDescription(gNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());
        types.add(group);
      }

      return types;
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupsOfUser(String user) throws Exception {
    // TODO
    try {
    } finally {

    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Collection getAllGroups() throws Exception {
    try {
      List<Group> types = new ArrayList<Group>();
      String statement = "select * from " + STORAGE_EXO_GROUPS.substring(1);
      Query gquery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(statement, Query.SQL);
      QueryResult gres = gquery.execute();
      for (NodeIterator gNodes = gres.getNodes(); gNodes.hasNext();) {
        Node gNode = gNodes.nextNode();

        String path = gNode.getPath();
        int posF = path.indexOf(STORAGE_EXO_GROUPS) + STORAGE_EXO_GROUPS.length();
        int posL = path.lastIndexOf('/');

        Group group = new GroupImpl(gNode.getName(), path.substring(posF, posL - posF));
        group.setDescription(gNode.getProperties(STORAGE_EXO_DESCRIPTION).toString());
        types.add(group);
      }

      return types;
    } finally {
    }
  }

  /**
   * {@inheritDoc}
   */
  public Group removeGroup(Group group, boolean broadcast) throws Exception {
    // TODO broadcast
    Session session = service.getStorageSession();
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS
          + group.getParentId() + "/" + group.getLabel());

      NodeIterator groups = gNode.getNodes();
      if (groups.hasNext()) {
        throw new OrganizationServiceException("The group has a child group.");
      }

      String statement = "select * from " + MembershipHandlerImpl.STORAGE_EXO_USER_MEMBERSHIP
          + " where " + MembershipHandlerImpl.STORAGE_EXO_GROUP + "=" + gNode.getUUID();
      Query gquery = service.getStorageSession()
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(statement, Query.SQL);
      QueryResult gres = gquery.execute();
      for (NodeIterator mNodes = gres.getNodes(); mNodes.hasNext();) {
        mNodes.nextNode().remove();
      }

      gNode.remove();
      session.save();
      return group;
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeGroupEventListener(GroupEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void saveGroup(Group group, boolean broadcast) throws Exception {
    // TODO: broadcast
    Session session = service.getStorageSession();
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS
          + group.getParentId() + "/" + group.getLabel());
      gNode.setProperty(STORAGE_EXO_DESCRIPTION, group.getDescription());
      session.save();
    } finally {
      session.logout();
    }
  }
}
