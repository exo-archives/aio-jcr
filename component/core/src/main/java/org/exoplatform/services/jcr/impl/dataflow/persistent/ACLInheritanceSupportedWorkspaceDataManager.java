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
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.Calendar;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.SharedDataManager;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. Data Manager supported ACL Inheritance
 * 
 * @author Gennady Azarenkov
 * @version $Id: ACLInheritanceSupportedWorkspaceDataManager.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class ACLInheritanceSupportedWorkspaceDataManager implements SharedDataManager {

  private static Log                          log = ExoLogger.getLogger("jcr.ACLInheritanceSupportedWorkspaceDataManager");

  protected final CacheableWorkspaceDataManager persistentManager;

  public ACLInheritanceSupportedWorkspaceDataManager(CacheableWorkspaceDataManager persistentManager) {
    this.persistentManager = persistentManager;
  }

  /**
   * Traverse items parents in persistent storage for ACL containing parent. Same work is made in
   * SessionDataManager.getItemData(NodeData, QPathEntry[]) but for session scooped items.
   * 
   * @param node - item
   * @return - parent or null
   * @throws RepositoryException
   */
  private AccessControlList getNearestACAncestorAcl(NodeData node) throws RepositoryException {

    if (node.getParentIdentifier() != null) {
      NodeData parent = (NodeData) getItemData(node.getParentIdentifier());
      while (parent != null) {
        if (parent.getACL() != null) {
          // has an AC parent
          return parent.getACL();
        }
        // going up to the root
        parent = (NodeData) getItemData(parent.getParentIdentifier());
      }
    }
    return new AccessControlList();
  }

  /**
   * @param parent - a parent, can be null (get item by id)
   * @param data - an item data
   * @return - an item data with ACL was initialized
   * @throws RepositoryException
   */
  private ItemData initACL(NodeData parent, NodeData node) throws RepositoryException {
    if (node != null) {
      AccessControlList acl = node.getACL();
      if (acl == null) {
        if (parent != null) {
          // use parent ACL
          node.setACL(parent.getACL());
        } else {
          // use nearest ancestor ACL... case of get by id
          node.setACL(getNearestACAncestorAcl(node));
        }
      } else if (!acl.hasPermissions()) {
        // use nearest ancestor permissions
        AccessControlList ancestorAcl = getNearestACAncestorAcl(node);
        node.setACL(new AccessControlList(acl.getOwner(), ancestorAcl.getPermissionEntries()));
      } else if (!acl.hasOwner()) {
        // use nearest ancestor owner
        AccessControlList ancestorAcl = getNearestACAncestorAcl(node);
        node.setACL(new AccessControlList(ancestorAcl.getOwner(), acl.getPermissionEntries()));
      }
    }

    return node;
  }

  // ------------ ItemDataConsumer impl ------------

  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException {
    final List<NodeData> nodes = persistentManager.getChildNodesData(parent);
    for (NodeData node : nodes)
      initACL(parent, node);
    return nodes;
  }

  public ItemData getItemData(NodeData parent, QPathEntry name) throws RepositoryException {
    final ItemData item = persistentManager.getItemData(parent, name);
    return item != null && item.isNode() ? initACL(parent, (NodeData) item) : item;
  }

  public ItemData getItemData(String identifier) throws RepositoryException {
    final ItemData item = persistentManager.getItemData(identifier);
    return item != null && item.isNode() ? initACL(null, (NodeData) item) : item;
  }

  public List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException {
    return persistentManager.getChildPropertiesData(parent);
  }

  public List<PropertyData> listChildPropertiesData(NodeData parent) throws RepositoryException {
    return persistentManager.listChildPropertiesData(parent);
  }

  public List<PropertyData> getReferencesData(String identifier, boolean skipVersionStorage) throws RepositoryException {
    return persistentManager.getReferencesData(identifier, skipVersionStorage);
  }

  // ------------ SharedDataManager ----------------------

  public void save(ItemStateChangesLog changes) throws InvalidItemStateException,
                                               UnsupportedOperationException,
                                               RepositoryException {
    persistentManager.save(changes);
  }

  public Calendar getCurrentTime() {
    return persistentManager.getCurrentTime();
  }
}
