/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.util.UUIDGenerator;

/**
 * The class visits each node, all subnodes and all of them properties. It
 * transfer as parameter of a method <code>ItemData.visits()</code>. During
 * visiting the class forms the <b>itemAddStates</b> list of
 * <code>List&lt;ItemState&gt;</code> for copying new nodes and their
 * properties.
 * 
* @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
* @version $Id: ItemDataCopyVisitor.java 13619 2007-03-21 13:36:51Z ksm $
 */

public class ItemDataCopyVisitor extends ItemDataTraversingVisitor {

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
   * The variable shows necessity of preservation <code>UUID</code>, not
   * generate new one, at transformation of <code>Item</code>.
   */
  protected boolean             keepUUIDs;

  /**
   * The NodeTypeManager
   */
  protected NodeTypeManagerImpl ntManager;

  protected InternalQPath ancestorToSave = null;
  /**
   * Creates an instance of this class.
   * 
   * @param parent - The parent node
   * @param destNodeName - Destination node name
   * @param nodeTypeManager - The NodeTypeManager
   * @param dataManager - Source data manager
   * @param keepUUIDs - Is it necessity to keep <code>UUID</code>
   */

  public ItemDataCopyVisitor(NodeData parent, InternalQName destNodeName,
      NodeTypeManagerImpl nodeTypeManager, SessionDataManager dataManager, boolean keepUUIDs) {
    super(dataManager);

    this.keepUUIDs = keepUUIDs;
    this.ntManager = nodeTypeManager;
    this.destNodeName = destNodeName;

    this.parents = new Stack<NodeData>();
    this.parents.add(parent);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor#entering(org.exoplatform.services.jcr.datamodel.PropertyData,
   *      int)
   */
  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {

    InternalQName qname = property.getQPath().getName();

    List<ValueData> values;
    if (ntManager.isNodeType(Constants.MIX_REFERENCEABLE,
        curParent().getPrimaryTypeName(),
        curParent().getMixinTypeNames())
        && qname.equals(Constants.JCR_UUID)) {

      values = new ArrayList<ValueData>(1);
      values.add(new TransientValueData(curParent().getUUID()));
    } else {
      values = property.getValues();
    }
    TransientPropertyData newProperty = new TransientPropertyData(InternalQPath
        .makeChildPath(curParent().getQPath(), qname),
        keepUUIDs?property.getUUID():UUIDGenerator.generate(),
        -1,
        property.getType(),
        curParent().getUUID(),
        property.isMultiValued());
    
    newProperty.setValues(values);
    itemAddStates
        .add(new ItemState(newProperty, ItemState.ADDED, true, ancestorToSave, level != 0));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor#entering(org.exoplatform.services.jcr.datamodel.NodeData,
   *      int)
   */
  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    
    if (ancestorToSave == null){
      ancestorToSave = curParent().getQPath();
    }
    
    InternalQName qname = null;

    List<NodeData> existedChilds = dataManager.getChildNodesData(curParent());
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

    TransientNodeData newNode = TransientNodeData.createNodeData(
        curParent(), 
        qname, 
        node.getPrimaryTypeName(), 
        newIndex);
    
    newNode.setMixinTypeNames(node.getMixinTypeNames());
    newNode.setACL(node.getACL());

    // [PN] 05.01.07 Calc order number if parent supports orderable nodes...
    // If ordering is supported by the node type of the parent node of the new location, then the
    // newly moved node is appended to the end of the child node list.
    if (ntManager.isOrderableChildNodesSupported(curParent().getPrimaryTypeName(), curParent().getMixinTypeNames())) {
      if (existedChilds.size() > 0)
        newNode.setOrderNumber(existedChilds.get(existedChilds.size() - 1).getOrderNumber() + 1);
      else
        newNode.setOrderNumber(0);  
    } else 
      newNode.setOrderNumber(node.getOrderNumber()); // has no matter
        
    if (keepUUIDs)
      newNode.setUUID(node.getUUID());
    else
      newNode.setUUID(UUIDGenerator.generate());

    parents.push(newNode);
    
    // ancestorToSave is a parent node
    //if level == 0 set internal createt as false for validating on save
    itemAddStates.add(new ItemState(newNode, ItemState.ADDED, true, ancestorToSave, level != 0));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor#leaving(org.exoplatform.services.jcr.datamodel.PropertyData,
   *      int)
   */
  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor#leaving(org.exoplatform.services.jcr.datamodel.NodeData,
   *      int)
   */
  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {
    parents.pop();
  }

  /**
   * Returns the current parent node
   */
  protected NodeData curParent() {
    return parents.peek();
  }

  /**
   * Returns the list of item add states
   */
  public List<ItemState> getItemAddStates() {
    return itemAddStates;
  }
}
