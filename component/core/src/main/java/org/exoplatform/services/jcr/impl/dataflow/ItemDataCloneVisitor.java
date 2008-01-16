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
package org.exoplatform.services.jcr.impl.dataflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;

/**
 * The class visits each node, all subnodes and all of them properties. It
 * transfer as parameter of a method <code>ItemData.visits()</code>. During
 * visiting the class forms the <b>itemAddStates</b> list of
 * <code>List&lt;ItemState&gt;</code> for clone new nodes and their
 * properties and <b>ItemDeletedExistingStates</b> list for remove existing nodes 
 * if <code>removeExisting</code> is true.
 * 
 * @version $Id$
 */
public class ItemDataCloneVisitor extends DefaultItemDataCopyVisitor {

  private boolean                    removeExisting;

  /**
   * The list of deleted existing item states
   */
  protected List<ItemState>          itemDeletedExistingStates = new ArrayList<ItemState>();

  protected final SessionDataManager dstDataManager;

  private boolean                    deletedExistingPropery    = false;

  private final SessionChangesLog changes;

  /**
   * Creates an instance of this class.
   * 
   * @param parent - The parent node
   * @param dstNodeName Destination node name
   * @param nodeTypeManager - The NodeTypeManager
   * @param dataManager - Source data manager
   * @param dstDataManager - Destination data manager
   * @param removeExisting - If <code>removeExisting</code> is true and an
   *          existing node in this workspace (the destination workspace) has
   *          the same <code>UUID</code> as a node being cloned from
   *          srcWorkspace, then the incoming node takes precedence, and the
   *          existing node (and its subtree) is removed. If
   *          <code>removeExisting</code> is false then a <code>UUID</code>
   *          collision causes this method to throw a <b>ItemExistsException</b>
   *          and no changes are made.
   */
  public ItemDataCloneVisitor(NodeData parent, InternalQName dstNodeName,
      NodeTypeManagerImpl nodeTypeManager, SessionDataManager srcDataManager,
      SessionDataManager dstDataManager, boolean removeExisting, SessionChangesLog changes) {
    super(parent, dstNodeName, nodeTypeManager, srcDataManager, false);

    this.dstDataManager = dstDataManager;
    this.removeExisting = removeExisting;
    this.changes = changes;
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {

    boolean isMixReferenceable = ntManager.isNodeType(Constants.MIX_REFERENCEABLE, node
        .getPrimaryTypeName(), node.getMixinTypeNames());
    deletedExistingPropery = false;
    if (isMixReferenceable) {
      String identifier = node.getIdentifier();
      ItemImpl relItem = dstDataManager.getItemByIdentifier(identifier, false); // TODO pool=false
      
      ItemState changesItemState = null;
      if (changes != null) {
        changesItemState = changes.getItemState(identifier);
      }
      
      if (relItem != null && !(changesItemState != null && changesItemState.isDeleted())) {
        if (removeExisting) {
          deletedExistingPropery = true;
          itemDeletedExistingStates.add(new ItemState(relItem.getData(),
              ItemState.DELETED,
              true,
              dstDataManager.getItemByIdentifier(relItem.getParentIdentifier(), false).getInternalPath(),level != 0)); // TODO pool=false
        } else {
          throw new ItemExistsException("Item exists id = " + identifier + " name "
              + relItem.getName());
        }
      }
      keepIdentifiers = true;
    }

    super.entering(node, level);
    keepIdentifiers = false;
  }
  /**
   * Returns the list of item delete existing states
   */
  public List<ItemState> getItemDeletedExistingStates(boolean isInverse) {

    if (isInverse) {
      Collections.reverse(itemDeletedExistingStates);
    }
    return itemDeletedExistingStates;

  }

  /**
   * Return true if the itemstate for item with <code>itemId</code> UUId exist in
   * <code>List&lt;ItemState&gt;</code> list.
   * 
   * @param list
   * @param itemId
   * @param state
   * @return
   */
  private boolean itemInItemStateList(List<ItemState> list, String itemId, int state) {
    boolean retval = false;
    for (ItemState itemState : list) {
      if (itemState.getState() == state && itemState.getData().getIdentifier().equals(itemId)) {
        retval = true;
        break;
      }
    }
    return retval;
  };

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {

    if (deletedExistingPropery && removeExisting) {
      // if parent of this property in destination must be deleted, property
      // must be deleted too.
      if (itemInItemStateList(itemDeletedExistingStates, property.getParentIdentifier(),
          ItemState.DELETED)) {
        // search destination propery
        ItemData dstParentNodeData = dstDataManager.getItemByIdentifier(property.getParentIdentifier(), false) // TODO pool=false
            .getData();
        List<PropertyData> dstChildProperties = dstDataManager
            .getChildPropertiesData((NodeData) dstParentNodeData);
        PropertyData dstProperty = null;

        for (PropertyData propertyData : dstChildProperties) {
          if (propertyData.getQPath().getName().equals(property.getQPath().getName())) {
            dstProperty = propertyData;
            break;
          }
        }
        if (dstProperty != null) {
          itemDeletedExistingStates.add(new ItemState(dstProperty, ItemState.DELETED, true,
              dstDataManager.getItemByIdentifier(dstProperty.getParentIdentifier(), false).getInternalPath(),level != 0)); // TODO pool=false
        } else {
          throw new RepositoryException("Destination propery " + property.getQPath().getAsString()
              + " not found. ");
        }
      }
    }
    super.entering(property, level);
  }
}
