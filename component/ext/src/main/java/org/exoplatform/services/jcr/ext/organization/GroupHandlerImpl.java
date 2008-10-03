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

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 03.10.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: GroupHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class GroupHandlerImpl implements GroupHandler {

  public static final String                 STORAGE_EXO_GROUPS      = "/exo:groups";

  public static final String                 STORAGE_EXO_DESCRIPTION = "exo:description";

  protected final List<GroupEventListener>   listeners               = new ArrayList<GroupEventListener>();

  protected final JCROrganizationServiceImpl service;

  /**
   * GroupHandlerImpl constructor.
   * 
   * @param service
   *          organization service
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
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS
          + parent.getParentId() + "/" + parent.getLabel());

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
    // TODO This method not used.
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupByMembership(String userName, String membershipType) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroups(Group parent) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS
          + parent.getParentId());

      List<Group> types = new ArrayList<Group>();

      String label = "";
      String parentId = "";
      if (parent != null) {
        label = parent.getLabel();
        parentId = parent.getParentId();
      }

      for (NodeIterator nodes = storageNode.getNodes(label); nodes.hasNext();) {
        Node gNode = nodes.nextNode();
        Group group = new GroupImpl(gNode.getName(), parentId + '/' + label);
        group.setDescription(gNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());
        types.add(group);
      }

    } finally {

    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupsOfUser(String user) throws Exception {
    List<Group> groups = new ArrayList<Group>();
    String statement = "select * from " + UserHandlerImpl.STORAGE_EXO_USERS.substring(1) + ", "
        + " where " + UserHandlerImpl.STORAGE_EXO_USER_NAME + "=" + user + " AND ";

    return groups;
  }

  /**
   * {@inheritDoc}
   */
  public Collection getAllGroups() throws Exception {
    Session session = service.getStorageSession();
    try {
      Node storageNode = (Node) session.getItem(service.getStoragePath());

      List<Group> types = new ArrayList<Group>();

      for (NodeIterator nodes = storageNode.getNodes(STORAGE_EXO_GROUPS.substring(1)); nodes.hasNext();) {
        // types.add(group);
      }

      return types;
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Group removeGroup(Group group, boolean broadcast) throws Exception {
    // TODO broadcast
    // TODO remove membership
    Session session = service.getStorageSession();
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS + "/"
          + group.getLabel());

      NodeIterator groups = gNode.getNodes();
      if (groups.hasNext()) {
        throw new OrganizationServiceException("The group has a child group.");
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
    // TODO implement broadcast
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
