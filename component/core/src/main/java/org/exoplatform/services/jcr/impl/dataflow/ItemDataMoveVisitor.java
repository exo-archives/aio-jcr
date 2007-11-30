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
import java.util.Stack;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.util.IdGenerator;

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
public class ItemDataMoveVisitor   extends ItemDataTraversingVisitor{
  /**
   * The list of added item states
   */
  protected List<ItemState> itemDeletedStates = new ArrayList<ItemState>();

  /**
   * Destination node name
   */
  private InternalQName         destNodeName;

  /**
   * The stack. In the top it contains a parent node.
   */
  protected Stack<NodeData>     parents;

  /**
   * The list of added item states
   */
  protected List<ItemState>     itemAddStates = new ArrayList<ItemState>();

  /**
   * The variable shows necessity of preservation <code>Identifier</code>, not
   * generate new one, at transformation of <code>Item</code>.
   */
  protected boolean             keepIdentifiers;

  /**
   * The NodeTypeManager
   */
  protected NodeTypeManagerImpl ntManager;

  
  protected QPath ancestorToSave = null;
  /**
   * Creates an instance of this class.
   * 
   * @param parent - The parent node
   * @param dstNodeName Destination node name
   * @param nodeTypeManager - The NodeTypeManager
   * @param srcDataManager - Source data manager
   * @param keepIdentifiers - Is it necessity to keep <code>Identifiers</code>
   */

  public ItemDataMoveVisitor(NodeData parent, InternalQName dstNodeName,
      NodeTypeManagerImpl nodeTypeManager, SessionDataManager srcDataManager, boolean keepIdentifiers) {
    super(srcDataManager);
    this.keepIdentifiers = keepIdentifiers;
    this.ntManager = nodeTypeManager;
    this.destNodeName = dstNodeName;

    this.parents = new Stack<NodeData>();
    this.parents.add(parent);
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    
    if (ancestorToSave == null){
      ancestorToSave = QPath.getCommonAncestorPath(curParent().getQPath(),node.getQPath());
    }

    NodeData parent = curParent();
    
    InternalQName qname = null;

    List<NodeData> existedChilds = dataManager.getChildNodesData(parent);
    int newIndex = 1;
    if (level == 0) {
      qname = destNodeName;
      // [PN] 12.01.07 Calculate SNS index for dest root
      for(NodeData child: existedChilds) {
        if (child.getQPath().getName().equals(qname)) {
          newIndex++; // next sibling index
        }
      }
    } else {
      qname = node.getQPath().getName();
      newIndex = node.getQPath().getIndex();
    }    
    
    // [PN] 05.01.07 Calc order number if parent supports orderable nodes...
    // If ordering is supported by the node type of the parent node of the new location, then the
    // newly moved node is appended to the end of the child node list.
    int orderNum = 0;
    if (ntManager.isOrderableChildNodesSupported(parent.getPrimaryTypeName(), parent.getMixinTypeNames())) {
      if (existedChilds.size() > 0)
        orderNum = existedChilds.get(existedChilds.size() - 1).getOrderNumber() + 1;  
    } else 
      orderNum = node.getOrderNumber(); // has no matter    

    String id = keepIdentifiers ? node.getIdentifier() : IdGenerator.generate();
    
    QPath qpath = QPath.makeChildPath(parent.getQPath(), qname, newIndex);
    
    TransientNodeData newNode = new TransientNodeData(
        qpath, id, -1, node.getPrimaryTypeName(),
        node.getMixinTypeNames(), orderNum, parent.getIdentifier(), node.getACL());
        
    parents.push(newNode);
    
    // ancestorToSave is a parent node
    //if level == 0 set internal createt as false for validating on save
    itemAddStates.add(new ItemState(newNode,
                                    ItemState.RENAMED,
                                    level == 0,
                                    ancestorToSave,
                                    false,
                                    level == 0));
    itemDeletedStates.add(new ItemState(node,
                                        ItemState.DELETED,
                                        level == 0,
                                        ancestorToSave,
                                        false,
                                        false));
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {

    InternalQName qname = property.getQPath().getName();

    List<ValueData> values;
    
    if (ntManager.isNodeType(Constants.MIX_REFERENCEABLE,
        curParent().getPrimaryTypeName(),
        curParent().getMixinTypeNames())
        && qname.equals(Constants.JCR_UUID)) {

      values = new ArrayList<ValueData>(1);
      values.add(new TransientValueData(curParent().getIdentifier()));
    } else {
      values = property.getValues();
    }
    
    TransientPropertyData newProperty = new TransientPropertyData(QPath
        .makeChildPath(curParent().getQPath(), qname),
        keepIdentifiers?property.getIdentifier():IdGenerator.generate(),
        -1,
        property.getType(),
        curParent().getIdentifier(),
        property.isMultiValued());
    
    newProperty.setValues(values);
    itemAddStates.add(new ItemState(newProperty,
                                    ItemState.RENAMED,
                                    false,
                                    ancestorToSave,
                                    false,
                                    false));
    
    // get last from super.entering(property, level)
    //ItemState copy = itemAddStates.get(itemAddStates.size() - 1);
    
    itemDeletedStates.add(new ItemState(property,
                                        ItemState.DELETED,
                                        false,
                                        ancestorToSave,
                                        false,
                                        false));
  }
  
  public List<ItemState> getAllStates() {
    List<ItemState> list  = getItemDeletedStates(true);
    list.addAll(getItemAddStates());
    return list;
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
  /**
   * Returns the current parent node
   */
  protected NodeData curParent() {
    return parents.peek();
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }

  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {
    parents.pop();
  }
  /**
   * Returns the list of item add states
   */
  public List<ItemState> getItemAddStates() {
    return itemAddStates;
  }
}
