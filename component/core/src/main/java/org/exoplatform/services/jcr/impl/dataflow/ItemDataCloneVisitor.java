/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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

/**
 * The class visits each node, all subnodes and all of them properties. It
 * transfer as parameter of a method <code>ItemData.visits()</code>. During
 * visiting the class forms the <b>itemAddStates</b> list of
 * <code>List&lt;ItemState&gt;</code> for clone new nodes and their
 * properties and <b>ItemDeletedExistingStates</b> list for remove existing nodes 
 * if <code>removeExisting</code> is true.
 */
public class ItemDataCloneVisitor extends DefaultItemDataCopyVisitor {

  private boolean                    removeExisting;

  /**
   * The list of deleded existing item states
   */
  protected List<ItemState>          itemDeletedExistingStates = new ArrayList<ItemState>();

  protected final SessionDataManager dstDataManager;

  private boolean                    deletedExistingPropery    = false;

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
      SessionDataManager dstDataManager, boolean removeExisting) {
    super(parent, dstNodeName, nodeTypeManager, srcDataManager, false);

    this.dstDataManager = dstDataManager;
    this.removeExisting = removeExisting;
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {

    boolean isMixReferenceable = ntManager.isNodeType(Constants.MIX_REFERENCEABLE, node.getPrimaryTypeName(), node.getMixinTypeNames());
    deletedExistingPropery = false;
    if (isMixReferenceable) {
      String identifier = node.getIdentifier();
      ItemImpl relItem = dstDataManager.getItemByIdentifier(identifier, true);
      if (relItem != null) {
        if (removeExisting) {
          deletedExistingPropery = true;
          itemDeletedExistingStates.add(new ItemState(relItem.getData(), ItemState.DELETED, true,
              dstDataManager.getItemByIdentifier(relItem.getParentIdentifier(), true).getInternalPath(),level != 0));
        } else {
          throw new ItemExistsException("Item exists id = " + identifier + " name " + relItem.getName());
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
        ItemData dstParentNodeData = dstDataManager.getItemByIdentifier(property.getParentIdentifier(), true)
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
              dstDataManager.getItemByIdentifier(dstProperty.getParentIdentifier(), true).getInternalPath(),level != 0));
        } else {
          throw new RepositoryException("Destination propery " + property.getQPath().getAsString()
              + " not found. ");
        }
      }
    }
    super.entering(property, level);
  }
}
