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
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.SharedDataManager;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. Data Manager supported ACL Inheritance
 * 
 * @author Gennady Azarenkov
 * @version $Id: ACLInheritanceSupportedWorkspaceDataManager.java 8440
 *          2007-11-30 15:52:29Z svm $
 */
public class ACLInheritanceSupportedWorkspaceDataManager implements SharedDataManager {

  private static Log                          log = ExoLogger.getLogger("jcr.ACLInheritanceSupportedWorkspaceDataManager");

  private final CacheableWorkspaceDataManager persistentManager;

  public ACLInheritanceSupportedWorkspaceDataManager(CacheableWorkspaceDataManager persistentManager) {
    this.persistentManager = persistentManager;
  }

  /**
   * Traverse items parents in persistent storage for ACL containing parent.
   * Same work is made in SessionDataManager.getItemData(NodeData, QPathEntry[])
   * but for session scooped items.
   * 
   * @param data - item
   * @return - parent or null
   * @throws RepositoryException
   */
  private AccessControlList getNearestACAncestorAcl(ItemData data) throws RepositoryException {

    if (data.getParentIdentifier() != null) {
      NodeData parent = (NodeData) getItemData(data.getParentIdentifier());
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
  private ItemData initACL(NodeData parent, ItemData data) throws RepositoryException {
    if (data != null && data.isNode()) {
      NodeData nData = (NodeData) data;
      if (((NodeData) data).getACL() == null) {
        // ACL

        if (parent != null) {
          nData.setACL(parent.getACL());
        } else {
          // case of get by id
          nData.setACL(getNearestACAncestorAcl(data));
        }
      } else {

        boolean isMixOwnable = false;
        boolean isMixPrivilegeble = false;
        InternalQName[] mixinNames = nData.getMixinTypeNames();
        for (int i = 0; i < mixinNames.length; i++) {
          if (Constants.EXO_PRIVILEGEABLE.equals(mixinNames[i]))
            isMixPrivilegeble = true;
          else if (Constants.EXO_OWNEABLE.equals(mixinNames[i]))
            isMixOwnable = true;
        }
        // isMixOwnable or isMixPrivilegeble

        if (isMixOwnable ^ isMixPrivilegeble) {
          // case of get by id
          AccessControlList ancestorAcl = getNearestACAncestorAcl(data);
          String newOwner = isMixOwnable ? nData.getACL().getOwner() : ancestorAcl.getOwner();
          List<AccessControlEntry> newAccesssList = isMixPrivilegeble ? nData.getACL()
                                                                             .getPermissionEntries()
                                                                     : ancestorAcl.getPermissionEntries();

          if (newOwner == null || newAccesssList == null || newAccesssList.size() < 1)
            throw new RepositoryException("Invalid ACL " + newOwner + ":" + newAccesssList);

          nData.setACL(new AccessControlList(newOwner, newAccesssList));

        }

      }
    }

    return data;
  }

  // ------------ ItemDataConsumer impl ------------

  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException {
    List<NodeData> nodes = persistentManager.getChildNodesData(parent);
    for (NodeData node : nodes)
      initACL(parent, node);
    return nodes;
  }

  public ItemData getItemData(NodeData parent, QPathEntry name) throws RepositoryException {
    return initACL(parent, persistentManager.getItemData(parent, name));
  }

  public ItemData getItemData(String identifier) throws RepositoryException {
    return initACL(null, persistentManager.getItemData(identifier));
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
