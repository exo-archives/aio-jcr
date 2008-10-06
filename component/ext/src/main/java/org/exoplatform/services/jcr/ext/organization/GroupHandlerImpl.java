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
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.GroupHandler;

/**
 * Created by The eXo Platform SAS Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: GroupHandlerImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class GroupHandlerImpl implements GroupHandler {

  public static final String                 STORAGE_EXO_GROUPS      = "/exo:groups";

  public static final String                 STORAGE_EXO_DESCRIPTION = "exo:description";

  public static final String                 STORAGE_EXO_LABEL       = "exo:label";

  protected final List<GroupEventListener>   listeners               = new ArrayList<GroupEventListener>();

  protected final JCROrganizationServiceImpl service;

  GroupHandlerImpl(JCROrganizationServiceImpl service) {
    this.service = service;
  }

  /**
   * Use this method to create a new group. The developer should call createGroupInstance() method
   * to create a group instance, initialize the group properties such owner , label.. and then call
   * this method to persist the group. Use this method only when you are creating a new group. If
   * you want to update a group , use the saveGroup(..) method.
   * 
   * @param parent
   *          The parent group of the new group. use 'null' if you want to create the group at the
   *          root level.
   * @param child
   *          The group that you want to create.
   * @param broadcast
   *          Broacast the new group event to all the registered listener if broadcast is true
   * @throws Exception
   *           An exception is throwed if the method fail to persist the new group or there is
   *           already one child group with the same group name in the database or any registered
   *           listener fail to handle the event.
   */
  public void addChild(Group parent, Group child, boolean broadcast) throws Exception {
    // TODO implement broadcast
    String parentId = "";
    String label = "";
    if (parent != null) {
      parentId = parent.getParentId();
      label = parent.getLabel();
    }

    Session session = service.getStorageSession();
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS + parentId
          + "/" + label);
      try {
        Node newNode = gNode.addNode(child.getLabel());
        newNode.setProperty(STORAGE_EXO_DESCRIPTION, child.getDescription());
        newNode.setProperty(STORAGE_EXO_LABEL, child.getLabel());
        session.save();
      } finally {
      }
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find parent group " + parentId + "/" + label);
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to register a group event listener
   * 
   * @param listener
   *          the group event listener instance.
   */
  public void addGroupEventListener(GroupEventListener listener) {
    listeners.add(listener);
  }

  /**
   * @deprecated This method should not be used , use the addChild(..) method and pass the null as
   *             the parent if you want to add the group to the root level.
   */
  public void createGroup(Group group, boolean broadcast) throws Exception {
    addChild(null, group, broadcast);
  }

  /**
   * @return a new object instance that implement the Group interface
   */
  public Group createGroupInstance() {
    return new GroupImpl();
  }

  /**
   * Use this method to search for a group
   * 
   * @param groupId
   *          the id of the group that you want to search for
   * @return null if no record matched the group id or the found group
   * @throws Exception
   *           An exception is throwed if the method cannot access the database or more than one
   *           group is found.
   */
  public Group findGroupById(String groupId) throws Exception {
    Session session = service.getStorageSession();
    try {
      Node gNode = session.getNodeByUUID(groupId);

      String path = gNode.getPath();
      int posF = path.indexOf(STORAGE_EXO_GROUPS) + STORAGE_EXO_GROUPS.length();
      int posL = path.lastIndexOf('/');
      Group group = new GroupImpl(gNode.getName(), path.substring(posF, posL - posF));
      group.setDescription(gNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());
      group.setLabel(gNode.getProperty(STORAGE_EXO_LABEL).getString());
      return group;
    } catch (ItemNotFoundException e) {
      throw new OrganizationServiceException("Can not find group by UUId " + groupId);
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to find all the groups of an user with the specified membership type
   * 
   * @param userName
   *          The user that the method should search for.
   * @param membershipType
   *          The type of the membership. Since an user can have one or more membership in a group,
   *          this parameter is necessary. If the membershipType is null, it should mean any
   *          membership type.
   * @return A collection of the found groups
   * @throws Exception
   *           An exception is throwed if the method cannot access the database. TODO currently the
   *           implementation should not handle the case of membershipType is null. Also we should
   *           merge this method with the findGroupsOfUser method.
   */
  public Collection findGroupByMembership(String userName, String membershipType) throws Exception {
    // TODO Auto-generated method stub
    Session session = service.getStorageSession();
    List<Group> types = new ArrayList<Group>();
    try {
      String uPath = service.getStoragePath() + UserHandlerImpl.STORAGE_EXO_USERS + "/" + userName;
      Node uNode = (Node) session.getItem(uPath);

      // TODO check STORAGE_EXO_MEMBERSHIP
      Node storagePath = uNode.getNode(UserHandlerImpl.STORAGE_EXO_MEMBERSHIP);
      for (NodeIterator mNodes = storagePath.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        if (membershipType == null) {

        }

      }
      // Node mNode = uNode.getNode(UserHandlerImpl.);

      return types;
    } catch (PathNotFoundException e) {
      return types;
    } finally {
      session.logout();
    }
  }

  /**
   * Use this method to find all the children group of a group.
   * 
   * @param parent
   *          The group that you want to search. Use null if you want to search from the root.
   * @return A collection of the children group
   * @throws Exception
   *           An exception is throwed is the method cannot access the database
   */
  public Collection findGroups(Group parent) throws Exception {
    Session session = service.getStorageSession();

    String parentLabel = "";
    String parentId = "";
    if (parent != null) {
      parentLabel = "/" + parent.getLabel();
      parentId = parent.getParentId();
    }

    try {
      List<Group> types = new ArrayList<Group>();

      Node storageNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS
          + parentId + "/" + parentLabel);

      for (NodeIterator nodes = storageNode.getNodes(); nodes.hasNext();) {
        Node gNode = nodes.nextNode();
        Group group = new GroupImpl(gNode.getName(), parentId + parentLabel);
        group.setDescription(gNode.getProperty(STORAGE_EXO_DESCRIPTION).getString());
        group.setLabel(gNode.getProperty(STORAGE_EXO_LABEL).getString());
        types.add(group);
      }

      return types;
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find parent node " + parentId + parentLabel);
    } finally {
      session.logout();
    }
  }

  /**
   * use this method to look all the group that the user has at least one membership.
   * 
   * @param user
   *          The username of the user
   * @return A collection of the found group. The return collection cannot be null, but it can be
   *         empty if no group is found.
   * @throws Exception
   *           An exception is throwed if the method cannot access the database.
   */
  public Collection findGroupsOfUser(String user) throws Exception {
    return findGroupByMembership(user, null);
  }

  /**
   * Use this method to get all the groups. But the third party should not use this method
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
        group.setLabel(gNode.getProperties(STORAGE_EXO_LABEL).toString());
        types.add(group);
      }

      return types;
    } finally {
    }
  }

  /**
   * Use this method to remove a group from the group database. If the group has the children group.
   * The method should not remove the group and throw and exception
   * 
   * @param group
   *          The group to be removed. The group parameter should be obtained form the
   *          findGroupId(..) method. When the groupn is removed, the memberships of the group
   *          should be removed as well.
   * @param broadcast
   *          Broadcast the event to the registered listener if the broadcast value is 'true'
   * @return Return the removed group.
   * @throws Exception
   *           An exception is throwed if the method fail to remove the group from the database, the
   *           group is not existed in the database, or any listener fail to handle the event. TODO
   *           Currently the implementation simply remove the children group without broadcasting
   *           the event. We should add the parameter 'recursive' to the parameter list so the third
   *           party can have more control. Also should we broadcast the membership remove event
   */
  public Group removeGroup(Group group, boolean broadcast) throws Exception {
    // TODO broadcast
    Session session = service.getStorageSession();
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS
          + group.getParentId() + "/" + group.getLabel());

      try {
        NodeIterator groups = gNode.getNodes();
        if (groups.hasNext()) {
          throw new OrganizationServiceException("The group " + group.getParentId() + "/"
              + group.getLabel() + " has a child group.");
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
      }
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find group " + group.getParentId() + "/"
          + group.getLabel() + " for remove");
    } finally {
      session.logout();
    }
  }

  /**
   * Remove registered listener
   * 
   * @param listener
   *          The registered listener for removing
   * 
   */
  public void removeGroupEventListener(GroupEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * Use this method to update the properties of an existed group. Usually you should use the method
   * findGroupById(..) to find the group, use the methods set to change the data of the group and
   * then call this method to persisted the updated information. You should not call this method
   * with the group instance you get from the createGroupInstance()
   * 
   * @param group
   *          The group object with the updated information.
   * @param broadcast
   *          Broadcast the event to all the registered listener if the broadcast value is true
   * @throws Exception
   *           An exception is thorwed if the method cannot access the database or any listener fail
   *           to handle the event
   */
  public void saveGroup(Group group, boolean broadcast) throws Exception {
    // TODO: broadcast
    Session session = service.getStorageSession();
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + STORAGE_EXO_GROUPS
          + group.getParentId() + "/" + group.getLabel());
      try {
        gNode.setProperty(STORAGE_EXO_DESCRIPTION, group.getDescription());
        gNode.setProperty(STORAGE_EXO_LABEL, group.getLabel());
        session.save();
      } finally {
      }
    } catch (PathNotFoundException e) {
      throw new OrganizationServiceException("Can not find group " + group.getParentId() + "/"
          + group.getLabel() + " for save changes.");
    } finally {
      session.logout();
    }
  }
}
