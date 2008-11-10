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

import org.apache.commons.logging.Log;

import org.exoplatform.services.log.ExoLogger;
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

  /**
   * The group property that contain description.
   */
  public static final String                 EXO_DESCRIPTION    = "exo:description";

  /**
   * The group property that contain groupId.
   */
  public static final String                 EXO_GROUP_ID       = "exo:groupId";

  /**
   * The group property that contain parentId.
   */
  public static final String                 EXO_PARENT_ID      = "exo:parentId";

  /**
   * The group property that contain label.
   */
  public static final String                 EXO_LABEL          = "exo:label";

  /**
   * The node to storage groups.
   */
  public static final String                 STORAGE_EXO_GROUPS = "exo:groups";

  /**
   * The list of listeners to broadcast events.
   */
  protected final List<GroupEventListener>   listeners          = new ArrayList<GroupEventListener>();

  /**
   * Organization service implementation covering the handler.
   */
  protected final JCROrganizationServiceImpl service;

  /**
   * Log.
   */
  protected static Log                       log                = ExoLogger.getLogger("jcr.GroupHandlerImpl");

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
    Session session = service.getStorageSession();
    try {
      addChild(session, parent, child, broadcast);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addChild(Session session, Group parent, Group child, boolean broadcast) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("addChild started");
    }

    try {
      String parentId = (parent == null) ? "" : parent.getId();
      String parentPath = service.getStoragePath() + "/" + STORAGE_EXO_GROUPS + parentId;
      if (!session.itemExists(parentPath)) {
        return;
      }

      Node parentNode = (Node) session.getItem(parentPath);
      Node gNode = parentNode.addNode(child.getGroupName(), "exo:hierarchyGroup");

      Group group = new GroupImpl(child.getGroupName(), parentId, gNode.getUUID());
      group.setDescription(child.getDescription());
      group.setLabel(child.getLabel() != null ? child.getLabel() : child.getGroupName());

      if (broadcast) {
        preSave(child, true);
      }

      writeObjectToNode(group, gNode);
      session.save();

      if (broadcast) {
        postSave(child, true);
      }

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not add child group with groupId '"
          + child.getId() + "'", e);
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
    if (log.isDebugEnabled()) {
      log.debug("createGroup method");
    }

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
      return findGroupById(session, groupId);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Group findGroupById(Session session, String groupId) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("findGroupById started");
    }

    try {
      String gPath = service.getStoragePath() + "/" + STORAGE_EXO_GROUPS + groupId;
      if (!session.itemExists(gPath)) {
        return null;
      }

      Node gNode = (Node) session.getItem(gPath);
      Group g = readObjectFromNode(gNode);
      return g;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find group by groupId '" + groupId + "'", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupByMembership(String userName, String membershipType) throws Exception {
    Session session = service.getStorageSession();
    try {
      return findGroupByMembership(session, userName, membershipType);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupByMembership(Session session, String userName, String membershipType) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("findGroupByMembership started");
    }

    try {
      List<Group> types = new ArrayList<Group>();
      String mtUUID = (membershipType == null)
          ? null
          : ((Node) session.getItem(service.getStoragePath() + "/"
              + MembershipTypeHandlerImpl.STORAGE_EXO_MEMBERSHIP_TYPES + "/" + membershipType)).getUUID();
      String whereStatement = (mtUUID == null) ? "" : " where exo:membershipType='" + mtUUID + "'";

      // find memberships
      String mStatement = "select * from exo:userMembership" + whereStatement;
      Query mQuery = session.getWorkspace().getQueryManager().createQuery(mStatement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        Node uNode = mNode.getParent();

        // check user name
        if (uNode.getName().equals(userName)) {
          Node gNode = session.getNodeByUUID(readStringProperty(mNode,
                                                                MembershipHandlerImpl.EXO_GROUP));
          Group g = readObjectFromNode(gNode);
          types.add(g);
        }
      }

      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find groups", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroups(Group parent) throws Exception {
    Session session = service.getStorageSession();
    try {
      return findGroups(session, parent);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroups(Session session, Group parent) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("findGroups started");
    }

    try {
      List<Group> types = new ArrayList<Group>();

      String gPath = service.getStoragePath() + "/" + STORAGE_EXO_GROUPS;
      if (parent == null || session.itemExists(gPath + parent.getId())) {
        String parentId = parent == null ? "" : parent.getId();
        String whereStatement = "where exo:parentId "
            + (parentId.equals("") ? " is null" : " = '" + parentId + "'");
        String statement = "select * from exo:group " + whereStatement + " order by exo:groupId";
        Query query = session.getWorkspace().getQueryManager().createQuery(statement, Query.SQL);
        QueryResult gRes = query.execute();
        for (NodeIterator gNodes = gRes.getNodes(); gNodes.hasNext();) {
          Node gNode = gNodes.nextNode();
          Group g = readObjectFromNode(gNode);
          types.add(g);
        }
      }
      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not find groups", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection findGroupsOfUser(String user) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("findGroupsOfUser started");
    }

    return findGroupByMembership(user, null);
  }

  /**
   * {@inheritDoc}
   */
  public Collection getAllGroups() throws Exception {
    Session session = service.getStorageSession();
    try {
      return getAllGroups(session);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Collection getAllGroups(Session session) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("getAllGroups started");
    }

    try {
      List<Group> types = new ArrayList<Group>();

      String statement = "select * from exo:group order by exo:groupId";
      Query query = session.getWorkspace().getQueryManager().createQuery(statement, Query.SQL);
      QueryResult gRes = query.execute();
      for (NodeIterator gNodes = gRes.getNodes(); gNodes.hasNext();) {
        Node gNode = gNodes.nextNode();
        Group g = readObjectFromNode(gNode);
        types.add(g);
      }
      return types;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not get all groups ", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Group removeGroup(Group group, boolean broadcast) throws Exception {
    Session session = service.getStorageSession();
    try {
      return removeGroup(session, group, broadcast);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Group removeGroup(Session session, Group group, boolean broadcast) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("removeGroup started");
    }

    // TODO implement transaction
    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_GROUPS
          + group.getId());

      // remove child groups
      for (NodeIterator gNodes = gNode.getNodes(); gNodes.hasNext();) {
        removeGroup(readObjectFromNode(gNodes.nextNode()), true);
      }

      // remove membership
      String mStatement = "select * from exo:userMembership where exo:group='" + gNode.getUUID()
          + "'";
      Query mQuery = session.getWorkspace().getQueryManager().createQuery(mStatement, Query.SQL);
      QueryResult mRes = mQuery.execute();
      for (NodeIterator mNodes = mRes.getNodes(); mNodes.hasNext();) {
        Node mNode = mNodes.nextNode();
        service.getMembershipHandler().removeMembership(mNode.getUUID(), broadcast);
      }

      // remove group
      Group g = readObjectFromNode(gNode);

      if (broadcast) {
        preDelete(group);
      }

      gNode.remove();
      session.save();

      if (broadcast) {
        postDelete(group);
      }

      return g;

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not remove group with groupId '" + group.getId()
          + "'", e);
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
    Session session = service.getStorageSession();
    try {
      saveGroup(session, group, broadcast);
    } finally {
      session.logout();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveGroup(Session session, Group group, boolean broadcast) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("saveGroup started");
    }

    try {
      Node gNode = (Node) session.getItem(service.getStoragePath() + "/" + STORAGE_EXO_GROUPS
          + group.getId());

      if (broadcast) {
        preSave(group, false);
      }

      writeObjectToNode(group, gNode);
      session.save();

      if (broadcast) {
        postSave(group, false);
      }

    } catch (Exception e) {
      throw new OrganizationServiceException("Can not save group '" + group.getId() + "'", e);
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
      node.setProperty(EXO_LABEL, group.getLabel());
      node.setProperty(EXO_DESCRIPTION, group.getDescription());
      node.setProperty(EXO_GROUP_ID, group.getId());
      node.setProperty(EXO_PARENT_ID, group.getParentId());
    } catch (Exception e) {
      throw new OrganizationServiceException("Can not write node properties", e);
    }
  }

  /**
   * PreSave event.
   * 
   * @param group
   *          The group to save
   * @param isNew
   *          Is it new group or not
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void preSave(Group group, boolean isNew) throws Exception {
    for (GroupEventListener listener : listeners)
      listener.preSave(group, isNew);
  }

  /**
   * PostSave event.
   * 
   * @param group
   *          The group to save
   * @param isNew
   *          Is it new group or not
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void postSave(Group group, boolean isNew) throws Exception {
    for (GroupEventListener listener : listeners)
      listener.postSave(group, isNew);
  }

  /**
   * PreDelete event.
   * 
   * @param group
   *          The group to delete
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void preDelete(Group group) throws Exception {
    for (GroupEventListener listener : listeners)
      listener.preDelete(group);
  }

  /**
   * PostDelete event.
   * 
   * @param group
   *          The group to delete
   * @throws Exception
   *           If listeners fail to handle the user event
   */
  private void postDelete(Group group) throws Exception {
    for (GroupEventListener listener : listeners)
      listener.postDelete(group);
  }
}
