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
import org.exoplatform.services.jcr.datamodel.QPath;
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
 * 
 * @version $Id$
 */
public class ItemDataMoveVisitor extends DefaultItemDataCopyVisitor {
  /**
   * The list of added item states
   */
  protected List<ItemState> itemDeletedStates = new ArrayList<ItemState>();
  
  /**
   * Creates an instance of this class.
   * 
   * @param parent - The parent node
   * @param dstNodeName Destination node name
   * @param nodeTypeManager - The NodeTypeManager
   * @param dataManager - Source data manager
   * @param keepIdentifiers - Is it necessity to keep <code>Identifiers</code>
   */

  public ItemDataMoveVisitor(NodeData parent, InternalQName dstNodeName,
      NodeTypeManagerImpl nodeTypeManager, SessionDataManager srcDataManager, boolean keepIdentifiers) {
    super(parent, dstNodeName, nodeTypeManager, srcDataManager, keepIdentifiers);
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    
    if (ancestorToSave == null){
      ancestorToSave = QPath.getPrimogenitorPath(curParent().getQPath(),node.getQPath());
    }

    super.entering(node, level);
    
    if (level > 0)
      itemAddStates.get(itemAddStates.size() - 1).eraseEventFire();
    
    itemDeletedStates.add(new ItemState(node, ItemState.DELETED, level == 0, ancestorToSave));
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    super.entering(property, level);
    
    List<ValueData> vals = property.getValues();
    for (ValueData valueData : vals) {
      ((TransientValueData) valueData).lock();
    }
    
    if (level > 1)
      itemAddStates.get(itemAddStates.size() - 1).eraseEventFire();
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
