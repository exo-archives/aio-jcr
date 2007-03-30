/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;

/**
 * The class visits each node, all subnodes and all of them properties. It
 * transfer as parameter of a method <code>ItemData.visits()</code>. During
 * visiting the class forms the <b>itemAddStates</b> list of
 * <code>List&lt;ItemState&gt;</code> for copying new nodes and their
 * properties and <b>itemDeletedStates</b> for deleting existing nodes and
 * properties.
 */
public class ItemDataMoveVisitor extends ItemDataCopyVisitor {
  /**
   * The list of added item states
   */
  protected List<ItemState> itemDeletedStates = new ArrayList<ItemState>();
  
  //protected final InternalQPath rootParentPath;
  //protected boolean isRename = false;
  
  /**
   * Creates an instance of this class.
   * 
   * @param parent - The parent node
   * @param dstNodeName Destination node name
   * @param nodeTypeManager - The NodeTypeManager
   * @param dataManager - Source data manager
   * @param keepUUIDs - Is it necessity to keep <code>UUID</code>
   */

  public ItemDataMoveVisitor(NodeData parent, InternalQName dstNodeName,
      NodeTypeManagerImpl nodeTypeManager, SessionDataManager srcDataManager, boolean keepUUIDs) {
    super(parent, dstNodeName, nodeTypeManager, srcDataManager, keepUUIDs);
    
    //this.rootParentPath = parent.getQPath();
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    // [PN] Check if it's a rename operation
//    if (level == 0) {
//      this.isRename = node.getQPath().makeParentPath().equals(rootParentPath);
//    }
    
    if (ancestorToSave == null){
      ancestorToSave = InternalQPath.getPrimogenitorPath(curParent().getQPath(),node.getQPath());
    }

    super.entering(node, level);
    
    // [PN] 09.01.07 If it's rename then gen events for the src/dest roots only
    //if (isRename && level > 0) 
    if (level > 0)
      itemAddStates.get(itemAddStates.size() - 1).eraseEventFire();
    
    //itemDeletedStates.add(new ItemState(node, ItemState.DELETED, isRename ? level == 0 : true, ancestorToSave));
    itemDeletedStates.add(new ItemState(node, ItemState.DELETED, level == 0, ancestorToSave));
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    super.entering(property, level);
    
    List<ValueData> vals = property.getValues();
    for (ValueData valueData : vals) {
      ((TransientValueData) valueData).lock();
    }
    
    //if (isRename && level > 1) 
    if (level > 1)
      itemAddStates.get(itemAddStates.size() - 1).eraseEventFire();
    // [PN] 09.01.07 Fire if not rename, NOTE: level is 1 as root it's a node always (must be)
    //itemDeletedStates.add(new ItemState(property, ItemState.DELETED, isRename ? level == 1: true, ancestorToSave));
    itemDeletedStates.add(new ItemState(property, ItemState.DELETED, level == 1, ancestorToSave));
  }

  /**
   * Returns the list of item deleted states
   */
  public List<ItemState> getItemDeletedStates(boolean isInverse) {
    if (isInverse) {
      Collections.reverse(itemDeletedStates);
    }
    return itemDeletedStates;
  }

}
